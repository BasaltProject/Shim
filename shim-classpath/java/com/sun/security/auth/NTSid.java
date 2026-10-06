/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth;

import java.security.Principal;

public class NTSid implements Principal, java.io.Serializable {
  private static final long serialVersionUID = 4412290580770249885L;

  private String sid;

  public NTSid(String stringSid) {
    if (stringSid == null) {
      throw new NullPointerException("invalid null input: stringSid");
    }
    if (stringSid.length() == 0) {
      throw new IllegalArgumentException("Invalid NTSid value");
    }
    sid = new String(stringSid);
  }

  public String getName() {
    return sid;
  }

  public String toString() {
    return "NTSid: " + sid;
  }

  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (!(o instanceof NTSid)) {
      return false;
    }
    NTSid that = (NTSid) o;
    return sid.equals(that.sid);
  }

  public int hashCode() {
    return sid.hashCode();
  }
}
