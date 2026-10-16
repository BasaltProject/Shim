/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

public final class IDN {
  public static final int ALLOW_UNASSIGNED = 0x01;
  public static final int USE_STD3_ASCII_RULES = 0x02;

  private static final String kAcePrefix = "xn--";
  private static final int kBase = 36;
  private static final int kTMin = 1;
  private static final int kTMax = 26;
  private static final int kSkew = 38;
  private static final int kDamp = 700;
  private static final int kInitialBias = 72;
  private static final int kInitialN = 128;

  private IDN() { }

  public static String toASCII(String input, int flag) {
    int p = 0;
    int q;
    StringBuilder out = new StringBuilder();
    while (p < input.length()) {
      q = searchDots(input, p);
      out.append(toASCIIInternal(input.substring(p, q), flag));
      if (q != input.length()) {
        out.append('.');
      }
      p = q + 1;
    }
    return out.toString();
  }

  public static String toASCII(String input) {
    return toASCII(input, 0);
  }

  public static String toUnicode(String input, int flag) {
    int p = 0;
    int q;
    StringBuilder out = new StringBuilder();
    while (p < input.length()) {
      q = searchDots(input, p);
      out.append(toUnicodeInternal(input.substring(p, q), flag));
      if (q != input.length()) {
        out.append('.');
      }
      p = q + 1;
    }
    return out.toString();
  }

  public static String toUnicode(String input) {
    return toUnicode(input, 0);
  }

  private static boolean isAllAscii(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) > 0x7f) {
        return false;
      }
    }
    return true;
  }

  private static String toASCIIInternal(String label, int flag) {
    boolean isASCII = isAllAscii(label);
    String dest;
    if (!isASCII) {
      dest = label.toLowerCase(java.util.Locale.ROOT);
    } else {
      dest = label;
    }
    if ((flag & USE_STD3_ASCII_RULES) != 0) {
      for (int i = 0; i < dest.length(); i++) {
        char c = dest.charAt(i);
        if (c <= 0x7f && !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-')) {
          throw new IllegalArgumentException("Contains non-LDH ASCII characters");
        }
      }
      if (dest.length() > 0 && (dest.charAt(0) == '-' || dest.charAt(dest.length() - 1) == '-')) {
        throw new IllegalArgumentException("Has leading or trailing hyphen");
      }
    }
    if (!isAllAscii(dest)) {
      if (dest.regionMatches(true, 0, kAcePrefix, 0, kAcePrefix.length())) {
        throw new IllegalArgumentException("The input starts with the ACE Prefix");
      }
      dest = kAcePrefix + punyEncode(dest);
    }
    if (dest.length() > 63) {
      throw new IllegalArgumentException("The label in the input is too long");
    }
    if (dest.isEmpty()) {
      throw new IllegalArgumentException("Empty label is not a legal name");
    }
    return dest;
  }

  private static String toUnicodeInternal(String label, int flag) {
    if (isAllAscii(label) && label.regionMatches(true, 0, kAcePrefix, 0, kAcePrefix.length())) {
      try {
        String decoded = punyDecode(label.substring(kAcePrefix.length()));
        String back = toASCIIInternal(decoded, flag);
        if (back.equalsIgnoreCase(label)) {
          return decoded;
        }
      } catch (RuntimeException e) {
        return label;
      }
    }
    return label;
  }

  private static int searchDots(String s, int start) {
    int i;
    for (i = start; i < s.length(); i++) {
      if (isLabelSeparator(s.charAt(i))) {
        break;
      }
    }
    return i;
  }

  private static boolean isLabelSeparator(char c) {
    return c == '.' || c == '。' || c == '．' || c == '｡';
  }

  private static int adapt(int delta, int numPoints, boolean first) {
    delta = first ? delta / kDamp : delta / 2;
    delta += delta / numPoints;
    int k = 0;
    while (delta > ((kBase - kTMin) * kTMax) / 2) {
      delta /= kBase - kTMin;
      k += kBase;
    }
    return k + (kBase - kTMin + 1) * delta / (delta + kSkew);
  }

  private static char digit(int d) {
    return (char) (d < 26 ? 'a' + d : '0' + (d - 26));
  }

  private static String punyEncode(String input) {
    int[] cps = input.codePoints().toArray();
    StringBuilder out = new StringBuilder();
    int n = kInitialN;
    int delta = 0;
    int bias = kInitialBias;
    int basic = 0;
    for (int cp : cps) {
      if (cp < 0x80) {
        out.append((char) cp);
        basic++;
      }
    }
    int h = basic;
    if (basic > 0) {
      out.append('-');
    }
    while (h < cps.length) {
      int m = Integer.MAX_VALUE;
      for (int cp : cps) {
        if (cp >= n && cp < m) {
          m = cp;
        }
      }
      delta += (m - n) * (h + 1);
      n = m;
      for (int cp : cps) {
        if (cp < n) {
          delta++;
        }
        if (cp == n) {
          int q = delta;
          for (int k = kBase;; k += kBase) {
            int t = k <= bias ? kTMin : (k >= bias + kTMax ? kTMax : k - bias);
            if (q < t) {
              break;
            }
            out.append(digit(t + (q - t) % (kBase - t)));
            q = (q - t) / (kBase - t);
          }
          out.append(digit(q));
          bias = adapt(delta, h + 1, h == basic);
          delta = 0;
          h++;
        }
      }
      delta++;
      n++;
    }
    return out.toString();
  }

  private static String punyDecode(String input) {
    java.util.ArrayList<Integer> output = new java.util.ArrayList<Integer>();
    int n = kInitialN;
    int i = 0;
    int bias = kInitialBias;
    int b = input.lastIndexOf('-');
    if (b > 0) {
      for (int j = 0; j < b; j++) {
        output.add((int) input.charAt(j));
      }
    }
    for (int in = b > 0 ? b + 1 : 0; in < input.length();) {
      int oldi = i;
      int w = 1;
      for (int k = kBase;; k += kBase) {
        char c = input.charAt(in++);
        int d = c >= '0' && c <= '9' ? c - '0' + 26 : (c >= 'a' && c <= 'z' ? c - 'a' : (c >= 'A' && c <= 'Z' ? c - 'A' : -1));
        if (d < 0) {
          throw new IllegalArgumentException("bad input");
        }
        i += d * w;
        int t = k <= bias ? kTMin : (k >= bias + kTMax ? kTMax : k - bias);
        if (d < t) {
          break;
        }
        w *= kBase - t;
      }
      bias = adapt(i - oldi, output.size() + 1, oldi == 0);
      n += i / (output.size() + 1);
      i %= output.size() + 1;
      output.add(i, n);
      i++;
    }
    StringBuilder sb = new StringBuilder();
    for (int cp : output) {
      sb.appendCodePoint(cp);
    }
    return sb.toString();
  }
}
