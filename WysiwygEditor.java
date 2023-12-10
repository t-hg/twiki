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
import javax.swing.undo.*;

import static javax.swing.JComponent.WHEN_FOCUSED;

public class WysiwygEditor extends JTextPane implements Editor {
  private String filename;
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
      //      .collect(Collectors.joining("\n")));

      var undoManager = new UndoManager();
      getDocument().addUndoableEditListener(undoManager);
      
      unsavedChangesTracker = new UnsavedChangesTracker();
      getDocument().addDocumentListener(unsavedChangesTracker);

      ((HTMLDocument) getDocument()).setBase(Paths.get(Config.notes()).toFile().toURI().toURL());

      registerKeyboardAction(getActionMap().get("font-bold"), KeyStrokes.CTRL_B, WHEN_FOCUSED);
      registerKeyboardAction(getActionMap().get("font-italic"), KeyStrokes.CTRL_I, WHEN_FOCUSED);
      registerKeyboardAction(getActionMap().get("font-underline"), KeyStrokes.CTRL_U, WHEN_FOCUSED);
      registerKeyboardAction(pasteFromClipboard(), KeyStrokes.CTRL_V, WHEN_FOCUSED);
      registerKeyboardAction(continueNext(), KeyStrokes.ENTER, WHEN_FOCUSED);
      registerKeyboardAction(insertBreak(), KeyStrokes.SHIFT_ENTER, WHEN_FOCUSED);
      registerKeyboardAction(insertCodeBlock(), KeyStrokes.CTRL_SHIFT_C, WHEN_FOCUSED);
      registerKeyboardAction(insertInfoBlock(), KeyStrokes.CTRL_SHIFT_I, WHEN_FOCUSED);
      registerKeyboardAction(insertWarnBlock(), KeyStrokes.CTRL_SHIFT_W, WHEN_FOCUSED);
      registerKeyboardAction(insertUnorderedList(), KeyStrokes.CTRL_SHIFT_U, WHEN_FOCUSED);
      registerKeyboardAction(insertOrderedList(), KeyStrokes.CTRL_SHIFT_O, WHEN_FOCUSED);
      registerKeyboardAction(insertTable(), KeyStrokes.CTRL_SHIFT_T, WHEN_FOCUSED);
      registerKeyboardAction(indent(), KeyStrokes.TAB, WHEN_FOCUSED);
      registerKeyboardAction(unindent(), KeyStrokes.SHIFT_TAB, WHEN_FOCUSED);
      registerKeyboardAction(toParagraph(), KeyStrokes.CTRL_0, WHEN_FOCUSED);
      registerKeyboardAction(toHeading(1), KeyStrokes.CTRL_1, WHEN_FOCUSED);
      registerKeyboardAction(toHeading(2), KeyStrokes.CTRL_2, WHEN_FOCUSED);
      registerKeyboardAction(toHeading(3), KeyStrokes.CTRL_3, WHEN_FOCUSED);
      registerKeyboardAction(toHeading(4), KeyStrokes.CTRL_4, WHEN_FOCUSED);
      registerKeyboardAction(toHeading(5), KeyStrokes.CTRL_5, WHEN_FOCUSED);
      registerKeyboardAction(toHeading(6), KeyStrokes.CTRL_6, WHEN_FOCUSED);
      registerKeyboardAction(save(), KeyStrokes.CTRL_S, WHEN_FOCUSED);
      registerKeyboardAction(refresh(), KeyStrokes.CTRL_R, WHEN_FOCUSED);
      registerKeyboardAction(undo(undoManager), KeyStrokes.CTRL_Z, WHEN_FOCUSED);
      registerKeyboardAction(redo(undoManager), KeyStrokes.CTRL_Y, WHEN_FOCUSED);
      registerKeyboardAction(backspace(), KeyStrokes.BACKSPACE, WHEN_FOCUSED);
      registerKeyboardAction(debug(), KeyStrokes.F12, WHEN_FOCUSED);
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public void onSearch(String searchString) {
    Editor.onSearch(this, searchString);
  }

  public void onFileSelected(String name) {
    if (hasUnsavedChanges() && MessageDialogs.unsavedChanges(this) != 0) {
      return;
    }
    filename = name;
    setText(Pandoc.markdownToHtml(name));
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
        if (!Files.exists(attachments)) {
          Files.createDirectory(attachments);
        }
        var imageFileName = filename + "_" + System.currentTimeMillis() + ".png";
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
            App.component(), 
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
      if (filename == null) {
        return;
      }
      Pandoc.htmlToMarkdown(filename, getText());
      unsavedChangesTracker.reset();
    };
  }

  private ActionListener refresh() {
    return event -> {
      if (filename == null) {
        return;
      }
      onFileSelected(filename);
    };
  }

  private ActionListener undo(UndoManager undoManager) {
    return event -> {
      try {
        undoManager.undo();
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }
  
  private ActionListener redo(UndoManager undoManager) {
    return event -> {
      try {
        undoManager.redo();
      } catch (Exception exc) {
        throw new RuntimeException(exc);
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
      dialog.setLocationRelativeTo(App.component());
      dialog.setVisible(true);
    };
  }
}
