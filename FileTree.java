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

  public FileTree() {
    refresh();
    expandRow(0);
    setRootVisible(false);
    setShowsRootHandles(true);
    addMouseListener(mouseListenerOnSelected());
    addTreeSelectionListener(treeSelectionListenerOnSelected());
    registerKeyboardAction(showNewDialog(), KeyStrokes.CTRL_N, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(showDeleteDialog(), KeyStrokes.DEL, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(showRenameDialog(), KeyStrokes.F2, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(openInFileBrowser(), KeyStrokes.F3, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(triggerRefresh(), KeyStrokes.CTRL_R, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(triggerClearSelection(), KeyStrokes.ESC, JComponent.WHEN_FOCUSED);
    setToggleClickCount(-1);
  }

  public void selectNote(Note note) {
    expandNote(note, true);
  }

  private MouseListener mouseListenerOnSelected() {
    return new MouseAdapter() {
      public void mouseReleased(MouseEvent event) {
        var path = getClosestPathForLocation(event.getX(), event.getY());
        if (path == null) return;
        setSelectionPath(path);
        if (event.getClickCount() > 1) {
          if (isExpanded(path)) {
            collapsePath(path);
          } else {
            expandPath(path);
          }
        }
      }
    };
  }
  
  private TreeSelectionListener treeSelectionListenerOnSelected() {
    return event -> {
      var path = event.getPath();
      var fullName = getFullName(path);
      var note = Note.ofFullName(fullName);
      App.instance().getFileTabs().openNote(note);
      grabFocus();
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
      expandNote(note, false);
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

  private void expandNote(Note note, boolean select) {
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
      var parent =
        Optional.ofNullable(getSelectionPath())
                .map(path -> getFullName(path))
                .map(Note::ofFullName)
                .orElse(null);
      notebook
        .newNote(parent)
        .ifPresent(note -> {
          refresh();
          expandNote(note, true);
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

  private ActionListener openInFileBrowser() {
    return event -> {
      notebook.openInFileBrowser();
    };
  }
}
