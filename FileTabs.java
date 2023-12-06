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

  private List<Tab> tabs = new ArrayList<>();

  public FileTabs() {
    addMouseListener(removeTab());
  }

  public void onFileSelected(String name) {
    Tab tab = tabs.stream()
                  .filter(it -> it.filename().equals(name))
                  .findFirst()
                  .orElse(null);

    if(tab != null) {
      tab.editorTabs().onFileSelected(name);
      setSelectedComponent(tab.editorTabs()); 
    } else {
      var editorTabs = new EditorTabs();
      tab = new Tab(name, editorTabs);
      tabs.add(tab);
      editorTabs.onFileSelected(name);
      add(tab.getTitle(), editorTabs);
      setSelectedComponent(editorTabs); 
    }
  }

  private MouseListener removeTab() {
    return new MouseAdapter() {
      public void mouseReleased(MouseEvent event) {
        if (SwingUtilities.isMiddleMouseButton(event)) {
          var index = getSelectedIndex();
          tabs.remove(index);
          remove(index);
        }
      }
    };
  }
}
