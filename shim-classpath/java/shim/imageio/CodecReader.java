/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public abstract class CodecReader extends ImageReader {
  private final List<BufferedImage> cache = new ArrayList<>();

  protected CodecReader(ImageReaderSpi spi) {
    super(spi);
  }

  protected ImageInputStream stream() throws IOException {
    if (input == null) {
      throw new IllegalStateException("Input not set!");
    }
    return (ImageInputStream) input;
  }

  protected abstract int imageCount() throws IOException;

  protected abstract BufferedImage decode(int index) throws IOException;

  protected void checkIndex(int imageIndex) throws IOException {
    if (imageIndex < minIndex) {
      throw new IndexOutOfBoundsException("imageIndex < minIndex!");
    }
    if (imageIndex >= imageCount()) {
      throw new IndexOutOfBoundsException("imageIndex >= number of images");
    }
  }

  private BufferedImage raw(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    while (cache.size() <= imageIndex) {
      cache.add(null);
    }
    BufferedImage bi = cache.get(imageIndex);
    if (bi == null) {
      bi = decode(imageIndex);
      cache.set(imageIndex, bi);
    }
    return bi;
  }

  public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
    super.setInput(input, seekForwardOnly, ignoreMetadata);
    cache.clear();
    resetState();
  }

  protected void resetState() { }

  public int getNumImages(boolean allowSearch) throws IOException {
    return imageCount();
  }

  public int getWidth(int imageIndex) throws IOException {
    return raw(imageIndex).getWidth();
  }

  public int getHeight(int imageIndex) throws IOException {
    return raw(imageIndex).getHeight();
  }

  public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
    BufferedImage bi = raw(imageIndex);
    List<ImageTypeSpecifier> list = new ArrayList<>();
    list.add(new ImageTypeSpecifier(bi.getColorModel(), bi.getSampleModel()));
    if (bi.getType() == BufferedImage.TYPE_BYTE_INDEXED || bi.getType() == BufferedImage.TYPE_BYTE_BINARY
        || bi.getType() == BufferedImage.TYPE_BYTE_GRAY || bi.getType() == BufferedImage.TYPE_USHORT_GRAY) {
      list.add(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB));
      list.add(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB));
    } else {
      list.add(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB));
    }
    return list.iterator();
  }

  public IIOMetadata getStreamMetadata() throws IOException {
    return null;
  }

  protected String[] nativeFormat() { return null; }

  public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    String[] nf = nativeFormat();
    if (nf == null) {
      return null;
    }
    return new ImageInfoMetadata(raw(imageIndex), nf[0], nf[1], Boolean.parseBoolean(nf[2]));
  }

  public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
    BufferedImage src = raw(imageIndex);
    processImageStarted(imageIndex);
    if (param == null) {
      processImageProgress(100f);
      processImageComplete();
      return copyOf(src);
    }
    Rectangle srcRegion = new Rectangle();
    Rectangle dstRegion = new Rectangle();
    BufferedImage dest = param.getDestination();
    computeRegions(param, src.getWidth(), src.getHeight(), dest, srcRegion, dstRegion);
    if (dest == null) {
      dest = getDestination(param, getImageTypes(imageIndex), src.getWidth(), src.getHeight());
    }
    int xs = param.getSourceXSubsampling();
    int ys = param.getSourceYSubsampling();
    boolean sameModel = dest.getType() == src.getType() && dest.getColorModel().equals(src.getColorModel());
    for (int dy = 0; dy < dstRegion.height; dy++) {
      int sy = srcRegion.y + dy * ys;
      for (int dx = 0; dx < dstRegion.width; dx++) {
        int sx = srcRegion.x + dx * xs;
        if (sameModel) {
          dest.getRaster().setDataElements(dstRegion.x + dx, dstRegion.y + dy,
              src.getRaster().getDataElements(sx, sy, null));
        } else {
          dest.setRGB(dstRegion.x + dx, dstRegion.y + dy, src.getRGB(sx, sy));
        }
      }
    }
    processImageProgress(100f);
    processImageComplete();
    return dest;
  }

  private static BufferedImage copyOf(BufferedImage src) {
    WritableRaster r = src.getRaster().createCompatibleWritableRaster();
    r.setRect(src.getRaster());
    return new BufferedImage(src.getColorModel(), r, src.isAlphaPremultiplied(), null);
  }

  public void dispose() {
    cache.clear();
  }

  protected IIOException bad(String message) {
    return new IIOException(message);
  }
}
