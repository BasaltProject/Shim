/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package java.awt.dnd;

import java.awt.event.InputEvent;

public class DragSourceDragEvent extends DragSourceEvent {
  private static final long serialVersionUID = 481346297933902471L;
  private static final int kJdk1Modifiers = InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK;
  private static final int kJdk14Modifiers = InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK
      | InputEvent.ALT_DOWN_MASK;
  private final int targetActions;
  private final int dropAction;
  private final int gestureModifiers;

  public DragSourceDragEvent(DragSourceContext dsc, int dropAction, int action, int modifiers) {
    super(dsc);
    targetActions = action;
    this.dropAction = dropAction;
    gestureModifiers = modifiers;
    check();
  }

  public DragSourceDragEvent(DragSourceContext dsc, int dropAction, int action, int modifiers, int x, int y) {
    super(dsc, x, y);
    targetActions = action;
    this.dropAction = dropAction;
    gestureModifiers = modifiers;
    check();
  }

  private void check() {
    if ((gestureModifiers & ~(kJdk1Modifiers | kJdk14Modifiers)) != 0) {
      throw new IllegalArgumentException("Invalid modifiers");
    }
  }

  public int getTargetActions() {
    return targetActions;
  }

  public int getGestureModifiers() {
    int modifiers = gestureModifiers;
    if ((modifiers & kJdk14Modifiers) != 0) {
      if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0) {
        modifiers |= InputEvent.BUTTON1_MASK;
      }
      if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0) {
        modifiers |= InputEvent.BUTTON2_MASK;
      }
      if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0) {
        modifiers |= InputEvent.BUTTON3_MASK;
      }
    }
    return modifiers & (InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK
        | kJdk1Modifiers);
  }

  public int getGestureModifiersEx() {
    int modifiers = gestureModifiers;
    if ((modifiers & kJdk1Modifiers) != 0) {
      if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
        modifiers |= InputEvent.SHIFT_DOWN_MASK;
      }
      if ((modifiers & InputEvent.ALT_MASK) != 0) {
        modifiers |= InputEvent.ALT_DOWN_MASK;
      }
      if ((modifiers & InputEvent.CTRL_MASK) != 0) {
        modifiers |= InputEvent.CTRL_DOWN_MASK;
      }
      if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
        modifiers |= InputEvent.BUTTON1_DOWN_MASK;
      }
      if ((modifiers & InputEvent.BUTTON2_MASK) != 0) {
        modifiers |= InputEvent.BUTTON2_DOWN_MASK;
      }
      if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
        modifiers |= InputEvent.BUTTON3_DOWN_MASK;
      }
    }
    return modifiers & (InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK
        | InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK);
  }

  public int getUserAction() {
    return dropAction;
  }

  public int getDropAction() {
    return dropAction & targetActions & getDragSourceContext().getSourceActions();
  }
}
