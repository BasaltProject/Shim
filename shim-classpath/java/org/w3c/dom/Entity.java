/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface Entity extends Node {
  String getPublicId();

  String getSystemId();

  String getNotationName();

  String getInputEncoding();

  String getXmlEncoding();

  String getXmlVersion();
}
