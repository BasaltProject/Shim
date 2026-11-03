/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.event;

public interface ObjectChangeListener extends javax.naming.event.NamingListener {
  void objectChanged(javax.naming.event.NamingEvent namingEvent);
}
