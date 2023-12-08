import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.ref.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class FileTree extends JTree {

  private java.util.List<WeakReference<Consumer<String>>> selectionListeners = new ArrayList<>();

  public FileTree() {
    refresh();
    expandRow(0);
    setRootVisible(false);
    addTreeSelectionListener(onSelected());
    registerKeyboardAction(showNewDialog(), KeyStrokes.CTRL_N, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(showDeleteDialog(), KeyStrokes.DEL, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(showRenameDialog(), KeyStrokes.F2, JComponent.WHEN_FOCUSED);
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
    selectionListeners.add(new WeakReference<>(listener));
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
      selectionListeners.forEach(listener -> listener.get().accept(filename));
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

  private ActionListener showDeleteDialog() {
    return event -> {
      var filename = getSelectionPath() != null ? getFileName(getSelectionPath()) : "";
      if (filename == null || "".equals(filename.strip())) {
        return;
      }
      new DeleteDialog(filename);
    };
  }
  
  private ActionListener showRenameDialog() {
    return event -> {
      var filename = getSelectionPath() != null ? getFileName(getSelectionPath()) : "";
      if (filename == null || "".equals(filename.strip())) {
        return;
      }
      new RenameDialog(filename);
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
          setVisible(false);
          dispose();
          refresh();
          expandFileName(filename, true);
        } catch (Exception exc) {
          throw new RuntimeException(exc);
        }
      };
    }
  }
  
  class DeleteDialog extends JDialog {
    private String filename;

    public DeleteDialog(String filename) {
      this.filename = filename;
      setTitle("Delete " + filename);
      var button = new JButton("Delete");
      button.addActionListener(deleteFile());
      setModal(true);
      add(button); 
      setSize(300, 70);
      setLocationRelativeTo(null);
      setVisible(true);
    }

    private ActionListener deleteFile() {
      return event -> {
        try {
          var path = Paths.get(Config.notebook(), filename);
          Files.deleteIfExists(path);
          setVisible(false);
          dispose();
          refresh();
          expandFileName(filename, true);
        } catch (Exception exc) {
          throw new RuntimeException(exc);
        }
      };
    }
  }

  class RenameDialog extends JDialog {
    private String filename;

    public RenameDialog(String filename) {
      this.filename = filename;
      setTitle("Rename " + filename);
      var textField = new JTextField(filename);
      textField.addActionListener(renameFile());
      setModal(true);
      add(textField); 
      setSize(300, 70);
      setLocationRelativeTo(null);
      setVisible(true);
    }

    private ActionListener renameFile() {
      return event -> {
        try {
          var source = Paths.get(Config.notebook(), filename);
          var newFileName = ((JTextField) event.getSource()).getText();
          var target = Paths.get(Config.notebook(), newFileName);
          while (Files.exists(target)) {
            newFileName = newFileName + "_copy";
            target = Paths.get(Config.notebook(), newFileName);
          }
          Files.move(source, target);
          setVisible(false);
          dispose();
          refresh();
          expandFileName(filename, true);
        } catch (Exception exc) {
          throw new RuntimeException(exc);
        }
      };
    }
  }
}
