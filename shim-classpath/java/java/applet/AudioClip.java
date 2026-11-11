/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.applet;

@Deprecated(since = "9", forRemoval = true)
public interface AudioClip {
  void stop();
  void loop();
  void play();
}
