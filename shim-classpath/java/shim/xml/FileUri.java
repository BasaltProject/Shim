/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.io.File;

public final class FileUri {
  private FileUri() {
  }

  public static String fromPath(String path) {
    StringBuilder sb = new StringBuilder("file://");
    String p = path.replace(File.separatorChar, '/');
    if (!p.startsWith("/")) {
      sb.append('/');
    }
    byte[] bytes = p.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    for (byte b : bytes) {
      int c = b & 0xff;
      if (c >= 0x80 || c <= 0x20 || "\"<>#%{}|\\^[]`".indexOf(c) >= 0) {
        sb.append('%');
        sb.append(Character.toUpperCase(Character.forDigit(c >> 4, 16)));
        sb.append(Character.toUpperCase(Character.forDigit(c & 15, 16)));
      } else {
        sb.append((char) c);
      }
    }
    return sb.toString();
  }
}
