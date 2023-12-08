import javax.swing.text.*;
import javax.swing.event.*;

public class UnsavedChangesTracker implements DocumentListener {
  private boolean hasUnsavedChanges;

  public boolean hasUnsavedChanges() {
    return hasUnsavedChanges;
  }

  public void reset() {
    hasUnsavedChanges = false;
  }

  public void insertUpdate(DocumentEvent event) {
    hasUnsavedChanges = true;
  }

  public void removeUpdate(DocumentEvent event) {
    hasUnsavedChanges = true;
  }

  public void changedUpdate(DocumentEvent event) {
  }
}
