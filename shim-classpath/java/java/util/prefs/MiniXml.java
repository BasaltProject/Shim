/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.prefs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MiniXml {
  static final class Element {
    final String name;
    final Map<String, String> attributes = new LinkedHashMap<String, String>();
    final List<Element> children = new ArrayList<Element>();

    Element(String name) {
      this.name = name;
    }
  }

  static final class ParseException extends Exception {
    private static final long serialVersionUID = 1L;

    ParseException(String message) {
      super(message);
    }
  }

  private final String text;
  private int pos;

  private MiniXml(String text) {
    this.text = text;
  }

  static Element parse(String text) throws ParseException {
    MiniXml parser = new MiniXml(text);
    return parser.document();
  }

  static String escape(String value) {
    StringBuilder sb = new StringBuilder(value.length() + 8);
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
      case '&': sb.append("&amp;"); break;
      case '<': sb.append("&lt;"); break;
      case '>': sb.append("&gt;"); break;
      case '"': sb.append("&quot;"); break;
      default:
        if (c < 0x20 && c != 0) {
          sb.append("&#").append((int) c).append(';');
        } else {
          sb.append(c);
        }
      }
    }
    return sb.toString();
  }

  private Element document() throws ParseException {
    Element root = null;
    while (true) {
      skipSpace();
      if (pos >= text.length()) {
        break;
      }
      if (text.startsWith("<?", pos)) {
        skipTo("?>");
      } else if (text.startsWith("<!--", pos)) {
        skipTo("-->");
      } else if (text.startsWith("<!DOCTYPE", pos)) {
        skipDoctype();
      } else if (text.charAt(pos) == '<') {
        if (root != null) {
          throw new ParseException("The markup in the document following the root element must be well-formed.");
        }
        root = element();
      } else {
        throw new ParseException("Content is not allowed in prolog.");
      }
    }
    if (root == null) {
      throw new ParseException("Premature end of file.");
    }
    return root;
  }

  private void skipSpace() {
    while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
      pos++;
    }
  }

  private void skipTo(String end) throws ParseException {
    int at = text.indexOf(end, pos);
    if (at < 0) {
      throw new ParseException("Premature end of file.");
    }
    pos = at + end.length();
  }

  private void skipDoctype() throws ParseException {
    int depth = 0;
    while (pos < text.length()) {
      char c = text.charAt(pos++);
      if (c == '[') {
        depth++;
      } else if (c == ']') {
        depth--;
      } else if (c == '>' && depth <= 0) {
        return;
      }
    }
    throw new ParseException("Premature end of file.");
  }

  private String name() throws ParseException {
    int start = pos;
    while (pos < text.length()) {
      char c = text.charAt(pos);
      if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.' || c == ':') {
        pos++;
      } else {
        break;
      }
    }
    if (start == pos) {
      throw new ParseException("The markup in the document must be well-formed.");
    }
    return text.substring(start, pos);
  }

  private Element element() throws ParseException {
    pos++;
    Element e = new Element(name());
    while (true) {
      skipSpace();
      if (pos >= text.length()) {
        throw new ParseException("Premature end of file.");
      }
      char c = text.charAt(pos);
      if (c == '/') {
        if (!text.startsWith("/>", pos)) {
          throw new ParseException("Element \"" + e.name + "\" must be terminated by the matching end-tag.");
        }
        pos += 2;
        return e;
      }
      if (c == '>') {
        pos++;
        break;
      }
      String attr = name();
      skipSpace();
      if (pos >= text.length() || text.charAt(pos) != '=') {
        throw new ParseException("Attribute name \"" + attr + "\" must be followed by the ' = ' character.");
      }
      pos++;
      skipSpace();
      if (pos >= text.length() || (text.charAt(pos) != '"' && text.charAt(pos) != '\'')) {
        throw new ParseException("Open quote is expected for attribute \"" + attr + "\".");
      }
      char quote = text.charAt(pos++);
      int end = text.indexOf(quote, pos);
      if (end < 0) {
        throw new ParseException("Premature end of file.");
      }
      e.attributes.put(attr, unescape(text.substring(pos, end)));
      pos = end + 1;
    }
    while (true) {
      if (pos >= text.length()) {
        throw new ParseException("Premature end of file.");
      }
      if (text.startsWith("</", pos)) {
        pos += 2;
        String closing = name();
        skipSpace();
        if (!closing.equals(e.name) || pos >= text.length() || text.charAt(pos) != '>') {
          throw new ParseException("The element type \"" + e.name + "\" must be terminated by the matching end-tag \""
              + "</" + e.name + ">\".");
        }
        pos++;
        return e;
      }
      if (text.startsWith("<!--", pos)) {
        skipTo("-->");
      } else if (text.startsWith("<?", pos)) {
        skipTo("?>");
      } else if (text.charAt(pos) == '<') {
        e.children.add(element());
      } else {
        int next = text.indexOf('<', pos);
        if (next < 0) {
          throw new ParseException("Premature end of file.");
        }
        pos = next;
      }
    }
  }

  private String unescape(String value) throws ParseException {
    if (value.indexOf('&') < 0) {
      return value;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c != '&') {
        sb.append(c);
        continue;
      }
      int semi = value.indexOf(';', i);
      if (semi < 0) {
        throw new ParseException("The entity name must immediately follow the '&' in the entity reference.");
      }
      String ref = value.substring(i + 1, semi);
      if (ref.equals("amp")) {
        sb.append('&');
      } else if (ref.equals("lt")) {
        sb.append('<');
      } else if (ref.equals("gt")) {
        sb.append('>');
      } else if (ref.equals("quot")) {
        sb.append('"');
      } else if (ref.equals("apos")) {
        sb.append('\'');
      } else if (ref.startsWith("#x")) {
        sb.appendCodePoint(Integer.parseInt(ref.substring(2), 16));
      } else if (ref.startsWith("#")) {
        sb.appendCodePoint(Integer.parseInt(ref.substring(1)));
      } else {
        throw new ParseException("The entity \"" + ref + "\" was referenced, but not declared.");
      }
      i = semi;
    }
    return sb.toString();
  }
}
