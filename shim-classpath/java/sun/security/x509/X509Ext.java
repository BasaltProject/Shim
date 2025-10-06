/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.x509;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;
import sun.security.util.ObjectIdentifier;

public final class X509Ext {
  public final String oid;
  public final boolean critical;
  public final byte[] value;

  X509Ext(String oid, boolean critical, byte[] value) {
    this.oid = oid;
    this.critical = critical;
    this.value = value;
  }

  public static String nameOf(String oid) {
    switch (oid) {
    case "2.5.29.35":
      return "AuthorityKeyIdentifier";
    case "2.5.29.19":
      return "BasicConstraints";
    case "2.5.29.31":
      return "CRLDistributionPoints";
    case "2.5.29.32":
      return "CertificatePolicies";
    case "2.5.29.37":
      return "ExtendedKeyUsage";
    case "2.5.29.18":
      return "IssuerAlternativeName";
    case "2.5.29.15":
      return "KeyUsage";
    case "2.5.29.30":
      return "NameConstraints";
    case "2.5.29.33":
      return "PolicyMappings";
    case "2.5.29.36":
      return "PolicyConstraints";
    case "2.5.29.16":
      return "PrivateKeyUsage";
    case "2.5.29.17":
      return "SubjectAlternativeName";
    case "2.5.29.14":
      return "SubjectKeyIdentifier";
    case "2.5.29.54":
      return "InhibitAnyPolicy";
    case "1.3.6.1.5.5.7.1.1":
      return "AuthorityInfoAccess";
    case "1.3.6.1.5.5.7.1.11":
      return "SubjectInfoAccess";
    case "2.16.840.1.113730.1.1":
      return "NetscapeCertType";
    default:
      return oid;
    }
  }

  public static boolean isSupported(String oid) {
    return !nameOf(oid).equals(oid);
  }

  private static final String[] kKeyUsageNames = {"DigitalSignature", "Non_repudiation", "Key_Encipherment",
    "Data_Encipherment", "Key_Agreement", "Key_CertSign", "Crl_Sign", "Encipher_Only", "Decipher_Only"};

  static String ekuName(String oid) {
    switch (oid) {
    case "1.3.6.1.5.5.7.3.1":
      return "serverAuth";
    case "1.3.6.1.5.5.7.3.2":
      return "clientAuth";
    case "1.3.6.1.5.5.7.3.3":
      return "codeSigning";
    case "1.3.6.1.5.5.7.3.4":
      return "emailProtection";
    case "1.3.6.1.5.5.7.3.5":
      return "ipsecEndSystem";
    case "1.3.6.1.5.5.7.3.6":
      return "ipsecTunnel";
    case "1.3.6.1.5.5.7.3.7":
      return "ipsecUser";
    case "1.3.6.1.5.5.7.3.8":
      return "timeStamping";
    case "1.3.6.1.5.5.7.3.9":
      return "OCSPSigning";
    default:
      return oid;
    }
  }

  static boolean[] bitsOf(DerValue bitString) {
    byte[] data = bitString.getDataBytes();
    int unused = data.length == 0 ? 0 : data[0] & 0xff;
    int total = (data.length - 1) * 8 - unused;
    boolean[] bits = new boolean[Math.max(total, 9)];
    for (int i = 0; i < total; i++) {
      bits[i] = (data[1 + i / 8] & (0x80 >> (i % 8))) != 0;
    }
    return bits;
  }

  public static String hexDump(byte[] data) {
    return new HexDumpEncoder().encodeBuffer(data);
  }

