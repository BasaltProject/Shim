/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package shim;

public final class HiddenFields {
  public static final int kHidden = -2;

  private static final String[] kFields = {
    "java.lang.String|value|[B", "java.lang.String|coder|B", "java.lang.String|hash|I",
    "java.lang.String|hashIsZero|Z", "java.util.ArrayList|elementData|[Ljava/lang/Object;",
    "java.util.ArrayList|size|I", "java.util.HashMap|table|[Ljava/util/HashMap$Node;", "java.util.HashMap|size|I",
    "java.util.HashMap|threshold|I", "java.util.HashMap|loadFactor|F", "java.lang.Integer|value|I",
    "java.lang.Long|value|J", "java.lang.Boolean|value|Z", "java.lang.Double|value|D",
    "java.lang.Thread|name|Ljava/lang/String;", "java.lang.Thread|tid|J",
  };

  private HiddenFields() {}

  public static java.lang.reflect.Field find(Class<?> c, VMClass vmClass, String name) {
    if (c.getClassLoader() != null) {
      return null;
    }
    String prefix = c.getName() + "|" + name + "|";
    for (String entry : kFields) {
      if (entry.startsWith(prefix)) {
        VMField f = new VMField();
        f.name = bytes(name);
        f.spec = bytes(entry.substring(prefix.length()));
        f.flags = (short) (java.lang.reflect.Modifier.PRIVATE | java.lang.reflect.Modifier.FINAL);
        f.nativeID = kHidden;
        f.class_ = vmClass;
        return new java.lang.reflect.Field(f);
      }
    }
    return null;
  }

  private static byte[] bytes(String s) {
    byte[] out = new byte[s.length() + 1];
    for (int i = 0; i < s.length(); i++) {
      out[i] = (byte) s.charAt(i);
    }
    return out;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Throwable> void deny(String what) throws T {
    throw (T) new IllegalAccessException("Can not access " + what);
  }
}
