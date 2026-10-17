/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import javax.security.auth.x500.X500Principal; import java.util.*;
public class X500PrincipalModern { static byte[] name(int o1, int o2, int o3, int tag, byte[] v) { byte[] ava = new byte[2 + 5 + 2 + v.length]; ava[0] = 0x30; ava[1] = (byte) (5 + 2 + v.length); ava[2] = 0x06; ava[3] = 3; ava[4] = (byte) o1; ava[5] = (byte) o2; ava[6] = (byte) o3; ava[7] = (byte) tag; ava[8] = (byte) v.length; System.arraycopy(v, 0, ava, 9, v.length); byte[] set = new byte[2 + ava.length]; set[0] = 0x31; set[1] = (byte) ava.length; System.arraycopy(ava, 0, set, 2, ava.length); byte[] out = new byte[2 + set.length]; out[0] = 0x30; out[1] = (byte) set.length; System.arraycopy(set, 0, out, 2, set.length); return out; }
  public static void main(String[] a) throws Exception {
  String[] dns = {"CN=Test", "CN=Test, O=Org, C=US", "cn=test,o=org,c=us", "CN=a\\,b, O=x", "CN=\"quoted, value\", O=y", "CN=Test + OU=Unit, O=Org", "OU=A+CN=B,O=Z", "1.2.3.4=#0c0141, CN=x", "CN=  spaces  ,O=a", "CN=\\ lead", "CN=trail\\ ", "CN=#04024142", "EMAILADDRESS=a@b.com, CN=x", "E=a@b.com", "DC=example, DC=com", "UID=u1, CN=x", "STREET=1 Main, L=City, ST=State, C=US", "S=State, C=US", "T=Title, CN=x", "SERIALNUMBER=123, CN=x", "SURNAME=Doe, GIVENNAME=Jo", "CN=José, O=Café", "CN=中文", "CN=a;b", "CN=a<b>c", "CN=a\"b", "CN=a\\\\b", "CN=a=b", "CN=#hexnot", "OID.1.2.840.113549.1.9.1=x@y.z", "CN=Test,", "CN=,O=x", "", "CN=A,CN=B", "C=US,O=Org,CN=Test"};
  for (String d : dns) {
    try {
      X500Principal p = new X500Principal(d);
      System.out.println("[" + d + "] enc=" + HexFormat.of().formatHex(p.getEncoded()));
      System.out.println("  default=" + p.getName() + " | 2253=" + p.getName(X500Principal.RFC2253) + " | 1779=" + p.getName(X500Principal.RFC1779) + " | canon=" + p.getName(X500Principal.CANONICAL) + " | str=" + p);
    } catch (Exception e) { System.out.println("[" + d + "] " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
  }
  System.out.println(new X500Principal("CN=A").equals(new X500Principal("cn=a")) + " " + new X500Principal("CN=A, O=B").equals(new X500Principal("CN=A,O=B")) + " " + (new X500Principal("CN=A").hashCode() == new X500Principal("cn =  a").hashCode()) + " " + new X500Principal("CN=A").equals(null) + " " + new X500Principal("CN=Test").getName());
  int[] tags = {0x13, 0x0c, 0x16, 0x14, 0x1e, 0x04, 0x02, 0x1c, 0x12, 0x1a};
  byte[][] vals = {"Test2".getBytes(), "Caf\u00e9".getBytes(java.nio.charset.StandardCharsets.UTF_8), "test.com".getBytes(), new byte[]{'C', 'e', 's', 't', ' ', (byte) 0xe9, (byte) 0xe2}, "ABCD".getBytes(java.nio.charset.StandardCharsets.UTF_16BE), new byte[]{0x41, 0x42}, new byte[]{0x05}, "AB".getBytes(java.nio.charset.Charset.forName("UTF-32BE")), "123".getBytes(), "vis".getBytes()};
  for (int i = 0; i < tags.length; i++) {
    byte[] d = name(0x55, 0x04, 0x03, tags[i], vals[i]);
    try { X500Principal p = new X500Principal(d); System.out.println("t" + Integer.toHexString(tags[i]) + " -> " + p.getName() + " | " + p + " | " + p.getName(X500Principal.CANONICAL) + " | " + p.getName(X500Principal.RFC1779) + " | " + HexFormat.of().formatHex(p.getEncoded())); } catch (Exception e) { System.out.println("t" + Integer.toHexString(tags[i]) + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
  }
  for (byte[] bad : new byte[][]{new byte[]{0x0a}, new byte[]{0x30, 0x05, 0x31}, new byte[]{0x30, 0x02, 0x31, 0x00}, new byte[]{0x31, 0x00}, new byte[]{0x30, 0x00}, new byte[]{0x30, 0x04, 0x31, 0x02, 0x30, 0x00}, new byte[]{0x30, 0x00, 0x00}}) {
    try { X500Principal p = new X500Principal(bad); System.out.println(HexFormat.of().formatHex(bad) + " -> ok [" + p.getName() + "]"); } catch (Exception e) { System.out.println(HexFormat.of().formatHex(bad) + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
  }
  X500Principal rt = new X500Principal(new java.io.ByteArrayInputStream(new X500Principal("CN=Stream, O=Test").getEncoded()));
  System.out.println(rt.getName());
  java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(); new java.io.ObjectOutputStream(bo).writeObject(new X500Principal("CN=Ser, C=US"));
  System.out.println(((X500Principal) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bo.toByteArray())).readObject()).getName());
  Map<String,String> kw = new HashMap<>(); kw.put("FOO", "1.2.3.4");
  System.out.println(new X500Principal("FOO=bar, CN=x", kw).getName(X500Principal.RFC1779, Map.of("1.2.3.4", "FOO")) + " | " + new X500Principal("FOO=bar, CN=x", kw).getName(X500Principal.RFC2253, Map.of("1.2.3.4", "FOO")) + " | " + new X500Principal("FOO=bar, CN=x", kw));
  try { new X500Principal((String) null); } catch (Exception e) { System.out.println(e.getClass().getName()); }
  try { new X500Principal(new byte[0]); } catch (Exception e) { System.out.println(e); }
  try { new X500Principal("CN=x").getName("foo"); } catch (Exception e) { System.out.println(e); }
}}
