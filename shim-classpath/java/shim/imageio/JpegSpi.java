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
      super("Oracle Corporation", "0.5", new String[] {"JPEG", "jpeg", "JPG", "jpg"}, new String[] {"jpg", "jpeg"},
          new String[] {"image/jpeg"}, "com.sun.imageio.plugins.jpeg.JPEGImageReader", STANDARD_INPUT_TYPE,
          new String[] {"com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi"}, true, "javax_imageio_jpeg_stream_1.0", null, null, null, true, "javax_imageio_jpeg_image_1.0", null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard JPEG Image Reader";
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
      super("Oracle Corporation", "0.5", new String[] {"JPEG", "jpeg", "JPG", "jpg"}, new String[] {"jpg", "jpeg"},
          new String[] {"image/jpeg"}, "com.sun.imageio.plugins.jpeg.JPEGImageWriter", STANDARD_OUTPUT_TYPE,
          new String[] {"com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi"}, true, "javax_imageio_jpeg_stream_1.0", null, null, null, true, "javax_imageio_jpeg_image_1.0", null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard JPEG Image Writer";
    }

    public boolean isFormatLossless() {
      return false;
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
      return new JpegWriter(this);
    }
  }
}
