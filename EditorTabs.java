import javax.swing.*;

public class EditorTabs extends JTabbedPane {
  private WysiwygEditor wysiwygEditor = new WysiwygEditor();
  private HtmlEditor htmlEditor = new HtmlEditor();
  private MarkdownEditor markdownEditor = new MarkdownEditor();

  public EditorTabs() {
    super(JTabbedPane.BOTTOM);
    add("Rendered", wysiwygEditor);
    add("HTML", htmlEditor);
    add("Markdown", markdownEditor);
  }

  public void onFileSelected(String name) {
    wysiwygEditor.onFileSelected(name);
    htmlEditor.onFileSelected(name);
    markdownEditor.onFileSelected(name);
  }
}

