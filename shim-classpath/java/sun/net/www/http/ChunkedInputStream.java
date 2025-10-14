/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import sun.net.www.MessageHeader;

public class ChunkedInputStream extends InputStream {
  private InputStream in;
  private final HttpClient hc;
  private final MessageHeader responses;
  private long chunkRemaining;
  private boolean atEnd;
  private boolean error;
  private boolean closed;

  public ChunkedInputStream(InputStream in, HttpClient hc, MessageHeader responses) {
    this.in = in;
    this.hc = hc;
    this.responses = responses;
  }

  private String readLine() throws IOException {
    StringBuilder sb = new StringBuilder();
    int c;
    while ((c = in.read()) != -1) {
      if (c == '\n') {
        return sb.toString();
      }
      if (c != '\r') {
        sb.append((char) c);
      }
    }
    error = true;
    throw new IOException("Premature EOF");
  }

  private void nextChunk() throws IOException {
    String line = readLine();
    int semi = line.indexOf(';');
    if (semi >= 0) {
      line = line.substring(0, semi);
    }
    line = line.trim();
    try {
      chunkRemaining = Long.parseLong(line, 16);
    } catch (NumberFormatException e) {
      error = true;
      throw new IOException("Bogus chunk size");
    }
    if (chunkRemaining < 0) {
      error = true;
      throw new IOException("Bogus chunk size");
    }
    if (chunkRemaining == 0) {
      atEnd = true;
      responses.mergeHeader(in);
    }
  }

  private void endChunk() throws IOException {
    int c = in.read();
    if (c == '\r') {
      c = in.read();
    }
    if (c != '\n') {
      error = true;
      throw new IOException("missing CRLF after chunk");
    }
  }

  @Override
  public int read() throws IOException {
    byte[] one = new byte[1];
    int n = read(one, 0, 1);
    return n < 0 ? -1 : one[0] & 0xff;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (closed) {
      throw new IOException("stream is closed");
    }
    Objects.checkFromIndexSize(off, len, b.length);
    if (len == 0) {
      return 0;
    }
    if (atEnd) {
      return -1;
    }
    if (chunkRemaining == 0) {
      nextChunk();
      if (atEnd) {
        finish();
        return -1;
      }
    }
    int n = in.read(b, off, (int) Math.min(len, chunkRemaining));
    if (n < 0) {
      error = true;
      throw new IOException("Premature EOF");
    }
    chunkRemaining -= n;
    if (chunkRemaining == 0) {
      endChunk();
    }
    return n;
  }

  @Override
  public int available() throws IOException {
    if (closed) {
      throw new IOException("stream is closed");
    }
    if (atEnd) {
      return 0;
    }
    return (int) Math.min(in.available(), chunkRemaining);
  }

  private void finish() {
    if (in != null) {
      if (!error && atEnd) {
        hc.finished();
      } else {
        hc.closeServer();
      }
      in = null;
    }
  }

  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }
    closed = true;
    finish();
  }
}
