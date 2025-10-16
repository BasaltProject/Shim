/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface Attr extends Node {
  String getName();

  boolean getSpecified();

  String getValue();

  void setValue(String p0) throws DOMException;

  Element getOwnerElement();

  TypeInfo getSchemaTypeInfo();

  boolean isId();
}
