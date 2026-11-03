/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class SortControl extends BasicControl {
  private static final long serialVersionUID = -1965961680233330744L;

  public static final String OID = "1.2.840.113556.1.4.473";

  public SortControl(String sortBy, boolean criticality) throws IOException {
    super(OID, criticality, null);
    super.value = encode(new SortKey[] {new SortKey(sortBy)});
  }

  public SortControl(String[] sortBy, boolean criticality) throws IOException {
    super(OID, criticality, null);
    SortKey[] keys = new SortKey[sortBy.length];
    for (int i = 0; i < sortBy.length; i++) {
      keys[i] = new SortKey(sortBy[i]);
    }
    super.value = encode(keys);
  }

  public SortControl(SortKey[] sortBy, boolean criticality) throws IOException {
    super(OID, criticality, null);
    super.value = encode(sortBy);
  }

  private static byte[] encode(SortKey[] keys) throws IOException {
    ByteArrayOutputStream all = new ByteArrayOutputStream();
    for (SortKey key : keys) {
      ByteArrayOutputStream one = new ByteArrayOutputStream();
      byte[] attr = Ber.tlv(Ber.OCTET_STRING, key.getAttributeID().getBytes("UTF-8"));
      one.write(attr, 0, attr.length);
      if (key.getMatchingRuleID() != null) {
        byte[] rule = Ber.tlv(0x80, key.getMatchingRuleID().getBytes("UTF-8"));
        one.write(rule, 0, rule.length);
      }
      if (!key.isAscending()) {
        byte[] rev = Ber.tlv(0x81, new byte[] {(byte) 0xff});
        one.write(rev, 0, rev.length);
      }
      byte[] seq = Ber.tlv(Ber.SEQUENCE, one.toByteArray());
      all.write(seq, 0, seq.length);
    }
    return Ber.tlv(Ber.SEQUENCE, all.toByteArray());
  }
}
