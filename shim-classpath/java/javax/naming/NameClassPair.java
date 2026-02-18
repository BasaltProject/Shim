/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

import java.io.Serializable;

public class NameClassPair implements Serializable {
  private static final long serialVersionUID = 5620776610160863339L;

  private String name;
  private String className;
  private String fullName = null;
  private boolean isRel = true;

  public NameClassPair(String name, String className) {
    this.name = name;
    this.className = className;
  }

  public NameClassPair(String name, String className, boolean isRelative) {
    this.name = name;
    this.className = className;
    this.isRel = isRelative;
  }

  public String getClassName() {
    return className;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setClassName(String name) {
    this.className = name;
  }

  public boolean isRelative() {
    return isRel;
  }

  public void setRelative(boolean r) {
    isRel = r;
  }

  public String getNameInNamespace() {
    if (fullName == null) {
      throw new UnsupportedOperationException();
    }
    return fullName;
  }

  public void setNameInNamespace(String fullName) {
    this.fullName = fullName;
  }

  @Override
  public String toString() {
    return (isRelative() ? "" : "(not relative)") + getName() + ": " + getClassName();
  }
}
