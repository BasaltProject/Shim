/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class AWTKeyStroke implements Serializable {
  private static final long serialVersionUID = -6430539691155161871L;

  private static final Map<AWTKeyStroke, AWTKeyStroke> cache = new HashMap<>();
  private static AWTKeyStroke cacheKey = new AWTKeyStroke();
  private static Class<?> ctorClass = AWTKeyStroke.class;
  private static Map<String, Integer> nameToCode;
  private static Map<Integer, String> codeToName;

  private char keyChar = KeyEvent.CHAR_UNDEFINED;
  private int keyCode = KeyEvent.VK_UNDEFINED;
  private int modifiers;
  private boolean onKeyRelease;

  protected AWTKeyStroke() { }

  protected AWTKeyStroke(char keyChar, int keyCode, int modifiers, boolean onKeyRelease) {
    this.keyChar = keyChar;
    this.keyCode = keyCode;
    this.modifiers = modifiers;
    this.onKeyRelease = onKeyRelease;
  }

  protected static void registerSubclass(Class<?> subclass) {
    if (subclass == null) {
      throw new IllegalArgumentException("subclass cannot be null");
    }
    synchronized (AWTKeyStroke.class) {
      if (ctorClass != null && ctorClass.equals(subclass)) {
        return;
      }
    }
    if (!AWTKeyStroke.class.isAssignableFrom(subclass)) {
      throw new ClassCastException("subclass is not derived from AWTKeyStroke");
    }
    try {
      java.lang.reflect.Constructor<?> c = subclass.getDeclaredConstructor();
      c.setAccessible(true);
      c.newInstance();
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("subclass could not be instantiated");
    } catch (Exception e) {
      throw new IllegalArgumentException("subclass could not be instantiated");
    }
    synchronized (AWTKeyStroke.class) {
      ctorClass = subclass;
      cache.clear();
      cacheKey = newInstance();
    }
  }

  private static AWTKeyStroke newInstance() {
    try {
      java.lang.reflect.Constructor<?> c = ctorClass.getDeclaredConstructor();
      c.setAccessible(true);
      return (AWTKeyStroke) c.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static synchronized AWTKeyStroke getCachedStroke(char keyChar, int keyCode, int modifiers,
      boolean onKeyRelease) {
    AWTKeyStroke key = cacheKey;
    key.keyChar = keyChar;
    key.keyCode = keyCode;
    key.modifiers = mapNewModifiers(mapOldModifiers(modifiers));
    key.onKeyRelease = onKeyRelease;
    AWTKeyStroke value = cache.get(key);
    if (value == null) {
      value = key;
      cache.put(value, value);
      cacheKey = newInstance();
    }
    return value;
  }

  public static AWTKeyStroke getAWTKeyStroke(char keyChar) {
    return getCachedStroke(keyChar, KeyEvent.VK_UNDEFINED, 0, false);
  }

  public static AWTKeyStroke getAWTKeyStroke(Character keyChar, int modifiers) {
    if (keyChar == null) {
      throw new IllegalArgumentException("keyChar cannot be null");
    }
    return getCachedStroke(keyChar.charValue(), KeyEvent.VK_UNDEFINED, modifiers, false);
  }

  public static AWTKeyStroke getAWTKeyStroke(int keyCode, int modifiers, boolean onKeyRelease) {
    return getCachedStroke(KeyEvent.CHAR_UNDEFINED, keyCode, modifiers, onKeyRelease);
  }

  public static AWTKeyStroke getAWTKeyStroke(int keyCode, int modifiers) {
    return getCachedStroke(KeyEvent.CHAR_UNDEFINED, keyCode, modifiers, false);
  }

  public static AWTKeyStroke getAWTKeyStrokeForEvent(KeyEvent anEvent) {
    int id = anEvent.getID();
    switch (id) {
      case KeyEvent.KEY_PRESSED:
      case KeyEvent.KEY_RELEASED:
        return getCachedStroke(KeyEvent.CHAR_UNDEFINED, anEvent.getKeyCode(), anEvent.getModifiers(),
            id == KeyEvent.KEY_RELEASED);
      case KeyEvent.KEY_TYPED:
        return getCachedStroke(anEvent.getKeyChar(), KeyEvent.VK_UNDEFINED, anEvent.getModifiers(), false);
      default:
        return null;
    }
  }

  private static synchronized void ensureNames() {
    if (nameToCode != null) {
      return;
    }
    nameToCode = new HashMap<>();
    codeToName = new HashMap<>();
    for (Field f : KeyEvent.class.getDeclaredFields()) {
      int m = f.getModifiers();
      if (Modifier.isStatic(m) && Modifier.isPublic(m) && f.getName().startsWith("VK_") && f.getType() == int.class) {
        try {
          String n = f.getName().substring(3);
          int code = f.getInt(null);
          nameToCode.put(n, code);
          if (!codeToName.containsKey(code)) {
            codeToName.put(code, n);
          }
        } catch (IllegalAccessException e) {
          // skip
        }
      }
    }
  }

  public static AWTKeyStroke getAWTKeyStroke(String s) {
    if (s == null) {
      throw new IllegalArgumentException("String cannot be null");
    }
    ensureNames();
    StringTokenizer t = new StringTokenizer(s, " ");
    int mask = 0;
    boolean released = false;
    boolean typed = false;
    boolean pressed = false;
    String errmsg = "String formatted incorrectly";
    int count = t.countTokens();
    if (count == 0) {
      throw new IllegalArgumentException(errmsg);
    }
    for (int i = 1; i <= count; i++) {
      String token = t.nextToken();
      if (typed) {
        if (token.length() != 1 || i != count) {
          throw new IllegalArgumentException(errmsg);
        }
        return getCachedStroke(token.charAt(0), KeyEvent.VK_UNDEFINED, mask, false);
      }
      if (pressed || released || i == count) {
        if (i != count) {
          throw new IllegalArgumentException(errmsg);
        }
        Integer code = nameToCode.get(token);
        if (code == null) {
          if (token.length() == 1) {
            Integer c = nameToCode.get(token.toUpperCase());
            if (c != null) {
              code = c;
            }
          }
          if (code == null) {
            throw new IllegalArgumentException(errmsg);
          }
        }
        return getCachedStroke(KeyEvent.CHAR_UNDEFINED, code.intValue(), mask, released);
      }
      switch (token) {
        case "shift": mask |= InputEvent.SHIFT_DOWN_MASK; break;
        case "control":
        case "ctrl": mask |= InputEvent.CTRL_DOWN_MASK; break;
        case "meta": mask |= InputEvent.META_DOWN_MASK; break;
        case "alt": mask |= InputEvent.ALT_DOWN_MASK; break;
        case "altGraph": mask |= InputEvent.ALT_GRAPH_DOWN_MASK; break;
        case "button1": mask |= InputEvent.BUTTON1_DOWN_MASK; break;
        case "button2": mask |= InputEvent.BUTTON2_DOWN_MASK; break;
        case "button3": mask |= InputEvent.BUTTON3_DOWN_MASK; break;
        case "typed": typed = true; break;
        case "pressed": pressed = true; break;
        case "released": released = true; break;
        default: throw new IllegalArgumentException(errmsg);
      }
    }
    throw new IllegalArgumentException(errmsg);
  }

  public final char getKeyChar() { return keyChar; }

  public final int getKeyCode() { return keyCode; }

  public final int getModifiers() { return modifiers; }

  public final boolean isOnKeyRelease() { return onKeyRelease; }

  public final int getKeyEventType() {
    if (keyCode == KeyEvent.VK_UNDEFINED) {
      return KeyEvent.KEY_TYPED;
    }
    return onKeyRelease ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED;
  }

  public int hashCode() {
    return (((int) keyChar) + 1) * (2 * (keyCode + 1)) * (modifiers + 1) + (onKeyRelease ? 1 : 2);
  }

  public final boolean equals(Object anObject) {
    if (anObject instanceof AWTKeyStroke) {
      AWTKeyStroke ks = (AWTKeyStroke) anObject;
      return ks.keyChar == keyChar && ks.keyCode == keyCode && ks.onKeyRelease == onKeyRelease
          && ks.modifiers == modifiers;
    }
    return false;
  }

  public String toString() {
    if (keyCode == KeyEvent.VK_UNDEFINED) {
      return getModifiersText(modifiers) + "typed " + keyChar;
    }
    return getModifiersText(modifiers) + (onKeyRelease ? "released" : "pressed") + " " + getVKText(keyCode);
  }

  static String getModifiersText(int modifiers) {
    StringBuilder buf = new StringBuilder();
    if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
      buf.append("shift ");
    }
    if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
      buf.append("ctrl ");
    }
    if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
      buf.append("meta ");
    }
    if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
      buf.append("alt ");
    }
    if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
      buf.append("altGraph ");
    }
    if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0) {
      buf.append("button1 ");
    }
    if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0) {
      buf.append("button2 ");
    }
    if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0) {
      buf.append("button3 ");
    }
    return buf.toString();
  }

  static String getVKText(int keyCode) {
    ensureNames();
    String n = codeToName.get(keyCode);
    if (n != null) {
      return n;
    }
    int expected = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
    return "UNKNOWN";
  }

  private static int mapOldModifiers(int modifiers) {
    if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
      modifiers |= InputEvent.SHIFT_DOWN_MASK;
    }
    if ((modifiers & InputEvent.ALT_MASK) != 0) {
      modifiers |= InputEvent.ALT_DOWN_MASK;
    }
    if ((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
      modifiers |= InputEvent.ALT_GRAPH_DOWN_MASK;
    }
    if ((modifiers & InputEvent.CTRL_MASK) != 0) {
      modifiers |= InputEvent.CTRL_DOWN_MASK;
    }
    if ((modifiers & InputEvent.META_MASK) != 0) {
      modifiers |= InputEvent.META_DOWN_MASK;
    }
    modifiers &= InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK
        | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK
        | InputEvent.BUTTON2_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK;
    return modifiers;
  }

  private static int mapNewModifiers(int modifiers) {
    if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
      modifiers |= InputEvent.SHIFT_MASK;
    }
    if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
      modifiers |= InputEvent.ALT_MASK;
    }
    if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
      modifiers |= InputEvent.ALT_GRAPH_MASK;
    }
    if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
      modifiers |= InputEvent.CTRL_MASK;
    }
    if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
      modifiers |= InputEvent.META_MASK;
    }
    return modifiers;
  }

  protected Object readResolve() throws ObjectStreamException {
    synchronized (AWTKeyStroke.class) {
      if (getClass().equals(ctorClass)) {
        return getCachedStroke(keyChar, keyCode, modifiers, onKeyRelease);
      }
    }
    return getCachedStroke(keyChar, keyCode, modifiers, onKeyRelease);
  }
}
