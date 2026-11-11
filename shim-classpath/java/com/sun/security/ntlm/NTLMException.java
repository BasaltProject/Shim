/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.ntlm;

import java.security.GeneralSecurityException;

public final class NTLMException extends GeneralSecurityException {
  private static final long serialVersionUID = -3298539507906689430L;

  public static final int PACKET_READ_ERROR = 1;
  public static final int NO_DOMAIN_INFO = 2;
  public static final int USER_UNKNOWN = 3;
  public static final int AUTH_FAILED = 4;
  public static final int BAD_VERSION = 5;
  public static final int PROTOCOL = 6;

  private final int errorCode;

  public NTLMException(int errorCode, String msg) {
    super(msg);
    this.errorCode = errorCode;
  }

  public int errorCode() {
    return errorCode;
  }
}
