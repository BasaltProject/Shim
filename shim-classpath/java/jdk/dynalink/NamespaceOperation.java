/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

import java.util.Arrays;
import java.util.Objects;

public final class NamespaceOperation implements Operation {
  private final Operation baseOperation;
  private final Namespace[] namespaces;

  public NamespaceOperation(Operation baseOperation, Namespace... namespaces) {
    this.baseOperation = Objects.requireNonNull(baseOperation, "baseOperation is null");
    if (baseOperation instanceof NamespaceOperation) {
      throw new IllegalArgumentException("baseOperation is a NamespaceOperation");
    }
    this.namespaces = Objects.requireNonNull(namespaces, "namespaces is null").clone();
    if (namespaces.length == 0) {
      throw new IllegalArgumentException("Must specify at least one namespace");
    }
    for (int i = 0; i < this.namespaces.length; ++i) {
      Objects.requireNonNull(this.namespaces[i], "operations[" + i + "] is null");
    }
  }

  public Operation getBaseOperation() {
    return baseOperation;
  }

  public Namespace[] getNamespaces() {
    return namespaces.clone();
  }

  public int getNamespaceCount() {
    return namespaces.length;
  }

  public Namespace getNamespace(int i) {
    try {
      return namespaces[i];
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new IndexOutOfBoundsException(Integer.toString(i));
    }
  }

  public boolean contains(Namespace namespace) {
    for (Namespace component : namespaces) {
      if (component.equals(namespace)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NamespaceOperation) {
      final NamespaceOperation other = (NamespaceOperation) obj;
      return baseOperation.equals(other.baseOperation) && Arrays.equals(namespaces, other.namespaces);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return baseOperation.hashCode() + 31 * Arrays.hashCode(namespaces);
  }

  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder();
    b.append(baseOperation).append(':');
    b.append(namespaces[0]);
    for (int i = 1; i < namespaces.length; ++i) {
      b.append('|').append(namespaces[i]);
    }
    return b.toString();
  }

  public static Operation getBaseOperation(Operation op) {
    return op instanceof NamespaceOperation ? ((NamespaceOperation) op).getBaseOperation() : op;
  }

  public static Namespace[] getNamespaces(Operation op) {
    return op instanceof NamespaceOperation ? ((NamespaceOperation) op).getNamespaces() : new Namespace[0];
  }

  public static boolean contains(Operation op, Operation baseOperation, Namespace namespace) {
    return op instanceof NamespaceOperation && ((NamespaceOperation) op).baseOperation.equals(baseOperation)
        && ((NamespaceOperation) op).contains(namespace);
  }
}
