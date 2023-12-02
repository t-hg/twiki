
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;

public class SourceEditor extends JTextPane {
  public SourceEditor() {
    setContentType("text/plain");
    setCursor(new Cursor(Cursor.TEXT_CURSOR));

    var undoManager = new UndoManager();
    getDocument().addUndoableEditListener(undoManager);

    registerKeyboardAction(this.save(), KeyStrokes.CTRL_S, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.undo(undoManager), KeyStrokes.CTRL_Z, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.redo(undoManager), KeyStrokes.CTRL_Y, JComponent.WHEN_FOCUSED);
  }

  public void onFileSelected(String name) {
    try {
      var path = Paths.get(Config.notebook(), name);
      setText(Files.readString(path));
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
}
