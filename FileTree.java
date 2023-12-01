import javax.swing.*;
import javax.swing.tree.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileTree extends JTree {
  private Config config;

  public FileTree(Config config) {
    try {
      this.config = config;
      var root = new DefaultMutableTreeNode("root");
      var model = new DefaultTreeModel(root);
      setModel(model);
      Files
        .list(Paths.get(config.notebook))
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
      expandRow(0);
      setRootVisible(false);
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }
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
}
