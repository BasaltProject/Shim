/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Objects;

public class URLEncoder {
  private URLEncoder() {
  }

  @Deprecated
  public static String encode(String s) {
    return encode(s, StandardCharsets.UTF_8);
  }

  public static String encode(String s, String enc) throws UnsupportedEncodingException {
    if (enc == null) {
      throw new NullPointerException("charsetName");
    }
    try {
      Charset charset = Charset.forName(enc);
      return encode(s, charset);
    } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
      throw new UnsupportedEncodingException(enc);
    }
  }

  public static String encode(String s, Charset charset) {
    Objects.requireNonNull(s, "s");
    Objects.requireNonNull(charset, "charset");

    StringBuilder out = new StringBuilder(s.length());
    int i = 0;
    while (i < s.length()) {
      char c = s.charAt(i);
      if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
          || c == '-' || c == '_' || c == '.' || c == '*') {
        out.append(c);
        i++;
      } else if (c == ' ') {
        out.append('+');
        i++;
      } else {
        int start = i;
        do {
          if (c >= 0xD800 && c <= 0xDBFF && i + 1 < s.length()) {
            char d = s.charAt(i + 1);
            if (d >= 0xDC00 && d <= 0xDFFF) {
              i++;
            }
          }
          i++;
        } while (i < s.length() && !isUnreserved((c = s.charAt(i))));

        byte[] ba = s.substring(start, i).getBytes(charset);
        for (byte b : ba) {
          out.append('%');
          char ch1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
          char ch2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
          out.append(ch1).append(ch2);
        }
      }
    }
    return out.toString();
  }

  private static boolean isUnreserved(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
        || c == ' ' || c == '-' || c == '_' || c == '.' || c == '*';
  }
}
