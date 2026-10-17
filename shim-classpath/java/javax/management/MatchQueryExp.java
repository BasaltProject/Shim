/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class MatchQueryExp extends QueryEval implements QueryExp {
  private static final long serialVersionUID = -7156126829777889419L;

  private AttributeValueExp exp;
  private String pattern;

  MatchQueryExp(AttributeValueExp v1, StringValueExp s) {
    exp = v1;
    pattern = s.getValue();
  }

  @Override
  public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    ValueExp val = exp.apply(name);
    if (!(val instanceof StringValueExp)) {
      return false;
    }
    return wildmatch(((StringValueExp) val).getValue(), pattern);
  }

  @Override
  public String toString() {
    return exp + " like " + new StringValueExp(pattern);
  }

  static boolean wildmatch(String str, String pat) {
    return match(str, 0, pat, 0);
  }

  private static boolean match(String s, int si, String p, int pi) {
    while (pi < p.length()) {
      char c = p.charAt(pi);
      if (c == '*') {
        while (pi < p.length() && p.charAt(pi) == '*') {
          pi++;
        }
        if (pi == p.length()) {
          return true;
        }
        for (int k = si; k <= s.length(); k++) {
          if (match(s, k, p, pi)) {
            return true;
          }
        }
        return false;
      }
      if (si >= s.length()) {
        return false;
      }
      char sc = s.charAt(si);
      if (c == '?') {
        pi++;
        si++;
      } else if (c == '[') {
        int end = p.indexOf(']', pi + 1);
        if (end < 0) {
          return false;
        }
        boolean negate = p.charAt(pi + 1) == '!';
        int i = negate ? pi + 2 : pi + 1;
        boolean found = false;
        while (i < end) {
          char lo = p.charAt(i);
          if (i + 2 < end && p.charAt(i + 1) == '-') {
            char hi = p.charAt(i + 2);
            if (sc >= lo && sc <= hi) {
              found = true;
            }
            i += 3;
          } else {
            if (sc == lo) {
              found = true;
            }
            i++;
          }
        }
        if (found == negate) {
          return false;
        }
        pi = end + 1;
        si++;
      } else {
        if (c == '\\' && pi + 1 < p.length()) {
          pi++;
          c = p.charAt(pi);
        }
        if (c != sc) {
          return false;
        }
        pi++;
        si++;
      }
    }
    return si == s.length();
  }
}
