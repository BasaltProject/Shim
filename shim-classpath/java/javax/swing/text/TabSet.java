/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

import java.io.Serializable;

public class TabSet implements Serializable {
  private final TabStop[] tabs;
  private int hashCode = Integer.MAX_VALUE;

  public TabSet(TabStop[] tabs) {
    this.tabs = tabs == null ? new TabStop[0] : tabs.clone();
  }

  public int getTabCount() { return tabs.length; }

  public TabStop getTab(int index) {
    if (index < 0 || index >= tabs.length) {
      throw new IllegalArgumentException(index + " is outside the range of tabs");
    }
    return tabs[index];
  }

  public TabStop getTabAfter(float location) {
    int idx = getTabIndexAfter(location);
    return idx == -1 ? null : tabs[idx];
  }

  public int getTabIndex(TabStop tab) {
    for (int i = 0; i < tabs.length; i++) {
      if (tabs[i] == tab) {
        return i;
      }
    }
    return -1;
  }

  public int getTabIndexAfter(float location) {
    int lo = 0;
    int hi = tabs.length;
    while (lo < hi) {
      int mid = (lo + hi) / 2;
      if (location > tabs[mid].getPosition()) {
        lo = mid + 1;
      } else {
        hi = mid;
      }
    }
    if (lo < tabs.length && tabs[lo].getPosition() == location) {
      lo++;
    }
    return lo < tabs.length ? lo : -1;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TabSet) {
      return java.util.Arrays.equals(tabs, ((TabSet) o).tabs);
    }
    return false;
  }

  public int hashCode() {
    if (hashCode == Integer.MAX_VALUE) {
      hashCode = 0;
      for (TabStop t : tabs) {
        hashCode ^= t.hashCode();
      }
      if (hashCode == Integer.MAX_VALUE) {
        hashCode -= 1;
      }
    }
    return hashCode;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("[ ");
    for (int i = 0; i < tabs.length; i++) {
      sb.append(i == 0 ? "" : " - ").append(tabs[i]);
    }
    return sb.append(" ]").toString();
  }
}
