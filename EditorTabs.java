import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class EditorTabs extends JTabbedPane {
  private WysiwygEditor wysiwygEditor = new WysiwygEditor();
  private HtmlEditor htmlEditor = new HtmlEditor();
  private MarkdownEditor markdownEditor = new MarkdownEditor();

  private String filename;

  public EditorTabs() {
    super(JTabbedPane.BOTTOM);
    setModel(new EditorTabsModel());
    add("Rendered", wysiwygEditor);
    add("HTML", htmlEditor);
    add("Markdown", markdownEditor);
    addChangeListener(reload());
  }

  public void onSearch(String searchString) {
    wysiwygEditor.onSearch(searchString);
    htmlEditor.onSearch(searchString);
    markdownEditor.onSearch(searchString);
  }

  public void onFileSelected(String name) {
    this.filename = name;
    wysiwygEditor.onFileSelected(name);
    htmlEditor.onFileSelected(name);
    markdownEditor.onFileSelected(name);
  }

  public boolean hasUnsavedChanges() {
    return wysiwygEditor.hasUnsavedChanges() 
        || htmlEditor.hasUnsavedChanges()
        || markdownEditor.hasUnsavedChanges();
  }

  private ChangeListener reload() {
    return event -> {
      ((Editor) getSelectedComponent()).onFileSelected(filename);
    };
  }

  class EditorTabsModel extends DefaultSingleSelectionModel {
    public void setSelectedIndex(int index) {
      if (hasUnsavedChanges()) {
        MessageDialogs.unsavedChanges(EditorTabs.this);
        return;
      }
      super.setSelectedIndex(index);
    }
  }
}

