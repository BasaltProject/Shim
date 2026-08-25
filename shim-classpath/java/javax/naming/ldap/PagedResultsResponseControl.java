/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

import java.io.IOException;

public final class PagedResultsResponseControl extends BasicControl {
  private static final long serialVersionUID = -8819778744844514666L;

  public static final String OID = "1.2.840.113556.1.4.319";

  private final int resultSize;
  private final byte[] cookie;

  public PagedResultsResponseControl(String id, boolean criticality, byte[] value) throws IOException {
    super(id, criticality, value);
    Ber seq = new Ber(value).enter(Ber.SEQUENCE);
    resultSize = seq.readInt(Ber.INTEGER);
    byte[] c = seq.readBytes(Ber.OCTET_STRING);
    cookie = c.length == 0 ? null : c;
  }

  public int getResultSize() {
    return resultSize;
  }

  public byte[] getCookie() {
    return cookie;
  }
}
