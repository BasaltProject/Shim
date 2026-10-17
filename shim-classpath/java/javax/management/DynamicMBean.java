/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public interface DynamicMBean {
  Object getAttribute(String p0) throws AttributeNotFoundException, MBeanException, ReflectionException;

  void setAttribute(Attribute p0) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException,
      ReflectionException;

  AttributeList getAttributes(String[] p0);

  AttributeList setAttributes(AttributeList p0);

  Object invoke(String p0, Object[] p1, String[] p2) throws MBeanException, ReflectionException;

  MBeanInfo getMBeanInfo();
}
