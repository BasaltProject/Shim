/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.event;

public interface TableColumnModelListener extends java.util.EventListener {
  void columnAdded(javax.swing.event.TableColumnModelEvent p0);
  void columnRemoved(javax.swing.event.TableColumnModelEvent p0);
  void columnMoved(javax.swing.event.TableColumnModelEvent p0);
  void columnMarginChanged(javax.swing.event.ChangeEvent p0);
  void columnSelectionChanged(javax.swing.event.ListSelectionEvent p0);
}
