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

  private Notebook notebook = new Notebook();
  private java.util.List<Consumer<Note>> selectionListeners = new ArrayList<>();

  public FileTree() {
    refresh();
    expandRow(0);
    setRootVisible(false);
    addMouseListener(mouseListenerOnSelected());
    addTreeSelectionListener(treeSelectionListenerOnSelected());
    registerKeyboardAction(showNewDialog(), KeyStrokes.CTRL_N, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(showDeleteDialog(), KeyStrokes.DEL, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(showRenameDialog(), KeyStrokes.F2, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(triggerRefresh(), KeyStrokes.CTRL_R, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(triggerClearSelection(), KeyStrokes.ESC, JComponent.WHEN_FOCUSED);
  }

  private MouseListener mouseListenerOnSelected() {
    return new MouseAdapter() {
      public void mouseReleased(MouseEvent event) {
        var path = getClosestPathForLocation(event.getX(), event.getY());
        if (path == null) return;
        setSelectionPath(path);
        if (event.getClickCount() > 1) {
          expandPath(path);
        }
      }
    };
  }
  
  private TreeSelectionListener treeSelectionListenerOnSelected() {
    return event -> {
      var path = event.getPath();
      var fullName = getFullName(path);
      var note = Note.ofFullName(fullName);
      selectionListeners.forEach(listener -> listener.accept(note));
    };
  }

  private ActionListener triggerClearSelection() {
    return event -> clearSelection();
  }

  private ActionListener triggerRefresh() {
    return event -> refresh();
  }

  private void refresh() {
    var model = (DefaultTreeModel) getModel();
    var expanded = getExpandedDescendants(new TreePath(model.getRoot()));
    var root = new DefaultMutableTreeNode("root");
    notebook.listNotes().forEach(note -> {
      var node = root;
      for (var part : note.getFullName().split("\\.")) {
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
    while(expanded != null && expanded.hasMoreElements()) {
      var treePath = expanded.nextElement();
      var fullName = getFullName(treePath);
      var note = Note.ofFullName(fullName);
      expandFileName(note, false);
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

  public void addSelectionListener(Consumer<Note> listener) {
    selectionListeners.add(listener);
  }

  private String getFullName(TreePath path) {
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

  private void expandFileName(Note note, boolean select) {
    var fullName = note.getFullName();
    var parts = fullName.split("\\.");
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
      notebook
        .newNote()
        .ifPresent(note -> {
          refresh();
          expandFileName(note, true);
        });
    };
  }

  private ActionListener showDeleteDialog() {
    return event -> {
      var fullName = getSelectionPath() != null ? getFullName(getSelectionPath()) : "";
      if (fullName == null || "".equals(fullName.strip())) {
        return;
      }
      var note = Note.ofFullName(fullName);
      notebook.deleteNote(note);
      refresh();
    };
  }
  
  private ActionListener showRenameDialog() {
    return event -> {
      var fullName = getSelectionPath() != null ? getFullName(getSelectionPath()) : "";
      if (fullName == null || "".equals(fullName.strip())) {
        return;
      }
      var note = Note.ofFullName(fullName);
      notebook.renameNote(note);
      refresh();
    };
  }
}
