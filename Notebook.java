import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import javax.swing.*;

public class Notebook {

  public List<Note> listNotes() {
    try {
      return Files.list(Paths.get(Config.notes()))
                  .sorted(Comparator.comparing(Path::toString))
                  .map(Note::new)
                  .collect(Collectors.toList());
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public Optional<Note> newNote() {
    try {
      var fullName = JOptionPane.showInputDialog(App.instance(), "Name:", "New note");
      if (fullName == null || "".equals(fullName.strip())) {
        return Optional.empty();
      }
      var note = Note.ofFullName(fullName);
      while (Files.exists(note.getPath())) {
        note = Note.ofFullName(fullName + "_new");
      }
      Files.createFile(note.getPath());
      return Optional.of(note);
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public void deleteNote(Note note) {
    try {
      var answer = 
        JOptionPane.showConfirmDialog(
            App.instance(),
            "Delete?", 
            "Delete note", 
            JOptionPane.YES_NO_OPTION);
      if (answer == JOptionPane.YES_OPTION) {
        Files.deleteIfExists(note.getPath());
      }
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public void renameNote(Note note) {
    try {
      var fullName = 
        (String) JOptionPane.showInputDialog(
            App.instance(), 
            "Name:",
            "Rename note",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null, 
            note.getFullName()) ;
      note = Note.ofFullName(fullName);
      while (Files.exists(note.getPath())) {
        note = Note.ofFullName(note.getFullName() + "_rename");
      }
      Files.createFile(note.getPath());
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

}
