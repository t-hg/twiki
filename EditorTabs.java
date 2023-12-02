import javax.swing.*;

public class EditorTabs extends JTabbedPane {
  private WysiwygEditor wysiwygEditor = new WysiwygEditor();
  private MarkdownEditor markdownEditor = new MarkdownEditor();

  public EditorTabs() {
    super(JTabbedPane.BOTTOM);
    add("Rendered", wysiwygEditor);
    add("Markdown", markdownEditor);
  }

  public void onFileSelected(String name) {
    wysiwygEditor.onFileSelected(name);
    markdownEditor.onFileSelected(name);
  }
}

