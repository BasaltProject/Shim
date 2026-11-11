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

public final class PngSpi {
  private PngSpi() { }

  public static final class Reader extends ImageReaderSpi {
    public Reader() {
      super("Oracle Corporation", "1.0", new String[] {"png", "PNG"}, new String[] {"png"},
          new String[] {"image/png", "image/x-png"}, "com.sun.imageio.plugins.png.PNGImageReader", STANDARD_INPUT_TYPE,
          new String[] {"com.sun.imageio.plugins.png.PNGImageWriterSpi"}, false, null, null, null, null, true, "javax_imageio_png_1.0", null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard PNG image reader";
    }

    public boolean canDecodeInput(Object source) throws IOException {
      if (!(source instanceof ImageInputStream)) {
        return false;
      }
      ImageInputStream stream = (ImageInputStream) source;
      byte[] b = new byte[8];
      stream.mark();
      try {
        stream.readFully(b);
      } catch (java.io.EOFException e) {
        return false;
      } finally {
        stream.reset();
      }
      return (b[0] & 0xff) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G' && b[4] == 0x0d && b[5] == 0x0a
          && b[6] == 0x1a && b[7] == 0x0a;
    }

    public ImageReader createReaderInstance(Object extension) throws IOException {
      return new PngReader(this);
    }
  }

  public static final class Writer extends ImageWriterSpi {
    public Writer() {
      super("Oracle Corporation", "1.0", new String[] {"png", "PNG"}, new String[] {"png"},
          new String[] {"image/png", "image/x-png"}, "com.sun.imageio.plugins.png.PNGImageWriter", STANDARD_OUTPUT_TYPE,
          new String[] {"com.sun.imageio.plugins.png.PNGImageReaderSpi"}, false, null, null, null, null, true, "javax_imageio_png_1.0", null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard PNG image writer";
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
      return true;
    }

    public ImageWriter createWriterInstance(Object extension) throws IOException {
      return new PngWriter(this);
    }
  }
}
