/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.spi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

public interface InetAddressResolver {
  Stream<InetAddress> lookupByName(String host, LookupPolicy lookupPolicy) throws UnknownHostException;

  String lookupByAddress(byte[] addr) throws UnknownHostException;

  final class LookupPolicy {
    public static final int IPV4 = 1 << 0;
    public static final int IPV6 = 1 << 1;
    public static final int IPV4_FIRST = 1 << 2;
    public static final int IPV6_FIRST = 1 << 3;

    private final int characteristics;

    private LookupPolicy(int characteristics) {
      this.characteristics = characteristics;
    }

    public static LookupPolicy of(int characteristics) {
      if ((characteristics & (IPV4 | IPV6)) == 0) {
        throw new IllegalArgumentException("No address type specified");
      }
      if ((characteristics & IPV4_FIRST) != 0 && (characteristics & IPV6_FIRST) != 0) {
        throw new IllegalArgumentException("Addresses order cannot be determined");
      }
      if (((characteristics & IPV4) == 0 && (characteristics & IPV4_FIRST) != 0)
          || ((characteristics & IPV6) == 0 && (characteristics & IPV6_FIRST) != 0)) {
        throw new IllegalArgumentException("Addresses order and type do not match");
      }
      return new LookupPolicy(characteristics);
    }

    public int characteristics() {
      return characteristics;
    }
  }
}
