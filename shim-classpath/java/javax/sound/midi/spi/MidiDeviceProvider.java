/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi.spi;

public abstract class MidiDeviceProvider {
  protected MidiDeviceProvider() {
  }
  public abstract javax.sound.midi.MidiDevice.Info[] getDeviceInfo();
  public boolean isDeviceSupported(javax.sound.midi.MidiDevice.Info info) {
    javax.sound.midi.MidiDevice.Info[] infos = getDeviceInfo();
    for (int i = 0; i < infos.length; i++) {
      if (info.equals(infos[i])) {
        return true;
      }
    }
    return false;
  }
  public abstract javax.sound.midi.MidiDevice getDevice(javax.sound.midi.MidiDevice.Info p0);
}
