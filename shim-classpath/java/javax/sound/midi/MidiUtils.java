/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

final class MidiUtils {
  private MidiUtils() { }

  static boolean isMetaEndOfTrack(MidiMessage midiMsg) {
    if (midiMsg.getLength() != 3 || midiMsg.getStatus() != MetaMessage.META) {
      return false;
    }
    byte[] msg = midiMsg.getMessage();
    return (msg[1] & 0xFF) == 0x2F && msg[2] == 0;
  }

  static boolean isMetaTempo(MidiMessage midiMsg) {
    if (midiMsg.getLength() != 6 || midiMsg.getStatus() != MetaMessage.META) {
      return false;
    }
    byte[] msg = midiMsg.getMessage();
    return (msg[1] & 0xFF) == 0x51 && msg[2] == 3;
  }

  static int getTempoMPQ(MidiMessage midiMsg) {
    if (midiMsg.getLength() != 6 || midiMsg.getStatus() != 0xFF) {
      return -1;
    }
    byte[] msg = midiMsg.getMessage();
    if (msg[1] != 0x51 || msg[2] != 0x03) {
      return -1;
    }
    return (msg[5] & 0xFF) | ((msg[4] & 0xFF) << 8) | ((msg[3] & 0xFF) << 16);
  }
}
