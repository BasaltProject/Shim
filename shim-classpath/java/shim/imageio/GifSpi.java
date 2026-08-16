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

public final class GifSpi {
  private GifSpi() { }

  public static final class Reader extends ImageReaderSpi {
    public Reader() {
      super("Oracle Corporation", "1.0", new String[] {"gif", "GIF"}, new String[] {"gif"}, new String[] {"image/gif"}, "shim.imageio.GifReader",
          STANDARD_INPUT_TYPE, new String[] {"shim.imageio.GifSpi$Writer"}, false, null, null, null, null, true,
          null, null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard GIF image reader";
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
      return b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8';
    }

    public ImageReader createReaderInstance(Object extension) throws IOException {
      return new GifReader(this);
    }
  }

  public static final class Writer extends ImageWriterSpi {
    public Writer() {
      super("Oracle Corporation", "1.0", new String[] {"gif", "GIF"}, new String[] {"gif"}, new String[] {"image/gif"}, "shim.imageio.GifWriter",
          STANDARD_OUTPUT_TYPE, new String[] {"shim.imageio.GifSpi$Reader"}, false, null, null, null, null, true,
          null, null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard GIF image writer";
    }

    public boolean isFormatLossless() {
      return true;
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
      return true;
    }

    public ImageWriter createWriterInstance(Object extension) throws IOException {
      return new GifWriter(this);
    }
  }
}
