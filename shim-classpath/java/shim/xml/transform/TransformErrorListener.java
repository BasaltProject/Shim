/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.transform;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

final class TransformErrorListener implements ErrorListener {
  public void warning(TransformerException exception) throws TransformerException {
    System.err.println("Warning:  " + exception.getMessageAndLocation());
  }

  public void error(TransformerException exception) throws TransformerException {
    System.err.println("Error:  " + exception.getMessageAndLocation());
    throw exception;
  }

  public void fatalError(TransformerException exception) throws TransformerException {
    System.err.println("Fatal Error:  " + exception.getMessageAndLocation());
    throw exception;
  }
}
