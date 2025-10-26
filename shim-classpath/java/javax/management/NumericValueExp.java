/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class NumericValueExp extends QueryEval implements ValueExp {
  private static final long serialVersionUID = -4679739485102359104L;

  private Number val = 0.0;

  NumericValueExp() {
  }

  NumericValueExp(Number val) {
    this.val = val;
  }

  double doubleValue() {
    if (val instanceof Long || val instanceof Integer) {
      return (double) val.longValue();
    }
    return val.doubleValue();
  }

  long longValue() {
    if (val instanceof Long || val instanceof Integer) {
      return val.longValue();
    }
    return (long) val.doubleValue();
  }

  boolean isInteger() {
    return val instanceof Long || val instanceof Integer;
  }

  @Override
  public String toString() {
    if (val == null) {
      return "null";
    }
    if (val instanceof Long || val instanceof Integer) {
      return Long.toString(val.longValue());
    }
    double d = val.doubleValue();
    if (Double.isInfinite(d)) {
      return d > 0 ? "~+Inf~" : "~-Inf~";
    } else if (Double.isNaN(d)) {
      return "~NaN~";
    } else {
      return Double.toString(d);
    }
  }

  @Override
  public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    return this;
  }

  @Override
  @Deprecated
  public void setMBeanServer(MBeanServer s) {
    super.setMBeanServer(s);
  }
}
