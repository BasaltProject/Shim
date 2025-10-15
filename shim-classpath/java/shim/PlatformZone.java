/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

public final class PlatformZone {
  private PlatformZone() { }

  public static String detect() {
    String id = System.getenv("TZ");
    if (id != null && id.startsWith(":")) {
      id = id.substring(1);
    }
    if (id != null && !id.isEmpty()) {
      return id;
    }
    try {
      java.nio.file.Path tz = java.nio.file.Paths.get("/etc/timezone");
      if (java.nio.file.Files.isRegularFile(tz)) {
        String line = java.nio.file.Files.readString(tz).trim();
        if (!line.isEmpty()) {
          return line;
        }
      }
      java.nio.file.Path local = java.nio.file.Paths.get("/etc/localtime");
      if (java.nio.file.Files.isSymbolicLink(local)) {
        String target = java.nio.file.Files.readSymbolicLink(local).toString();
        int at = target.indexOf("zoneinfo/");
        if (at >= 0) {
          return target.substring(at + "zoneinfo/".length());
        }
      }
    } catch (java.io.IOException | RuntimeException ignored) {
    }
    return null;
  }
}
