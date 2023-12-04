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
                    .orElse(null);
  }

  public static String pandoc() {
    return properties.getProperty("pandoc");
  }

  public static String stylesheet() {
    return properties.getProperty("stylesheet");
  }
}
