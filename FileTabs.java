import java.util.*;
import javax.swing.*;

public class FileTabs extends JTabbedPane {
  private Map<String, EditorTabs> openFiles = new HashMap<>();

  public void onFileSelected(String name) {
    if(openFiles.containsKey(name)) {
      var editorTabs = openFiles.get(name);
      editorTabs.onFileSelected(name);
      setSelectedComponent(editorTabs); 
    } else {
      var editorTabs = new EditorTabs();
      openFiles.put(name, editorTabs);
      editorTabs.onFileSelected(name);
      var parts = name.split("\\.");
      add(parts[parts.length - 1], editorTabs);
      setSelectedComponent(editorTabs); 
    }
  }
}
