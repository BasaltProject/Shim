/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.net;

import java.io.IOException;

public abstract class ContentHandler {
  public ContentHandler() { }

  public abstract Object getContent(URLConnection urlc) throws IOException;

  @SuppressWarnings("rawtypes")
  public Object getContent(URLConnection urlc, Class[] classes) throws IOException {
    Object obj = getContent(urlc);
    for (int i = 0; i < classes.length; i++) {
      if (classes[i].isInstance(obj)) {
        return obj;
      }
    }
    return null;
  }
}
