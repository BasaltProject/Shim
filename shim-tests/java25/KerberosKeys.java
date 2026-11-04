/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import javax.security.auth.kerberos.*; import java.util.*;
public class KerberosKeys {
  static String hex(byte[] b) { StringBuilder s = new StringBuilder(); for (byte x : b) s.append(String.format("%02x", x)); return s.toString(); }
  public static void main(String[] a) throws Exception {
    KerberosPrincipal p = new KerberosPrincipal("user@EXAMPLE.COM");
    System.out.println(p + " " + p.getRealm() + " " + p.getNameType() + " " + p.equals(new KerberosPrincipal("user@EXAMPLE.COM")) + " " + p.hashCode());
    try { new KerberosPrincipal("a@b@c"); } catch (Exception e) { System.out.println(e); }
    try { new KerberosPrincipal("a@"); } catch (Exception e) { System.out.println(e); }
    try { new KerberosPrincipal("x", 99); } catch (Exception e) { System.out.println(e); }
    for (String alg : new String[]{"ArcFourHmac", "AES128", "AES256"}) { KerberosKey k = new KerberosKey(p, "secret".toCharArray(), alg); System.out.println(alg + " " + k.getAlgorithm() + " " + k.getKeyType() + " " + hex(k.getEncoded()) + " " + k.getVersionNumber() + " " + k.getFormat()); }
    try { new KerberosKey(p, "secret".toCharArray(), "bogus"); } catch (Exception e) { System.out.println(e); }
    KerberosKey k2 = new KerberosKey(p, new byte[]{1,2,3,4,5,6,7,8}, 3, 7); System.out.println(k2.getAlgorithm() + " " + k2.getVersionNumber() + " " + k2.equals(new KerberosKey(p, new byte[]{1,2,3,4,5,6,7,8}, 3, 7)));
    k2.destroy(); System.out.println(k2.isDestroyed()); try { k2.getEncoded(); } catch (Exception e) { System.out.println(e); }
    Date now = new Date(1000000), end = new Date(9000000);
    KerberosTicket t = new KerberosTicket(new byte[]{1,2,3}, p, new KerberosPrincipal("krbtgt/EXAMPLE.COM@EXAMPLE.COM"), new byte[]{9,8,7,6,5,4,3,2}, 17, new boolean[]{false,true,false,true,false,false,false,false,true,true}, now, now, end, end, null);
    System.out.println(t.isForwardable() + " " + t.isForwarded() + " " + t.isProxiable() + " " + t.isRenewable() + " " + t.isInitial() + " " + t.getSessionKeyType() + " " + t.getSessionKey().getAlgorithm() + " " + t.getEndTime().getTime() + " " + t.getFlags().length + " " + t.isCurrent() + " " + t.equals(t));
    try { t.refresh(); } catch (Exception e) { System.out.println(e.getClass().getName()); }
    try { new KerberosTicket(new byte[]{1}, p, p, new byte[]{1}, 17, new boolean[]{false,false,false,false,false,false,false,false,true}, now, now, end, null, null); } catch (Exception e) { System.out.println(e); }
    KerberosCredMessage m = new KerberosCredMessage(p, p, new byte[]{1,2}); System.out.println(m.getSender() + " " + m.getEncoded().length);
  }
}
