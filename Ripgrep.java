import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;

public class Ripgrep {
  public record SearchResult(Note note, int count) {};

  public static List<SearchResult> search(String searchString) {
    try {
      var path = Paths.get(Config.notes());
      var process = 
        new ProcessBuilder(
            Config.ripgrep(),
            "-i", "-c", searchString,
            path.toString())
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start();
      var output = 
        process.inputReader(StandardCharsets.UTF_8)
               .lines()
               .collect(Collectors.joining(System.lineSeparator()));
      process.waitFor(30, TimeUnit.SECONDS);
      if (process.exitValue() > 1) { 
        // 0: found, 
        // 1: nothing found, 
        // 2: error
        throw new RuntimeException(output);
      } 
      return output.lines()
                   .map(String::strip)
                   .map(str ->  {
                     var index = str.lastIndexOf(":");
                     return new SearchResult(
                       new Note(Paths.get(str.substring(0, index))), 
                       Integer.valueOf(str.substring(index + 1)));
                   })
                   .sorted((a, b) -> b.count() - a.count())
                   .collect(Collectors.toList());
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }
}
