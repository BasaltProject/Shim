/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;

class UnknownContentHandler extends ContentHandler {
  static final ContentHandler kInstance = new UnknownContentHandler();

  @Override
  public Object getContent(URLConnection uc) throws IOException {
    return uc.getInputStream();
  }
}
