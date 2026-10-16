/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.io.Serializable;
import java.time.zone.ZoneRules;
import java.time.zone.ZoneRulesException;
import java.time.zone.ZoneRulesProvider;
import java.util.Objects;

final class ZoneRegion extends ZoneId implements Serializable {
  private static final long serialVersionUID = 8386373296231747096L;

  private final String id;
  private final transient ZoneRules rules;

  static ZoneRegion ofId(String zoneId, boolean checkAvailable) {
    Objects.requireNonNull(zoneId, "zoneId");
    checkName(zoneId);
    ZoneRules rules = null;
    try {
      rules = ZoneRulesProvider.getRules(zoneId, true);
    } catch (ZoneRulesException ex) {
      if (checkAvailable) {
        throw ex;
      }
    }
    return new ZoneRegion(zoneId, rules);
  }

  private static void checkName(String zoneId) {
    int n = zoneId.length();
    if (n < 2) {
      throw new DateTimeException("Invalid ID for region-based ZoneId, invalid format: " + zoneId);
    }
    for (int i = 0; i < n; i++) {
      char c = zoneId.charAt(i);
      if (c >= 'a' && c <= 'z') {
        continue;
      }
      if (c == '/' && i != 0) {
        continue;
      }
      if (c >= 'A' && c <= 'Z') {
        continue;
      }
      if (c >= '0' && c <= '9' && i != 0) {
        continue;
      }
      if (c == '~' && i != 0) {
        continue;
      }
      if (c == '.' && i != 0) {
        continue;
      }
      if (c == '_' && i != 0) {
        continue;
      }
      if (c == '+' && i != 0) {
        continue;
      }
      if (c == '-' && i != 0) {
        continue;
      }
      throw new DateTimeException("Invalid ID for region-based ZoneId, invalid format: " + zoneId);
    }
  }

  ZoneRegion(String id, ZoneRules rules) {
    this.id = id;
    this.rules = rules;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public ZoneRules getRules() {
    return (rules != null ? rules : ZoneRulesProvider.getRules(id, false));
  }
}
