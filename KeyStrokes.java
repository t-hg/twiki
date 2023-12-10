import java.awt.event.*;
import javax.swing.*;

import static java.awt.event.KeyEvent.*;

public class KeyStrokes {
  public static KeyStroke CTRL_0 = ctrl(VK_0);
  public static KeyStroke CTRL_1 = ctrl(VK_1);
  public static KeyStroke CTRL_2 = ctrl(VK_2);
  public static KeyStroke CTRL_3 = ctrl(VK_3);
  public static KeyStroke CTRL_4 = ctrl(VK_4);
  public static KeyStroke CTRL_5 = ctrl(VK_5);
  public static KeyStroke CTRL_6 = ctrl(VK_6);
  public static KeyStroke CTRL_B = ctrl(VK_B);
  public static KeyStroke CTRL_E = ctrl(VK_E);
  public static KeyStroke CTRL_F = ctrl(VK_F);
  public static KeyStroke CTRL_I = ctrl(VK_I);
  public static KeyStroke CTRL_N = ctrl(VK_N);
  public static KeyStroke CTRL_R = ctrl(VK_R);
  public static KeyStroke CTRL_S = ctrl(VK_S);
  public static KeyStroke CTRL_U = ctrl(VK_U);
  public static KeyStroke CTRL_V = ctrl(VK_V);
  public static KeyStroke CTRL_Y = ctrl(VK_Y);
  public static KeyStroke CTRL_Z = ctrl(VK_Z);

  public static KeyStroke CTRL_SHIFT_C = ctrlShift(VK_C);
  public static KeyStroke CTRL_SHIFT_F = ctrlShift(VK_F);
  public static KeyStroke CTRL_SHIFT_I = ctrlShift(VK_I);
  public static KeyStroke CTRL_SHIFT_U = ctrlShift(VK_U);
  public static KeyStroke CTRL_SHIFT_W = ctrlShift(VK_W);

  public static KeyStroke DEL   = plain(VK_DELETE);
  public static KeyStroke ENTER = plain(VK_ENTER);
  public static KeyStroke ESC   = plain(VK_ESCAPE);
  public static KeyStroke F2    = plain(VK_F2);
  public static KeyStroke F12   = plain(VK_F12);

  public static KeyStroke SHIFT_ENTER = shift(VK_ENTER);

  private static KeyStroke plain(int keyCode) {
    return KeyStroke.getKeyStroke(keyCode, 0);
  }

  private static KeyStroke ctrl(int keyCode) {
    return KeyStroke.getKeyStroke(keyCode, CTRL_DOWN_MASK);
  }
  
  private static KeyStroke shift(int keyCode) {
    return KeyStroke.getKeyStroke(keyCode, SHIFT_DOWN_MASK);
  }
  
  private static KeyStroke ctrlShift(int keyCode) {
    return KeyStroke.getKeyStroke(keyCode, CTRL_DOWN_MASK | SHIFT_DOWN_MASK);
  }
}
