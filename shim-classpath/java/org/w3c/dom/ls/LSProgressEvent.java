/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.ls;

public interface LSProgressEvent extends org.w3c.dom.events.Event {
  LSInput getInput();

  int getPosition();

  int getTotalSize();
}
