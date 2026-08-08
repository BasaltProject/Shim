/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.spi;

import java.awt.image.RenderedImage;
import java.io.IOException;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public abstract class ImageWriterSpi extends ImageReaderWriterSpi {
  public static final Class<?>[] STANDARD_OUTPUT_TYPE = {ImageOutputStream.class};

  protected Class<?>[] outputTypes = null;
  protected String[] readerSpiNames = null;

  protected ImageWriterSpi() { }

  public ImageWriterSpi(String vendorName, String version, String[] names, String[] suffixes, String[] MIMETypes,
      String writerClassName, Class<?>[] outputTypes, String[] readerSpiNames,
      boolean supportsStandardStreamMetadataFormat, String nativeStreamMetadataFormatName,
      String nativeStreamMetadataFormatClassName, String[] extraStreamMetadataFormatNames,
      String[] extraStreamMetadataFormatClassNames, boolean supportsStandardImageMetadataFormat,
      String nativeImageMetadataFormatName, String nativeImageMetadataFormatClassName,
      String[] extraImageMetadataFormatNames, String[] extraImageMetadataFormatClassNames) {
    super(vendorName, version, names, suffixes, MIMETypes, writerClassName, supportsStandardStreamMetadataFormat,
        nativeStreamMetadataFormatName, nativeStreamMetadataFormatClassName, extraStreamMetadataFormatNames,
        extraStreamMetadataFormatClassNames, supportsStandardImageMetadataFormat, nativeImageMetadataFormatName,
        nativeImageMetadataFormatClassName, extraImageMetadataFormatNames, extraImageMetadataFormatClassNames);
    if (outputTypes == null) {
      throw new IllegalArgumentException("outputTypes == null!");
    }
    if (outputTypes.length == 0) {
      throw new IllegalArgumentException("outputTypes.length == 0!");
    }
    this.outputTypes = (outputTypes == STANDARD_OUTPUT_TYPE) ? STANDARD_OUTPUT_TYPE : outputTypes.clone();
    if (readerSpiNames != null && readerSpiNames.length > 0) {
      this.readerSpiNames = readerSpiNames.clone();
    }
  }

  public boolean isFormatLossless() { return true; }

  public Class<?>[] getOutputTypes() { return outputTypes.clone(); }

  public abstract boolean canEncodeImage(ImageTypeSpecifier type);

  public boolean canEncodeImage(RenderedImage im) {
    return canEncodeImage(ImageTypeSpecifier.createFromRenderedImage(im));
  }

  public ImageWriter createWriterInstance() throws IOException {
    return createWriterInstance(null);
  }

  public abstract ImageWriter createWriterInstance(Object extension) throws IOException;

  public boolean isOwnWriter(ImageWriter writer) {
    if (writer == null) {
      throw new IllegalArgumentException("writer == null!");
    }
    return writer.getOriginatingProvider() == this;
  }

  public String[] getImageReaderSpiNames() { return readerSpiNames == null ? null : readerSpiNames.clone(); }
}
