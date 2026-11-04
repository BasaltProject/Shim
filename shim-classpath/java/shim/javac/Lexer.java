/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Lexer {
  static final Set<String> KEYWORDS = new HashSet<String>(Arrays.asList("abstract", "assert", "boolean", "break",
      "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum",
      "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
      "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static",
      "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void",
      "volatile", "while", "true", "false", "null", "_"));
  private static final String[] OPS3 = {"<<=", "...", "->", "::", "++", "--", "&&", "||", "==", "!=", "<=", "+=", "-=",
      "*=", "/=", "&=", "|=", "^=", "%=", "<<"};

  public static final class LexError extends RuntimeException {
    public final int pos;

    LexError(String msg, int pos) {
      super(msg);
      this.pos = pos;
    }
  }

  private final String src;
  private final int[] map;
  private final String text;
  private int p;
  private final List<Token> tokens = new ArrayList<Token>();
  private String pendingDoc;
  private int pendingEnd = -1;

  private int markdownEnd(int lineStart) {
    int q = lineStart - 1;
    while (q >= 0 && (text.charAt(q) == ' ' || text.charAt(q) == '\t' || text.charAt(q) == '\n'
        || text.charAt(q) == '\r' || text.charAt(q) == '\f')) {
      q--;
    }
    return q + 1;
  }
  public final List<int[]> errors = new ArrayList<int[]>();
  public final List<String> errorMessages = new ArrayList<String>();

  public Lexer(String source) {
    this.src = source;
    if (source.indexOf("\\u") >= 0) {
      StringBuilder sb = new StringBuilder();
      int[] m = new int[source.length() + 1];
      int out = 0;
      int i = 0;
      while (i < source.length()) {
        char c = source.charAt(i);
        if (c == '\\' && i + 1 < source.length() && source.charAt(i + 1) == 'u' && evenBackslashes(source, i)) {
          int j = i + 1;
          while (j < source.length() && source.charAt(j) == 'u') {
            j++;
          }
          if (j + 4 <= source.length()) {
            try {
              char u = (char) Integer.parseInt(source.substring(j, j + 4), 16);
              sb.append(u);
              m[out++] = i;
              i = j + 4;
              continue;
            } catch (NumberFormatException e) {
            }
          }
        }
        sb.append(c);
        m[out++] = i;
        i++;
      }
      m[out] = source.length();
      this.text = sb.toString();
      this.map = m;
    } else {
      this.text = source;
      this.map = null;
    }
  }

  private static boolean evenBackslashes(String s, int i) {
    int n = 0;
    int k = i - 1;
    while (k >= 0 && s.charAt(k) == '\\') {
      n++;
      k--;
    }
    return n % 2 == 0;
  }

  private int orig(int i) {
    return map == null ? i : map[Math.min(i, map.length - 1)];
  }

  public List<Token> lex() {
    int n = text.length();
    while (true) {
      skipSpace();
      if (p >= n) {
        break;
      }
      int start = p;
      char c = text.charAt(p);
      try {
        if (Character.isJavaIdentifierStart(text.codePointAt(p))) {
          p += Character.charCount(text.codePointAt(p));
          while (p < n && Character.isJavaIdentifierPart(text.codePointAt(p))) {
            p += Character.charCount(text.codePointAt(p));
          }
          String id = text.substring(start, p);
          add(KEYWORDS.contains(id) ? Token.KEYWORD : Token.IDENT, id, start, null);
        } else if (c >= '0' && c <= '9' || (c == '.' && p + 1 < n && Character.isDigit(text.charAt(p + 1)))) {
          number(start);
        } else if (c == '"') {
          string(start);
        } else if (c == '\'') {
          chr(start);
        } else {
          op(start);
        }
      } catch (LexError e) {
        errors.add(new int[] {orig(e.pos)});
        errorMessages.add(e.getMessage());
        if (p <= start) {
          p = start + 1;
        }
        add(Token.ERROR, text.substring(start, Math.min(p, n)), start, null);
      }
    }
    Token eof = new Token(Token.EOF, "", orig(n), orig(n), null);
    tokens.add(eof);
    return tokens;
  }

  private void add(int kind, String t, int start, Object value) {
    Token tok = new Token(kind, t, orig(start), orig(p), value);
    tok.doc = pendingDoc;
    pendingDoc = null;
    tokens.add(tok);
  }

  private void skipSpace() {
    int n = text.length();
    while (p < n) {
      char c = text.charAt(p);
      if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f') {
        p++;
      } else if (c == '/' && p + 1 < n && text.charAt(p + 1) == '/') {
        int ls = p;
        while (p < n && text.charAt(p) != '\n' && text.charAt(p) != '\r') {
          p++;
        }
        if (text.startsWith("///", ls) && !(text.startsWith("////", ls))) {
          String line = text.substring(ls, p);
          if (pendingDoc != null && pendingDoc.startsWith("///") && pendingEnd == markdownEnd(ls)) {
            pendingDoc = pendingDoc + "\n" + line;
          } else {
            pendingDoc = line;
          }
          pendingEnd = p;
        } else if (pendingDoc != null && pendingDoc.startsWith("///")) {
          pendingDoc = null;
        }
      } else if (c == '/' && p + 1 < n && text.charAt(p + 1) == '*') {
        int s = p;
        int e = text.indexOf("*/", p + 2);
        if (e < 0) {
          p = n;
          errors.add(new int[] {orig(s)});
          errorMessages.add("unclosed comment");
          return;
        }
        if (text.charAt(p + 2) == '*' && e > p + 2) {
          pendingDoc = text.substring(s, e + 2);
        } else if (text.charAt(p + 2) == '*' && e == p + 2) {
          pendingDoc = null;
        }
        p = e + 2;
      } else {
        return;
      }
    }
  }

  private void op(int start) {
    int n = text.length();
    char c = text.charAt(p);
    for (String o : OPS3) {
      if (text.startsWith(o, p)) {
        if (o.equals("<<=") || o.equals("...")) {
          p += 3;
        } else {
          p += o.length();
        }
        add(Token.OP, o, start, null);
        return;
      }
    }
    if ("(){}[];,.@=<>!~?:+-*/&|^%".indexOf(c) >= 0) {
      p++;
      add(Token.OP, String.valueOf(c), start, null);
      return;
    }
    p++;
    throw new LexError("illegal character: '" + c + "'", start);
  }

  private void number(int start) {
    int n = text.length();
    boolean hex = false;
    boolean bin = false;
    boolean isFloat = false;
    int radix = 10;
    if (text.charAt(p) == '0' && p + 1 < n && (text.charAt(p + 1) == 'x' || text.charAt(p + 1) == 'X')) {
      hex = true;
      radix = 16;
      p += 2;
    } else if (text.charAt(p) == '0' && p + 1 < n && (text.charAt(p + 1) == 'b' || text.charAt(p + 1) == 'B')) {
      bin = true;
      radix = 2;
      p += 2;
    }
    int digitsStart = p;
    while (p < n && (Character.digit(text.charAt(p), hex ? 16 : 10) >= 0 || text.charAt(p) == '_')) {
      p++;
    }
    if (!bin) {
      if (p < n && text.charAt(p) == '.' && !(p + 1 < n && text.charAt(p + 1) == '.')
          && (hex || p + 1 >= n || !Character.isJavaIdentifierStart(text.charAt(p + 1)) || isExp(text.charAt(p + 1))
          || "fFdD".indexOf(text.charAt(p + 1)) >= 0)) {
        isFloat = true;
        p++;
        while (p < n && (Character.digit(text.charAt(p), hex ? 16 : 10) >= 0 || text.charAt(p) == '_')) {
          p++;
        }
      }
      if (p < n && (hex ? (text.charAt(p) == 'p' || text.charAt(p) == 'P') : (text.charAt(p) == 'e' || text.charAt(p) == 'E'))) {
        isFloat = true;
        p++;
        if (p < n && (text.charAt(p) == '+' || text.charAt(p) == '-')) {
          p++;
        }
        int ds = p;
        while (p < n && Character.isDigit(text.charAt(p))) {
          p++;
        }
        if (ds == p) {
          throw new LexError("malformed floating-point literal", start);
        }
      }
    }
    String body = text.substring(start, p).replace("_", "");
    char suffix = p < n ? text.charAt(p) : 0;
    if (!hex && (suffix == 'f' || suffix == 'F' || suffix == 'd' || suffix == 'D')) {
      p++;
      String lit = body;
      if (suffix == 'f' || suffix == 'F') {
        add(Token.FLOATLIT, text.substring(start, p), start, Float.valueOf(Float.parseFloat(lit)));
      } else {
        add(Token.DOUBLELIT, text.substring(start, p), start, Double.valueOf(Double.parseDouble(lit)));
      }
      return;
    }
    if (isFloat) {
      if (hex && (suffix == 'f' || suffix == 'F')) {
        p++;
        add(Token.FLOATLIT, text.substring(start, p), start, Float.valueOf(Float.parseFloat(body)));
      } else if (hex && (suffix == 'd' || suffix == 'D')) {
        p++;
        add(Token.DOUBLELIT, text.substring(start, p), start, Double.valueOf(Double.parseDouble(body)));
      } else {
        add(Token.DOUBLELIT, text.substring(start, p), start, Double.valueOf(Double.parseDouble(body)));
      }
      return;
    }
    boolean isLong = suffix == 'l' || suffix == 'L';
    if (isLong) {
      p++;
    }
    String digits = body;
    if (hex || bin) {
      digits = body.substring(2);
    } else if (body.length() > 1 && body.charAt(0) == '0') {
      radix = 8;
      digits = body.substring(1);
    }
    if (digits.isEmpty()) {
      throw new LexError(hex ? "hexadecimal numbers must contain at least one hexadecimal digit"
          : "binary numbers must contain at least one binary digit", start);
    }
    try {
      java.math.BigInteger v = new java.math.BigInteger(digits, radix);
      if (isLong) {
        if (radix == 10 ? v.compareTo(java.math.BigInteger.valueOf(Long.MAX_VALUE).add(java.math.BigInteger.ONE)) > 0
            : v.bitLength() > 64) {
          throw new LexError("integer number too large", start);
        }
        add(Token.LONGLIT, text.substring(start, p), start, Long.valueOf(v.longValue()));
      } else {
        if (radix == 10 ? v.compareTo(java.math.BigInteger.valueOf(Integer.MAX_VALUE + 1L)) > 0 : v.bitLength() > 32) {
          throw new LexError("integer number too large", start);
        }
        add(Token.INTLIT, text.substring(start, p), start, Integer.valueOf(v.intValue()));
      }
    } catch (NumberFormatException e) {
      throw new LexError("illegal digit in an octal literal", start);
    }
  }

  private static boolean isExp(char c) {
    return c == 'e' || c == 'E';
  }

  private void chr(int start) {
    int n = text.length();
    p++;
    if (p >= n || text.charAt(p) == '\n') {
      throw new LexError("illegal line end in char literal", start);
    }
    char v;
    if (text.charAt(p) == '\\') {
      StringBuilder sb = new StringBuilder();
      escape(sb);
      v = sb.charAt(0);
    } else if (text.charAt(p) == '\'') {
      p++;
      throw new LexError("empty character literal", start);
    } else {
      v = text.charAt(p++);
    }
    if (p >= n || text.charAt(p) != '\'') {
      throw new LexError("unclosed character literal", start);
    }
    p++;
    add(Token.CHARLIT, text.substring(start, p), start, Character.valueOf(v));
  }

  private void escape(StringBuilder sb) {
    int n = text.length();
    int s = p;
    p++;
    if (p >= n) {
      throw new LexError("illegal escape character", s);
    }
    char c = text.charAt(p++);
    switch (c) {
      case 'b':
        sb.append('\b');
        break;
      case 't':
        sb.append('\t');
        break;
      case 'n':
        sb.append('\n');
        break;
      case 'f':
        sb.append('\f');
        break;
      case 'r':
        sb.append('\r');
        break;
      case 's':
        sb.append(' ');
        break;
      case '"':
        sb.append('"');
        break;
      case '\'':
        sb.append('\'');
        break;
      case '\\':
        sb.append('\\');
        break;
      default:
        if (c >= '0' && c <= '7') {
          int v = c - '0';
          int max = c <= '3' ? 2 : 1;
          while (max-- > 0 && p < n && text.charAt(p) >= '0' && text.charAt(p) <= '7') {
            v = v * 8 + (text.charAt(p++) - '0');
          }
          sb.append((char) v);
        } else {
          throw new LexError("illegal escape character", s);
        }
    }
  }

  private void string(int start) {
    int n = text.length();
    if (text.startsWith("\"\"\"", p)) {
      textBlock(start);
      return;
    }
    p++;
    StringBuilder sb = new StringBuilder();
    while (true) {
      if (p >= n || text.charAt(p) == '\n' || text.charAt(p) == '\r') {
        throw new LexError("unclosed string literal", start);
      }
      char c = text.charAt(p);
      if (c == '"') {
        p++;
        break;
      }
      if (c == '\\') {
        escape(sb);
      } else {
        sb.append(c);
        p++;
      }
    }
    add(Token.STRINGLIT, text.substring(start, p), start, sb.toString());
  }

  private void textBlock(int start) {
    int n = text.length();
    p += 3;
    while (p < n && (text.charAt(p) == ' ' || text.charAt(p) == '\t' || text.charAt(p) == '\f')) {
      p++;
    }
    if (p >= n || (text.charAt(p) != '\n' && text.charAt(p) != '\r')) {
      throw new LexError("illegal text block open delimiter sequence, missing line terminator", start);
    }
    if (text.charAt(p) == '\r' && p + 1 < n && text.charAt(p + 1) == '\n') {
      p++;
    }
    p++;
    int bodyStart = p;
    while (true) {
      if (p >= n) {
        throw new LexError("unclosed text block", start);
      }
      if (text.startsWith("\"\"\"", p)) {
        break;
      }
      if (text.charAt(p) == '\\') {
        p += 2;
      } else {
        p++;
      }
    }
    String raw = text.substring(bodyStart, p).replace("\r\n", "\n").replace('\r', '\n');
    p += 3;
    String[] lines = raw.split("\n", -1);
    int min = Integer.MAX_VALUE;
    for (int i = 0; i < lines.length; i++) {
      String l = lines[i];
      boolean last = i == lines.length - 1;
      if (l.trim().isEmpty() && !last) {
        continue;
      }
      int ind = 0;
      while (ind < l.length() && (l.charAt(ind) == ' ' || l.charAt(ind) == '\t' || l.charAt(ind) == '\f')) {
        ind++;
      }
      min = Math.min(min, ind);
    }
    if (min == Integer.MAX_VALUE) {
      min = 0;
    }
    StringBuilder joined = new StringBuilder();
    for (int i = 0; i < lines.length; i++) {
      String l = lines[i];
      boolean last = i == lines.length - 1;
      String stripped = l.trim().isEmpty() ? "" : l.substring(Math.min(min, l.length()));
      int e = stripped.length();
      while (e > 0 && (stripped.charAt(e - 1) == ' ' || stripped.charAt(e - 1) == '\t' || stripped.charAt(e - 1) == '\f')) {
        e--;
      }
      joined.append(stripped, 0, e);
      if (!last) {
        joined.append('\n');
      }
    }
    String body = joined.toString();
    StringBuilder out = new StringBuilder();
    int save = p;
    int i = 0;
    while (i < body.length()) {
      char c = body.charAt(i);
      if (c == '\\' && i + 1 < body.length()) {
        char d = body.charAt(i + 1);
        if (d == '\n') {
          i += 2;
          continue;
        }
        i += 2;
        switch (d) {
          case 'b':
            out.append('\b');
            break;
          case 't':
            out.append('\t');
            break;
          case 'n':
            out.append('\n');
            break;
          case 'f':
            out.append('\f');
            break;
          case 'r':
            out.append('\r');
            break;
          case 's':
            out.append(' ');
            break;
          case '"':
            out.append('"');
            break;
          case '\'':
            out.append('\'');
            break;
          case '\\':
            out.append('\\');
            break;
          default:
            if (d >= '0' && d <= '7') {
              int v = d - '0';
              int max = d <= '3' ? 2 : 1;
              while (max-- > 0 && i < body.length() && body.charAt(i) >= '0' && body.charAt(i) <= '7') {
                v = v * 8 + (body.charAt(i++) - '0');
              }
              out.append((char) v);
            } else {
              throw new LexError("illegal escape character", start);
            }
        }
      } else {
        out.append(c);
        i++;
      }
    }
    p = save;
    Token tok = new Token(Token.STRINGLIT, text.substring(start, p), orig(start), orig(p), out.toString());
    tok.textBlock = true;
    tok.doc = pendingDoc;
    pendingDoc = null;
    tokens.add(tok);
  }
}
