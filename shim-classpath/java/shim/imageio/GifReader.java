/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.IIOException;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public final class GifReader extends CodecReader {
  private static final class Frame {
    int left;
    int top;
    int width;
    int height;
    boolean interlaced;
    int transparent = -1;
    int[] palette;
    byte[] pixels;
  }

  private List<Frame> frames;

  public GifReader(ImageReaderSpi spi) {
    super(spi);
  }

  protected String[] nativeFormat() { return new String[] {"javax_imageio_gif_image_1.0", "lzw", "true"}; }

  protected void resetState() {
    frames = null;
  }

  private void parse() throws IOException {
    if (frames != null) {
      return;
    }
    List<Frame> out = new ArrayList<>();
    ImageInputStream in = stream();
    in.seek(0);
    in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    byte[] sig = new byte[6];
    in.readFully(sig);
    if (sig[0] != 'G' || sig[1] != 'I' || sig[2] != 'F') {
      throw new IIOException("Bad GIF signature");
    }
    in.readUnsignedShort();
    in.readUnsignedShort();
    int flags = in.readUnsignedByte();
    in.readUnsignedByte();
    in.readUnsignedByte();
    int[] global = null;
    if ((flags & 0x80) != 0) {
      global = readPalette(in, 1 << ((flags & 7) + 1));
    }
    int transparent = -1;
    while (true) {
      int b = in.read();
      if (b < 0 || b == 0x3b) {
        break;
      }
      if (b == 0x21) {
        int label = in.readUnsignedByte();
        if (label == 0xf9) {
          int size = in.readUnsignedByte();
          int pf = in.readUnsignedByte();
          in.readUnsignedShort();
          int ti = in.readUnsignedByte();
          transparent = (pf & 1) != 0 ? ti : -1;
          in.skipBytes(size - 4);
          skipBlocks(in);
        } else {
          skipBlocks(in);
        }
      } else if (b == 0x2c) {
        Frame f = new Frame();
        f.left = in.readUnsignedShort();
        f.top = in.readUnsignedShort();
        f.width = in.readUnsignedShort();
        f.height = in.readUnsignedShort();
        int lf = in.readUnsignedByte();
        f.interlaced = (lf & 0x40) != 0;
        f.palette = (lf & 0x80) != 0 ? readPalette(in, 1 << ((lf & 7) + 1)) : global;
        f.transparent = transparent;
        transparent = -1;
        int minCode = in.readUnsignedByte();
        java.io.ByteArrayOutputStream data = new java.io.ByteArrayOutputStream();
        while (true) {
          int n = in.readUnsignedByte();
          if (n == 0) {
            break;
          }
          byte[] chunk = new byte[n];
          in.readFully(chunk);
          data.write(chunk, 0, n);
        }
        f.pixels = lzw(data.toByteArray(), minCode, f.width * f.height);
        out.add(f);
      } else {
        break;
      }
    }
    if (out.isEmpty()) {
      throw new IIOException("No image data in GIF");
    }
    frames = out;
  }

  private static int[] readPalette(ImageInputStream in, int n) throws IOException {
    int[] pal = new int[n];
    for (int i = 0; i < n; i++) {
      pal[i] = 0xff000000 | (in.readUnsignedByte() << 16) | (in.readUnsignedByte() << 8) | in.readUnsignedByte();
    }
    return pal;
  }

  private static void skipBlocks(ImageInputStream in) throws IOException {
    while (true) {
      int n = in.readUnsignedByte();
      if (n == 0) {
        return;
      }
      in.skipBytes(n);
    }
  }

  private static byte[] lzw(byte[] data, int minCode, int count) {
    byte[] out = new byte[count];
    int clear = 1 << minCode;
    int eoi = clear + 1;
    int codeSize = minCode + 1;
    int next = eoi + 1;
    short[] prefix = new short[4096];
    byte[] suffix = new byte[4096];
    byte[] stack = new byte[4097];
    for (int i = 0; i < clear; i++) {
      suffix[i] = (byte) i;
    }
    int bitBuf = 0;
    int bits = 0;
    int pos = 0;
    int o = 0;
    int old = -1;
    byte first = 0;
    while (o < count) {
      while (bits < codeSize && pos < data.length) {
        bitBuf |= (data[pos++] & 0xff) << bits;
        bits += 8;
      }
      if (bits < codeSize) {
        break;
      }
      int code = bitBuf & ((1 << codeSize) - 1);
      bitBuf >>>= codeSize;
      bits -= codeSize;
      if (code == clear) {
        codeSize = minCode + 1;
        next = eoi + 1;
        old = -1;
        continue;
      }
      if (code == eoi) {
        break;
      }
      if (old == -1) {
        out[o++] = suffix[code];
        old = code;
        first = (byte) code;
        continue;
      }
      int in = code;
      int sp = 0;
      if (code >= next) {
        stack[sp++] = first;
        code = old;
      }
      while (code >= clear) {
        stack[sp++] = suffix[code];
        code = prefix[code];
      }
      first = suffix[code];
      stack[sp++] = first;
      if (next < 4096) {
        prefix[next] = (short) old;
        suffix[next] = first;
        next++;
        if (next == (1 << codeSize) && codeSize < 12) {
          codeSize++;
        }
      }
      old = in;
      while (sp > 0 && o < count) {
        out[o++] = stack[--sp];
      }
    }
    return out;
  }

  public javax.imageio.metadata.IIOMetadata getStreamMetadata() throws IOException {
    return new GifMetadata(stream(), true, 0);
  }

  public javax.imageio.metadata.IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return new GifMetadata(stream(), false, imageIndex);
  }

  protected int imageCount() throws IOException {
    parse();
    return frames.size();
  }

  protected BufferedImage decode(int index) throws IOException {
    parse();
    Frame f = frames.get(index);
    int n = f.palette == null ? 2 : f.palette.length;
    int bits = n <= 2 ? 1 : n <= 4 ? 2 : n <= 16 ? 4 : 8;
    int size = 1 << bits;
    byte[] r = new byte[size];
    byte[] g = new byte[size];
    byte[] b = new byte[size];
    byte[] a = new byte[size];
    java.util.Arrays.fill(a, (byte) 255);
    for (int i = 0; i < size; i++) {
      int c = f.palette != null && i < f.palette.length ? f.palette[i] : 0;
      r[i] = (byte) (c >> 16);
      g[i] = (byte) (c >> 8);
      b[i] = (byte) c;
    }
    IndexColorModel cm;
    if (f.transparent >= 0 && f.transparent < size) {
      a[f.transparent] = 0;
      cm = new IndexColorModel(bits, size, r, g, b, a);
    } else {
      cm = new IndexColorModel(bits, size, r, g, b);
    }
    BufferedImage bi = new BufferedImage(f.width, f.height, bits == 8 ? BufferedImage.TYPE_BYTE_INDEXED
        : BufferedImage.TYPE_BYTE_BINARY, cm);
    int[] row = new int[f.width];
    int[] order = rowOrder(f.height, f.interlaced);
    for (int y = 0; y < f.height; y++) {
      for (int x = 0; x < f.width; x++) {
        row[x] = f.pixels[y * f.width + x] & 0xff;
        if (row[x] >= size) {
          row[x] = 0;
        }
      }
      bi.getRaster().setPixels(0, order[y], f.width, 1, row);
    }
    return bi;
  }

  private static int[] rowOrder(int h, boolean interlaced) {
    int[] order = new int[h];
    if (!interlaced) {
      for (int i = 0; i < h; i++) {
        order[i] = i;
      }
      return order;
    }
    int k = 0;
    int[] start = {0, 4, 2, 1};
    int[] step = {8, 8, 4, 2};
    for (int p = 0; p < 4; p++) {
      for (int y = start[p]; y < h; y += step[p]) {
        order[k++] = y;
      }
    }
    return order;
  }
}
