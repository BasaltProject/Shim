/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.zone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ZoneRulesProvider {
  private static final CopyOnWriteArrayList<ZoneRulesProvider> PROVIDERS =
      new CopyOnWriteArrayList<ZoneRulesProvider>();
  private static final ConcurrentMap<String, ZoneRulesProvider> ZONES =
      new ConcurrentHashMap<String, ZoneRulesProvider>(512, 0.75f, 2);
  private static volatile Set<String> ZONE_IDS = Collections.emptySet();

  static {
    ZoneRulesProvider builtIn = new BuiltInZoneRulesProvider();
    registerProvider0(builtIn);
    PROVIDERS.add(builtIn);
  }

  public static Set<String> getAvailableZoneIds() {
    return ZONE_IDS;
  }

  public static ZoneRules getRules(String zoneId, boolean forCaching) {
    Objects.requireNonNull(zoneId, "zoneId");
    return getProvider(zoneId).provideRules(zoneId, forCaching);
  }

  public static NavigableMap<String, ZoneRules> getVersions(String zoneId) {
    Objects.requireNonNull(zoneId, "zoneId");
    return getProvider(zoneId).provideVersions(zoneId);
  }

  private static ZoneRulesProvider getProvider(String zoneId) {
    ZoneRulesProvider provider = ZONES.get(zoneId);
    if (provider == null) {
      if (ZONES.isEmpty()) {
        throw new ZoneRulesException("No time-zone data files registered");
      }
      throw new ZoneRulesException("Unknown time-zone ID: " + zoneId);
    }
    return provider;
  }

  public static void registerProvider(ZoneRulesProvider provider) {
    Objects.requireNonNull(provider, "provider");
    registerProvider0(provider);
    PROVIDERS.add(provider);
  }

  private static synchronized void registerProvider0(ZoneRulesProvider provider) {
    for (String zoneId : provider.provideZoneIds()) {
      Objects.requireNonNull(zoneId, "zoneId");
      ZoneRulesProvider old = ZONES.putIfAbsent(zoneId, provider);
      if (old != null) {
        throw new ZoneRulesException("Unable to register zone as one already registered with that ID: " + zoneId
            + ", currently loading from provider: " + provider);
      }
    }
    ZONE_IDS = Collections.unmodifiableSet(new HashSet<String>(ZONES.keySet()));
  }

  public static boolean refresh() {
    boolean changed = false;
    for (ZoneRulesProvider provider : PROVIDERS) {
      changed |= provider.provideRefresh();
    }
    return changed;
  }

  protected ZoneRulesProvider() { }

  protected abstract Set<String> provideZoneIds();

  protected abstract ZoneRules provideRules(String zoneId, boolean forCaching);

  protected abstract NavigableMap<String, ZoneRules> provideVersions(String zoneId);

  protected boolean provideRefresh() {
    return false;
  }
}
