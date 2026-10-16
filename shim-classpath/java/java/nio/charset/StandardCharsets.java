/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

public final class StandardCharsets {
  private StandardCharsets() { }

  public static final Charset US_ASCII = sun.nio.cs.US_ASCII.INSTANCE;
  public static final Charset ISO_8859_1 = sun.nio.cs.ISO_8859_1.INSTANCE;
  public static final Charset UTF_8 = sun.nio.cs.UTF_8.INSTANCE;
  public static final Charset UTF_16BE = sun.nio.cs.UnicodeCharset.UTF_16BE;
  public static final Charset UTF_16LE = sun.nio.cs.UnicodeCharset.UTF_16LE;
  public static final Charset UTF_16 = sun.nio.cs.UnicodeCharset.UTF_16;
}
