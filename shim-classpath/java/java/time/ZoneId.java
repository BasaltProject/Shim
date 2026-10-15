/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ZoneId {
  public static final Map<String, String> SHORT_IDS;

  static {
    Map<String, String> map = new HashMap<String, String>();
    map.put("ACT", "Australia/Darwin");
    map.put("AET", "Australia/Sydney");
    map.put("AGT", "America/Argentina/Buenos_Aires");
    map.put("ART", "Africa/Cairo");
    map.put("AST", "America/Anchorage");
    map.put("BET", "America/Sao_Paulo");
    map.put("BST", "Asia/Dhaka");
    map.put("CAT", "Africa/Harare");
    map.put("CNT", "America/St_Johns");
    map.put("CST", "America/Chicago");
    map.put("CTT", "Asia/Shanghai");
    map.put("EAT", "Africa/Addis_Ababa");
    map.put("ECT", "Europe/Paris");
    map.put("IET", "America/Indiana/Indianapolis");
    map.put("IST", "Asia/Kolkata");
    map.put("JST", "Asia/Tokyo");
    map.put("MIT", "Pacific/Apia");
    map.put("NET", "Asia/Yerevan");
    map.put("NST", "Pacific/Auckland");
    map.put("PLT", "Asia/Karachi");
    map.put("PNT", "America/Phoenix");
    map.put("PRT", "America/Puerto_Rico");
    map.put("PST", "America/Los_Angeles");
    map.put("SST", "Pacific/Guadalcanal");
    map.put("VST", "Asia/Ho_Chi_Minh");
    map.put("EST", "-05:00");
    map.put("MST", "-07:00");
    map.put("HST", "-10:00");
    SHORT_IDS = Collections.unmodifiableMap(map);
  }

  ZoneId() {
  }

  public static ZoneId systemDefault() {
    return ZoneOffset.UTC;
  }

  public static ZoneId of(String zoneId) {
    Objects.requireNonNull(zoneId, "zoneId");
    if (zoneId.equals("Z") || zoneId.startsWith("+") || zoneId.startsWith("-")) {
      return ZoneOffset.of(zoneId);
    }
    return new SimpleZoneId(zoneId);
  }

  public static ZoneId of(String zoneId, Map<String, String> aliasMap) {
    Objects.requireNonNull(zoneId, "zoneId");
    Objects.requireNonNull(aliasMap, "aliasMap");
    String name = aliasMap.get(zoneId);
    return of(name != null ? name : zoneId);
  }

  public abstract String getId();

  public abstract java.time.zone.ZoneRules getRules();

  public ZoneId normalized() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof ZoneId) {
      return getId().equals(((ZoneId) obj).getId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public String toString() {
    return getId();
  }

  private static final class SimpleZoneId extends ZoneId {
    private final String id;

    SimpleZoneId(String id) {
      this.id = id;
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public java.time.zone.ZoneRules getRules() {
      return java.time.zone.ZoneRules.of(ZoneOffset.UTC);
    }
  }
}
