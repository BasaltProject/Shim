/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.util.Arrays;

public abstract class SNIServerName {
  private static final char[] kHexes = "0123456789ABCDEF".toCharArray();

  private final int type;
  private final byte[] encoded;

  protected SNIServerName(int type, byte[] encoded) {
    if (type < 0) {
      throw new IllegalArgumentException("Server name type cannot be less than zero");
    } else if (type > 255) {
      throw new IllegalArgumentException("Server name type cannot be greater than 255");
    }
    this.type = type;
    if (encoded == null) {
      throw new NullPointerException("Server name encoded value cannot be null");
    }
    this.encoded = encoded.clone();
  }

  public final int getType() {
    return type;
  }

  public final byte[] getEncoded() {
    return encoded.clone();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (getClass() != other.getClass()) {
      return false;
    }
    SNIServerName that = (SNIServerName) other;
    return type == that.type && Arrays.equals(encoded, that.encoded);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + type;
    result = 31 * result + Arrays.hashCode(encoded);
    return result;
  }

  @Override
  public String toString() {
    if (type == StandardConstants.SNI_HOST_NAME) {
      return "type=host_name (0), value=" + toHexString(encoded);
    }
    return "type=(" + type + "), value=" + toHexString(encoded);
  }

  private static String toHexString(byte[] bytes) {
    if (bytes.length == 0) {
      return "(empty)";
    }
    StringBuilder sb = new StringBuilder(bytes.length * 3 - 1);
    boolean first = true;
    for (byte b : bytes) {
      if (first) {
        first = false;
      } else {
        sb.append(':');
      }
      sb.append(kHexes[(b & 0xf0) >> 4]);
      sb.append(kHexes[b & 0x0f]);
    }
    return sb.toString();
  }
}
