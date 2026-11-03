/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class KeyboardFocusManager implements KeyEventDispatcher, KeyEventPostProcessor {
  public static final int FORWARD_TRAVERSAL_KEYS = 0;
  public static final int BACKWARD_TRAVERSAL_KEYS = 1;
  public static final int UP_CYCLE_TRAVERSAL_KEYS = 2;
  public static final int DOWN_CYCLE_TRAVERSAL_KEYS = 3;

  static final int TRAVERSAL_KEY_LENGTH = DOWN_CYCLE_TRAVERSAL_KEYS + 1;

  private static KeyboardFocusManager currentManager;

  private Component focusOwner;
  private Component permanentFocusOwner;
  private Window focusedWindow;
  private Window activeWindow;
  private FocusTraversalPolicy defaultPolicy = new DefaultFocusTraversalPolicy();
  @SuppressWarnings("unchecked")
  private final Set<AWTKeyStroke>[] defaultFocusTraversalKeys = new Set[TRAVERSAL_KEY_LENGTH];
  private Container currentFocusCycleRoot;
  private VetoableChangeSupport vetoableSupport;
  private PropertyChangeSupport changeSupport;
  private LinkedList<KeyEventDispatcher> keyEventDispatchers;
  private LinkedList<KeyEventPostProcessor> keyEventPostProcessors;

  static final String[] kKeyNames = {
      "forwardDefaultFocusTraversalKeys", "backwardDefaultFocusTraversalKeys", "upCycleDefaultFocusTraversalKeys",
      "downCycleDefaultFocusTraversalKeys"};

  public static KeyboardFocusManager getCurrentKeyboardFocusManager() {
    synchronized (KeyboardFocusManager.class) {
      if (currentManager == null) {
        currentManager = new DefaultKeyboardFocusManager();
      }
      return currentManager;
    }
  }

  public static void setCurrentKeyboardFocusManager(KeyboardFocusManager newManager) throws SecurityException {
    synchronized (KeyboardFocusManager.class) {
      currentManager = newManager == null ? new DefaultKeyboardFocusManager() : newManager;
    }
  }

  public KeyboardFocusManager() {
    defaultFocusTraversalKeys[FORWARD_TRAVERSAL_KEYS] = setOf(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0),
        AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    defaultFocusTraversalKeys[BACKWARD_TRAVERSAL_KEYS] = setOf(
        AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, java.awt.event.InputEvent.SHIFT_DOWN_MASK),
        AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
            java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
    defaultFocusTraversalKeys[UP_CYCLE_TRAVERSAL_KEYS] = Collections.emptySet();
    defaultFocusTraversalKeys[DOWN_CYCLE_TRAVERSAL_KEYS] = Collections.emptySet();
  }

  private static Set<AWTKeyStroke> setOf(AWTKeyStroke... ks) {
    Set<AWTKeyStroke> s = new HashSet<AWTKeyStroke>();
    Collections.addAll(s, ks);
    return Collections.unmodifiableSet(s);
  }

  public Component getFocusOwner() { return focusOwner; }

  protected Component getGlobalFocusOwner() { return focusOwner; }

  protected void setGlobalFocusOwner(Component focusOwner) {
    Component oldFocusOwner = this.focusOwner;
    if (focusOwner == null || focusOwner.isFocusable()) {
      try {
        fireVetoableChange("focusOwner", oldFocusOwner, focusOwner);
      } catch (PropertyVetoException e) {
        return;
      }
      this.focusOwner = focusOwner;
      firePropertyChange("focusOwner", oldFocusOwner, focusOwner);
    }
  }

  public void clearFocusOwner() { clearGlobalFocusOwner(); }

  public void clearGlobalFocusOwner() {
    Component old = focusOwner;
    focusOwner = null;
    if (old != null) {
      firePropertyChange("focusOwner", old, null);
    }
  }

  public Component getPermanentFocusOwner() { return permanentFocusOwner; }

  protected Component getGlobalPermanentFocusOwner() { return permanentFocusOwner; }

  protected void setGlobalPermanentFocusOwner(Component permanentFocusOwner) {
    Component oldPermanentFocusOwner = this.permanentFocusOwner;
    if (permanentFocusOwner == null || permanentFocusOwner.isFocusable()) {
      try {
        fireVetoableChange("permanentFocusOwner", oldPermanentFocusOwner, permanentFocusOwner);
      } catch (PropertyVetoException e) {
        return;
      }
      this.permanentFocusOwner = permanentFocusOwner;
      firePropertyChange("permanentFocusOwner", oldPermanentFocusOwner, permanentFocusOwner);
    }
  }

  public Window getFocusedWindow() { return focusedWindow; }

  protected Window getGlobalFocusedWindow() { return focusedWindow; }

  protected void setGlobalFocusedWindow(Window focusedWindow) {
    Window oldFocusedWindow = this.focusedWindow;
    if (focusedWindow == null || focusedWindow.isFocusableWindow()) {
      try {
        fireVetoableChange("focusedWindow", oldFocusedWindow, focusedWindow);
      } catch (PropertyVetoException e) {
        return;
      }
      this.focusedWindow = focusedWindow;
      firePropertyChange("focusedWindow", oldFocusedWindow, focusedWindow);
    }
  }

  public Window getActiveWindow() { return activeWindow; }

  protected Window getGlobalActiveWindow() { return activeWindow; }

  protected void setGlobalActiveWindow(Window activeWindow) {
    Window oldActiveWindow = this.activeWindow;
    try {
      fireVetoableChange("activeWindow", oldActiveWindow, activeWindow);
    } catch (PropertyVetoException e) {
      return;
    }
    this.activeWindow = activeWindow;
    firePropertyChange("activeWindow", oldActiveWindow, activeWindow);
  }

  public synchronized FocusTraversalPolicy getDefaultFocusTraversalPolicy() { return defaultPolicy; }

  public void setDefaultFocusTraversalPolicy(FocusTraversalPolicy defaultPolicy) {
    if (defaultPolicy == null) {
      throw new IllegalArgumentException("default focus traversal policy cannot be null");
    }
    FocusTraversalPolicy oldPolicy;
    synchronized (this) {
      oldPolicy = this.defaultPolicy;
      this.defaultPolicy = defaultPolicy;
    }
    firePropertyChange("defaultFocusTraversalPolicy", oldPolicy, defaultPolicy);
  }

  public void setDefaultFocusTraversalKeys(int id, Set<? extends AWTKeyStroke> keystrokes) {
    if (id < 0 || id >= TRAVERSAL_KEY_LENGTH) {
      throw new IllegalArgumentException("invalid focus traversal key identifier");
    }
    if (keystrokes == null) {
      throw new IllegalArgumentException("cannot set null Set of default focus traversal keys");
    }
    Set<AWTKeyStroke> oldKeys;
    synchronized (this) {
      for (AWTKeyStroke keystroke : keystrokes) {
        if (keystroke == null) {
          throw new IllegalArgumentException("cannot set null focus traversal key");
        }
        if (keystroke.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
          throw new IllegalArgumentException("focus traversal keys cannot map to KEY_TYPED events");
        }
        for (int i = 0; i < TRAVERSAL_KEY_LENGTH; i++) {
          if (i == id) {
            continue;
          }
          if (defaultFocusTraversalKeys[i].contains(keystroke)) {
            throw new IllegalArgumentException("focus traversal keys must be unique for a Component");
          }
        }
      }
      oldKeys = defaultFocusTraversalKeys[id];
      defaultFocusTraversalKeys[id] = Collections.unmodifiableSet(new HashSet<AWTKeyStroke>(keystrokes));
    }
    firePropertyChange(kKeyNames[id], oldKeys, keystrokes);
  }

  public Set<AWTKeyStroke> getDefaultFocusTraversalKeys(int id) {
    if (id < 0 || id >= TRAVERSAL_KEY_LENGTH) {
      throw new IllegalArgumentException("invalid focus traversal key identifier");
    }
    return defaultFocusTraversalKeys[id];
  }

  public Container getCurrentFocusCycleRoot() { return currentFocusCycleRoot; }

  protected Container getGlobalCurrentFocusCycleRoot() { return currentFocusCycleRoot; }

  public void setGlobalCurrentFocusCycleRoot(Container newFocusCycleRoot) {
    Container oldFocusCycleRoot = currentFocusCycleRoot;
    currentFocusCycleRoot = newFocusCycleRoot;
    firePropertyChange("currentFocusCycleRoot", oldFocusCycleRoot, newFocusCycleRoot);
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    if (listener != null) {
      synchronized (this) {
        if (changeSupport == null) {
          changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
      }
    }
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    if (listener != null) {
      synchronized (this) {
        if (changeSupport != null) {
          changeSupport.removePropertyChangeListener(listener);
        }
      }
    }
  }

  public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
    if (changeSupport == null) {
      changeSupport = new PropertyChangeSupport(this);
    }
    return changeSupport.getPropertyChangeListeners();
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    if (listener != null) {
      synchronized (this) {
        if (changeSupport == null) {
          changeSupport = new PropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(propertyName, listener);
      }
    }
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    if (listener != null) {
      synchronized (this) {
        if (changeSupport != null) {
          changeSupport.removePropertyChangeListener(propertyName, listener);
        }
      }
    }
  }

  public synchronized PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
    if (changeSupport == null) {
      changeSupport = new PropertyChangeSupport(this);
    }
    return changeSupport.getPropertyChangeListeners(propertyName);
  }

  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (oldValue == newValue) {
      return;
    }
    PropertyChangeSupport changeSupport = this.changeSupport;
    if (changeSupport != null) {
      changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  public void addVetoableChangeListener(VetoableChangeListener listener) {
    if (listener != null) {
      synchronized (this) {
        if (vetoableSupport == null) {
          vetoableSupport = new VetoableChangeSupport(this);
        }
        vetoableSupport.addVetoableChangeListener(listener);
      }
    }
  }

  public void removeVetoableChangeListener(VetoableChangeListener listener) {
    if (listener != null) {
      synchronized (this) {
        if (vetoableSupport != null) {
          vetoableSupport.removeVetoableChangeListener(listener);
        }
      }
    }
  }

  public synchronized VetoableChangeListener[] getVetoableChangeListeners() {
    if (vetoableSupport == null) {
      vetoableSupport = new VetoableChangeSupport(this);
    }
    return vetoableSupport.getVetoableChangeListeners();
  }

  public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
    if (listener != null) {
      synchronized (this) {
        if (vetoableSupport == null) {
          vetoableSupport = new VetoableChangeSupport(this);
        }
        vetoableSupport.addVetoableChangeListener(propertyName, listener);
      }
    }
  }

  public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
    if (listener != null) {
      synchronized (this) {
        if (vetoableSupport != null) {
          vetoableSupport.removeVetoableChangeListener(propertyName, listener);
        }
      }
    }
  }

  public synchronized VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
    if (vetoableSupport == null) {
      vetoableSupport = new VetoableChangeSupport(this);
    }
    return vetoableSupport.getVetoableChangeListeners(propertyName);
  }

  protected void fireVetoableChange(String propertyName, Object oldValue, Object newValue)
      throws PropertyVetoException {
    if (oldValue == newValue) {
      return;
    }
    VetoableChangeSupport vetoableSupport = this.vetoableSupport;
    if (vetoableSupport != null) {
      vetoableSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }
  }

  public void addKeyEventDispatcher(KeyEventDispatcher dispatcher) {
    if (dispatcher != null) {
      synchronized (this) {
        if (keyEventDispatchers == null) {
          keyEventDispatchers = new LinkedList<KeyEventDispatcher>();
        }
        keyEventDispatchers.add(dispatcher);
      }
    }
  }

  public void removeKeyEventDispatcher(KeyEventDispatcher dispatcher) {
    if (dispatcher != null) {
      synchronized (this) {
        if (keyEventDispatchers != null) {
          keyEventDispatchers.remove(dispatcher);
        }
      }
    }
  }

  protected synchronized List<KeyEventDispatcher> getKeyEventDispatchers() {
    return keyEventDispatchers != null ? new ArrayList<KeyEventDispatcher>(keyEventDispatchers) : null;
  }

  public void addKeyEventPostProcessor(KeyEventPostProcessor processor) {
    if (processor != null) {
      synchronized (this) {
        if (keyEventPostProcessors == null) {
          keyEventPostProcessors = new LinkedList<KeyEventPostProcessor>();
        }
        keyEventPostProcessors.add(processor);
      }
    }
  }

  public void removeKeyEventPostProcessor(KeyEventPostProcessor processor) {
    if (processor != null) {
      synchronized (this) {
        if (keyEventPostProcessors != null) {
          keyEventPostProcessors.remove(processor);
        }
      }
    }
  }

  protected List<KeyEventPostProcessor> getKeyEventPostProcessors() {
    return keyEventPostProcessors != null ? new ArrayList<KeyEventPostProcessor>(keyEventPostProcessors) : null;
  }

  public abstract boolean dispatchEvent(AWTEvent e);

  public final void redispatchEvent(Component target, AWTEvent e) {
    target.dispatchEvent(e);
  }

  public abstract boolean dispatchKeyEvent(KeyEvent e);

  public abstract boolean postProcessKeyEvent(KeyEvent e);

  public abstract void processKeyEvent(Component focusedComponent, KeyEvent e);

  protected abstract void enqueueKeyEvents(long after, Component untilFocused);

  protected abstract void dequeueKeyEvents(long after, Component untilFocused);

  protected abstract void discardKeyEvents(Component comp);

  public abstract void focusNextComponent(Component aComponent);

  public abstract void focusPreviousComponent(Component aComponent);

  public abstract void upFocusCycle(Component aComponent);

  public abstract void downFocusCycle(Container aContainer);

  public final void focusNextComponent() {
    Component focusOwner = getFocusOwner();
    if (focusOwner != null) {
      focusNextComponent(focusOwner);
    }
  }

  public final void focusPreviousComponent() {
    Component focusOwner = getFocusOwner();
    if (focusOwner != null) {
      focusPreviousComponent(focusOwner);
    }
  }

  public final void upFocusCycle() {
    Component focusOwner = getFocusOwner();
    if (focusOwner != null) {
      upFocusCycle(focusOwner);
    }
  }

  public final void downFocusCycle() {
    Component focusOwner = getFocusOwner();
    if (focusOwner instanceof Container) {
      downFocusCycle((Container) focusOwner);
    }
  }
}
