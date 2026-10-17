/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.net.www.content.text;

import java.io.FilterInputStream;
import java.io.InputStream;

public class PlainTextInputStream extends FilterInputStream {
  public PlainTextInputStream(InputStream is) {
    super(is);
  }
}
