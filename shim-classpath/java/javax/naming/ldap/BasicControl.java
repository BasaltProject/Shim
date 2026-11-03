/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

public class BasicControl implements Control {
  private static final long serialVersionUID = -4233907508771791687L;

  protected String id;
  protected boolean criticality = false;
  protected byte[] value = null;

  public BasicControl(String id) {
    this.id = id;
  }

  public BasicControl(String id, boolean criticality, byte[] value) {
    this.id = id;
    this.criticality = criticality;
    this.value = value;
  }

  public String getID() {
    return id;
  }

  public boolean isCritical() {
    return criticality;
  }

  public byte[] getEncodedValue() {
    return value;
  }
}
