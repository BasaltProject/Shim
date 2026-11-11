/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import com.sun.imageio.plugins.tiff.TIFFImageMetadata;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFDirectory;
import javax.imageio.plugins.tiff.TIFFField;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public final class TiffWriter extends CodecWriter {
  private static final String[] kCompressionTypes = {"CCITT RLE", "CCITT T.4", "CCITT T.6", "LZW", "JPEG", "ZLib",
      "PackBits", "Deflate", "Exif JPEG"};
  private static final int[] kStructural = {256, 257, 292, 258, 259, 262, 273, 277, 278, 279, 320, 322, 323, 324, 325,
      338, 339};
  private long pendingNextPointer = -1;
  private boolean sequence;

  private static final class Layout {
    int bands;
    int bits;
    int photometric;
    int extra = -1;
    int[] colormap;
    boolean viaRgb;
    boolean indexed;
  }

  private static final class Entry {
    final int tag;
    final int type;
    final int count;
    final byte[] data;

    Entry(int tag, int type, int count, byte[] data) {
      this.tag = tag;
      this.type = type;
      this.count = count;
      this.data = data;
    }
  }

  public TiffWriter(ImageWriterSpi spi) {
    super(spi);
  }

  public ImageWriteParam getDefaultWriteParam() {
    return new com.sun.imageio.plugins.tiff.TIFFImageWriteParam(getLocale());
  }

  public boolean canWriteRasters() {
    return true;
  }

  public boolean canWriteSequence() {
    return true;
  }

  private static Layout layout(ColorModel cm, SampleModel sm) {
    Layout l = new Layout();
    int nb = sm.getNumBands();
    if (cm instanceof IndexColorModel && nb == 1 && sm.getSampleSize(0) <= 8) {
      IndexColorModel icm = (IndexColorModel) cm;
      l.indexed = true;
      l.bands = 1;
      l.bits = sm.getSampleSize(0);
      l.photometric = 3;
      int size = 1 << l.bits;
      if (l.bits == 1 && icm.getMapSize() == 2) {
        int c0 = icm.getRGB(0) & 0xffffff;
        int c1 = icm.getRGB(1) & 0xffffff;
        if (c0 == 0 && c1 == 0xffffff) {
          l.photometric = 1;
        } else if (c0 == 0xffffff && c1 == 0) {
          l.photometric = 0;
        }
      }
      if (l.photometric == 3) {
        l.colormap = new int[3 * size];
        for (int i = 0; i < size && i < icm.getMapSize(); i++) {
          l.colormap[i] = icm.getRed(i) * 257;
          l.colormap[size + i] = icm.getGreen(i) * 257;
          l.colormap[2 * size + i] = icm.getBlue(i) * 257;
        }
      }
      return l;
    }
    boolean uniform = true;
    for (int b = 0; b < nb; b++) {
      int s = sm.getSampleSize(b);
      if (s != sm.getSampleSize(0) || (s != 8 && s != 16)) {
        uniform = false;
      }
    }
    int type = cm.getColorSpace().getType();
    if (uniform && (type == java.awt.color.ColorSpace.TYPE_GRAY || type == java.awt.color.ColorSpace.TYPE_RGB)
        && nb == cm.getNumComponents()) {
      l.bands = nb;
      l.bits = sm.getSampleSize(0);
      l.photometric = type == java.awt.color.ColorSpace.TYPE_GRAY ? 1 : 2;
      if (cm.hasAlpha()) {
        l.extra = cm.isAlphaPremultiplied() ? 1 : 2;
      }
      return l;
    }
    l.viaRgb = true;
    l.bits = 8;
    l.photometric = 2;
    l.bands = cm.hasAlpha() ? 4 : 3;
    if (cm.hasAlpha()) {
      l.extra = 2;
    }
    return l;
  }

  public javax.imageio.metadata.IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier type, ImageWriteParam param) {
    Layout l = layout(type.getColorModel(), type.getSampleModel());
    TIFFDirectory dir = new TIFFDirectory(new javax.imageio.plugins.tiff.TIFFTagSet[] {BaselineTIFFTagSet.getInstance()},
        null);
    BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
    char[] bps = new char[l.bands];
    Arrays.fill(bps, (char) l.bits);
    dir.addTIFFField(new TIFFField(base.getTag(256), 1));
    dir.addTIFFField(new TIFFField(base.getTag(257), 1));
    if (!(l.bits == 1 && l.bands == 1)) {
      dir.addTIFFField(new TIFFField(base.getTag(258), TIFFTag.TIFF_SHORT, l.bands, bps));
    }
    dir.addTIFFField(new TIFFField(base.getTag(259), 1));
    dir.addTIFFField(new TIFFField(base.getTag(262), l.photometric));
    dir.addTIFFField(new TIFFField(base.getTag(273), TIFFTag.TIFF_LONG, 1, new long[] {0}));
    dir.addTIFFField(new TIFFField(base.getTag(277), l.bands));
    dir.addTIFFField(new TIFFField(base.getTag(278), 1));
    dir.addTIFFField(new TIFFField(base.getTag(279), TIFFTag.TIFF_LONG, 1, new long[] {0}));
    dir.addTIFFField(new TIFFField(base.getTag(282), TIFFTag.TIFF_RATIONAL, 1, new long[][] {{1, 1}}));
    dir.addTIFFField(new TIFFField(base.getTag(283), TIFFTag.TIFF_RATIONAL, 1, new long[][] {{1, 1}}));
    dir.addTIFFField(new TIFFField(base.getTag(296), 1));
    if (l.colormap != null) {
      char[] map = new char[l.colormap.length];
      for (int i = 0; i < map.length; i++) {
        map[i] = (char) l.colormap[i];
      }
      dir.addTIFFField(new TIFFField(base.getTag(320), TIFFTag.TIFF_SHORT, map.length, map));
    }
    if (l.extra >= 0) {
      dir.addTIFFField(new TIFFField(base.getTag(338), l.extra));
    }
    if (l.bits == 16) {
      char[] sf = new char[l.bands];
      Arrays.fill(sf, (char) 1);
      dir.addTIFFField(new TIFFField(base.getTag(339), TIFFTag.TIFF_SHORT, l.bands, sf));
    }
    return new TIFFImageMetadata(dir);
  }

  private int compressionTag(ImageWriteParam param, TIFFDirectory meta) throws IOException {
    String ct = null;
    if (param != null && param.canWriteCompressed()) {
      int mode = param.getCompressionMode();
      if (mode == ImageWriteParam.MODE_EXPLICIT) {
        ct = param.getCompressionType();
        if (ct == null) {
          throw new IllegalStateException("Compression type is not set!");
        }
      } else if (mode == ImageWriteParam.MODE_COPY_FROM_METADATA && meta != null
          && meta.containsTIFFField(259)) {
        return meta.getTIFFField(259).getAsInt(0);
      }
    } else if (meta != null && meta.containsTIFFField(259)) {
      return meta.getTIFFField(259).getAsInt(0);
    }
    if (ct == null) {
      return 1;
    }
    switch (ct) {
    case "LZW":
      return 5;
    case "ZLib":
      return 8;
    case "Deflate":
      return 32946;
    case "PackBits":
      return 32773;
    case "JPEG":
      return 7;
    case "CCITT RLE":
      return 2;
    case "CCITT T.4":
      return 3;
    case "CCITT T.6":
      return 4;
    case "Exif JPEG":
      throw new IIOException("Old JPEG compression not supported!");
    default:
      throw new IIOException("Unsupported compression " + ct);
    }
  }

  private static byte[] shorts(ByteOrder order, int... v) {
    ByteBuffer bb = ByteBuffer.allocate(v.length * 2).order(order);
    for (int x : v) {
      bb.putShort((short) x);
    }
    return bb.array();
  }

  private static byte[] longs(ByteOrder order, long... v) {
    ByteBuffer bb = ByteBuffer.allocate(v.length * 4).order(order);
    for (long x : v) {
      bb.putInt((int) x);
    }
    return bb.array();
  }

  private static Entry shortOrLong(ByteOrder order, int tag, int v) {
    return v < 65536 ? new Entry(tag, 3, 1, shorts(order, v)) : new Entry(tag, 4, 1, longs(order, v));
  }

  private static byte[] fieldBytes(ByteOrder order, TIFFField f) {
    int type = f.getType();
    int count = f.getCount();
    ByteBuffer bb;
    switch (type) {
    case 1:
    case 6:
    case 7:
      return f.getAsBytes().clone();
    case 2: {
      ByteArrayOutputStream o = new ByteArrayOutputStream();
      for (int i = 0; i < count; i++) {
        byte[] s = f.getAsString(i).getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        o.write(s, 0, s.length);
        o.write(0);
      }
      return o.toByteArray();
    }
    case 3:
      bb = ByteBuffer.allocate(count * 2).order(order);
      for (char c : f.getAsChars()) {
        bb.putShort((short) c);
      }
      return bb.array();
    case 8:
      bb = ByteBuffer.allocate(count * 2).order(order);
      for (short c : (short[]) f.getData()) {
        bb.putShort(c);
      }
      return bb.array();
    case 4:
    case 13:
      return longs(order, f.getAsLongs());
    case 9:
      bb = ByteBuffer.allocate(count * 4).order(order);
      for (int c : (int[]) f.getData()) {
        bb.putInt(c);
      }
      return bb.array();
    case 5:
      bb = ByteBuffer.allocate(count * 8).order(order);
      for (long[] r : f.getAsRationals()) {
        bb.putInt((int) r[0]).putInt((int) r[1]);
      }
      return bb.array();
    case 10:
      bb = ByteBuffer.allocate(count * 8).order(order);
      for (int[] r : f.getAsSRationals()) {
        bb.putInt(r[0]).putInt(r[1]);
      }
      return bb.array();
    case 11:
      bb = ByteBuffer.allocate(count * 4).order(order);
      for (float c : f.getAsFloats()) {
        bb.putFloat(c);
      }
      return bb.array();
    default:
      bb = ByteBuffer.allocate(count * 8).order(order);
      for (double c : f.getAsDoubles()) {
        bb.putDouble(c);
      }
      return bb.array();
    }
  }

  private static int fieldCount(TIFFField f) {
    if (f.getType() == 2) {
      int n = 0;
      for (int i = 0; i < f.getCount(); i++) {
        n += f.getAsString(i).length() + 1;
      }
      return n;
    }
    return f.getCount();
  }

  private byte[] chunk(Raster r, Layout l, int x0, int y0, int cw, int ch, int w, int h, RenderedImage ri,
      ByteOrder order, int predictor) {
    int bytesPerSample = l.bits == 16 ? 2 : 1;
    int rowBytes = (cw * l.bands * l.bits + 7) / 8;
    byte[] out = new byte[rowBytes * ch];
    int rx = r.getMinX();
    int ry = r.getMinY();
    int[] samples = new int[cw];
    for (int row = 0; row < ch; row++) {
      int y = y0 + row;
      if (y >= h) {
        break;
      }
      int avail = Math.min(cw, w - x0);
      int base = row * rowBytes;
      if (l.viaRgb) {
        BufferedImage bi = (BufferedImage) ri;
        for (int x = 0; x < avail; x++) {
          int argb = bi.getRGB(x0 + x, y);
          int p = base + x * l.bands;
          out[p] = (byte) (argb >> 16);
          out[p + 1] = (byte) (argb >> 8);
          out[p + 2] = (byte) argb;
          if (l.bands == 4) {
            out[p + 3] = (byte) (argb >>> 24);
          }
        }
        continue;
      }
      for (int b = 0; b < l.bands; b++) {
        r.getSamples(rx + x0, ry + y, avail, 1, b, samples);
        if (l.bits < 8) {
          for (int x = 0; x < avail; x++) {
            int bit = x * l.bits;
            out[base + (bit >> 3)] |= (byte) (samples[x] << (8 - l.bits - (bit & 7)));
          }
        } else if (bytesPerSample == 1) {
          for (int x = 0; x < avail; x++) {
            out[base + x * l.bands + b] = (byte) samples[x];
          }
        } else {
          for (int x = 0; x < avail; x++) {
            int p = base + (x * l.bands + b) * 2;
            if (order == ByteOrder.BIG_ENDIAN) {
              out[p] = (byte) (samples[x] >> 8);
              out[p + 1] = (byte) samples[x];
            } else {
              out[p] = (byte) samples[x];
              out[p + 1] = (byte) (samples[x] >> 8);
            }
          }
        }
      }
    }
    if (predictor == 2 && l.bits >= 8) {
      for (int row = 0; row < ch; row++) {
        int base = row * rowBytes;
        if (bytesPerSample == 1) {
          for (int i = cw * l.bands - 1; i >= l.bands; i--) {
            out[base + i] -= out[base + i - l.bands];
          }
        } else {
          for (int i = cw * l.bands - 1; i >= l.bands; i--) {
            int a = sample16(out, base + i * 2, order);
            int b = sample16(out, base + (i - l.bands) * 2, order);
            put16(out, base + i * 2, a - b, order);
          }
        }
      }
    }
    return out;
  }

  private static int[] fill(int n, int v) {
    int[] a = new int[n];
    Arrays.fill(a, v);
    return a;
  }

  private static int sample16(byte[] b, int p, ByteOrder order) {
    return order == ByteOrder.BIG_ENDIAN ? ((b[p] & 0xff) << 8) | (b[p + 1] & 0xff)
        : ((b[p + 1] & 0xff) << 8) | (b[p] & 0xff);
  }

  private static void put16(byte[] b, int p, int v, ByteOrder order) {
    if (order == ByteOrder.BIG_ENDIAN) {
      b[p] = (byte) (v >> 8);
      b[p + 1] = (byte) v;
    } else {
      b[p] = (byte) v;
      b[p + 1] = (byte) (v >> 8);
    }
  }

  private static byte[] jpegChunk(byte[] raw, int width, int rows, int bands, float quality) {
    BufferedImage img = new BufferedImage(width, rows, bands == 1 ? BufferedImage.TYPE_BYTE_GRAY
                                                                  : BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < rows; y++) {
      for (int x = 0; x < width; x++) {
        int p = (y * width + x) * bands;
        if (bands == 1) {
          img.getRaster().setSample(x, y, 0, raw[p] & 0xff);
        } else {
          img.setRGB(x, y, 0xff000000 | (raw[p] & 0xff) << 16 | (raw[p + 1] & 0xff) << 8 | (raw[p + 2] & 0xff));
        }
      }
    }
    try {
      javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpeg").next();
      ImageWriteParam jp = writer.getDefaultWriteParam();
      jp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      jp.setCompressionQuality(quality);
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ImageOutputStream out = javax.imageio.ImageIO.createImageOutputStream(bytes);
      writer.setOutput(out);
      writer.write(null, new IIOImage(img, null, null), jp);
      out.close();
      writer.dispose();
      return bytes.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private byte[] compress(byte[] raw, int compression, int rowBytes, int level, int widthPx, boolean twoD) {
    switch (compression) {
    case 2:
    case 3:
    case 4:
      return TiffFax.encode(raw, rowBytes, widthPx, raw.length / rowBytes, compression, twoD);
    case 5:
      return TiffCodec.lzwEncode(raw, 0, raw.length);
    case 8:
    case 32946:
      return TiffCodec.deflate(raw, 0, raw.length, level);
    case 32773: {
      ByteArrayOutputStream o = new ByteArrayOutputStream();
      for (int p = 0; p < raw.length; p += rowBytes) {
        byte[] row = TiffCodec.packBitsEncode(raw, p, Math.min(rowBytes, raw.length - p));
        o.write(row, 0, row.length);
      }
      return o.toByteArray();
    }
    default:
      return raw;
    }
  }

  public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
    if (image == null) {
      throw new IllegalArgumentException("image == null!");
    }
    ImageOutputStream out = stream();
    if (param != null && param.canWriteCompressed() && param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT
        && "Exif JPEG".equals(param.getCompressionType())) {
      writeExifJpeg(out, image);
      return;
    }
    if (!sequence) {
      pendingNextPointer = -1;
      writeHeader(out);
    }
    long ifdPos = sequence ? align(out.getStreamPosition(), 2) : 8;
    if (sequence && ifdPos != out.getStreamPosition()) {
      out.write(0);
    }
    processImageStarted(0);
    writeImage(out, image, param, ifdPos);
    out.flush();
    processImageProgress(100f);
    processImageComplete();
  }

  private void writeExifJpeg(ImageOutputStream out, IIOImage image) throws IOException {
    BufferedImage bi = shim.awt.ImageSupport.toBuffered(image.getRenderedImage());
    java.awt.image.ColorModel cm = bi.getColorModel();
    if (cm.getNumComponents() != 3 || cm.hasAlpha() || cm.getComponentSize(0) != 8 || cm.getComponentSize(1) != 8
        || cm.getComponentSize(2) != 8) {
      throw new IIOException("Old JPEG compression not supported!");
    }
    processImageStarted(0);
    byte[] jpeg = JpegWriter.encodeWithoutJfif(bi);
    ByteOrder order = out.getByteOrder();
    ByteBuffer bb = ByteBuffer.allocate(104 + jpeg.length).order(order);
    bb.put((byte) (order == ByteOrder.BIG_ENDIAN ? 'M' : 'I')).put((byte) (order == ByteOrder.BIG_ENDIAN ? 'M' : 'I'));
    bb.putShort((short) 42).putInt(8).putShort((short) 6);
    int[][] entries = {{259, 3, 6}, {282, 5, 88}, {283, 5, 96}, {296, 3, 1}, {513, 4, 104}, {514, 4, jpeg.length}};
    for (int[] e : entries) {
      bb.putShort((short) e[0]).putShort((short) e[1]).putInt(1);
      if (e[1] == 3) {
        bb.putShort((short) e[2]).putShort((short) 0);
      } else {
        bb.putInt(e[2]);
      }
    }
    bb.putInt(0).putShort((short) 0);
    bb.putInt(1).putInt(1).putInt(1).putInt(1);
    bb.put(jpeg);
    out.write(bb.array());
    out.flush();
    processImageProgress(100f);
    processImageComplete();
  }

  private void writeHeader(ImageOutputStream out) throws IOException {
    ByteOrder order = out.getByteOrder();
    out.write(order == ByteOrder.BIG_ENDIAN ? 'M' : 'I');
    out.write(order == ByteOrder.BIG_ENDIAN ? 'M' : 'I');
    out.writeShort(42);
    out.writeInt(8);
  }

  private static long align(long v, int a) {
    return (v + a - 1) / a * a;
  }

  private void writeImage(ImageOutputStream out, IIOImage image, ImageWriteParam param, long ifdPos)
      throws IOException {
    ByteOrder order = out.getByteOrder();
    RenderedImage ri = image.hasRaster() ? null : image.getRenderedImage();
    Raster raster;
    ColorModel cm;
    SampleModel sm;
    int w;
    int h;
    if (ri != null) {
      raster = ri.getData();
      cm = ri.getColorModel();
      sm = ri.getSampleModel();
      w = ri.getWidth();
      h = ri.getHeight();
    } else {
      raster = image.getRaster();
      sm = raster.getSampleModel();
      w = raster.getWidth();
      h = raster.getHeight();
      cm = null;
    }
    Layout l;
    if (cm == null) {
      l = new Layout();
      l.bands = sm.getNumBands();
      l.bits = sm.getSampleSize(0);
      l.photometric = l.bands >= 3 ? 2 : 1;
    } else {
      l = layout(cm, sm);
    }
    if (l.viaRgb && !(ri instanceof BufferedImage)) {
      BufferedImage bi = new BufferedImage(cm, cm.createCompatibleWritableRaster(w, h), cm.isAlphaPremultiplied(),
          null);
      bi.getRaster().setRect(-raster.getMinX(), -raster.getMinY(), raster);
      ri = bi;
    }
    TIFFDirectory meta = null;
    if (image.getMetadata() instanceof TIFFImageMetadata) {
      meta = ((TIFFImageMetadata) image.getMetadata()).getRootIFD();
    }
    int compression = compressionTag(param, meta);
    int predictor = meta != null && meta.containsTIFFField(317) ? meta.getTIFFField(317).getAsInt(0) : 1;
    if (compression != 5 && compression != 8 && compression != 32946) {
      predictor = 1;
    }
    boolean fax = compression >= 2 && compression <= 4;
    if (fax && (l.bits != 1 || l.bands != 1 || l.photometric > 1)) {
      String[] names = {"RLE", "T4", "T6"};
      throw new IIOException("Bits per sample must be 1 for " + names[compression - 2] + " compression!");
    }
    boolean t4TwoD = compression != 3 || meta == null || !meta.containsTIFFField(292)
        || (meta.getTIFFField(292).getAsInt(0) & 1) != 0;
    boolean jpeg = compression == 7;
    if (jpeg && (l.bits != 8 || l.indexed || (l.bands != 1 && l.bands != 3))) {
      throw new IIOException("JPEG compression supported for 1- and 3-band byte images only!");
    }
    float quality = param != null && param.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT
        ? param.getCompressionQuality() : jpeg ? 0.75f : 1.0f;
    int level = Math.max(1, Math.min(9, 1 + (int) (8 * quality)));
    boolean tiled = param != null && param.canWriteTiles() && param.getTilingMode() == ImageWriteParam.MODE_EXPLICIT
        && param.getTileWidth() > 0;
    int tw = tiled ? param.getTileWidth() : w;
    int th = tiled ? param.getTileHeight() : 0;
    int rowBytesFull = (w * l.bands * l.bits + 7) / 8;
    if (!tiled) {
      boolean fromMeta = meta != null && meta.containsTIFFField(278)
          && (param == null || param.getTilingMode() == ImageWriteParam.MODE_COPY_FROM_METADATA);
      th = fromMeta ? Math.max(1, Math.min(h, meta.getTIFFField(278).getAsInt(0)))
          : Math.min(h, Math.max(1, 8192 / Math.max(1, rowBytesFull)));
      if (jpeg) {
        th = Math.min(h, (th + 15) / 16 * 16);
      }
    }
    int across = (w + tw - 1) / tw;
    int down = (h + th - 1) / th;
    int n = across * down;
    byte[][] chunks = new byte[n][];
    for (int ty = 0; ty < down; ty++) {
      for (int tx = 0; tx < across; tx++) {
        int x0 = tx * tw;
        int y0 = ty * th;
        int cw = tiled ? tw : w;
        int ch = tiled ? th : Math.min(th, h - y0);
        byte[] raw = chunk(raster, l, x0, y0, cw, ch, w, h, ri, order, predictor);
        if (fax && l.photometric == 1) {
          for (int i = 0; i < raw.length; i++) {
            raw[i] = (byte) ~raw[i];
          }
        }
        chunks[ty * across + tx] = jpeg ? jpegChunk(raw, cw, ch, l.bands, quality)
            : compress(raw, compression, (cw * l.bands * l.bits + 7) / 8, level, cw, t4TwoD);
      }
    }
    List<Entry> entries = new ArrayList<>();
    entries.add(shortOrLong(order, 256, w));
    entries.add(shortOrLong(order, 257, h));
    if (!(l.bits == 1 && l.bands == 1)) {
      int[] bps = new int[l.bands];
      Arrays.fill(bps, l.bits);
      entries.add(new Entry(258, 3, l.bands, shorts(order, bps)));
    }
    entries.add(new Entry(259, 3, 1, shorts(order, compression)));
    entries.add(new Entry(262, 3, 1, shorts(order, fax ? 0 : jpeg && l.bands == 3 ? 6 : l.photometric)));
    if (jpeg && l.bands == 3) {
      entries.add(new Entry(530, 3, 2, shorts(order, 2, 2)));
      entries.add(new Entry(531, 3, 1, shorts(order, 1)));
      entries.add(new Entry(532, 5, 6, longs(order, 0, 1, 255, 1, 128, 1, 255, 1, 128, 1, 255, 1)));
    }
    if (compression == 3) {
      entries.add(new Entry(292, 4, 1, longs(order, t4TwoD ? 1 : 0)));
    }
    entries.add(new Entry(tiled ? 324 : 273, 4, n, new byte[n * 4]));
    entries.add(new Entry(277, 3, 1, shorts(order, l.bands)));
    if (tiled) {
      entries.add(shortOrLong(order, 322, tw));
      entries.add(shortOrLong(order, 323, th));
    } else {
      entries.add(shortOrLong(order, 278, th));
    }
    long[] counts = new long[n];
    for (int i = 0; i < n; i++) {
      counts[i] = chunks[i].length;
    }
    entries.add(new Entry(tiled ? 325 : 279, 4, n, longs(order, counts)));
    if (meta == null || !meta.containsTIFFField(282)) {
      entries.add(new Entry(282, 5, 1, longs(order, 1, 1)));
    }
    if (meta == null || !meta.containsTIFFField(283)) {
      entries.add(new Entry(283, 5, 1, longs(order, 1, 1)));
    }
    if (meta == null || !meta.containsTIFFField(296)) {
      entries.add(new Entry(296, 3, 1, shorts(order, 1)));
    }
    if (predictor != 1) {
      entries.add(new Entry(317, 3, 1, shorts(order, predictor)));
    }
    if (l.colormap != null) {
      entries.add(new Entry(320, 3, l.colormap.length, shorts(order, l.colormap)));
    }
    if (l.extra >= 0) {
      entries.add(new Entry(338, 3, 1, shorts(order, l.extra)));
    }
    if (l.bits == 16) {
      entries.add(new Entry(339, 3, l.bands, shorts(order, fill(l.bands, 1))));
    }
    if (meta != null) {
      outer:
      for (TIFFField f : meta.getTIFFFields()) {
        for (int s : kStructural) {
          if (f.getTagNumber() == s) {
            continue outer;
          }
        }
        if (f.getType() == 13) {
          continue;
        }
        entries.add(new Entry(f.getTagNumber(), f.getType(), fieldCount(f), fieldBytes(order, f)));
      }
    }
    entries.sort((a, b) -> Integer.compare(a.tag, b.tag));
    long ifdEnd = ifdPos + 2 + entries.size() * 12L + 4;
    long pos = align(ifdEnd, 4);
    long[] offsets = new long[n];
    long[] extraPos = new long[entries.size()];
    for (int i = 0; i < entries.size(); i++) {
      Entry e = entries.get(i);
      int tagIndex = e.tag;
      if (e.data.length > 4 && tagIndex != 273 && tagIndex != 324) {
        extraPos[i] = pos;
        pos = align(pos + e.data.length, 4);
      } else if ((tagIndex == 273 || tagIndex == 324) && n > 1) {
        extraPos[i] = pos;
        pos = align(pos + n * 4L, 4);
      }
    }
    long dataStart = pos;
    long p = dataStart;
    for (int i = 0; i < n; i++) {
      offsets[i] = p;
      p += chunks[i].length;
    }
    ByteBuffer ifd = ByteBuffer.allocate((int) (pos - ifdPos)).order(order);
    ifd.putShort((short) entries.size());
    for (int i = 0; i < entries.size(); i++) {
      Entry e = entries.get(i);
      ifd.putShort((short) e.tag).putShort((short) e.type).putInt(e.count);
      if (e.tag == 273 || e.tag == 324) {
        if (n == 1) {
          ifd.putInt((int) offsets[0]);
        } else {
          ifd.putInt((int) extraPos[i]);
        }
      } else if (e.data.length > 4) {
        ifd.putInt((int) extraPos[i]);
      } else {
        byte[] v = Arrays.copyOf(e.data, 4);
        ifd.put(v);
      }
    }
    ifd.putInt(0);
    for (int i = 0; i < entries.size(); i++) {
      Entry e = entries.get(i);
      if (e.tag == 273 || e.tag == 324) {
        if (n > 1) {
          ifd.position((int) (extraPos[i] - ifdPos));
          for (long o : offsets) {
            ifd.putInt((int) o);
          }
        }
      } else if (e.data.length > 4) {
        ifd.position((int) (extraPos[i] - ifdPos));
        ifd.put(e.data);
      }
    }
    if (pendingNextPointer >= 0) {
      long save = out.getStreamPosition();
      out.seek(pendingNextPointer);
      out.writeInt((int) ifdPos);
      out.seek(save);
    }
    out.write(ifd.array());
    for (byte[] c : chunks) {
      out.write(c);
    }
    pendingNextPointer = ifdPos + 2 + entries.size() * 12L;
  }

  public void prepareWriteSequence(IIOMetadata streamMetadata) throws IOException {
    sequence = true;
    pendingNextPointer = -1;
    writeHeader(stream());
  }

  public void writeToSequence(IIOImage image, ImageWriteParam param) throws IOException {
    if (!sequence) {
      throw new IllegalStateException("prepareWriteSequence() has not been called!");
    }
    long ifdPos = pendingNextPointer < 0 ? 8 : align(stream().getStreamPosition(), 2);
    if (ifdPos != stream().getStreamPosition()) {
      stream().write(0);
    }
    writeImage(stream(), image, param, ifdPos);
  }

  public void endWriteSequence() throws IOException {
    if (!sequence) {
      throw new IllegalStateException("prepareWriteSequence() has not been called!");
    }
    sequence = false;
    pendingNextPointer = -1;
    stream().flush();
  }

  public void reset() {
    super.reset();
    sequence = false;
    pendingNextPointer = -1;
  }
}
