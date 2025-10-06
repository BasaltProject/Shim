/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider.certpath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class PKCS7Util {
  private PKCS7Util() {
  }

  public static byte[] encode(List<byte[]> certs) {
    try {
      DerOutputStream certSet = new DerOutputStream();
      for (byte[] c : certs) {
        certSet.write(c);
      }
      DerOutputStream signed = new DerOutputStream();
      signed.putInteger(1);
      signed.write(DerValue.tag_Set, new byte[0]);
      DerOutputStream dataInfo = new DerOutputStream();
      dataInfo.putOID(new ObjectIdentifier("1.2.840.113549.1.7.1"));
      signed.write(DerValue.tag_Sequence, dataInfo);
      signed.write((byte) 0xa0, certSet.toByteArray());
      signed.write(DerValue.tag_Set, new byte[0]);
      DerOutputStream signedSeq = new DerOutputStream();
      signedSeq.write(DerValue.tag_Sequence, signed);
      DerOutputStream info = new DerOutputStream();
      info.putOID(new ObjectIdentifier("1.2.840.113549.1.7.2"));
      info.write((byte) 0xa0, signedSeq.toByteArray());
      DerOutputStream out = new DerOutputStream();
      out.write(DerValue.tag_Sequence, info);
      return out.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static List<byte[]> certificates(byte[] data) throws IOException {
    if (data.length == 0) {
      throw new IOException("Empty input");
    }
    DerValue info = new DerValue(data);
    DerInputStream in = info.data();
    String oid = in.getOID().toString();
    if (!oid.equals("1.2.840.113549.1.7.2")) {
      throw new IOException("content type should be of system data or signed data");
    }
    DerValue content = new DerValue(in.getDerValue().getDataBytes());
    DerInputStream si = content.data();
    si.getInteger();
    si.getDerValue();
    si.getDerValue();
    List<byte[]> result = new ArrayList<byte[]>();
    while (si.available() > 0) {
      DerValue item = si.getDerValue();
      if (item.isContextSpecific((byte) 0)) {
        DerInputStream ci = new DerInputStream(item.getDataBytes());
        while (ci.available() > 0) {
          result.add(ci.getDerValue().toByteArray());
        }
      }
    }
    return result;
  }
}
