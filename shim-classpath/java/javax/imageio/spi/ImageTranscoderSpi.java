/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.imageio.spi;

import javax.imageio.ImageTranscoder;

public abstract class ImageTranscoderSpi extends IIOServiceProvider {
  protected ImageTranscoderSpi() { }

  public ImageTranscoderSpi(String vendorName, String version) {
    super(vendorName, version);
  }

  public abstract String getReaderServiceProviderName();

  public abstract String getWriterServiceProviderName();

  public abstract ImageTranscoder createTranscoderInstance();
}
