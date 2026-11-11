/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.IOException;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PolicyParser {
  public static class ParsingException extends GeneralSecurityException {
    private static final long serialVersionUID = -4330692689482574072L;

    public ParsingException(String msg) {
      super(msg);
    }
  }

  public static final class KeyStoreEntry {
    public final String name;
    public final Map<String, String> properties = new LinkedHashMap<String, String>();

    KeyStoreEntry(String name) {
      this.name = name;
    }
  }

  public static final class DomainEntry {
    public final String name;
    public final Map<String, String> properties = new LinkedHashMap<String, String>();
    public final Map<String, KeyStoreEntry> keystores = new LinkedHashMap<String, KeyStoreEntry>();

    DomainEntry(String name) {
      this.name = name;
    }
  }

  private final List<String> tokens = new ArrayList<String>();
  private final List<Boolean> quoted = new ArrayList<Boolean>();
  private final List<Integer> lines = new ArrayList<Integer>();
  private int pos;

  private PolicyParser() {
  }

  public static Map<String, DomainEntry> parseDomains(Reader reader) throws IOException, ParsingException {
    PolicyParser p = new PolicyParser();
    p.tokenize(reader);
    return p.parse();
  }

  private void tokenize(Reader r) throws IOException, ParsingException {
    StringBuilder text = new StringBuilder();
    char[] buf = new char[4096];
    int n;
    while ((n = r.read(buf)) > 0) {
      text.append(buf, 0, n);
    }
    int i = 0;
    int len = text.length();
    int line = 1;
    int seen = 0;
    while (i < len) {
      for (; seen < i; seen++) {
        if (text.charAt(seen) == '\n') {
          line++;
        }
      }
      while (lines.size() < tokens.size()) {
        lines.add(line);
      }
      char c = text.charAt(i);
      if (Character.isWhitespace(c)) {
        i++;
      } else if (c == '/' && i + 1 < len && text.charAt(i + 1) == '/' || c == '#') {
        while (i < len && text.charAt(i) != '\n') {
          i++;
        }
      } else if (c == '/' && i + 1 < len && text.charAt(i + 1) == '*') {
        int end = text.indexOf("*/", i + 2);
        if (end < 0) {
          throw new ParsingException("expected [*/], read [end of file]");
        }
        i = end + 2;
      } else if (c == '{' || c == '}' || c == ';' || c == '=') {
        tokens.add(String.valueOf(c));
        quoted.add(Boolean.FALSE);
        i++;
      } else if (c == '"' || c == '\'') {
        StringBuilder sb = new StringBuilder();
        i++;
        while (i < len && text.charAt(i) != c) {
          if (text.charAt(i) == '\\' && i + 1 < len) {
            i++;
          }
          sb.append(text.charAt(i++));
        }
        if (i >= len) {
          throw new ParsingException("unterminated string");
        }
        i++;
        tokens.add(sb.toString());
        quoted.add(Boolean.TRUE);
      } else {
        int start = i;
        while (i < len && !Character.isWhitespace(text.charAt(i)) && "{};=\"'".indexOf(text.charAt(i)) < 0) {
          i++;
        }
        tokens.add(text.substring(start, i));
        quoted.add(Boolean.FALSE);
      }
    }
    while (lines.size() < tokens.size()) {
      lines.add(line);
    }
  }

  private String peek() {
    return pos < tokens.size() ? tokens.get(pos) : null;
  }

  private boolean peekIs(String symbol) {
    return pos < tokens.size() && !quoted.get(pos) && tokens.get(pos).equals(symbol);
  }

  private String describe() {
    return pos < tokens.size() ? tokens.get(pos) : "end of file";
  }

  private ParsingException expected(String what) {
    if (pos >= tokens.size()) {
      return new ParsingException("expected [" + what + "], read [end of file]");
    }
    int line = lines.get(pos);
    return new ParsingException("line " + line + ": expected [" + what + "], found [" + describe() + "]");
  }

  private void expect(String symbol) throws ParsingException {
    if (!peekIs(symbol)) {
      throw expected(symbol);
    }
    pos++;
  }

  private String name(String expected) throws ParsingException {
    if (peek() == null || peekIs("{") || peekIs("}") || peekIs(";") || peekIs("=")) {
      throw expected(expected);
    }
    return tokens.get(pos++);
  }

  private void properties(Map<String, String> into) throws ParsingException {
    while (peek() != null && !peekIs("{") && !peekIs(";") && !peekIs("}")) {
      String key = name("property name");
      expect("=");
      if (peek() == null || peekIs("{") || peekIs("}") || peekIs(";")) {
        throw expected("property value");
      }
      into.put(key, expand(tokens.get(pos++)));
    }
  }

  private static String expand(String value) throws ParsingException {
    int start = value.indexOf("${");
    if (start < 0) {
      return value;
    }
    StringBuilder out = new StringBuilder();
    int i = 0;
    while (start >= 0) {
      int end = value.indexOf('}', start);
      if (end < 0) {
        throw new ParsingException("Missing '}' in property expansion");
      }
      out.append(value, i, start);
      String key = value.substring(start + 2, end);
      String v = key.startsWith("env.") ? System.getenv(key.substring(4)) : System.getProperty(key);
      if (v == null) {
        throw new ParsingException("Unable to expand property " + key);
      }
      out.append(v);
      i = end + 1;
      start = value.indexOf("${", i);
    }
    return out.append(value.substring(i)).toString();
  }

  private Map<String, DomainEntry> parse() throws ParsingException {
    Map<String, DomainEntry> domains = new LinkedHashMap<String, DomainEntry>();
    while (peek() != null) {
      if (!"domain".equals(peek())) {
        throw expected("domain");
      }
      pos++;
      DomainEntry domain = new DomainEntry(name("domain name"));
      if (domains.containsKey(domain.name)) {
        throw new ParsingException("duplicate keystore domain name: " + domain.name);
      }
      properties(domain.properties);
      expect("{");
      while (!peekIs("}")) {
        if (!"keystore".equals(peek())) {
          throw expected("keystore");
        }
        pos++;
        KeyStoreEntry ks = new KeyStoreEntry(name("keystore name"));
        if (domain.keystores.containsKey(ks.name)) {
          throw new ParsingException("duplicate keystore name: " + ks.name);
        }
        properties(ks.properties);
        expect(";");
        domain.keystores.put(ks.name, ks);
      }
      expect("}");
      expect(";");
      domains.put(domain.name, domain);
    }
    return domains;
  }
}
