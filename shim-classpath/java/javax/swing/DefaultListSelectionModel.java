/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.io.Serializable;
import java.util.BitSet;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DefaultListSelectionModel implements ListSelectionModel, Cloneable, Serializable {
  private static final int kNone = Integer.MAX_VALUE;
  private static final int kNever = -1;

  private int selectionMode = MULTIPLE_INTERVAL_SELECTION;
  private int minIndex = kNone;
  private int maxIndex = kNever;
  private int anchorIndex = -1;
  private int leadIndex = -1;
  private int dirtyLo = kNone;
  private int dirtyHi = kNever;
  private boolean adjusting = false;
  private int pendingLo = kNone;
  private int pendingHi = kNever;
  private BitSet bits = new BitSet(32);

  protected EventListenerList listenerList = new EventListenerList();
  protected boolean leadAnchorNotificationEnabled = true;

  public DefaultListSelectionModel() { }

  public int getMinSelectionIndex() { return isSelectionEmpty() ? -1 : minIndex; }

  public int getMaxSelectionIndex() { return maxIndex; }

  public boolean getValueIsAdjusting() { return adjusting; }

  public int getSelectionMode() { return selectionMode; }

  public void setSelectionMode(int selectionMode) {
    switch (selectionMode) {
      case SINGLE_SELECTION:
      case SINGLE_INTERVAL_SELECTION:
      case MULTIPLE_INTERVAL_SELECTION:
        this.selectionMode = selectionMode;
        break;
      default:
        throw new IllegalArgumentException("invalid selectionMode");
    }
    int min = getMinSelectionIndex();
    if (min < 0) {
      return;
    }
    if (selectionMode == SINGLE_SELECTION) {
      if (getMaxSelectionIndex() != min) {
        setSelectionInterval(min, min);
      }
    } else if (selectionMode == SINGLE_INTERVAL_SELECTION) {
      int end = min;
      while (isSelectedIndex(end + 1)) {
        end++;
      }
      if (end != getMaxSelectionIndex()) {
        setSelectionInterval(min, end);
      }
    }
  }

  public boolean isSelectedIndex(int index) {
    return !((index < minIndex) || (index > maxIndex) || (!bits.get(index)));
  }

  public boolean isSelectionEmpty() { return minIndex > maxIndex; }

  public void addListSelectionListener(ListSelectionListener l) { listenerList.add(ListSelectionListener.class, l); }

  public void removeListSelectionListener(ListSelectionListener l) {
    listenerList.remove(ListSelectionListener.class, l);
  }

  public ListSelectionListener[] getListSelectionListeners() {
    return listenerList.getListeners(ListSelectionListener.class);
  }

  protected void fireValueChanged(boolean isAdjusting) {
    if (pendingHi == kNever) {
      return;
    }
    int oldFirst = pendingLo;
    int oldLast = pendingHi;
    pendingLo = kNone;
    pendingHi = kNever;
    fireValueChanged(oldFirst, oldLast, isAdjusting);
  }

  protected void fireValueChanged(int firstIndex, int lastIndex) {
    fireValueChanged(firstIndex, lastIndex, getValueIsAdjusting());
  }

  protected void fireValueChanged(int firstIndex, int lastIndex, boolean isAdjusting) {
    Object[] listeners = listenerList.getListenerList();
    ListSelectionEvent e = null;
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ListSelectionListener.class) {
        if (e == null) {
          e = new ListSelectionEvent(this, firstIndex, lastIndex, isAdjusting);
        }
        ((ListSelectionListener) listeners[i + 1]).valueChanged(e);
      }
    }
  }

  private void fireValueChanged() {
    if (dirtyHi == kNever) {
      return;
    }
    if (getValueIsAdjusting()) {
      pendingLo = Math.min(pendingLo, dirtyLo);
      pendingHi = Math.max(pendingHi, dirtyHi);
    }
    int oldFirst = dirtyLo;
    int oldLast = dirtyHi;
    dirtyLo = kNone;
    dirtyHi = kNever;
    fireValueChanged(oldFirst, oldLast);
  }

  public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
    return listenerList.getListeners(listenerType);
  }

  private void noteDirty(int r) {
    if (r == -1) {
      return;
    }
    dirtyLo = Math.min(r, dirtyLo);
    dirtyHi = Math.max(r, dirtyHi);
  }

  private void set(int r) {
    if (bits.get(r)) {
      return;
    }
    bits.set(r);
    noteDirty(r);
    minIndex = Math.min(minIndex, r);
    maxIndex = Math.max(maxIndex, r);
  }

  private void clear(int r) {
    if (!bits.get(r)) {
      return;
    }
    bits.clear(r);
    noteDirty(r);
    if (r == minIndex) {
      for (minIndex = minIndex + 1; minIndex <= maxIndex; minIndex++) {
        if (bits.get(minIndex)) {
          break;
        }
      }
    }
    if (r == maxIndex) {
      for (maxIndex = maxIndex - 1; minIndex <= maxIndex; maxIndex--) {
        if (bits.get(maxIndex)) {
          break;
        }
      }
    }
    if (isSelectionEmpty()) {
      minIndex = kNone;
      maxIndex = kNever;
    }
  }

  public void setLeadAnchorNotificationEnabled(boolean flag) { leadAnchorNotificationEnabled = flag; }

  public boolean isLeadAnchorNotificationEnabled() { return leadAnchorNotificationEnabled; }

  private void moveAnchorAndLead(int anchorIndex, int leadIndex) {
    if (leadAnchorNotificationEnabled) {
      if (this.anchorIndex != anchorIndex) {
        noteDirty(this.anchorIndex);
        noteDirty(anchorIndex);
      }
      if (this.leadIndex != leadIndex) {
        noteDirty(this.leadIndex);
        noteDirty(leadIndex);
      }
    }
    this.anchorIndex = anchorIndex;
    this.leadIndex = leadIndex;
  }

  private boolean contains(int a, int b, int i) { return (i >= a) && (i <= b); }

  private void applyChange(int clearMin, int clearMax, int setMin, int setMax, boolean clearFirst) {
    for (int i = Math.min(setMin, clearMin); i <= Math.max(setMax, clearMax); i++) {
      boolean shouldClear = contains(clearMin, clearMax, i);
      boolean shouldSet = contains(setMin, setMax, i);
      if (shouldSet && shouldClear) {
        if (clearFirst) {
          shouldClear = false;
        } else {
          shouldSet = false;
        }
      }
      if (shouldSet) {
        set(i);
      }
      if (shouldClear) {
        clear(i);
      }
    }
    fireValueChanged();
  }

  private void applyChange(int clearMin, int clearMax, int setMin, int setMax) {
    applyChange(clearMin, clearMax, setMin, setMax, true);
  }

  public void clearSelection() { removeInternal(minIndex, maxIndex, false); }

  public void setSelectionInterval(int index0, int index1) {
    if (index0 == -1 || index1 == -1) {
      return;
    }
    if (getSelectionMode() == SINGLE_SELECTION) {
      index0 = index1;
    }
    moveAnchorAndLead(index0, index1);
    int clearMin = minIndex;
    int clearMax = maxIndex;
    int setMin = Math.min(index0, index1);
    int setMax = Math.max(index0, index1);
    applyChange(clearMin, clearMax, setMin, setMax);
  }

  public void addSelectionInterval(int index0, int index1) {
    if (index0 == -1 || index1 == -1) {
      return;
    }
    if (getSelectionMode() == SINGLE_SELECTION) {
      setSelectionInterval(index0, index1);
      return;
    }
    moveAnchorAndLead(index0, index1);
    int clearMin = kNone;
    int clearMax = kNever;
    int setMin = Math.min(index0, index1);
    int setMax = Math.max(index0, index1);
    if (getSelectionMode() == SINGLE_INTERVAL_SELECTION && (setMax < minIndex - 1 || setMin > maxIndex + 1)) {
      setSelectionInterval(index0, index1);
      return;
    }
    applyChange(clearMin, clearMax, setMin, setMax);
  }

  public void removeSelectionInterval(int index0, int index1) { removeInternal(index0, index1, true); }

  private void removeInternal(int index0, int index1, boolean changeLeadAnchor) {
    if (index0 == -1 || index1 == -1) {
      return;
    }
    if (changeLeadAnchor) {
      moveAnchorAndLead(index0, index1);
    }
    int clearMin = Math.min(index0, index1);
    int clearMax = Math.max(index0, index1);
    int setMin = kNone;
    int setMax = kNever;
    if (getSelectionMode() != MULTIPLE_INTERVAL_SELECTION && clearMin > minIndex && clearMax < maxIndex) {
      clearMax = maxIndex;
    }
    applyChange(clearMin, clearMax, setMin, setMax);
  }

  private void assign(int index, boolean state) {
    if (state) {
      set(index);
    } else {
      clear(index);
    }
  }

  public void insertIndexInterval(int index, int length, boolean before) {
    int insFrom = (before) ? index : index + 1;
    int insTo = (insFrom + length) - 1;
    for (int i = maxIndex; i >= insFrom; i--) {
      assign(i + length, bits.get(i));
    }
    boolean setInsertedValues = ((getSelectionMode() == SINGLE_SELECTION) ? false : bits.get(index));
    for (int i = insFrom; i <= insTo; i++) {
      assign(i, setInsertedValues);
    }
    int leadIndex = this.leadIndex;
    if (leadIndex > index || (before && leadIndex == index)) {
      leadIndex = this.leadIndex + length;
    }
    int anchorIndex = this.anchorIndex;
    if (anchorIndex > index || (before && anchorIndex == index)) {
      anchorIndex = this.anchorIndex + length;
    }
    if (leadIndex != this.leadIndex || anchorIndex != this.anchorIndex) {
      moveAnchorAndLead(anchorIndex, leadIndex);
    }
    fireValueChanged();
  }

  public void removeIndexInterval(int index0, int index1) {
    int cutFrom = Math.min(index0, index1);
    int cutTo = Math.max(index0, index1);
    int gap = (cutTo - cutFrom) + 1;
    for (int i = cutFrom; i <= maxIndex; i++) {
      assign(i, bits.get(i + gap));
    }
    int leadIndex = this.leadIndex;
    if (leadIndex == 0 && cutFrom == 0) {
      // keep
    } else if (leadIndex > cutTo) {
      leadIndex = this.leadIndex - gap;
    } else if (leadIndex >= cutFrom) {
      leadIndex = cutFrom - 1;
    }
    int anchorIndex = this.anchorIndex;
    if (anchorIndex == 0 && cutFrom == 0) {
      // keep
    } else if (anchorIndex > cutTo) {
      anchorIndex = this.anchorIndex - gap;
    } else if (anchorIndex >= cutFrom) {
      anchorIndex = cutFrom - 1;
    }
    if (leadIndex != this.leadIndex || anchorIndex != this.anchorIndex) {
      moveAnchorAndLead(anchorIndex, leadIndex);
    }
    fireValueChanged();
  }

  public void setValueIsAdjusting(boolean isAdjusting) {
    if (isAdjusting != this.adjusting) {
      this.adjusting = isAdjusting;
      this.fireValueChanged(isAdjusting);
    }
  }

  public String toString() {
    String s = ((getValueIsAdjusting()) ? "~" : "=") + bits.toString();
    return getClass().getName() + " " + Integer.toString(hashCode()) + " " + s;
  }

  public Object clone() throws CloneNotSupportedException {
    DefaultListSelectionModel clone = (DefaultListSelectionModel) super.clone();
    clone.bits = (BitSet) bits.clone();
    clone.listenerList = new EventListenerList();
    return clone;
  }

  public int getAnchorSelectionIndex() { return anchorIndex; }

  public int getLeadSelectionIndex() { return leadIndex; }

  public void setAnchorSelectionIndex(int anchorIndex) {
    moveAnchorAndLead(anchorIndex, this.leadIndex);
    fireValueChanged();
  }

  public void moveLeadSelectionIndex(int leadIndex) {
    if (leadIndex == -1) {
      if (this.anchorIndex != -1) {
        return;
      }
    }
    moveAnchorAndLead(this.anchorIndex, leadIndex);
    fireValueChanged();
  }

  public void setLeadSelectionIndex(int leadIndex) {
    int anchorIndex = this.anchorIndex;
    if (leadIndex == -1) {
      if (anchorIndex == -1) {
        moveAnchorAndLead(anchorIndex, leadIndex);
        fireValueChanged();
      }
      return;
    } else if (anchorIndex == -1) {
      return;
    }
    if (this.leadIndex == -1) {
      this.leadIndex = leadIndex;
    }
    boolean shouldSelect = bits.get(this.anchorIndex);
    if (getSelectionMode() == SINGLE_SELECTION) {
      anchorIndex = leadIndex;
      shouldSelect = true;
    }
    int oldMin = Math.min(this.anchorIndex, this.leadIndex);
    int oldMax = Math.max(this.anchorIndex, this.leadIndex);
    int newMin = Math.min(anchorIndex, leadIndex);
    int newMax = Math.max(anchorIndex, leadIndex);
    moveAnchorAndLead(anchorIndex, leadIndex);
    if (shouldSelect) {
      applyChange(oldMin, oldMax, newMin, newMax);
    } else {
      applyChange(newMin, newMax, oldMin, oldMax, false);
    }
  }
}
