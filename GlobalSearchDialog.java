import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class GlobalSearchDialog extends JDialog { 
  private JTextField searchField;
  private JTable table;
  private ResultTableModel tableModel;
  private java.util.List<Consumer<Note>> noteSelectionListeners = new ArrayList<>();

  public GlobalSearchDialog() {
    searchField = new JTextField("Search...");
    searchField.addFocusListener(selectAll());
    searchField.addActionListener(search());
    setTitle("Search");
    setLayout(new BorderLayout());
    add(searchField, BorderLayout.NORTH);
    tableModel = new ResultTableModel();
    table = new JTable(tableModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getSelectionModel().addListSelectionListener(openNote());
    table.getColumnModel().getColumn(0).setPreferredWidth(400);
    add(new JScrollPane(table), BorderLayout.CENTER);
    setModal(false);
    setSize(400, 300);
    setVisible(false);
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    getRootPane().registerKeyboardAction(close(), KeyStrokes.ESC, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  public void grabFocus() {
    searchField.grabFocus();
  }

  public String getSearchString() {
    return searchField.getText();
  }

  public void addNoteSelectionListener(Consumer<Note> listener) {
    noteSelectionListeners.add(listener);
  }

  private ActionListener close() {
    return event -> {
      setVisible(false);
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

  private ActionListener search() {
    return event -> {
      var textField = (JTextField) event.getSource();
      var searchString = textField.getText();
      tableModel.setRowCount(0);
      Ripgrep.search(searchString)
             .stream()
             .forEach(result -> tableModel.addRow(new Object[]{result.note().getFullName(), result.count()}));
      textField.select(0, textField.getText().length());
    };
  }

  private ListSelectionListener openNote() {
    return new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
          return;
        }
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
          return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        var fullName = tableModel.getValueAt(modelRow, 0).toString();
        var note = Note.ofFullName(fullName);
        App.instance().getFileTree().selectNote(note);
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
