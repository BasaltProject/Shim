/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.rsa;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

final class RSAUtil {
  static final String kOidRsa = "1.2.840.113549.1.1.1";
  static final String kOidPss = "1.2.840.113549.1.1.10";
  static final String kOidMgf1 = "1.2.840.113549.1.1.8";
  static final String kOidPSpecified = "1.2.840.113549.1.1.9";

  private RSAUtil() {
  }

  static String digestOid(String name) {
    switch (name.toUpperCase(java.util.Locale.ROOT)) {
    case "SHA-1":
    case "SHA1":
    case "SHA":
      return "1.3.14.3.2.26";
    case "SHA-224":
      return "2.16.840.1.101.3.4.2.4";
    case "SHA-256":
      return "2.16.840.1.101.3.4.2.1";
    case "SHA-384":
      return "2.16.840.1.101.3.4.2.2";
    case "SHA-512":
      return "2.16.840.1.101.3.4.2.3";
    case "SHA-512/224":
      return "2.16.840.1.101.3.4.2.5";
    case "SHA-512/256":
      return "2.16.840.1.101.3.4.2.6";
    case "SHA3-224":
      return "2.16.840.1.101.3.4.2.7";
    case "SHA3-256":
      return "2.16.840.1.101.3.4.2.8";
    case "SHA3-384":
      return "2.16.840.1.101.3.4.2.9";
    case "SHA3-512":
      return "2.16.840.1.101.3.4.2.10";
    case "MD2":
      return "1.2.840.113549.2.2";
    case "MD5":
      return "1.2.840.113549.2.5";
    default:
      return null;
    }
  }

  static String digestName(String oid) {
    String[] names = {"SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512", "SHA-512/224", "SHA-512/256", "SHA3-224",
      "SHA3-256", "SHA3-384", "SHA3-512"};
    for (String n : names) {
      if (digestOid(n).equals(oid)) {
        return n;
      }
    }
    return null;
  }

  static DerValue algId(String oid, DerValue params) throws IOException {
    DerOutputStream out = new DerOutputStream();
    out.putOID(new ObjectIdentifier(oid));
    if (params != null) {
      out.putDerValue(params);
    }
    return new DerValue(DerValue.tag_Sequence, out.toByteArray());
  }

  static DerValue digestAlgId(String digest) throws IOException {
    return algId(digestOid(digest), DerValue.nullValue());
  }

  static DerValue explicit(int tagNo, DerValue inner) throws IOException {
    return new DerValue((byte) (0xa0 | tagNo), inner.toByteArray());
  }

  static byte[] encodePssParams(PSSParameterSpec spec) throws IOException {
    DerOutputStream body = new DerOutputStream();
    String md = spec.getDigestAlgorithm();
    if (!md.equalsIgnoreCase("SHA-1")) {
      body.putDerValue(explicit(0, digestAlgId(md)));
    }
    String mgfMd = "SHA-1";
    AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
    if (mgfSpec instanceof MGF1ParameterSpec) {
      mgfMd = ((MGF1ParameterSpec) mgfSpec).getDigestAlgorithm();
    }
    if (!mgfMd.equalsIgnoreCase("SHA-1")) {
      body.putDerValue(explicit(1, algId(kOidMgf1, digestAlgId(mgfMd))));
    }
    if (spec.getSaltLength() != 20) {
      body.putDerValue(explicit(2, DerValue.integer(java.math.BigInteger.valueOf(spec.getSaltLength()))));
    }
    if (spec.getTrailerField() != 1) {
      body.putDerValue(explicit(3, DerValue.integer(java.math.BigInteger.valueOf(spec.getTrailerField()))));
    }
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, body);
    return out.toByteArray();
  }

  static PSSParameterSpec decodePssParams(byte[] encoded) throws IOException {
    DerValue seq = new DerValue(encoded);
    if (seq.getTag() != DerValue.tag_Sequence) {
      throw new IOException("Invalid RSASSA-PSS parameters");
    }
    String md = "SHA-1";
    String mgfMd = "SHA-1";
    int salt = 20;
    int trailer = 1;
    DerInputStream in = seq.data();
    while (in.available() > 0) {
      DerValue v = in.getDerValue();
      DerValue inner = new DerValue(v.getDataBytes());
      if (v.isContextSpecific((byte) 0)) {
        DerInputStream ai = inner.data();
        String oid = ai.getOID().toString();
        md = digestName(oid);
        if (md == null) {
          throw new IOException("Unsupported hash algorithm: " + oid);
        }
      } else if (v.isContextSpecific((byte) 1)) {
        DerInputStream ai = inner.data();
        String oid = ai.getOID().toString();
        if (!oid.equals(kOidMgf1)) {
          throw new IOException("Unsupported mask generation function: " + oid);
        }
        DerValue hashId = ai.getDerValue();
        mgfMd = digestName(hashId.data().getOID().toString());
        if (mgfMd == null) {
          throw new IOException("Unsupported MGF1 hash algorithm");
        }
      } else if (v.isContextSpecific((byte) 2)) {
        salt = inner.getInteger();
      } else if (v.isContextSpecific((byte) 3)) {
        trailer = inner.getInteger();
      } else {
        throw new IOException("Invalid RSASSA-PSS parameters");
      }
    }
    return new PSSParameterSpec(md, "MGF1", new MGF1ParameterSpec(mgfMd), salt, trailer);
  }
}
