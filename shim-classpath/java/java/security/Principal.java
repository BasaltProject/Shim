/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public interface Principal {
  default boolean implies(javax.security.auth.Subject subject) {
    return subject != null && subject.getPrincipals().contains(this);
  }

  String getName();

  boolean equals(Object another);

  String toString();

  int hashCode();
}
