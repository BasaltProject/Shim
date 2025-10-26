/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class NotQueryExp extends QueryEval implements QueryExp {
  private static final long serialVersionUID = 5269643775896723397L;

  private QueryExp exp;

  NotQueryExp(QueryExp q) {
    exp = q;
  }

  @Override
  public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    return !exp.apply(name);
  }

  @Override
  public String toString() {
    return "not (" + exp + ")";
  }
}
