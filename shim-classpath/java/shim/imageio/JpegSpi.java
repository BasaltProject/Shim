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

public final class JpegSpi {
  private JpegSpi() { }

  public static final class Reader extends ImageReaderSpi {
    public Reader() {
      super("Oracle Corporation", "1.0", new String[] {"jpeg", "jpg", "JPG", "JPEG"}, new String[] {"jpg", "jpeg"}, new String[] {"image/jpeg"}, "shim.imageio.JpegReader",
          STANDARD_INPUT_TYPE, new String[] {"shim.imageio.JpegSpi$Writer"}, false, null, null, null, null, true,
          null, null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard JPEG image reader";
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
      return (b[0] & 0xff) == 0xff && (b[1] & 0xff) == 0xd8;
    }

    public ImageReader createReaderInstance(Object extension) throws IOException {
      return new JpegReader(this);
    }
  }

  public static final class Writer extends ImageWriterSpi {
    public Writer() {
      super("Oracle Corporation", "1.0", new String[] {"jpeg", "jpg", "JPG", "JPEG"}, new String[] {"jpg", "jpeg"}, new String[] {"image/jpeg"}, "shim.imageio.JpegWriter",
          STANDARD_OUTPUT_TYPE, new String[] {"shim.imageio.JpegSpi$Reader"}, false, null, null, null, null, true,
          null, null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard JPEG image writer";
    }

    public boolean isFormatLossless() {
      return false;
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
      return !type.getColorModel().hasAlpha();
    }

    public ImageWriter createWriterInstance(Object extension) throws IOException {
      return new JpegWriter(this);
    }
  }
}
