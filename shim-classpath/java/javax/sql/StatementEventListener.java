/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface StatementEventListener extends java.util.EventListener {
  void statementClosed(StatementEvent p0);

  void statementErrorOccurred(StatementEvent p0);
}
