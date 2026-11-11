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
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public final class GifWriter extends CodecWriter {
  private boolean sequence;
  private boolean headerWritten;
  private GifMetadata streamMeta;
  private byte[] globalTable;

  private static final class Frame {
    int[] palette = new int[256];
    int paletteSize;
    int transparent = -1;
    int sampleBits = 8;
    byte[] idx;
    int w;
    int h;
  }

  public GifWriter(ImageWriterSpi spi) {
    super(spi);
  }

  public ImageWriteParam getDefaultWriteParam() {
    return new com.sun.imageio.plugins.gif.GIFImageWriteParam(getLocale());
  }

  public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
    return GifMetadata.defaultStream(null);
  }

  public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
    return GifMetadata.defaultImage(imageType);
  }

  public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
    return convert(inData, true, null);
  }

  public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
    return convert(inData, false, imageType);
  }

  private static GifMetadata convert(IIOMetadata in, boolean stream, ImageTypeSpecifier type) {
    if (in == null) {
      return null;
    }
    if (in instanceof GifMetadata && ((GifMetadata) in).isStream() == stream) {
      return (GifMetadata) in;
    }
    GifMetadata out = stream ? GifMetadata.defaultStream(type) : GifMetadata.defaultImage(type);
    String[] names = in.getMetadataFormatNames();
    if (names == null) {
      return null;
    }
    for (String name : names) {
      if (name.equals(out.getNativeMetadataFormatName())
          || name.equals(javax.imageio.metadata.IIOMetadataFormatImpl.standardMetadataFormatName)) {
        try {
          out.mergeTree(name, in.getAsTree(name));
          return out;
        } catch (javax.imageio.metadata.IIOInvalidTreeException e) {
          return null;
        }
      }
    }
    return null;
  }

  public boolean canWriteSequence() {
    return true;
  }

  public void prepareWriteSequence(IIOMetadata streamMetadata) throws IOException {
    stream();
    sequence = true;
    headerWritten = false;
    globalTable = null;
    streamMeta = convert(streamMetadata, true, null);
  }

  public void writeToSequence(IIOImage image, ImageWriteParam param) throws IOException {
    if (!sequence) {
      throw new IllegalStateException("prepareWriteSequence() was not invoked!");
    }
    writeFrame(image, param, !headerWritten);
    headerWritten = true;
  }

  public void endWriteSequence() throws IOException {
    if (!sequence) {
      throw new IllegalStateException("prepareWriteSequence() was not invoked!");
    }
    stream().writeByte(0x3b);
    stream().flush();
    sequence = false;
    streamMeta = null;
    globalTable = null;
  }

  public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
    if (sequence) {
      throw new IllegalStateException("sequence is active");
    }
    if (image.hasRaster()) {
      throw new UnsupportedOperationException("Rasters not supported");
    }
    streamMeta = convert(streamMetadata, true, null);
    globalTable = null;
    writeFrame(image, param, true);
    stream().writeByte(0x3b);
    stream().flush();
    streamMeta = null;
    globalTable = null;
  }

  private static Frame convert(BufferedImage bi) {
    Frame f = new Frame();
    int w = bi.getWidth();
    int h = bi.getHeight();
    f.w = w;
    f.h = h;
    int[] palette = f.palette;
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
    } else if (cm instanceof IndexColorModel && bi.getRaster().getNumBands() == 1
        && ((IndexColorModel) cm).getMapSize() <= 256) {
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
      int[] argb = bi.getRGB(0, 0, w, h, null, 0, w);
      Octree tree = Octree.build(argb, w, h, cm.getTransparency() != java.awt.Transparency.OPAQUE);
      System.arraycopy(tree.palette, 0, palette, 0, tree.paletteSize);
      paletteSize = tree.paletteSize;
      transparent = tree.transparentIndex;
      for (int i = 0; i < argb.length; i++) {
        idx[i] = (byte) tree.indexOf(argb[i]);
      }
    }
    f.sampleBits = bi.getSampleModel().getSampleSize(0);
    f.paletteSize = paletteSize;
    f.transparent = transparent;
    f.idx = idx;
    return f;
  }

  private static int bitsFor(int size) {
    int bits = 1;
    while ((1 << bits) < size) {
      bits++;
    }
    return bits;
  }

  private static byte[] tableBytes(Frame f) {
    int bits = bitsFor(f.paletteSize);
    byte[] t = new byte[3 << bits];
    for (int i = f.paletteSize; i < (1 << bits); i++) {
      t[3 * i] = (byte) (f.palette[0] >> 16);
      t[3 * i + 1] = (byte) (f.palette[0] >> 8);
      t[3 * i + 2] = (byte) f.palette[0];
    }
    for (int i = 0; i < f.paletteSize; i++) {
      t[3 * i] = (byte) (f.palette[i] >> 16);
      t[3 * i + 1] = (byte) (f.palette[i] >> 8);
      t[3 * i + 2] = (byte) f.palette[i];
    }
    return t;
  }

  private static boolean samePalette(byte[] global, Frame f) {
    if (global == null || global.length / 3 < f.paletteSize) {
      return false;
    }
    for (int i = 0; i < f.paletteSize; i++) {
      if (global[3 * i] != (byte) (f.palette[i] >> 16) || global[3 * i + 1] != (byte) (f.palette[i] >> 8)
          || global[3 * i + 2] != (byte) f.palette[i]) {
        return false;
      }
    }
    return true;
  }

  private void writeFrame(IIOImage image, ImageWriteParam param, boolean header) throws IOException {
    ImageOutputStream out = stream();
    out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    BufferedImage bi = shim.awt.ImageSupport.toBuffered(image.getRenderedImage());
    Frame f = convert(bi);
    GifMetadata im = convert(image.getMetadata(), false, ImageTypeSpecifier.createFromRenderedImage(bi));
    byte[] frameTable = im != null && im.localTableBytes() != null ? im.localTableBytes() : tableBytes(f);
    processImageStarted(0);
    if (header) {
      GifMetadata sm = streamMeta;
      byte[] gct = sm != null && sm.globalTableBytes() != null ? sm.globalTableBytes() : frameTable;
      globalTable = gct;
      int tableBits = bitsFor(gct.length / 3);
      int screenW = sm != null && sm.screenWidth() > 0 ? sm.screenWidth() : f.w;
      int screenH = sm != null && sm.screenHeight() > 0 ? sm.screenHeight() : f.h;
      int res = sm != null && sm.colorResolutionBits() > 0 ? sm.colorResolutionBits() : 8;
      out.write(new byte[] {'G', 'I', 'F', '8', sm != null && sm.versionName().equals("87a") ? (byte) '7' : (byte) '9',
          'a'});
      out.writeShort(screenW);
      out.writeShort(screenH);
      out.writeByte(0x80 | ((res - 1) << 4) | (sm != null && sm.globalSortFlag() ? 8 : 0) | (tableBits - 1));
      out.writeByte(sm != null ? sm.backgroundColorIndex() : 0);
      out.writeByte(sm != null ? sm.pixelAspect() : 0);
      out.write(gct, 0, gct.length);
    }
    boolean writeGce = im != null ? im.hasGraphicControl() : f.transparent >= 0;
    if (writeGce) {
      boolean transparentFlag = im != null ? im.transparentFlag() : true;
      int transparentIndex = im != null ? im.transparentColorIndex() : f.transparent;
      out.writeByte(0x21);
      out.writeByte(0xf9);
      out.writeByte(4);
      out.writeByte(((im != null ? im.disposalMethod() : 0) << 2) | (im != null && im.userInputFlag() ? 2 : 0)
          | (transparentFlag ? 1 : 0));
      out.writeShort(im != null ? im.delayTime() : 0);
      out.writeByte(transparentFlag ? transparentIndex : (im != null ? im.transparentColorIndex() : 0));
      out.writeByte(0);
    }
    if (im != null) {
      for (Object[] a : im.applicationExtensions()) {
        out.writeByte(0x21);
        out.writeByte(0xff);
        out.writeByte(11);
        out.write(pad((String) a[0], 8));
        out.write(pad((String) a[1], 3));
        writeBlocks(out, (byte[]) a[2]);
      }
      for (String c : im.commentExtensions()) {
        out.writeByte(0x21);
        out.writeByte(0xfe);
        writeBlocks(out, c.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
      }
    }
    boolean interlace = param != null && param.canWriteProgressive()
        ? (param.getProgressiveMode() == ImageWriteParam.MODE_COPY_FROM_METADATA
            ? (im == null || im.interlaceFlag()) : param.getProgressiveMode() != ImageWriteParam.MODE_DISABLED)
        : (im == null || im.interlaceFlag());
    boolean local = im != null && im.localTableBytes() != null
        ? !java.util.Arrays.equals(im.localTableBytes(), globalTable)
        : !samePalette(globalTable, f);
    byte[] table = local ? frameTable : globalTable;
    int bits = bitsFor(table.length / 3);
    out.writeByte(0x2c);
    out.writeShort(im != null ? im.imageLeft() : 0);
    out.writeShort(im != null ? im.imageTop() : 0);
    out.writeShort(f.w);
    out.writeShort(f.h);
    out.writeByte((local ? 0x80 : 0) | (bits - 1) | (interlace ? 0x40 : 0)
        | (local && im != null && im.localSortFlag() ? 0x20 : 0));
    if (local) {
      out.write(frameTable, 0, frameTable.length);
    }
    byte[] idx = interlace ? interlace(f.idx, f.w, f.h) : f.idx;
    int minCode = Math.max(2, Math.max(bits, f.sampleBits));
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

  private static byte[] pad(String s, int n) {
    byte[] b = new byte[n];
    byte[] src = s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
    System.arraycopy(src, 0, b, 0, Math.min(n, src.length));
    return b;
  }

  private static void writeBlocks(ImageOutputStream out, byte[] data) throws IOException {
    int off = 0;
    while (off < data.length) {
      int n = Math.min(255, data.length - off);
      out.writeByte(n);
      out.write(data, off, n);
      off += n;
    }
    out.writeByte(0);
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
