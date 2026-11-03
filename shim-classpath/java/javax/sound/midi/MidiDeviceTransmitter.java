/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public interface MidiDeviceTransmitter extends javax.sound.midi.Transmitter {
  javax.sound.midi.MidiDevice getMidiDevice();
}
