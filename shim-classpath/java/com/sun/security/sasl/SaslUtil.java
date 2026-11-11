/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.sasl;

import java.util.Map;

public final class SaslUtil {
  public static final int NOPLAINTEXT = 0x01;
  public static final int NOACTIVE = 0x02;
  public static final int NODICTIONARY = 0x04;
  public static final int FORWARD_SECRECY = 0x08;
  public static final int NOANONYMOUS = 0x10;
  public static final int PASS_CREDENTIALS = 0x20;

  private SaslUtil() {
  }

  public static boolean policyMatches(int flags, Map<String, ?> props) {
    if (props == null) {
      return true;
    }
    if (flagSet(props, javax.security.sasl.Sasl.POLICY_NOPLAINTEXT) && (flags & NOPLAINTEXT) == 0) {
      return false;
    }
    if (flagSet(props, javax.security.sasl.Sasl.POLICY_NOACTIVE) && (flags & NOACTIVE) == 0) {
      return false;
    }
    if (flagSet(props, javax.security.sasl.Sasl.POLICY_NODICTIONARY) && (flags & NODICTIONARY) == 0) {
      return false;
    }
    if (flagSet(props, javax.security.sasl.Sasl.POLICY_NOANONYMOUS) && (flags & NOANONYMOUS) == 0) {
      return false;
    }
    if (flagSet(props, javax.security.sasl.Sasl.POLICY_FORWARD_SECRECY) && (flags & FORWARD_SECRECY) == 0) {
      return false;
    }
    if (flagSet(props, javax.security.sasl.Sasl.POLICY_PASS_CREDENTIALS) && (flags & PASS_CREDENTIALS) == 0) {
      return false;
    }
    return true;
  }

  private static boolean flagSet(Map<String, ?> props, String key) {
    Object v = props.get(key);
    return v != null && "true".equalsIgnoreCase(String.valueOf(v));
  }

  public static String hex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (byte x : b) {
      sb.append(Character.forDigit((x >> 4) & 0xf, 16)).append(Character.forDigit(x & 0xf, 16));
    }
    return sb.toString();
  }

  public static byte[] hmacMd5(byte[] key, byte[] text) {
    try {
      javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacMD5");
      mac.init(new javax.crypto.spec.SecretKeySpec(key.length == 0 ? new byte[1] : key, "HmacMD5"));
      return mac.doFinal(text);
    } catch (java.security.GeneralSecurityException e) {
      throw new IllegalStateException(e);
    }
  }
}
