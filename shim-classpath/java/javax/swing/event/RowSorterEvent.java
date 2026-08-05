/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

import java.util.EventObject;
import javax.swing.RowSorter;

public class RowSorterEvent extends EventObject {
  private final Type type;
  private final int[] oldViewToModel;

  public enum Type {
    SORT_ORDER_CHANGED, SORTED
  }

  public RowSorterEvent(RowSorter source) { this(source, Type.SORT_ORDER_CHANGED, null); }

  public RowSorterEvent(RowSorter source, Type type, int[] previousRowIndexToModel) {
    super(source);
    if (type == null) {
      throw new IllegalArgumentException("type must be non-null");
    }
    this.type = type;
    this.oldViewToModel = previousRowIndexToModel;
  }

  public RowSorter getSource() { return (RowSorter) super.getSource(); }

  public Type getType() { return type; }

  public int convertPreviousRowIndexToModel(int index) {
    if (oldViewToModel != null && index >= 0 && index < oldViewToModel.length) {
      return oldViewToModel[index];
    }
    return -1;
  }

  public int getPreviousRowCount() { return oldViewToModel == null ? 0 : oldViewToModel.length; }
}
