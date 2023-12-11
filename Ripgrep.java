import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class Ripgrep {
  public record SearchResult(String filename, int count) {};

  public static List<SearchResult> search(String searchString) {
    try {
      var path = Paths.get(Config.notes());
      var process = 
        new ProcessBuilder(
            Config.ripgrep(),
            "-i", "-c", searchString,
            path.toString())
        .start();
      process.waitFor();
      if (process.exitValue() > 1) { // 0: found, 
                                     // 1: nothing found, 
                                     // 2: error
        throw new RuntimeException(
            process.errorReader()
                   .lines()
                   .collect(Collectors.joining(System.lineSeparator())));
      } 
      return process.inputReader()
                   .lines()
                   .map(String::strip)
                   .map(str ->  {
                     var index = str.lastIndexOf(":");
                     return new SearchResult(
                       Paths.get(str.substring(0, index)).getFileName().toString(), 
                       Integer.valueOf(str.substring(index + 1)));
                   })
                   .sorted((a, b) -> b.count() - a.count())
                   .collect(Collectors.toList());
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }
}
