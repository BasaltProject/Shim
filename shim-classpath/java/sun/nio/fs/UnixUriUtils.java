/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;

final class UnixUriUtils {
  private UnixUriUtils() { }

  static Path fromUri(UnixFileSystem fs, URI uri) {
    if (!uri.isAbsolute()) {
      throw new IllegalArgumentException("URI is not absolute");
    }
    if (uri.isOpaque()) {
      throw new IllegalArgumentException("URI is not hierarchical");
    }
    String scheme = uri.getScheme();
    if ((scheme == null) || !scheme.equalsIgnoreCase("file")) {
      throw new IllegalArgumentException("URI scheme is not \"file\"");
    }
    if (uri.getRawAuthority() != null) {
      throw new IllegalArgumentException("URI has an authority component");
    }
    if (uri.getRawFragment() != null) {
      throw new IllegalArgumentException("URI has a fragment component");
    }
    if (uri.getRawQuery() != null) {
      throw new IllegalArgumentException("URI has a query component");
    }
    if (!uri.toString().startsWith("file:///")) {
      return new File(uri).toPath();
    }
    String p = uri.getRawPath();
    int len = p.length();
    if (len == 0) {
      throw new IllegalArgumentException("URI path component is empty");
    }
    if (p.endsWith("/") && len > 1) {
      len--;
    }
    byte[] result = new byte[len];
    int rlen = 0;
    int pos = 0;
    while (pos < len) {
      char c = p.charAt(pos++);
      byte b;
      if (c == '%') {
        char c1 = p.charAt(pos++);
        char c2 = p.charAt(pos++);
        b = (byte) ((decode(c1) << 4) | decode(c2));
        if (b == 0) {
          throw new IllegalArgumentException("Nul character not allowed");
        }
      } else {
        if (c == 0 || c >= 0x80) {
          throw new IllegalArgumentException("Bad escape");
        }
        b = (byte) c;
      }
      result[rlen++] = b;
    }
    if (rlen != result.length) {
      result = Arrays.copyOf(result, rlen);
    }
    return new UnixPath(fs, result);
  }

  static URI toUri(UnixPath up) {
    byte[] path = up.toAbsolutePath().asByteArray();
    StringBuilder sb = new StringBuilder("file:///");
    for (int i = 1; i < path.length; i++) {
      char c = (char) (path[i] & 0xff);
      if (isPathChar(c)) {
        sb.append(c);
      } else {
        sb.append('%');
        sb.append(kHexDigits[(c >> 4) & 0x0f]);
        sb.append(kHexDigits[(c) & 0x0f]);
      }
    }
    if (sb.charAt(sb.length() - 1) != '/') {
      try {
        UnixFileAttributes attrs = UnixFileAttributes.getIfExists(up);
        if (attrs != null && ((attrs.mode() & UnixConstants.S_IFMT) == UnixConstants.S_IFDIR)) {
          sb.append('/');
        }
      } catch (UnixException x) {
      }
    }
    try {
      return new URI(sb.toString());
    } catch (URISyntaxException x) {
      throw new AssertionError(x);
    }
  }

  private static final char[] kHexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
      'F'};

  private static boolean isPathChar(char c) {
    if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') {
      return true;
    }
    return "-_.!~*'():@&=+$,;/".indexOf(c) >= 0;
  }

  private static int decode(char c) {
    if ((c >= '0') && (c <= '9')) {
      return c - '0';
    }
    if ((c >= 'a') && (c <= 'f')) {
      return c - 'a' + 10;
    }
    if ((c >= 'A') && (c <= 'F')) {
      return c - 'A' + 10;
    }
    throw new AssertionError();
  }
}
