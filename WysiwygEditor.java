import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.HTMLEditorKit.HTMLTextAction;
import javax.swing.undo.*;

import static javax.swing.JComponent.WHEN_FOCUSED;

public class WysiwygEditor extends JTextPane implements Editor {
  private Note note;
  private UnsavedChangesTracker unsavedChangesTracker;

  public WysiwygEditor() {
    try {
      var editorKit = new HTMLEditorKit();
      editorKit.setDefaultCursor(new Cursor(Cursor.TEXT_CURSOR));

      //var rules = editorKit.getStyleSheet().getStyleNames();
      //while (rules.hasMoreElements()) {
      //    var name = (String) rules.nextElement();
      //    var rule = editorKit.getStyleSheet().getStyle(name);
      //    System.out.println(rule.toString());
      //}

      editorKit.setStyleSheet(Config.stylesheet());
      setEditorKit(editorKit);

      //System.out.println(
      //    Arrays.stream(getActionMap().allKeys())
      //      .map(Object::toString)
      //      .sorted()
      //      .collect(Collectors.joining(System.lineSeparator())));

      var undoManager = new UndoManager();
      getDocument().addUndoableEditListener(undoManager);
      
      unsavedChangesTracker = new UnsavedChangesTracker();
      getDocument().addDocumentListener(unsavedChangesTracker);

      ((HTMLDocument) getDocument()).setBase(Paths.get(Config.notes()).toFile().toURI().toURL());

      addMouseListener(followHyperlinks());
    

      getInputMap().put(KeyStrokes.CTRL_B, new StyledEditorKit.BoldAction());
      getInputMap().put(KeyStrokes.CTRL_I, new StyledEditorKit.ItalicAction());
      getInputMap().put(KeyStrokes.CTRL_U, new StyledEditorKit.UnderlineAction());
      getInputMap().put(KeyStrokes.CTRL_1, new HeadingAction(1));
      getInputMap().put(KeyStrokes.CTRL_2, new HeadingAction(2));
      getInputMap().put(KeyStrokes.CTRL_3, new HeadingAction(3));
      getInputMap().put(KeyStrokes.CTRL_4, new HeadingAction(4));
      getInputMap().put(KeyStrokes.CTRL_5, new HeadingAction(5));
      getInputMap().put(KeyStrokes.CTRL_6, new HeadingAction(6));
      getInputMap().put(KeyStrokes.SHIFT_ENTER, new LineBreakAction());
      getInputMap().put(
          KeyStrokes.CTRL_SHIFT_C, 
          new HTMLEditorKit.InsertHTMLTextAction(
            "InsertCodeBlockAction", 
            "<pre></pre>", 
            HTML.Tag.BODY, 
            HTML.Tag.PRE));
      getInputMap().put(
          KeyStrokes.CTRL_SHIFT_I,
          new HTMLEditorKit.InsertHTMLTextAction(
            "InsertInfoBlockAction", 
            "<div class=\"info\"></div>", 
            HTML.Tag.BODY, 
            HTML.Tag.DIV));
      getInputMap().put(
          KeyStrokes.CTRL_SHIFT_W,
          new HTMLEditorKit.InsertHTMLTextAction(
            "InsertWarnBlockAction", 
            "<div class=\"warn\"></div>", 
            HTML.Tag.BODY, 
            HTML.Tag.DIV));

      registerKeyboardAction(pasteFromClipboard(), KeyStrokes.CTRL_V, WHEN_FOCUSED);
      //registerKeyboardAction(continueNext(), KeyStrokes.ENTER, WHEN_FOCUSED);
      //registerKeyboardAction(insertBreak(), KeyStrokes.SHIFT_ENTER, WHEN_FOCUSED);
      registerKeyboardAction(insertUnorderedList(), KeyStrokes.CTRL_SHIFT_U, WHEN_FOCUSED);
      registerKeyboardAction(insertOrderedList(), KeyStrokes.CTRL_SHIFT_O, WHEN_FOCUSED);
      registerKeyboardAction(insertTable(), KeyStrokes.CTRL_SHIFT_T, WHEN_FOCUSED);
      registerKeyboardAction(indent(), KeyStrokes.TAB, WHEN_FOCUSED);
      registerKeyboardAction(unindent(), KeyStrokes.SHIFT_TAB, WHEN_FOCUSED);
      registerKeyboardAction(toParagraph(), KeyStrokes.CTRL_0, WHEN_FOCUSED);
      registerKeyboardAction(save(), KeyStrokes.CTRL_S, WHEN_FOCUSED);
      registerKeyboardAction(refresh(), KeyStrokes.CTRL_R, WHEN_FOCUSED);
      registerKeyboardAction(undo(undoManager), KeyStrokes.CTRL_Z, WHEN_FOCUSED);
      registerKeyboardAction(redo(undoManager), KeyStrokes.CTRL_Y, WHEN_FOCUSED);
      //registerKeyboardAction(backspace(), KeyStrokes.BACKSPACE, WHEN_FOCUSED);
      registerKeyboardAction(debug(), KeyStrokes.F12, WHEN_FOCUSED);
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public void search(String searchString) {
    Editor.search(this, searchString);
  }

  public void openNote(Note note) {
    if (hasUnsavedChanges() && MessageDialogs.unsavedChanges(this) != 0) {
      return;
    }
    this.note = note;
    setText(Pandoc.markdownToHtml(note));
    unsavedChangesTracker.reset();
  }

  public boolean hasUnsavedChanges() {
    return unsavedChangesTracker.hasUnsavedChanges();
  }

  private void resetCaret() {
    setCaretPosition(getCaretPosition());
  }

  private ActionListener pasteFromClipboard() {
    return event -> {
      try {
        var content = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (content == null) {
          return;
        }
        if(!content.isDataFlavorSupported(DataFlavor.imageFlavor)) {
          var string = (String) content.getTransferData(DataFlavor.stringFlavor);
          getDocument().insertString(getCaretPosition(), string, null);
          return;
        }
        var image = (BufferedImage) content.getTransferData(DataFlavor.imageFlavor);
        var attachments = Paths.get(Config.attachments());
        var imageFileName = note.getFullName() + "_" + System.currentTimeMillis() + ".png";
        var imageFile = Paths.get(attachments.toString(), imageFileName);
        ImageIO.write(image, "png", imageFile.toFile());
        var html = "<p><img src=\"../attachments/"+imageFileName+"\"></p>";
        addToBody(html);
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }

  private ActionListener insertUnorderedList() {
    return event ->  {
      addToBody("<ul><li></li></ul>");
    };
  }
  
  private ActionListener insertOrderedList() {
    return event ->  {
      addToBody("<ol><li></li></ol>");
    };
  }

  private ActionListener insertBreak() {
    return event -> {
      try {
        var document = (HTMLDocument) getDocument();
        var editorKit = (HTMLEditorKit) getEditorKit();
        editorKit.insertHTML(document, getCaretPosition(), "<br>", 0, 0, HTML.Tag.BR);
        resetCaret();
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }

  private ActionListener insertInfoBlock() {
    return event -> {
      addToBody("<div class=\"info\"><p></p></div>");
    };
  }
  
  private ActionListener insertWarnBlock() {
    return event -> {
      addToBody("<div class=\"warn\"><p></p></div>");
    };
  }

  private ActionListener insertCodeBlock() {
    return event -> {
      try {
        var document = (HTMLDocument) getDocument();
        var editorKit = (HTMLEditorKit) getEditorKit();
        var selectedText = getSelectedText();
        if (selectedText != null && !selectedText.isBlank()) {
          document.remove(getSelectionStart(), selectedText.length());
          var html = "<code>" + selectedText + "</code>";
          editorKit.insertHTML(document, getCaretPosition(), html, 0, 0, HTML.Tag.CODE);
          resetCaret();
        } else {
          addToBody("<pre><code></code></pre>");
        }
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }

  private ActionListener insertTable() {
    return event -> {
      var colSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 100, 1));
      var rowSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 100, 1));
      
      Object[] message = {
        "Columns:", colSpinner,
        "Rows:   ", rowSpinner
      };

      var option = 
        JOptionPane.showConfirmDialog(
            App.instance(), 
            message, 
            "Insert table", 
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

      if (option != JOptionPane.OK_OPTION) {
        return;
      }

      var cols = (int) colSpinner.getValue();
      var rows = (int) rowSpinner.getValue();
     
      var sb = new StringBuilder();
      sb.append("<table>");
      sb.append("<thead>");
      sb.append("<tr>");
      for (int col = 0; col < cols; col++) {
        sb.append("<th></th>");
      }
      sb.append("</tr>");
      sb.append("</thead>");
      sb.append("<tbody>");
      for (int row = 0; row < rows; row++) {
        sb.append("<tr>");
        for (int col = 0; col < cols; col++) {
          sb.append("<td></td>");
        }
        sb.append("</tr>");
      }
      sb.append("</tbody>");
      sb.append("</table>");

      addToBody(sb.toString());
    };
  }

  private ActionListener continueNext() {
    return event -> {
      try {
        var document = (HTMLDocument) getDocument();
        var element = document.getParagraphElement(getCaretPosition()).getParentElement();
        if (element != null) {
          var text = getElementText(element);
          if ("li".equals(getElementName(element))) {
            if (!text.isBlank()) {
              document.insertAfterEnd(element, "<li></li>");
              setCaretPosition(element.getEndOffset());
              return;
            } else {
              document.removeElement(element);
            }
          }
        }
        addToBody("<p></p>");
      } catch (Exception exc)  {
        throw new RuntimeException(exc);
      }
    };
  }

  private String getElementName(Element element) {
    return element.getAttributes().getAttribute(AttributeSet.NameAttribute).toString();
  }

  private String getElementText(Element element) {
    try {
      var document = (HTMLDocument) getDocument();
      var length = element.getEndOffset() - element.getStartOffset();
      return document.getText(element.getStartOffset(), length);
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  private void addToBody(String html) {
    try {
      var document = (HTMLDocument) getDocument();
      var element = document.getParagraphElement(getCaretPosition());
      while (!"body".equals(getElementName(element.getParentElement()))) {
        element = element.getParentElement();
      }
      document.insertAfterEnd(element, html);
      setCaretPosition(element.getEndOffset());
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  private ActionListener toParagraph() {
    return event -> {
      var document = (HTMLDocument) getDocument();
      var element = document.getParagraphElement(getCaretPosition());
      var parent = element.getParentElement();
      if (!"body".equals(getElementName(parent))) {
        return;
      }
      replaceTag("<p>", "</p>");
    };
  }

  private ActionListener toHeading(int level) {
    return event -> replaceTag("<h" + level + ">", "</h" + level + ">");
  }

  private void replaceTag(String startTag, String endTag) {
    try {
      var document = (HTMLDocument) getDocument();
      var element = document.getParagraphElement(getCaretPosition());
      var elementText = getElementText(element);
      document.setOuterHTML(element, startTag + elementText + endTag);
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  private ActionListener save() {
    return event -> {
      if (note == null) {
        return;
      }
      Pandoc.htmlToMarkdown(note, getText());
      unsavedChangesTracker.reset();
    };
  }

  private ActionListener refresh() {
    return event -> {
      if (note == null) {
        return;
      }
      openNote(note);
    };
  }

  private ActionListener undo(UndoManager undoManager) {
    return event -> {
      try {
        undoManager.undo();
      } catch (CannotUndoException exc) {
      }
    };
  }
  
  private ActionListener redo(UndoManager undoManager) {
    return event -> {
      try {
        undoManager.redo();
      } catch (CannotRedoException exc) {
      }
    };
  }

  private ActionListener indent() {
    return event -> {
      try {
        var document = (HTMLDocument) getDocument();
        var position = getCaretPosition();
        var element = document.getParagraphElement(position).getParentElement();
        if (element != null && "li".equals(getElementName(element))) {
          var parent = element.getParentElement();
          var listTag = getElementName(parent);
          var sb = new StringBuilder();
          var iterator = new ElementIterator(element);
          Element current = null;
          while ((current = iterator.next()) != null) {
            switch (getElementName(current)) {
              case "content" -> sb.append(getElementText(current));
              case "li" -> sb.append("<li>");
              case "ul" -> sb.append("<ul>");
              case "ol" -> sb.append("<ol>");
            }
          }
          for(var index = 0; index < parent.getElementCount(); index++) {
            if (parent.getElement(index) == element && index > 0) {
              var previous = parent.getElement(index - 1);
              if (previous.getElementCount() > 1) {
                document.insertBeforeEnd(previous.getElement(1), sb.toString());
              } else {
                document.insertBeforeEnd(previous, "<%s>%s</%s>".formatted(listTag, sb.toString(), listTag));
              }
              document.removeElement(element);
              break;
            }
          }
          setCaretPosition(position);
        } else {
          getActionMap().get("insert-tab").actionPerformed(event);
        }
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }

  private ActionListener unindent() {
    return event -> {
      try {
        var document = (HTMLDocument) getDocument();
        var position = getCaretPosition();
        var element = document.getParagraphElement(position).getParentElement();
        if (element != null && "li".equals(getElementName(element))) {
          var parent = element.getParentElement();
          var listTag = getElementName(parent);
          var parentOfParent = parent.getParentElement();
          if (parentOfParent != null && "li".equals(getElementName(parentOfParent))) {
            var sb = new StringBuilder();
            var iterator = new ElementIterator(element);
            Element current = null;
            while ((current = iterator.next()) != null) {
              switch (getElementName(current)) {
                case "content" -> sb.append(getElementText(current));
                case "li" -> sb.append("<li>");
                case "ul" -> sb.append("<ul>");
                case "ol" -> sb.append("<ol>");
              }
            }
            document.insertBeforeEnd(parentOfParent.getParentElement(), sb.toString());
            document.removeElement(element);
            setCaretPosition(position);
          }
        }
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }

  private ActionListener backspace() {
    return event -> {
      getActionMap().get("delete-previous").actionPerformed(event);
      resetCaret();
    };
  }

  private ActionListener debug() {
    return event -> {
      var textarea = new JTextArea();
      textarea.setText(getText());
      textarea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
      var scrollPane = new JScrollPane(textarea);
      var dialog = new JDialog();
      dialog.setTitle("Debug");
      dialog.setLayout(new BorderLayout());
      dialog.add(scrollPane, BorderLayout.CENTER);
      dialog.setModal(true);
      dialog.pack();
      dialog.setLocationRelativeTo(App.instance());
      dialog.setVisible(true);
    };
  }
  
  private MouseListener followHyperlinks() {
    return new MouseAdapter() {
      public void mouseReleased(MouseEvent event) {
        try {
          if (!event.isControlDown()) {
            return;
          }
          var document = (HTMLDocument) getDocument();
          var element = document.getCharacterElement(getCaretPosition());
          if (element == null) {
            return;
          }
          var attributes = element.getAttributes();
          var a = (SimpleAttributeSet) attributes.getAttribute(HTML.Tag.A);
          if (a == null) {
            return;
          }
          var href = (String) a.getAttribute(HTML.Attribute.HREF);
          if (href == null) {
            return;
          }
          if (href.startsWith("http://") || href.startsWith("https://")) {
            Desktop.getDesktop().browse(new URI(href));
          } else if (href.startsWith("./")) {
            var fullName = href.substring(2);
            var note = Note.ofFullName(fullName);
            App.instance().getFileTree().selectNote(note);
          }
        } catch (Exception exc) {
          throw new RuntimeException(exc);
        }
      }
    };
  }

  class HeadingAction extends HTMLTextAction {
    private int level;

    public HeadingAction(int level) {
      super("HeadingAction");
      this.level = level;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
      var editor = getEditor(event);
      var attrs = new SimpleAttributeSet();
      var tag = switch(level) {
        case 1 -> HTML.Tag.H1;
        case 2 -> HTML.Tag.H2;
        case 3 -> HTML.Tag.H3;
        case 4 -> HTML.Tag.H4;
        case 5 -> HTML.Tag.H5;
        case 6 -> HTML.Tag.H6;
        default -> throw new RuntimeException("level " + level);
      };
      attrs.addAttribute(AttributeSet.NameAttribute, tag);
      setParagraphAttributes(editor, attrs, true);
    }
  }

  class LineBreakAction extends HTMLTextAction { 
    public LineBreakAction() {
      super("LineBreakAction");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
      try {
        var editor = getEditor(event);
        var editorKit = getHTMLEditorKit(editor);
        var document = getHTMLDocument(editor);
        var position = editor.getCaretPosition();
        editorKit.insertHTML(document, position, "<br>", 0, 0, HTML.Tag.BR);
      } catch (Exception exc) {
        throw new RuntimeException();
      }
    }
  }
}
