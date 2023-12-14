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
  private Note note;
  private UnsavedChangesTracker unsavedChangesTracker;

  public HtmlEditor() {
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
  
  public void onSearch(String searchString) {
    Editor.onSearch(this, searchString);
  }

  public void openNote(Note note) {
    try {
      if (hasUnsavedChanges() && MessageDialogs.unsavedChanges(HtmlEditor.this) != 0) {
        return;
      }
      this.note = note;
      setText(Pandoc.markdownToHtml(note));
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
}
