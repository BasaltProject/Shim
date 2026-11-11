/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.imageio;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;

public final class TiffSpi {
  private TiffSpi() { }

  public static final class Reader extends ImageReaderSpi {
    public Reader() {
      super("Oracle Corporation", "1.0", new String[] {"tif", "TIF", "tiff", "TIFF"}, new String[] {"tif", "tiff"},
          new String[] {"image/tiff"}, "com.sun.imageio.plugins.tiff.TIFFImageReader", STANDARD_INPUT_TYPE,
          new String[] {"com.sun.imageio.plugins.tiff.TIFFImageWriterSpi"}, false, "javax_imageio_tiff_stream_1.0", null, null, null, true, "javax_imageio_tiff_image_1.0", null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard TIFF image reader";
    }

    public boolean canDecodeInput(Object source) throws IOException {
      if (!(source instanceof ImageInputStream)) {
        return false;
      }
      ImageInputStream in = (ImageInputStream) source;
      byte[] b = new byte[4];
      in.mark();
      try {
        in.readFully(b);
      } catch (java.io.EOFException e) {
        return false;
      } finally {
        in.reset();
      }
      return (b[0] == 'I' && b[1] == 'I' && b[2] == 42 && b[3] == 0)
          || (b[0] == 'M' && b[1] == 'M' && b[2] == 0 && b[3] == 42);
    }

    public ImageReader createReaderInstance(Object extension) throws IOException {
      return new TiffReader(this);
    }
  }

  public static final class Writer extends ImageWriterSpi {
    public Writer() {
      super("Oracle Corporation", "1.0", new String[] {"tif", "TIF", "tiff", "TIFF"}, new String[] {"tif", "tiff"},
          new String[] {"image/tiff"}, "com.sun.imageio.plugins.tiff.TIFFImageWriter", STANDARD_OUTPUT_TYPE,
          new String[] {"com.sun.imageio.plugins.tiff.TIFFImageReaderSpi"}, false, "javax_imageio_tiff_stream_1.0", null, null, null, true, "javax_imageio_tiff_image_1.0", null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard TIFF image writer";
    }

    public boolean isFormatLossless() {
      return true;
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
      return true;
    }

    public ImageWriter createWriterInstance(Object extension) throws IOException {
      return new TiffWriter(this);
    }
  }
}
