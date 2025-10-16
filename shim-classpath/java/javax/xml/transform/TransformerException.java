/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform;

import java.io.PrintStream;
import java.io.PrintWriter;

public class TransformerException extends Exception {
  private static final long serialVersionUID = 975798773772956428L;

  SourceLocator locator;
  Throwable containedException;

  public SourceLocator getLocator() {
    return locator;
  }

  public void setLocator(SourceLocator location) {
    locator = location;
  }

  public Throwable getException() {
    return containedException;
  }

  public Throwable getCause() {
    return ((containedException == this) ? null : containedException);
  }

  public synchronized Throwable initCause(Throwable cause) {
    if (this.containedException != null) {
      throw new IllegalStateException("Can't overwrite cause");
    }
    if (cause == this) {
      throw new IllegalArgumentException("Self-causation not permitted");
    }
    this.containedException = cause;
    return this;
  }

  public TransformerException(String message) {
    super(message);
    this.containedException = null;
    this.locator = null;
  }

  public TransformerException(Throwable e) {
    super(e.toString());
    this.containedException = e;
    this.locator = null;
  }

  public TransformerException(String message, Throwable e) {
    super(((message == null) || (message.length() == 0)) ? e.toString() : message);
    this.containedException = e;
    this.locator = null;
  }

  public TransformerException(String message, SourceLocator locator) {
    super(message);
    this.containedException = null;
    this.locator = locator;
  }

  public TransformerException(String message, SourceLocator locator, Throwable e) {
    super(message);
    this.containedException = e;
    this.locator = locator;
  }

  public String getMessageAndLocation() {
    StringBuilder sbuffer = new StringBuilder();
    String message = super.getMessage();
    if (null != message) {
      sbuffer.append(message);
    }
    if (null != locator) {
      String systemID = locator.getSystemId();
      int line = locator.getLineNumber();
      int column = locator.getColumnNumber();
      if (null != systemID) {
        sbuffer.append("; SystemID: ");
        sbuffer.append(systemID);
      }
      if (0 != line) {
        sbuffer.append("; Line#: ");
        sbuffer.append(line);
      }
      if (0 != column) {
        sbuffer.append("; Column#: ");
        sbuffer.append(column);
      }
    }
    return sbuffer.toString();
  }

  public String getLocationAsString() {
    if (null != locator) {
      StringBuilder sbuffer = new StringBuilder();
      String systemID = locator.getSystemId();
      int line = locator.getLineNumber();
      int column = locator.getColumnNumber();
      if (null != systemID) {
        sbuffer.append("; SystemID: ");
        sbuffer.append(systemID);
      }
      if (0 != line) {
        sbuffer.append("; Line#: ");
        sbuffer.append(line);
      }
      if (0 != column) {
        sbuffer.append("; Column#: ");
        sbuffer.append(column);
      }
      return sbuffer.toString();
    }
    return null;
  }

  public void printStackTrace() {
    printStackTrace(new PrintWriter(System.err, true));
  }

  public void printStackTrace(PrintStream s) {
    printStackTrace(new PrintWriter(s));
  }

  public void printStackTrace(PrintWriter s) {
    if (s == null) {
      s = new PrintWriter(System.err, true);
    }
    try {
      String locInfo = getLocationAsString();
      if (null != locInfo) {
        s.println(locInfo);
      }
      super.printStackTrace(s);
    } catch (Throwable e) {
    }
  }
}
