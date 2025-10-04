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

package java.io;

import java.util.Enumeration;
import java.util.Objects;
import java.util.Vector;

public class SequenceInputStream extends InputStream {
  private final Enumeration<? extends InputStream> e;
  private InputStream in;

  public SequenceInputStream(Enumeration<? extends InputStream> e) {
    this.e = e;
    peekNextStream();
  }

  public SequenceInputStream(InputStream s1, InputStream s2) {
    Vector<InputStream> v = new Vector<>(2);
    v.addElement(s1);
    v.addElement(s2);
    e = v.elements();
    peekNextStream();
  }

  final void nextStream() throws IOException {
    if (in != null) {
      in.close();
    }
    peekNextStream();
  }

  private void peekNextStream() {
    if (e.hasMoreElements()) {
      in = (InputStream) e.nextElement();
      if (in == null) {
        throw new NullPointerException();
      }
    } else {
      in = null;
    }
  }

  public int available() throws IOException {
    if (in == null) {
      return 0;
    }
    return in.available();
  }

  public int read() throws IOException {
    while (in != null) {
      int c = in.read();
      if (c != -1) {
        return c;
      }
      nextStream();
    }
    return -1;
  }

  public int read(byte[] b, int off, int len) throws IOException {
    if (in == null) {
      return -1;
    } else if (b == null) {
      throw new NullPointerException();
    }
    Objects.checkFromIndexSize(off, len, b.length);
    if (len == 0) {
      return 0;
    }
    do {
      int n = in.read(b, off, len);
      if (n > 0) {
        return n;
      }
      nextStream();
    } while (in != null);
    return -1;
  }

  public void close() throws IOException {
    do {
      nextStream();
    } while (in != null);
  }
}
