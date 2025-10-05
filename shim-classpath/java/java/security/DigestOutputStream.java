/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DigestOutputStream extends FilterOutputStream {
  private boolean on = true;
  protected MessageDigest digest;

  public DigestOutputStream(OutputStream stream, MessageDigest digest) {
    super(stream);
    setMessageDigest(digest);
  }

  public MessageDigest getMessageDigest() {
    return digest;
  }

  public void setMessageDigest(MessageDigest digest) {
    this.digest = digest;
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
    if (on) {
      digest.update((byte) b);
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    if (on) {
      digest.update(b, off, len);
    }
  }

  public void on(boolean on) {
    this.on = on;
  }

  @Override
  public String toString() {
    return "[Digest Output Stream] " + digest.toString();
  }
}
