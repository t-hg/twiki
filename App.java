import java.awt.*;
import javax.swing.*;

public class App extends JFrame {
  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    SwingUtilities.invokeLater(App::new);
  }
  
  public App() {
    var config = Config.load();
    setTitle("twiki");
    setSize(1280, 720);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());
    add(
      new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT, 
        new JScrollPane(new FileTree(config)), 
        new JScrollPane(new Editor())), 
      BorderLayout.CENTER);
    setVisible(true);
  }
}
