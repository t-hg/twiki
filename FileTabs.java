import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class FileTabs extends JTabbedPane {
  private record Tab(String filename, EditorTabs editorTabs) {
    public String getTitle() {
      var parts = filename.split("\\.");
      return parts[parts.length - 1];
    }
  }

  private String filename;
  private List<Tab> tabs = new ArrayList<>();

  public FileTabs() {
    addMouseListener(removeTab());
  }

  public void onSearch(String searchString) {
    Tab tab = tabs.stream()
                  .filter(it -> it.filename().equals(filename))
                  .findFirst()
                  .orElse(null);
    if (tab != null) {
      tab.editorTabs().onSearch(searchString);
    }
  }

  public void onFileSelected(String name) {
    filename = name;
    Tab tab = tabs.stream()
                  .filter(it -> it.filename().equals(filename))
                  .findFirst()
                  .orElse(null);
    if(tab != null) {
      tab.editorTabs().onFileSelected(filename);
      setSelectedComponent(tab.editorTabs()); 
    } else {
      var editorTabs = new EditorTabs();
      tab = new Tab(filename, editorTabs);
      tabs.add(tab);
      editorTabs.onFileSelected(filename);
      add(tab.getTitle(), editorTabs);
      setSelectedComponent(editorTabs); 
    }
  }

  public boolean hasUnsavedChanges() {
    for(var tab : tabs) {
      if (tab.editorTabs.hasUnsavedChanges()) {
        return true;
      }
    }
    return false;
  }

  private MouseListener removeTab() {
    return new MouseAdapter() {
      public void mouseReleased(MouseEvent event) {
        if (SwingUtilities.isMiddleMouseButton(event)) {
          var index = getSelectedIndex();
          var tab = tabs.get(index);
          if (tab.editorTabs.hasUnsavedChanges()) {
            MessageDialogs.unsavedChanges(FileTabs.this);
            return;
          }
          tabs.remove(index);
          remove(index);
        }
      }
    };
  }
}
