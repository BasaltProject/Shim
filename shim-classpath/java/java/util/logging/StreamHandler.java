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

public class StreamHandler extends Handler {
  private OutputStream output;
  private boolean doneHeader;
  private Writer writer;

  public StreamHandler() {
    this(null, null);
  }

  public StreamHandler(OutputStream out, Formatter formatter) {
    if (formatter != null) {
      setFormatter(formatter);
    }
    setOutputStream(out);
  }

  protected synchronized void setOutputStream(OutputStream out) throws SecurityException {
    if (out == null) {
      return;
    }
    flushAndClose();
    this.output = out;
    this.doneHeader = false;
    String encoding = getEncoding();
    if (encoding == null) {
      this.writer = new OutputStreamWriter(output);
    } else {
      try {
        this.writer = new OutputStreamWriter(output, encoding);
      } catch (UnsupportedEncodingException e) {
        this.writer = new OutputStreamWriter(output);
      }
    }
  }

  public synchronized void publish(LogRecord record) {
    if (!isLoggable(record)) {
      return;
    }
    String msg;
    try {
      Formatter formatter = getFormatter();
      if (formatter == null) {
        formatter = new SimpleFormatter();
      }
      msg = formatter.format(record);
    } catch (Exception ex) {
      reportError(null, ex, ErrorManager.FORMAT_FAILURE);
      return;
    }
    try {
      if (!doneHeader) {
        Formatter formatter = getFormatter();
        if (formatter != null) {
          writer.write(formatter.getHead(this));
        }
        doneHeader = true;
      }
      writer.write(msg);
    } catch (Exception ex) {
      reportError(null, ex, ErrorManager.WRITE_FAILURE);
    }
  }

  public boolean isLoggable(LogRecord record) {
    if (writer == null || record == null) {
      return false;
    }
    return super.isLoggable(record);
  }

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
    if (writer != null) {
      try {
        if (!doneHeader) {
          Formatter formatter = getFormatter();
          if (formatter != null) {
            writer.write(formatter.getHead(this));
          }
          doneHeader = true;
        }
        Formatter formatter = getFormatter();
        if (formatter != null) {
          writer.write(formatter.getTail(this));
        }
        writer.flush();
        writer.close();
      } catch (Exception ex) {
        reportError(null, ex, ErrorManager.CLOSE_FAILURE);
      }
      writer = null;
      output = null;
    }
  }

  public synchronized void close() throws SecurityException {
    flushAndClose();
  }
}
