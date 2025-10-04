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

public class URLDecoder {
  private URLDecoder() {
  }

  @Deprecated
  public static String decode(String s) {
    return decode(s, StandardCharsets.UTF_8);
  }

  public static String decode(String s, String enc) throws UnsupportedEncodingException {
    if (enc == null) {
      throw new NullPointerException("charsetName");
    }
    try {
      Charset charset = Charset.forName(enc);
      return decode(s, charset);
    } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
      throw new UnsupportedEncodingException(enc);
    }
  }

  public static String decode(String s, Charset charset) {
    Objects.requireNonNull(s, "s");
    Objects.requireNonNull(charset, "charset");

    int numChars = s.length();
    StringBuilder sb = new StringBuilder(numChars);
    int i = 0;
    byte[] bytes = null;
    while (i < numChars) {
      char c = s.charAt(i);
      switch (c) {
        case '+':
          sb.append(' ');
          i++;
          break;
        case '%':
          if (bytes == null) {
            bytes = new byte[(numChars - i) / 3 + 1];
          }
          int pos = 0;
          while (((i + 2) < numChars) && (c == '%')) {
            try {
              int v = java.util.HexFormat.fromHexDigits(s, i + 1, i + 3);
              bytes[pos++] = (byte) v;
            } catch (NumberFormatException e) {
              throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - "
                  + e.getMessage());
            }
            i += 3;
            if (i < numChars) {
              c = s.charAt(i);
            }
          }
          if ((i < numChars) && (c == '%')) {
            throw new IllegalArgumentException("URLDecoder: Incomplete trailing escape (%) pattern");
          }
          sb.append(new String(bytes, 0, pos, charset));
          break;
        default:
          sb.append(c);
          i++;
          break;
      }
    }
    return sb.toString();
  }
}
