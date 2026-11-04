/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model;

public enum SourceVersion {
  RELEASE_0, RELEASE_1, RELEASE_2, RELEASE_3, RELEASE_4, RELEASE_5, RELEASE_6, RELEASE_7, RELEASE_8, RELEASE_9,
  RELEASE_10, RELEASE_11, RELEASE_12, RELEASE_13, RELEASE_14, RELEASE_15, RELEASE_16, RELEASE_17, RELEASE_18,
  RELEASE_19, RELEASE_20, RELEASE_21, RELEASE_22, RELEASE_23, RELEASE_24, RELEASE_25;

  public static SourceVersion latest() {
    return RELEASE_25;
  }

  public static SourceVersion latestSupported() {
    return RELEASE_25;
  }

  public static SourceVersion valueOf(java.lang.Runtime.Version rv) {
    return valueOf("RELEASE_" + rv.feature());
  }

  public java.lang.Runtime.Version runtimeVersion() {
    return ordinal() >= 6 ? java.lang.Runtime.Version.parse(Integer.toString(ordinal())) : null;
  }

  public static boolean isIdentifier(CharSequence name) {
    String id = name.toString();
    if (id.length() == 0) {
      return false;
    }
    int cp = id.codePointAt(0);
    if (!Character.isJavaIdentifierStart(cp)) {
      return false;
    }
    for (int i = Character.charCount(cp); i < id.length(); i += Character.charCount(cp)) {
      cp = id.codePointAt(i);
      if (!Character.isJavaIdentifierPart(cp)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isName(CharSequence name) {
    return isName(name, latest());
  }

  public static boolean isName(CharSequence name, SourceVersion version) {
    String id = name.toString();
    for (String s : id.split("\\.", -1)) {
      if (!isIdentifier(s) || isKeyword(s, version)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isKeyword(CharSequence s) {
    return isKeyword(s, latest());
  }

  public static boolean isKeyword(CharSequence s, SourceVersion version) {
    String id = s.toString();
    switch (id) {
      case "strictfp":
        return version.compareTo(RELEASE_2) >= 0;
      case "assert":
        return version.compareTo(RELEASE_4) >= 0;
      case "enum":
        return version.compareTo(RELEASE_5) >= 0;
      case "_":
        return version.compareTo(RELEASE_9) >= 0;
      case "public": case "protected": case "private": case "abstract": case "static": case "final":
      case "transient": case "volatile": case "synchronized": case "native": case "class": case "interface":
      case "extends": case "package": case "throws": case "implements": case "boolean": case "byte":
      case "char": case "short": case "int": case "long": case "float": case "double": case "void":
      case "if": case "else": case "try": case "catch": case "finally": case "do": case "while": case "for":
      case "continue": case "switch": case "case": case "default": case "break": case "throw": case "return":
      case "this": case "new": case "super": case "import": case "instanceof": case "goto": case "const":
      case "null": case "true": case "false":
        return true;
      default:
        return false;
    }
  }
}
