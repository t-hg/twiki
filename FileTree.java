import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class FileTree extends JTree {

  private List<Consumer<String>> selectionListeners = new ArrayList<>();

  public FileTree() {
    var root = new DefaultMutableTreeNode("root");
    var model = new DefaultTreeModel(root);
    setModel(model);
    try {
      Files
        .list(Paths.get(Config.notebook()))
        .sorted()
        .map(Path::getFileName) 
        .map(Path::toString)
        .forEach(name -> {
          var node = root;
          for (var part : name.split("\\.")) {
            var child = getChild(node, part);
            if (child == null) {
              child = new DefaultMutableTreeNode(part);
              node.add(child); 
            } 
            node = child;
          }
        });
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }
    expandRow(0);
    setRootVisible(false);
    addTreeSelectionListener(this::onSelected);
  }

  private DefaultMutableTreeNode getChild(DefaultMutableTreeNode node, Object userObject) {
    var children = node.children();
    while(children.hasMoreElements()) {
      var child = (DefaultMutableTreeNode) children.nextElement();
      if (userObject.equals(child.getUserObject())) {
        return child;
      }
    }
    return null;
  }

  public void addSelectionListener(Consumer<String> listener) {
    selectionListeners.add(listener);
  }

  private void onSelected(TreeSelectionEvent event) {
    var names = new ArrayList<String>();
    for(var part : event.getPath().getPath()) {
      var name = part.toString();
      if ("root".equals(name)) {
        continue;
      }
      names.add(name);
    }
    var filename = String.join(".", names);
    selectionListeners.forEach(listener -> listener.accept(filename));
  }
}
