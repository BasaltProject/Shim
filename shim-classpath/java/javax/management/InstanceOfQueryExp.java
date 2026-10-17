/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class InstanceOfQueryExp extends QueryEval implements QueryExp {
  private static final long serialVersionUID = -1081892073854801359L;

  private StringValueExp classNameValue;

  InstanceOfQueryExp(StringValueExp classNameValue) {
    if (classNameValue == null) {
      throw new IllegalArgumentException("Null class name.");
    }
    this.classNameValue = classNameValue;
  }

  @Override
  public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    StringValueExp val;
    try {
      val = (StringValueExp) classNameValue.apply(name);
    } catch (ClassCastException x) {
      BadStringOperationException y = new BadStringOperationException(x.toString());
      y.initCause(x);
      throw y;
    }
    try {
      return QueryEval.getMBeanServer().isInstanceOf(name, val.getValue());
    } catch (InstanceNotFoundException infe) {
      return false;
    }
  }

  @Override
  public String toString() {
    return "InstanceOf " + classNameValue.toString();
  }
}
