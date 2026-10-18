/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import javax.security.auth.PrivateCredentialPermission;
import java.security.*;
import java.util.*;
public class PrivateCredentialPermissionModern {
  static String ps(PrivateCredentialPermission p) { StringBuilder sb = new StringBuilder(); for (String[] x : p.getPrincipals()) sb.append(Arrays.toString(x)); return p.getCredentialClass() + "|" + p.getActions() + "|" + sb + "|" + p.getName() + "|" + p.hashCode(); }
  public static void main(String[] a) {
    Locale.setDefault(Locale.ENGLISH);
    String[] names = {"a.B \"pc\" \"pn\"", "a.B * \"*\"", "a.B a.P \"x\" b.Q \"y\"", "*", "* * \"*\"", "a.B", "a.B a.P", "a.B a.P x", "a.B a.P \"x", "a.B * \"x\"", "", " ", "a.B a.P \"x\" b.Q", "a.B  a.P  \"x\"", "a.B a.P \"x y\"", "a.B a.P \"\"", "a.B * \"*\"  ", "javax.security.auth.Subject$SecureSet * \"*\"", "a.B \"x\" \"y\"", "a.B a.P \"x\" \"y\""};
    for (String n : names) {
      try { PrivateCredentialPermission p = new PrivateCredentialPermission(n, "read"); System.out.println("[" + n + "] " + ps(p)); } catch (Throwable e) { System.out.println("[" + n + "] !" + e.getClass().getName() + ":" + e.getMessage()); }
    }
    for (String act : new String[] {"read", "READ", "write", "", null, "read,write"}) { try { System.out.println(act + " " + new PrivateCredentialPermission("a.B * \"*\"", act).getActions()); } catch (Throwable e) { System.out.println(act + " !" + e.getClass().getName() + ":" + e.getMessage()); } }
    try { new PrivateCredentialPermission(null, "read"); } catch (Throwable e) { System.out.println("null !" + e.getClass().getName() + ":" + e.getMessage()); }
    String[][] imp = {{"a.B * \"*\"", "a.B a.P \"x\""}, {"a.B a.P \"x\"", "a.B * \"*\""}, {"a.B a.P \"*\"", "a.B a.P \"x\""}, {"a.B a.P \"x\"", "a.B a.P \"x\""}, {"* * \"*\"", "a.B a.P \"x\""}, {"a.B a.P \"x\"", "a.C a.P \"x\""}, {"a.B a.P \"x\" b.Q \"y\"", "a.B a.P \"x\""}, {"a.B a.P \"x\"", "a.B a.P \"x\" b.Q \"y\""}, {"a.B a.P \"x\" b.Q \"y\"", "a.B b.Q \"y\" a.P \"x\""}, {"a.B * \"*\"", "a.B * \"*\""}, {"a.B a.P \"x\"", "a.B a.P \"y\""}, {"a.B * \"*\"", "a.B b.Q \"y\" a.P \"x\""}};
    for (String[] pair : imp) { PrivateCredentialPermission p = new PrivateCredentialPermission(pair[0], "read"), q = new PrivateCredentialPermission(pair[1], "read"); System.out.println(pair[0] + " implies " + pair[1] + " = " + p.implies(q) + " eq=" + p.equals(q) + " " + (p.hashCode() == q.hashCode())); }
    PrivateCredentialPermission p = new PrivateCredentialPermission("a.B a.P \"x\"", "read");
    System.out.println(p.implies(new RuntimePermission("x")) + " " + p.implies(null) + " " + p.equals("s") + " " + p.equals(null) + " " + p);
    PermissionCollection pc = p.newPermissionCollection(); System.out.println(pc);
    System.out.println(new PrivateCredentialPermission("a.B a.P \"x\"", "read").hashCode());
  }
}
