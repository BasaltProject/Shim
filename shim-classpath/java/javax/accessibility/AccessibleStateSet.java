/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

import java.util.Vector;

public class AccessibleStateSet {
  protected Vector<AccessibleState> states = null;

  public AccessibleStateSet() { }

  public AccessibleStateSet(AccessibleState[] states) {
    if (states.length != 0) {
      this.states = new Vector<>(states.length);
      for (AccessibleState s : states) {
        if (!this.states.contains(s)) {
          this.states.addElement(s);
        }
      }
    }
  }

  public boolean add(AccessibleState state) {
    if (states == null) {
      states = new Vector<>();
    }
    if (!states.contains(state)) {
      states.addElement(state);
      return true;
    }
    return false;
  }

  public void addAll(AccessibleState[] states) {
    if (states.length != 0) {
      if (this.states == null) {
        this.states = new Vector<>(states.length);
      }
      for (AccessibleState s : states) {
        if (!this.states.contains(s)) {
          this.states.addElement(s);
        }
      }
    }
  }

  public boolean remove(AccessibleState state) { return states == null ? false : states.removeElement(state); }

  public void clear() {
    if (states != null) {
      states.removeAllElements();
    }
  }

  public boolean contains(AccessibleState state) { return states == null ? false : states.contains(state); }

  public AccessibleState[] toArray() {
    int n = states == null ? 0 : states.size();
    AccessibleState[] result = new AccessibleState[n];
    for (int i = 0; i < n; i++) {
      result[i] = states.elementAt(i);
    }
    return result;
  }

  public String toString() {
    String ret = null;
    if (states != null && states.size() > 0) {
      ret = states.elementAt(0).toDisplayString();
      for (int i = 1; i < states.size(); i++) {
        ret = ret + "," + states.elementAt(i).toDisplayString();
      }
    }
    return ret;
  }
}
