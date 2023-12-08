import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;

public class App extends JFrame {
  private FileTabs fileTabs;
  private ToolBar toolBar;
  private GlobalSearchDialog globalSearchDialog;

  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    SwingUtilities.invokeLater(App::new);
  }
  
  public App() {
    Config.load();
    fileTabs = new FileTabs();
    toolBar = new ToolBar();
    globalSearchDialog = new GlobalSearchDialog();
    setTitle("twiki");
    setSize(1280, 720);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setLayout(new BorderLayout());
    toolBar.addSearchListener(fileTabs::onSearch);
    var editorPanel = new JPanel();
    editorPanel.setLayout(new BorderLayout());
    editorPanel.add(fileTabs, BorderLayout.CENTER);
    editorPanel.add(toolBar, BorderLayout.SOUTH);
    var fileTree = new FileTree();
    fileTree.addSelectionListener(fileTabs::onFileSelected);
    var splitPane = 
      new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT, true,
        new JScrollPane(fileTree), 
        editorPanel);
    splitPane.setDividerLocation(200);
    add(splitPane, BorderLayout.CENTER);
    setLocationRelativeTo(null);
    setVisible(true);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent event) {
        if (fileTabs.hasUnsavedChanges() && MessageDialogs.unsavedChanges(App.this) != 0) {
          return;
        }
        dispose();
        System.exit(0);
      }
    });

    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter); 
        var textarea = new JTextArea();
        textarea.setText(stringWriter.toString());
        var scrollPane = new JScrollPane(textarea);
        var dialog = new JDialog();
        dialog.setTitle("Uncaught Exception");
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
      }
    });
   
    getRootPane().registerKeyboardAction(
        globalSearch(), 
        KeyStrokes.CTRL_SHIFT_F, 
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    
    fileTabs.registerKeyboardAction(
        showToolBar(), 
        KeyStrokes.CTRL_F, 
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private ActionListener globalSearch() {
    return event -> {
      globalSearchDialog.setVisible(true);
      globalSearchDialog.grabFocus();
      globalSearchDialog.addFileSelectionListener(filename -> {
        var searchString = globalSearchDialog.getSearchString();
        toolBar.setVisible(true);
        toolBar.setSearchString(searchString);
        fileTabs.onFileSelected(filename);
        fileTabs.onSearch(searchString);
      });
    };
  }

  private ActionListener showToolBar() {
    return event -> {
      toolBar.setVisible(true);
      toolBar.focusSearch();
    };
  }
}
