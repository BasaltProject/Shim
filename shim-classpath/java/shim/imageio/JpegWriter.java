/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public final class JpegWriter extends CodecWriter {
  private ImageOutputStream out;
  private int bitBuf;
  private int bitCnt;
  private final int[][] dcCode = new int[2][12];
  private final int[][] dcLen = new int[2][12];
  private final int[][] acCode = new int[2][256];
  private final int[][] acLen = new int[2][256];

  public JpegWriter(ImageWriterSpi spi) {
    super(spi);
  }

  public ImageWriteParam getDefaultWriteParam() {
    return new javax.imageio.plugins.jpeg.JPEGImageWriteParam(getLocale());
  }

  private static void build(int[] bits, int[] vals, int[] code, int[] len) {
    int c = 0;
    int k = 0;
    for (int l = 1; l <= 16; l++) {
      for (int i = 0; i < bits[l - 1]; i++) {
        code[vals[k]] = c++;
        len[vals[k]] = l;
        k++;
      }
      c <<= 1;
    }
  }

  private void put(int code, int len) throws IOException {
    if (len == 0) {
      return;
    }
    bitBuf = (bitBuf << len) | (code & ((1 << len) - 1));
    bitCnt += len;
    while (bitCnt >= 8) {
      int b = (bitBuf >> (bitCnt - 8)) & 0xff;
      out.write(b);
      if (b == 0xff) {
        out.write(0);
      }
      bitCnt -= 8;
    }
    bitBuf &= (1 << bitCnt) - 1;
  }

  private void flushBitsToByte() throws IOException {
    if (bitCnt > 0) {
      put(0x7f, 8 - bitCnt);
    }
  }

  public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
    out = stream();
    BufferedImage bi = shim.awt.ImageSupport.toBuffered(image.getRenderedImage());
    int w = bi.getWidth();
    int h = bi.getHeight();
    boolean gray = bi.getColorModel().getNumColorComponents() == 1;
    float quality = 0.75f;
    if (param != null && param.canWriteCompressed() && param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT) {
      quality = param.getCompressionQuality();
    }
    int q = (int) (quality * 100);
    if (q <= 0) {
      q = 1;
    }
    int scale = q < 50 ? 5000 / q : 200 - q * 2;
    int[][] qt = new int[2][64];
    for (int i = 0; i < 64; i++) {
      qt[0][i] = Math.max(1, Math.min(255, (JpegTables.kLumaQuant[i] * scale + 50) / 100));
      qt[1][i] = Math.max(1, Math.min(255, (JpegTables.kChromaQuant[i] * scale + 50) / 100));
    }
    build(JpegTables.kDcLumaBits, JpegTables.kDcLumaVals, dcCode[0], dcLen[0]);
    build(JpegTables.kDcChromaBits, JpegTables.kDcChromaVals, dcCode[1], dcLen[1]);
    build(JpegTables.kAcLumaBits, JpegTables.kAcLumaVals, acCode[0], acLen[0]);
    build(JpegTables.kAcChromaBits, JpegTables.kAcChromaVals, acCode[1], acLen[1]);
    processImageStarted(0);
    out.write(0xff);
    out.write(0xd8);
    out.write(new byte[] {(byte) 0xff, (byte) 0xe0, 0, 16, 'J', 'F', 'I', 'F', 0, 1, 2, 0, 0, 1, 0, 1, 0, 0});
    for (int t = 0; t < (gray ? 1 : 2); t++) {
      out.write(0xff);
      out.write(0xdb);
      out.writeShort(67);
      out.write(t);
      for (int i = 0; i < 64; i++) {
        out.write(qt[t][JpegTables.kZigzag[i]]);
      }
    }
    int nc = gray ? 1 : 3;
    out.write(0xff);
    out.write(0xc0);
    out.writeShort(8 + 3 * nc);
    out.write(8);
    out.writeShort(h);
    out.writeShort(w);
    out.write(nc);
    for (int i = 0; i < nc; i++) {
      out.write(i + 1);
      out.write(gray ? 0x11 : (i == 0 ? 0x22 : 0x11));
      out.write(i == 0 ? 0 : 1);
    }
    huffTable(0x00, JpegTables.kDcLumaBits, JpegTables.kDcLumaVals);
    huffTable(0x10, JpegTables.kAcLumaBits, JpegTables.kAcLumaVals);
    if (!gray) {
      huffTable(0x01, JpegTables.kDcChromaBits, JpegTables.kDcChromaVals);
      huffTable(0x11, JpegTables.kAcChromaBits, JpegTables.kAcChromaVals);
    }
    out.write(0xff);
    out.write(0xda);
    out.writeShort(6 + 2 * nc);
    out.write(nc);
    for (int i = 0; i < nc; i++) {
      out.write(i + 1);
      out.write(i == 0 ? 0x00 : 0x11);
    }
    out.write(0);
    out.write(63);
    out.write(0);
    int[] argb;
    if (gray && !(bi.getColorModel() instanceof java.awt.image.IndexColorModel)) {
      argb = new int[w * h];
      int shift = bi.getColorModel().getComponentSize(0) > 8 ? bi.getColorModel().getComponentSize(0) - 8 : 0;
      int[] px = new int[bi.getRaster().getNumBands()];
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          bi.getRaster().getPixel(x, y, px);
          int v = px[0] >> shift;
          argb[y * w + x] = 0xff000000 | v * 0x010101;
        }
      }
    } else {
      argb = bi.getRGB(0, 0, w, h, null, 0, w);
    }
    int mw = gray ? 8 : 16;
    int mcusX = (w + mw - 1) / mw;
    int mcusY = (h + mw - 1) / mw;
    int[] pred = new int[3];
    double[][] y = new double[4][64];
    double[] cb = new double[64];
    double[] cr = new double[64];
    for (int my = 0; my < mcusY; my++) {
      for (int mx = 0; mx < mcusX; mx++) {
        if (gray) {
          fillGray(argb, w, h, mx * 8, my * 8, y[0]);
          pred[0] = block(y[0], qt[0], 0, pred[0]);
        } else {
          for (int by = 0; by < 2; by++) {
            for (int bx = 0; bx < 2; bx++) {
              fillY(argb, w, h, mx * 16 + bx * 8, my * 16 + by * 8, y[by * 2 + bx]);
            }
          }
          fillChroma(argb, w, h, mx * 16, my * 16, cb, cr);
          for (int k = 0; k < 4; k++) {
            pred[0] = block(y[k], qt[0], 0, pred[0]);
          }
          pred[1] = block(cb, qt[1], 1, pred[1]);
          pred[2] = block(cr, qt[1], 1, pred[2]);
        }
      }
    }
    flushBitsToByte();
    out.write(0xff);
    out.write(0xd9);
    out.flush();
    processImageProgress(100f);
    processImageComplete();
  }

  private void huffTable(int id, int[] bits, int[] vals) throws IOException {
    out.write(0xff);
    out.write(0xc4);
    out.writeShort(19 + vals.length);
    out.write(id);
    for (int b : bits) {
      out.write(b);
    }
    for (int v : vals) {
      out.write(v);
    }
  }

  private static double luma(int p) {
    return 0.299 * ((p >> 16) & 0xff) + 0.587 * ((p >> 8) & 0xff) + 0.114 * (p & 0xff);
  }

  private static int pix(int[] argb, int w, int h, int x, int y) {
    return argb[Math.min(h - 1, y) * w + Math.min(w - 1, x)];
  }

  private static void fillGray(int[] argb, int w, int h, int x0, int y0, double[] out) {
    for (int y = 0; y < 8; y++) {
      for (int x = 0; x < 8; x++) {
        out[y * 8 + x] = luma(pix(argb, w, h, x0 + x, y0 + y)) - 128;
      }
    }
  }

  private static void fillY(int[] argb, int w, int h, int x0, int y0, double[] out) {
    fillGray(argb, w, h, x0, y0, out);
  }

  private static void fillChroma(int[] argb, int w, int h, int x0, int y0, double[] cb, double[] cr) {
    for (int y = 0; y < 8; y++) {
      for (int x = 0; x < 8; x++) {
        double sb = 0;
        double sr = 0;
        for (int dy = 0; dy < 2; dy++) {
          for (int dx = 0; dx < 2; dx++) {
            int p = pix(argb, w, h, x0 + x * 2 + dx, y0 + y * 2 + dy);
            double r = (p >> 16) & 0xff;
            double g = (p >> 8) & 0xff;
            double b = p & 0xff;
            sb += -0.168736 * r - 0.331264 * g + 0.5 * b;
            sr += 0.5 * r - 0.418688 * g - 0.081312 * b;
          }
        }
        cb[y * 8 + x] = sb / 4;
        cr[y * 8 + x] = sr / 4;
      }
    }
  }

  private int block(double[] samples, int[] q, int tbl, int pred) throws IOException {
    double[] f = new double[64];
    JpegTables.fdct(samples, f);
    int[] zz = new int[64];
    for (int i = 0; i < 64; i++) {
      int idx = JpegTables.kZigzag[i];
      zz[i] = (int) Math.round(f[idx] / q[idx]);
    }
    int diff = zz[0] - pred;
    int mag = diff == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(Math.abs(diff));
    put(dcCode[tbl][mag], dcLen[tbl][mag]);
    if (mag > 0) {
      put(diff < 0 ? diff - 1 : diff, mag);
    }
    int run = 0;
    for (int i = 1; i < 64; i++) {
      int v = zz[i];
      if (v == 0) {
        run++;
        continue;
      }
      while (run > 15) {
        put(acCode[tbl][0xf0], acLen[tbl][0xf0]);
        run -= 16;
      }
      int m = 32 - Integer.numberOfLeadingZeros(Math.abs(v));
      int sym = (run << 4) | m;
      put(acCode[tbl][sym], acLen[tbl][sym]);
      put(v < 0 ? v - 1 : v, m);
      run = 0;
    }
    if (run > 0) {
      put(acCode[tbl][0], acLen[tbl][0]);
    }
    return zz[0];
  }
}
