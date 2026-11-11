/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.util;

import java.security.PEMRecord;

public final class Pem {
  private static final String kLineSeparator = "\r\n";

  private Pem() {
  }

  public static String pemEncoded(PEMRecord record) {
    return format(record.type(), record.content());
  }

  public static String format(String type, String content) {
    StringBuilder sb = new StringBuilder();
    sb.append("-----BEGIN ").append(type).append("-----").append(kLineSeparator);
    if (content.isEmpty()) {
      sb.append(kLineSeparator);
    }
    for (int i = 0; i < content.length(); i += 64) {
      String chunk = content.substring(i, Math.min(content.length(), i + 64));
      sb.append(chunk);
      if (!chunk.endsWith("\n")) {
        sb.append(kLineSeparator);
      }
    }
    sb.append("-----END ").append(type).append("-----").append(kLineSeparator);
    return sb.toString();
  }
}
