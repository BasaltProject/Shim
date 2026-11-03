/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.management.jfr;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import jdk.jfr.Recording;

public final class RecordingInfo {
  private final long id;
  private final String name;
  private final String state;
  private final boolean dumpOnExit;
  private final long size;
  private final boolean disk;
  private final long maxAge;
  private final long maxSize;
  private final long startTime;
  private final long stopTime;
  private final String destination;
  private final long duration;
  private final Map<String, String> settings;

  RecordingInfo(Recording r) {
    this.id = r.getId();
    this.name = r.getName();
    this.state = r.getState().toString();
    this.dumpOnExit = r.getDumpOnExit();
    this.size = r.getSize();
    this.disk = r.isToDisk();
    Duration age = r.getMaxAge();
    this.maxAge = age == null ? 0 : age.getSeconds();
    this.maxSize = r.getMaxSize();
    this.startTime = r.getStartTime() == null ? 0 : r.getStartTime().toEpochMilli();
    this.stopTime = r.getStopTime() == null ? 0 : r.getStopTime().toEpochMilli();
    this.destination = r.getDestination() == null ? null : r.getDestination().toString();
    Duration d = r.getDuration();
    this.duration = d == null ? 0 : d.getSeconds();
    this.settings = r.getSettings();
  }

  private RecordingInfo(CompositeData d) {
    this.id = (Long) d.get("id");
    this.name = (String) d.get("name");
    this.state = (String) d.get("state");
    this.dumpOnExit = (Boolean) d.get("dumpOnExit");
    this.size = (Long) d.get("size");
    this.disk = (Boolean) d.get("toDisk");
    this.maxAge = (Long) d.get("maxAge");
    this.maxSize = (Long) d.get("maxSize");
    this.startTime = (Long) d.get("startTime");
    this.stopTime = (Long) d.get("stopTime");
    this.destination = (String) d.get("destination");
    this.duration = (Long) d.get("duration");
    this.settings = new LinkedHashMap<String, String>();
    Object s = d.get("settings");
    if (s instanceof TabularData) {
      for (Object row : ((TabularData) s).values()) {
        CompositeData cd = (CompositeData) row;
        settings.put((String) cd.get("key"), (String) cd.get("value"));
      }
    }
  }

  public static RecordingInfo from(CompositeData compositeData) {
    return compositeData == null ? null : new RecordingInfo(compositeData);
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getState() {
    return state;
  }

  public boolean getDumpOnExit() {
    return dumpOnExit;
  }

  public long getSize() {
    return size;
  }

  public boolean isToDisk() {
    return disk;
  }

  public long getMaxAge() {
    return maxAge;
  }

  public long getMaxSize() {
    return maxSize;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getStopTime() {
    return stopTime;
  }

  public String getDestination() {
    return destination;
  }

  public long getDuration() {
    return duration;
  }

  public Map<String, String> getSettings() {
    return settings;
  }

  public String toString() {
    return "name=\"" + name + "\" id=" + id + " maxAge=" + maxAge + " maxSize=" + maxSize;
  }
}
