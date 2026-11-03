/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.midi;

public interface ControllerEventListener extends java.util.EventListener {
  void controlChange(javax.sound.midi.ShortMessage p0);
}
