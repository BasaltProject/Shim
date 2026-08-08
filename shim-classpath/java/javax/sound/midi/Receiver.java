/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public interface Receiver extends java.lang.AutoCloseable {
  void close();
  void send(javax.sound.midi.MidiMessage p0, long p1);
}
