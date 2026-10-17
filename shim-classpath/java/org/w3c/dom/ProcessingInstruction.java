/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface ProcessingInstruction extends Node {
  String getTarget();

  String getData();

  void setData(String p0) throws DOMException;
}
