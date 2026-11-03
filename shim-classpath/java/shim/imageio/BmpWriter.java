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
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.ByteOrder;
import javax.imageio.IIOImage;
import javax.imageio.IIOException;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public final class BmpWriter extends CodecWriter {
  public BmpWriter(ImageWriterSpi spi) {
    super(spi);
  }

  public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
    ImageOutputStream out = stream();
    BufferedImage bi = shim.awt.ImageSupport.toBuffered(image.getRenderedImage());
    ColorModel cm = bi.getColorModel();
    if (cm.hasAlpha()) {
      throw new IIOException("Image has alpha, unsupported by BMP");
    }
    int w = bi.getWidth();
    int h = bi.getHeight();
    boolean gray = bi.getType() == BufferedImage.TYPE_BYTE_GRAY;
    boolean indexed = gray || cm instanceof IndexColorModel && ((IndexColorModel) cm).getMapSize() <= 256
        && bi.getRaster().getNumBands() == 1;
    int bits = 24;
    int colors = 0;
    if (indexed) {
      int n = gray ? 256 : ((IndexColorModel) cm).getMapSize();
      bits = n <= 2 ? 1 : n <= 16 ? 4 : 8;
      colors = n;
    }
    int rowBytes = ((w * bits + 31) / 32) * 4;
    int paletteBytes = indexed ? (1 << bits) * 4 : 0;
    int offset = 14 + 40 + paletteBytes;
    int fileSize = offset + rowBytes * h;
    out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    processImageStarted(0);
    out.writeByte('B');
    out.writeByte('M');
    out.writeInt(fileSize);
    out.writeInt(0);
    out.writeInt(offset);
    out.writeInt(40);
    out.writeInt(w);
    out.writeInt(h);
    out.writeShort(1);
    out.writeShort(bits);
    out.writeInt(0);
    out.writeInt(0);
    out.writeInt(0);
    out.writeInt(0);
    out.writeInt(0);
    out.writeInt(indexed ? 1 << bits : 0);
    if (indexed) {
      IndexColorModel icm = gray ? null : (IndexColorModel) cm;
      for (int i = 0; i < (1 << bits); i++) {
        if (gray) {
          out.writeByte(i);
          out.writeByte(i);
          out.writeByte(i);
        } else if (i < colors) {
          out.writeByte(icm.getBlue(i));
          out.writeByte(icm.getGreen(i));
          out.writeByte(icm.getRed(i));
        } else {
          out.writeByte(0);
          out.writeByte(0);
          out.writeByte(0);
        }
        out.writeByte(gray || i < colors ? 0xff : 0);
      }
    }
    byte[] row = new byte[rowBytes];
    int[] px = new int[1];
    for (int y = h - 1; y >= 0; y--) {
      java.util.Arrays.fill(row, (byte) 0);
      for (int x = 0; x < w; x++) {
        if (indexed) {
          bi.getRaster().getPixel(x, y, px);
          int v = px[0];
          if (bits == 8) {
            row[x] = (byte) v;
          } else {
            int bit = x * bits;
            row[bit >> 3] |= (byte) (v << (8 - bits - (bit & 7)));
          }
        } else {
          int rgb = bi.getRGB(x, y);
          row[x * 3] = (byte) rgb;
          row[x * 3 + 1] = (byte) (rgb >> 8);
          row[x * 3 + 2] = (byte) (rgb >> 16);
        }
      }
      out.write(row);
    }
    out.flush();
    processImageProgress(100f);
    processImageComplete();
  }
}
