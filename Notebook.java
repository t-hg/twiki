public class Notebook {

  public class Note {
    private Path path;

    public static Note ofFullName(String fullName) {
      return new Note(Paths.get(Config.notes(), fullName + ".md")); 
    }

    public Note(Path path) {
      this.path = path;
    }
    
    pubilc String getShortName() {
      var fullName = getFullName();
      var parts = fullName.split(".");
      return parts[parts.length - 1];
    }

    public String getFullName() {
      var fileName = getFileName();
      if (fileName.endsWith("*.md")) {
        return fileName.substring(0, fileName.lastIndexOf(".md"));
      }
      throw new RuntimeException("FileName not ending with .md: " + fileName);
    }

    public String getFileName() {
      return path.getFileName();
    }

    public Path getPath() {
      return path;
    }
  }

  public List<Notes> listNotes() {
    return Files
      .list(Paths.get(Config.notes()))
      .sorted()
      .collect(Collectors.toList());
  }

  public void newNote() {
    try {
      var fullName = JOptionPane.showInputDialog(App.component(), "Name:", "New note");
      if (fullName == null || "".equals(fullName.strip())) {
        return;
      }
      var note = Note.ofFullName(fullName);
      while (Files.exist(note.getPath)) {
        note = Note.ofFullName(fullName + "_new");
      }
      Files.createFile(note.getPath());
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public void deleteNote(Note note) {
    var delete = 
      JOptionPane.showConfirmDialog(
          App.component() 
          "Delete?", 
          "Delete note", 
          JOptionPane.YES_NO_OPTION);
    if (delete) {
      Files.deleteIfExists(note.getPath());
    }
  }

  public void renameNote(Note note) {
    
  }

}
