/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.awt;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

public final class ImageGraphicsDevice extends GraphicsDevice {
  private final GraphicsConfiguration config;

  ImageGraphicsDevice(GraphicsConfiguration config) {
    this.config = config;
  }

  public int getType() {
    return TYPE_IMAGE_BUFFER;
  }

  public String getIDstring() {
    return "Image";
  }

  public GraphicsConfiguration[] getConfigurations() {
    return new GraphicsConfiguration[] {config};
  }

  public GraphicsConfiguration getDefaultConfiguration() {
    return config;
  }
}
