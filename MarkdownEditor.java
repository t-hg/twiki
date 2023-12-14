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
  private Note note;
  private UnsavedChangesTracker unsavedChangesTracker;

  public MarkdownEditor() {
    setContentType("text/plain");
    setCursor(new Cursor(Cursor.TEXT_CURSOR));
    setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

    var undoManager = new UndoManager();
    getDocument().addUndoableEditListener(undoManager);

    unsavedChangesTracker = new UnsavedChangesTracker();
    getDocument().addDocumentListener(unsavedChangesTracker);

    registerKeyboardAction(this.save(), KeyStrokes.CTRL_S, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.refresh(), KeyStrokes.CTRL_R, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.undo(undoManager), KeyStrokes.CTRL_Z, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.redo(undoManager), KeyStrokes.CTRL_Y, JComponent.WHEN_FOCUSED);
  }
  
  public void search(String searchString) {
    Editor.search(this, searchString);
  }

  public void openNote(Note note) {
    try {
      if (hasUnsavedChanges() && MessageDialogs.unsavedChanges(this) != 0) {
        return;
      }
      this.note = note;
      var path = note.getPath();
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
        if (note == null) {
          return;
        }
        var writer = Files.newBufferedWriter(note.getPath());
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
}
