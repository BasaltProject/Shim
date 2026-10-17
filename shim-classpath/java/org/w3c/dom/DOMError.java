/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface DOMError {
  short SEVERITY_WARNING = 1;
  short SEVERITY_ERROR = 2;
  short SEVERITY_FATAL_ERROR = 3;

  short getSeverity();

  String getMessage();

  String getType();

  Object getRelatedException();

  Object getRelatedData();

  DOMLocator getLocation();
}
