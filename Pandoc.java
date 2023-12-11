import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.*;

public class Pandoc {
  public static void htmlToMarkdown(String filename, String text) {
    try {
      var process = 
        new ProcessBuilder(
            Config.pandoc(), 
            "-f", "html", 
            "-t", "markdown",
            "-o", Paths.get(Config.notes(), filename).toString())
        .start();
      process.outputWriter().write(text);
      process.outputWriter().flush();
      process.outputWriter().close();
      process.waitFor(30, TimeUnit.SECONDS);
      if (process.exitValue() != 0) {
        throw new RuntimeException(
            process
              .errorReader()
              .lines()
              .collect(Collectors.joining(System.lineSeparator())));
      } 
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public static String markdownToHtml(String filename) {
    try {
      var path = Paths.get(Config.notes(), filename);
      if (!Files.exists(path)) {
        return "";
      }
      var process = 
        new ProcessBuilder(
            Config.pandoc(), 
            "-f", "markdown", 
            "-t", "html", 
            path.toString())
        .start();
      process.waitFor(30, TimeUnit.SECONDS);
      if (process.exitValue() != 0) {
        throw new RuntimeException(
            process.errorReader()
                   .lines()
                   .collect(Collectors.joining(System.lineSeparator())));
      } 
      var html = 
        process.inputReader()
               .lines()
               .collect(Collectors.joining(System.lineSeparator()));
      return html;
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }
}
