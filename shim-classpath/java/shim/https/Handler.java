/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.https;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Handler extends shim.http.Handler {
  @Override
  protected int getDefaultPort() {
    return 443;
  }
}
