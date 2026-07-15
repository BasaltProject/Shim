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

public class ItemEvent extends java.awt.AWTEvent {
  Object item;
  int stateChange;
  public static final int ITEM_FIRST = 701;
  public static final int ITEM_LAST = 701;
  public static final int ITEM_STATE_CHANGED = 701;
  public static final int SELECTED = 1;
  public static final int DESELECTED = 2;
  public ItemEvent(java.awt.ItemSelectable p0, int p1, java.lang.Object p2, int p3) {
    super(p0, p1);
    this.item = p2;
    this.stateChange = p3;
  }
  public java.lang.String paramString() {
    String typeStr;
    switch (id) {
    case ITEM_STATE_CHANGED:
      typeStr = "ITEM_STATE_CHANGED";
      break;
    default:
      typeStr = "unknown type";
    }
    String stateStr;
    switch (stateChange) {
    case SELECTED:
      stateStr = "SELECTED";
      break;
    case DESELECTED:
      stateStr = "DESELECTED";
      break;
    default:
      stateStr = "unknown type";
    }
    return typeStr + ",item=" + item + ",stateChange=" + stateStr;
  }
  public java.lang.Object getItem() {
    return item;
  }
  public int getStateChange() {
    return stateChange;
  }
  public java.awt.ItemSelectable getItemSelectable() {
    return (ItemSelectable) source;
  }
}
