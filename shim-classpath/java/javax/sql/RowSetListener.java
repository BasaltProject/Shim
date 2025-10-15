/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface RowSetListener extends java.util.EventListener {
  void rowSetChanged(RowSetEvent p0);

  void rowChanged(RowSetEvent p0);

  void cursorMoved(RowSetEvent p0);
}
