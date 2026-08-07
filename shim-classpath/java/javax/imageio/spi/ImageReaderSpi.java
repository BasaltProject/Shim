/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.spi;

import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public abstract class ImageReaderSpi extends ImageReaderWriterSpi {
  public static final Class<?>[] STANDARD_INPUT_TYPE = {ImageInputStream.class};

  protected Class<?>[] inputTypes = null;
  protected String[] writerSpiNames = null;

  protected ImageReaderSpi() { }

  public ImageReaderSpi(String vendorName, String version, String[] names, String[] suffixes, String[] MIMETypes,
      String readerClassName, Class<?>[] inputTypes, String[] writerSpiNames,
      boolean supportsStandardStreamMetadataFormat, String nativeStreamMetadataFormatName,
      String nativeStreamMetadataFormatClassName, String[] extraStreamMetadataFormatNames,
      String[] extraStreamMetadataFormatClassNames, boolean supportsStandardImageMetadataFormat,
      String nativeImageMetadataFormatName, String nativeImageMetadataFormatClassName,
      String[] extraImageMetadataFormatNames, String[] extraImageMetadataFormatClassNames) {
    super(vendorName, version, names, suffixes, MIMETypes, readerClassName, supportsStandardStreamMetadataFormat,
        nativeStreamMetadataFormatName, nativeStreamMetadataFormatClassName, extraStreamMetadataFormatNames,
        extraStreamMetadataFormatClassNames, supportsStandardImageMetadataFormat, nativeImageMetadataFormatName,
        nativeImageMetadataFormatClassName, extraImageMetadataFormatNames, extraImageMetadataFormatClassNames);
    if (inputTypes == null) {
      throw new IllegalArgumentException("inputTypes == null!");
    }
    if (inputTypes.length == 0) {
      throw new IllegalArgumentException("inputTypes.length == 0!");
    }
    this.inputTypes = (inputTypes == STANDARD_INPUT_TYPE) ? STANDARD_INPUT_TYPE : inputTypes.clone();
    if (writerSpiNames != null && writerSpiNames.length > 0) {
      this.writerSpiNames = writerSpiNames.clone();
    }
  }

  public Class<?>[] getInputTypes() { return inputTypes.clone(); }

  public abstract boolean canDecodeInput(Object source) throws IOException;

  public ImageReader createReaderInstance() throws IOException {
    return createReaderInstance(null);
  }

  public abstract ImageReader createReaderInstance(Object extension) throws IOException;

  public boolean isOwnReader(ImageReader reader) {
    if (reader == null) {
      throw new IllegalArgumentException("reader == null!");
    }
    return reader.getOriginatingProvider() == this;
  }

  public String[] getImageWriterSpiNames() { return writerSpiNames == null ? null : writerSpiNames.clone(); }
}
