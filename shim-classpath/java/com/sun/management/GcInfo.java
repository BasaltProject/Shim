/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.management;

import java.lang.management.MemoryUsage;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

public class GcInfo implements CompositeData {
  private final long index;
  private final long startTime;
  private final long endTime;
  private final Map<String, MemoryUsage> usageBeforeGc;
  private final Map<String, MemoryUsage> usageAfterGc;
  private final CompositeData data;

  GcInfo(long index, long startTime, long endTime, Map<String, MemoryUsage> before, Map<String, MemoryUsage> after,
      CompositeData data) {
    this.index = index;
    this.startTime = startTime;
    this.endTime = endTime;
    this.usageBeforeGc = before;
    this.usageAfterGc = after;
    this.data = data;
  }

  public long getId() {
    return index;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public long getDuration() {
    return endTime - startTime;
  }

  public Map<String, MemoryUsage> getMemoryUsageBeforeGc() {
    return usageBeforeGc;
  }

  public Map<String, MemoryUsage> getMemoryUsageAfterGc() {
    return usageAfterGc;
  }

  public static GcInfo from(CompositeData cd) {
    return cd instanceof GcInfo ? (GcInfo) cd : null;
  }

  @Override
  public CompositeType getCompositeType() {
    return data.getCompositeType();
  }

  @Override
  public Object get(String key) {
    return data.get(key);
  }

  @Override
  public Object[] getAll(String[] keys) {
    return data.getAll(keys);
  }

  @Override
  public boolean containsKey(String key) {
    return data.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return data.containsValue(value);
  }

  @Override
  public Collection<?> values() {
    return data.values();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof GcInfo && data.equals(((GcInfo) obj).data);
  }

  @Override
  public int hashCode() {
    return data.hashCode();
  }

  @Override
  public String toString() {
    return data.toString();
  }
}
