import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;

public class HtmlEditor extends JTextPane implements Editor {
  private String filename;

  public HtmlEditor() {
    setContentType("text/plain");
    setCursor(new Cursor(Cursor.TEXT_CURSOR));

    var undoManager = new UndoManager();
    getDocument().addUndoableEditListener(undoManager);

    registerKeyboardAction(this.save(), KeyStrokes.CTRL_S, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.refresh(), KeyStrokes.CTRL_R, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.undo(undoManager), KeyStrokes.CTRL_Z, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.redo(undoManager), KeyStrokes.CTRL_Y, JComponent.WHEN_FOCUSED);
  }
  
  public void onSearch(String searchString) {
    Editor.onSearch(this, searchString);
  }

  public void onFileSelected(String name) {
    try {
      filename = name;
      setText(Pandoc.markdownToHtml(filename));
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
