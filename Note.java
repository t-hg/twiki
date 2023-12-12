import java.nio.file.*;
import java.util.*;

public class Note {
  private Path path;

  public static Note ofFullName(String fullName) {
    return new Note(Paths.get(Config.notes(), fullName + ".md")); 
  }

  public Note(Path path) {
    this.path = Objects.requireNonNull(path);
  }
  
  public String getShortName() {
    var fullName = getFullName();
    var parts = fullName.split("\\.");
    return parts[parts.length - 1];
  }

  public String getFullName() {
    var fileName = getFileName();
    if (fileName.endsWith(".md")) {
      return fileName.substring(0, fileName.lastIndexOf(".md"));
    }
    return fileName;
  }

  public String getFileName() {
    return path.getFileName().toString();
  }

  public Path getPath() {
    return path;
  }

  public int hashCode() {
    return Objects.hash(this.path);
  }

  public boolean equals(Note other) {
    if (other == null) {
      return false;
    }
    if (this == other) {
      return true;
    }
    return Objects.equals(this.path, other.path);
  }
}
