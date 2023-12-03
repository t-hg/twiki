import javax.swing.*;
import javax.swing.event.*;

public class EditorTabs extends JTabbedPane {
  private WysiwygEditor wysiwygEditor = new WysiwygEditor();
  private HtmlEditor htmlEditor = new HtmlEditor();
  private MarkdownEditor markdownEditor = new MarkdownEditor();

  private String filename;

  public EditorTabs() {
    super(JTabbedPane.BOTTOM);
    add("Rendered", wysiwygEditor);
    add("HTML", htmlEditor);
    add("Markdown", markdownEditor);
    addChangeListener(reload());
  }

  public void onFileSelected(String name) {
    this.filename = name;
    wysiwygEditor.onFileSelected(name);
    htmlEditor.onFileSelected(name);
    markdownEditor.onFileSelected(name);
  }

  private ChangeListener reload() {
    return event -> {
      ((Editor) getSelectedComponent()).onFileSelected(filename);
    };
  }
}

