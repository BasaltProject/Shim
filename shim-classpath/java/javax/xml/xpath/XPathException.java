/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.xpath;

import java.io.PrintStream;
import java.io.PrintWriter;

public class XPathException extends Exception {
  private static final long serialVersionUID = -1837080260374986980L;

  public XPathException(String message) {
    super(message);
    if (message == null) {
      throw new NullPointerException("message can't be null");
    }
  }

  public XPathException(Throwable cause) {
    super(cause);
    if (cause == null) {
      throw new NullPointerException("cause can't be null");
    }
  }

  public Throwable getCause() {
    return super.getCause();
  }

  public void printStackTrace(PrintStream s) {
    if (getCause() != null) {
      getCause().printStackTrace(s);
      s.println("--------------- linked to ------------------");
    }
    super.printStackTrace(s);
  }

  public void printStackTrace() {
    printStackTrace(System.err);
  }

  public void printStackTrace(PrintWriter s) {
    if (getCause() != null) {
      getCause().printStackTrace(s);
      s.println("--------------- linked to ------------------");
    }
    super.printStackTrace(s);
  }
}
