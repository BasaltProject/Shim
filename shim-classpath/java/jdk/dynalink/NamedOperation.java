/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.dynalink;

import java.util.Objects;

public final class NamedOperation implements Operation {
  private final Operation baseOperation;
  private final Object name;

  public NamedOperation(Operation baseOperation, Object name) {
    this.baseOperation = Objects.requireNonNull(baseOperation, "baseOperation is null");
    this.name = Objects.requireNonNull(name, "name is null");
  }

  public Operation getBaseOperation() {
    return baseOperation;
  }

  public Object getName() {
    return name;
  }

  public final NamedOperation changeName(String newName) {
    if (newName.equals(name)) {
      return this;
    }
    return new NamedOperation(baseOperation, newName);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NamedOperation) {
      final NamedOperation other = (NamedOperation) obj;
      return baseOperation.equals(other.baseOperation) && name.equals(other.name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return baseOperation.hashCode() + 31 * name.hashCode();
  }

  @Override
  public String toString() {
    return baseOperation.toString() + ":" + name.toString();
  }

  public static Operation getBaseOperation(Operation op) {
    return op instanceof NamedOperation ? ((NamedOperation) op).baseOperation : op;
  }

  public static Object getName(Operation op) {
    return op instanceof NamedOperation ? ((NamedOperation) op).name : null;
  }
}
