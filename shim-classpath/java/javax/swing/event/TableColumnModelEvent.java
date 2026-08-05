/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

import java.util.EventObject;
import javax.swing.table.TableColumnModel;

public class TableColumnModelEvent extends EventObject {
  protected int fromIndex;
  protected int toIndex;

  public TableColumnModelEvent(TableColumnModel source, int from, int to) {
    super(source);
    fromIndex = from;
    toIndex = to;
  }

  public int getFromIndex() { return fromIndex; }

  public int getToIndex() { return toIndex; }
}
