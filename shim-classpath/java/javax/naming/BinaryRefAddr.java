/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

public class BinaryRefAddr extends RefAddr {
  private static final long serialVersionUID = -3415254970957330361L;

  private byte[] buf;

  public BinaryRefAddr(String addrType, byte[] src) {
    this(addrType, src, 0, src.length);
  }

  public BinaryRefAddr(String addrType, byte[] src, int offset, int count) {
    super(addrType);
    buf = new byte[count];
    System.arraycopy(src, offset, buf, 0, count);
  }

  public Object getContent() {
    return buf;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BinaryRefAddr) {
      BinaryRefAddr target = (BinaryRefAddr) obj;
      if (addrType.compareTo(target.addrType) == 0) {
        return java.util.Arrays.equals(buf, target.buf);
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = addrType.hashCode();
    for (int i = 0; i < buf.length; i++) {
      hash += buf[i];
    }
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder("Address Type: " + addrType + "\n");
    str.append("AddressContents: ");
    for (int i = 0; i < buf.length && i < 32; i++) {
      str.append(Integer.toHexString(buf[i])).append(' ');
    }
    if (buf.length >= 32) {
      str.append(" ...\n");
    }
    return str.toString();
  }
}
