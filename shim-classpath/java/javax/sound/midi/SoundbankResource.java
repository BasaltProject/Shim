/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public abstract class SoundbankResource {
  private final Soundbank soundBank;
  private final String name;
  private final Class<?> dataClass;

  protected SoundbankResource(Soundbank soundBank, String name, Class<?> dataClass) {
    this.soundBank = soundBank;
    this.name = name;
    this.dataClass = dataClass;
  }

  public Soundbank getSoundbank() { return soundBank; }

  public String getName() { return name; }

  public Class<?> getDataClass() { return dataClass; }

  public abstract Object getData();
}
