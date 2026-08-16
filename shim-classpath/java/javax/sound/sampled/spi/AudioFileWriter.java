/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled.spi;

public abstract class AudioFileWriter {
  protected AudioFileWriter() {
  }
  public abstract int write(javax.sound.sampled.AudioInputStream p0, javax.sound.sampled.AudioFileFormat.Type p1, java.io.OutputStream p2) throws java.io.IOException;
  public abstract int write(javax.sound.sampled.AudioInputStream p0, javax.sound.sampled.AudioFileFormat.Type p1, java.io.File p2) throws java.io.IOException;
  public abstract javax.sound.sampled.AudioFileFormat.Type[] getAudioFileTypes(javax.sound.sampled.AudioInputStream p0);
  public abstract javax.sound.sampled.AudioFileFormat.Type[] getAudioFileTypes();
  public boolean isFileTypeSupported(javax.sound.sampled.AudioFileFormat.Type fileType, javax.sound.sampled.AudioInputStream stream) {
    javax.sound.sampled.AudioFileFormat.Type[] types = getAudioFileTypes(stream);
    for (int i = 0; i < types.length; i++) {
      if (fileType.equals(types[i])) {
        return true;
      }
    }
    return false;
  }
  public boolean isFileTypeSupported(javax.sound.sampled.AudioFileFormat.Type fileType) {
    javax.sound.sampled.AudioFileFormat.Type[] types = getAudioFileTypes();
    for (int i = 0; i < types.length; i++) {
      if (fileType.equals(types[i])) {
        return true;
      }
    }
    return false;
  }
}
