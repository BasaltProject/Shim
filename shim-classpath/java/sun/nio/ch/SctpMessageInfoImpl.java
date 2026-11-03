/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.MessageInfo;
import java.net.SocketAddress;

public final class SctpMessageInfoImpl extends MessageInfo {
  private final int assocId;
  private Association association;
  private SocketAddress address;
  private int bytes;
  private int streamNumber;
  private boolean complete = true;
  private boolean unordered;
  private int ppid;
  private long timeToLive;

  public SctpMessageInfoImpl(int assocId, SocketAddress address, int streamNumber) {
    this.assocId = assocId;
    this.address = address;
    this.streamNumber = streamNumber;
  }

  public SctpMessageInfoImpl(Association association, SocketAddress address, int streamNumber) {
    this(association == null ? 0 : association.associationID(), address, streamNumber);
    this.association = association;
  }

  void received(Association association, SocketAddress address, int bytes, int stream, boolean complete,
      boolean unordered, int ppid) {
    this.association = association;
    this.address = address;
    this.bytes = bytes;
    this.streamNumber = stream;
    this.complete = complete;
    this.unordered = unordered;
    this.ppid = ppid;
  }

  public Association association() {
    return association;
  }

  int associationId() {
    return association != null ? association.associationID() : assocId;
  }

  public SocketAddress address() {
    return address;
  }

  public int bytes() {
    return bytes;
  }

  public int streamNumber() {
    return streamNumber;
  }

  public MessageInfo streamNumber(int streamNumber) {
    if (streamNumber < 0 || streamNumber > 65536) {
      throw new IllegalArgumentException("Invalid stream number");
    }
    this.streamNumber = streamNumber;
    return this;
  }

  public int payloadProtocolID() {
    return ppid;
  }

  public MessageInfo payloadProtocolID(int ppid) {
    this.ppid = ppid;
    return this;
  }

  public boolean isComplete() {
    return complete;
  }

  public MessageInfo complete(boolean complete) {
    this.complete = complete;
    return this;
  }

  public boolean isUnordered() {
    return unordered;
  }

  public MessageInfo unordered(boolean unordered) {
    this.unordered = unordered;
    return this;
  }

  public long timeToLive() {
    return timeToLive;
  }

  public MessageInfo timeToLive(long millis) {
    if (millis < 0) {
      throw new IllegalArgumentException("Invalid timeToLive: " + millis);
    }
    this.timeToLive = millis;
    return this;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append(" [Address: ").append(address).append(", Association: ").append(association).append(", Assoc ID: ")
        .append(assocId).append(", Bytes: ").append(bytes).append(", Stream Number: ").append(streamNumber)
        .append(", Complete: ").append(complete).append(", isUnordered: ").append(unordered).append("]");
    return sb.toString();
  }
}
