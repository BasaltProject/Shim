/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.io.IOException;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public abstract class CodecWriter extends ImageWriter {
  protected CodecWriter(ImageWriterSpi spi) {
    super(spi);
  }

  protected ImageOutputStream stream() {
    if (output == null) {
      throw new IllegalStateException("Output has not been set!");
    }
    return (ImageOutputStream) output;
  }

  public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
    return null;
  }

  public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
    return null;
  }

  public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
    return null;
  }

  public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
    return null;
  }
}
