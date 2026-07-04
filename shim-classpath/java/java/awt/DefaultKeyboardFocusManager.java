/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.event.KeyEvent;
import java.util.List;

public class DefaultKeyboardFocusManager extends KeyboardFocusManager {
  public DefaultKeyboardFocusManager() { }

  public boolean dispatchEvent(AWTEvent e) {
    if (e instanceof KeyEvent) {
      return dispatchKeyEvent((KeyEvent) e);
    }
    return false;
  }

  public boolean dispatchKeyEvent(KeyEvent e) {
    List<KeyEventDispatcher> dispatchers = getKeyEventDispatchers();
    if (dispatchers != null) {
      for (KeyEventDispatcher d : dispatchers) {
        if (d.dispatchKeyEvent(e)) {
          return true;
        }
      }
    }
    Component focusOwner = getFocusOwner();
    if (focusOwner != null && !e.isConsumed()) {
      redispatchEvent(focusOwner, e);
    }
    return postProcessKeyEvent(e);
  }

  public boolean postProcessKeyEvent(KeyEvent e) {
    List<KeyEventPostProcessor> processors = getKeyEventPostProcessors();
    if (processors != null) {
      for (KeyEventPostProcessor p : processors) {
        if (p.postProcessKeyEvent(e)) {
          return true;
        }
      }
    }
    return true;
  }

  public void processKeyEvent(Component focusedComponent, KeyEvent e) {
    if (focusedComponent != null && focusedComponent.getFocusTraversalKeysEnabled() && !e.isConsumed()) {
      AWTKeyStroke stroke = AWTKeyStroke.getAWTKeyStrokeForEvent(e);
      if (focusedComponent.getFocusTraversalKeys(FORWARD_TRAVERSAL_KEYS).contains(stroke)) {
        focusNextComponent(focusedComponent);
        e.consume();
      } else if (focusedComponent.getFocusTraversalKeys(BACKWARD_TRAVERSAL_KEYS).contains(stroke)) {
        focusPreviousComponent(focusedComponent);
        e.consume();
      }
    }
  }

  protected synchronized void enqueueKeyEvents(long after, Component untilFocused) { }

  protected synchronized void dequeueKeyEvents(long after, Component untilFocused) { }

  protected synchronized void discardKeyEvents(Component comp) { }

  public void focusPreviousComponent(Component aComponent) {
    if (aComponent != null) {
      aComponent.transferFocusBackward();
    }
  }

  public void focusNextComponent(Component aComponent) {
    if (aComponent != null) {
      aComponent.transferFocus();
    }
  }

  public void upFocusCycle(Component aComponent) {
    if (aComponent != null) {
      aComponent.transferFocusUpCycle();
    }
  }

  public void downFocusCycle(Container aContainer) {
    if (aContainer != null && aContainer.isFocusCycleRoot()) {
      aContainer.transferFocusDownCycle();
    }
  }
}
