/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

public interface Operation {
  default NamedOperation named(Object name) {
    return new NamedOperation(this, name);
  }

  default NamespaceOperation withNamespace(Namespace namespace) {
    return new NamespaceOperation(this, namespace);
  }

  default NamespaceOperation withNamespaces(Namespace... namespaces) {
    return new NamespaceOperation(this, namespaces);
  }
}
