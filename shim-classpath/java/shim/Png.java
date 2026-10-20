/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.imageio.IIOException;

public final class Png {
  private static final byte[] kSignature = {(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a};

  private Png() { }

  public static boolean isPng(byte[] b) {
    if (b.length < 8) {
      return false;
    }
    for (int i = 0; i < 8; i++) {
      if (b[i] != kSignature[i]) {
        return false;
      }
    }
    return true;
  }

  private static int u32(byte[] b, int o) {
    return ((b[o] & 0xff) << 24) | ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff);
  }

  public static BufferedImage decode(byte[] b) throws IOException {
    int pos = 8;
    int width = 0;
    int height = 0;
    int depth = 0;
    int colorType = 0;
    int interlace = 0;
    byte[] palette = null;
    byte[] trns = null;
    ByteArrayOutputStream idat = new ByteArrayOutputStream();
    boolean sawHeader = false;
    while (pos + 8 <= b.length) {
      int len = u32(b, pos);
      String name = new String(b, pos + 4, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
      int body = pos + 8;
      if (len < 0 || body + len + 4 > b.length) {
        throw new IIOException("Error reading PNG image data");
      }
      if (name.equals("IHDR")) {
        width = u32(b, body);
        height = u32(b, body + 4);
        depth = b[body + 8] & 0xff;
        colorType = b[body + 9] & 0xff;
        interlace = b[body + 12] & 0xff;
        sawHeader = true;
      } else if (name.equals("PLTE")) {
        palette = java.util.Arrays.copyOfRange(b, body, body + len);
      } else if (name.equals("tRNS")) {
        trns = java.util.Arrays.copyOfRange(b, body, body + len);
      } else if (name.equals("IDAT")) {
        idat.write(b, body, len);
      } else if (name.equals("IEND")) {
        break;
      }
      pos = body + len + 4;
    }
    if (!sawHeader || width <= 0 || height <= 0) {
      throw new IIOException("Error reading PNG image data");
    }
    int channels;
    switch (colorType) {
    case 0: channels = 1; break;
    case 2: channels = 3; break;
    case 3: channels = 1; break;
    case 4: channels = 2; break;
    case 6: channels = 4; break;
    default: throw new IIOException("Error reading PNG image data");
    }
    int bitsPerPixel = channels * depth;
    int bpp = Math.max(1, bitsPerPixel / 8);
    byte[] raw = inflate(idat.toByteArray(), interlace == 0 ? (1 + (width * bitsPerPixel + 7) / 8) * height : -1);
    int[] argb = new int[width * height];
    int grayMax = (1 << Math.min(depth, 16)) - 1;
    int[] passX = {0, 4, 0, 2, 0, 1, 0};
    int[] passY = {0, 0, 4, 0, 2, 0, 1};
    int[] stepX = {8, 8, 4, 4, 2, 2, 1};
    int[] stepY = {8, 8, 8, 4, 4, 2, 2};
    int rp = 0;
    int passes = interlace == 0 ? 1 : 7;
    boolean alphaSeen = colorType == 4 || colorType == 6 || trns != null;
    for (int pass = 0; pass < passes; pass++) {
      int x0 = interlace == 0 ? 0 : passX[pass];
      int y0 = interlace == 0 ? 0 : passY[pass];
      int dx = interlace == 0 ? 1 : stepX[pass];
      int dy = interlace == 0 ? 1 : stepY[pass];
      int pw = (width - x0 + dx - 1) / dx;
      int ph = (height - y0 + dy - 1) / dy;
      if (pw <= 0 || ph <= 0) {
        continue;
      }
      int rowBytes = (pw * bitsPerPixel + 7) / 8;
      byte[] prev = new byte[rowBytes];
      byte[] cur = new byte[rowBytes];
      for (int row = 0; row < ph; row++) {
        if (rp + 1 + rowBytes > raw.length) {
          throw new IIOException("Error reading PNG image data");
        }
        int filter = raw[rp++] & 0xff;
        System.arraycopy(raw, rp, cur, 0, rowBytes);
        rp += rowBytes;
        unfilter(filter, cur, prev, bpp);
        for (int col = 0; col < pw; col++) {
          int x = x0 + col * dx;
          int y = y0 + row * dy;
          argb[y * width + x] = pixel(cur, col, depth, colorType, palette, trns, grayMax);
        }
        byte[] t = prev;
        prev = cur;
        cur = t;
      }
    }
    int type;
    switch (colorType) {
    case 0: type = depth == 16 ? BufferedImage.TYPE_CUSTOM : (depth == 8 ? BufferedImage.TYPE_BYTE_GRAY
        : BufferedImage.TYPE_BYTE_BINARY); break;
    case 2: type = depth == 16 ? BufferedImage.TYPE_CUSTOM : (trns != null ? BufferedImage.TYPE_4BYTE_ABGR
        : BufferedImage.TYPE_3BYTE_BGR); break;
    case 3: type = depth == 8 ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_BINARY; break;
    case 4: type = BufferedImage.TYPE_CUSTOM; break;
    case 6: type = depth == 16 ? BufferedImage.TYPE_CUSTOM : BufferedImage.TYPE_4BYTE_ABGR; break;
    default: type = BufferedImage.TYPE_CUSTOM; break;
    }
    BufferedImage img = shim.ImageFactory.create(width, height, type, alphaSeen, colorType == 4);
    shim.ImageFactory.fill(img, argb, colorType == 0 && depth == 8, colorType == 4);
    return img;
  }

  private static int pixel(byte[] row, int col, int depth, int colorType, byte[] palette, byte[] trns, int grayMax) {
    switch (colorType) {
    case 0: {
      int v = sample(row, col, depth);
      int g8 = depth == 16 ? (v * 255 + 32767) / 65535 : (depth == 8 ? v : v * 255 / grayMax);
      int a = 0xff;
      if (trns != null && trns.length >= 2 && ((trns[0] & 0xff) << 8 | (trns[1] & 0xff)) == v) {
        a = 0;
      }
      return (a << 24) | (g8 << 16) | (g8 << 8) | g8;
    }
    case 2: {
      int r;
      int g;
      int bl;
      if (depth == 8) {
        r = row[col * 3] & 0xff;
        g = row[col * 3 + 1] & 0xff;
        bl = row[col * 3 + 2] & 0xff;
      } else {
        r = ((((row[col * 6] & 0xff) << 8 | (row[col * 6 + 1] & 0xff)) * 255) + 32767) / 65535;
        g = ((((row[col * 6 + 2] & 0xff) << 8 | (row[col * 6 + 3] & 0xff)) * 255) + 32767) / 65535;
        bl = ((((row[col * 6 + 4] & 0xff) << 8 | (row[col * 6 + 5] & 0xff)) * 255) + 32767) / 65535;
      }
      int a = 0xff;
      if (trns != null && trns.length >= 6 && depth == 8 && (trns[1] & 0xff) == r && (trns[3] & 0xff) == g
          && (trns[5] & 0xff) == bl) {
        a = 0;
      }
      return (a << 24) | (r << 16) | (g << 8) | bl;
    }
    case 3: {
      int idx = sample(row, col, depth);
      if (palette == null || idx * 3 + 2 >= palette.length) {
        return 0xff000000;
      }
      int a = trns != null && idx < trns.length ? trns[idx] & 0xff : 0xff;
      return (a << 24) | ((palette[idx * 3] & 0xff) << 16) | ((palette[idx * 3 + 1] & 0xff) << 8)
          | (palette[idx * 3 + 2] & 0xff);
    }
    case 4: {
      int step = depth == 8 ? 2 : 4;
      int g = row[col * step] & 0xff;
      int a = row[col * step + (depth == 8 ? 1 : 2)] & 0xff;
      return (a << 24) | (g << 16) | (g << 8) | g;
    }
    default: {
      int step = depth == 8 ? 4 : 8;
      int d = depth == 8 ? 1 : 2;
      int r = row[col * step] & 0xff;
      int g = row[col * step + d] & 0xff;
      int bl = row[col * step + 2 * d] & 0xff;
      int a = row[col * step + 3 * d] & 0xff;
      return (a << 24) | (r << 16) | (g << 8) | bl;
    }
    }
  }

  private static int sample(byte[] row, int col, int depth) {
    if (depth == 8) {
      return row[col] & 0xff;
    }
    if (depth == 16) {
      return ((row[col * 2] & 0xff) << 8) | (row[col * 2 + 1] & 0xff);
    }
    int perByte = 8 / depth;
    int shift = 8 - depth * (col % perByte + 1);
    return ((row[col / perByte] & 0xff) >> shift) & ((1 << depth) - 1);
  }

  private static void unfilter(int filter, byte[] cur, byte[] prev, int bpp) throws IIOException {
    int n = cur.length;
    switch (filter) {
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
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);
        int pred = (pa <= pb && pa <= pc) ? a : (pb <= pc ? b : c);
        cur[i] = (byte) (cur[i] + pred);
      }
      break;
    default:
      throw new IIOException("Error reading PNG image data");
    }
  }

  private static byte[] inflate(byte[] z, int expected) throws IIOException {
    Inflater inf = new Inflater();
    inf.setInput(z);
    ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(expected, 1024));
    byte[] buf = new byte[65536];
    try {
      while (!inf.finished()) {
        int n = inf.inflate(buf);
        if (n == 0 && (inf.needsInput() || inf.needsDictionary())) {
          break;
        }
        out.write(buf, 0, n);
      }
    } catch (DataFormatException e) {
      throw new IIOException("Error reading PNG image data", e);
    } finally {
      inf.end();
    }
    return out.toByteArray();
  }

  public static byte[] encode(RenderedImage ri) throws IOException {
    BufferedImage im = (BufferedImage) ri;
    int w = im.getWidth();
    int h = im.getHeight();
    int t = im.getType();
    boolean gray = t == BufferedImage.TYPE_BYTE_GRAY;
    boolean alpha = im.getTransparency() != java.awt.Transparency.OPAQUE;
    int channels = gray ? 1 : (alpha ? 4 : 3);
    int colorType = gray ? 0 : (alpha ? 6 : 2);
    byte[] raw = new byte[(1 + w * channels) * h];
    int p = 0;
    for (int y = 0; y < h; y++) {
      raw[p++] = 0;
      for (int x = 0; x < w; x++) {
        if (gray) {
          raw[p++] = (byte) shim.ImageFactory.graySample(im, x, y);
        } else {
          int rgb = im.getRGB(x, y);
          raw[p++] = (byte) (rgb >> 16);
          raw[p++] = (byte) (rgb >> 8);
          raw[p++] = (byte) rgb;
          if (alpha) {
            raw[p++] = (byte) (rgb >>> 24);
          }
        }
      }
    }
    int stride = 1 + w * channels;
    byte[] filtered = new byte[raw.length];
    byte[] best = new byte[stride];
    for (int y = 0; y < h; y++) {
      int rowStart = y * stride;
      int bestFilter = 0;
      long bestSum = Long.MAX_VALUE;
      for (int f = 0; f < 5; f++) {
        long sum = 0;
        for (int i = 0; i < stride - 1; i++) {
          int cur = raw[rowStart + 1 + i] & 0xff;
          int a = i >= channels ? raw[rowStart + 1 + i - channels] & 0xff : 0;
          int b = y > 0 ? raw[rowStart - stride + 1 + i] & 0xff : 0;
          int c = (i >= channels && y > 0) ? raw[rowStart - stride + 1 + i - channels] & 0xff : 0;
          int v = filterByte(f, cur, a, b, c);
          sum += Math.abs((byte) v);
        }
        if (sum < bestSum) {
          bestSum = sum;
          bestFilter = f;
        }
      }
      filtered[rowStart] = (byte) bestFilter;
      for (int i = 0; i < stride - 1; i++) {
        int cur = raw[rowStart + 1 + i] & 0xff;
        int a = i >= channels ? raw[rowStart + 1 + i - channels] & 0xff : 0;
        int b = y > 0 ? raw[rowStart - stride + 1 + i] & 0xff : 0;
        int c = (i >= channels && y > 0) ? raw[rowStart - stride + 1 + i - channels] & 0xff : 0;
        filtered[rowStart + 1 + i] = (byte) filterByte(bestFilter, cur, a, b, c);
      }
    }
    Deflater def = new Deflater();
    def.setInput(filtered);
    def.finish();
    ByteArrayOutputStream z = new ByteArrayOutputStream();
    byte[] buf = new byte[65536];
    while (!def.finished()) {
      int n = def.deflate(buf);
      z.write(buf, 0, n);
    }
    def.end();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(kSignature);
    byte[] ihdr = new byte[13];
    putInt(ihdr, 0, w);
    putInt(ihdr, 4, h);
    ihdr[8] = 8;
    ihdr[9] = (byte) colorType;
    chunk(out, "IHDR", ihdr);
    chunk(out, "IDAT", z.toByteArray());
    chunk(out, "IEND", new byte[0]);
    return out.toByteArray();
  }

  private static int filterByte(int f, int cur, int a, int b, int c) {
    switch (f) {
    case 1:
      return cur - a;
    case 2:
      return cur - b;
    case 3:
      return cur - ((a + b) >> 1);
    case 4: {
      int p = a + b - c;
      int pa = Math.abs(p - a);
      int pb = Math.abs(p - b);
      int pc = Math.abs(p - c);
      int pred = (pa <= pb && pa <= pc) ? a : (pb <= pc ? b : c);
      return cur - pred;
    }
    default:
      return cur;
    }
  }

  private static void putInt(byte[] b, int o, int v) {
    b[o] = (byte) (v >> 24);
    b[o + 1] = (byte) (v >> 16);
    b[o + 2] = (byte) (v >> 8);
    b[o + 3] = (byte) v;
  }

  private static void chunk(ByteArrayOutputStream out, String name, byte[] body) throws IOException {
    byte[] len = new byte[4];
    putInt(len, 0, body.length);
    out.write(len);
    byte[] nm = name.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
    out.write(nm);
    out.write(body);
    CRC32 crc = new CRC32();
    crc.update(nm);
    crc.update(body);
    byte[] c = new byte[4];
    putInt(c, 0, (int) crc.getValue());
    out.write(c);
  }
}
