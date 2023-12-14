import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class FileTabs extends JTabbedPane {
  private record Tab(Note note, EditorTabs editorTabs) {
  }

  private Note note;
  private List<Tab> tabs = new ArrayList<>();

  public FileTabs() {
    addMouseListener(removeTab());
  }

  public void onSearch(String searchString) {
    Tab tab = 
      tabs.stream()
          .filter(it -> it.note().equals(note))
          .findFirst()
          .orElse(null);
    if (tab != null) {
      tab.editorTabs().onSearch(searchString);
    }
  }

  public void openNote(Note note) {
    this.note = note;
    Tab tab = 
      tabs.stream()
          .filter(it -> it.note().equals(note))
          .findFirst()
          .orElse(null);
    if(tab != null) {
      tab.editorTabs().openNote(note);
      setSelectedComponent(tab.editorTabs()); 
    } else {
      var editorTabs = new EditorTabs();
      tab = new Tab(note, editorTabs);
      tabs.add(tab);
      editorTabs.openNote(note);
      add(tab.note().getShortName(), editorTabs);
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
          if (index < 0) {
            return;
          }
          var tab = tabs.get(index);
          if (hasUnsavedChanges() && MessageDialogs.unsavedChanges(FileTabs.this) != 0) {
            return;
          }
          tabs.remove(index);
          remove(index);
        }
      }
    };
  }
}
