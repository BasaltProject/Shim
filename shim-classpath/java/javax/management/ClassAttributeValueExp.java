/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class ClassAttributeValueExp extends AttributeValueExp {
  private static final long serialVersionUID = -2212731951078526753L;

  private String attr;

  ClassAttributeValueExp() {
    super("Class");
    attr = "Class";
  }

  @Override
  public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    Object result = getAttribute(name);
    if (result instanceof String) {
      return new StringValueExp((String) result);
    }
    throw new BadAttributeValueExpException(result);
  }

  @Override
  protected Object getAttribute(ObjectName name) {
    try {
      MBeanServer server = QueryEval.getMBeanServer();
      return server.getObjectInstance(name).getClassName();
    } catch (Exception re) {
      return null;
    }
  }

  @Override
  public String toString() {
    return attr;
  }
}
