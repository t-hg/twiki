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

      ((HTMLDocument) getDocument()).setBase(Paths.get(Config.notebook()).toFile().toURI().toURL());

      registerKeyboardAction(getActionMap().get("font-bold"), KeyStrokes.CTRL_B, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(getActionMap().get("font-italic"), KeyStrokes.CTRL_I, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(getActionMap().get("font-underline"), KeyStrokes.CTRL_U, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(pasteFromClipboard(), KeyStrokes.CTRL_V, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(insertParagraph(), KeyStrokes.ENTER, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(insertBreak(), KeyStrokes.SHIFT_ENTER, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toParagraph(), KeyStrokes.CTRL_0, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(1), KeyStrokes.CTRL_1, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(2), KeyStrokes.CTRL_2, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(3), KeyStrokes.CTRL_3, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(4), KeyStrokes.CTRL_4, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(5), KeyStrokes.CTRL_5, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(6), KeyStrokes.CTRL_6, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toCode(), KeyStrokes.CTRL_SHIFT_C, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(save(), KeyStrokes.CTRL_S, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(refresh(), KeyStrokes.CTRL_R, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(undo(undoManager), KeyStrokes.CTRL_Z, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(redo(undoManager), KeyStrokes.CTRL_Y, JComponent.WHEN_FOCUSED);
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
        var imageDirectory = Paths.get(Config.notebook(), "images");
        if (!Files.exists(imageDirectory)) {
          Files.createDirectory(imageDirectory);
        }
        var imageFile = 
          Paths.get(
              imageDirectory.toString(), 
              filename + "_" + System.currentTimeMillis() + ".png");
        ImageIO.write(image, "png", imageFile.toFile());
        var imageFileRelative = 
          Paths.get(Config.notebook())
            .toFile()
            .toURI()
            .relativize(imageFile.toFile().toURI())
            .getPath();
        var html = "<p><img src=\""+imageFileRelative+"\"></p>";
        var document = (HTMLDocument) getDocument();
        var editorKit = (HTMLEditorKit) getEditorKit();
        editorKit.insertHTML(document, getCaretPosition(), html, 1, 0, HTML.Tag.P);
        resetCaret();
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

  private boolean isInCodeBlock() {
    var document = (HTMLDocument) getDocument();
    var element = document.getParagraphElement(getCaretPosition());
    var parent = element.getParentElement();
    var attribute = parent.getAttributes().getAttribute(AttributeSet.NameAttribute);
    return "pre".equals(attribute.toString());
  }

  private ActionListener toCode() {
    return event -> {
      try {
        if (isInCodeBlock()) {
          return;
        }
        var document = (HTMLDocument) getDocument();
        var editorKit = (HTMLEditorKit) getEditorKit();
        var selectedText = getSelectedText();
        if (selectedText != null && !selectedText.isBlank()) {
          document.remove(getSelectionStart(), selectedText.length());
          var html = "<code>" + selectedText + "</code>";
          editorKit.insertHTML(document, getCaretPosition(), html, 0, 0, HTML.Tag.CODE);
          resetCaret();
        } else {
          // to code block
          var element = document.getParagraphElement(getCaretPosition());
          var html = "<pre><code></code></pre>";
          document.insertAfterEnd(element, html);
          setCaretPosition(element.getEndOffset());
        }
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }

  private ActionListener insertParagraph() {
    return event -> {
      try {
        var document = (HTMLDocument) getDocument();
        var element = document.getParagraphElement(getCaretPosition());
        if (isInCodeBlock()) {
          element = element.getParentElement();
        }
        var html = "<p></p>";
        document.insertAfterEnd(element, html);
        setCaretPosition(element.getEndOffset());
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }

  private ActionListener toParagraph() {
    return event -> {
      if (isInCodeBlock()) {
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
}
