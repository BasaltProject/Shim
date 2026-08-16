/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

public interface Mixer extends javax.sound.sampled.Line {
  int getMaxLines(javax.sound.sampled.Line.Info p0);
  javax.sound.sampled.Line[] getSourceLines();
  javax.sound.sampled.Line[] getTargetLines();
  void synchronize(javax.sound.sampled.Line[] p0, boolean p1);
  void unsynchronize(javax.sound.sampled.Line[] p0);
  boolean isSynchronizationSupported(javax.sound.sampled.Line[] p0, boolean p1);
  javax.sound.sampled.Mixer.Info getMixerInfo();
  javax.sound.sampled.Line.Info[] getSourceLineInfo();
  javax.sound.sampled.Line.Info[] getSourceLineInfo(javax.sound.sampled.Line.Info p0);
  javax.sound.sampled.Line.Info[] getTargetLineInfo(javax.sound.sampled.Line.Info p0);
  javax.sound.sampled.Line.Info[] getTargetLineInfo();
  boolean isLineSupported(javax.sound.sampled.Line.Info p0);
  javax.sound.sampled.Line getLine(javax.sound.sampled.Line.Info p0) throws javax.sound.sampled.LineUnavailableException;

  public static class Info {
    private final String name;
    private final String vendor;
    private final String description;
    private final String version;

    protected Info(String name, String vendor, String description, String version) {
      this.name = name;
      this.vendor = vendor;
      this.description = description;
      this.version = version;
    }

    public final boolean equals(Object obj) { return super.equals(obj); }

    public final int hashCode() { return super.hashCode(); }

    public final String getName() { return name; }

    public final String getVendor() { return vendor; }

    public final String getDescription() { return description; }

    public final String getVersion() { return version; }

    public final String toString() { return name + ", version " + version; }
  }
}
