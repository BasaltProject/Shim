/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.management.jfr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;
import jdk.jfr.Configuration;
import jdk.jfr.EventType;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Recording;

final class FlightRecorderMXBeanImpl implements FlightRecorderMXBean {
  private static final List<String> OPTIONS = Arrays.asList("dumpOnExit", "duration", "name", "maxAge", "maxSize",
      "disk", "destination");
  private final Map<Long, InputStream> streams = new HashMap<Long, InputStream>();
  private final Map<Long, Integer> blockSizes = new HashMap<Long, Integer>();
  private long nextStream = 1;

  public ObjectName getObjectName() {
    try {
      return new ObjectName(MXBEAN_NAME);
    } catch (javax.management.MalformedObjectNameException e) {
      throw new InternalError(e);
    }
  }

  private Recording find(long id) {
    for (Recording r : FlightRecorder.getFlightRecorder().getRecordings()) {
      if (r.getId() == id) {
        return r;
      }
    }
    throw new IllegalArgumentException("No recording available with id " + id);
  }

  public long newRecording() throws IllegalStateException {
    return new Recording().getId();
  }

  public long takeSnapshot() {
    return FlightRecorder.getFlightRecorder().takeSnapshot().getId();
  }

  public void startRecording(long id) throws IllegalStateException {
    find(id).start();
  }

  public boolean stopRecording(long id) throws IllegalArgumentException, IllegalStateException {
    return find(id).stop();
  }

  public void closeRecording(long id) throws IOException {
    find(id).close();
  }

  public long cloneRecording(long id, boolean stop) throws IllegalArgumentException {
    return find(id).copy(stop).getId();
  }

  public void copyTo(long id, String outputFile) throws IOException {
    find(id).dump(Path.of(outputFile));
  }

  public long openStream(long id, Map<String, String> options) throws IOException {
    Recording r = find(id);
    Instant from = null;
    Instant to = null;
    int block = 50000;
    if (options != null) {
      if (options.get("startTime") != null) {
        from = Instant.parse(options.get("startTime"));
      }
      if (options.get("endTime") != null) {
        to = Instant.parse(options.get("endTime"));
      }
      if (options.get("blockSize") != null) {
        block = Integer.parseInt(options.get("blockSize"));
      }
    }
    InputStream in = r.getStream(from, to);
    if (in == null) {
      throw new IOException("No recording data available");
    }
    synchronized (streams) {
      long sid = nextStream++;
      streams.put(sid, in);
      blockSizes.put(sid, block);
      return sid;
    }
  }

  public void closeStream(long streamIdentifier) throws IOException {
    InputStream in;
    synchronized (streams) {
      in = streams.remove(streamIdentifier);
      blockSizes.remove(streamIdentifier);
    }
    if (in == null) {
      throw new IllegalArgumentException("Unknown stream identifier " + streamIdentifier);
    }
    in.close();
  }

  public byte[] readStream(long streamIdentifier) throws IOException {
    InputStream in;
    int block;
    synchronized (streams) {
      in = streams.get(streamIdentifier);
      Integer b = blockSizes.get(streamIdentifier);
      block = b == null ? 50000 : b;
    }
    if (in == null) {
      throw new IOException("Stream with id " + streamIdentifier + " is closed");
    }
    byte[] buf = new byte[block];
    int n = in.read(buf);
    if (n <= 0) {
      return null;
    }
    return n == buf.length ? buf : Arrays.copyOf(buf, n);
  }

  public Map<String, String> getRecordingOptions(long id) throws IllegalArgumentException {
    Recording r = find(id);
    Map<String, String> m = new LinkedHashMap<String, String>();
    m.put("name", r.getName());
    m.put("dumpOnExit", String.valueOf(r.getDumpOnExit()));
    m.put("disk", String.valueOf(r.isToDisk()));
    m.put("maxSize", String.valueOf(r.getMaxSize()));
    m.put("maxAge", r.getMaxAge() == null ? "0" : format(r.getMaxAge()));
    m.put("duration", r.getDuration() == null ? "0" : format(r.getDuration()));
    m.put("destination", r.getDestination() == null ? null : r.getDestination().toString());
    return m;
  }

