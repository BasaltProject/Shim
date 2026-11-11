/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import com.sun.imageio.plugins.tiff.TIFFImageMetadata;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.IIOException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.ExifParentTIFFTagSet;
import javax.imageio.plugins.tiff.FaxTIFFTagSet;
import javax.imageio.plugins.tiff.GeoTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public final class TiffReader extends CodecReader {
  private List<Long> ifdOffsets;
  private final List<TIFFDirectory> directories = new ArrayList<>();
  private ByteOrder order;

  public TiffReader(ImageReaderSpi spi) {
    super(spi);
  }

  public javax.imageio.ImageReadParam getDefaultReadParam() {
    return new javax.imageio.plugins.tiff.TIFFImageReadParam();
  }

  protected void resetState() {
    ifdOffsets = null;
    directories.clear();
  }

  private void scan() throws IOException {
    if (ifdOffsets != null) {
      return;
    }
    ImageInputStream in = stream();
    in.seek(0);
    int b0 = in.read();
    int b1 = in.read();
    if (b0 == 'I' && b1 == 'I') {
      order = ByteOrder.LITTLE_ENDIAN;
    } else if (b0 == 'M' && b1 == 'M') {
      order = ByteOrder.BIG_ENDIAN;
    } else {
      throw new IIOException("Invalid TIFF byte order");
    }
    in.setByteOrder(order);
    if (in.readUnsignedShort() != 42) {
      throw new IIOException("Bad TIFF magic number");
    }
    List<Long> offsets = new ArrayList<>();
    long off = in.readUnsignedInt();
    while (off != 0 && offsets.size() < 4096) {
      offsets.add(off);
      in.seek(off);
      int n = in.readUnsignedShort();
      in.seek(off + 2 + n * 12L);
      off = in.readUnsignedInt();
    }
    ifdOffsets = offsets;
  }

  protected int imageCount() throws IOException {
    scan();
    return ifdOffsets.size();
  }

  private static TIFFTagSet[] rootSets() {
    return new TIFFTagSet[] {BaselineTIFFTagSet.getInstance(), FaxTIFFTagSet.getInstance(),
        ExifParentTIFFTagSet.getInstance(), GeoTIFFTagSet.getInstance()};
  }

  private TIFFDirectory directory(int index) throws IOException {
    scan();
    while (directories.size() <= index) {
      directories.add(null);
    }
    TIFFDirectory d = directories.get(index);
    if (d == null) {
      d = parseIfd(ifdOffsets.get(index), rootSets(), null, 0);
      directories.set(index, d);
    }
    return d;
  }

  private TIFFDirectory parseIfd(long offset, TIFFTagSet[] sets, TIFFTag parent, int depth) throws IOException {
    ImageInputStream in = stream();
    in.setByteOrder(order);
    TIFFDirectory dir = new TIFFDirectory(sets, parent);
    in.seek(offset);
    int n = in.readUnsignedShort();
    for (int i = 0; i < n; i++) {
      in.seek(offset + 2 + i * 12L);
      int tagNumber = in.readUnsignedShort();
      int type = in.readUnsignedShort();
      long count = in.readUnsignedInt();
      byte[] raw4 = new byte[4];
      in.readFully(raw4);
      if (type < 1 || type > 13) {
        continue;
      }
      int size = TIFFTag.getSizeOfType(type);
      long total = size * count;
      long valueOffset = total <= 4 ? offset + 2 + i * 12L + 8 : readOffset(raw4);
      if (count > Integer.MAX_VALUE || total > in.length() && in.length() >= 0) {
        continue;
      }
      TIFFTag tag = null;
      for (TIFFTagSet ts : sets) {
        tag = ts.getTag(tagNumber);
        if (tag != null) {
          break;
        }
      }
      if (tag == null || !tag.isDataTypeOK(type)) {
        tag = new TIFFTag(TIFFTag.UNKNOWN_TAG_NAME, tagNumber, 1 << type);
      }
      long pos = in.getStreamPosition();
      in.seek(valueOffset);
      Object data = readValues(in, type, (int) count);
      TIFFField field;
      if (tag.isIFDPointer() && depth < 4 && data instanceof long[] && ((long[]) data).length > 0) {
        long sub = ((long[]) data)[0];
        TIFFTagSet[] childSets = new TIFFTagSet[] {tag.getTagSet()};
        TIFFDirectory child = parseIfd(sub, childSets, tag, depth + 1);
        field = new TIFFField(tag, type, sub, child);
      } else {
        int fieldCount = type == 2 ? ((String[]) data).length : (int) count;
        field = new TIFFField(tag, type, fieldCount, data);
      }
      dir.addTIFFField(field);
      in.seek(pos);
    }
    return dir;
  }

  private long readOffset(byte[] raw) {
    long v = 0;
    if (order == ByteOrder.BIG_ENDIAN) {
      for (int i = 0; i < 4; i++) {
        v = (v << 8) | (raw[i] & 0xff);
      }
    } else {
      for (int i = 3; i >= 0; i--) {
        v = (v << 8) | (raw[i] & 0xff);
      }
    }
    return v;
  }

  private static Object readValues(ImageInputStream in, int type, int count) throws IOException {
    switch (type) {
    case 1:
    case 6:
    case 7: {
      byte[] b = new byte[count];
      in.readFully(b);
      return b;
    }
    case 2: {
      byte[] b = new byte[count];
      in.readFully(b);
      List<String> strings = new ArrayList<>();
      int start = 0;
      for (int i = 0; i < count; i++) {
        if (b[i] == 0) {
          strings.add(new String(b, start, i - start, StandardCharsets.ISO_8859_1));
          start = i + 1;
        }
      }
      if (start < count) {
        strings.add(new String(b, start, count - start, StandardCharsets.ISO_8859_1));
      }
      return strings.toArray(new String[0]);
    }
    case 3: {
      char[] c = new char[count];
      for (int i = 0; i < count; i++) {
        c[i] = (char) in.readUnsignedShort();
      }
      return c;
    }
    case 8: {
      short[] c = new short[count];
      in.readFully(c, 0, count);
      return c;
    }
    case 4:
    case 13: {
      long[] c = new long[count];
      for (int i = 0; i < count; i++) {
        c[i] = in.readUnsignedInt();
      }
      return c;
    }
    case 9: {
      int[] c = new int[count];
      in.readFully(c, 0, count);
      return c;
    }
    case 5: {
      long[][] c = new long[count][2];
      for (int i = 0; i < count; i++) {
        c[i][0] = in.readUnsignedInt();
        c[i][1] = in.readUnsignedInt();
      }
      return c;
    }
    case 10: {
      int[][] c = new int[count][2];
      for (int i = 0; i < count; i++) {
        c[i][0] = in.readInt();
        c[i][1] = in.readInt();
      }
      return c;
    }
    case 11: {
      float[] c = new float[count];
      in.readFully(c, 0, count);
      return c;
    }
    default: {
      double[] c = new double[count];
      in.readFully(c, 0, count);
      return c;
    }
    }
  }

  public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return new TIFFImageMetadata(directory(imageIndex));
  }

  private static int intField(TIFFDirectory d, int tag, int def) {
    TIFFField f = d.getTIFFField(tag);
    return f == null ? def : f.getAsInt(0);
  }

  private static long[] longsOf(TIFFDirectory d, int tag) {
    TIFFField f = d.getTIFFField(tag);
    if (f == null) {
      return null;
    }
    long[] r = new long[f.getCount()];
    for (int i = 0; i < r.length; i++) {
      r[i] = f.getAsLong(i);
    }
    return r;
  }

  protected BufferedImage decode(int index) throws IOException {
    TIFFDirectory d = directory(index);
    ImageInputStream in = stream();
    if (intField(d, 259, 1) == 6 && d.containsTIFFField(513)) {
      return exifJpeg(d);
    }
    int w = intField(d, 256, 0);
    int h = intField(d, 257, 0);
    if (w <= 0 || h <= 0) {
      throw new IIOException("Missing or invalid image dimensions");
    }
    int spp = intField(d, 277, 1);
    TIFFField bpsField = d.getTIFFField(258);
    int bits = bpsField == null ? 1 : bpsField.getAsInt(0);
    int photometric = intField(d, 262, -1);
    if (photometric < 0) {
      throw new IIOException("Missing PhotometricInterpretation");
    }
    int compression = intField(d, 259, 1);
    int planar = intField(d, 284, 1);
    int predictor = intField(d, 317, 1);
    int sampleFormat = intField(d, 339, 1);
    int fillOrder = intField(d, 266, 1);
    int extra = intField(d, 338, -1);
    if (sampleFormat != 1 || (bits != 1 && bits != 2 && bits != 4 && bits != 8 && bits != 16)) {
      throw new IIOException("Unsupported sample format or bit depth");
    }
    boolean jpegData = compression == 7;
    if (jpegData && photometric == 6) {
      photometric = 2;
    }
    if (photometric != 0 && photometric != 1 && photometric != 2 && photometric != 3) {
      throw new IIOException("Unsupported PhotometricInterpretation " + photometric);
    }
    if (compression != 1 && compression != 5 && compression != 8 && compression != 32946 && compression != 32773
        && (compression < 2 || compression > 4 || bits != 1 || spp != 1)
        && !(jpegData && bits == 8 && (spp == 1 || spp == 3))) {
      throw new IIOException("Unsupported compression " + compression);
    }
    boolean tiled = d.containsTIFFField(322);
    int cw = tiled ? intField(d, 322, w) : w;
    int chh = tiled ? intField(d, 323, h) : Math.min(h, intField(d, 278, h) <= 0 ? h : intField(d, 278, h));
    long[] offs = longsOf(d, tiled ? 324 : 273);
    long[] cnts = longsOf(d, tiled ? 325 : 279);
    if (offs == null) {
      throw new IIOException("Missing strip or tile offsets");
    }
    int across = (w + cw - 1) / cw;
    int down = (h + chh - 1) / chh;
    int planes = planar == 2 ? spp : 1;
    int perPlane = across * down;
    int pixelSamples = planar == 2 ? 1 : spp;
    int bytesPerSample = bits == 16 ? 2 : 1;
    int rowBytesImage = (w * spp * bits + 7) / 8;
    byte[] pixels = new byte[rowBytesImage * h];
    boolean sub8 = bits < 8;
    for (int plane = 0; plane < planes; plane++) {
      for (int ty = 0; ty < down; ty++) {
        for (int tx = 0; tx < across; tx++) {
          int idx = plane * perPlane + ty * across + tx;
          if (idx >= offs.length) {
            continue;
          }
          int x0 = tx * cw;
          int y0 = ty * chh;
          int curW = tiled ? cw : w;
          int curH = tiled ? chh : Math.min(chh, h - y0);
          int rowBytes = (curW * pixelSamples * bits + 7) / 8;
          long count = cnts != null && idx < cnts.length ? cnts[idx] : in.length() - offs[idx];
          byte[] enc = new byte[(int) Math.min(count, Integer.MAX_VALUE)];
          in.seek(offs[idx]);
          in.readFully(enc);
          if (fillOrder == 2) {
            for (int i = 0; i < enc.length; i++) {
              enc[i] = (byte) (Integer.reverse(enc[i] & 0xff) >>> 24);
            }
          }
          int expected = rowBytes * curH;
          byte[] raw;
          switch (compression) {
          case 5:
            raw = TiffCodec.lzwDecode(enc, expected);
            break;
          case 8:
          case 32946:
            raw = TiffCodec.inflate(enc, expected);
            break;
          case 32773:
            raw = TiffCodec.packBitsDecode(enc, expected);
            break;
          case 7:
            raw = jpegStrip(enc, d, curW, curH, spp);
            break;
          case 2:
          case 3:
          case 4:
            raw = TiffFax.decode(enc, rowBytes, curW, curH, compression, false, (intField(d, 292, 0) & 1) != 0);
            if (photometric == 1) {
              for (int i = 0; i < raw.length; i++) {
                raw[i] = (byte) ~raw[i];
              }
            }
            break;
          default:
            raw = java.util.Arrays.copyOf(enc, expected);
          }
          if (predictor == 2 && bits >= 8) {
            for (int row = 0; row < curH; row++) {
              int base = row * rowBytes;
              if (bytesPerSample == 1) {
                for (int i = pixelSamples; i < curW * pixelSamples; i++) {
                  raw[base + i] += raw[base + i - pixelSamples];
                }
              } else {
                for (int i = pixelSamples; i < curW * pixelSamples; i++) {
                  int a = sample16(raw, base + i * 2);
                  int b = sample16(raw, base + (i - pixelSamples) * 2);
                  put16(raw, base + i * 2, a + b);
                }
              }
            }
          }
          int copyW = Math.min(curW, w - x0);
          for (int row = 0; row < curH && y0 + row < h; row++) {
            if (sub8) {
              if (tiled && cw != w) {
                for (int x = 0; x < copyW; x++) {
                  int sbit = x * spp * bits;
                  int dbit = (x0 + x) * spp * bits;
                  int v = (raw[row * rowBytes + (sbit >> 3)] >> (8 - bits - (sbit & 7))) & ((1 << bits) - 1);
                  pixels[(y0 + row) * rowBytesImage + (dbit >> 3)] |= (byte) (v << (8 - bits - (dbit & 7)));
                }
              } else {
                System.arraycopy(raw, row * rowBytes, pixels, (y0 + row) * rowBytesImage, rowBytes);
              }
            } else if (planar == 2) {
              int step = spp * bytesPerSample;
              for (int x = 0; x < copyW; x++) {
                for (int k = 0; k < bytesPerSample; k++) {
                  pixels[(y0 + row) * rowBytesImage + (x0 + x) * step + plane * bytesPerSample + k] = raw[row
                      * rowBytes + x * bytesPerSample + k];
                }
              }
            } else {
              int step = spp * bytesPerSample;
              System.arraycopy(raw, row * rowBytes, pixels, (y0 + row) * rowBytesImage + x0 * step, copyW * step);
            }
          }
        }
      }
    }
    return build(d, pixels, w, h, spp, bits, photometric, extra);
  }

  private int sample16(byte[] b, int p) {
    return order == ByteOrder.BIG_ENDIAN ? ((b[p] & 0xff) << 8) | (b[p + 1] & 0xff)
        : ((b[p + 1] & 0xff) << 8) | (b[p] & 0xff);
  }

  private void put16(byte[] b, int p, int v) {
    if (order == ByteOrder.BIG_ENDIAN) {
      b[p] = (byte) (v >> 8);
      b[p + 1] = (byte) v;
    } else {
      b[p] = (byte) v;
      b[p + 1] = (byte) (v >> 8);
    }
  }

  private BufferedImage build(TIFFDirectory d, byte[] pixels, int w, int h, int spp, int bits, int photometric,
      int extra) throws IIOException {
    if (spp == 1 && (photometric == 0 || photometric == 1 || photometric == 3)) {
      if (bits < 8 || photometric == 3) {
        IndexColorModel icm;
        int size = 1 << bits;
        if (photometric == 3) {
          TIFFField cmap = d.getTIFFField(320);
          if (cmap == null) {
            throw new IIOException("Missing ColorMap");
          }
          byte[] r = new byte[size];
          byte[] g = new byte[size];
          byte[] b = new byte[size];
          for (int i = 0; i < size && 2 * size + i < cmap.getCount(); i++) {
            r[i] = (byte) (cmap.getAsInt(i) >> 8);
            g[i] = (byte) (cmap.getAsInt(size + i) >> 8);
            b[i] = (byte) (cmap.getAsInt(2 * size + i) >> 8);
          }
          icm = new IndexColorModel(bits, size, r, g, b);
        } else {
          byte[] ramp = new byte[size];
          for (int i = 0; i < size; i++) {
            int v = i * 255 / (size - 1);
            ramp[i] = (byte) (photometric == 0 ? 255 - v : v);
          }
          icm = new IndexColorModel(bits, size, ramp, ramp, ramp);
        }
        int type = bits == 8 ? BufferedImage.TYPE_BYTE_INDEXED : BufferedImage.TYPE_BYTE_BINARY;
        BufferedImage bi = new BufferedImage(w, h, type, icm);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        System.arraycopy(pixels, 0, data, 0, Math.min(data.length, pixels.length));
        return bi;
      }
      if (bits == 8) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < data.length; i++) {
          data[i] = photometric == 0 ? (byte) (255 - (pixels[i] & 0xff)) : pixels[i];
        }
        return bi;
      }
      BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
      short[] data = ((DataBufferUShort) bi.getRaster().getDataBuffer()).getData();
      for (int i = 0; i < data.length; i++) {
        int v = sample16(pixels, i * 2);
        data[i] = (short) (photometric == 0 ? 65535 - v : v);
      }
      return bi;
    }
    boolean gray = photometric == 0 || photometric == 1;
    if (photometric == 2 && spp < 3 || gray && spp < 2 || photometric == 3) {
      throw new IIOException("Unsupported sample layout");
    }
    int colorBands = gray ? 1 : 3;
    boolean alpha = spp > colorBands && extra >= 0 && extra != 0;
    if (!alpha && spp > colorBands) {
      spp = colorBands;
    }
    int srcSpp = spp;
    boolean premultiplied = extra == 1;
    int bands = alpha ? colorBands + 1 : colorBands;
    ColorSpace cs = ColorSpace.getInstance(gray ? ColorSpace.CS_GRAY : ColorSpace.CS_sRGB);
    int dataType = bits == 16 ? DataBuffer.TYPE_USHORT : DataBuffer.TYPE_BYTE;
    ComponentColorModel cm = new ComponentColorModel(cs, alpha, premultiplied,
        alpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE, dataType);
    int[] offsets = new int[bands];
    for (int i = 0; i < bands; i++) {
      offsets[i] = i;
    }
    WritableRaster r = Raster.createInterleavedRaster(dataType, w, h, w * bands, bands, offsets, null);
    int fileSpp = intField(d, 277, 1);
    int stride = fileSpp;
    if (dataType == DataBuffer.TYPE_BYTE) {
      byte[] data = ((DataBufferByte) r.getDataBuffer()).getData();
      for (int p = 0; p < w * h; p++) {
        for (int b = 0; b < bands; b++) {
          byte v = pixels[p * stride + b];
          data[p * bands + b] = photometric == 0 && b < colorBands ? (byte) (255 - (v & 0xff)) : v;
        }
      }
    } else {
      short[] data = ((DataBufferUShort) r.getDataBuffer()).getData();
      for (int p = 0; p < w * h; p++) {
        for (int b = 0; b < bands; b++) {
          int v = sample16(pixels, (p * stride + b) * 2);
          data[p * bands + b] = (short) (photometric == 0 && b < colorBands ? 65535 - v : v);
        }
      }
    }
    return new BufferedImage(cm, r, premultiplied, null);
  }

  private BufferedImage exifJpeg(TIFFDirectory d) throws IOException {
    ImageInputStream in = stream();
    long offset = d.getTIFFField(513).getAsLong(0);
    int length = d.containsTIFFField(514) ? d.getTIFFField(514).getAsInt(0) : (int) (in.length() - offset);
    byte[] data = new byte[length];
    in.seek(offset);
    in.readFully(data);
    BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(data));
    if (img == null) {
      throw new IIOException("Cannot decode JPEG compressed TIFF data");
    }
    if (img.getRaster().getNumBands() != 3) {
      return img;
    }
    java.awt.image.WritableRaster raster = java.awt.image.Raster.createInterleavedRaster(
        java.awt.image.DataBuffer.TYPE_BYTE, img.getWidth(), img.getHeight(), img.getWidth() * 3, 3,
        new int[] {0, 1, 2}, null);
    int[] px = new int[3];
    for (int y = 0; y < img.getHeight(); y++) {
      for (int x = 0; x < img.getWidth(); x++) {
        int argb = img.getRGB(x, y);
        px[0] = (argb >> 16) & 0xff;
        px[1] = (argb >> 8) & 0xff;
        px[2] = argb & 0xff;
        raster.setPixel(x, y, px);
      }
    }
    java.awt.image.ColorModel cm = new java.awt.image.ComponentColorModel(
        java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_sRGB), false, false,
        java.awt.Transparency.OPAQUE, java.awt.image.DataBuffer.TYPE_BYTE);
    return new BufferedImage(cm, raster, false, null);
  }

  private static byte[] jpegStrip(byte[] enc, TIFFDirectory d, int width, int rows, int bands) throws IOException {
    TIFFField tables = d.getTIFFField(347);
    byte[] stream = enc;
    if (tables != null && enc.length > 2 && (enc[0] & 0xff) == 0xff && (enc[1] & 0xff) == 0xd8) {
      byte[] t = tables.getAsBytes();
      int tl = t.length >= 4 && (t[t.length - 2] & 0xff) == 0xff && (t[t.length - 1] & 0xff) == 0xd9 ? t.length - 2
                                                                                                      : t.length;
      ByteArrayOutputStream merged = new ByteArrayOutputStream();
      merged.write(enc, 0, 2);
      merged.write(t, 2, tl - 2);
      merged.write(enc, 2, enc.length - 2);
      stream = merged.toByteArray();
    }
    BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(stream));
    if (img == null) {
      throw new IIOException("Cannot decode JPEG compressed TIFF data");
    }
    byte[] out = new byte[width * rows * bands];
    for (int y = 0; y < Math.min(rows, img.getHeight()); y++) {
      for (int x = 0; x < Math.min(width, img.getWidth()); x++) {
        int p = (y * width + x) * bands;
        if (bands == 1) {
          out[p] = (byte) img.getRaster().getSample(x, y, 0);
        } else {
          int argb = img.getRGB(x, y);
          out[p] = (byte) (argb >> 16);
          out[p + 1] = (byte) (argb >> 8);
          out[p + 2] = (byte) argb;
        }
      }
    }
    return out;
  }
}
