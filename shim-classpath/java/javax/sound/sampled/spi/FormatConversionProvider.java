/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled.spi;

public abstract class FormatConversionProvider {
  protected FormatConversionProvider() {
  }
  public boolean isSourceEncodingSupported(javax.sound.sampled.AudioFormat.Encoding sourceEncoding) {
    javax.sound.sampled.AudioFormat.Encoding[] sourceEncodings = getSourceEncodings();
    for (int i = 0; i < sourceEncodings.length; i++) {
      if (sourceEncoding.equals(sourceEncodings[i])) {
        return true;
      }
    }
    return false;
  }
  public abstract javax.sound.sampled.AudioFormat.Encoding[] getTargetEncodings(javax.sound.sampled.AudioFormat p0);
  public abstract javax.sound.sampled.AudioFormat.Encoding[] getTargetEncodings();
  public boolean isConversionSupported(javax.sound.sampled.AudioFormat.Encoding targetEncoding, javax.sound.sampled.AudioFormat sourceFormat) {
    javax.sound.sampled.AudioFormat.Encoding[] targetEncodings = getTargetEncodings(sourceFormat);
    for (int i = 0; i < targetEncodings.length; i++) {
      if (targetEncoding.equals(targetEncodings[i])) {
        return true;
      }
    }
    return false;
  }
  public boolean isConversionSupported(javax.sound.sampled.AudioFormat targetFormat, javax.sound.sampled.AudioFormat sourceFormat) {
    javax.sound.sampled.AudioFormat[] targetFormats = getTargetFormats(targetFormat.getEncoding(), sourceFormat);
    for (int i = 0; i < targetFormats.length; i++) {
      if (targetFormat.matches(targetFormats[i])) {
        return true;
      }
    }
    return false;
  }
  public abstract javax.sound.sampled.AudioInputStream getAudioInputStream(javax.sound.sampled.AudioFormat.Encoding p0, javax.sound.sampled.AudioInputStream p1);
  public abstract javax.sound.sampled.AudioInputStream getAudioInputStream(javax.sound.sampled.AudioFormat p0, javax.sound.sampled.AudioInputStream p1);
  public abstract javax.sound.sampled.AudioFormat[] getTargetFormats(javax.sound.sampled.AudioFormat.Encoding p0, javax.sound.sampled.AudioFormat p1);
  public abstract javax.sound.sampled.AudioFormat.Encoding[] getSourceEncodings();
  public boolean isTargetEncodingSupported(javax.sound.sampled.AudioFormat.Encoding targetEncoding) {
    javax.sound.sampled.AudioFormat.Encoding[] targetEncodings = getTargetEncodings();
    for (int i = 0; i < targetEncodings.length; i++) {
      if (targetEncoding.equals(targetEncodings[i])) {
        return true;
      }
    }
    return false;
  }
}
