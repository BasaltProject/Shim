/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.zip;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DeflaterOutputStream extends FilterOutputStream {
  protected Deflater def;
  protected byte[] buf;

  private boolean closed;
  private final boolean syncFlush;
  boolean usesDefaultDeflater;

  public DeflaterOutputStream(OutputStream out, Deflater def, int size, boolean syncFlush) {
    super(out);
    if (out == null || def == null) {
      throw new NullPointerException();
    } else if (size <= 0) {
      throw new IllegalArgumentException("buffer size <= 0");
    }
    this.def = def;
    this.buf = new byte[size];
    this.syncFlush = syncFlush;
  }

  public DeflaterOutputStream(OutputStream out, Deflater def, int size) {
    this(out, def, size, false);
  }

  public DeflaterOutputStream(OutputStream out, Deflater def, boolean syncFlush) {
    this(out, def, 512, syncFlush);
  }

  public DeflaterOutputStream(OutputStream out, Deflater def) {
    this(out, def, 512, false);
  }

  public DeflaterOutputStream(OutputStream out, boolean syncFlush) {
    this(out, new Deflater(), 512, syncFlush);
    usesDefaultDeflater = true;
  }

  public DeflaterOutputStream(OutputStream out) {
    this(out, false);
    usesDefaultDeflater = true;
  }

  public void write(int b) throws IOException {
    byte[] one = new byte[1];
    one[0] = (byte) (b & 0xff);
    write(one, 0, 1);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    if (def.finished()) {
      throw new IOException("write beyond end of stream");
    }
    java.util.Objects.checkFromIndexSize(off, len, b.length);
    if (len == 0) {
      return;
    }
    def.setInput(b, off, len);
    while (!def.needsInput()) {
      deflate();
    }
  }

  public void finish() throws IOException {
    if (!def.finished()) {
      def.finish();
      while (!def.finished()) {
        deflate();
      }
    }
  }

  public void close() throws IOException {
    if (!closed) {
      closed = true;
      try {
        finish();
      } finally {
        if (usesDefaultDeflater) {
          def.end();
        }
      }
      out.close();
    }
  }

  protected void deflate() throws IOException {
    int len = def.deflate(buf, 0, buf.length);
    if (len > 0) {
      out.write(buf, 0, len);
    }
  }

  public void flush() throws IOException {
    if (syncFlush && !def.finished()) {
      int len;
      while ((len = def.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH)) > 0) {
        out.write(buf, 0, len);
        if (len < buf.length) {
          break;
        }
      }
    }
    out.flush();
  }
}
