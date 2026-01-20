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

package java.nio.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Objects;

public class Channels {
  public static InputStream newInputStream(ReadableByteChannel channel) {
    return new MyInputStream(channel);
  }

  public static OutputStream newOutputStream(WritableByteChannel channel) {
    return new MyOutputStream(channel);
  }

  public static InputStream newInputStream(AsynchronousByteChannel channel) {
    Objects.requireNonNull(channel, "ch");
    return new AsyncInputStream(channel);
  }

  public static OutputStream newOutputStream(AsynchronousByteChannel channel) {
    Objects.requireNonNull(channel, "ch");
    return new AsyncOutputStream(channel);
  }

  private static final class AsyncInputStream extends InputStream {
    private final AsynchronousByteChannel channel;

    AsyncInputStream(AsynchronousByteChannel channel) {
      this.channel = channel;
    }

    @Override
    public int read() throws IOException {
      byte[] one = new byte[1];
      int n = read(one, 0, 1);
      return n <= 0 ? -1 : one[0] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
      Objects.checkFromIndexSize(off, len, b.length);
      if (len == 0) {
        return 0;
      }
      try {
        return channel.read(ByteBuffer.wrap(b, off, len)).get();
      } catch (java.util.concurrent.ExecutionException e) {
        Throwable c = e.getCause();
        throw c instanceof IOException ? (IOException) c : new IOException(c);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException(e);
      }
    }

    @Override
    public void close() throws IOException {
      channel.close();
    }
  }

  private static final class AsyncOutputStream extends OutputStream {
    private final AsynchronousByteChannel channel;

    AsyncOutputStream(AsynchronousByteChannel channel) {
      this.channel = channel;
    }

    @Override
    public void write(int b) throws IOException {
      write(new byte[] {(byte) b}, 0, 1);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      Objects.checkFromIndexSize(off, len, b.length);
      ByteBuffer bb = ByteBuffer.wrap(b, off, len);
      try {
        while (bb.hasRemaining()) {
          channel.write(bb).get();
        }
      } catch (java.util.concurrent.ExecutionException e) {
        Throwable c = e.getCause();
        throw c instanceof IOException ? (IOException) c : new IOException(c);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException(e);
      }
    }

    @Override
    public void close() throws IOException {
      channel.close();
    }
  }

  public static ReadableByteChannel newChannel(InputStream stream) {
    return new InputStreamChannel(stream);
  }

  public static WritableByteChannel newChannel(OutputStream stream) {
    return new OutputStreamChannel(stream);
  }

  public static Reader newReader(ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap) {
    return new ChannelReader(ch, dec, minBufferCap);
  }

  public static Reader newReader(ReadableByteChannel ch, String csName) {
    return newReader(ch, Charset.forName(csName).newDecoder(), -1);
  }

  public static Reader newReader(ReadableByteChannel ch, Charset charset) {
    return newReader(ch, Objects.requireNonNull(charset, "charset").newDecoder(), -1);
  }

  public static Writer newWriter(WritableByteChannel ch, CharsetEncoder enc, int minBufferCap) {
    return new ChannelWriter(ch, enc, minBufferCap);
  }

  public static Writer newWriter(WritableByteChannel ch, String csName) {
    return newWriter(ch, Charset.forName(csName).newEncoder(), -1);
  }

  public static Writer newWriter(WritableByteChannel ch, Charset charset) {
    return newWriter(ch, Objects.requireNonNull(charset, "charset").newEncoder(), -1);
  }

  private static class MyInputStream extends InputStream {
    private final ReadableByteChannel channel;

    public MyInputStream(ReadableByteChannel channel) {
      this.channel = channel;
    }

    public int read() throws IOException {
      byte[] buffer = new byte[1];
      int r = read(buffer);
      if (r == -1) {
        return -1;
      } else {
        return buffer[0] & 0xFF;
      }
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
      return channel.read(ByteBuffer.wrap(buffer, offset, length));
    }

    public void close() throws IOException {
      channel.close();
    }
  }

  private static class MyOutputStream extends OutputStream {
    private final WritableByteChannel channel;

    public MyOutputStream(WritableByteChannel channel) {
      this.channel = channel;
    }

    public void write(int v) throws IOException {
      byte[] buffer = new byte[] { (byte) (v & 0xFF) };
      write(buffer);
    }

    public void write(byte[] buffer, int offset, int length)
      throws IOException
    {
      channel.write(ByteBuffer.wrap(buffer, offset, length));
    }

    public void close() throws IOException {
      channel.close();
    }
  }

  private static class InputStreamChannel implements ReadableByteChannel {
    private InputStream stream;

    public InputStreamChannel(InputStream stream) {
      this.stream = stream;
    }

    public void close() throws IOException {
      if (stream != null) {
        stream.close();
        stream = null;
      }
    }

    public boolean isOpen() {
      return stream != null;
    }

