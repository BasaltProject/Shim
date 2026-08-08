/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public interface Sequencer extends javax.sound.midi.MidiDevice {
  int LOOP_CONTINUOUSLY = -1;
  void start();
  void stop();
  long getMicrosecondPosition();
  long getMicrosecondLength();
  javax.sound.midi.Sequence getSequence();
  long getTickLength();
  void setSequence(javax.sound.midi.Sequence p0) throws javax.sound.midi.InvalidMidiDataException;
  void setSequence(java.io.InputStream p0) throws java.io.IOException, javax.sound.midi.InvalidMidiDataException;
  void startRecording();
  void stopRecording();
  boolean isRecording();
  void recordEnable(javax.sound.midi.Track p0, int p1);
  void recordDisable(javax.sound.midi.Track p0);
  float getTempoInBPM();
  void setTempoInBPM(float p0);
  float getTempoInMPQ();
  void setTempoInMPQ(float p0);
  void setTempoFactor(float p0);
  float getTempoFactor();
  long getTickPosition();
  void setTickPosition(long p0);
  void setMicrosecondPosition(long p0);
  void setMasterSyncMode(javax.sound.midi.Sequencer.SyncMode p0);
  javax.sound.midi.Sequencer.SyncMode getMasterSyncMode();
  javax.sound.midi.Sequencer.SyncMode[] getMasterSyncModes();
  void setSlaveSyncMode(javax.sound.midi.Sequencer.SyncMode p0);
  javax.sound.midi.Sequencer.SyncMode getSlaveSyncMode();
  javax.sound.midi.Sequencer.SyncMode[] getSlaveSyncModes();
  void setTrackMute(int p0, boolean p1);
  boolean getTrackMute(int p0);
  void setTrackSolo(int p0, boolean p1);
  boolean getTrackSolo(int p0);
  boolean addMetaEventListener(javax.sound.midi.MetaEventListener p0);
  void removeMetaEventListener(javax.sound.midi.MetaEventListener p0);
  int[] addControllerEventListener(javax.sound.midi.ControllerEventListener p0, int[] p1);
  int[] removeControllerEventListener(javax.sound.midi.ControllerEventListener p0, int[] p1);
  void setLoopStartPoint(long p0);
  long getLoopStartPoint();
  void setLoopEndPoint(long p0);
  long getLoopEndPoint();
  void setLoopCount(int p0);
  int getLoopCount();
  boolean isRunning();

  public static class SyncMode {
    public static final SyncMode INTERNAL_CLOCK = new SyncMode("Internal Clock");
    public static final SyncMode MIDI_SYNC = new SyncMode("MIDI Sync");
    public static final SyncMode MIDI_TIME_CODE = new SyncMode("MIDI Time Code");
    public static final SyncMode NO_SYNC = new SyncMode("No Timing");

    private String name;

    protected SyncMode(String name) { this.name = name; }

    public final boolean equals(Object obj) { return super.equals(obj); }

    public final int hashCode() { return super.hashCode(); }

    public final String toString() { return name; }
  }
}
