import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.text.html.*;

public class Config {
  private static Properties properties = new Properties();

  public static void load() {
    Optional.ofNullable(System.getProperty("os.name")).ifPresent(Config::loadFromOS);
  }

  private static void loadFromOS(String os) {
    switch (os) {
      case "Windows" -> loadFromPath(Paths.get(System.getenv("APPDATA")));
      case "Linux"   -> loadFromPath(Paths.get(System.getProperty("user.home"), ".config"));
    }
  }

  private static void loadFromPath(Path path) {
    try {
      path = Paths.get(path.toString(), "twiki.properties");
      properties.load(new FileInputStream(path.toFile())); 
    } catch (FileNotFoundException exc) {
      // use sane defaults
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
    return Optional.ofNullable(properties.getProperty("pandoc")).orElse("pandoc");
  }
  
  public static String ripgrep() {
    return Optional.ofNullable(properties.getProperty("pandoc")).orElse("rg");
  }

  public static StyleSheet stylesheet() {
    try {
      var stylesheet = new StyleSheet();
      var path = properties.getProperty("stylesheet");
      InputStreamReader reader = 
        path != null 
          ? new FileReader(Paths.get(path).toFile())
          : new InputStreamReader(Config.class.getResourceAsStream("style.css"));
      stylesheet.loadRules(reader, null);
      return stylesheet;
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }
}
