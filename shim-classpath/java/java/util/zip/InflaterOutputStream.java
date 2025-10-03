/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.zip;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class InflaterOutputStream extends FilterOutputStream {
  protected final Inflater inf;
  protected final byte[] buf;

  private final byte[] wbuf = new byte[1];
  private boolean usesDefaultInflater;
  private boolean closed;

  private void ensureOpen() throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
  }

  public InflaterOutputStream(OutputStream out) {
    this(out, new Inflater());
    usesDefaultInflater = true;
  }

  public InflaterOutputStream(OutputStream out, Inflater infl) {
    this(out, infl, 512);
  }

  public InflaterOutputStream(OutputStream out, Inflater infl, int bufLen) {
    super(out);
    if (out == null) {
      throw new NullPointerException("Null output");
    }
    if (infl == null) {
      throw new NullPointerException("Null inflater");
    }
    if (bufLen <= 0) {
      throw new IllegalArgumentException("Buffer size < 1");
    }
    inf = infl;
    buf = new byte[bufLen];
  }

  public void close() throws IOException {
    if (!closed) {
      try {
        finish();
      } finally {
        out.close();
        closed = true;
      }
    }
  }

  public void flush() throws IOException {
    ensureOpen();
    if (!inf.finished()) {
      try {
        while (!inf.finished() && !inf.needsInput()) {
          int n = inf.inflate(buf, 0, buf.length);
          if (n < 1) {
            break;
          }
          out.write(buf, 0, n);
        }
        super.flush();
      } catch (DataFormatException ex) {
        throw new ZipException(message(ex));
      }
    }
  }

  public void finish() throws IOException {
    ensureOpen();
    flush();
    if (usesDefaultInflater) {
      inf.end();
    }
  }

  public void write(int b) throws IOException {
    wbuf[0] = (byte) b;
    write(wbuf, 0, 1);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    ensureOpen();
    if (b == null) {
      throw new NullPointerException("Null buffer for read");
    }
    java.util.Objects.checkFromIndexSize(off, len, b.length);
    if (len == 0) {
      return;
    }
    try {
      for (;;) {
        int n;
        if (inf.needsInput()) {
          if (len < 1) {
            break;
          }
          int part = len < 512 ? len : 512;
          inf.setInput(b, off, part);
          off += part;
          len -= part;
        }
        do {
          n = inf.inflate(buf, 0, buf.length);
          if (n > 0) {
            out.write(buf, 0, n);
          }
        } while (n > 0);
        if (inf.finished()) {
          break;
        }
        if (inf.needsDictionary()) {
          throw new ZipException("ZLIB dictionary missing");
        }
      }
    } catch (DataFormatException ex) {
      throw new ZipException(message(ex));
    }
  }

  private static String message(DataFormatException ex) {
    String msg = ex.getMessage();
    return msg == null ? "Invalid ZLIB data format" : msg;
  }
}
