import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

public interface Editor {
  void onSearch(String searchString);
  void onFileSelected(String name);
  boolean hasUnsavedChanges();

  static void onSearch(JTextPane textPane, String searchString) {
    try {
      var highlighter = textPane.getHighlighter();
      highlighter.removeAllHighlights();
      if (searchString == null || "".equals(searchString.strip())) {
        return;
      }
      var highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
      var document = textPane.getDocument();
      var text = document.getText(0, document.getLength());
      int index = 0;
      boolean firstOccurence = true;
      while ((index = text.indexOf(searchString, index)) > -1) {
        if (firstOccurence) {
          textPane.setCaretPosition(index);
          firstOccurence = false;
        }
        highlighter.addHighlight(index, searchString.length() + index, highlightPainter);
        index = index + searchString.length();
      }
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }
}
