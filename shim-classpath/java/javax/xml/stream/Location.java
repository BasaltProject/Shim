/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream;

public interface Location {
  int getLineNumber();

  int getColumnNumber();

  int getCharacterOffset();

  String getPublicId();

  String getSystemId();
}
