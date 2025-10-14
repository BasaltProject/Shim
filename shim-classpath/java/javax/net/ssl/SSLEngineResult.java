/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

public class SSLEngineResult {
  public enum Status {
    BUFFER_UNDERFLOW,
    BUFFER_OVERFLOW,
    OK,
    CLOSED;
  }

  public enum HandshakeStatus {
    NOT_HANDSHAKING,
    FINISHED,
    NEED_TASK,
    NEED_WRAP,
    NEED_UNWRAP,
    NEED_UNWRAP_AGAIN;
  }

  private final Status status;
  private final HandshakeStatus handshakeStatus;
  private final int bytesConsumed;
  private final int bytesProduced;
  private final long sequenceNumber;

  public SSLEngineResult(Status status, HandshakeStatus handshakeStatus, int bytesConsumed, int bytesProduced) {
    this(status, handshakeStatus, bytesConsumed, bytesProduced, -1L);
  }

  public SSLEngineResult(Status status, HandshakeStatus handshakeStatus, int bytesConsumed, int bytesProduced,
      long sequenceNumber) {
    if (status == null || handshakeStatus == null) {
      throw new IllegalArgumentException("Invalid Parameter(s)");
    }
    if (bytesConsumed < 0 || bytesProduced < 0) {
      throw new IllegalArgumentException("Invalid Parameter(s)");
    }
    this.status = status;
    this.handshakeStatus = handshakeStatus;
    this.bytesConsumed = bytesConsumed;
    this.bytesProduced = bytesProduced;
    this.sequenceNumber = sequenceNumber;
  }

  public final Status getStatus() {
    return status;
  }

  public final HandshakeStatus getHandshakeStatus() {
    return handshakeStatus;
  }

  public final int bytesConsumed() {
    return bytesConsumed;
  }

  public final int bytesProduced() {
    return bytesProduced;
  }

  public final long sequenceNumber() {
    return sequenceNumber;
  }

  @Override
  public String toString() {
    return "Status = " + status + " HandshakeStatus = " + handshakeStatus + "\nbytesConsumed = " + bytesConsumed
        + " bytesProduced = " + bytesProduced;
  }
}
