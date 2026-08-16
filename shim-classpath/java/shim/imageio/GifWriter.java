/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public final class GifWriter extends CodecWriter {
  private boolean sequence;
  private boolean headerWritten;

  public GifWriter(ImageWriterSpi spi) {
    super(spi);
  }

  public boolean canWriteSequence() {
    return true;
  }

  public void prepareWriteSequence(IIOMetadata streamMetadata) throws IOException {
    sequence = true;
    headerWritten = false;
  }

  public void writeToSequence(IIOImage image, ImageWriteParam param) throws IOException {
    if (!sequence) {
      throw new IllegalStateException("prepareWriteSequence() was not invoked!");
    }
    writeFrame(image, !headerWritten, true);
    headerWritten = true;
  }

  public void endWriteSequence() throws IOException {
    if (!sequence) {
      throw new IllegalStateException("prepareWriteSequence() was not invoked!");
    }
    stream().writeByte(0x3b);
    stream().flush();
    sequence = false;
  }

  public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
    writeFrame(image, true, false);
    stream().writeByte(0x3b);
    stream().flush();
  }

  private void writeFrame(IIOImage image, boolean header, boolean loop) throws IOException {
    ImageOutputStream out = stream();
    out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    BufferedImage bi = shim.awt.ImageSupport.toBuffered(image.getRenderedImage());
    int w = bi.getWidth();
    int h = bi.getHeight();
    int[] palette = new int[256];
    int paletteSize;
    int transparent = -1;
    byte[] idx = new byte[w * h];
    ColorModel cm = bi.getColorModel();
    if (bi.getType() == BufferedImage.TYPE_BYTE_GRAY) {
      paletteSize = 256;
      for (int i = 0; i < 256; i++) {
        palette[i] = 0xff000000 | i * 0x010101;
      }
      int[] px = new int[1];
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          bi.getRaster().getPixel(x, y, px);
          idx[y * w + x] = (byte) px[0];
        }
      }
    } else if (cm instanceof IndexColorModel && bi.getRaster().getNumBands() == 1 && ((IndexColorModel) cm).getMapSize() <= 256) {
      IndexColorModel icm = (IndexColorModel) cm;
      paletteSize = icm.getMapSize();
      for (int i = 0; i < paletteSize; i++) {
        palette[i] = icm.getRGB(i);
      }
      transparent = icm.getTransparentPixel();
      int[] px = new int[1];
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          bi.getRaster().getPixel(x, y, px);
          idx[y * w + x] = (byte) px[0];
        }
      }
    } else {
      Map<Integer, Integer> colors = new HashMap<>();
      int[] argb = bi.getRGB(0, 0, w, h, null, 0, w);
      boolean overflow = false;
      boolean hasTransparent = false;
      for (int p : argb) {
        if ((p >>> 24) < 128) {
          hasTransparent = true;
        } else if (!colors.containsKey(p | 0xff000000)) {
          if (colors.size() >= (hasTransparent ? 255 : 256)) {
            overflow = true;
            break;
          }
          colors.put(p | 0xff000000, colors.size());
        }
      }
      if (overflow) {
        colors = quantize(argb);
        hasTransparent = false;
        for (int p : argb) {
          if ((p >>> 24) < 128) {
            hasTransparent = true;
            break;
          }
        }
      }
      int count = colors.size();
      for (Map.Entry<Integer, Integer> e : colors.entrySet()) {
        palette[e.getValue()] = e.getKey();
      }
      if (hasTransparent) {
        transparent = count;
        count++;
      }
      paletteSize = count;
      for (int i = 0; i < argb.length; i++) {
        int p = argb[i];
        if ((p >>> 24) < 128) {
          idx[i] = (byte) transparent;
        } else {
          Integer v = colors.get(p | 0xff000000);
          idx[i] = (byte) (v != null ? v : nearest(palette, colors.size(), p));
        }
      }
    }
    int bits = 1;
    while ((1 << bits) < paletteSize) {
      bits++;
    }
    processImageStarted(0);
    if (header) {
      out.write(new byte[] {'G', 'I', 'F', '8', '9', 'a'});
      out.writeShort(w);
      out.writeShort(h);
      out.writeByte(loop ? 0x70 : 0xF0 | (bits - 1));
      out.writeByte(0);
      out.writeByte(0);
      if (!loop) {
        writeTable(out, palette, paletteSize, bits);
      }
      if (loop) {
        out.writeByte(0x21);
        out.writeByte(0xff);
        out.writeByte(11);
        out.write(new byte[] {'N', 'E', 'T', 'S', 'C', 'A', 'P', 'E', '2', '.', '0'});
        out.writeByte(3);
        out.writeByte(1);
        out.writeShort(0);
        out.writeByte(0);
      }
    }
    if (transparent >= 0 || loop || header) {
      out.writeByte(0x21);
      out.writeByte(0xf9);
      out.writeByte(4);
      out.writeByte((transparent >= 0 ? 1 : 0) | (loop ? 8 : 0));
      out.writeShort(loop ? 10 : 0);
      out.writeByte(transparent >= 0 ? transparent : 0);
      out.writeByte(0);
    }
    out.writeByte(0x2c);
    out.writeShort(0);
    out.writeShort(0);
    out.writeShort(w);
    out.writeShort(h);
    if (!loop && header) {
      out.writeByte(0x40);
      idx = interlace(idx, w, h);
    } else {
      out.writeByte(0x80 | (bits - 1));
      writeTable(out, palette, paletteSize, bits);
    }
    int minCode = Math.max(2, bits);
    out.writeByte(minCode);
    byte[] lzw = encode(idx, minCode);
    int off = 0;
    while (off < lzw.length) {
      int n = Math.min(255, lzw.length - off);
      out.writeByte(n);
      out.write(lzw, off, n);
      off += n;
    }
    out.writeByte(0);
    processImageProgress(100f);
    processImageComplete();
  }

  private static void writeTable(ImageOutputStream out, int[] palette, int paletteSize, int bits) throws IOException {
    for (int i = 0; i < (1 << bits); i++) {
      int c = i < paletteSize ? palette[i] : 0;
      out.writeByte(c >> 16);
      out.writeByte(c >> 8);
      out.writeByte(c);
    }
  }

  private static byte[] interlace(byte[] idx, int w, int h) {
    byte[] r = new byte[idx.length];
    int row = 0;
    int[] starts = {0, 4, 2, 1};
    int[] steps = {8, 8, 4, 2};
    for (int p = 0; p < 4; p++) {
      for (int y = starts[p]; y < h; y += steps[p]) {
        System.arraycopy(idx, y * w, r, row * w, w);
        row++;
      }
    }
    return r;
  }

  private static int nearest(int[] palette, int count, int argb) {
    int best = 0;
    int bestD = Integer.MAX_VALUE;
    for (int i = 0; i < count; i++) {
      int dr = ((argb >> 16) & 0xff) - ((palette[i] >> 16) & 0xff);
      int dg = ((argb >> 8) & 0xff) - ((palette[i] >> 8) & 0xff);
      int db = (argb & 0xff) - (palette[i] & 0xff);
      int d = dr * dr + dg * dg + db * db;
      if (d < bestD) {
        bestD = d;
        best = i;
      }
    }
    return best;
  }

  private static Map<Integer, Integer> quantize(int[] argb) {
    Map<Integer, int[]> buckets = new HashMap<>();
    for (int p : argb) {
      if ((p >>> 24) < 128) {
        continue;
      }
      int key = ((p >> 16) & 0xe0) << 16 | ((p >> 8) & 0xe0) << 8 | (p & 0xc0);
      int[] b = buckets.get(key);
      if (b == null) {
        b = new int[4];
        buckets.put(key, b);
      }
      b[0] += (p >> 16) & 0xff;
      b[1] += (p >> 8) & 0xff;
      b[2] += p & 0xff;
      b[3]++;
    }
    Map<Integer, Integer> out = new HashMap<>();
    int i = 0;
    for (int[] b : buckets.values()) {
      if (i >= 255) {
        break;
      }
      int c = 0xff000000 | ((b[0] / b[3]) << 16) | ((b[1] / b[3]) << 8) | (b[2] / b[3]);
      out.put(c, i++);
    }
    for (int p : argb) {
      if ((p >>> 24) >= 128 && !out.containsKey(p | 0xff000000) && out.size() < 255) {
        out.put(p | 0xff000000, out.size());
      }
    }
    return out;
  }

  private static byte[] encode(byte[] data, int minCode) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int clear = 1 << minCode;
    int eoi = clear + 1;
    int codeSize = minCode + 1;
    int next = eoi + 1;
    Map<Long, Integer> dict = new HashMap<>();
    int[] bitBuf = {0};
    int[] bitCnt = {0};
    emit(out, bitBuf, bitCnt, clear, codeSize);
    int prefix = -1;
    for (byte bb : data) {
      int c = bb & 0xff;
      if (prefix < 0) {
        prefix = c;
        continue;
      }
      long key = ((long) prefix << 8) | c;
      Integer found = dict.get(key);
      if (found != null) {
        prefix = found;
        continue;
      }
      emit(out, bitBuf, bitCnt, prefix, codeSize);
      if (next < 4096) {
        dict.put(key, next++);
        if (next > (1 << codeSize) && codeSize < 12) {
          codeSize++;
        }
      } else {
        emit(out, bitBuf, bitCnt, clear, codeSize);
        dict.clear();
        codeSize = minCode + 1;
        next = eoi + 1;
      }
      prefix = c;
    }
    if (prefix >= 0) {
      emit(out, bitBuf, bitCnt, prefix, codeSize);
    }
    emit(out, bitBuf, bitCnt, eoi, codeSize);
    if (bitCnt[0] > 0) {
      out.write(bitBuf[0] & 0xff);
    }
    return out.toByteArray();
  }

  private static void emit(ByteArrayOutputStream out, int[] buf, int[] cnt, int code, int size) {
    buf[0] |= code << cnt[0];
    cnt[0] += size;
    while (cnt[0] >= 8) {
      out.write(buf[0] & 0xff);
      buf[0] >>>= 8;
      cnt[0] -= 8;
    }
  }
}
