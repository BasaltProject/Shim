/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class StringValueExp implements ValueExp {
  private static final long serialVersionUID = -3256390509806284044L;

  private String val;

  public StringValueExp() {
  }

  public StringValueExp(String val) {
    this.val = val;
  }

  public String getValue() {
    return val;
  }

  @Override
  public String toString() {
    return "'" + val.replace("'", "''") + "'";
  }

  @Override
  @Deprecated
  public void setMBeanServer(MBeanServer s) {
  }

  @Override
  public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    return this;
  }
}
