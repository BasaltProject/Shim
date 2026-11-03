/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.sound;

import javax.sound.midi.MidiChannel;

final class SoftChannel implements MidiChannel {
  private final SoftSynthesizer synth;
  private final int channel;
  private final int[] controllers = new int[128];
  private final int[] polyPressure = new int[128];
  private final boolean[] notes = new boolean[128];
  private int program;
  private int pitchBend = 8192;
  private int channelPressure;
  private boolean mono;
  private boolean omni;
  private boolean mute;
  private boolean solo;
  private boolean localControl = true;

  SoftChannel(SoftSynthesizer synth, int channel) {
    this.synth = synth;
    this.channel = channel;
    resetControllers();
    omni = false;
  }

  private void resetControllers() {
    java.util.Arrays.fill(controllers, 0);
    java.util.Arrays.fill(polyPressure, 0);
    controllers[7] = 100;
    controllers[10] = 64;
    controllers[11] = 127;
    controllers[91] = 40;
    controllers[100] = 127;
    controllers[101] = 127;
    pitchBend = 8192;
    channelPressure = 0;
  }

  public void noteOn(int noteNumber, int velocity) {
    if (velocity == 0) {
      noteOff(noteNumber, 64);
      return;
    }
    if (noteNumber < 0 || noteNumber > 127) {
      return;
    }
    notes[noteNumber] = true;
    synth.voiceOn(channel, controllers[0] * 128 + controllers[32], program, noteNumber, velocity);
  }

  public void noteOff(int noteNumber, int velocity) {
    if (noteNumber < 0 || noteNumber > 127) {
      return;
    }
    notes[noteNumber] = false;
    synth.voiceOff(channel, noteNumber);
  }

  public void noteOff(int noteNumber) { noteOff(noteNumber, 64); }

  public void setPolyPressure(int noteNumber, int pressure) { polyPressure[noteNumber & 127] = pressure & 127; }

  public int getPolyPressure(int noteNumber) { return polyPressure[noteNumber & 127]; }

  public void setChannelPressure(int pressure) { channelPressure = pressure & 127; }

  public int getChannelPressure() { return channelPressure; }

  public void controlChange(int controller, int value) {
    controller &= 127;
    value &= 127;
    switch (controller) {
      case 120:
        allSoundOff();
        return;
      case 121:
        resetAllControllers();
        return;
      case 123:
        allNotesOff();
        return;
      case 124:
        omni = false;
        break;
      case 125:
        omni = true;
        break;
      case 126:
        mono = true;
        break;
      case 127:
        mono = false;
        break;
      case 122:
        localControl = value >= 64;
        break;
      default:
    }
    controllers[controller] = value;
  }

  public int getController(int controller) { return controllers[controller & 127]; }

  public void programChange(int program) { this.program = program & 127; }

  public void programChange(int bank, int program) {
    controllers[0] = (bank >> 7) & 127;
    controllers[32] = bank & 127;
    this.program = program & 127;
  }

  public int getProgram() { return program; }

  public void setPitchBend(int bend) { pitchBend = bend & 16383; }

  public int getPitchBend() { return pitchBend; }

  public void resetAllControllers() {
    int bank0 = controllers[0];
    int bank32 = controllers[32];
    int vol = controllers[7];
    int pan = controllers[10];
    resetControllers();
    controllers[0] = bank0;
    controllers[32] = bank32;
    controllers[7] = vol;
    controllers[10] = pan;
  }

  public void allNotesOff() {
    for (int n = 0; n < 128; n++) {
      if (notes[n]) {
        noteOff(n);
      }
    }
  }

  public void allSoundOff() {
    java.util.Arrays.fill(notes, false);
    synth.voicesOff(channel);
  }

  public boolean localControl(boolean on) {
    localControl = on;
    return localControl;
  }

  public void setMono(boolean on) { mono = on; }

  public boolean getMono() { return mono; }

  public void setOmni(boolean on) { omni = on; }

  public boolean getOmni() { return omni; }

  public void setMute(boolean mute) { this.mute = mute; }

  public boolean getMute() { return mute; }

  public void setSolo(boolean soloState) { solo = soloState; }

  public boolean getSolo() { return solo; }
}
