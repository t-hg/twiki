import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class FileTree extends JTree {

  private java.util.List<Consumer<String>> selectionListeners = new ArrayList<>();

  public FileTree() {
    refresh();
    expandRow(0);
    setRootVisible(false);
    addTreeSelectionListener(onSelected());
    registerKeyboardAction(showNewDialog(), KeyStrokes.CTRL_N, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(triggerRefresh(), KeyStrokes.CTRL_R, JComponent.WHEN_FOCUSED);
  }

  private ActionListener triggerRefresh() {
    return event -> refresh();
  }

  private void refresh() {
    try {
      var model = (DefaultTreeModel) getModel();
      var expanded = getExpandedDescendants(new TreePath(model.getRoot()));
      var root = new DefaultMutableTreeNode("root");
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
      model.setRoot(root);
      model.reload();
      while(expanded.hasMoreElements()) {
        var treePath = expanded.nextElement();
        var filename = getFileName(treePath);
        expandFileName(filename, false);
      }
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

  public void addSelectionListener(Consumer<String> listener) {
    selectionListeners.add(listener);
  }

  private String getFileName(TreePath path) {
    var names = new ArrayList<String>();
    for(var part : path.getPath()) {
      var name = part.toString();
      if ("root".equals(name)) {
        continue;
      }
      names.add(name);
    }
    return String.join(".", names);
  }

  private TreeSelectionListener onSelected() {
    return event -> {
      var path = event.getPath();
      var filename = getFileName(path);
      selectionListeners.forEach(listener -> listener.accept(filename));
    };
  }

  private void expandFileName(String filename, boolean select) {
    var parts = filename.split("\\.");
    var stack = new Stack<String>();
    for (int i = parts.length - 1; i >= 0; i--) {
      stack.push(parts[i]);
    }
    var row = 0;
    while(!stack.isEmpty()) {
      var part = stack.pop();
      for (; row < getRowCount(); row++) {
        var treePath = getPathForRow(row);
        var nodes = treePath.getPath();
        var lastComponent = nodes[nodes.length - 1];
        if (lastComponent.toString().equals(part)) {
          expandPath(treePath);
          if (select) {
            setSelectionPath(treePath);
          }
          break;
        }
      }
    }
  }

  private ActionListener showNewDialog() {
    return event -> {
      new NewDialog();
    };
  }

  class NewDialog extends JDialog {
    public NewDialog() {
      var filename = getSelectionPath() != null ? getFileName(getSelectionPath()) : "";
      var textField = new JTextField(filename);
      textField.addActionListener(addNewFile());
      setTitle("New");
      setModal(true);
      add(textField); 
      pack();
      setSize(300, 70);
      setLocationRelativeTo(null);
      setVisible(true);
    }

    private ActionListener addNewFile() {
      return event -> {
        try {
          var filename = ((JTextField) event.getSource()).getText();
          var path = Paths.get(Config.notebook(), filename);
          while (Files.exists(path)) {
            filename = filename + "_copy";
            path = Paths.get(Config.notebook(), filename);
          }
          Files.createFile(path);
          refresh();
          expandFileName(filename, true);
          setVisible(false);
          dispose();
        } catch (Exception exc) {
          throw new RuntimeException(exc);
        }
      };
    }
  }
}
