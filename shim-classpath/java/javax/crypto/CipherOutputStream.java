/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CipherOutputStream extends FilterOutputStream {
  private final OutputStream output;
  private final byte[] ibuffer = new byte[1];
  private byte[] obuffer = null;
  private boolean closed = false;
  private final Cipher cipher;

  public CipherOutputStream(OutputStream os, Cipher c) {
    super(os);
    output = os;
    cipher = c;
  }

  protected CipherOutputStream(OutputStream os) {
    super(os);
    output = os;
    cipher = new NullCipher();
  }

  @Override
  public void write(int b) throws IOException {
    ibuffer[0] = (byte) b;
    obuffer = cipher.update(ibuffer, 0, 1);
    if (obuffer != null) {
      output.write(obuffer);
      obuffer = null;
    }
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    obuffer = cipher.update(b, off, len);
    if (obuffer != null) {
      output.write(obuffer);
      obuffer = null;
    }
  }

  @Override
  public void flush() throws IOException {
    if (obuffer != null) {
      output.write(obuffer);
      obuffer = null;
    }
    output.flush();
  }

  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }
    closed = true;
    try {
      obuffer = cipher.doFinal();
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      obuffer = null;
    }
    try {
      flush();
    } catch (IOException ignored) {
    }
    out.close();
  }
}
