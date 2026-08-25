/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

import java.io.IOException;

public final class PagedResultsControl extends BasicControl {
  private static final long serialVersionUID = 6684806685736844298L;

  public static final String OID = "1.2.840.113556.1.4.319";

  public PagedResultsControl(int pageSize, boolean criticality) throws IOException {
    super(OID, criticality, null);
    value = encode(pageSize, null);
  }

  public PagedResultsControl(int pageSize, byte[] cookie, boolean criticality) throws IOException {
    super(OID, criticality, null);
    value = encode(pageSize, cookie);
  }

  private static byte[] encode(int pageSize, byte[] cookie) {
    byte[] c = cookie == null ? new byte[0] : cookie;
    return Ber.tlv(Ber.SEQUENCE, Ber.concat(Ber.integer(Ber.INTEGER, pageSize), Ber.tlv(Ber.OCTET_STRING, c)));
  }
}
