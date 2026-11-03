/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.event;

public interface NamespaceChangeListener extends javax.naming.event.NamingListener {
  void objectAdded(javax.naming.event.NamingEvent namingEvent);
  void objectRemoved(javax.naming.event.NamingEvent namingEvent);
  void objectRenamed(javax.naming.event.NamingEvent namingEvent);
}
