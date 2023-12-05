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
    try {
      var root = new DefaultMutableTreeNode("root");
      var model = new DefaultTreeModel(root);
      setModel(model);
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
      expandRow(0);
      setRootVisible(false);
      addTreeSelectionListener(onSelected());
      registerKeyboardAction(newFile(), KeyStrokes.CTRL_N, JComponent.WHEN_FOCUSED);
    } catch (Exception exc) {
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

  private TreeSelectionListener onSelected() {
    return event -> {
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
    };
  }

  private ActionListener newFile() {
    return event -> {
      new NewDialog();
    };
  }

  class NewDialog extends JDialog {
    public NewDialog() {
      var textField = new JTextField();
      textField.addActionListener(System.out::println);
      setTitle("New");
      setModal(true);
      add(textField); 
      pack();
      setSize(300, 70);
      setLocationRelativeTo(null);
      setVisible(true);
    }
  }
}
