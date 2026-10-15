/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Base64 {
  private static final char[] RFC4648 =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
  private static final char[] RFC4648_URL =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();

  private static final byte[] CRLF = new byte[] { '\r', '\n' };

  private static final Encoder RFC4648_ENCODER = new Encoder(RFC4648, null, -1, true);
  private static final Encoder RFC4648_URL_ENCODER = new Encoder(RFC4648_URL, null, -1, true);
  private static final Encoder RFC2045_MIME_ENCODER = new Encoder(RFC4648, CRLF, 76, true);

  private static final Decoder RFC4648_DECODER = new Decoder(false, false);
  private static final Decoder RFC4648_URL_DECODER = new Decoder(true, false);
  private static final Decoder RFC2045_MIME_DECODER = new Decoder(false, true);

  private Base64() {}

  public static Encoder getEncoder() {
    return RFC4648_ENCODER;
  }

  public static Encoder getUrlEncoder() {
    return RFC4648_URL_ENCODER;
  }

  public static Encoder getMimeEncoder() {
    return RFC2045_MIME_ENCODER;
  }

  public static Encoder getMimeEncoder(int lineLength, byte[] lineSeparator) {
    Objects.requireNonNull(lineSeparator, "lineSeparator");
    int[] table = Decoder.FROM_BASE64;
    for (byte b : lineSeparator) {
      if (b >= 0 && table[b] != -1) {
        throw new IllegalArgumentException(
            "Illegal base64 character in line separator 0x" + Integer.toHexString(b));
      }
    }
    lineLength = (lineLength >> 2) << 2;
    if (lineLength <= 0) {
      return RFC4648_ENCODER;
    }
    return new Encoder(RFC4648, lineSeparator, lineLength, true);
  }

  public static Decoder getDecoder() {
    return RFC4648_DECODER;
  }

  public static Decoder getUrlDecoder() {
    return RFC4648_URL_DECODER;
  }

  public static Decoder getMimeDecoder() {
    return RFC2045_MIME_DECODER;
  }

  public static class Encoder {
    private final char[] alphabet;
    private final byte[] newline;
    private final int lineLength;
    private final boolean doPadding;

    private Encoder(char[] alphabet, byte[] newline, int lineLength, boolean doPadding) {
      this.alphabet = alphabet;
      this.newline = newline;
      this.lineLength = lineLength;
      this.doPadding = doPadding;
    }

    public Encoder withoutPadding() {
      if (!doPadding) return this;
      return new Encoder(alphabet, newline, lineLength, false);
    }

    public byte[] encode(byte[] src) {
      int len = outLength(src.length);
      byte[] dst = new byte[len];
      int ret = encode(src, 0, src.length, dst);
      if (ret != dst.length) {
        byte[] copy = new byte[ret];
        System.arraycopy(dst, 0, copy, 0, ret);
        return copy;
      }
      return dst;
    }

    public int encode(byte[] src, byte[] dst) {
      int len = outLength(src.length);
      if (dst.length < len) {
        throw new IllegalArgumentException(
            "Output byte array is too small for encoding all input bytes: "
                + dst.length
                + " < "
                + len);
      }
      return encode(src, 0, src.length, dst);
    }

    public String encodeToString(byte[] src) {
      byte[] encoded = encode(src);
      char[] chars = new char[encoded.length];
      for (int i = 0; i < encoded.length; ++i) {
        chars[i] = (char) encoded[i];
      }
      return new String(chars);
    }

    public ByteBuffer encode(ByteBuffer buffer) {
      int len = outLength(buffer.remaining());
      byte[] dst = new byte[len];
      int ret;
      if (buffer.hasArray()) {
        ret = encode(buffer.array(),
                     buffer.arrayOffset() + buffer.position(),
                     buffer.arrayOffset() + buffer.limit(),
                     dst);
        buffer.position(buffer.limit());
      } else {
        byte[] src = new byte[buffer.remaining()];
        buffer.get(src);
        ret = encode(src, 0, src.length, dst);
      }
      if (ret != dst.length) {
        byte[] copy = new byte[ret];
        System.arraycopy(dst, 0, copy, 0, ret);
        dst = copy;
      }
      return ByteBuffer.wrap(dst);
    }

    public OutputStream wrap(OutputStream os) {
      Objects.requireNonNull(os, "os");
      return new FilterOutputStream(os) {
        private int b0, b1, b2;
        private int bits = 0;
        private int linePos = 0;

        @Override
        public void write(int b) throws IOException {
          byte[] buf = new byte[] { (byte) b };
          write(buf, 0, 1);
        }

        @Override
        public void write(byte[] b) throws IOException {
          write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
          Objects.checkFromIndexSize(off, len, b.length);
          for (int i = 0; i < len; ++i) {
            byte val = b[off + i];
            if (bits == 0) {
              b0 = val & 0xFF;
              bits = 8;
            } else if (bits == 8) {
              b1 = val & 0xFF;
              bits = 16;
            } else {
              b2 = val & 0xFF;
              writeEncoded(b0, b1, b2, 3);
              bits = 0;
            }
          }
        }

        private void writeEncoded(int v0, int v1, int v2, int count) throws IOException {
          int bits24 = (v0 << 16) | (v1 << 8) | v2;
          char c0 = alphabet[(bits24 >>> 18) & 0x3F];
          char c1 = alphabet[(bits24 >>> 12) & 0x3F];
          char c2 = count > 1 ? alphabet[(bits24 >>> 6) & 0x3F] : '=';
          char c3 = count > 2 ? alphabet[bits24 & 0x3F] : '=';

          writeChar(c0);
          writeChar(c1);
          if (count > 1 || doPadding) writeChar(c2);
          if (count > 2 || doPadding) writeChar(c3);
        }

        private void writeChar(char c) throws IOException {
          if (lineLength > 0 && linePos == lineLength) {
            out.write(newline);
            linePos = 0;
          }
          out.write((byte) c);
          linePos++;
        }

        @Override
        public void close() throws IOException {
          if (bits == 8) {
            writeEncoded(b0, 0, 0, 1);
            bits = 0;
          } else if (bits == 16) {
            writeEncoded(b0, b1, 0, 2);
            bits = 0;
          }
          out.close();
        }
      };
    }

    private int outLength(int srclen) {
      int len = 0;
      if (doPadding) {
        len = 4 * ((srclen + 2) / 3);
      } else {
        int n = srclen % 3;
        len = 4 * (srclen / 3) + (n == 0 ? 0 : n + 1);
      }
      if (lineLength > 0) {
        len += (len - 1) / lineLength * newline.length;
      }
      return len;
    }

    private int encode(byte[] src, int sp, int sl, byte[] dst) {
      int dp = 0;
      int bpos = 0;
      while (sp < sl) {
        int remaining = sl - sp;
        int b0 = src[sp++] & 0xFF;
        if (remaining == 1) {
          int bits = b0 << 16;
          dst[dp++] = (byte) alphabet[(bits >>> 18) & 0x3F];
          dst[dp++] = (byte) alphabet[(bits >>> 12) & 0x3F];
          if (doPadding) {
            dst[dp++] = '=';
            dst[dp++] = '=';
          }
          break;
        }
        int b1 = src[sp++] & 0xFF;
        if (remaining == 2) {
          int bits = (b0 << 16) | (b1 << 8);
          dst[dp++] = (byte) alphabet[(bits >>> 18) & 0x3F];
          dst[dp++] = (byte) alphabet[(bits >>> 12) & 0x3F];
          dst[dp++] = (byte) alphabet[(bits >>> 6) & 0x3F];
          if (doPadding) {
            dst[dp++] = '=';
          }
          break;
        }
        int b2 = src[sp++] & 0xFF;
        int bits = (b0 << 16) | (b1 << 8) | b2;
        dst[dp++] = (byte) alphabet[(bits >>> 18) & 0x3F];
        dst[dp++] = (byte) alphabet[(bits >>> 12) & 0x3F];
        dst[dp++] = (byte) alphabet[(bits >>> 6) & 0x3F];
        dst[dp++] = (byte) alphabet[bits & 0x3F];
        bpos += 4;
        if (lineLength > 0 && bpos == lineLength && sp < sl) {
          for (byte b : newline) {
            dst[dp++] = b;
          }
          bpos = 0;
        }
      }
      return dp;
    }
  }

  public static class Decoder {
    private static final int[] FROM_BASE64 = new int[256];
    private static final int[] FROM_BASE64_URL = new int[256];

    static {
      Arrays.fill(FROM_BASE64, -1);
      for (int i = 0; i < RFC4648.length; i++) {
        FROM_BASE64[RFC4648[i]] = i;
      }
      FROM_BASE64['='] = -2;

      Arrays.fill(FROM_BASE64_URL, -1);
      for (int i = 0; i < RFC4648_URL.length; i++) {
        FROM_BASE64_URL[RFC4648_URL[i]] = i;
      }
      FROM_BASE64_URL['='] = -2;
    }

    private final boolean isUrl;
    private final boolean isMime;

    private Decoder(boolean isUrl, boolean isMime) {
      this.isUrl = isUrl;
      this.isMime = isMime;
    }

    public byte[] decode(byte[] src) {
      byte[] dst = new byte[outLength(src, 0, src.length)];
      int ret = decode(src, 0, src.length, dst);
      if (ret != dst.length) {
        byte[] copy = new byte[ret];
        System.arraycopy(dst, 0, copy, 0, ret);
        return copy;
      }
      return dst;
    }

    public byte[] decode(String src) {
      byte[] bytes = new byte[src.length()];
      for (int i = 0; i < src.length(); ++i) {
        bytes[i] = (byte) src.charAt(i);
      }
      return decode(bytes);
    }

    public int decode(byte[] src, byte[] dst) {
      int len = outLength(src, 0, src.length);
      if (dst.length < len) {
        throw new IllegalArgumentException(
            "Output byte array is too small for decoding all input bytes: "
                + dst.length
                + " < "
                + len);
      }
      return decode(src, 0, src.length, dst);
    }

    public ByteBuffer decode(ByteBuffer buffer) {
      byte[] src;
      int sp, sl;
      if (buffer.hasArray()) {
        src = buffer.array();
        sp = buffer.arrayOffset() + buffer.position();
        sl = buffer.arrayOffset() + buffer.limit();
        buffer.position(buffer.limit());
      } else {
        src = new byte[buffer.remaining()];
        buffer.get(src);
        sp = 0;
        sl = src.length;
      }
      byte[] dst = new byte[outLength(src, sp, sl)];
      int ret = decode(src, sp, sl, dst);
      if (ret != dst.length) {
        byte[] copy = new byte[ret];
        System.arraycopy(dst, 0, copy, 0, ret);
        dst = copy;
      }
      return ByteBuffer.wrap(dst);
    }

    public InputStream wrap(InputStream is) {
      Objects.requireNonNull(is, "is");
      return new FilterInputStream(is) {
        private final byte[] buffer = new byte[1];
        private byte[] decodedChunk = null;
        private int chunkPos = 0;
        private boolean closed = false;
        private boolean eof = false;

        @Override
        public int read() throws IOException {
          int ret = read(buffer, 0, 1);
          return ret == -1 ? -1 : (buffer[0] & 0xFF);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
          Objects.checkFromIndexSize(off, len, b.length);
          if (closed) throw new IOException("Stream closed");
          if (len == 0) return 0;
          if (decodedChunk == null || chunkPos >= decodedChunk.length) {
            if (eof) return -1;
            ByteArrayOutputStream raw = new ByteArrayOutputStream();
            int c;
            while ((c = in.read()) != -1) {
              raw.write(c);
            }
            eof = true;
            byte[] inBytes = raw.toByteArray();
            if (inBytes.length == 0) return -1;
            decodedChunk = decode(inBytes);
            chunkPos = 0;
            if (decodedChunk.length == 0) return -1;
          }
          int available = decodedChunk.length - chunkPos;
          int toCopy = Math.min(len, available);
          System.arraycopy(decodedChunk, chunkPos, b, off, toCopy);
          chunkPos += toCopy;
          return toCopy;
        }

        @Override
        public void close() throws IOException {
          closed = true;
          in.close();
        }
      };
    }

    private int outLength(byte[] src, int sp, int sl) {
      int[] table = isUrl ? FROM_BASE64_URL : FROM_BASE64;
      int paddings = 0;
      int len = sl - sp;
      if (len == 0) return 0;
      if (len < 2) {
        if (isMime && table[src[sp] & 0xFF] == -1) return 0;
        throw new IllegalArgumentException("Input byte[] should at least have 2 bytes for base64 bytes: " + len);
      }
      if (isMime) {
        int n = 0;
        while (sp < sl) {
          int b = src[sp++] & 0xFF;
          if (b == '=') {
            len -= (sl - sp + 1);
            break;
          }
          if (table[b] == -1) {
            n++;
          }
        }
        len -= n;
      } else {
        if (src[sl - 1] == '=') {
          paddings++;
          if (src[sl - 2] == '=') {
            paddings++;
          }
        }
      }
      if (paddings == 0 && (len & 0x3) != 0) {
        paddings = 4 - (len & 0x3);
      }
      return 3 * ((len + 3) / 4) - paddings;
    }

    private int decode(byte[] src, int sp, int sl, byte[] dst) {
      int[] table = isUrl ? FROM_BASE64_URL : FROM_BASE64;
      int dp = 0;
      int bits = 0;
      int bitCount = 0;
      while (sp < sl) {
        int b = src[sp++] & 0xFF;
        int val = table[b];
        if (val == -1) {
          if (isMime) continue;
          throw new IllegalArgumentException("Illegal base64 character 0x" + Integer.toHexString(b));
        }
        if (val == -2) {
          // padding '='
          break;
        }
        bits = (bits << 6) | val;
        bitCount += 6;
        if (bitCount >= 8) {
          bitCount -= 8;
          dst[dp++] = (byte) ((bits >>> bitCount) & 0xFF);
        }
      }
      return dp;
    }
  }
}
