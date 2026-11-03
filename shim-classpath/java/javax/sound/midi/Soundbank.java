/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public interface Soundbank {
  java.lang.String getName();
  javax.sound.midi.SoundbankResource[] getResources();
  java.lang.String getVersion();
  java.lang.String getDescription();
  java.lang.String getVendor();
  javax.sound.midi.Instrument[] getInstruments();
  javax.sound.midi.Instrument getInstrument(javax.sound.midi.Patch p0);
}
