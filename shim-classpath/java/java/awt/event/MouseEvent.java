/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.event;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class MouseEvent extends java.awt.event.InputEvent {
  int x;
  int y;
  int xAbs;
  int yAbs;
  int clickCount;
  int button;
  boolean popupTrigger = false;
  public static final int MOUSE_FIRST = 500;
  public static final int MOUSE_LAST = 507;
  public static final int MOUSE_CLICKED = 500;
  public static final int MOUSE_PRESSED = 501;
  public static final int MOUSE_RELEASED = 502;
  public static final int MOUSE_MOVED = 503;
  public static final int MOUSE_ENTERED = 504;
  public static final int MOUSE_EXITED = 505;
  public static final int MOUSE_DRAGGED = 506;
  public static final int MOUSE_WHEEL = 507;
  public static final int NOBUTTON = 0;
  public static final int BUTTON1 = 1;
  public static final int BUTTON2 = 2;
  public static final int BUTTON3 = 3;
  public MouseEvent(java.awt.Component source, int id, long when, int modifiers, int x, int y, int clickCount,
      boolean popupTrigger, int button) {
    this(source, id, when, modifiers, x, y, 0, 0, clickCount, popupTrigger, button);
    if (source != null && source.isShowing()) {
      java.awt.Point origin = source.getLocationOnScreen();
      this.xAbs = origin.x + x;
      this.yAbs = origin.y + y;
    }
  }
  public MouseEvent(java.awt.Component source, int id, long when, int modifiers, int x, int y, int clickCount,
      boolean popupTrigger) {
    this(source, id, when, modifiers, x, y, clickCount, popupTrigger, NOBUTTON);
  }
  public MouseEvent(java.awt.Component source, int id, long when, int modifiers, int x, int y, int xAbs, int yAbs,
      int clickCount, boolean popupTrigger, int button) {
    super(source, id, when, modifiers);
    this.x = x;
    this.y = y;
    this.xAbs = xAbs;
    this.yAbs = yAbs;
    this.clickCount = clickCount;
    this.popupTrigger = popupTrigger;
    if (button < NOBUTTON) {
      throw new IllegalArgumentException("Invalid button value :" + button);
    }
    if (button > BUTTON3 && java.awt.GraphicsEnvironment.isHeadless()) {
      throw new java.awt.HeadlessException();
    }
    this.button = button;
    if (getModifiers() != 0 && getModifiersEx() == 0) {
      convertMouseToExtended();
    } else if (getModifiers() == 0 && (getModifiersEx() != 0 || button != NOBUTTON) && button <= BUTTON3) {
      convertMouseToLegacy();
    }
  }
  private boolean isButtonTransition() {
    return id == MOUSE_PRESSED || id == MOUSE_RELEASED || id == MOUSE_CLICKED;
  }
  private void convertMouseToExtended() {
    convertToExtended();
    if ((modifiers & BUTTON2_MASK) != 0) {
      modifiers |= BUTTON2_DOWN_MASK;
    }
    if ((modifiers & BUTTON3_MASK) != 0) {
      modifiers |= BUTTON3_DOWN_MASK;
    }
    if (isButtonTransition()) {
      if ((modifiers & BUTTON1_MASK) != 0) {
        button = BUTTON1;
        modifiers &= ~(BUTTON2_MASK | BUTTON3_MASK);
        if (id != MOUSE_PRESSED) {
          modifiers &= ~BUTTON1_DOWN_MASK;
        }
      } else if ((modifiers & BUTTON2_MASK) != 0) {
        button = BUTTON2;
        modifiers &= ~(BUTTON1_MASK | BUTTON3_MASK);
        if (id != MOUSE_PRESSED) {
          modifiers &= ~BUTTON2_DOWN_MASK;
        }
      } else if ((modifiers & BUTTON3_MASK) != 0) {
        button = BUTTON3;
        modifiers &= ~(BUTTON1_MASK | BUTTON2_MASK);
        if (id != MOUSE_PRESSED) {
          modifiers &= ~BUTTON3_DOWN_MASK;
        }
      }
    }
  }
  private void convertMouseToLegacy() {
    convertToLegacy(false);
    if (isButtonTransition()) {
      switch (button) {
        case BUTTON1:
          modifiers |= BUTTON1_MASK;
          break;
        case BUTTON2:
          modifiers |= BUTTON2_MASK;
          break;
        case BUTTON3:
          modifiers |= BUTTON3_MASK;
          break;
        default:
          break;
      }
    } else {
      if ((modifiers & BUTTON1_DOWN_MASK) != 0) {
        modifiers |= BUTTON1_MASK;
      }
      if ((modifiers & BUTTON2_DOWN_MASK) != 0) {
        modifiers |= BUTTON2_MASK;
      }
      if ((modifiers & BUTTON3_DOWN_MASK) != 0) {
        modifiers |= BUTTON3_MASK;
      }
    }
  }
  public java.lang.String paramString() {
    StringBuilder str = new StringBuilder(80);
    switch (id) {
      case MOUSE_PRESSED:
        str.append("MOUSE_PRESSED");
        break;
      case MOUSE_RELEASED:
        str.append("MOUSE_RELEASED");
        break;
      case MOUSE_CLICKED:
        str.append("MOUSE_CLICKED");
        break;
      case MOUSE_ENTERED:
        str.append("MOUSE_ENTERED");
        break;
      case MOUSE_EXITED:
        str.append("MOUSE_EXITED");
        break;
      case MOUSE_MOVED:
        str.append("MOUSE_MOVED");
        break;
      case MOUSE_DRAGGED:
        str.append("MOUSE_DRAGGED");
        break;
      case MOUSE_WHEEL:
        str.append("MOUSE_WHEEL");
        break;
      default:
        str.append("unknown type");
    }
    str.append(",(").append(x).append(',').append(y).append(')');
    str.append(",absolute(").append(xAbs).append(',').append(yAbs).append(')');
    if (id != MOUSE_DRAGGED && id != MOUSE_MOVED) {
      str.append(",button=").append(getButton());
    }
    if (getModifiers() != 0) {
      str.append(",modifiers=").append(getMouseModifiersText(modifiers));
    }
    if (getModifiersEx() != 0) {
      str.append(",extModifiers=").append(getModifiersExText(modifiers));
    }
    str.append(",clickCount=").append(clickCount);
    return str.toString();
  }
  public int getX() {
    return x;
  }
  public int getY() {
    return y;
  }
  public java.awt.Point getLocationOnScreen() {
    return new Point(xAbs, yAbs);
  }
  public int getXOnScreen() {
    return xAbs;
  }
  public int getYOnScreen() {
    return yAbs;
  }
  public int getClickCount() {
    return clickCount;
  }
  public boolean isPopupTrigger() {
    return popupTrigger;
  }
  public int getButton() {
    return button;
  }
  public static java.lang.String getMouseModifiersText(int modifiers) {
    StringBuilder buf = new StringBuilder();
    if ((modifiers & ALT_MASK) != 0) {
      buf.append("Alt+");
    }
    if ((modifiers & META_MASK) != 0) {
      buf.append("Meta+");
    }
    if ((modifiers & SHIFT_MASK) != 0) {
      buf.append("Shift+");
    }
    if ((modifiers & CTRL_MASK) != 0) {
      buf.append("Ctrl+");
    }
    if ((modifiers & ALT_GRAPH_MASK) != 0) {
      buf.append("Alt Graph+");
    }
    boolean b1 = (modifiers & BUTTON1_MASK) != 0;
    boolean b2 = (modifiers & BUTTON2_MASK) != 0;
    boolean b3 = (modifiers & BUTTON3_MASK) != 0;
    if (b1) {
      buf.append("Button1+");
    }
    if (b2) {
      buf.append("Button2+");
    }
    if (b3) {
      buf.append("Button3+");
    }
    if (!b1 && (modifiers & BUTTON1_DOWN_MASK) != 0) {
      buf.append("Button1+");
    }
    if (!b2 && (modifiers & BUTTON2_DOWN_MASK) != 0) {
      buf.append("Button2+");
    }
    if (!b3 && (modifiers & BUTTON3_DOWN_MASK) != 0) {
      buf.append("Button3+");
    }
    if (buf.length() > 0) {
      buf.setLength(buf.length() - 1);
    }
    return buf.toString();
  }
  public java.awt.Point getPoint() {
    return new Point(x, y);
  }
  public void translatePoint(int p0, int p1) {
    x += p0;
    y += p1;
  }
}
