import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;

public class App extends JFrame {
  private static App instance;

  public static void main(String[] args) throws Exception {
    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    SwingUtilities.invokeLater(() -> {
      instance = new App();
    });
  }

  public static App instance() {
    return App.instance;
  }
  
  private FileTabs fileTabs;
  private FileTree fileTree;
  private ToolBar toolBar;
  private GlobalSearchDialog globalSearchDialog;
  
  public App() {
    Config.load();
    fileTabs = new FileTabs();
    fileTree = new FileTree();
    toolBar = new ToolBar();
    globalSearchDialog = new GlobalSearchDialog();
    setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());
    setTitle("twiki");
    setSize(1280, 720);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setLayout(new BorderLayout());
    var editorPanel = new JPanel();
    editorPanel.setLayout(new BorderLayout());
    editorPanel.add(fileTabs, BorderLayout.CENTER);
    editorPanel.add(toolBar, BorderLayout.SOUTH);
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
        textarea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        var scrollPane = new JScrollPane(textarea);
        var dialog = new JDialog();
        dialog.setTitle("Uncaught Exception");
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setModal(true);
        dialog.pack();
        dialog.setLocationRelativeTo(App.this);
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

  public FileTree getFileTree() {
    return fileTree;
  }

  public FileTabs getFileTabs() {
    return fileTabs;
  }
  
  public ToolBar getToolBar() {
    return toolBar;
  }

  private ActionListener globalSearch() {
    return event -> {
      globalSearchDialog.setLocationRelativeTo(App.this);
      globalSearchDialog.setVisible(true);
      globalSearchDialog.grabFocus();
    };
  }

  private ActionListener showToolBar() {
    return event -> {
      toolBar.setVisible(true);
      toolBar.focusSearch();
    };
  }
}
