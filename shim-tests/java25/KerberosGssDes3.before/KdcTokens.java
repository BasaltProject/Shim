/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

import java.io.*;
import java.util.*;

final class KdcTokens {
  static String hex(byte[] b, int off, int len) {
    StringBuilder s = new StringBuilder();
    for (int i = off; i < off + len && i < b.length; i++) s.append(String.format("%02x", b[i]));
    return s.toString();
  }
  static int[] hdr(byte[] t) {
    // returns {offset after OID+tokid, tokid}
    int p = 1;
    int l = t[p++] & 0xff;
    if (l >= 0x80) p += l & 0x7f;
    p++; int ol = t[p++] & 0xff;
    p += ol;
    return new int[] {p + 2, ((t[p] & 0xff) << 8) | (t[p + 1] & 0xff)};
  }
  static String describeInit(byte[] token, Map<Integer, byte[]> svcKeys) throws Exception {
    int[] h = hdr(token);
    StringBuilder sb = new StringBuilder();
    sb.append("tokid=").append(Integer.toHexString(h[1])).append(" oid=").append(hex(token, hdr0(token), 11));
    KdcAsn1.Node ap = KdcAsn1.read(token, h[0], token.length).inner();
    sb.append(" apOptions=").append(Integer.toHexString(ap.need(2).asBits32()));
    KdcAsn1.Node tkt = ap.need(3).inner();
    KdcAsn1.Node te = tkt.need(3);
    int tet = te.need(0).asInt();
    sb.append(" tktEtype=").append(tet).append(" kvno=").append(te.field(1) == null ? -1 : te.field(1).asInt());
    sb.append(" tktSname=").append(MiniKdc.princName(tkt.need(2)));
    byte[] encTkt = KdcCrypto.decrypt(tet, svcKeys.get(tet), 2, te.need(2).content());
    KdcAsn1.Node et = KdcAsn1.parse(encTkt).inner();
    sb.append(" tktFlags=").append(Integer.toHexString(et.need(0).asBits32()));
    byte[] skey = et.need(1).need(1).content();
    int stype = et.need(1).need(0).asInt();
    sb.append(" sessType=").append(stype).append(" ad=").append(et.field(10) != null);
    KdcAsn1.Node ae = ap.need(4);
    byte[] ab = KdcCrypto.decrypt(ae.need(0).asInt(), skey, 11, ae.need(2).content());
    KdcAsn1.Node au = KdcAsn1.parse(ab).inner();
    sb.append(" authEtype=").append(ae.need(0).asInt());
    sb.append(" crealm=").append(au.need(1).asString()).append(" cname=").append(MiniKdc.princName(au.need(2)));
    KdcAsn1.Node ck = au.field(3);
    if (ck != null) {
      sb.append(" cksumType=").append(ck.need(0).asInt());
      byte[] cv = ck.need(1).content();
      sb.append(" cksumLen=").append(cv.length);
      if (cv.length >= 24) {
        int bnd = (cv[0] & 0xff) | ((cv[1] & 0xff) << 8) | ((cv[2] & 0xff) << 16) | ((cv[3] & 0xff) << 24);
        int flags = (cv[20] & 0xff) | ((cv[21] & 0xff) << 8) | ((cv[22] & 0xff) << 16) | ((cv[23] & 0xff) << 24);
        sb.append(" bndLen=").append(bnd).append(" gssFlags=").append(Integer.toHexString(flags));
        sb.append(" bnd=").append(hex(cv, 4, 16));
        if (cv.length > 24) {
          sb.append(" dlgOpt=").append(((cv[24] & 0xff) | ((cv[25] & 0xff) << 8))).append(" dlgLen=").append(((cv[26] & 0xff) | ((cv[27] & 0xff) << 8)));
        }
      }
    }
    sb.append(" cusec=").append(au.field(4) != null);
    KdcAsn1.Node sk = au.field(6);
    sb.append(" subkey=").append(sk == null ? "none" : sk.need(0).asInt());
    sb.append(" seq=").append(au.field(7) != null).append(" authzd=").append(au.field(8) != null);
    return sb.toString();
  }
  static int hdr0(byte[] t) { int p = 1; int l = t[p++] & 0xff; if (l >= 0x80) p += l & 0x7f; return p; }
  static byte[] sessionKey(byte[] token, Map<Integer, byte[]> svcKeys, int[] etypeOut, byte[][] subkeyOut) throws Exception {
    int[] h = hdr(token);
    KdcAsn1.Node ap = KdcAsn1.read(token, h[0], token.length).inner();
    KdcAsn1.Node te = ap.need(3).inner().need(3);
    int tet = te.need(0).asInt();
    byte[] encTkt = KdcCrypto.decrypt(tet, svcKeys.get(tet), 2, te.need(2).content());
    KdcAsn1.Node et = KdcAsn1.parse(encTkt).inner();
    etypeOut[0] = et.need(1).need(0).asInt();
    byte[] skey = et.need(1).need(1).content();
    KdcAsn1.Node ae = ap.need(4);
    byte[] ab = KdcCrypto.decrypt(ae.need(0).asInt(), skey, 11, ae.need(2).content());
    KdcAsn1.Node au = KdcAsn1.parse(ab).inner();
    KdcAsn1.Node sk = au.field(6);
    if (sk != null) { subkeyOut[0] = sk.need(1).content(); etypeOut[1] = sk.need(0).asInt(); }
    return skey;
  }
  static String describeRep(byte[] token, byte[] skey, int etype) throws Exception {
    int[] h = hdr(token);
    StringBuilder sb = new StringBuilder("tokid=" + Integer.toHexString(h[1]));
    KdcAsn1.Node rep = KdcAsn1.read(token, h[0], token.length).inner();
    KdcAsn1.Node ep = rep.need(2);
    byte[] p = KdcCrypto.decrypt(ep.need(0).asInt(), skey, 12, ep.need(2).content());
    KdcAsn1.Node e = KdcAsn1.parse(p).inner();
    sb.append(" repEtype=").append(ep.need(0).asInt()).append(" subkey=").append(e.field(2) == null ? "none" : e.field(2).need(0).asInt()).append(" seq=").append(e.field(3) != null);
    return sb.toString();
  }
}
