/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public interface Transmitter extends java.lang.AutoCloseable {
  void close();
  javax.sound.midi.Receiver getReceiver();
  void setReceiver(javax.sound.midi.Receiver p0);
}
