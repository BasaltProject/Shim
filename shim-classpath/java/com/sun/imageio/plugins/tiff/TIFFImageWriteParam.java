/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.imageio.plugins.tiff;

import java.util.Locale;
import javax.imageio.ImageWriteParam;

public class TIFFImageWriteParam extends ImageWriteParam {
  public TIFFImageWriteParam(Locale locale) {
    super(locale);
    canWriteTiles = true;
    canWriteCompressed = true;
    compressionTypes = new String[] {"CCITT RLE", "CCITT T.4", "CCITT T.6", "LZW", "JPEG", "ZLib", "PackBits",
        "Deflate", "Exif JPEG"};
  }

  public String[] getCompressionTypes() {
    return super.getCompressionTypes();
  }
}
