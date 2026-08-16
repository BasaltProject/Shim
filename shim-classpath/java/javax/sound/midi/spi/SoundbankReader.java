/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi.spi;

public abstract class SoundbankReader {
  protected SoundbankReader() {
  }
  public abstract javax.sound.midi.Soundbank getSoundbank(java.io.File p0) throws javax.sound.midi.InvalidMidiDataException, java.io.IOException;
  public abstract javax.sound.midi.Soundbank getSoundbank(java.io.InputStream p0) throws javax.sound.midi.InvalidMidiDataException, java.io.IOException;
  public abstract javax.sound.midi.Soundbank getSoundbank(java.net.URL p0) throws javax.sound.midi.InvalidMidiDataException, java.io.IOException;
}
