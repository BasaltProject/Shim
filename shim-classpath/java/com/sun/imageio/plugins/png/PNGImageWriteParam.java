/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.imageio.plugins.png;

import java.util.Locale;
import javax.imageio.ImageWriteParam;

public class PNGImageWriteParam extends ImageWriteParam {
  public PNGImageWriteParam(Locale locale) {
    super(locale);
    canWriteProgressive = true;
    canWriteCompressed = true;
    compressionTypes = new String[] {"Deflate"};
    compressionType = compressionTypes[0];
    compressionQuality = 0.5f;
    progressiveMode = MODE_COPY_FROM_METADATA;
    compressionMode = MODE_DEFAULT;
  }

  public void unsetCompression() {
    super.unsetCompression();
    compressionType = compressionTypes[0];
    compressionQuality = 0.5f;
  }

  public boolean isCompressionLossless() {
    return true;
  }

  public String[] getCompressionQualityDescriptions() {
    super.getCompressionQualityDescriptions();
    return new String[] {"High compression", "Medium compression", "Low compression"};
  }

  public float[] getCompressionQualityValues() {
    super.getCompressionQualityValues();
    return new float[] {0.0f, 0.3f, 0.75f, 1.0f};
  }
}
