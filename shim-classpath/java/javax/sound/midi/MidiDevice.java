/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public interface MidiDevice extends java.lang.AutoCloseable {
  boolean isOpen();
  void close();
  void open() throws javax.sound.midi.MidiUnavailableException;
  javax.sound.midi.MidiDevice.Info getDeviceInfo();
  long getMicrosecondPosition();
  int getMaxReceivers();
  int getMaxTransmitters();
  javax.sound.midi.Receiver getReceiver() throws javax.sound.midi.MidiUnavailableException;
  java.util.List<javax.sound.midi.Receiver> getReceivers();
  javax.sound.midi.Transmitter getTransmitter() throws javax.sound.midi.MidiUnavailableException;
  java.util.List<javax.sound.midi.Transmitter> getTransmitters();

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

    public final String toString() { return name; }
  }
}
