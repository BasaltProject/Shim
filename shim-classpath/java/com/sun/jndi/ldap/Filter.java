/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jndi.ldap;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.naming.directory.InvalidSearchFilterException;

/** RFC 2254 string filter to BER. */
final class Filter {
  private final String s;
  private int p;

  private Filter(String s) {
    this.s = s;
  }

  static byte[] encode(String filter) throws InvalidSearchFilterException {
    Filter f = new Filter(filter.trim());
    byte[] out = f.parse();
    if (f.p != f.s.length()) {
      throw new InvalidSearchFilterException("Unbalanced parenthesis");
    }
    return out;
  }

  static String format(String filter, Object[] args) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < filter.length(); i++) {
      char c = filter.charAt(i);
      int close = c == '{' ? filter.indexOf('}', i) : -1;
      if (close > i + 1 && filter.substring(i + 1, close).chars().allMatch(Character::isDigit)) {
        int idx = Integer.parseInt(filter.substring(i + 1, close));
        if (args != null && idx < args.length) {
          appendValue(sb, args[idx]);
        }
        i = close;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  private static void appendValue(StringBuilder sb, Object v) {
    if (v instanceof byte[]) {
      for (byte b : (byte[]) v) {
        sb.append(String.format("\\%02x", b & 0xff));
      }
      return;
    }
    String t = String.valueOf(v);
    for (int i = 0; i < t.length(); i++) {
      char c = t.charAt(i);
      switch (c) {
        case '*':
          sb.append("\\2a");
          break;
        case '(':
          sb.append("\\28");
          break;
        case ')':
          sb.append("\\29");
          break;
        case '\\':
          sb.append("\\5c");
          break;
        case 0:
          sb.append("\\00");
          break;
        default:
          sb.append(c);
      }
    }
  }

  private byte[] parse() throws InvalidSearchFilterException {
    if (p >= s.length() || s.charAt(p) != '(') {
      throw new InvalidSearchFilterException("Missing 'attributeDescription'");
    }
    p++;
    if (p >= s.length()) {
      throw new InvalidSearchFilterException("Unbalanced parenthesis");
    }
    byte[] out;
    char c = s.charAt(p);
    if (c == '&' || c == '|') {
      p++;
      ByteArrayOutputStream parts = new ByteArrayOutputStream();
      while (p < s.length() && s.charAt(p) == '(') {
        parts.writeBytes(parse());
      }
      out = Ber.tlv(c == '&' ? 0xa0 : 0xa1, parts.toByteArray());
    } else if (c == '!') {
      p++;
      out = Ber.tlv(0xa2, parse());
    } else {
      int close = closeParen();
      out = item(s.substring(p, close));
      p = close;
    }
    if (p >= s.length() || s.charAt(p) != ')') {
      throw new InvalidSearchFilterException("Unbalanced parenthesis");
    }
    p++;
    return out;
  }

  private int closeParen() throws InvalidSearchFilterException {
    int i = p;
    while (i < s.length() && s.charAt(i) != ')') {
      if (s.charAt(i) == '\\') {
        i++;
      }
      i++;
    }
    if (i >= s.length()) {
      throw new InvalidSearchFilterException("Unbalanced parenthesis");
    }
    return i;
  }

  private static byte[] item(String it) throws InvalidSearchFilterException {
    int eq = it.indexOf('=');
    if (eq < 0) {
      throw new InvalidSearchFilterException("Missing 'equals'");
    }
    String attr = it.substring(0, eq);
    String val = it.substring(eq + 1);
    int tag = 0xa3;
    if (attr.endsWith(":")) {
      return extensible(attr.substring(0, attr.length() - 1), val);
    }
    if (attr.endsWith("~")) {
      tag = 0xa8;
      attr = attr.substring(0, attr.length() - 1);
    } else if (attr.endsWith(">")) {
      tag = 0xa5;
      attr = attr.substring(0, attr.length() - 1);
    } else if (attr.endsWith("<")) {
      tag = 0xa6;
      attr = attr.substring(0, attr.length() - 1);
    }
    if (attr.isEmpty()) {
      throw new InvalidSearchFilterException("Missing 'attributeDescription'");
    }
    if (tag == 0xa3) {
      if (val.equals("*")) {
        return Ber.str(0x87, attr);
      }
      if (hasWildcard(val)) {
        return substring(attr, val);
      }
    }
    return Ber.tlv(tag, Ber.str(0x04, attr), Ber.octets(0x04, unescape(val)));
  }

  private static boolean hasWildcard(String v) {
    for (int i = 0; i < v.length(); i++) {
      if (v.charAt(i) == '\\') {
        i++;
      } else if (v.charAt(i) == '*') {
        return true;
      }
    }
    return false;
  }

  private static byte[] substring(String attr, String val) {
    java.util.List<String> parts = new java.util.ArrayList<String>();
    StringBuilder cur = new StringBuilder();
    for (int i = 0; i < val.length(); i++) {
      char c = val.charAt(i);
      if (c == '\\' && i + 2 < val.length() + 1) {
        cur.append(c).append(val.charAt(++i)).append(i + 1 < val.length() ? val.charAt(++i) : "");
      } else if (c == '*') {
        parts.add(cur.toString());
        cur.setLength(0);
      } else {
        cur.append(c);
      }
    }
    parts.add(cur.toString());
    ByteArrayOutputStream subs = new ByteArrayOutputStream();
    for (int i = 0; i < parts.size(); i++) {
      String part = parts.get(i);
      if (part.isEmpty()) {
        continue;
      }
      int tag = i == 0 ? 0x80 : (i == parts.size() - 1 ? 0x82 : 0x81);
      subs.writeBytes(Ber.octets(tag, unescape(part)));
    }
    return Ber.tlv(0xa4, Ber.str(0x04, attr), Ber.tlv(0x30, subs.toByteArray()));
  }

  private static byte[] extensible(String head, String val) {
    String[] bits = head.split(":");
    String type = null;
    String rule = null;
    boolean dn = false;
    for (int i = 0; i < bits.length; i++) {
      if (bits[i].equalsIgnoreCase("dn")) {
        dn = true;
      } else if (i == 0 && !head.startsWith(":")) {
        type = bits[i];
      } else if (!bits[i].isEmpty()) {
        rule = bits[i];
      }
    }
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    if (rule != null) {
      o.writeBytes(Ber.str(0x81, rule));
    }
    if (type != null) {
      o.writeBytes(Ber.str(0x82, type));
    }
    o.writeBytes(Ber.octets(0x83, unescape(val)));
    if (dn) {
      o.writeBytes(Ber.tlv(0x84, new byte[] {(byte) 0xff}));
    }
    return Ber.tlv(0xa9, o.toByteArray());
  }

  static byte[] unescape(String v) {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    byte[] raw = v.getBytes(StandardCharsets.UTF_8);
    for (int i = 0; i < raw.length; i++) {
      if (raw[i] == '\\' && i + 2 < raw.length + 0 && hex(raw[i + 1]) >= 0 && hex(raw[i + 2]) >= 0) {
        o.write(hex(raw[i + 1]) * 16 + hex(raw[i + 2]));
        i += 2;
      } else {
        o.write(raw[i]);
      }
    }
    return o.toByteArray();
  }

  private static int hex(int c) {
    return Character.digit(c, 16);
  }
}
