/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled.spi;

public abstract class MixerProvider {
  protected MixerProvider() {
  }
  public boolean isMixerSupported(javax.sound.sampled.Mixer.Info info) {
    javax.sound.sampled.Mixer.Info[] infos = getMixerInfo();
    for (int i = 0; i < infos.length; i++) {
      if (info.equals(infos[i])) {
        return true;
      }
    }
    return false;
  }
  public abstract javax.sound.sampled.Mixer getMixer(javax.sound.sampled.Mixer.Info p0);
  public abstract javax.sound.sampled.Mixer.Info[] getMixerInfo();
}
