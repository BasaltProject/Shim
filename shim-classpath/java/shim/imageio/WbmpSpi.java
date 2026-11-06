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

public final class WbmpSpi {
  private WbmpSpi() { }

  public static final class Reader extends ImageReaderSpi {
    public Reader() {
      super("Oracle Corporation", "1.0", new String[] {"wbmp", "WBMP"}, new String[] {"wbmp"},
          new String[] {"image/vnd.wap.wbmp"}, "shim.imageio.WbmpReader", STANDARD_INPUT_TYPE,
          new String[] {"shim.imageio.WbmpSpi$Writer"}, false, null, null, null, null, true, null, null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard WBMP Image Reader";
    }

    public boolean canDecodeInput(Object source) throws IOException {
      if (!(source instanceof ImageInputStream)) {
        return false;
      }
      ImageInputStream in = (ImageInputStream) source;
      in.mark();
      try {
        if (in.readUnsignedByte() != 0 || in.readUnsignedByte() != 0) {
          return false;
        }
        int w = WbmpReader.readMultiByte(in);
        int h = WbmpReader.readMultiByte(in);
        if (w <= 0 || h <= 0) {
          return false;
        }
        long len = in.length();
        if (len == -1) {
          return true;
        }
        long dataStart = in.getStreamPosition();
        return len - dataStart == (long) ((w + 7) / 8) * h;
      } catch (java.io.EOFException e) {
        return false;
      } catch (javax.imageio.IIOException e) {
        return false;
      } finally {
        in.reset();
      }
    }

    public ImageReader createReaderInstance(Object extension) throws IOException {
      return new WbmpReader(this);
    }
  }

  public static final class Writer extends ImageWriterSpi {
    public Writer() {
      super("Oracle Corporation", "1.0", new String[] {"wbmp", "WBMP"}, new String[] {"wbmp"},
          new String[] {"image/vnd.wap.wbmp"}, "shim.imageio.WbmpWriter", STANDARD_OUTPUT_TYPE,
          new String[] {"shim.imageio.WbmpSpi$Reader"}, false, null, null, null, null, true, null, null, null, null);
    }

    public String getDescription(Locale locale) {
      return "Standard WBMP Image Writer";
    }

    public boolean isFormatLossless() {
      return true;
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
      return WbmpWriter.bilevel(type.getSampleModel());
    }

    public ImageWriter createWriterInstance(Object extension) throws IOException {
      return new WbmpWriter(this);
    }
  }
}
