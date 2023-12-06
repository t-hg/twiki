import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;

public class ToolBar extends JToolBar {
  public ToolBar() {
    var searchField = new JTextField("Search...");
    searchField.addFocusListener(selectAll());
    searchField.setColumns(12);
    add(Box.createHorizontalGlue());
    add(searchField);
    setFloatable(false);
    addMouseListener(hideBar());
    setVisible(false);
  }

  private FocusListener selectAll() {
    return new FocusAdapter() {
      public void focusGained(FocusEvent event) {
        var textField = (JTextField) event.getSource();
        textField.select(0, textField.getText().length());
      }
    };
  }

  private MouseListener hideBar() {
    return new MouseAdapter() {
      public void mouseReleased(MouseEvent event) {
        if (SwingUtilities.isMiddleMouseButton(event)) {
          setVisible(false);
        }
      }
    };
  }
}
