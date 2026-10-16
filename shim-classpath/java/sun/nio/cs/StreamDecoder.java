/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.cs;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class StreamDecoder extends Reader {
  private static final int kDefaultByteBufferSize = 8192;

  private volatile boolean closed;
  private final Charset cs;
  private final CharsetDecoder decoder;
  private final ByteBuffer bb;
  private final InputStream in;
  private boolean haveLeftoverChar;
  private char leftoverChar;

  public static StreamDecoder forInputStreamReader(InputStream in, Object lock, String charsetName)
      throws UnsupportedEncodingException {
    String csn = charsetName;
    if (csn == null) {
      csn = Charset.defaultCharset().name();
    }
    try {
      if (Charset.isSupported(csn)) {
        return new StreamDecoder(in, lock, Charset.forName(csn));
      }
    } catch (IllegalArgumentException x) {
      // fall through to the unsupported encoding error
    }
    throw new UnsupportedEncodingException(csn);
  }

  public static StreamDecoder forInputStreamReader(InputStream in, Object lock, Charset cs) {
    return new StreamDecoder(in, lock, cs);
  }

  public static StreamDecoder forInputStreamReader(InputStream in, Object lock, CharsetDecoder dec) {
    return new StreamDecoder(in, lock, dec);
  }

  StreamDecoder(InputStream in, Object lock, Charset cs) {
    this(in, lock, cs.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE));
  }

  StreamDecoder(InputStream in, Object lock, CharsetDecoder dec) {
    super(lock);
    this.cs = dec.charset();
    this.decoder = dec;
    this.in = in;
    this.bb = ByteBuffer.allocate(kDefaultByteBufferSize);
    bb.flip();
  }

  private void ensureOpen() throws IOException {
    if (closed) {
      throw new IOException("Stream closed");
    }
  }

  public String getEncoding() {
    if (isOpen()) {
      return encodingName();
    }
    return null;
  }

  private String encodingName() {
    return cs instanceof HistoricallyNamedCharset ? ((HistoricallyNamedCharset) cs).historicalName() : cs.name();
  }

  public int read() throws IOException {
    return read0();
  }

  private int read0() throws IOException {
    synchronized (lock) {
      if (haveLeftoverChar) {
        haveLeftoverChar = false;
        return leftoverChar;
      }
      char[] cb = new char[2];
      int n = read(cb, 0, 2);
      switch (n) {
      case -1:
        return -1;
      case 2:
        leftoverChar = cb[1];
        haveLeftoverChar = true;
        return cb[0];
      case 1:
        return cb[0];
      default:
        return -1;
      }
    }
  }

  public int read(char[] cbuf, int offset, int length) throws IOException {
    int off = offset;
    int len = length;
    synchronized (lock) {
      ensureOpen();
      if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0) {
        throw new IndexOutOfBoundsException();
      }
      if (len == 0) {
        return 0;
      }
      int n = 0;
      if (haveLeftoverChar) {
        cbuf[off] = leftoverChar;
        off++;
        len--;
        haveLeftoverChar = false;
        n = 1;
        if (len == 0 || !implReady()) {
          return n;
        }
      }
      if (len == 1) {
        int c = read0();
        if (c == -1) {
          return n == 0 ? -1 : n;
        }
        cbuf[off] = (char) c;
        return n + 1;
      }
      return n + implRead(cbuf, off, off + len);
    }
  }

  public boolean ready() throws IOException {
    synchronized (lock) {
      ensureOpen();
      return haveLeftoverChar || implReady();
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

  private int readBytes() throws IOException {
    bb.compact();
    try {
      int lim = bb.limit();
      int pos = bb.position();
      int rem = pos <= lim ? lim - pos : 0;
      int n = in.read(bb.array(), bb.arrayOffset() + pos, rem);
      if (n < 0) {
        return n;
      }
      if (n == 0) {
        throw new IOException("Underlying input stream returned zero bytes");
      }
      bb.position(pos + n);
    } finally {
      bb.flip();
    }
    return bb.remaining();
  }

  int implRead(char[] cbuf, int off, int end) throws IOException {
    CharBuffer cb = CharBuffer.wrap(cbuf, off, end - off);
    if (cb.position() != 0) {
      cb = cb.slice();
    }
    boolean eof = false;
    for (;;) {
      CoderResult cr = decoder.decode(bb, cb, eof);
      if (cr.isUnderflow()) {
        if (eof) {
          break;
        }
        if (!cb.hasRemaining()) {
          break;
        }
        if (cb.position() > 0 && !inReady()) {
          break;
        }
        int n = readBytes();
        if (n < 0) {
          eof = true;
          if (cb.position() == 0 && !bb.hasRemaining()) {
            break;
          }
          decoder.reset();
        }
        continue;
      }
      if (cr.isOverflow()) {
        break;
      }
      cr.throwException();
    }
    if (eof) {
      decoder.reset();
    }
    if (cb.position() == 0) {
      if (eof) {
        return -1;
      }
    }
    return cb.position();
  }

  private boolean inReady() {
    try {
      return in != null && in.available() > 0;
    } catch (IOException x) {
      return false;
    }
  }

  boolean implReady() {
    return bb.hasRemaining() || inReady();
  }

  void implClose() throws IOException {
    in.close();
  }
}
