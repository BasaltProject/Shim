/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.xpath;

import java.util.ArrayList;
import java.util.List;

final class XPathLexer {
  static final int kNumber = 1;
  static final int kLiteral = 2;
  static final int kName = 3;
  static final int kVar = 4;
  static final int kOp = 5;
  static final int kEnd = 6;

  static final class Tok {
    final int type;
    final String text;
    final String prefix;
    final String local;

    Tok(int type, String text, String prefix, String local) {
      this.type = type;
      this.text = text;
      this.prefix = prefix;
      this.local = local;
    }

    boolean is(String op) {
      return type == kOp && text.equals(op);
    }

    boolean isNamed(String name) {
      return type == kName && prefix == null && local.equals(name);
    }
  }

  private XPathLexer() {
  }

  static boolean nameStart(char ch) {
    return ch == '_' || Character.isLetter(ch) || ch >= 0xC0 && Character.isUnicodeIdentifierStart(ch);
  }

  static boolean nameChar(char ch) {
    return nameStart(ch) || ch == '-' || ch == '.' || (ch >= '0' && ch <= '9') || ch == 0xB7
        || (ch >= 0x300 && Character.isUnicodeIdentifierPart(ch));
  }

  static List<Tok> lex(String s) {
    List<Tok> out = new ArrayList<Tok>();
    int i = 0;
    int n = s.length();
    while (i < n) {
      char ch = s.charAt(i);
      if (XPathValues.isSpace(ch)) {
        i++;
        continue;
      }
      Tok prev = out.isEmpty() ? null : out.get(out.size() - 1);
      boolean operandExpected = prev == null
          || prev.type == kOp && !prev.is(")") && !prev.is("]") && !prev.is(".") && !prev.is("..");
      if (ch == '"' || ch == '\'') {
        int end = s.indexOf(ch, i + 1);
        if (end < 0) {
          throw new XPathError("misquoted literal... expected " + (ch == '"' ? "double" : "single") + " quote!");
        }
        out.add(new Tok(kLiteral, s.substring(i, end + 1), null, s.substring(i + 1, end)));
        i = end + 1;
      } else if ((ch >= '0' && ch <= '9') || (ch == '.' && i + 1 < n && s.charAt(i + 1) >= '0' && s.charAt(i + 1) <= '9')) {
        int j = i;
        int dots = 0;
        while (j < n && (s.charAt(j) >= '0' && s.charAt(j) <= '9' || s.charAt(j) == '.')) {
          if (s.charAt(j) == '.') {
            dots++;
          }
          j++;
        }
        int k = j;
        while (k < n && (nameChar(s.charAt(k)) && s.charAt(k) != '-')) {
          k++;
        }
        if (dots > 1 || k > j || (j < n && s.charAt(j) == '.')) {
          throw new XPathError(s.substring(i, Math.max(k, j)) + " could not be formatted to a number!");
        }
        out.add(new Tok(kNumber, s.substring(i, j), null, s.substring(i, j)));
        i = j;
      } else if (ch == '$') {
        int j = i + 1;
        while (j < n && (nameChar(s.charAt(j)) || s.charAt(j) == ':')) {
          j++;
        }
        String q = s.substring(i + 1, j);
        int colon = q.indexOf(':');
        out.add(new Tok(kVar, s.substring(i, j), colon < 0 ? null : q.substring(0, colon),
            colon < 0 ? q : q.substring(colon + 1)));
        i = j;
      } else if (nameStart(ch) || (ch == '*' && operandExpected)) {
        int j = i;
        String prefix = null;
        String local;
        if (ch == '*') {
          j = i + 1;
          local = "*";
        } else {
          while (j < n && nameChar(s.charAt(j))) {
            j++;
          }
          local = s.substring(i, j);
          if (j < n && s.charAt(j) == ':' && !(j + 1 < n && s.charAt(j + 1) == ':')) {
            prefix = local;
            j++;
            if (j < n && s.charAt(j) == '*') {
              local = "*";
              j++;
            } else if (j < n && nameStart(s.charAt(j))) {
              int st = j;
              while (j < n && nameChar(s.charAt(j))) {
                j++;
              }
              local = s.substring(st, j);
            } else {
              throw new XPathError("A node test that matches either NCName:* or QName was expected.");
            }
          }
        }
        if (!operandExpected && prefix == null && ch != '*'
            && (local.equals("and") || local.equals("or") || local.equals("mod") || local.equals("div"))) {
          out.add(new Tok(kOp, local, null, null));
        } else {
          out.add(new Tok(kName, s.substring(i, j), prefix, local));
        }
        i = j;
      } else {
        String two = i + 1 < n ? s.substring(i, i + 2) : "";
        if (two.equals("//") || two.equals("::") || two.equals("..") || two.equals("!=") || two.equals("<=")
            || two.equals(">=")) {
          out.add(new Tok(kOp, two, null, null));
          i += 2;
        } else if ("()[]@,./|+-=<>*".indexOf(ch) >= 0) {
          out.add(new Tok(kOp, String.valueOf(ch), null, null));
          i++;
        } else {
          out.add(new Tok(kOp, String.valueOf(ch), null, null));
          i++;
        }
      }
    }
    out.add(new Tok(kEnd, "", null, null));
    return out;
  }
}
