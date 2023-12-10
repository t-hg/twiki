import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;

public class Config {
  private static Properties properties = new Properties();

  public static void load() {
    try {
      properties.load(Config.class.getResourceAsStream("application.properties"));
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public static String notebook() {
    return Optional.ofNullable(properties.getProperty("notebook"))
                   .map(notebook -> notebook.replaceFirst("^~", System.getProperty("user.home")))
                   .orElse(Paths.get(System.getProperty("user.home"), "Documents", "twiki").toString());
  }
  
  public static String notes() {
    return Paths.get(notebook(), "notes").toString();
  }

  public static String attachments() {
    return Paths.get(notebook(), "attachments").toString();
  }

  public static String pandoc() {
    return properties.getProperty("pandoc");
  }
  
  public static String ripgrep() {
    return properties.getProperty("ripgrep");
  }

  public static String stylesheet() {
    return properties.getProperty("stylesheet");
  }
}
