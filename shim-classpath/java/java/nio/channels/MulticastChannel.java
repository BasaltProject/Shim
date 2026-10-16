/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;

public interface MulticastChannel extends NetworkChannel {
  @Override
  void close() throws IOException;

  MembershipKey join(InetAddress group, NetworkInterface interf) throws IOException;

  MembershipKey join(InetAddress group, NetworkInterface interf, InetAddress source) throws IOException;
}
