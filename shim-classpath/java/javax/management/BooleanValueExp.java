/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class BooleanValueExp extends QueryEval implements ValueExp {
  private static final long serialVersionUID = 7754922052666594581L;

  private boolean val = false;

  BooleanValueExp(boolean val) {
    this.val = val;
  }

  BooleanValueExp(Boolean val) {
    this.val = val.booleanValue();
  }

  boolean getValue() {
    return val;
  }

  @Override
  public String toString() {
    return String.valueOf(val);
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
