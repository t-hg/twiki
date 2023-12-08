import java.awt.*;
import javax.swing.*;

public class MessageDialogs {
  
  public static int unsavedChanges(Component component) {
    return JOptionPane.showConfirmDialog(
        component, 
        "Discard unsaved changes?", 
        "Unsaved changes", 
        JOptionPane.YES_NO_OPTION);
  }
}
