/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class QualifiedAttributeValueExp extends AttributeValueExp {
  private static final long serialVersionUID = 8832517277410933254L;

  private String className;

  QualifiedAttributeValueExp(String className, String attr) {
    super(attr);
    this.className = className;
  }

  String getAttrClassName() {
    return className;
  }

  @Override
  public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    try {
      MBeanServer server = QueryEval.getMBeanServer();
      String v = server.getObjectInstance(name).getClassName();
      if (v.equals(className)) {
        return super.apply(name);
      }
      throw new InvalidApplicationException("Class name is " + v + " should be " + className);
    } catch (InstanceNotFoundException e) {
      throw new InvalidApplicationException("MBean " + name + " not found");
    }
  }

  @Override
  public String toString() {
    return className + "." + super.toString();
  }
}
