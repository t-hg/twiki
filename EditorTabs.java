import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class EditorTabs extends JTabbedPane {
  private WysiwygEditor wysiwygEditor = new WysiwygEditor();
  private HtmlEditor htmlEditor = new HtmlEditor();
  private MarkdownEditor markdownEditor = new MarkdownEditor();

  private String filename;

  public EditorTabs() {
    super(JTabbedPane.BOTTOM);
    setBorder(new EmptyBorder(2, 0, 4, 0));
    setModel(new EditorTabsModel());
    add("Rendered", scroll(wysiwygEditor));
    add("HTML", scroll(htmlEditor));
    add("Markdown", scroll(markdownEditor));
    addChangeListener(reload());
  }

  private JScrollPane scroll(JComponent component) {
    var scrollPane = new JScrollPane(component);
    scrollPane.setViewportBorder(null);
    return scrollPane;
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
      var scrollPane = (JScrollPane) getSelectedComponent();
      var editor = (Editor) scrollPane.getViewport().getView();
      editor.onFileSelected(filename);
    };
  }

  class EditorTabsModel extends DefaultSingleSelectionModel {
    public void setSelectedIndex(int index) {
      if (hasUnsavedChanges() && MessageDialogs.unsavedChanges(EditorTabs.this) != 0) {
        return;
      }
      super.setSelectedIndex(index);
    }
  }
}

