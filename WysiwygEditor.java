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

public class WysiwygEditor extends JTextPane implements Editor {
  private String filename;
  private UnsavedChangesTracker unsavedChangesTracker;

  public WysiwygEditor() {
    try {
      var editorKit = new HTMLEditorKit();
      editorKit.setDefaultCursor(new Cursor(Cursor.TEXT_CURSOR));

      var stylesheet = new StyleSheet();
      stylesheet.loadRules(new InputStreamReader(getClass().getResourceAsStream(Config.stylesheet())), null);
      editorKit.setStyleSheet(stylesheet);

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

      registerKeyboardAction(getActionMap().get("font-bold"), KeyStrokes.CTRL_B, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(getActionMap().get("font-italic"), KeyStrokes.CTRL_I, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(getActionMap().get("font-underline"), KeyStrokes.CTRL_U, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(pasteFromClipboard(), KeyStrokes.CTRL_V, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(insertParagraph(), KeyStrokes.ENTER, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(insertBreak(), KeyStrokes.SHIFT_ENTER, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(insertCodeBlock(), KeyStrokes.CTRL_SHIFT_C, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(insertInfoBlock(), KeyStrokes.CTRL_SHIFT_I, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(insertWarnBlock(), KeyStrokes.CTRL_SHIFT_W, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toParagraph(), KeyStrokes.CTRL_0, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(1), KeyStrokes.CTRL_1, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(2), KeyStrokes.CTRL_2, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(3), KeyStrokes.CTRL_3, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(4), KeyStrokes.CTRL_4, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(5), KeyStrokes.CTRL_5, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(6), KeyStrokes.CTRL_6, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(save(), KeyStrokes.CTRL_S, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(refresh(), KeyStrokes.CTRL_R, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(undo(undoManager), KeyStrokes.CTRL_Z, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(redo(undoManager), KeyStrokes.CTRL_Y, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(debug(), KeyStrokes.F12, JComponent.WHEN_FOCUSED);
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
        var html = "<p><img src=\"../attachments/"+imageFileName+"\"></p><p></p>";
        var document = (HTMLDocument) getDocument();
        var element = document.getParagraphElement(getCaretPosition());
        if (isInBlock()) {
          element = element.getParentElement();
        }
        document.insertAfterEnd(element, html);
        setCaretPosition(element.getEndOffset() + 2);
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
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
      insertBlock("<div class=\"info\"><p></p></div>");
    };
  }
  
  private ActionListener insertWarnBlock() {
    return event -> {
      insertBlock("<div class=\"warn\"><p></p></div>");
    };
  }

  private boolean isInBlock() {
    var document = (HTMLDocument) getDocument();
    var element = document.getParagraphElement(getCaretPosition());
    var parent = element.getParentElement();
    var attribute = parent.getAttributes().getAttribute(AttributeSet.NameAttribute);
    return "pre".equals(attribute.toString()) || "div".equals(attribute.toString());
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
          insertBlock("<pre><code></code></pre>");
        }
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }

  private ActionListener insertParagraph() {
    return event -> {
      insertBlock("<p></p>");
    };
  }

  private void insertBlock(String html) {
    try {
      var document = (HTMLDocument) getDocument();
      var element = document.getParagraphElement(getCaretPosition());
      if (isInBlock()) {
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
      if (isInBlock()) {
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
      var elementText = 
        document.getText(
            element.getStartOffset(), 
            element.getEndOffset() - element.getStartOffset()); 
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