    public int read(ByteBuffer b) throws IOException {
      if (b.hasArray()) {
        int c = stream.read
          (b.array(), b.arrayOffset() + b.position(), b.remaining());
        if (c > 0) {
          b.position(b.position() + c);
        }
        return c;
      } else {
        int len = Math.min(b.remaining(), 8192);
        byte[] buf = new byte[len];
        int c = stream.read(buf, 0, len);
        if (c > 0) {
          b.put(buf, 0, c);
        }
        return c;
      }
    }
  }

  private static class OutputStreamChannel implements WritableByteChannel {
    private OutputStream stream;

    public OutputStreamChannel(OutputStream stream) {
      this.stream = stream;
    }

    public void close() throws IOException {
      if (stream != null) {
        stream.close();
        stream = null;
      }
    }

    public boolean isOpen() {
      return stream != null;
    }

    public int write(ByteBuffer b) throws IOException {
      if (b.hasArray()) {
        stream.write(b.array(), b.arrayOffset() + b.position(), b.remaining());
        int c = b.remaining();
        b.position(b.limit());
        return c;
      } else {
        int c = b.remaining();
        byte[] buf = new byte[Math.min(c, 8192)];
        int total = 0;
        while (b.hasRemaining()) {
          int len = Math.min(b.remaining(), buf.length);
          b.get(buf, 0, len);
          stream.write(buf, 0, len);
          total += len;
        }
        return total;
      }
    }
  }

  private static class ChannelReader extends Reader {
    private final ReadableByteChannel ch;
    private final CharsetDecoder dec;
    private final ByteBuffer bb;
    private final CharBuffer cb;
    private boolean eof = false;

    ChannelReader(ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap) {
      this.ch = Objects.requireNonNull(ch, "ch");
      this.dec = Objects.requireNonNull(dec, "dec");
      int cap = minBufferCap > 0 ? minBufferCap : 8192;
      this.bb = ByteBuffer.allocate(cap);
      this.bb.flip();
      this.cb = CharBuffer.allocate((int) Math.max(16, cap * dec.averageCharsPerByte()));
      this.cb.flip();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
      if (len == 0) return 0;
      Objects.checkFromIndexSize(off, len, cbuf.length);

      int readCount = 0;
      while (readCount < len) {
        if (cb.hasRemaining()) {
          int count = Math.min(len - readCount, cb.remaining());
          cb.get(cbuf, off + readCount, count);
          readCount += count;
          if (readCount == len) break;
        }

        if (!bb.hasRemaining() && !eof) {
          bb.compact();
          int n = ch.read(bb);
          bb.flip();
          if (n < 0) {
            eof = true;
          }
        }

        cb.clear();
        CoderResult cr = dec.decode(bb, cb, eof);
        cb.flip();

        if (cr.isError()) {
          cr.throwException();
        }

        if (!cb.hasRemaining() && eof && !bb.hasRemaining()) {
          cb.clear();
          CoderResult flushCr = dec.flush(cb);
          cb.flip();
          if (flushCr.isError()) {
            flushCr.throwException();
          }
          if (!cb.hasRemaining()) {
            return readCount > 0 ? readCount : -1;
          }
        }
      }
      return readCount;
    }

    @Override
    public void close() throws IOException {
      ch.close();
    }
  }

  private static class ChannelWriter extends Writer {
    private final WritableByteChannel ch;
    private final CharsetEncoder enc;
    private final ByteBuffer bb;
    private final CharBuffer cb;

    ChannelWriter(WritableByteChannel ch, CharsetEncoder enc, int minBufferCap) {
      this.ch = Objects.requireNonNull(ch, "ch");
      this.enc = Objects.requireNonNull(enc, "enc");
      int cap = minBufferCap > 0 ? minBufferCap : 8192;
      this.bb = ByteBuffer.allocate(cap);
      this.cb = CharBuffer.allocate(cap);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      Objects.checkFromIndexSize(off, len, cbuf.length);
      int p = off;
      int end = off + len;
      while (p < end) {
        int count = Math.min(end - p, cb.remaining());
        cb.put(cbuf, p, count);
        p += count;
        drain();
      }
    }

    private void drain() throws IOException {
      cb.flip();
      while (cb.hasRemaining()) {
        CoderResult cr = enc.encode(cb, bb, false);
        if (cr.isError()) {
          cr.throwException();
        }
        writeOut();
        if (cr.isUnderflow()) break;
      }
      cb.compact();
    }

    private void writeOut() throws IOException {
      bb.flip();
      while (bb.hasRemaining()) {
        ch.write(bb);
      }
      bb.clear();
    }

    @Override
    public void flush() throws IOException {
      drain();
      writeOut();
    }

    @Override
    public void close() throws IOException {
      drain();
      cb.flip();
      CoderResult cr = enc.encode(cb, bb, true);
      if (cr.isError()) cr.throwException();
      writeOut();
      cb.clear();
      CoderResult flushCr = enc.flush(bb);
      if (flushCr.isError()) flushCr.throwException();
      writeOut();
      ch.close();
    }
  }
}
