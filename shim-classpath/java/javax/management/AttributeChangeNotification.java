/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public class AttributeChangeNotification extends Notification {
  private static final long serialVersionUID = 535176054565814134L;

  public static final String ATTRIBUTE_CHANGE = "jmx.attribute.change";

  private String attributeName;
  private String attributeType;
  private Object oldValue;
  private Object newValue;

  public AttributeChangeNotification(Object source, long sequenceNumber, long timeStamp, String msg,
      String attributeName, String attributeType, Object oldValue, Object newValue) {
    super(ATTRIBUTE_CHANGE, source, sequenceNumber, timeStamp, msg);
    this.attributeName = attributeName;
    this.attributeType = attributeType;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public String getAttributeType() {
    return attributeType;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }
}
