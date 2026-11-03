/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;

public abstract class LookAndFeel {
  public static void installColors(JComponent c, String defaultBgName, String defaultFgName) {
    Color bg = c.getBackground();
    if (bg == null || bg instanceof UIResource) {
      c.setBackground(UIManager.getColor(defaultBgName));
    }
    Color fg = c.getForeground();
    if (fg == null || fg instanceof UIResource) {
      c.setForeground(UIManager.getColor(defaultFgName));
    }
  }

  public static void installColorsAndFont(JComponent c, String defaultBgName, String defaultFgName,
      String defaultFontName) {
    Font f = c.getFont();
    if (f == null || f instanceof UIResource) {
      c.setFont(UIManager.getFont(defaultFontName));
    }
    installColors(c, defaultBgName, defaultFgName);
  }

  public static void installBorder(JComponent c, String defaultBorderName) {
    Border b = c.getBorder();
    if (b == null || b instanceof UIResource) {
      c.setBorder(UIManager.getBorder(defaultBorderName));
    }
  }

  public static void uninstallBorder(JComponent c) {
    if (c.getBorder() instanceof UIResource) {
      c.setBorder(null);
    }
  }

  public static void installProperty(JComponent c, String propertyName, Object propertyValue) {
    c.setUIProperty(propertyName, propertyValue);
  }

  public static JTextComponent.KeyBinding[] makeKeyBindings(Object[] keyBindingList) {
    JTextComponent.KeyBinding[] rv = new JTextComponent.KeyBinding[keyBindingList.length / 2];
    for (int i = 0; i < rv.length; i++) {
      Object o = keyBindingList[2 * i];
      KeyStroke keyStroke = (o instanceof KeyStroke) ? (KeyStroke) o : KeyStroke.getKeyStroke((String) o);
      String action = (String) keyBindingList[2 * i + 1];
      rv[i] = new JTextComponent.KeyBinding(keyStroke, action);
    }
    return rv;
  }

  public static InputMap makeInputMap(Object[] keys) {
    InputMap retMap = new javax.swing.plaf.InputMapUIResource();
    loadKeyBindings(retMap, keys);
    return retMap;
  }

  public static ComponentInputMap makeComponentInputMap(JComponent c, Object[] keys) {
    ComponentInputMap retMap = new javax.swing.plaf.ComponentInputMapUIResource(c);
    loadKeyBindings(retMap, keys);
    return retMap;
  }

  public static void loadKeyBindings(InputMap retMap, Object[] keys) {
    if (keys != null) {
      for (int i = 0; i < keys.length - 1; i += 2) {
        Object key = keys[i];
        KeyStroke keyStroke = (key instanceof KeyStroke) ? (KeyStroke) key : KeyStroke.getKeyStroke((String) key);
        retMap.put(keyStroke, keys[i + 1]);
      }
    }
  }

  public static Object makeIcon(final Class<?> baseClass, final String gifFile) {
    return new UIDefaults.LazyValue() {
      public Object createValue(UIDefaults table) {
        java.io.InputStream resource = baseClass.getResourceAsStream(gifFile);
        if (resource == null) {
          System.err.println(baseClass.getName() + "/" + gifFile + " not found.");
          return null;
        }
        try {
          return new javax.swing.plaf.IconUIResource(new ImageIcon(resource.readAllBytes()));
        } catch (java.io.IOException e) {
          return null;
        }
      }
    };
  }

  public static Object getDesktopPropertyValue(String systemPropertyName, Object fallbackValue) {
    Object value = Toolkit.getDefaultToolkit().getDesktopProperty(systemPropertyName);
    return value == null ? fallbackValue : value;
  }

  public Icon getDisabledIcon(JComponent component, Icon icon) {
    if (icon instanceof ImageIcon) {
      return new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) icon).getImage()));
    }
    return null;
  }

  public Icon getDisabledSelectedIcon(JComponent component, Icon icon) {
    return getDisabledIcon(component, icon);
  }

  public abstract String getName();

  public abstract String getID();

  public abstract String getDescription();

  public boolean getSupportsWindowDecorations() { return false; }

  public LayoutStyle getLayoutStyle() { return shim.swing.MetalLayoutStyle.kInstance; }

  public abstract boolean isNativeLookAndFeel();

  public abstract boolean isSupportedLookAndFeel();

  public void initialize() { }

  public void uninitialize() { }

  public UIDefaults getDefaults() { return null; }

  public void provideErrorFeedback(Component component) {
    Toolkit.getDefaultToolkit().beep();
  }

  public String toString() { return "[" + getDescription() + " - " + getClass().getName() + "]"; }
}
