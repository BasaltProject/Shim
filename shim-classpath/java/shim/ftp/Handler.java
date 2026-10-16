/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.ftp;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Handler extends java.net.URLStreamHandler {
  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    throw new IOException("ftp connections are not supported");
  }

  @Override
  protected int getDefaultPort() {
    return 21;
  }
}
