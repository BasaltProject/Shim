/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class Binding extends NameClassPair {
  private static final long serialVersionUID = 8839217842691845890L;

  private Object boundObj;

  public Binding(String name, Object obj) {
    super(name, null);
    this.boundObj = obj;
  }

  public Binding(String name, Object obj, boolean isRelative) {
    super(name, null, isRelative);
    this.boundObj = obj;
  }

  public Binding(String name, String className, Object obj) {
    super(name, className);
    this.boundObj = obj;
  }

  public Binding(String name, String className, Object obj, boolean isRelative) {
    super(name, className, isRelative);
    this.boundObj = obj;
  }

  @Override
  public String getClassName() {
    String cname = super.getClassName();
    if (cname != null) {
      return cname;
    }
    if (boundObj != null) {
      return boundObj.getClass().getName();
    } else {
      return null;
    }
  }

  public Object getObject() {
    return boundObj;
  }

  public void setObject(Object obj) {
    boundObj = obj;
  }

  @Override
  public String toString() {
    return super.toString() + ":" + getObject();
  }
}
