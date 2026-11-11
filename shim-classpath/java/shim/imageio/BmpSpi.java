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

public final class BmpSpi {
  private BmpSpi() { }

  public static final class Reader extends ImageReaderSpi {
    public Reader() {
      super("Oracle Corporation", "1.0", new String[] {"bmp", "BMP"}, new String[] {"bmp"},
          new String[] {"image/bmp"}, "com.sun.imageio.plugins.bmp.BMPImageReader", STANDARD_INPUT_TYPE,
          new String[] {"com.sun.imageio.plugins.bmp.BMPImageWriterSpi"}, false, null, null, null, null, true, "javax_imageio_bmp_1.0", null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard BMP Image Reader";
    }

    public boolean canDecodeInput(Object source) throws IOException {
      if (!(source instanceof ImageInputStream)) {
        return false;
      }
      ImageInputStream stream = (ImageInputStream) source;
      byte[] b = new byte[4];
      stream.mark();
      try {
        stream.readFully(b);
      } catch (java.io.EOFException e) {
        return false;
      } finally {
        stream.reset();
      }
      return b[0] == 'B' && b[1] == 'M';
    }

    public ImageReader createReaderInstance(Object extension) throws IOException {
      return new BmpReader(this);
    }
  }

  public static final class Writer extends ImageWriterSpi {
    public Writer() {
      super("Oracle Corporation", "1.0", new String[] {"bmp", "BMP"}, new String[] {"bmp"},
          new String[] {"image/bmp"}, "com.sun.imageio.plugins.bmp.BMPImageWriter", STANDARD_OUTPUT_TYPE,
          new String[] {"com.sun.imageio.plugins.bmp.BMPImageReaderSpi"}, false, null, null, null, null, true, "javax_imageio_bmp_1.0", null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard BMP Image Writer";
    }

    public boolean isFormatLossless() {
      return true;
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
      if (type.getColorModel().hasAlpha()) {
        return false;
      }
      for (int size : type.getSampleModel().getSampleSize()) {
        if (size > 8) {
          return false;
        }
      }
      return true;
    }

    public ImageWriter createWriterInstance(Object extension) throws IOException {
      return new BmpWriter(this);
    }
  }
}
