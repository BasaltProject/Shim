/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.cs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public final class StreamEncoder extends Writer {
  private static final int kDefaultByteBufferSize = 8192;

  private volatile boolean closed;
  private final Charset cs;
  private final CharsetEncoder encoder;
  private final ByteBuffer bb;
  private final OutputStream out;
  private boolean haveLeftoverChar;
  private char leftoverChar;
  private CharBuffer lcb;

  public static StreamEncoder forOutputStreamWriter(OutputStream out, Object lock, String charsetName)
      throws UnsupportedEncodingException {
    String csn = charsetName;
    if (csn == null) {
      csn = Charset.defaultCharset().name();
    }
    try {
      if (Charset.isSupported(csn)) {
        return new StreamEncoder(out, lock, Charset.forName(csn));
      }
    } catch (IllegalArgumentException x) {
      // fall through to the unsupported encoding error
    }
    throw new UnsupportedEncodingException(csn);
  }

  public static StreamEncoder forOutputStreamWriter(OutputStream out, Object lock, Charset cs) {
    return new StreamEncoder(out, lock, cs);
  }

  public static StreamEncoder forOutputStreamWriter(OutputStream out, Object lock, CharsetEncoder enc) {
    return new StreamEncoder(out, lock, enc);
  }

  private StreamEncoder(OutputStream out, Object lock, Charset cs) {
    this(out, lock, cs.newEncoder().onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE));
  }

  private StreamEncoder(OutputStream out, Object lock, CharsetEncoder enc) {
    super(lock);
    this.out = out;
    this.cs = enc.charset();
    this.encoder = enc;
    this.bb = ByteBuffer.allocate(kDefaultByteBufferSize);
  }

  private void ensureOpen() throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
  }

  public void flushBuffer() throws IOException {
    synchronized (lock) {
      if (isOpen()) {
        implFlushBuffer();
      } else {
        throw new IOException("Stream closed");
      }
    }
  }

  public void write(int c) throws IOException {
    char[] cbuf = new char[1];
    cbuf[0] = (char) c;
    write(cbuf, 0, 1);
  }

  public void write(char[] cbuf, int off, int len) throws IOException {
    synchronized (lock) {
      ensureOpen();
      if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0) {
        throw new IndexOutOfBoundsException();
      } else if (len == 0) {
        return;
      }
      implWrite(cbuf, off, len);
    }
  }

  public void write(String str, int off, int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    char[] cbuf = new char[len];
    str.getChars(off, off + len, cbuf, 0);
    write(cbuf, 0, len);
  }

  public Writer append(CharSequence csq, int start, int end) throws IOException {
    if (csq == null) {
      csq = "null";
    }
    return append(csq.subSequence(start, end));
  }

  public Writer append(CharSequence csq) throws IOException {
    write(String.valueOf(csq));
    return this;
  }

  public void flush() throws IOException {
    synchronized (lock) {
      ensureOpen();
      implFlush();
    }
  }

  public void close() throws IOException {
    synchronized (lock) {
      if (closed) {
        return;
      }
      try {
        implClose();
      } finally {
        closed = true;
      }
    }
  }

  private boolean isOpen() {
    return !closed;
  }

  public String getEncoding() {
    if (isOpen()) {
      return cs instanceof HistoricallyNamedCharset ? ((HistoricallyNamedCharset) cs).historicalName()
          : cs.name();
    }
    return null;
  }

  private void writeBytes() throws IOException {
    bb.flip();
    int lim = bb.limit();
    int pos = bb.position();
    int rem = pos <= lim ? lim - pos : 0;
    if (rem > 0) {
      out.write(bb.array(), bb.arrayOffset() + pos, rem);
    }
    bb.clear();
  }

  private void flushLeftoverChar(CharBuffer cb, boolean endOfInput) throws IOException {
    if (!haveLeftoverChar && !endOfInput) {
      return;
    }
    if (lcb == null) {
      lcb = CharBuffer.allocate(2);
    } else {
      lcb.clear();
    }
    if (haveLeftoverChar) {
      lcb.put(leftoverChar);
    }
    if (cb != null && cb.hasRemaining()) {
      lcb.put(cb.get());
    }
    lcb.flip();
    while (lcb.hasRemaining() || endOfInput) {
      CoderResult cr = encoder.encode(lcb, bb, endOfInput);
      if (cr.isUnderflow()) {
        if (lcb.hasRemaining()) {
          leftoverChar = lcb.get();
          if (cb != null && cb.hasRemaining()) {
            lcb.clear();
            lcb.put(leftoverChar).put(cb.get()).flip();
            continue;
          }
          return;
        }
        break;
      }
      if (cr.isOverflow()) {
        writeBytes();
        continue;
      }
      cr.throwException();
    }
    haveLeftoverChar = false;
  }

  private void implWrite(char[] cbuf, int off, int len) throws IOException {
    CharBuffer cb = CharBuffer.wrap(cbuf, off, len);
    if (haveLeftoverChar) {
      flushLeftoverChar(cb, false);
    }
    while (cb.hasRemaining()) {
      CoderResult cr = encoder.encode(cb, bb, false);
      if (cr.isUnderflow()) {
        if (cb.remaining() == 1) {
          haveLeftoverChar = true;
          leftoverChar = cb.get();
        }
        break;
      }
      if (cr.isOverflow()) {
        writeBytes();
        continue;
      }
      cr.throwException();
    }
  }

  private void implFlushBuffer() throws IOException {
    if (bb.position() > 0) {
      writeBytes();
    }
  }

  private void implFlush() throws IOException {
    implFlushBuffer();
    if (out != null) {
      out.flush();
    }
  }

  private void implClose() throws IOException {
    flushLeftoverChar(null, true);
    try {
      for (;;) {
        CoderResult cr = encoder.flush(bb);
        if (cr.isUnderflow()) {
          break;
        }
        if (cr.isOverflow()) {
          writeBytes();
          continue;
        }
        cr.throwException();
      }
      if (bb.position() > 0) {
        writeBytes();
      }
      if (out != null) {
        out.close();
      }
    } catch (IOException x) {
      encoder.reset();
      throw x;
    }
  }
}
