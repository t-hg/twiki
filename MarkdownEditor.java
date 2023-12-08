import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;

public class MarkdownEditor extends JTextPane implements Editor {
  private String filename;
  private UnsavedChangesTracker unsavedChangesTracker;

  public MarkdownEditor() {
    setContentType("text/plain");
    setCursor(new Cursor(Cursor.TEXT_CURSOR));

    var undoManager = new UndoManager();
    getDocument().addUndoableEditListener(undoManager);

    unsavedChangesTracker = new UnsavedChangesTracker();
    getDocument().addDocumentListener(unsavedChangesTracker);

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
      if (hasUnsavedChanges() && MessageDialogs.unsavedChanges(this) != 0) {
        return;
      }
      filename = name;
      var path = Paths.get(Config.notebook(), name);
      if (!Files.exists(path)) {
        return;
      }
      setText(Files.readString(path));
      unsavedChangesTracker.reset();
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public boolean hasUnsavedChanges() {
    return unsavedChangesTracker.hasUnsavedChanges();
  }

  private ActionListener save() {
    return event -> {
      try {
        if (filename == null) {
          return;
        }
        var writer = Files.newBufferedWriter(Paths.get(Config.notebook(), filename));
        writer.write(getText());
        writer.flush();
        writer.close();
        unsavedChangesTracker.reset();
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
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
