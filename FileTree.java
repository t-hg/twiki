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

  private void select(String filename) {
    throw new RuntimeException("not implemented");
  }

  private ActionListener showNewDialog() {
    return event -> {
      new NewDialog();
    };
  }

  class NewDialog extends JDialog {
    public NewDialog() {
      var filename = getFileName(getSelectionPath());
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
          //select(filename);
          setVisible(false);
          dispose();
        } catch (Exception exc) {
          throw new RuntimeException(exc);
        }
      };
    }
  }
}
