import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class EditorTabs extends JTabbedPane {
  private WysiwygEditor wysiwygEditor = new WysiwygEditor();
  private HtmlEditor htmlEditor = new HtmlEditor();
  private MarkdownEditor markdownEditor = new MarkdownEditor();

  private Note note;

  public EditorTabs() {
    super(JTabbedPane.BOTTOM);
    setModel(new EditorTabsModel());
    add("Rendered", scroll(wysiwygEditor));
    add("HTML", scroll(htmlEditor));
    add("Markdown", scroll(markdownEditor));
    addChangeListener(reload());
  }

  private JScrollPane scroll(JComponent component) {
    return new JScrollPane(component);
  }

  public void onSearch(String searchString) {
    wysiwygEditor.onSearch(searchString);
    htmlEditor.onSearch(searchString);
    markdownEditor.onSearch(searchString);
  }

  public void openNote(Note note) {
    this.note = note;
    wysiwygEditor.openNote(note);
    htmlEditor.openNote(note);
    markdownEditor.openNote(note);
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
      editor.openNote(note);
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

