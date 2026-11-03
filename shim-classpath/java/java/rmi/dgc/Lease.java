/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.dgc;

import java.io.Serializable;

public final class Lease implements Serializable {
  private VMID vmid;
  private long value;
  private static final long serialVersionUID = -5713411624328831948L;

  public Lease(VMID id, long duration) {
    vmid = id;
    value = duration;
  }

  public VMID getVMID() {
    return vmid;
  }

  public long getValue() {
    return value;
  }
}
