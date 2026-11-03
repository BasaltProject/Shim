/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.beans;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MiniXml {
  public static final class Node {
    public final String name;
    public final Map<String, String> attrs = new LinkedHashMap<String, String>();
    public final List<Node> children = new ArrayList<Node>();
    public final StringBuilder text = new StringBuilder();

    Node(String name) { this.name = name; }
  }

  private final String src;
  private int pos;

  private MiniXml(String src) { this.src = src; }

  public static Node parse(InputStream in) throws IOException {
    Reader r = new InputStreamReader(in, "UTF-8");
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[4096];
    int n;
    while ((n = r.read(buf)) > 0) {
      sb.append(buf, 0, n);
    }
    return new MiniXml(sb.toString()).parseDocument();
  }

  private Node parseDocument() throws IOException {
    skipMisc();
    if (pos >= src.length() || src.charAt(pos) != '<') {
      throw new IOException("no root element");
    }
    return parseElement();
  }

  private void skipMisc() {
    while (true) {
      while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
        pos++;
      }
      if (src.startsWith("<?", pos)) {
        int e = src.indexOf("?>", pos);
        pos = e < 0 ? src.length() : e + 2;
      } else if (src.startsWith("<!--", pos)) {
        int e = src.indexOf("-->", pos);
        pos = e < 0 ? src.length() : e + 3;
      } else if (src.startsWith("<!", pos)) {
        int e = src.indexOf('>', pos);
        pos = e < 0 ? src.length() : e + 1;
      } else {
        return;
      }
    }
  }

  private Node parseElement() throws IOException {
    pos++;
    int start = pos;
    while (pos < src.length() && !Character.isWhitespace(src.charAt(pos)) && src.charAt(pos) != '>'
        && src.charAt(pos) != '/') {
      pos++;
    }
    Node node = new Node(src.substring(start, pos));
    while (true) {
      while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
        pos++;
      }
      if (pos >= src.length()) {
        throw new IOException("unexpected end");
      }
      char c = src.charAt(pos);
      if (c == '/') {
        pos += 2;
        return node;
      }
      if (c == '>') {
        pos++;
        break;
      }
      int ns = pos;
      while (pos < src.length() && src.charAt(pos) != '=' && !Character.isWhitespace(src.charAt(pos))) {
        pos++;
      }
      String an = src.substring(ns, pos);
      while (pos < src.length() && (Character.isWhitespace(src.charAt(pos)) || src.charAt(pos) == '=')) {
        pos++;
      }
      char q = src.charAt(pos++);
      int vs = pos;
      while (src.charAt(pos) != q) {
        pos++;
      }
      node.attrs.put(an, unescape(src.substring(vs, pos)));
      pos++;
    }
    while (pos < src.length()) {
      if (src.startsWith("</", pos)) {
        int e = src.indexOf('>', pos);
        pos = e + 1;
        return node;
      } else if (src.startsWith("<!--", pos)) {
        int e = src.indexOf("-->", pos);
        pos = e + 3;
      } else if (src.startsWith("<![CDATA[", pos)) {
        int e = src.indexOf("]]>", pos);
        node.text.append(src, pos + 9, e);
        pos = e + 3;
      } else if (src.charAt(pos) == '<') {
        node.children.add(parseElement());
      } else {
        int e = src.indexOf('<', pos);
        if (e < 0) {
          e = src.length();
        }
        node.text.append(unescape(src.substring(pos, e)));
        pos = e;
      }
    }
    return node;
  }

  static String unescape(String s) {
    if (s.indexOf('&') < 0) {
      return s;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '&') {
        int e = s.indexOf(';', i);
        String ent = s.substring(i + 1, e);
        if (ent.equals("amp")) {
          sb.append('&');
        } else if (ent.equals("lt")) {
          sb.append('<');
        } else if (ent.equals("gt")) {
          sb.append('>');
        } else if (ent.equals("quot")) {
          sb.append('"');
        } else if (ent.equals("apos")) {
          sb.append('\'');
        } else if (ent.startsWith("#x")) {
          sb.appendCodePoint(Integer.parseInt(ent.substring(2), 16));
        } else if (ent.startsWith("#")) {
          sb.appendCodePoint(Integer.parseInt(ent.substring(1)));
        }
        i = e;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
