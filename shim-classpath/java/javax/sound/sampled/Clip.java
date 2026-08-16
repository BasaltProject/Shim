/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

public interface Clip extends javax.sound.sampled.DataLine {
  int LOOP_CONTINUOUSLY = -1;
  void open(javax.sound.sampled.AudioFormat p0, byte[] p1, int p2, int p3) throws javax.sound.sampled.LineUnavailableException;
  void open(javax.sound.sampled.AudioInputStream p0) throws javax.sound.sampled.LineUnavailableException, java.io.IOException;
  void loop(int p0);
  void setFramePosition(int p0);
  void setLoopPoints(int p0, int p1);
  long getMicrosecondLength();
  void setMicrosecondPosition(long p0);
  int getFrameLength();
}
