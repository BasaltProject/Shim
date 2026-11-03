/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

import java.io.IOException;
import java.io.InputStream;

public class AudioInputStream extends InputStream {
  private final InputStream stream;
  protected AudioFormat format;
  protected long frameLength;
  protected int frameSize;
  protected long framePos;
  private long markpos;
  private byte[] pushBackBuf = null;
  private int pushBackLen = 0;
  private byte[] markPushBackBuf = null;
  private int markPushBackLen = 0;

  public AudioInputStream(InputStream stream, AudioFormat format, long length) {
    super();
    this.format = format;
    this.frameLength = length;
    this.frameSize = format.getFrameSize();
    if (this.frameSize == AudioSystem.NOT_SPECIFIED || frameSize <= 0) {
      this.frameSize = 1;
    }
    this.stream = stream;
    framePos = 0;
    markpos = 0;
  }

  public AudioInputStream(TargetDataLine line) {
    TargetDataLineInputStream tstream = new TargetDataLineInputStream(line);
    format = line.getFormat();
    frameLength = AudioSystem.NOT_SPECIFIED;
    frameSize = format.getFrameSize();
    if (frameSize == AudioSystem.NOT_SPECIFIED || frameSize <= 0) {
      frameSize = 1;
    }
    this.stream = tstream;
    framePos = 0;
    markpos = 0;
  }

  public AudioFormat getFormat() { return format; }

  public long getFrameLength() { return frameLength; }

  public int read() throws IOException {
    if (frameSize != 1) {
      throw new IOException("cannot read a single byte if frame size > 1");
    }
    byte[] data = new byte[1];
    int temp = read(data);
    if (temp <= 0) {
      return -1;
    }
    return data[0] & 0xFF;
  }

  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  public int read(byte[] b, int off, int len) throws IOException {
    if ((len % frameSize) != 0) {
      len -= (len % frameSize);
      if (len == 0) {
        return 0;
      }
    }
    if (frameLength != AudioSystem.NOT_SPECIFIED) {
      if (framePos >= frameLength) {
        return -1;
      } else {
        if ((len / frameSize) > (frameLength - framePos)) {
          len = (int) (frameLength - framePos) * frameSize;
        }
      }
    }
    int bytesRead = 0;
    int thisOff = off;
    if (pushBackLen > 0 && len >= pushBackLen) {
      System.arraycopy(pushBackBuf, 0, b, off, pushBackLen);
      thisOff += pushBackLen;
      len -= pushBackLen;
      bytesRead += pushBackLen;
      pushBackLen = 0;
    }
    int thisBytesRead = stream.read(b, thisOff, len);
    if (thisBytesRead == -1) {
      return -1;
    }
    if (thisBytesRead > 0) {
      bytesRead += thisBytesRead;
    }
    if (bytesRead > 0) {
      pushBackLen = bytesRead % frameSize;
      if (pushBackLen > 0) {
        if (pushBackBuf == null) {
          pushBackBuf = new byte[frameSize];
        }
        System.arraycopy(b, off + bytesRead - pushBackLen, pushBackBuf, 0, pushBackLen);
        bytesRead -= pushBackLen;
      }
      framePos += bytesRead / frameSize;
    }
    return bytesRead;
  }

  public long skip(long n) throws IOException {
    if ((n % frameSize) != 0) {
      n -= (n % frameSize);
    }
    if (frameLength != AudioSystem.NOT_SPECIFIED) {
      if ((n / frameSize) > (frameLength - framePos)) {
        n = (frameLength - framePos) * frameSize;
      }
    }
    long temp = stream.skip(n);
    if (temp % frameSize != 0) {
      throw new IOException("Could not skip an integer number of frames.");
    }
    if (temp >= 0) {
      framePos += temp / frameSize;
    }
    return temp;
  }

  public int available() throws IOException {
    int temp = stream.available();
    if ((frameLength != AudioSystem.NOT_SPECIFIED) && ((temp / frameSize) > (frameLength - framePos))) {
      return (int) (frameLength - framePos) * frameSize;
    } else {
      return temp;
    }
  }

  public void close() throws IOException { stream.close(); }

  public void mark(int readlimit) {
    stream.mark(readlimit);
    if (markSupported()) {
      markpos = framePos;
      markPushBackLen = pushBackLen;
      if (markPushBackLen > 0) {
        if (markPushBackBuf == null) {
          markPushBackBuf = new byte[frameSize];
        }
        System.arraycopy(pushBackBuf, 0, markPushBackBuf, 0, markPushBackLen);
      }
    }
  }

  public void reset() throws IOException {
    stream.reset();
    framePos = markpos;
    pushBackLen = markPushBackLen;
    if (pushBackLen > 0) {
      if (pushBackBuf == null) {
        pushBackBuf = new byte[frameSize - 1];
      }
      System.arraycopy(markPushBackBuf, 0, pushBackBuf, 0, pushBackLen);
    }
  }

  public boolean markSupported() { return stream.markSupported(); }

  private static final class TargetDataLineInputStream extends InputStream {
    private final TargetDataLine line;

    TargetDataLineInputStream(TargetDataLine line) {
      this.line = line;
    }

    public int available() throws IOException { return line.available(); }

    public void close() throws IOException {
      if (line.isActive()) {
        line.flush();
        line.stop();
      }
      line.close();
    }

    public int read() throws IOException {
      byte[] b = new byte[1];
      int value = read(b, 0, 1);
      if (value == -1) {
        return -1;
      }
      value = b[0];
      if (line.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
        value += 128;
      }
      return value;
    }

    public int read(byte[] b, int off, int len) throws IOException {
      try {
        return line.read(b, off, len);
      } catch (IllegalArgumentException e) {
        throw new IOException(e.getMessage());
      }
    }
  }
}
