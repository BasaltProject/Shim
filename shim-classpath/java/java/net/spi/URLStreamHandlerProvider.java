/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net.spi;

import java.net.URLStreamHandlerFactory;

public abstract class URLStreamHandlerProvider implements URLStreamHandlerFactory {
  protected URLStreamHandlerProvider() {
  }
}
