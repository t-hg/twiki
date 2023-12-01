import java.awt.*;
import javax.swing.*;

public class App extends JFrame {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(App::new);
  }
  
  public App() {
      setTitle("twiki");
      setSize(1280, 720);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLayout(new BorderLayout());
      add(new JScrollPane(new Editor()), BorderLayout.CENTER);
      setVisible(true);
  }
}
