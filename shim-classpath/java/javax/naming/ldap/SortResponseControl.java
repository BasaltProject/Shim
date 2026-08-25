/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

import java.io.IOException;
import javax.naming.NamingException;

public final class SortResponseControl extends BasicControl {
  private static final long serialVersionUID = 5142939176006310877L;

  public static final String OID = "1.2.840.113556.1.4.474";

  private int resultCode = 0;
  private String badAttrId = null;

  public SortResponseControl(String id, boolean criticality, byte[] value) throws IOException {
    super(id, criticality, value);
    Ber seq = new Ber(value).enter(Ber.SEQUENCE);
    resultCode = seq.readInt(Ber.ENUMERATED);
    if (seq.hasMore() && seq.peekTag() == 0x80) {
      badAttrId = new String(seq.readBytes(0x80), "UTF-8");
    }
  }

  public boolean isSorted() {
    return resultCode == 0;
  }

  public int getResultCode() {
    return resultCode;
  }

  public String getAttributeID() {
    return badAttrId;
  }

  public NamingException getException() {
    switch (resultCode) {
      case 0:
        return null;
      case 3:
        return new javax.naming.TimeLimitExceededException(msg("Timelimit Exceeded"));
      case 11:
        return new javax.naming.LimitExceededException(msg("Administrative Limit Exceeded"));
      case 12:
        return new javax.naming.OperationNotSupportedException(msg("Unavailable Critical Extension"));
      case 16:
        return new javax.naming.directory.NoSuchAttributeException(msg("No Such Attribute"));
      case 18:
        return new javax.naming.directory.InvalidSearchFilterException(msg("Inappropriate Matching"));
      case 50:
        return new javax.naming.NoPermissionException(msg("Insufficient Access Rights"));
      case 51:
        return new javax.naming.ServiceUnavailableException(msg("Busy"));
      case 53:
        return new javax.naming.OperationNotSupportedException(msg("Unwilling To Perform"));
      default:
        return new NamingException(msg("Other"));
    }
  }

  private String msg(String text) {
    return "[LDAP: error code " + resultCode + " - " + text + "]";
  }
}
