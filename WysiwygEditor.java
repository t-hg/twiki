
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.undo.*;

public class WysiwygEditor extends JTextPane implements Editor {
  private String filename;
  private UnsavedChangesTracker unsavedChangesTracker;

  public WysiwygEditor() {
    try {
      var editorKit = new HTMLEditorKit();
      editorKit.setDefaultCursor(new Cursor(Cursor.TEXT_CURSOR));

      var stylesheet = new StyleSheet();
      stylesheet.loadRules(new InputStreamReader(getClass().getResourceAsStream(Config.stylesheet())), null);
      editorKit.setStyleSheet(stylesheet);

      setEditorKit(editorKit);
      System.out.println(
          Arrays.stream(getActionMap().allKeys())
            .map(Object::toString)
            .sorted()
            .collect(Collectors.joining("\n")));

      var undoManager = new UndoManager();
      getDocument().addUndoableEditListener(undoManager);
      
      unsavedChangesTracker = new UnsavedChangesTracker();
      getDocument().addDocumentListener(unsavedChangesTracker);

      registerKeyboardAction(getActionMap().get("font-bold"), KeyStrokes.CTRL_B, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(getActionMap().get("font-italic"), KeyStrokes.CTRL_I, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(getActionMap().get("font-underline"), KeyStrokes.CTRL_U, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toParagraph(), KeyStrokes.CTRL_0, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(1), KeyStrokes.CTRL_1, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(2), KeyStrokes.CTRL_2, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(3), KeyStrokes.CTRL_3, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(4), KeyStrokes.CTRL_4, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(5), KeyStrokes.CTRL_5, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toHeading(6), KeyStrokes.CTRL_6, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(toCode(), KeyStrokes.CTRL_SHIFT_C, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(save(), KeyStrokes.CTRL_S, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(refresh(), KeyStrokes.CTRL_R, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(undo(undoManager), KeyStrokes.CTRL_Z, JComponent.WHEN_FOCUSED);
      registerKeyboardAction(redo(undoManager), KeyStrokes.CTRL_Y, JComponent.WHEN_FOCUSED);
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public void onSearch(String searchString) {
    Editor.onSearch(this, searchString);
  }

  public void onFileSelected(String name) {
    if (hasUnsavedChanges() && MessageDialogs.unsavedChanges(this) != 0) {
      return;
    }
    filename = name;
    setText(Pandoc.markdownToHtml(name));
    unsavedChangesTracker.reset();
  }

  public boolean hasUnsavedChanges() {
    return unsavedChangesTracker.hasUnsavedChanges();
  }

  private ActionListener toParagraph() {
    return event -> toTag("<p>", "</p>");
  }

  private ActionListener toHeading(int level) {
    return event -> toTag("<h" + level + ">", "</h" + level + ">");
  }

  private ActionListener toCode() {
    return event -> toTag("<pre><code>", "</code></pre>");
  }

  private void toTag(String startTag, String endTag) {
    try {
      var document = (HTMLDocument) getDocument();
      var element = document.getParagraphElement(getCaretPosition());
      var elementText = document.getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset()); 
      var parent = element.getParentElement();
      var parentText = document.getText(parent.getStartOffset(), parent.getEndOffset() - parent.getStartOffset()); 
    
      if ("pre".equals(parent.getName())) {
        if ("".equals(elementText.strip())) {
          document.insertAfterEnd(parent, startTag + endTag);
          setCaretPosition(getCaretPosition() + 1);
        } else {
          document.setOuterHTML(parent, startTag + parentText + endTag);
        }
      } else {
        document.setOuterHTML(element, startTag + elementText + endTag);
      }
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  private ActionListener save() {
    return event -> {
      if (filename == null) {
        return;
      }
      Pandoc.htmlToMarkdown(filename, getText());
      unsavedChangesTracker.reset();
    };
  }

  private ActionListener refresh() {
    return event -> {
      if (filename == null) {
        return;
      }
      onFileSelected(filename);
    };
  }

  private ActionListener undo(UndoManager undoManager) {
    return event -> {
      try {
        undoManager.undo();
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }
  
  private ActionListener redo(UndoManager undoManager) {
    return event -> {
      try {
        undoManager.redo();
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    };
  }
}
