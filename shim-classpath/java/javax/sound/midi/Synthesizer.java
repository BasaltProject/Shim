/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public interface Synthesizer extends javax.sound.midi.MidiDevice {
  int getMaxPolyphony();
  long getLatency();
  javax.sound.midi.MidiChannel[] getChannels();
  javax.sound.midi.VoiceStatus[] getVoiceStatus();
  boolean isSoundbankSupported(javax.sound.midi.Soundbank p0);
  boolean loadInstrument(javax.sound.midi.Instrument p0);
  void unloadInstrument(javax.sound.midi.Instrument p0);
  boolean remapInstrument(javax.sound.midi.Instrument p0, javax.sound.midi.Instrument p1);
  javax.sound.midi.Soundbank getDefaultSoundbank();
  javax.sound.midi.Instrument[] getAvailableInstruments();
  javax.sound.midi.Instrument[] getLoadedInstruments();
  boolean loadAllInstruments(javax.sound.midi.Soundbank p0);
  void unloadAllInstruments(javax.sound.midi.Soundbank p0);
  boolean loadInstruments(javax.sound.midi.Soundbank p0, javax.sound.midi.Patch[] p1);
  void unloadInstruments(javax.sound.midi.Soundbank p0, javax.sound.midi.Patch[] p1);
}
