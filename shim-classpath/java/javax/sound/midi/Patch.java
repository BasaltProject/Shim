/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public class Patch {
  private final int bank;
  private final int program;

  public Patch(int bank, int program) {
    this.bank = bank;
    this.program = program;
  }

  public int getBank() { return bank; }

  public int getProgram() { return program; }
}
