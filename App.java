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
    var wysiwygEditor = new WysiwygEditor();
    var markdownEditor = new MarkdownEditor();
    var fileTree = new FileTree();
    fileTree.addSelectionListener(wysiwygEditor::onFileSelected);
    fileTree.addSelectionListener(markdownEditor::onFileSelected);
    var tabs = new JTabbedPane();
    tabs.add("Rendered", wysiwygEditor);
    tabs.add("Markdown", markdownEditor);
    var splitPane = 
      new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT, true,
        new JScrollPane(fileTree), 
        tabs);
    splitPane.setDividerLocation(200);
    add(splitPane, BorderLayout.CENTER);
    setVisible(true);
  }
}
