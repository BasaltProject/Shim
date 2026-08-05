/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

public interface ListCellRenderer<E> {
  java.awt.Component getListCellRendererComponent(javax.swing.JList<? extends E> p0, E p1, int p2, boolean p3, boolean p4);
}
