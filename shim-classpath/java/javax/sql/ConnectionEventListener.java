/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql;

public interface ConnectionEventListener extends java.util.EventListener {
  void connectionClosed(ConnectionEvent p0);

  void connectionErrorOccurred(ConnectionEvent p0);
}
