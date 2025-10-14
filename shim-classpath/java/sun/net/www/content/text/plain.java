/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.content.text;

import java.io.IOException;
import java.net.ContentHandler;
import java.net.URLConnection;

public class plain extends ContentHandler {
  @Override
  public Object getContent(URLConnection uc) {
    try {
      return new PlainTextInputStream(uc.getInputStream());
    } catch (IOException e) {
      return "Error reading document:\n" + e.toString();
    }
  }
}
