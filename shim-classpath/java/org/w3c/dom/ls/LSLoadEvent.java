/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.ls;

public interface LSLoadEvent extends org.w3c.dom.events.Event {
  org.w3c.dom.Document getNewDocument();

  LSInput getInput();
}
