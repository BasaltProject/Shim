/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.management.jfr;

public interface FlightRecorderMXBean extends java.lang.management.PlatformManagedObject {
  java.lang.String MXBEAN_NAME = "jdk.management.jfr:type=FlightRecorder";

  long cloneRecording(long l, boolean flag) throws java.lang.IllegalArgumentException;
  void closeRecording(long l) throws java.io.IOException;
  void closeStream(long l) throws java.io.IOException;
  void copyTo(long l, java.lang.String string) throws java.io.IOException;
  java.util.List<jdk.management.jfr.ConfigurationInfo> getConfigurations();
  java.util.List<jdk.management.jfr.EventTypeInfo> getEventTypes();
  java.util.Map<java.lang.String, java.lang.String> getRecordingOptions(long l) throws java.lang.IllegalArgumentException;
  java.util.Map<java.lang.String, java.lang.String> getRecordingSettings(long l) throws java.lang.IllegalArgumentException;
  java.util.List<jdk.management.jfr.RecordingInfo> getRecordings();
  long newRecording() throws java.lang.IllegalStateException;
  long openStream(long l, java.util.Map<java.lang.String, java.lang.String> map) throws java.io.IOException;
  byte[] readStream(long l) throws java.io.IOException;
  void setConfiguration(long l, java.lang.String string) throws java.lang.IllegalArgumentException;
  void setPredefinedConfiguration(long l, java.lang.String string) throws java.lang.IllegalArgumentException;
  void setRecordingOptions(long l, java.util.Map<java.lang.String, java.lang.String> map) throws java.lang.IllegalArgumentException;
  void setRecordingSettings(long l, java.util.Map<java.lang.String, java.lang.String> map) throws java.lang.IllegalArgumentException;
  void startRecording(long l) throws java.lang.IllegalStateException;
  boolean stopRecording(long l) throws java.lang.IllegalArgumentException, java.lang.IllegalStateException;
  long takeSnapshot();
}
