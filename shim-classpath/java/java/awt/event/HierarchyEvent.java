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

public class HierarchyEvent extends java.awt.AWTEvent {
  Component changed;
  Container changedParent;
  long changeFlags;
  public static final int HIERARCHY_FIRST = 1400;
  public static final int HIERARCHY_CHANGED = 1400;
  public static final int ANCESTOR_MOVED = 1401;
  public static final int ANCESTOR_RESIZED = 1402;
  public static final int HIERARCHY_LAST = 1402;
  public static final int PARENT_CHANGED = 1;
  public static final int DISPLAYABILITY_CHANGED = 2;
  public static final int SHOWING_CHANGED = 4;
  public HierarchyEvent(java.awt.Component p0, int p1, java.awt.Component p2, java.awt.Container p3) {
    super(p0, p1);
    this.changed = p2;
    this.changedParent = p3;
  }
  public HierarchyEvent(java.awt.Component p0, int p1, java.awt.Component p2, java.awt.Container p3, long p4) {
    super(p0, p1);
    this.changed = p2;
    this.changedParent = p3;
    this.changeFlags = p4;
  }
  public java.lang.String paramString() {
    String typeStr;
    switch (id) {
      case ANCESTOR_MOVED:
        typeStr = "ANCESTOR_MOVED (" + changed + "," + changedParent + ")";
        break;
      case ANCESTOR_RESIZED:
        typeStr = "ANCESTOR_RESIZED (" + changed + "," + changedParent + ")";
        break;
      case HIERARCHY_CHANGED:
        StringBuilder b = new StringBuilder("HIERARCHY_CHANGED (");
        boolean first = true;
        if ((changeFlags & PARENT_CHANGED) != 0) {
          first = false;
          b.append("PARENT_CHANGED");
        }
        if ((changeFlags & DISPLAYABILITY_CHANGED) != 0) {
          if (!first) {
            b.append(',');
          }
          first = false;
          b.append("DISPLAYABILITY_CHANGED");
        }
        if ((changeFlags & SHOWING_CHANGED) != 0) {
          if (!first) {
            b.append(',');
          }
          b.append("SHOWING_CHANGED");
        }
        b.append(',').append(changed).append(',').append(changedParent).append(')');
        typeStr = b.toString();
        break;
      default:
        typeStr = "unknown type";
    }
    return typeStr;
  }
  public java.awt.Component getComponent() {
    return (source instanceof Component) ? (Component) source : null;
  }
  public java.awt.Component getChanged() {
    return changed;
  }
  public java.awt.Container getChangedParent() {
    return changedParent;
  }
  public long getChangeFlags() {
    return changeFlags;
  }
}
