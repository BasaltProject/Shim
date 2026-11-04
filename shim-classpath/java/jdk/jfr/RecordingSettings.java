/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

final class RecordingSettings extends EventSettings {
  private final Recording recording;
  private final String identifier;

  RecordingSettings(Recording recording, String identifier) {
    this.recording = recording;
    this.identifier = identifier;
  }

  public EventSettings with(String name, String value) {
    java.util.Objects.requireNonNull(name);
    java.util.Objects.requireNonNull(value);
    recording.putSetting(identifier + "#" + name, value);
    return this;
  }
}
