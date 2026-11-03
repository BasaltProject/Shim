/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound;

public final class SoundClip {
  SoundClip() { }
  public void stop() { }

  public void loop() { }

  public boolean canPlay() { return false; }

  public boolean isPlaying() { return false; }

  public void play() { }

  public static javax.sound.SoundClip createSoundClip(java.io.File p0) throws java.io.IOException {
    if (p0 == null) {
      throw new NullPointerException("file");
    }
    if (!p0.isFile()) {
      throw new java.io.FileNotFoundException(p0.getPath());
    }
    return new javax.sound.SoundClip();
  }
}
