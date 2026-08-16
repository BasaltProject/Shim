/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled.spi;

public abstract class AudioFileReader {
  protected AudioFileReader() {
  }
  public abstract javax.sound.sampled.AudioInputStream getAudioInputStream(java.io.File p0) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException;
  public abstract javax.sound.sampled.AudioInputStream getAudioInputStream(java.io.InputStream p0) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException;
  public abstract javax.sound.sampled.AudioInputStream getAudioInputStream(java.net.URL p0) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException;
  public abstract javax.sound.sampled.AudioFileFormat getAudioFileFormat(java.net.URL p0) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException;
  public abstract javax.sound.sampled.AudioFileFormat getAudioFileFormat(java.io.InputStream p0) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException;
  public abstract javax.sound.sampled.AudioFileFormat getAudioFileFormat(java.io.File p0) throws javax.sound.sampled.UnsupportedAudioFileException, java.io.IOException;
}
