/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import shim.NioNative;

final class UnixMountEntry {
  private final String name;
  private final String dir;
  private final String fstype;
  private final String opts;

  UnixMountEntry(String name, String dir, String fstype, String opts) {
    this.name = name;
    this.dir = dir;
    this.fstype = fstype;
    this.opts = opts;
  }

  String name() {
    return name;
  }

  String dir() {
    return dir;
  }

  String fstype() {
    return fstype;
  }

  boolean hasOption(String requested) {
    for (String opt : opts.split(",")) {
      if (opt.equals(requested)) {
        return true;
      }
    }
    return false;
  }

  boolean isIgnored() {
    return hasOption("ignore");
  }

  private static String unescape(String s) {
    if (s.indexOf('\\') < 0) {
      return s;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' && i + 3 < s.length() + 0 && Character.isDigit(s.charAt(i + 1))) {
        try {
          sb.append((char) Integer.parseInt(s.substring(i + 1, i + 4), 8));
          i += 3;
          continue;
        } catch (NumberFormatException ignore) {
        }
      }
      sb.append(c);
    }
    return sb.toString();
  }

  static List<UnixMountEntry> readEntries() {
    List<UnixMountEntry> list = new ArrayList<UnixMountEntry>();
    try (BufferedReader reader = new BufferedReader(new FileReader("/proc/mounts"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] f = line.split(" ");
        if (f.length >= 4) {
          list.add(new UnixMountEntry(unescape(f[0]), unescape(f[1]), f[2], f[3]));
        }
      }
    } catch (IOException ignore) {
    }
    return list;
  }
}
