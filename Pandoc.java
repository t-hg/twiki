import java.io.*;
import java.nio.*;
import java.nio.charset.*;
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
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start();
      var outputWriter = process.outputWriter(StandardCharsets.UTF_8);
      outputWriter.write(text);
      outputWriter.flush();
      outputWriter.close();
      var output =
        process.inputReader(StandardCharsets.UTF_8)
               .lines()
               .collect(Collectors.joining(System.lineSeparator()));
      process.waitFor(30, TimeUnit.SECONDS);
      if (process.exitValue() != 0) {
        throw new RuntimeException(output);
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
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start();
      var output = 
        process.inputReader(StandardCharsets.UTF_8)
               .lines()
               .collect(Collectors.joining(System.lineSeparator()));
      process.waitFor(30, TimeUnit.SECONDS);
      if (process.exitValue() != 0) {
        throw new RuntimeException(output);
      } 
      return output;
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }
}
