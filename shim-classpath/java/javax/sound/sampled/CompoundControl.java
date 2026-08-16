/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

public abstract class CompoundControl extends Control {
  private Control[] controls;

  protected CompoundControl(Type type, Control[] memberControls) {
    super(type);
    this.controls = memberControls;
  }

  public Control[] getMemberControls() {
    Control[] localArray = new Control[controls.length];
    for (int i = 0; i < controls.length; i++) {
      localArray[i] = controls[i];
    }
    return localArray;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < controls.length; i++) {
      if (i != 0) {
        buf.append(", ");
        if ((i + 1) == controls.length) {
          buf.append("and ");
        }
      }
      buf.append(controls[i].getType());
    }
    return new String(getType() + " Control containing " + buf + " Controls.");
  }

  public static class Type extends Control.Type {
    protected Type(String name) { super(name); }
  }
}
