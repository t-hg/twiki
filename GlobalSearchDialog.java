import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class GlobalSearchDialog extends JDialog {
  public GlobalSearchDialog() {
    var searchField = new JTextField("Search...");
    searchField.addFocusListener(selectAll());
    searchField.addActionListener(search());
    setTitle("Search");
    setLayout(new BorderLayout());
    add(searchField, BorderLayout.NORTH);
    add(new JTable(), BorderLayout.CENTER);
    setModal(true);
    setSize(400, 300);
    setLocationRelativeTo(null);
    setVisible(true);
    searchField.grabFocus();
  }

  private FocusListener selectAll() {
    return new FocusAdapter() {
      public void focusGained(FocusEvent event) {
        var textField = (JTextField) event.getSource();
        textField.select(0, textField.getText().length());
      }
    };
  }

  private ActionListener search() {
    return event -> {
      System.out.println(event);
      var textField = (JTextField) event.getSource();
      textField.select(0, textField.getText().length());
    };
  }
}
