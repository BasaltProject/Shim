/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.imageio.plugins.gif;

import java.util.Locale;
import javax.imageio.ImageWriteParam;

public class GIFImageWriteParam extends ImageWriteParam {
  public GIFImageWriteParam(Locale locale) {
    super(locale);
    canWriteCompressed = true;
    canWriteProgressive = true;
    compressionTypes = new String[] {"LZW"};
    progressiveMode = MODE_COPY_FROM_METADATA;
  }

  public void setCompressionMode(int mode) {
    super.setCompressionMode(mode);
    compressionType = null;
  }
}
