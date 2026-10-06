/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.nio.sctp;

public class Association {

  private final int associationID;
  private final int maxInbound;
  private final int maxOutbound;

  protected Association(int associationID, int maxInStreams, int maxOutStreams) {
    this.associationID = associationID;
    this.maxInbound = maxInStreams;
    this.maxOutbound = maxOutStreams;
  }

  public final int associationID() {
    return associationID;
  }
  public final int maxInboundStreams() {
    return maxInbound;
  }
  public final int maxOutboundStreams() {
    return maxOutbound;
  }
}
