/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class OrQueryExp extends QueryEval implements QueryExp {
  private static final long serialVersionUID = 2962973084421321234L;

  private QueryExp exp1;
  private QueryExp exp2;

  OrQueryExp(QueryExp q1, QueryExp q2) {
    exp1 = q1;
    exp2 = q2;
  }

  @Override
  public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    return exp1.apply(name) || exp2.apply(name);
  }

  @Override
  public String toString() {
    return "(" + exp1 + ") or (" + exp2 + ")";
  }
}
