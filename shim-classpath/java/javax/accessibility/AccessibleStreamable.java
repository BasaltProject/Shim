/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.accessibility;

public interface AccessibleStreamable {
  java.awt.datatransfer.DataFlavor[] getMimeTypes();
  java.io.InputStream getStream(java.awt.datatransfer.DataFlavor p0);
}
