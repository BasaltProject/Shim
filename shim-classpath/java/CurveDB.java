/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.util;

import java.math.BigInteger;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CurveDB {
  public static final class Info {
    public final String name;
    public final String[] aliases;
    public final String oid;
    public final int keySize;
    public final boolean supported;

    Info(String name, String[] aliases, String oid, int keySize, boolean supported) {
      this.name = name;
      this.aliases = aliases;
      this.oid = oid;
      this.keySize = keySize;
      this.supported = supported;
    }

    public String displayName() {
      if (aliases.length == 0) {
        return name;
      }
      StringBuilder sb = new StringBuilder(name).append(" [");
      for (int i = 0; i < aliases.length; i++) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(aliases[i]);
      }
      return sb.append(']').toString();
    }

    @Override
    public String toString() {
      return displayName() + " (" + oid + ")";
    }
  }

  private static final List<Info> kInfos = new ArrayList<Info>();
  private static final Map<String, NamedCurve> kCurves = new HashMap<String, NamedCurve>();

  private static void add(String name, String[] aliases, String oid, int keySize, boolean supported) {
    kInfos.add(new Info(name, aliases, oid, keySize, supported));
  }

  static {
    add("secp256r1", new String[] {"NIST P-256", "X9.62 prime256v1"}, "1.2.840.10045.3.1.7", 256, true);
    add("secp384r1", new String[] {"NIST P-384"}, "1.3.132.0.34", 384, true);
    add("secp521r1", new String[] {"NIST P-521"}, "1.3.132.0.35", 521, true);
    add("secp112r1", new String[] {}, "1.3.132.0.6", 112, false);
    add("secp112r2", new String[] {}, "1.3.132.0.7", 112, false);
    add("secp128r1", new String[] {}, "1.3.132.0.28", 128, false);
    add("secp128r2", new String[] {}, "1.3.132.0.29", 128, false);
    add("secp160k1", new String[] {}, "1.3.132.0.9", 160, false);
    add("secp160r1", new String[] {}, "1.3.132.0.8", 160, false);
    add("secp160r2", new String[] {}, "1.3.132.0.30", 160, false);
    add("secp192k1", new String[] {}, "1.3.132.0.31", 192, false);
    add("secp192r1", new String[] {"NIST P-192", "X9.62 prime192v1"}, "1.2.840.10045.3.1.1", 192, false);
    add("secp224k1", new String[] {}, "1.3.132.0.32", 224, false);
    add("secp224r1", new String[] {"NIST P-224"}, "1.3.132.0.33", 224, false);
    add("secp256k1", new String[] {}, "1.3.132.0.10", 256, false);
    add("sect113r1", new String[] {}, "1.3.132.0.4", 113, false);
    add("sect163k1", new String[] {"NIST K-163"}, "1.3.132.0.1", 163, false);
    add("sect233k1", new String[] {"NIST K-233"}, "1.3.132.0.26", 233, false);
    add("sect283k1", new String[] {"NIST K-283"}, "1.3.132.0.16", 283, false);
    add("sect409k1", new String[] {"NIST K-409"}, "1.3.132.0.36", 409, false);
    add("sect571k1", new String[] {"NIST K-571"}, "1.3.132.0.38", 571, false);
    add("sect571r1", new String[] {"NIST B-571"}, "1.3.132.0.39", 571, false);
    add("X9.62 c2tnb191v1", new String[] {}, "1.2.840.10045.3.0.5", 191, false);
    add("X9.62 prime192v2", new String[] {}, "1.2.840.10045.3.1.2", 192, false);
    add("X9.62 prime239v1", new String[] {}, "1.2.840.10045.3.1.4", 239, false);
    add("brainpoolP160r1", new String[] {}, "1.3.36.3.3.2.8.1.1.1", 160, false);
    add("brainpoolP192r1", new String[] {}, "1.3.36.3.3.2.8.1.1.3", 192, false);
    add("brainpoolP224r1", new String[] {}, "1.3.36.3.3.2.8.1.1.5", 224, false);
    add("brainpoolP256r1", new String[] {}, "1.3.36.3.3.2.8.1.1.7", 256, false);
    add("brainpoolP320r1", new String[] {}, "1.3.36.3.3.2.8.1.1.9", 320, false);
    add("brainpoolP384r1", new String[] {}, "1.3.36.3.3.2.8.1.1.11", 384, false);
    add("brainpoolP512r1", new String[] {}, "1.3.36.3.3.2.8.1.1.13", 512, false);
  }

  private CurveDB() {
  }

  public static Info lookupInfo(String nameOrOid) {
    if (nameOrOid == null) {
      return null;
    }
    for (Info i : kInfos) {
      if (i.name.equalsIgnoreCase(nameOrOid) || i.oid.equals(nameOrOid)) {
        return i;
      }
      for (String a : i.aliases) {
        if (a.equalsIgnoreCase(nameOrOid)) {
          return i;
        }
      }
    }
    return null;
  }

  public static Info lookupInfoBySize(int keySize) {
    for (Info i : kInfos) {
      if (i.keySize == keySize && i.supported) {
        return i;
      }
    }
    for (Info i : kInfos) {
      if (i.keySize == keySize && i.aliases.length > 0 && !i.name.startsWith("sect") && !i.name.startsWith("X9.62")) {
        return i;
      }
    }
    for (Info i : kInfos) {
      if (i.keySize == keySize && (i.name.startsWith("secp") || i.name.startsWith("brainpool"))) {
        return i;
      }
    }
    return null;
  }

  public static synchronized NamedCurve lookup(String nameOrOid) {
    Info info = lookupInfo(nameOrOid);
    if (info == null || !info.supported) {
      return null;
    }
    return curve(info);
  }

  public static synchronized NamedCurve curve(Info info) {
    NamedCurve nc = kCurves.get(info.name);
    if (nc == null) {
      nc = create(info);
      kCurves.put(info.name, nc);
    }
    return nc;
  }

  public static NamedCurve lookup(ECParameterSpec params) {
    if (params instanceof NamedCurve) {
      return (NamedCurve) params;
    }
    for (Info i : kInfos) {
      if (!i.supported) {
        continue;
      }
      NamedCurve nc = curve(i);
      if (nc.getCurve().getField().getFieldSize() == params.getCurve().getField().getFieldSize()
          && nc.getCurve().equals(params.getCurve()) && nc.getGenerator().equals(params.getGenerator())
          && nc.getOrder().equals(params.getOrder()) && nc.getCofactor() == params.getCofactor()) {
        return nc;
      }
    }
    return null;
  }

  private static NamedCurve create(Info info) {
    switch (info.name) {
      case "secp256r1":
        return build(info, "ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", "ffffffff00000001000000000000000000000000fffffffffffffffffffffffc", "5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", "6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", "4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", "ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551");
      case "secp384r1":
        return build(info, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffeffffffff0000000000000000ffffffff", "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffeffffffff0000000000000000fffffffc", "b3312fa7e23ee7e4988e056be3f82d19181d9c6efe8141120314088f5013875ac656398d8a2ed19d2a85c8edd3ec2aef", "aa87ca22be8b05378eb1c71ef320ad746e1d3b628ba79b9859f741e082542a385502f25dbf55296c3a545e3872760ab7", "3617de4a96262c6f5d9e98bf9292dc29f8f41dbd289a147ce9da3113b5f0b8c00a60b1ce1d7e819d7a431d7c90ea0e5f", "ffffffffffffffffffffffffffffffffffffffffffffffffc7634d81f4372ddf581a0db248b0a77aecec196accc52973");
      case "secp521r1":
        return build(info, "1ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", "1fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffc", "51953eb9618e1c9a1f929a21a0b68540eea2da725b99b315f3b8b489918ef109e156193951ec7e937b1652c0bd3bb1bf073573df883d2c34f1ef451fd46b503f00", "c6858e06b70404e9cd9e3ecb662395b4429c648139053fb521f828af606b4d3dbaa14b5e77efe75928fe1dc127a2ffa8de3348b3c1856a429bf97e7e31c2e5bd66", "11839296a789a3bc0045c8a5fb42c7d1bd998f54449579b446817afbd17273e662c97ee72995ef42640c550b9013fad0761353c7086a272c24088be94769fd16650", "1fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffa51868783bf2f966b7fcc0148f709a5d03bb5c9b8899c47aebb6fb71e91386409");
      default:
        return null;
    }
  }

  private static NamedCurve build(Info info, String p, String a, String b, String gx, String gy, String n) {
    EllipticCurve curve = new EllipticCurve(new ECFieldFp(new BigInteger(p, 16)), new BigInteger(a, 16),
        new BigInteger(b, 16));
    ECPoint g = new ECPoint(new BigInteger(gx, 16), new BigInteger(gy, 16));
    return new NamedCurve(info.displayName(), info.oid, curve, g, new BigInteger(n, 16), 1);
  }
}
