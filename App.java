import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;

public class App extends JFrame {
  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    SwingUtilities.invokeLater(App::new);
  }
  
  public App() {
    Config.load();
    setTitle("twiki");
    setSize(1280, 720);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setLayout(new BorderLayout());
    var fileTabs = new FileTabs();
    var toolBar = new ToolBar();
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
        if (fileTabs.hasUnsavedChanges()) {
          MessageDialogs.unsavedChanges(App.this);
          return;
        }
        dispose();
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
   
    getRootPane().registerKeyboardAction(globalSearch(), KeyStrokes.CTRL_SHIFT_F, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    fileTabs.registerKeyboardAction(showToolBar(toolBar), KeyStrokes.CTRL_F, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private ActionListener globalSearch() {
    return event -> {
      new GlobalSearchDialog();
    };
  }

  private ActionListener showToolBar(ToolBar toolBar) {
    return event -> {
      toolBar.setVisible(true);
      toolBar.focusSearch();
    };
  }
}
