/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.logging;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Objects;

public class StreamHandler extends Handler {
  private OutputStream output;
  private boolean doneHeader;
  private volatile Writer writer;

  public StreamHandler() {
    this(Level.INFO, new SimpleFormatter(), null);
  }

  public StreamHandler(OutputStream out, Formatter formatter) {
    this(Level.INFO, formatter, Objects.requireNonNull(formatter));
    setOutputStream(out);
  }

  StreamHandler(Level defaultLevel, Formatter defaultFormatter, Formatter specifiedFormatter) {
    super(defaultLevel, defaultFormatter, specifiedFormatter);
  }

  protected synchronized void setOutputStream(OutputStream out) throws SecurityException {
    if (out == null) {
      throw new NullPointerException();
    }
    flushAndClose();
    output = out;
    doneHeader = false;
    String encoding = getEncoding();
    if (encoding == null) {
      writer = new OutputStreamWriter(output);
    } else {
      try {
        writer = new OutputStreamWriter(output, encoding);
      } catch (UnsupportedEncodingException ex) {
        throw new Error("Unexpected exception " + ex);
      }
    }
  }

  @Override
  public synchronized void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
    super.setEncoding(encoding);
    if (output == null) {
      return;
    }
    flush();
    if (encoding == null) {
      writer = new OutputStreamWriter(output);
    } else {
      writer = new OutputStreamWriter(output, encoding);
    }
  }

  @Override
  public synchronized void publish(LogRecord record) {
    if (!isLoggable(record)) {
      return;
    }
    String msg;
    try {
      msg = getFormatter().format(record);
    } catch (Exception ex) {
      reportError(null, ex, ErrorManager.FORMAT_FAILURE);
      return;
    }
    try {
      if (!doneHeader) {
        writer.write(getFormatter().getHead(this));
        doneHeader = true;
      }
      writer.write(msg);
      synchronousPostWriteHook();
    } catch (Exception ex) {
      reportError(null, ex, ErrorManager.WRITE_FAILURE);
    }
  }

  void synchronousPostWriteHook() {
  }

  @Override
  public boolean isLoggable(LogRecord record) {
    if (writer == null || record == null) {
      return false;
    }
    return super.isLoggable(record);
  }

  @Override
  public synchronized void flush() {
    if (writer != null) {
      try {
        writer.flush();
      } catch (Exception ex) {
        reportError(null, ex, ErrorManager.FLUSH_FAILURE);
      }
    }
  }

  private synchronized void flushAndClose() throws SecurityException {
    checkPermission();
    if (writer != null) {
      try {
        if (!doneHeader) {
          writer.write(getFormatter().getHead(this));
          doneHeader = true;
        }
        writer.write(getFormatter().getTail(this));
        writer.flush();
        writer.close();
      } catch (Exception ex) {
        reportError(null, ex, ErrorManager.CLOSE_FAILURE);
      }
      writer = null;
      output = null;
    }
  }

  @Override
  public synchronized void close() throws SecurityException {
    flushAndClose();
  }
}
