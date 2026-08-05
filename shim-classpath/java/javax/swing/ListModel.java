/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface ListModel<E> {
  int getSize();
  void addListDataListener(javax.swing.event.ListDataListener p0);
  void removeListDataListener(javax.swing.event.ListDataListener p0);
  E getElementAt(int p0);
}
