/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public interface MidiChannel {
  int getController(int p0);
  void controlChange(int p0, int p1);
  int getProgram();
  void noteOn(int p0, int p1);
  void noteOff(int p0);
  void noteOff(int p0, int p1);
  void setPolyPressure(int p0, int p1);
  int getPolyPressure(int p0);
  void setChannelPressure(int p0);
  int getChannelPressure();
  void programChange(int p0, int p1);
  void programChange(int p0);
  void setPitchBend(int p0);
  int getPitchBend();
  void resetAllControllers();
  void allNotesOff();
  void allSoundOff();
  boolean localControl(boolean p0);
  void setMono(boolean p0);
  boolean getMono();
  void setOmni(boolean p0);
  boolean getOmni();
  void setMute(boolean p0);
  boolean getMute();
  void setSolo(boolean p0);
  boolean getSolo();
}
