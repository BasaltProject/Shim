/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

public class NTNumericCredential {
  private long impersonationToken;

  public NTNumericCredential(long token) {
    this.impersonationToken = token;
  }

  public long getToken() {
    return impersonationToken;
  }

  public String toString() {
    return "NTNumericCredential: " + Long.toString(impersonationToken);
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof NTNumericCredential)) {
      return false;
    }
    NTNumericCredential that = (NTNumericCredential) o;
    return impersonationToken == that.getToken();
  }

  public int hashCode() {
    return (int) this.impersonationToken;
  }
}
