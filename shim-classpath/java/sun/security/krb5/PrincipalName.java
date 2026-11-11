/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package sun.security.krb5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class PrincipalName implements Cloneable {
  public static final int KRB_NT_UNKNOWN = 0;
  public static final int KRB_NT_PRINCIPAL = 1;
  public static final int KRB_NT_SRV_INST = 2;
  public static final int KRB_NT_SRV_HST = 3;
  public static final int KRB_NT_SRV_XHST = 4;
  public static final int KRB_NT_UID = 5;
  public static final int KRB_NT_ENTERPRISE = 10;

  private final int type;
  private final String[] names;
  private String realm;

  public PrincipalName(int type, String[] names, String realm) {
    this.type = type;
    this.names = names.clone();
    this.realm = realm;
  }

  public PrincipalName(String name, int type) throws KrbException {
    this(name, type, null);
  }

  public PrincipalName(String name, int type, String realmDefault) throws KrbException {
    if (name == null) {
      throw new IllegalArgumentException("Principal name cannot be null");
    }
    this.type = type;
    List<String> parts = new ArrayList<String>();
    StringBuilder cur = new StringBuilder();
    String r = null;
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c == '\\' && i + 1 < name.length()) {
        char n = name.charAt(++i);
        cur.append(n == 'n' ? '\n' : n == 't' ? '\t' : n == 'b' ? '\b' : n == '0' ? '\0' : n);
      } else if (c == '/') {
        parts.add(cur.toString());
        cur.setLength(0);
      } else if (c == '@') {
        parts.add(cur.toString());
        cur.setLength(0);
        r = name.substring(i + 1);
        break;
      } else {
        cur.append(c);
      }
    }
    if (r == null) {
      parts.add(cur.toString());
    }
    this.names = parts.toArray(new String[0]);
    this.realm = r != null && !r.isEmpty() ? r : realmDefault;
  }

  public int getNameType() {
    return type;
  }

  public String[] getNameStrings() {
    return names.clone();
  }

  public String getRealmString() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public String getName() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < names.length; i++) {
      if (i > 0) {
        sb.append('/');
      }
      sb.append(escape(names[i]));
    }
    return sb.toString();
  }

  public String getNameString() {
    return getName();
  }

  private static String escape(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '/' || c == '@' || c == '\\') {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb.toString();
  }

  public String toString() {
    return realm == null ? getName() : getName() + "@" + realm;
  }

  public boolean isKrbtgt() {
    return names.length == 2 && names[0].equals("krbtgt");
  }

  public boolean equals(Object o) {
    if (!(o instanceof PrincipalName)) {
      return false;
    }
    PrincipalName p = (PrincipalName) o;
    return java.util.Arrays.equals(names, p.names) && (realm == null ? p.realm == null : realm.equals(p.realm));
  }

  public int hashCode() {
    return java.util.Arrays.hashCode(names) * 31 + (realm == null ? 0 : realm.hashCode());
  }

  public byte[] encode() {
    byte[][] parts = new byte[names.length][];
    for (int i = 0; i < parts.length; i++) {
      parts[i] = Asn1.general(names[i]);
    }
    return Asn1.seq(Asn1.ctx(0, Asn1.integer(type)), Asn1.ctx(1, Asn1.seq(parts)));
  }

  public static PrincipalName decode(Asn1.Node n, String realm) throws IOException {
    int t = n.need(0).asInt();
    List<Asn1.Node> l = n.need(1).children();
    String[] s = new String[l.size()];
    for (int i = 0; i < s.length; i++) {
      s[i] = l.get(i).asString();
    }
    return new PrincipalName(t, s, realm);
  }
}
