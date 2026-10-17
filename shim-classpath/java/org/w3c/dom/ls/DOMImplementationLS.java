/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.ls;

public interface DOMImplementationLS {
  short MODE_SYNCHRONOUS = 1;
  short MODE_ASYNCHRONOUS = 2;

  LSParser createLSParser(short p0, String p1) throws org.w3c.dom.DOMException;

  LSSerializer createLSSerializer();

  LSInput createLSInput();

  LSOutput createLSOutput();
}
