/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public final class PngWriter extends CodecWriter {
  public PngWriter(ImageWriterSpi spi) {
    super(spi);
  }

  public IIOMetadata getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier type, ImageWriteParam param) {
    return PngMetadata.defaultFor(type);
  }

  public IIOMetadata convertImageMetadata(IIOMetadata in, javax.imageio.ImageTypeSpecifier type, ImageWriteParam param) {
    if (in instanceof PngMetadata) {
      return in;
    }
    PngMetadata m = PngMetadata.defaultFor(type);
    try {
      for (String f : in.getMetadataFormatNames()) {
        if (f.equals(PngMetadata.kFormat) || f.equals("javax_imageio_1.0")) {
          m.mergeTree(f, in.getAsTree(f));
          return m;
        }
      }
    } catch (javax.imageio.metadata.IIOInvalidTreeException e) {
      return null;
    }
    return null;
  }

  public ImageWriteParam getDefaultWriteParam() {
    return new com.sun.imageio.plugins.png.PNGImageWriteParam(getLocale());
  }

  public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
    ImageOutputStream out = stream();
    if (image.hasRaster()) {
      throw new UnsupportedOperationException("Rasters not supported");
    }
    RenderedImage ri = image.getRenderedImage();
    BufferedImage bi = shim.awt.ImageSupport.toBuffered(ri);
    int w = bi.getWidth();
    int h = bi.getHeight();
    PngMetadata meta = image.getMetadata() instanceof PngMetadata ? (PngMetadata) image.getMetadata() : null;
    boolean interlaced = meta != null ? meta.interlaced() : false;
    if (param != null && param.canWriteProgressive() && param.getProgressiveMode() == ImageWriteParam.MODE_DEFAULT) {
      interlaced = true;
    }
    ColorModel cm = bi.getColorModel();
    int colorType;
    int depth = 8;
    byte[] plte = null;
    byte[] trns = null;
    int channels;
    boolean gray = cm instanceof ComponentColorModel && cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY
        && cm.getNumColorComponents() == 1 && cm.getComponentSize(0) >= 8;
    boolean indexed = cm instanceof IndexColorModel;
    boolean adaptive = false;
    if (indexed) {
      IndexColorModel icm = (IndexColorModel) cm;
      colorType = 3;
      channels = 1;
      int n = icm.getMapSize();
      int pixelBits = icm.getPixelSize();
      depth = pixelBits <= 1 ? 1 : pixelBits <= 2 ? 2 : pixelBits <= 4 ? 4 : 8;
      adaptive = true;
      plte = new byte[n * 3];
      byte[] a = new byte[n];
      boolean anyAlpha = false;
      for (int i = 0; i < n; i++) {
        plte[i * 3] = (byte) icm.getRed(i);
        plte[i * 3 + 1] = (byte) icm.getGreen(i);
        plte[i * 3 + 2] = (byte) icm.getBlue(i);
        a[i] = (byte) icm.getAlpha(i);
        anyAlpha |= (a[i] & 0xff) != 255;
      }
      if (n == 1 << pixelBits && !anyAlpha && pixelBits <= 8 && isGrayRamp(icm, n)) {
        indexed = false;
        adaptive = false;
        gray = true;
        colorType = 0;
        plte = null;
      }
      if (anyAlpha) {
        int last = n;
        while (last > 0 && (a[last - 1] & 0xff) == 255) {
          last--;
        }
        trns = java.util.Arrays.copyOf(a, last);
      }
    } else if (gray) {
      colorType = cm.hasAlpha() ? 4 : 0;
      channels = cm.hasAlpha() ? 2 : 1;
      depth = cm.getComponentSize(0) >= 16 ? 16 : 8;
    } else if (cm.hasAlpha()) {
      colorType = 6;
      channels = 4;
    } else {
      colorType = 2;
      channels = 3;
    }
    ByteArrayOutputStream raw = new ByteArrayOutputStream();
    int bpp = Math.max(1, channels * depth / 8);
    int[] passX = interlaced ? new int[] {0, 4, 0, 2, 0, 1, 0} : new int[] {0};
    int[] passY = interlaced ? new int[] {0, 0, 4, 0, 2, 0, 1} : new int[] {0};
    int[] stepX = interlaced ? new int[] {8, 8, 4, 4, 2, 2, 1} : new int[] {1};
    int[] stepY = interlaced ? new int[] {8, 8, 8, 4, 4, 2, 2} : new int[] {1};
    for (int pass = 0; pass < passX.length; pass++) {
    int pw = (w - passX[pass] + stepX[pass] - 1) / stepX[pass];
    int ph = (h - passY[pass] + stepY[pass] - 1) / stepY[pass];
    if (pw <= 0 || ph <= 0) {
      continue;
    }
    int rowBytes = (pw * channels * depth + 7) / 8;
    byte[] prev = new byte[rowBytes];
    byte[] cur = new byte[rowBytes];
    byte[][] cand = new byte[5][rowBytes];
    Raster r = bi.getRaster();
    int[] px = new int[Math.max(1, r.getNumBands())];
    for (int row = 0; row < ph; row++) {
      int y = passY[pass] + row * stepY[pass];
      java.util.Arrays.fill(cur, (byte) 0);
      for (int col = 0; col < pw; col++) {
        int x = passX[pass] + col * stepX[pass];
        if (indexed || gray) {
          r.getPixel(x, y, px);
          int v = px[0];
          if (depth == 16) {
            for (int c = 0; c < channels; c++) {
              cur[(col * channels + c) * 2] = (byte) (px[c] >> 8);
              cur[(col * channels + c) * 2 + 1] = (byte) px[c];
            }
          } else if (gray && channels == 2) {
            cur[col * 2] = (byte) px[0];
            cur[col * 2 + 1] = (byte) px[1];
          } else if (depth == 8) {
            cur[col] = (byte) v;
          } else {
            int bit = col * depth;
            cur[bit >> 3] |= (byte) (v << (8 - depth - (bit & 7)));
          }
        } else {
          int argb = bi.getRGB(x, y);
          int o = col * channels;
          cur[o] = (byte) (argb >> 16);
          cur[o + 1] = (byte) (argb >> 8);
          cur[o + 2] = (byte) argb;
          if (channels == 4) {
            cur[o + 3] = (byte) (argb >>> 24);
          }
        }
      }
      int best = 0;
      long bestSum = Long.MAX_VALUE;
      for (int f = 0; f < (adaptive ? 5 : 1); f++) {
        filter(f, cur, prev, cand[f], bpp);
        long sum = 0;
        for (int i = 0; i < rowBytes; i++) {
          sum += Math.abs((cur[i] & 0xff) - ((cur[i] - cand[f][i]) & 0xff));
        }
        if (sum < bestSum) {
          bestSum = sum;
          best = f;
        }
      }
      raw.write(best);
      raw.write(cand[best], 0, rowBytes);
      byte[] t = prev;
      prev = cur;
      cur = t;
    }
    }
    Deflater def = new Deflater(level(param));
    def.setInput(raw.toByteArray());
    def.finish();
    ByteArrayOutputStream z = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    while (!def.finished()) {
      int n = def.deflate(buf);
      z.write(buf, 0, n);
    }
    def.end();
    processImageStarted(0);
    out.write(new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a});
    byte[] ihdr = new byte[13];
    putInt(ihdr, 0, w);
    putInt(ihdr, 4, h);
    ihdr[8] = (byte) depth;
    ihdr[9] = (byte) colorType;
    ihdr[12] = (byte) (interlaced ? 1 : 0);
    chunk(out, "IHDR", ihdr);
    PngMetadata.ChunkSink sink = (t, d) -> chunk(out, t, d);
    if (meta != null) {
      meta.writeEarly(sink, null);
    }
    if (plte != null) {
      chunk(out, "PLTE", plte);
    }
    if (trns != null) {
      chunk(out, "tRNS", trns);
    }
    if (meta != null) {
      meta.writeLate(sink, null, trns != null);
    }
    byte[] zd = z.toByteArray();
    int off = 0;
    while (off < zd.length || off == 0) {
      int n = Math.min(zd.length - off, 65536);
      chunk(out, "IDAT", java.util.Arrays.copyOfRange(zd, off, off + n));
      off += n;
      if (n == 0) {
        break;
      }
    }
    chunk(out, "IEND", new byte[0]);
    out.flush();
    processImageProgress(100f);
    processImageComplete();
  }

  private static int level(ImageWriteParam param) {
    if (param != null && param.canWriteCompressed() && param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT) {
      return 9 - Math.round(param.getCompressionQuality() * 9);
    }
    return 4;
  }

  private static boolean isGrayRamp(IndexColorModel icm, int n) {
    for (int i = 0; i < n; i++) {
      int v = n == 1 ? 0 : i * 255 / (n - 1);
      if (icm.getRed(i) != v || icm.getGreen(i) != v || icm.getBlue(i) != v) {
        return false;
      }
    }
    return true;
  }

  private static void filter(int type, byte[] cur, byte[] prev, byte[] out, int bpp) {
    int n = cur.length;
    for (int i = 0; i < n; i++) {
      int a = i >= bpp ? cur[i - bpp] & 0xff : 0;
      int b = prev[i] & 0xff;
      int c = i >= bpp ? prev[i - bpp] & 0xff : 0;
      int x = cur[i] & 0xff;
      int pred;
      switch (type) {
      case 1:
        pred = a;
        break;
      case 2:
        pred = b;
        break;
      case 3:
        pred = (a + b) >> 1;
        break;
      case 4: {
        int pa = Math.abs(b - c);
        int pb = Math.abs(a - c);
        int pc = Math.abs(a + b - 2 * c);
        pred = (pa <= pb && pa <= pc) ? a : (pb <= pc ? b : c);
        break;
      }
      default:
        pred = 0;
        break;
      }
      out[i] = (byte) (x - pred);
    }
  }

  private static void putInt(byte[] b, int o, int v) {
    b[o] = (byte) (v >>> 24);
    b[o + 1] = (byte) (v >>> 16);
    b[o + 2] = (byte) (v >>> 8);
    b[o + 3] = (byte) v;
  }

  private static void chunk(ImageOutputStream out, String type, byte[] body) throws IOException {
    byte[] t = type.getBytes("ISO-8859-1");
    byte[] len = new byte[4];
    putInt(len, 0, body.length);
    out.write(len);
    out.write(t);
    out.write(body);
    CRC32 crc = new CRC32();
    crc.update(t);
    crc.update(body);
    byte[] c = new byte[4];
    putInt(c, 0, (int) crc.getValue());
    out.write(c);
  }
}
