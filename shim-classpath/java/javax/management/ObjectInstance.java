/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

import java.io.Serializable;

public class ObjectInstance implements Serializable {
  private static final long serialVersionUID = -4099952623687795850L;

  private ObjectName name;
  private String className;

  public ObjectInstance(String objectName, String className) throws MalformedObjectNameException {
    this(new ObjectName(objectName), className);
  }

  public ObjectInstance(ObjectName objectName, String className) {
    if (objectName.isPattern()) {
      throw new RuntimeOperationsException(new IllegalArgumentException("Invalid name->" + objectName.toString()));
    }
    this.name = objectName;
    this.className = className;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ObjectInstance)) {
      return false;
    }
    ObjectInstance val = (ObjectInstance) object;
    if (!name.equals(val.getObjectName())) {
      return false;
    }
    if (className == null) {
      return val.getClassName() == null;
    }
    return className.equals(val.getClassName());
  }

  @Override
  public int hashCode() {
    return name.hashCode() ^ (className == null ? 0 : className.hashCode());
  }

  public ObjectName getObjectName() {
    return name;
  }

  public String getClassName() {
    return className;
  }

  @Override
  public String toString() {
    return getClassName() + "[" + getObjectName() + "]";
  }
}
