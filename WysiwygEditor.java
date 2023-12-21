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
      getInputMap().put(KeyStrokes.ENTER, new EnterAction());
      getInputMap().put(KeyStrokes.SHIFT_ENTER, new EnterAction());
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
      getInputMap().put(
          KeyStrokes.CTRL_SHIFT_U, 
          new HTMLEditorKit.InsertHTMLTextAction(
            "InsertUnorderedListAction", 
            "<ul><li></li></ul>", 
            HTML.Tag.BODY, 
            HTML.Tag.UL));
      getInputMap().put(
          KeyStrokes.CTRL_SHIFT_O, 
          new HTMLEditorKit.InsertHTMLTextAction(
            "InsertOrderedListAction", 
            "<ol><li></li></ol>", 
            HTML.Tag.BODY, 
            HTML.Tag.OL));
      getInputMap().put(KeyStrokes.CTRL_SHIFT_T, new InsertTableAction());
      getInputMap().put(KeyStrokes.CTRL_V, new PasteAction());

      registerKeyboardAction(save(), KeyStrokes.CTRL_S, WHEN_FOCUSED);
      registerKeyboardAction(refresh(), KeyStrokes.CTRL_R, WHEN_FOCUSED);
      registerKeyboardAction(undo(undoManager), KeyStrokes.CTRL_Z, WHEN_FOCUSED);
      registerKeyboardAction(redo(undoManager), KeyStrokes.CTRL_Y, WHEN_FOCUSED);
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

  class EnterAction extends HTMLTextAction { 
    public EnterAction() {
      super("EnterAction");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
      var shiftDown = (event.getModifiers() & ActionEvent.SHIFT_MASK) > 0;
      if (shiftDown) {
        insertLineBreak(event);
        return;
      }
    }

    private void insertLineBreak(ActionEvent event) {
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

  class InsertTableAction extends HTMLTextAction {
    public InsertTableAction() {
      super("InsertTableAction");
    }

    public void actionPerformed(ActionEvent event) {
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

      new HTMLEditorKit.InsertHTMLTextAction(
          "InsertTableAction", 
          sb.toString(), 
          HTML.Tag.BODY, 
          HTML.Tag.TABLE).actionPerformed(event);
    }
  }

  class PasteAction extends HTMLTextAction {
    public PasteAction() {
      super("PasteAction");
    }

    @Override
    public void actionPerformed(ActionEvent event) {
      try {
        var content = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (content == null) {
          return;
        }
        if(!content.isDataFlavorSupported(DataFlavor.imageFlavor)) {
          var string = (String) content.getTransferData(DataFlavor.stringFlavor);
          var editor = getEditor(event);
          var document = getHTMLDocument(editor);
          document.insertString(editor.getCaretPosition(), string, null);
          return;
        }
        var image = (BufferedImage) content.getTransferData(DataFlavor.imageFlavor);
        var attachments = Paths.get(Config.attachments());
        var imageFileName = note.getFullName() + "_" + System.currentTimeMillis() + ".png";
        var imageFile = Paths.get(attachments.toString(), imageFileName);
        ImageIO.write(image, "png", imageFile.toFile());
        new HTMLEditorKit.InsertHTMLTextAction(
            "PasteAction", 
            "<p><img src=\"../attachments/" + imageFileName + "\"></p>", 
            HTML.Tag.BODY, 
            HTML.Tag.P).actionPerformed(event);
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    }
  }
}
