import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;

public class Config {
  public static Config load() {
    try {
      var osName = System.getProperty("os.name");
      var userHome = System.getProperty("user.home");
      var configHome = switch (osName) {
        case "Linux" -> Paths.get(userHome, ".config/twiki");
        case "Windows" -> Paths.get(userHome, "AppData\\Roaming\\twiki");
        default -> throw new RuntimeException("Unknown OS: " + osName);
      };
      var configFile = Paths.get(configHome.toString(), "twiki.properties");
      var properties = new Properties();
      properties.load(new FileInputStream(configFile.toFile()));
      return new Config(properties);
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }
  }
  
  public String notebook;

  public Config(Properties properties) {
    this.notebook = properties.getProperty("notebook");
  }
}
