/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform;

public interface ErrorListener {
  void warning(TransformerException p0) throws TransformerException;

  void error(TransformerException p0) throws TransformerException;

  void fatalError(TransformerException p0) throws TransformerException;
}
