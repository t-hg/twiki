
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public class Editor extends JTextPane {
  private static KeyStroke CTRL_0 = KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_2 = KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_3 = KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_4 = KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_5 = KeyStroke.getKeyStroke(KeyEvent.VK_5, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_6 = KeyStroke.getKeyStroke(KeyEvent.VK_6, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_B = KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_I = KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_U = KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_S = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
  private static KeyStroke CTRL_SHIFT_C = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);

  public Editor() {
    var editorKit = new HTMLEditorKit();
    editorKit.setDefaultCursor(new Cursor(Cursor.TEXT_CURSOR));
    setEditorKit(editorKit);
    registerKeyboardAction(new HTMLEditorKit.BoldAction(), CTRL_B, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(new HTMLEditorKit.ItalicAction(), CTRL_I, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(new HTMLEditorKit.UnderlineAction(), CTRL_U, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.save(), CTRL_S, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toParagraph(), CTRL_0, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(1), CTRL_1, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(2), CTRL_2, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(3), CTRL_3, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(4), CTRL_4, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(5), CTRL_5, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.toHeading(6), CTRL_6, JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this.insertCodeBlock(), CTRL_SHIFT_C, JComponent.WHEN_FOCUSED);
  }

  private ActionListener save() {
    return event -> System.out.println(getText());
  }

  private ActionListener toHeading(int level) {
    return toTag("<h" + level + ">", "</h" + level + "/>");
  }

  private ActionListener toParagraph() {
    return toTag("<p style=\"margin-top:0;\">", "</p>");
  }

  private ActionListener toTag(String startTag, String endTag) {
    return event -> {
      try {
        var document = (HTMLDocument) getDocument();
        var pos = getCaretPosition();
        var element = document.getParagraphElement(pos);
        var offset = element.getStartOffset();
        var length = element.getEndOffset() - offset;
        var tag = startTag + document.getText(offset, length) + endTag;
        document.setOuterHTML(element, tag);
      } catch (BadLocationException | IOException exc) {
        throw new RuntimeException(exc);
      }
    };
  }

  private ActionListener insertCodeBlock() {
    var html = "<pre></pre>";
    return new HTMLEditorKit.InsertHTMLTextAction("pre", html, HTML.Tag.BODY, HTML.Tag.PRE);
  }
}
