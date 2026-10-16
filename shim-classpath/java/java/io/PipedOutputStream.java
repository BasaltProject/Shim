/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.io;

public class PipedOutputStream extends OutputStream {
  private volatile PipedInputStream sink;

  public PipedOutputStream(PipedInputStream snk) throws IOException {
    connect(snk);
  }

  public PipedOutputStream() { }

  public synchronized void connect(PipedInputStream snk) throws IOException {
    if (snk == null) {
      throw new NullPointerException();
    } else if (sink != null || snk.connected) {
      throw new IOException("Already connected");
    }
    sink = snk;
    snk.in = -1;
    snk.out = 0;
    snk.connected = true;
  }

  public void write(int b) throws IOException {
    PipedInputStream s = sink;
    if (s == null) {
      throw new IOException("Pipe not connected");
    }
    s.receive(b);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    PipedInputStream s = sink;
    if (s == null) {
      throw new IOException("Pipe not connected");
    } else if (b == null) {
      throw new NullPointerException();
    } else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }
    s.receive(b, off, len);
  }

  public synchronized void flush() throws IOException {
    if (sink != null) {
      synchronized (sink) {
        sink.notifyAll();
      }
    }
  }

  public void close() throws IOException {
    PipedInputStream s = sink;
    if (s != null) {
      s.receivedLast();
    }
  }
}
