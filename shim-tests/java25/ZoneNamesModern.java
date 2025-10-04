/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.time.*; import java.time.format.*; import java.util.*;
public class ZoneNamesModern { public static void main(String[] a) {
 DateTimeFormatter z = DateTimeFormatter.ofPattern("z zzzz", Locale.US);
 DateTimeFormatter v = DateTimeFormatter.ofPattern("v vvvv", Locale.US);
 for (String id : new TreeSet<>(ZoneId.getAvailableZoneIds())) {
   ZoneId zid = ZoneId.of(id);
   System.out.println(id + "|" + zid.getDisplayName(TextStyle.FULL, Locale.US) + "|" + zid.getDisplayName(TextStyle.SHORT, Locale.US) + "|" + z.format(ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, zid)) + "|" + z.format(ZonedDateTime.of(2024, 7, 15, 12, 0, 0, 0, zid)) + "|" + v.format(ZonedDateTime.of(2024, 7, 15, 12, 0, 0, 0, zid)) + "|" + TimeZone.getTimeZone(id).getDisplayName(Locale.US) + "|" + TimeZone.getTimeZone(id).getDisplayName(true, TimeZone.SHORT, Locale.US));
 }
}}
