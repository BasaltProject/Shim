/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.plugins.bmp;

import java.util.Locale;
import javax.imageio.ImageWriteParam;

public class BMPImageWriteParam extends ImageWriteParam {
  private boolean topDown = false;

  public BMPImageWriteParam() { this(null); }

  public BMPImageWriteParam(Locale locale) {
    super(locale);
    compressionTypes = new String[] {"BI_RGB", "BI_RLE8", "BI_RLE4", "BI_BITFIELDS", "BI_JPEG", "BI_PNG"};
    canWriteCompressed = true;
    compressionMode = MODE_COPY_FROM_METADATA;
    compressionType = "BI_RGB";
  }

  public void setTopDown(boolean topDown) { this.topDown = topDown; }

  public boolean isTopDown() { return topDown; }
}
