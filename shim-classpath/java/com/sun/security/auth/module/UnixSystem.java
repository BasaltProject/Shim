/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */
package com.sun.security.auth.module;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class UnixSystem {
  protected String username;
  protected long uid;
  protected long gid;
  protected long[] groups;

  public UnixSystem() {
    username = System.getProperty("user.name");
    try {
      List<String> lines = Files.readAllLines(Paths.get("/proc/self/status"), StandardCharsets.UTF_8);
      for (String line : lines) {
        if (line.startsWith("Uid:")) {
          uid = Long.parseLong(line.substring(4).trim().split("\\s+")[0]);
        } else if (line.startsWith("Gid:")) {
          gid = Long.parseLong(line.substring(4).trim().split("\\s+")[0]);
        } else if (line.startsWith("Groups:")) {
          String rest = line.substring(7).trim();
          if (rest.isEmpty()) {
            groups = new long[0];
          } else {
            String[] parts = rest.split("\\s+");
            groups = new long[parts.length];
            for (int i = 0; i < parts.length; i++) {
              groups[i] = Long.parseLong(parts[i]);
            }
          }
        }
      }
    } catch (IOException | RuntimeException e) {
      throw new UnsupportedOperationException("Unable to read process credentials", e);
    }
    if (groups == null) {
      groups = new long[0];
    }
  }

  public String getUsername() {
    return username;
  }

  public long getUid() {
    return uid;
  }

  public long getGid() {
    return gid;
  }

  public long[] getGroups() {
    return groups == null ? null : groups.clone();
  }
}
