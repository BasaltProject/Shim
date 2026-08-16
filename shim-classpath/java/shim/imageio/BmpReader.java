/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteOrder;
import javax.imageio.IIOException;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public final class BmpReader extends CodecReader {
  public BmpReader(ImageReaderSpi spi) {
    super(spi);
  }

  protected String[] nativeFormat() { return new String[] {"javax_imageio_bmp_1.0", "BI_RGB", "true"}; }

  protected int imageCount() throws IOException {
    return 1;
  }

  public javax.imageio.metadata.IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return new BmpMetadata(stream());
  }

  protected BufferedImage decode(int index) throws IOException {
    ImageInputStream in = stream();
    in.seek(0);
    in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    if (in.readUnsignedByte() != 'B' || in.readUnsignedByte() != 'M') {
      throw new IIOException("Not a BMP file");
    }
    in.skipBytes(8);
    long dataOffset = in.readUnsignedInt();
    long headerSize = in.readUnsignedInt();
    int w;
    int h;
    int bits;
    int compression = 0;
    int colorsUsed = 0;
    int[] masks = null;
    boolean core = headerSize == 12;
    if (core) {
      w = in.readUnsignedShort();
      h = in.readUnsignedShort();
      in.readUnsignedShort();
      bits = in.readUnsignedShort();
    } else {
      w = in.readInt();
      h = in.readInt();
      in.readUnsignedShort();
      bits = in.readUnsignedShort();
      compression = (int) in.readUnsignedInt();
      in.skipBytes(12);
      colorsUsed = (int) in.readUnsignedInt();
      in.skipBytes(4);
      if (headerSize >= 52 || compression == 3 || compression == 6) {
        masks = new int[4];
        if (headerSize > 40) {
          masks[0] = in.readInt();
          masks[1] = in.readInt();
          masks[2] = in.readInt();
          if (headerSize >= 56) {
            masks[3] = in.readInt();
          }
        } else {
          in.seek(14 + headerSize);
          masks[0] = in.readInt();
          masks[1] = in.readInt();
          masks[2] = in.readInt();
        }
      }
    }
    boolean topDown = h < 0;
    h = Math.abs(h);
    if (w <= 0 || h <= 0) {
      throw new IIOException("Bad BMP dimensions");
    }
    int[] pal = null;
    if (bits <= 8) {
      int n = colorsUsed != 0 ? colorsUsed : 1 << bits;
      in.seek(14 + headerSize);
      pal = new int[n];
      for (int i = 0; i < n; i++) {
        int b = in.readUnsignedByte();
        int g = in.readUnsignedByte();
        int r = in.readUnsignedByte();
        if (!core) {
          in.readUnsignedByte();
        }
        pal[i] = 0xff000000 | (r << 16) | (g << 8) | b;
      }
    }
    in.seek(dataOffset);
    int rowBytes = ((w * bits + 31) / 32) * 4;
    int[] argb = new int[w * h];
    if (compression == 1 || compression == 2) {
      decodeRle(in, argb, w, h, compression == 1, pal, topDown);
      IndexColorModel icm = palette(pal, 8);
      BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, icm);
      WritableRaster r = bi.getRaster();
      for (int y = 0; y < h; y++) {
        r.setPixels(0, y, w, 1, java.util.Arrays.copyOfRange(argb, y * w, y * w + w));
      }
      return bi;
    }
    byte[] row = new byte[rowBytes];
    int[][] samples = bits <= 8 ? new int[h][w] : null;
    for (int ry = 0; ry < h; ry++) {
      in.readFully(row);
      int y = topDown ? ry : h - 1 - ry;
      for (int x = 0; x < w; x++) {
        switch (bits) {
        case 1:
        case 2:
        case 4:
        case 8: {
          int bit = x * bits;
          int v = (row[bit >> 3] >> (8 - bits - (bit & 7))) & ((1 << bits) - 1);
          samples[y][x] = v;
          break;
        }
        case 16: {
          int v = (row[x * 2] & 0xff) | ((row[x * 2 + 1] & 0xff) << 8);
          if (masks != null) {
            argb[y * w + x] = 0xff000000 | extract(v, masks[0]) << 16 | extract(v, masks[1]) << 8 | extract(v, masks[2]);
          } else {
            int r5 = (v >> 10) & 31;
            int g5 = (v >> 5) & 31;
            int b5 = v & 31;
            argb[y * w + x] = 0xff000000 | ((r5 << 3 | r5 >> 2) << 16) | ((g5 << 3 | g5 >> 2) << 8) | (b5 << 3 | b5 >> 2);
          }
          break;
        }
        case 24:
          argb[y * w + x] = 0xff000000 | ((row[x * 3 + 2] & 0xff) << 16) | ((row[x * 3 + 1] & 0xff) << 8)
              | (row[x * 3] & 0xff);
          break;
        case 32: {
          int v = (row[x * 4] & 0xff) | ((row[x * 4 + 1] & 0xff) << 8) | ((row[x * 4 + 2] & 0xff) << 16)
              | ((row[x * 4 + 3] & 0xff) << 24);
          if (masks != null && masks[0] != 0) {
            int a = masks[3] != 0 ? extract(v, masks[3]) : 255;
            argb[y * w + x] = (a << 24) | extract(v, masks[0]) << 16 | extract(v, masks[1]) << 8 | extract(v, masks[2]);
          } else {
            argb[y * w + x] = 0xff000000 | (v & 0xffffff);
          }
          break;
        }
        default:
          throw new IIOException("Unsupported BMP bit count " + bits);
        }
      }
    }
    if (bits == 8 && isGrayRamp(pal)) {
      BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
      for (int y = 0; y < h; y++) {
        bi.getRaster().setPixels(0, y, w, 1, samples[y]);
      }
      return bi;
    }
    if (bits <= 8) {
      IndexColorModel icm = palette(pal, bits);
      BufferedImage bi = new BufferedImage(w, h, bits == 8 ? BufferedImage.TYPE_BYTE_INDEXED
          : BufferedImage.TYPE_BYTE_BINARY, icm);
      for (int y = 0; y < h; y++) {
        bi.getRaster().setPixels(0, y, w, 1, samples[y]);
      }
      return bi;
    }
    int type = bits == 24 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_INT_RGB;
    BufferedImage bi = new BufferedImage(w, h, type);
    bi.setRGB(0, 0, w, h, argb, 0, w);
    return bi;
  }

  private static boolean isGrayRamp(int[] pal) {
    if (pal.length != 256) {
      return false;
    }
    for (int i = 0; i < 256; i++) {
      if ((pal[i] & 0xffffff) != i * 0x010101) {
        return false;
      }
    }
    return true;
  }

  private static int extract(int v, int mask) {
    if (mask == 0) {
      return 0;
    }
    int shift = Integer.numberOfTrailingZeros(mask);
    int bits = Integer.bitCount(mask >>> shift);
    int c = (v & mask) >>> shift;
    return bits >= 8 ? c >> (bits - 8) : (c * 255) / ((1 << bits) - 1);
  }

  private static IndexColorModel palette(int[] pal, int bits) {
    int size = 1 << bits;
    byte[] r = new byte[size];
    byte[] g = new byte[size];
    byte[] b = new byte[size];
    for (int i = 0; i < size && i < pal.length; i++) {
      r[i] = (byte) (pal[i] >> 16);
      g[i] = (byte) (pal[i] >> 8);
      b[i] = (byte) pal[i];
    }
    return new IndexColorModel(bits, size, r, g, b);
  }

  private static void decodeRle(ImageInputStream in, int[] idx, int w, int h, boolean rle8, int[] pal,
      boolean topDown) throws IOException {
    int x = 0;
    int y = 0;
    while (y < h) {
      int n = in.read();
      int c = in.read();
      if (n < 0 || c < 0) {
        break;
      }
      if (n > 0) {
        for (int i = 0; i < n; i++) {
          int v = rle8 ? c : ((i & 1) == 0 ? c >> 4 : c & 15);
          put(idx, w, h, x++, y, v, topDown);
        }
      } else if (c == 0) {
        x = 0;
        y++;
      } else if (c == 1) {
        break;
      } else if (c == 2) {
        x += in.readUnsignedByte();
        y += in.readUnsignedByte();
      } else {
        int count = c;
        int bytes = rle8 ? count : (count + 1) / 2;
        byte[] data = new byte[bytes];
        in.readFully(data);
        for (int i = 0; i < count; i++) {
          int v = rle8 ? data[i] & 0xff : ((i & 1) == 0 ? (data[i / 2] & 0xff) >> 4 : data[i / 2] & 15);
          put(idx, w, h, x++, y, v, topDown);
        }
        if ((bytes & 1) != 0) {
          in.read();
        }
      }
    }
  }

  private static void put(int[] idx, int w, int h, int x, int y, int v, boolean topDown) {
    if (x < 0 || x >= w || y < 0 || y >= h) {
      return;
    }
    int row = topDown ? y : h - 1 - y;
    idx[row * w + x] = v;
  }
}
