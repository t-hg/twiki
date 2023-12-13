import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;

public class ToolBar extends JToolBar {
  private JTextField searchField;
  private java.util.List<Consumer<String>> searchListeners = new ArrayList<>();

  public ToolBar() {
    searchField = new JTextField("Search...");
    searchField.addFocusListener(selectAll());
    searchField.setColumns(12);
    searchField.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));
    searchField.addActionListener(search());
    add(Box.createHorizontalGlue());
    add(searchField);
    setBorder(new EmptyBorder(5, 5, 5, 5));
    setFloatable(false);
    setVisible(false);
    addMouseListener(hideBar());
  }

  public void setSearchString(String searchString) {
    searchField.setText(searchString);
  }

  public void focusSearch() {
    searchField.grabFocus();
  }

  public void addSearchListener(Consumer<String> searchListener) {
    searchListeners.add(searchListener);
  }

  private ActionListener search() {
    return event -> {
      var textField = (JTextField) event.getSource();
      var searchString = textField.getText();
      searchListeners.forEach(listener -> listener.accept(searchString));
      textField.select(0, textField.getText().length());
    };
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
