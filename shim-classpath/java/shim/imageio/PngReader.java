/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import javax.imageio.IIOException;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;

public final class PngReader extends CodecReader {
  private int width;
  private int height;
  private int depth;
  private int colorType;
  private int interlace;
  private byte[] palette;
  private byte[] trns;
  private byte[] idat;

  public PngReader(ImageReaderSpi spi) {
    super(spi);
  }

  public javax.imageio.metadata.IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    javax.imageio.metadata.IIOMetadata m = super.getImageMetadata(imageIndex);
    return ((ImageInfoMetadata) m).withNativeTree(PngNative.read(stream()));
  }

  protected String[] nativeFormat() { return new String[] {"javax_imageio_png_1.0", "deflate", "true"}; }

  protected void resetState() {
    idat = null;
  }

  protected int imageCount() throws IOException {
    return 1;
  }

  private void readChunks() throws IOException {
    if (idat != null) {
      return;
    }
    javax.imageio.stream.ImageInputStream in = stream();
    in.seek(0);
    byte[] sig = new byte[8];
    in.readFully(sig);
    if ((sig[0] & 0xff) != 0x89 || sig[1] != 'P' || sig[2] != 'N' || sig[3] != 'G') {
      throw new IIOException("Bad PNG signature");
    }
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    boolean sawHeader = false;
    while (true) {
      int length;
      try {
        length = in.readInt();
      } catch (java.io.EOFException e) {
        break;
      }
      byte[] type = new byte[4];
      in.readFully(type);
      if (length < 0) {
        throw new IIOException("Bad chunk length");
      }
      byte[] body = new byte[length];
      in.readFully(body);
      in.readInt();
      String name = new String(type, "ISO-8859-1");
      if (name.equals("IHDR")) {
        width = i32(body, 0);
        height = i32(body, 4);
        depth = body[8] & 0xff;
        colorType = body[9] & 0xff;
        interlace = body[12] & 0xff;
        sawHeader = true;
      } else if (name.equals("PLTE")) {
        palette = body;
      } else if (name.equals("tRNS")) {
        trns = body;
      } else if (name.equals("IDAT")) {
        data.write(body, 0, body.length);
      } else if (name.equals("IEND")) {
        break;
      }
    }
    if (!sawHeader) {
      throw new IIOException("Missing IHDR chunk");
    }
    if (width <= 0 || height <= 0) {
      throw new IIOException("Bad image dimensions");
    }
    idat = data.toByteArray();
  }

  private static int i32(byte[] b, int o) {
    return ((b[o] & 0xff) << 24) | ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff);
  }

  private int channels() {
    switch (colorType) {
    case 0:
    case 3:
      return 1;
    case 2:
      return 3;
    case 4:
      return 2;
    default:
      return 4;
    }
  }

  protected BufferedImage decode(int index) throws IOException {
    try {
      return decodeImage();
    } catch (java.io.EOFException e) {
      throw new IIOException("Error reading PNG image data", e);
    }
  }

  private BufferedImage decodeImage() throws IOException {
    readChunks();
    int ch = channels();
    int bitsPerPixel = ch * depth;
    int bpp = Math.max(1, bitsPerPixel / 8);
    Inflater inf = new Inflater();
    inf.setInput(idat);
    int[] passX = {0, 4, 0, 2, 0, 1, 0};
    int[] passY = {0, 0, 4, 0, 2, 0, 1};
    int[] stepX = {8, 8, 4, 4, 2, 2, 1};
    int[] stepY = {8, 8, 8, 4, 4, 2, 2};
    int passes = interlace == 1 ? 7 : 1;
    int[][] samples = new int[height][width * ch];
    for (int p = 0; p < passes; p++) {
      int px = interlace == 1 ? passX[p] : 0;
      int py = interlace == 1 ? passY[p] : 0;
      int sx = interlace == 1 ? stepX[p] : 1;
      int sy = interlace == 1 ? stepY[p] : 1;
      int pw = (width - px + sx - 1) / sx;
      int ph = (height - py + sy - 1) / sy;
      if (pw <= 0 || ph <= 0) {
        continue;
      }
      int rowBytes = (pw * bitsPerPixel + 7) / 8;
      byte[] prev = new byte[rowBytes];
      byte[] cur = new byte[rowBytes];
      byte[] line = new byte[rowBytes + 1];
      for (int y = 0; y < ph; y++) {
        fully(inf, line);
        int ft = line[0] & 0xff;
        System.arraycopy(line, 1, cur, 0, rowBytes);
        unfilter(ft, cur, prev, bpp);
        int dy = py + y * sy;
        for (int x = 0; x < pw; x++) {
          int dx = px + x * sx;
          for (int c = 0; c < ch; c++) {
            samples[dy][dx * ch + c] = sample(cur, x * ch + c);
          }
        }
        byte[] t = prev;
        prev = cur;
        cur = t;
      }
    }
    inf.end();
    return build(samples);
  }

  private int sample(byte[] row, int i) {
    switch (depth) {
    case 8:
      return row[i] & 0xff;
    case 16:
      return ((row[i * 2] & 0xff) << 8) | (row[i * 2 + 1] & 0xff);
    default: {
      int bit = i * depth;
      int v = row[bit >> 3] & 0xff;
      int shift = 8 - depth - (bit & 7);
      return (v >> shift) & ((1 << depth) - 1);
    }
    }
  }

  private static void fully(Inflater inf, byte[] buf) throws IOException {
    int off = 0;
    try {
      while (off < buf.length) {
        int n = inf.inflate(buf, off, buf.length - off);
        if (n == 0) {
          if (inf.finished() || inf.needsDictionary() || inf.needsInput()) {
            throw new IIOException("Truncated PNG image data");
          }
        }
        off += n;
      }
    } catch (DataFormatException e) {
      throw new IIOException("Error reading PNG image data", e);
    }
  }

  private static void unfilter(int type, byte[] cur, byte[] prev, int bpp) throws IOException {
    int n = cur.length;
    switch (type) {
    case 0:
      break;
    case 1:
      for (int i = bpp; i < n; i++) {
        cur[i] = (byte) (cur[i] + cur[i - bpp]);
      }
      break;
    case 2:
      for (int i = 0; i < n; i++) {
        cur[i] = (byte) (cur[i] + prev[i]);
      }
      break;
    case 3:
      for (int i = 0; i < n; i++) {
        int left = i >= bpp ? cur[i - bpp] & 0xff : 0;
        cur[i] = (byte) (cur[i] + ((left + (prev[i] & 0xff)) >> 1));
      }
      break;
    case 4:
      for (int i = 0; i < n; i++) {
        int a = i >= bpp ? cur[i - bpp] & 0xff : 0;
        int b = prev[i] & 0xff;
        int c = i >= bpp ? prev[i - bpp] & 0xff : 0;
        int pa = Math.abs(b - c);
        int pb = Math.abs(a - c);
        int pc = Math.abs(a + b - 2 * c);
        int pr = (pa <= pb && pa <= pc) ? a : (pb <= pc ? b : c);
        cur[i] = (byte) (cur[i] + pr);
      }
      break;
    default:
      throw new IIOException("Unknown PNG filter type " + type);
    }
  }

  private BufferedImage build(int[][] s) throws IOException {
    boolean sixteen = depth == 16;
    switch (colorType) {
    case 0: {
      if (depth == 8) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster r = bi.getRaster();
        for (int y = 0; y < height; y++) {
          int[] row = s[y];
          r.setPixels(0, y, width, 1, row);
        }
        return bi;
      }
      if (sixteen) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
        WritableRaster r = bi.getRaster();
        for (int y = 0; y < height; y++) {
          r.setPixels(0, y, width, 1, s[y]);
        }
        return bi;
      }
      int size = 1 << depth;
      byte[] g = new byte[size];
      for (int i = 0; i < size; i++) {
        g[i] = (byte) (i * 255 / (size - 1));
      }
      IndexColorModel cm = new IndexColorModel(depth, size, g, g, g);
      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY, cm);
      for (int y = 0; y < height; y++) {
        bi.getRaster().setPixels(0, y, width, 1, s[y]);
      }
      return bi;
    }
    case 2: {
      boolean key = trns != null && trns.length >= 6;
      int tr = key ? ((trns[0] & 0xff) << 8) | (trns[1] & 0xff) : 0;
      int tg = key ? ((trns[2] & 0xff) << 8) | (trns[3] & 0xff) : 0;
      int tb = key ? ((trns[4] & 0xff) << 8) | (trns[5] & 0xff) : 0;
      if (sixteen) {
        ImageTypeSpecifier t = ImageTypeSpecifier.createInterleaved(ColorSpace.getInstance(ColorSpace.CS_sRGB),
            new int[] {0, 1, 2}, DataBuffer.TYPE_USHORT, false, false);
        BufferedImage bi = t.createBufferedImage(width, height);
        for (int y = 0; y < height; y++) {
          bi.getRaster().setPixels(0, y, width, 1, s[y]);
        }
        return bi;
      }
      BufferedImage bi = new BufferedImage(width, height, key ? BufferedImage.TYPE_4BYTE_ABGR
          : BufferedImage.TYPE_3BYTE_BGR);
      WritableRaster r = bi.getRaster();
      int[] px = new int[key ? 4 : 3];
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          px[0] = s[y][x * 3];
          px[1] = s[y][x * 3 + 1];
          px[2] = s[y][x * 3 + 2];
          if (key) {
            px[3] = (px[0] == tr && px[1] == tg && px[2] == tb) ? 0 : 255;
          }
          r.setPixel(x, y, px);
        }
      }
      return bi;
    }
    case 3: {
      if (palette == null) {
        throw new IIOException("Missing PLTE chunk");
      }
      int n = palette.length / 3;
      int size = 1 << depth;
      byte[] r = new byte[size];
      byte[] g = new byte[size];
      byte[] b = new byte[size];
      byte[] a = null;
      for (int i = 0; i < n && i < size; i++) {
        r[i] = palette[i * 3];
        g[i] = palette[i * 3 + 1];
        b[i] = palette[i * 3 + 2];
      }
      if (trns != null) {
        a = new byte[size];
        java.util.Arrays.fill(a, (byte) 255);
        for (int i = 0; i < trns.length && i < size; i++) {
          a[i] = trns[i];
        }
      }
      IndexColorModel cm = a == null ? new IndexColorModel(depth, size, r, g, b)
          : new IndexColorModel(depth, size, r, g, b, a);
      BufferedImage bi = new BufferedImage(width, height, depth == 8 ? BufferedImage.TYPE_BYTE_INDEXED
          : BufferedImage.TYPE_BYTE_BINARY, cm);
      for (int y = 0; y < height; y++) {
        bi.getRaster().setPixels(0, y, width, 1, s[y]);
      }
      return bi;
    }
    case 4: {
      int type = sixteen ? DataBuffer.TYPE_USHORT : DataBuffer.TYPE_BYTE;
      ImageTypeSpecifier t = ImageTypeSpecifier.createInterleaved(ColorSpace.getInstance(ColorSpace.CS_GRAY),
          new int[] {0, 1}, type, true, false);
      BufferedImage bi = t.createBufferedImage(width, height);
      for (int y = 0; y < height; y++) {
        bi.getRaster().setPixels(0, y, width, 1, s[y]);
      }
      return bi;
    }
    default: {
      if (sixteen) {
        ImageTypeSpecifier t = ImageTypeSpecifier.createInterleaved(ColorSpace.getInstance(ColorSpace.CS_sRGB),
            new int[] {0, 1, 2, 3}, DataBuffer.TYPE_USHORT, true, false);
        BufferedImage bi = t.createBufferedImage(width, height);
        for (int y = 0; y < height; y++) {
          bi.getRaster().setPixels(0, y, width, 1, s[y]);
        }
        return bi;
      }
      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
      for (int y = 0; y < height; y++) {
        bi.getRaster().setPixels(0, y, width, 1, s[y]);
      }
      return bi;
    }
    }
  }
}
