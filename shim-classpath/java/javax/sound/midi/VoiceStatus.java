/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public class VoiceStatus {
  public boolean active;
  public int channel;
  public int bank;
  public int program;
  public int note;
  public int volume;
  public VoiceStatus() {
  }
}
