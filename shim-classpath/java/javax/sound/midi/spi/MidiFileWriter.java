/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi.spi;

public abstract class MidiFileWriter {
  protected MidiFileWriter() {
  }
  public abstract int write(javax.sound.midi.Sequence p0, int p1, java.io.File p2) throws java.io.IOException;
  public abstract int write(javax.sound.midi.Sequence p0, int p1, java.io.OutputStream p2) throws java.io.IOException;
  public abstract int[] getMidiFileTypes();
  public abstract int[] getMidiFileTypes(javax.sound.midi.Sequence p0);
  public boolean isFileTypeSupported(int fileType, javax.sound.midi.Sequence sequence) {
    int[] types = getMidiFileTypes(sequence);
    for (int i = 0; i < types.length; i++) {
      if (fileType == types[i]) {
        return true;
      }
    }
    return false;
  }
  public boolean isFileTypeSupported(int fileType) {
    int[] types = getMidiFileTypes();
    for (int i = 0; i < types.length; i++) {
      if (fileType == types[i]) {
        return true;
      }
    }
    return false;
  }
}
