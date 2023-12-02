import java.awt.*;
import javax.swing.*;

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
    setVisible(true);
  }
}
