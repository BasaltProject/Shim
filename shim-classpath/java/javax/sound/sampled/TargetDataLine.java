/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

public interface TargetDataLine extends javax.sound.sampled.DataLine {
  void open(javax.sound.sampled.AudioFormat p0, int p1) throws javax.sound.sampled.LineUnavailableException;
  void open(javax.sound.sampled.AudioFormat p0) throws javax.sound.sampled.LineUnavailableException;
  int read(byte[] p0, int p1, int p2);
}
