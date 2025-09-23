/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.channels.MembershipKey;
import java.nio.channels.MulticastChannel;
import shim.NetNative;

final class MembershipKeyImpl extends MembershipKey {
  private final DatagramChannelImpl channel;
  private final InetAddress group;
  private final NetworkInterface interf;
  private volatile boolean valid = true;

  MembershipKeyImpl(DatagramChannelImpl channel, InetAddress group, NetworkInterface interf) {
    this.channel = channel;
    this.group = group;
    this.interf = interf;
  }

  @Override
  public boolean isValid() {
    return valid;
  }

  @Override
  public void drop() {
    if (valid) {
      valid = false;
      channel.drop(this);
    }
  }

  @Override
  public MembershipKey block(InetAddress source) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public MembershipKey unblock(InetAddress source) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MulticastChannel channel() {
    return channel;
  }

  @Override
  public InetAddress group() {
    return group;
  }

  @Override
  public NetworkInterface networkInterface() {
    return interf;
  }

  @Override
  public InetAddress sourceAddress() {
    return null;
  }

  void invalidate() {
    valid = false;
  }

  @Override
  public String toString() {
    return "<" + group.getHostAddress() + "," + interf.getName() + ">";
  }
}
