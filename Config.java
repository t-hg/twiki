import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;

public class Config {
  private static Properties properties = new Properties();

  public static void load() {
    try {
      var osName = System.getProperty("os.name");
      var userHome = System.getProperty("user.home");
      var configHome = switch (osName) {
        case "Linux" -> Paths.get(userHome, ".config/twiki");
        case "Windows" -> Paths.get(userHome, "AppData\\Roaming\\twiki");
        default -> throw new RuntimeException("Unknown OS: " + osName);
      };
      var configFile = Paths.get(configHome.toString(), "twiki.properties");
      properties.load(new FileInputStream(configFile.toFile()));
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }

  public static String notebook() {
    return properties.getProperty("notebook");
  }

  public static String pandoc() {
    return properties.getProperty("pandoc");
  }
}
