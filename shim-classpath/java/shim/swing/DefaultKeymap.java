/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.swing;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

public final class DefaultKeymap implements Keymap {
  private final String name;
  private Keymap parent;
  private Action defaultAction;
  private final Map<KeyStroke, Action> bindings = new LinkedHashMap<>();

  public DefaultKeymap(String name, Keymap parent) {
    this.name = name;
    this.parent = parent;
  }

  public Action getDefaultAction() {
    if (defaultAction != null) {
      return defaultAction;
    }
    return parent != null ? parent.getDefaultAction() : null;
  }

  public void setDefaultAction(Action a) { defaultAction = a; }

  public String getName() { return name; }

  public Action getAction(KeyStroke key) {
    Action a = bindings.get(key);
    if (a == null && parent != null) {
      a = parent.getAction(key);
    }
    return a;
  }

  public KeyStroke[] getBoundKeyStrokes() { return bindings.keySet().toArray(new KeyStroke[0]); }

  public Action[] getBoundActions() { return bindings.values().toArray(new Action[0]); }

  public KeyStroke[] getKeyStrokesForAction(Action a) {
    java.util.List<KeyStroke> found = new java.util.ArrayList<>();
    for (Map.Entry<KeyStroke, Action> e : bindings.entrySet()) {
      if (e.getValue() == a) {
        found.add(e.getKey());
      }
    }
    if (parent != null) {
      KeyStroke[] pk = parent.getKeyStrokesForAction(a);
      if (pk != null) {
        for (KeyStroke k : pk) {
          if (!bindings.containsKey(k)) {
            found.add(k);
          }
        }
      }
    }
    return found.isEmpty() ? null : found.toArray(new KeyStroke[0]);
  }

  public boolean isLocallyDefined(KeyStroke key) { return bindings.containsKey(key); }

  public void addActionForKeyStroke(KeyStroke key, Action a) { bindings.put(key, a); }

  public void removeKeyStrokeBinding(KeyStroke key) { bindings.remove(key); }

  public void removeBindings() { bindings.clear(); }

  public Keymap getResolveParent() { return parent; }

  public void setResolveParent(Keymap parent) { this.parent = parent; }

  public String toString() { return "Keymap[" + name + "]" + bindings; }
}
