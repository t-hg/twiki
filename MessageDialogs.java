import java.awt.*;
import javax.swing.*;

public class MessageDialogs {
  
  public static void unsavedChanges(Component component) {
    JOptionPane.showMessageDialog(
        component, 
        "There are unsaved changes.", 
        "Unsaved changes", 
        JOptionPane.WARNING_MESSAGE);
  }
}
