/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.time.*;
import java.time.zone.*;
import java.util.*;

public class ZoneRulesModern {
  static long mix(long h, long v) {
    return h * 1099511628211L + v;
  }

  static long hash(ZoneRules r) {
    long h = 7;
    long[] instants = {Long.MIN_VALUE / 1000, -10000000000L, -3000000000L, -2200000000L, -1700000000L, -1000000000L, -500000000L, 0, 100000000L, 300000000L, 500000000L, 700000000L, 900000000L, 1100000000L, 1300000000L, 1500000000L, 1700000000L, 1800000000L, 2000000000L, 3000000000L, 5000000000L, 20000000000L};
    for (long t : instants) {
      Instant i = Instant.ofEpochSecond(t);
      h = mix(h, r.getOffset(i).getTotalSeconds());
      h = mix(h, r.getStandardOffset(i).getTotalSeconds());
      h = mix(h, r.getDaylightSavings(i).getSeconds());
      h = mix(h, r.isDaylightSavings(i) ? 1 : 0);
      ZoneOffsetTransition n = r.nextTransition(i);
      h = mix(h, n == null ? 0 : n.toEpochSecond());
      ZoneOffsetTransition p = r.previousTransition(i);
      h = mix(h, p == null ? 0 : p.toEpochSecond());
      LocalDateTime l = LocalDateTime.ofEpochSecond(t, 0, ZoneOffset.UTC);
      h = mix(h, r.getValidOffsets(l).size());
      h = mix(h, r.getOffset(l).getTotalSeconds());
      ZoneOffsetTransition tr = r.getTransition(l);
      h = mix(h, tr == null ? 0 : tr.toEpochSecond());
    }
    h = mix(h, r.getTransitions().size());
    for (ZoneOffsetTransition t : r.getTransitions()) {
      h = mix(h, t.toEpochSecond());
      h = mix(h, t.getOffsetBefore().getTotalSeconds());
      h = mix(h, t.getOffsetAfter().getTotalSeconds());
      h = mix(h, t.isGap() ? 1 : 0);
    }
    h = mix(h, r.getTransitionRules().size());
    for (ZoneOffsetTransitionRule x : r.getTransitionRules()) {
      h = mix(h, x.toString().hashCode());
    }
    h = mix(h, r.isFixedOffset() ? 1 : 0);
    h = mix(h, r.toString().hashCode());
    return h;
  }

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.US);
    TreeSet<String> ids = new TreeSet<>(ZoneId.getAvailableZoneIds());
    System.out.println("count=" + ids.size());
    System.out.println("version=" + ZoneRulesProvider.getVersions("UTC").firstKey());
    long all = 0;
    for (String id : ids) {
      long h = hash(ZoneId.of(id).getRules());
      all = mix(all, h);
      System.out.println(id + "=" + Long.toHexString(h));
    }
    System.out.println("all=" + Long.toHexString(all));
    String[] zones = {"America/New_York", "Europe/London", "Europe/Paris", "Asia/Kolkata", "Australia/Lord_Howe", "Pacific/Apia", "America/Sao_Paulo", "Europe/Moscow", "Asia/Tehran"};
    for (String z : zones) {
      ZoneRules r = ZoneId.of(z).getRules();
      System.out.println(z + " " + r + " " + r.getTransitions().size() + " " + r.getTransitionRules());
      System.out.println(z + " " + ZonedDateTime.of(LocalDateTime.of(1850, 1, 1, 12, 0), ZoneId.of(z)) + " " + ZonedDateTime.of(LocalDateTime.of(2024, 3, 10, 2, 30), ZoneId.of(z)) + " " + ZonedDateTime.of(LocalDateTime.of(2024, 11, 3, 1, 30), ZoneId.of(z)) + " " + ZonedDateTime.of(LocalDateTime.of(1930, 6, 1, 12, 0), ZoneId.of(z)));
    }
    System.out.println(ZoneId.of("Z") + " " + ZoneId.of("UTC+05:30").getRules() + " " + ZoneId.of("GMT").getRules());
    System.out.println(ZoneId.of("EST", ZoneId.SHORT_IDS).getRules() + " " + ZoneId.of("PST", ZoneId.SHORT_IDS));
    System.out.println(ZoneRulesProvider.getAvailableZoneIds().size());
    try { ZoneId.of("Nope/Zone"); } catch (Exception e) { System.out.println(e.getClass().getName() + ": " + e.getMessage()); }
  }
}
