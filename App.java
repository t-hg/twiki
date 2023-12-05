import java.awt.*;
import javax.swing.*;
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
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());
    var fileTabs = new FileTabs();
    var fileTree = new FileTree();
    fileTree.addSelectionListener(fileTabs::onFileSelected);
    var splitPane = 
      new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT, true,
        new JScrollPane(fileTree), 
        fileTabs);
    splitPane.setDividerLocation(200);
    add(splitPane, BorderLayout.CENTER);
    setLocationRelativeTo(null);
    setVisible(true);

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
  }
}
