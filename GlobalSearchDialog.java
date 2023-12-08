import java.awt.*;
import java.awt.event.*;
import java.lang.ref.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class GlobalSearchDialog extends JDialog { 
  private JTable table;
  private ResultTableModel tableModel;
  private java.util.List<WeakReference<Consumer<String>>> fileSelectionListeners = new ArrayList<>();

  public GlobalSearchDialog() {
    var searchField = new JTextField("Search...");
    searchField.addFocusListener(selectAll());
    searchField.addActionListener(search());
    setTitle("Search");
    setLayout(new BorderLayout());
    add(searchField, BorderLayout.NORTH);
    tableModel = new ResultTableModel();
    table = new JTable(tableModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getSelectionModel().addListSelectionListener(openFile());
    add(new JScrollPane(table), BorderLayout.CENTER);
    setModal(false);
    setSize(400, 300);
    setLocationRelativeTo(null);
    setVisible(true);
    searchField.grabFocus();
  }

  public void addFileSelectionListener(Consumer<String> listener) {
    fileSelectionListeners.add(new WeakReference<>(listener));
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
      var textField = (JTextField) event.getSource();
      var searchString = textField.getText();
      for(int row = 0; row < tableModel.getRowCount(); row++) {
        tableModel.removeRow(row);
      }
      Ripgrep.search(searchString)
             .stream()
             .forEach(result -> tableModel.addRow(new Object[]{result.filename(), result.count()}));
      textField.select(0, textField.getText().length());
    };
  }

  private ListSelectionListener openFile() {
    return new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
          return;
        }
        int viewRow = table.getSelectedRow();
        int modelRow = table.convertRowIndexToModel(viewRow);
        var filename = tableModel.getValueAt(modelRow, 0).toString();
        fileSelectionListeners.forEach(listener -> listener.get().accept(filename));
      }
    };
  }

  class ResultTableModel extends DefaultTableModel {
    public ResultTableModel() {
      addColumn("File");
      addColumn("Score");
    }

    public boolean isCellEditable(int row, int col) {
      return false;
    }
  }
}
