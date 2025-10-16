/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface DocumentType extends Node {
  String getName();

  NamedNodeMap getEntities();

  NamedNodeMap getNotations();

  String getPublicId();

  String getSystemId();

  String getInternalSubset();
}
