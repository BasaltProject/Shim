/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.zone;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransitionRule.TimeDefinition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

final class BuiltInZoneRulesProvider extends ZoneRulesProvider {
  private final byte[] data;
  private final Map<String, Integer> regionBlob = new TreeMap<String, Integer>();
  private int[] blobOffsets;
  private final Map<Integer, ZoneRules> blobCache = new ConcurrentHashMap<Integer, ZoneRules>();

  private int pos;

  BuiltInZoneRulesProvider() {
    StringBuilder all = new StringBuilder();
    for (String chunk : ZoneRulesData.chunks()) {
      all.append(chunk);
    }
    data = new byte[all.length()];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) all.charAt(i);
    }
    readIndex();
  }

  private long readVarLong() {
    long z = 0;
    int shift = 0;
    int b;
    do {
      b = data[pos++] & 0xFF;
      z |= (long) (b & 0x7F) << shift;
      shift += 7;
    } while ((b & 0x80) != 0);
    return (z >>> 1) ^ -(z & 1);
  }

  private int readVarInt() {
    return (int) readVarLong();
  }

  private void readIndex() {
    pos = 0;
    int regions = readVarInt();
    for (int i = 0; i < regions; i++) {
      int len = readVarInt();
      String id = new String(data, pos, len, java.nio.charset.StandardCharsets.UTF_8);
      pos += len;
      regionBlob.put(id, readVarInt());
    }
    int blobs = readVarInt();
    blobOffsets = new int[blobs];
    for (int i = 0; i < blobs; i++) {
      int len = readVarInt();
      blobOffsets[i] = pos;
      pos += len;
    }
  }

  @Override
  protected Set<String> provideZoneIds() {
    return new HashSet<String>(regionBlob.keySet());
  }

  @Override
  protected ZoneRules provideRules(String zoneId, boolean forCaching) {
    Integer blob = regionBlob.get(zoneId);
    if (blob == null) {
      throw new ZoneRulesException("Unknown time-zone ID: " + zoneId);
    }
    ZoneRules rules = blobCache.get(blob);
    if (rules == null) {
      synchronized (this) {
        rules = blobCache.get(blob);
        if (rules == null) {
          rules = decode(blobOffsets[blob]);
          blobCache.put(blob, rules);
        }
      }
    }
    return rules;
  }

  @Override
  protected NavigableMap<String, ZoneRules> provideVersions(String zoneId) {
    TreeMap<String, ZoneRules> map = new TreeMap<String, ZoneRules>();
    map.put(ZoneRulesData.kVersion, provideRules(zoneId, false));
    return map;
  }

  private ZoneRules decode(int offset) {
    pos = offset;
    int nStd = readVarInt();
    long[] stdEpoch = new long[nStd];
    long prev = 0;
    for (int i = 0; i < nStd; i++) {
      prev += readVarLong();
      stdEpoch[i] = prev;
    }
    ZoneOffset[] stdOffsets = new ZoneOffset[nStd + 1];
    for (int i = 0; i <= nStd; i++) {
      stdOffsets[i] = ZoneOffset.ofTotalSeconds(readVarInt());
    }
    int nSav = readVarInt();
    long[] savEpoch = new long[nSav];
    prev = 0;
    for (int i = 0; i < nSav; i++) {
      prev += readVarLong();
      savEpoch[i] = prev;
    }
    ZoneOffset[] wallOffsets = new ZoneOffset[nSav + 1];
    for (int i = 0; i <= nSav; i++) {
      wallOffsets[i] = ZoneOffset.ofTotalSeconds(readVarInt());
    }
    int nRules = readVarInt();
    List<ZoneOffsetTransitionRule> lastRules = new ArrayList<ZoneOffsetTransitionRule>();
    for (int i = 0; i < nRules; i++) {
      Month month = Month.of(readVarInt());
      int dom = readVarInt();
      int dow = readVarInt();
      int secs = readVarInt();
      boolean midnightEnd = readVarInt() == 1;
      TimeDefinition def = TimeDefinition.values()[readVarInt()];
      ZoneOffset std = ZoneOffset.ofTotalSeconds(readVarInt());
      ZoneOffset before = ZoneOffset.ofTotalSeconds(readVarInt());
      ZoneOffset after = ZoneOffset.ofTotalSeconds(readVarInt());
      LocalTime time = midnightEnd ? LocalTime.MIDNIGHT : LocalTime.ofSecondOfDay(secs);
      lastRules.add(ZoneOffsetTransitionRule.of(month, dom, dow == 0 ? null : DayOfWeek.of(dow), time, midnightEnd,
          def, std, before, after));
    }
    List<ZoneOffsetTransition> stdList = new ArrayList<ZoneOffsetTransition>();
    for (int i = 0; i < nStd; i++) {
      stdList.add(new ZoneOffsetTransition(stdEpoch[i], stdOffsets[i], stdOffsets[i + 1]));
    }
    List<ZoneOffsetTransition> savList = new ArrayList<ZoneOffsetTransition>();
    for (int i = 0; i < nSav; i++) {
      savList.add(new ZoneOffsetTransition(savEpoch[i], wallOffsets[i], wallOffsets[i + 1]));
    }
    if (nStd == 0 && nSav == 0 && nRules == 0) {
      return ZoneRules.of(wallOffsets[0]);
    }
    return ZoneRules.of(stdOffsets[0], wallOffsets[0], stdList, savList, lastRules);
  }
}