  private static String format(Duration d) {
    long ns = d.toNanos();
    if (ns == 0) {
      return "0";
    }
    String[] unit = {"ns", "us", "ms", "s", "m", "h", "d"};
    long[] div = {1, 1000L, 1000000L, 1000000000L, 60000000000L, 3600000000000L, 86400000000000L};
    for (int i = div.length - 1; i >= 0; i--) {
      if (ns % div[i] == 0) {
        return (ns / div[i]) + " " + unit[i];
      }
    }
    return ns + " ns";
  }

  private static Duration parse(String name, String v) {
    try {
      String t = v.trim();
      if (t.equals("0")) {
        return null;
      }
      int i = 0;
      while (i < t.length() && Character.isDigit(t.charAt(i))) {
        i++;
      }
      long n = Long.parseLong(t.substring(0, i));
      String u = t.substring(i).trim();
      switch (u) {
        case "ns":
          return Duration.ofNanos(n);
        case "us":
          return Duration.ofNanos(n * 1000L);
        case "ms":
          return Duration.ofMillis(n);
        case "s":
          return Duration.ofSeconds(n);
        case "m":
          return Duration.ofMinutes(n);
        case "h":
          return Duration.ofHours(n);
        case "d":
          return Duration.ofDays(n);
        default:
          throw new NumberFormatException("Unknown unit " + u);
      }
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Not a valid value for option '" + name + "'. " + e.getMessage());
    }
  }

  public void setRecordingOptions(long id, Map<String, String> options) throws IllegalArgumentException {
    Recording r = find(id);
    java.util.Objects.requireNonNull(options);
    for (String k : options.keySet()) {
      if (!OPTIONS.contains(k)) {
        throw new IllegalArgumentException("Unknown recording option: " + k + ". Valid options are " + OPTIONS + ".");
      }
    }
    for (Map.Entry<String, String> e : options.entrySet()) {
      String k = e.getKey();
      String v = e.getValue();
      switch (k) {
        case "name":
          r.setName(v);
          break;
        case "dumpOnExit":
          r.setDumpOnExit(Boolean.parseBoolean(v));
          break;
        case "disk":
          r.setToDisk(Boolean.parseBoolean(v));
          break;
        case "maxSize":
          try {
            r.setMaxSize(Long.parseLong(v));
          } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Not a valid value for option 'maxSize'. " + ex.getMessage());
          }
          break;
        case "maxAge":
          r.setMaxAge(v == null ? null : parse(k, v));
          break;
        case "duration":
          r.setDuration(v == null ? null : parse(k, v));
          break;
        default:
          try {
            r.setDestination(v == null ? null : Path.of(v));
          } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage());
          }
          break;
      }
    }
  }

  public Map<String, String> getRecordingSettings(long id) throws IllegalArgumentException {
    return find(id).getSettings();
  }

  public void setRecordingSettings(long id, Map<String, String> settings) throws IllegalArgumentException {
    find(id).setSettings(settings);
  }

  public void setConfiguration(long id, String contents) throws IllegalArgumentException {
    Recording r = find(id);
    try {
      r.setSettings(Configuration.create(new StringReader(contents)).getSettings());
    } catch (IOException | ParseException e) {
      throw new IllegalArgumentException("Could not parse configuration: " + e.getMessage());
    }
  }

  public void setPredefinedConfiguration(long id, String configurationName) throws IllegalArgumentException {
    Recording r = find(id);
    try {
      r.setSettings(Configuration.getConfiguration(configurationName).getSettings());
    } catch (IOException | ParseException e) {
      throw new IllegalArgumentException("Could not find configuration with name " + configurationName);
    }
  }

  public List<RecordingInfo> getRecordings() {
    List<RecordingInfo> l = new ArrayList<RecordingInfo>();
    for (Recording r : FlightRecorder.getFlightRecorder().getRecordings()) {
      l.add(new RecordingInfo(r));
    }
    return l;
  }

  public List<ConfigurationInfo> getConfigurations() {
    List<ConfigurationInfo> l = new ArrayList<ConfigurationInfo>();
    for (Configuration c : Configuration.getConfigurations()) {
      l.add(new ConfigurationInfo(c));
    }
    return l;
  }

  public List<EventTypeInfo> getEventTypes() {
    List<EventTypeInfo> l = new ArrayList<EventTypeInfo>();
    for (EventType t : FlightRecorder.getFlightRecorder().getEventTypes()) {
      l.add(new EventTypeInfo(t));
    }
    return l;
  }
}
