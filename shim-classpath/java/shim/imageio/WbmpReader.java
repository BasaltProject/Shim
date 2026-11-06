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
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public final class WbmpReader extends CodecReader {
  public WbmpReader(ImageReaderSpi spi) {
    super(spi);
  }

  public javax.imageio.metadata.IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    checkIndex(imageIndex);
    return new WbmpMetadata(getWidth(imageIndex), getHeight(imageIndex));
  }

  protected int imageCount() throws IOException {
    return 1;
  }

  static int readMultiByte(ImageInputStream in) throws IOException {
    int value = 0;
    for (int i = 0; i < 5; i++) {
      int b = in.readUnsignedByte();
      value = (value << 7) | (b & 0x7f);
      if ((b & 0x80) == 0) {
        return value;
      }
    }
    throw new IIOException("Invalid WBMP header");
  }

  protected BufferedImage decode(int index) throws IOException {
    ImageInputStream in = stream();
    in.seek(0);
    int type = in.readUnsignedByte();
    int fixed = in.readUnsignedByte();
    if (type != 0 || fixed != 0) {
      throw new IIOException("Unsupported WBMP type");
    }
    int w = readMultiByte(in);
    int h = readMultiByte(in);
    if (w <= 0 || h <= 0) {
      throw new IIOException("Invalid WBMP dimensions");
    }
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
    byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
    in.readFully(data, 0, data.length);
    return bi;
  }
}