  public static String describe(X509Ext ext) {
    try {
      DerValue v = new DerValue(ext.value);
      switch (ext.oid) {
      case "2.5.29.19": {
        DerInputStream in = v.data();
        boolean ca = false;
        String path = " undefined";
        if (in.available() > 0 && in.peekByte() == DerValue.tag_Boolean) {
          ca = in.getDerValue().getBoolean();
        }
        if (in.available() > 0) {
          path = Integer.toString(in.getInteger());
        }
        return "BasicConstraints:[\n  CA:" + ca + "\n  PathLen:" + path + "\n]\n";
      }
      case "2.5.29.15": {
        boolean[] bits = bitsOf(v);
        StringBuilder sb = new StringBuilder("KeyUsage [\n");
        for (int i = 0; i < kKeyUsageNames.length; i++) {
          if (bits[i]) {
            sb.append("  ").append(kKeyUsageNames[i]).append('\n');
          }
        }
        return sb.append("]\n").toString();
      }
      case "2.5.29.37": {
        StringBuilder sb = new StringBuilder("ExtendedKeyUsages [\n");
        DerInputStream in = v.data();
        while (in.available() > 0) {
          sb.append("  ").append(ekuName(in.getOID().toString())).append('\n');
        }
        return sb.append("]\n").toString();
      }
      case "2.5.29.17":
      case "2.5.29.18": {
        StringBuilder sb = new StringBuilder(ext.oid.equals("2.5.29.17") ? "SubjectAlternativeName [\n"
            : "IssuerAlternativeName [\n");
        DerInputStream in = v.data();
        while (in.available() > 0) {
          sb.append("  ").append(describeGeneralName(in.getDerValue())).append('\n');
        }
        return sb.append("]\n").toString();
      }
      case "2.5.29.14": {
        byte[] id = v.getOctetString();
        return "SubjectKeyIdentifier [\nKeyIdentifier [\n" + hexDump(id) + "]\n]\n";
      }
      case "2.5.29.35": {
        StringBuilder sb = new StringBuilder("AuthorityKeyIdentifier [\n");
        DerInputStream in = v.data();
        while (in.available() > 0) {
          DerValue item = in.getDerValue();
          if (item.isContextSpecific((byte) 0)) {
            sb.append("KeyIdentifier [\n").append(hexDump(item.getDataBytes())).append("]\n");
          } else if (item.isContextSpecific((byte) 2)) {
            sb.append("SerialNumber: ").append(new BigInteger(item.getDataBytes())).append('\n');
          }
        }
        return sb.append("]\n").toString();
      }
      default:
        return "Extension unknown: DER encoded OCTET string =\n" + hexDump(ext.value) + "\n";
      }
    } catch (IOException e) {
      return "Extension unparseable: DER encoded OCTET string =\n" + hexDump(ext.value) + "\n";
    }
  }

  static String describeGeneralName(DerValue gn) throws IOException {
    int tag = gn.getTag() & 0x1f;
    byte[] d = gn.getDataBytes();
    switch (tag) {
    case 1:
      return "RFC822Name: " + new String(d, StandardCharsets.ISO_8859_1);
    case 2:
      return "DNSName: " + new String(d, StandardCharsets.ISO_8859_1);
    case 4:
      return "DirName: " + X500Name.decode(d).toString();
    case 6:
      return "URIName: " + new String(d, StandardCharsets.ISO_8859_1);
    case 7:
      return "IPAddress: " + ipText(d);
    case 8:
      return "OIDName: " + ObjectIdentifier.decode(d);
    default:
      return "GeneralName type " + tag + ": " + hexDump(d);
    }
  }

  static String ipText(byte[] d) {
    if (d.length == 4) {
      return (d[0] & 0xff) + "." + (d[1] & 0xff) + "." + (d[2] & 0xff) + "." + (d[3] & 0xff);
    }
    if (d.length == 16) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 16; i += 2) {
        if (i > 0) {
          sb.append(':');
        }
        sb.append(Integer.toHexString(((d[i] & 0xff) << 8) | (d[i + 1] & 0xff)));
      }
      return sb.toString();
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < d.length; i++) {
      if (i > 0) {
        sb.append('.');
      }
      sb.append(d[i] & 0xff);
    }
    return sb.toString();
  }
}
