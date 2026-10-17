/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.x509;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class X500Name implements Principal {
  public static final class AVA {
    final String oid;
    final DerValue value;

    AVA(String oid, DerValue value) {
      this.oid = oid;
      this.value = value;
    }

    public String getOid() {
      return oid;
    }

    public DerValue getValue() {
      return value;
    }
  }

  private static final String[][] kKeywords = {
    {"CN", "2.5.4.3"}, {"C", "2.5.4.6"}, {"L", "2.5.4.7"}, {"S", "2.5.4.8"}, {"ST", "2.5.4.8"}, {"O", "2.5.4.10"},
    {"OU", "2.5.4.11"}, {"T", "2.5.4.12"}, {"TITLE", "2.5.4.12"}, {"STREET", "2.5.4.9"},
    {"DC", "0.9.2342.19200300.100.1.25"}, {"UID", "0.9.2342.19200300.100.1.1"},
    {"EMAILADDRESS", "1.2.840.113549.1.9.1"}, {"SERIALNUMBER", "2.5.4.5"}, {"SURNAME", "2.5.4.4"},
    {"GIVENNAME", "2.5.4.42"}, {"INITIALS", "2.5.4.43"}, {"GENERATION", "2.5.4.44"}, {"DNQ", "2.5.4.46"},
    {"DNQUALIFIER", "2.5.4.46"}, {"UNSTRUCTUREDNAME", "1.2.840.113549.1.9.2"},
    {"UNSTRUCTUREDADDRESS", "1.2.840.113549.1.9.8"}, {"PSEUDONYM", "2.5.4.65"}, {"IP", "1.3.6.1.4.1.42.2.11.2.1"},
  };

  private static final String[][] kToStringNames = {
    {"2.5.4.3", "CN"}, {"2.5.4.6", "C"}, {"2.5.4.7", "L"}, {"2.5.4.8", "ST"}, {"2.5.4.10", "O"}, {"2.5.4.11", "OU"},
    {"2.5.4.12", "T"}, {"2.5.4.9", "STREET"}, {"0.9.2342.19200300.100.1.25", "DC"},
    {"0.9.2342.19200300.100.1.1", "UID"}, {"1.2.840.113549.1.9.1", "EMAILADDRESS"}, {"2.5.4.5", "SERIALNUMBER"},
    {"2.5.4.4", "SURNAME"}, {"2.5.4.42", "GIVENNAME"}, {"2.5.4.43", "INITIALS"}, {"2.5.4.44", "GENERATION"},
    {"2.5.4.46", "DNQ"}, {"1.2.840.113549.1.9.2", "UNSTRUCTUREDNAME"}, {"1.2.840.113549.1.9.8", "UNSTRUCTUREDADDRESS"},
    {"2.5.4.65", "PSEUDONYM"}, {"1.3.6.1.4.1.42.2.11.2.1", "IP"},
  };

  private static final String[][] kRfc2253Names = {
    {"2.5.4.3", "CN"}, {"2.5.4.6", "C"}, {"2.5.4.7", "L"}, {"2.5.4.8", "ST"}, {"2.5.4.10", "O"}, {"2.5.4.11", "OU"},
    {"2.5.4.9", "STREET"}, {"0.9.2342.19200300.100.1.25", "DC"}, {"0.9.2342.19200300.100.1.1", "UID"},
  };

  private static final String[][] kRfc1779Names = {
    {"2.5.4.3", "CN"}, {"2.5.4.6", "C"}, {"2.5.4.7", "L"}, {"2.5.4.8", "ST"}, {"2.5.4.10", "O"}, {"2.5.4.11", "OU"},
    {"2.5.4.9", "STREET"},
  };

  private final List<List<AVA>> rdns;
  private byte[] encoded;

  private X500Name(List<List<AVA>> rdns) {
    this.rdns = rdns;
  }

  public static X500Name parse(String name, Map<String, String> keywordMap) {
    if (name == null) {
      throw new NullPointerException("provided null name");
    }
    try {
      return new X500Name(parseDN(name, keywordMap));
    } catch (IOException e) {
      throw new IllegalArgumentException("improperly specified input name: " + name, e);
    }
  }

  public static X500Name decode(byte[] der) {
    try {
      List<List<AVA>> rdns = new ArrayList<List<AVA>>();
      if (der.length == 0) {
        return new X500Name(rdns);
      }
      DerInputStream top = new DerInputStream(der);
      DerValue seq = top.getDerValue();
      if (seq.getTag() != DerValue.tag_Sequence && seq.getTag() != DerValue.tag_Set) {
        throw new IOException("not a sequence");
      }
      DerInputStream in = seq.data();
      while (in.available() > 0) {
        DerValue set = in.getDerValue();
        if (set.getTag() != DerValue.tag_Set) {
          throw new IOException("not a set");
        }
        List<AVA> avas = new ArrayList<AVA>();
        DerInputStream si = set.data();
        while (si.available() > 0) {
          DerValue ava = si.getDerValue();
          DerInputStream ai = ava.data();
          String oid = ai.getOID().toString();
          DerValue value = ai.getDerValue();
          avas.add(new AVA(oid, value));
        }
        if (!avas.isEmpty()) {
          rdns.add(avas);
        }
      }
      X500Name n = new X500Name(rdns);
      if (top.available() == 0 && seq.getTag() == DerValue.tag_Sequence) {
        n.encoded = der.clone();
      }
      return n;
    } catch (IOException e) {
      throw new IllegalArgumentException("improperly specified input name", e);
    }
  }

  private static String keywordOid(String keyword, Map<String, String> keywordMap) throws IOException {
    String k = keyword.trim();
    if (keywordMap != null) {
      for (Map.Entry<String, String> e : keywordMap.entrySet()) {
        if (e.getKey().equalsIgnoreCase(k)) {
          return new ObjectIdentifier(e.getValue()).toString();
        }
      }
    }
    String upper = k.toUpperCase(Locale.ENGLISH);
    for (String[] kw : kKeywords) {
      if (kw[0].equals(upper)) {
        return kw[1];
      }
    }
    String oid = upper.startsWith("OID.") ? k.substring(4) : k;
    if (!oid.isEmpty() && Character.isDigit(oid.charAt(0))) {
      return new ObjectIdentifier(oid).toString();
    }
    throw new IOException("Invalid keyword \"" + keyword + "\"");
  }

  private static List<List<AVA>> parseDN(String name, Map<String, String> keywordMap) throws IOException {
    List<List<AVA>> result = new ArrayList<List<AVA>>();
    if (name.isEmpty()) {
      return result;
    }
    List<String> rdnTexts = splitUnescaped(name, ",;");
    for (String rdnText : rdnTexts) {
      List<AVA> avas = new ArrayList<AVA>();
      for (String avaText : splitUnescaped(rdnText, "+")) {
        avas.add(parseAva(avaText, keywordMap));
      }
      result.add(0, avas);
    }
    return result;
  }

  private static List<String> splitUnescaped(String s, String seps) throws IOException {
    List<String> parts = new ArrayList<String>();
    StringBuilder cur = new StringBuilder();
    boolean quoted = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' && i + 1 < s.length()) {
        cur.append(c).append(s.charAt(++i));
        continue;
      }
      if (c == '"') {
        quoted = !quoted;
      }
      if (!quoted && seps.indexOf(c) >= 0) {
        parts.add(cur.toString());
        cur.setLength(0);
        continue;
      }
      cur.append(c);
    }
    if (quoted) {
      throw new IOException("Quote mismatch");
    }
    parts.add(cur.toString());
    return parts;
  }

  private static boolean isPrintable(String v) {
    for (int i = 0; i < v.length(); i++) {
      char c = v.charAt(i);
      boolean ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == ' ' || c == '\''
          || c == '(' || c == ')' || c == '+' || c == ',' || c == '-' || c == '.' || c == '/' || c == ':' || c == '='
          || c == '?';
      if (!ok) {
        return false;
      }
    }
    return true;
  }

  private static AVA parseAva(String text, Map<String, String> keywordMap) throws IOException {
    int eq = -1;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == '\\') {
        i++;
        continue;
      }
      if (c == '=') {
        eq = i;
        break;
      }
    }
    if (eq < 0) {
      throw new IOException("Invalid AVA, no '='");
    }
    String keyword = text.substring(0, eq);
    String oid = keywordOid(keyword, keywordMap);
    String valueText = text.substring(eq + 1);
    int start = 0;
    while (start < valueText.length() && valueText.charAt(start) == ' ') {
      start++;
    }
    if (start < valueText.length() && valueText.charAt(start) == '#') {
      String hex = valueText.substring(start + 1).trim();
      if (hex.isEmpty() || hex.length() % 2 != 0) {
        throw new IOException("Invalid hex value");
      }
      byte[] der = new byte[hex.length() / 2];
      for (int i = 0; i < der.length; i++) {
        int hi = Character.digit(hex.charAt(2 * i), 16);
        int lo = Character.digit(hex.charAt(2 * i + 1), 16);
        if (hi < 0 || lo < 0) {
          throw new IOException("Invalid hex digit");
        }
        der[i] = (byte) ((hi << 4) | lo);
      }
      return new AVA(oid, new DerValue(der));
    }
    String value = unescapeValue(valueText);
    boolean ia5 = oid.equals("0.9.2342.19200300.100.1.25") || oid.equals("1.2.840.113549.1.9.1");
    byte tag;
    if (ia5) {
      tag = DerValue.tag_IA5String;
    } else if (isPrintable(value)) {
      tag = DerValue.tag_PrintableString;
    } else {
      tag = DerValue.tag_UTF8String;
    }
    byte[] data = tag == DerValue.tag_UTF8String ? value.getBytes(StandardCharsets.UTF_8)
        : value.getBytes(StandardCharsets.ISO_8859_1);
    return new AVA(oid, new DerValue(tag, data));
  }

  private static String unescapeValue(String raw) throws IOException {
    int start = 0;
    int end = raw.length();
    boolean trailingEscapedSpace = false;
    while (start < end && raw.charAt(start) == ' ') {
      start++;
    }
    while (end > start && raw.charAt(end - 1) == ' ') {
      if (end - 2 >= start && raw.charAt(end - 2) == '\\') {
        trailingEscapedSpace = true;
        break;
      }
      end--;
    }
    String s = raw.substring(start, end);
    if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
      s = s.substring(1, s.length() - 1);
      return unescapeBody(s, true);
    }
    if (s.indexOf('"') >= 0 && s.indexOf('\\') < 0) {
      throw new IOException("Invalid quote");
    }
    return unescapeBody(s, false);
  }

  private static String unescapeBody(String s, boolean quoted) throws IOException {
    StringBuilder sb = new StringBuilder();
    java.io.ByteArrayOutputStream pending = new java.io.ByteArrayOutputStream();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\') {
        if (i + 1 >= s.length()) {
          throw new IOException("Invalid escape");
        }
        char n = s.charAt(i + 1);
        if (i + 2 < s.length() && Character.digit(n, 16) >= 0 && Character.digit(s.charAt(i + 2), 16) >= 0) {
          pending.write((Character.digit(n, 16) << 4) | Character.digit(s.charAt(i + 2), 16));
          i += 2;
          continue;
        }
        flush(pending, sb);
        sb.append(n);
        i++;
        continue;
      }
      flush(pending, sb);
      if (!quoted && (",+<>;".indexOf(c) >= 0 || c == '"')) {
        throw new IOException("Invalid unescaped character: " + c);
      }
      sb.append(c);
    }
    flush(pending, sb);
    return sb.toString();
  }

  private static void flush(java.io.ByteArrayOutputStream pending, StringBuilder sb) {
    if (pending.size() > 0) {
      sb.append(new String(pending.toByteArray(), StandardCharsets.UTF_8));
      pending.reset();
    }
  }

  public synchronized byte[] getEncoded() {
    if (encoded == null) {
      DerOutputStream out = new DerOutputStream();
      DerOutputStream body = new DerOutputStream();
      for (int i = 0; i < rdns.size(); i++) {
        List<byte[]> avaEncs = new ArrayList<byte[]>();
        for (AVA ava : rdns.get(i)) {
          DerOutputStream a = new DerOutputStream();
          try {
            a.putOID(new ObjectIdentifier(ava.oid));
            a.putDerValue(ava.value);
          } catch (IOException e) {
            throw new IllegalArgumentException(e);
          }
          DerOutputStream seq = new DerOutputStream();
          seq.write(DerValue.tag_Sequence, a);
          avaEncs.add(seq.toByteArray());
        }
        Collections.sort(avaEncs, new Comparator<byte[]>() {
          @Override
          public int compare(byte[] x, byte[] y) {
            int n = Math.min(x.length, y.length);
            for (int k = 0; k < n; k++) {
              int d = (x[k] & 0xff) - (y[k] & 0xff);
              if (d != 0) {
                return d;
              }
            }
            return x.length - y.length;
          }
        });
        DerOutputStream set = new DerOutputStream();
        for (byte[] e : avaEncs) {
          set.write(e);
        }
        body.write(DerValue.tag_Set, set);
      }
      out.write(DerValue.tag_Sequence, body);
      encoded = out.toByteArray();
    }
    return encoded.clone();
  }

  private static String stringValue(DerValue v, int style) {
    byte[] d = v.getDataBytes();
    byte tag = v.getTag();
    switch (tag) {
    case DerValue.tag_PrintableString:
      return new String(d, StandardCharsets.ISO_8859_1);
    case DerValue.tag_UTF8String:
      return new String(d, StandardCharsets.UTF_8);
    case DerValue.tag_IA5String:
      return style == 2 ? null : new String(d, StandardCharsets.ISO_8859_1);
    case DerValue.tag_T61String:
      return style == 2 ? null : new String(d, StandardCharsets.ISO_8859_1);
    case 0x1e:
      return style == 2 ? null : new String(d, StandardCharsets.UTF_16BE);
    case 0x1c:
      return style == 1 || style == 3 ? new String(d, java.nio.charset.Charset.forName("UTF-32BE")) : null;
    default:
      return null;
    }
  }

  private static String hexValue(DerValue v) {
    byte[] der;
    try {
      der = v.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    StringBuilder sb = new StringBuilder("#");
    for (byte b : der) {
      sb.append(Character.forDigit((b >> 4) & 0xf, 16)).append(Character.forDigit(b & 0xf, 16));
    }
    return sb.toString();
  }

  private static String lookup(String[][] table, String oid, Map<String, String> oidMap) {
    if (oidMap != null) {
      String m = oidMap.get(oid);
      if (m != null) {
        return m;
      }
    }
    for (String[] e : table) {
      if (e[0].equals(oid)) {
        return e[1];
      }
    }
    return null;
  }

  private static String escape2253(String v, boolean canonicalStyle) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < v.length(); i++) {
      char c = v.charAt(i);
      boolean special = c == ',' || c == '+' || c == '"' || c == '\\' || c == '<' || c == '>' || c == ';'
          || (!canonicalStyle && c == '=');
      if (special || (i == 0 && (c == '#' || c == ' ')) || (i == v.length() - 1 && c == ' ')) {
        sb.append('\\').append(c);
      } else if (c < 0x20 || c == 0x7f) {
        sb.append('\\').append(Character.forDigit((c >> 4) & 0xf, 16)).append(Character.forDigit(c & 0xf, 16));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static String quote1779(String v) {
    boolean needQuote = v.isEmpty() ? false : (v.charAt(0) == ' ' || v.charAt(v.length() - 1) == ' ');
    for (int i = 0; i < v.length() && !needQuote; i++) {
      char c = v.charAt(i);
      if (",+=\n<>#;\\\"".indexOf(c) >= 0) {
        needQuote = true;
      }
    }
    if (!needQuote) {
      return v;
    }
    StringBuilder sb = new StringBuilder("\"");
    for (int i = 0; i < v.length(); i++) {
      char c = v.charAt(i);
      if (c == '"' || c == '\\') {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb.append('"').toString();
  }

  private String format(int style, Map<String, String> oidMap) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int i = rdns.size() - 1; i >= 0; i--) {
      if (!first) {
        sb.append(style == 1 || style == 3 ? ", " : ",");
      }
      first = false;
      List<AVA> avas = new ArrayList<AVA>(rdns.get(i));
      if (style == 2) {
        Collections.sort(avas, new Comparator<AVA>() {
          @Override
          public int compare(AVA a, AVA b) {
            return compareOids(a.oid, b.oid);
          }
        });
      }
      boolean firstAva = true;
      for (AVA ava : avas) {
        if (!firstAva) {
          sb.append(style == 1 || style == 3 ? " + " : "+");
        }
        firstAva = false;
        sb.append(formatAva(ava, style, oidMap));
      }
    }
    return sb.toString();
  }

  private static int compareOids(String a, String b) {
    String[] x = a.split("\\.");
    String[] y = b.split("\\.");
    int n = Math.min(x.length, y.length);
    for (int i = 0; i < n; i++) {
      int d = Integer.compare(Integer.parseInt(x[i]), Integer.parseInt(y[i]));
      if (d != 0) {
        return d;
      }
    }
    return x.length - y.length;
  }

  private String formatAva(AVA ava, int style, Map<String, String> oidMap) {
    switch (style) {
    case 0: {
      String kw = lookup(kRfc2253Names, ava.oid, oidMap);
      String sv = stringValue(ava.value, 0);
      if (kw == null) {
        return ava.oid + "=" + hexValue(ava.value);
      }
      return kw + "=" + (sv == null ? hexValue(ava.value) : escape2253(sv, false));
    }
    case 1: {
      String kw = lookup(kRfc1779Names, ava.oid, oidMap);
      String sv = stringValue(ava.value, 1);
      String key = kw != null ? kw : "OID." + ava.oid;
      return key + "=" + (sv == null ? hexValue(ava.value) : quote1779(sv));
    }
    case 2: {
      String kw = lookup(kRfc2253Names, ava.oid, null);
      if (kw == null) {
        return ava.oid + "=" + hexValue(ava.value);
      }
      String sv = stringValue(ava.value, 2);
      if (sv == null) {
        return kw.toLowerCase(Locale.ENGLISH) + "=" + hexValue(ava.value);
      }
      return kw.toLowerCase(Locale.ENGLISH) + "=" + escape2253(normalize(sv), true);
    }
    default: {
      String kw = lookup(kToStringNames, ava.oid, null);
      String sv = stringValue(ava.value, 3);
      String key = kw != null ? kw : "OID." + ava.oid;
      return key + "=" + (sv == null ? hexValue(ava.value) : quote1779(sv));
    }
    }
  }

  private static String normalize(String s) {
    String lower = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKD).toUpperCase(Locale.US).toLowerCase(Locale.US);
    StringBuilder sb = new StringBuilder();
    boolean space = false;
    for (int i = 0; i < lower.length(); i++) {
      char c = lower.charAt(i);
      if (c == ' ') {
        space = true;
        continue;
      }
      if (space && sb.length() > 0) {
        sb.append(' ');
      }
      space = false;
      sb.append(c);
    }
    return sb.toString();
  }

  public String getMostSpecificCommonName() {
    for (int i = rdns.size() - 1; i >= 0; i--) {
      for (AVA ava : rdns.get(i)) {
        if (ava.oid.equals("2.5.4.3")) {
          return stringValue(ava.value, 0);
        }
      }
    }
    return null;
  }

  public String getRFC2253Name(Map<String, String> oidMap) {
    return format(0, oidMap);
  }

  public String getRFC1779Name(Map<String, String> oidMap) {
    return format(1, oidMap);
  }

  public String getCanonicalName() {
    return format(2, null);
  }

  @Override
  public String getName() {
    return toString();
  }

  @Override
  public String toString() {
    return format(3, null);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof X500Name && getCanonicalName().equals(((X500Name) o).getCanonicalName());
  }

  @Override
  public int hashCode() {
    return getCanonicalName().hashCode();
  }
}
