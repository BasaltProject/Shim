/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public abstract class Instrument extends SoundbankResource {
  private final Patch patch;

  protected Instrument(Soundbank soundbank, Patch patch, String name, Class<?> dataClass) {
    super(soundbank, name, dataClass);
    this.patch = patch;
  }

  public Patch getPatch() { return patch; }
}
