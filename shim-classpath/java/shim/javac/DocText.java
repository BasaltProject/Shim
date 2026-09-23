/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

public final class DocText {
  private DocText() {
  }

  public static String strip(String raw) {
    if (raw.startsWith("///")) {
      StringBuilder md = new StringBuilder();
      String[] lines = raw.split("\n");
      for (int k = 0; k < lines.length; k++) {
        String l = lines[k];
        int st = l.indexOf("///");
        String body = st >= 0 ? l.substring(st + 3) : l;
        if (body.startsWith(" ")) {
          body = body.substring(1);
        }
        if (k > 0) {
          md.append('\n');
        }
        md.append(body);
      }
      return md.toString();
    }
    int n = raw.length();
    int i = 3;
    while (i < n && raw.charAt(i) == '*') {
      i++;
    }
    int end = n - 2;
    if (end < i) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    boolean firstLine = true;
    while (i < end) {
      int lineEnd = i;
      while (lineEnd < end && raw.charAt(lineEnd) != '\n' && raw.charAt(lineEnd) != '\r') {
        lineEnd++;
      }
      int s = i;
      if (!firstLine) {
        while (s < lineEnd && (raw.charAt(s) == ' ' || raw.charAt(s) == '\t' || raw.charAt(s) == '\f')) {
          s++;
        }
        if (s < lineEnd && raw.charAt(s) == '*') {
          while (s < lineEnd && raw.charAt(s) == '*') {
            s++;
          }
        } else {
          s = i;
        }
      }
      boolean blank = true;
      for (int q = s; q < lineEnd; q++) {
        if (!Character.isWhitespace(raw.charAt(q))) {
          blank = false;
          break;
        }
      }
      if (!(blank && (firstLine || lineEnd == end))) {
        sb.append(raw, s, lineEnd);
      }
      if (lineEnd < end && !(firstLine && blank)) {
        if (raw.charAt(lineEnd) == '\r' && lineEnd + 1 < end && raw.charAt(lineEnd + 1) == '\n') {
          lineEnd++;
        }
        sb.append('\n');
        lineEnd++;
      } else if (lineEnd < end) {
        if (raw.charAt(lineEnd) == '\r' && lineEnd + 1 < end && raw.charAt(lineEnd + 1) == '\n') {
          lineEnd++;
        }
        lineEnd++;
      }
      i = lineEnd;
      firstLine = false;
    }
    return sb.toString();
  }
}
