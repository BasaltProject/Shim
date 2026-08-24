/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public abstract class RefAddr implements java.io.Serializable {
  private static final long serialVersionUID = -1468165120479154358L;

  protected String addrType;

  protected RefAddr(String addrType) {
    this.addrType = addrType;
  }

  public String getType() {
    return addrType;
  }

  public abstract Object getContent();

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RefAddr) {
      RefAddr target = (RefAddr) obj;
      if (addrType.compareTo(target.addrType) == 0) {
        Object thisobj = this.getContent();
        Object thatobj = target.getContent();
        if (thisobj == thatobj) {
          return true;
        }
        if (thisobj != null) {
          return thisobj.equals(thatobj);
        }
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (getContent() == null) ? addrType.hashCode() : addrType.hashCode() + getContent().hashCode();
  }

  @Override
  public String toString() {
    return "Type: " + addrType + "\nContent: " + getContent() + "\n";
  }
}
