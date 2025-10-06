/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.io.IOException;
import java.io.OutputStream;

public interface Extension {
  String getId();

  boolean isCritical();

  byte[] getValue();

  void encode(OutputStream out) throws IOException;
}
