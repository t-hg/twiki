
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.stream.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;

public class WysiwygEditor extends JTextPane {

  public WysiwygEditor() {
    var editorKit = new HTMLEditorKit();
    editorKit.setDefaultCursor(new Cursor(Cursor.TEXT_CURSOR));
    setEditorKit(editorKit);

    var undoManager = new UndoManager();
    getDocument().addUndoableEditListener(undoManager);

    registerKeyboardAction(new HTMLEditorKit.BoldAction(), KeyStrokes.CTRL_B, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(new HTMLEditorKit.ItalicAction(), KeyStrokes.CTRL_I, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(new HTMLEditorKit.UnderlineAction(), KeyStrokes.CTRL_U, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.save(), KeyStrokes.CTRL_S, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.undo(undoManager), KeyStrokes.CTRL_Z, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.redo(undoManager), KeyStrokes.CTRL_Y, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toParagraph(), KeyStrokes.CTRL_0, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(1), KeyStrokes.CTRL_1, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(2), KeyStrokes.CTRL_2, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(3), KeyStrokes.CTRL_3, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(4), KeyStrokes.CTRL_4, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(5), KeyStrokes.CTRL_5, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(6), KeyStrokes.CTRL_6, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.insertCodeBlock(), KeyStrokes.CTRL_SHIFT_C, JComponent.WHEN_FOCUSED);
  }

  public void onFileSelected(String name) {
    try {
      var path = Paths.get(Config.notebook(), name);
      var process = 
        new ProcessBuilder(
            Config.pandoc(), 
            "-f", "markdown", 
            "-t", "html", 
            path.toString())
        .start();
      SwingUtilities.invokeLater(() -> {
        try {
          process.waitFor();
          if (process.exitValue() != 0) {
            throw new RuntimeException(process.errorReader().lines().collect(Collectors.joining()));
          } 
          setText(process.inputReader().lines().collect(Collectors.joining()));
        } catch (Exception exc) {
          throw new RuntimeException(exc);
        }
      });
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  private ActionListener save() {
    return event -> System.out.println(getText());
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

  private ActionListener toHeading(int level) {
    return toTag("<h" + level + ">", "</h" + level + "/>");
  }

  private ActionListener toParagraph() {
    return toTag("<p style=\"margin-top:0;\">", "</p>");
  }

  private ActionListener toTag(String startTag, String endTag) {
    return event -> {
      try {
        var document = (HTMLDocument) getDocument();
        var pos = getCaretPosition();
        var element = document.getParagraphElement(pos);
        var offset = element.getStartOffset();
        var length = element.getEndOffset() - offset;
        var tag = startTag + document.getText(offset, length) + endTag;
        document.setOuterHTML(element, tag);
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }

  private ActionListener insertCodeBlock() {
    var html = "<pre></pre>";
    return new HTMLEditorKit.InsertHTMLTextAction("pre", html, HTML.Tag.BODY, HTML.Tag.PRE);
  }
}
