/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.smartcardio;

import java.security.Permission;

public class CardPermission extends Permission {
  private static final long serialVersionUID = 7146787880530705613L;

  private static final int A_CONNECT = 0x01;
  private static final int A_EXCLUSIVE = 0x02;
  private static final int A_GET_BASIC_CHANNEL = 0x04;
  private static final int A_OPEN_LOGICAL_CHANNEL = 0x08;
  private static final int A_RESET = 0x10;
  private static final int A_TRANSMIT_CONTROL = 0x20;
  private static final int A_ALL = 0x3f;

  private transient int mask;
  private volatile String actions;

  public CardPermission(String terminalName, String actions) {
    super(terminalName);
    if (terminalName == null) {
      throw new NullPointerException();
    }
    this.actions = actions;
    mask = getMask(actions);
  }

  private static int getMask(String actions) {
    if ((actions == null) || (actions.length() == 0)) {
      throw new IllegalArgumentException("actions must not be empty");
    }
    int m = 0;
    for (String a : actions.split(",")) {
      String t = a.trim();
      if (t.equals("*")) {
        m |= A_ALL;
      } else if (t.equals("connect")) {
        m |= A_CONNECT;
      } else if (t.equals("exclusive")) {
        m |= A_EXCLUSIVE;
      } else if (t.equals("getBasicChannel")) {
        m |= A_GET_BASIC_CHANNEL;
      } else if (t.equals("openLogicalChannel")) {
        m |= A_OPEN_LOGICAL_CHANNEL;
      } else if (t.equals("reset")) {
        m |= A_RESET;
      } else if (t.equals("transmitControl")) {
        m |= A_TRANSMIT_CONTROL;
      } else {
        throw new IllegalArgumentException("Invalid action: '" + t + "'");
      }
    }
    return m;
  }

  public String getActions() {
    if (actions == null) {
      StringBuilder sb = new StringBuilder();
      String[] names = {"connect", "exclusive", "getBasicChannel", "openLogicalChannel", "reset", "transmitControl"};
      for (int i = 0; i < names.length; i++) {
        if ((mask & (1 << i)) != 0) {
          if (sb.length() > 0) {
            sb.append(',');
          }
          sb.append(names[i]);
        }
      }
      actions = sb.toString();
    }
    return actions;
  }

  public boolean implies(Permission permission) {
    if (permission instanceof CardPermission == false) {
      return false;
    }
    CardPermission other = (CardPermission) permission;
    if ((mask & other.mask) != other.mask) {
      return false;
    }
    String thisName = getName();
    if (thisName.equals("*")) {
      return true;
    }
    return thisName.equals(other.getName());
  }

  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof CardPermission == false) {
      return false;
    }
    CardPermission other = (CardPermission) obj;
    return getName().equals(other.getName()) && (mask == other.mask);
  }

  public int hashCode() {
    return getName().hashCode() + 31 * mask;
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    mask = getMask(actions);
  }
}
