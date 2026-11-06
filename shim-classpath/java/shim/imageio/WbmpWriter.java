/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public final class WbmpWriter extends CodecWriter {
  public WbmpWriter(ImageWriterSpi spi) {
    super(spi);
  }

  public IIOMetadata getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier type, ImageWriteParam param) {
    return new WbmpMetadata(0, 0);
  }

  public boolean canWriteRasters() {
    return true;
  }

  static boolean bilevel(SampleModel sm) {
    int t = sm.getDataType();
    return t >= 0 && t <= 3 && sm.getNumBands() == 1 && sm.getSampleSize(0) == 1;
  }

  private static void multiByte(ImageOutputStream out, int v) throws IOException {
    int n = 1;
    while ((v >>> (7 * n)) != 0) {
      n++;
    }
    for (int i = n - 1; i >= 0; i--) {
      int b = (v >>> (7 * i)) & 0x7f;
      out.write(i > 0 ? b | 0x80 : b);
    }
  }

  public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
    if (image == null) {
      throw new IllegalArgumentException("image == null!");
    }
    ImageOutputStream out = stream();
    RenderedImage ri = image.hasRaster() ? null : image.getRenderedImage();
    Raster source = image.hasRaster() ? image.getRaster() : null;
    SampleModel model = ri != null ? ri.getSampleModel() : source.getSampleModel();
    if (!bilevel(model)) {
      throw new IllegalArgumentException("Only integral single-band bilevel image is supported.");
    }
    processImageStarted(0);
    int w = ri != null ? ri.getWidth() : source.getWidth();
    int h = ri != null ? ri.getHeight() : source.getHeight();
    int rowBytes = (w + 7) / 8;
    byte[] rows = new byte[rowBytes * h];
    SampleModel sm = model;
    if (ri instanceof BufferedImage && sm instanceof MultiPixelPackedSampleModel
        && ((MultiPixelPackedSampleModel) sm).getScanlineStride() == rowBytes
        && ((BufferedImage) ri).getRaster().getDataBuffer() instanceof DataBufferByte
        && ((BufferedImage) ri).getRaster().getSampleModelTranslateX() == 0
        && ((BufferedImage) ri).getRaster().getSampleModelTranslateY() == 0) {
      byte[] src = ((DataBufferByte) ((BufferedImage) ri).getRaster().getDataBuffer()).getData();
      System.arraycopy(src, 0, rows, 0, rows.length);
    } else {
      Raster r = ri != null ? ri.getData() : source;
      int[] px = new int[w];
      for (int y = 0; y < h; y++) {
        r.getSamples(r.getMinX(), r.getMinY() + y, w, 1, 0, px);
        for (int x = 0; x < w; x++) {
          if (px[x] != 0) {
            rows[y * rowBytes + (x >> 3)] |= (byte) (0x80 >> (x & 7));
          }
        }
      }
    }
    boolean invert = false;
    if (ri != null && ri.getColorModel() instanceof IndexColorModel) {
      IndexColorModel icm = (IndexColorModel) ri.getColorModel();
      invert = icm.getRed(0) > icm.getRed(1);
    }
    if (invert) {
      for (int i = 0; i < rows.length; i++) {
        rows[i] = (byte) ~rows[i];
      }
    }
    out.write(0);
    out.write(0);
    multiByte(out, w);
    multiByte(out, h);
    out.write(rows, 0, rows.length);
    processImageProgress(100f);
    processImageComplete();
    out.flushBefore(out.getStreamPosition());
  }
}
