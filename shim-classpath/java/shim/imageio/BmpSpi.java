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
      super("Oracle Corporation", "1.0", new String[] {"bmp", "BMP"}, new String[] {"bmp"}, new String[] {"image/bmp"}, "shim.imageio.BmpReader",
          STANDARD_INPUT_TYPE, new String[] {"shim.imageio.BmpSpi$Writer"}, false, null, null, null, null, true,
          null, null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard BMP image reader";
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
      super("Oracle Corporation", "1.0", new String[] {"bmp", "BMP"}, new String[] {"bmp"}, new String[] {"image/bmp"}, "shim.imageio.BmpWriter",
          STANDARD_OUTPUT_TYPE, new String[] {"shim.imageio.BmpSpi$Reader"}, false, null, null, null, null, true,
          null, null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard BMP image writer";
    }

    public boolean isFormatLossless() {
      return true;
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
      return !type.getColorModel().hasAlpha();
    }

    public ImageWriter createWriterInstance(Object extension) throws IOException {
      return new BmpWriter(this);
    }
  }
}
