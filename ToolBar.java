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
  }

  private FocusAdapter selectAll() {
    return new FocusAdapter() {
      public void focusGained(FocusEvent event) {
        var textField = (JTextField) event.getSource();
        textField.select(0, textField.getText().length());
      }
    };
  }
}
