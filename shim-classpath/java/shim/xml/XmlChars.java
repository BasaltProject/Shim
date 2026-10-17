/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

public final class XmlChars {
  private XmlChars() {
  }

  public static boolean isSpace(int c) {
    return c == 0x20 || c == 0x09 || c == 0x0A || c == 0x0D;
  }

  public static boolean isValid(int c) {
    return c == 0x09 || c == 0x0A || c == 0x0D || (c >= 0x20 && c <= 0xD7FF) || (c >= 0xE000 && c <= 0xFFFD)
        || (c >= 0x10000 && c <= 0x10FFFF);
  }

  public static boolean isNameStart(int c) {
    if (c < 0x80) {
      return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == ':';
    }
    return (c >= 0xC0 && c <= 0xD6) || (c >= 0xD8 && c <= 0xF6) || (c >= 0xF8 && c <= 0x2FF)
        || (c >= 0x370 && c <= 0x37D) || (c >= 0x37F && c <= 0x1FFF) || (c >= 0x200C && c <= 0x200D)
        || (c >= 0x2070 && c <= 0x218F) || (c >= 0x2C00 && c <= 0x2FEF) || (c >= 0x3001 && c <= 0xD7FF)
        || (c >= 0xF900 && c <= 0xFDCF) || (c >= 0xFDF0 && c <= 0xFFFD) || (c >= 0x10000 && c <= 0xEFFFF);
  }

  public static boolean isName(int c) {
    if (isNameStart(c)) {
      return true;
    }
    if (c < 0x80) {
      return (c >= '0' && c <= '9') || c == '-' || c == '.';
    }
    return c == 0xB7 || (c >= 0x300 && c <= 0x36F) || (c >= 0x203F && c <= 0x2040);
  }

  public static boolean isNmtoken(String s) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (!isName(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static boolean isXmlName(String s) {
    if (s.isEmpty() || !isNameStart(s.charAt(0))) {
      return false;
    }
    return isNmtoken(s);
  }

  public static boolean isNcName(String s) {
    return isXmlName(s) && s.indexOf(':') < 0;
  }

  public static boolean isPubidChar(int c) {
    return c == 0x20 || c == 0x0D || c == 0x0A || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
        || (c >= '0' && c <= '9') || "-'()+,./:=?;!*#@$_%".indexOf(c) >= 0;
  }
}
