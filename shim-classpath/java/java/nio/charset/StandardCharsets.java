/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.charset;

public final class StandardCharsets {
  private StandardCharsets() {}

  public static final Charset US_ASCII = new SimpleCharset("US-ASCII", new String[] {"iso-ir-6", "ANSI_X3.4-1968", "ascii"});
  public static final Charset ISO_8859_1 = new SimpleCharset("ISO-8859-1", new String[] {"iso-ir-100", "ISO_8859-1", "latin1", "l1", "IBM819", "cp819"});
  public static final Charset UTF_8 = new SimpleCharset("UTF-8", new String[] {"UTF8", "unicode-1-1-utf-8"});
  public static final Charset UTF_16BE = new SimpleCharset("UTF-16BE", new String[] {"UTF_16BE", "ISO-10646-UCS-2", "X-UTF-16BE", "UnicodeBigUnmarked"});
  public static final Charset UTF_16LE = new SimpleCharset("UTF-16LE", new String[] {"UTF_16LE", "X-UTF-16LE", "UnicodeLittleUnmarked"});
  public static final Charset UTF_16 = new SimpleCharset("UTF-16", new String[] {"UTF_16", "utf16", "unicode", "UnicodeBig"});

  private static final class SimpleCharset extends Charset {
    SimpleCharset(String name, String[] aliases) {
      super(name, aliases);
    }
  }
}
