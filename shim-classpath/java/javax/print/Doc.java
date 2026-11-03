/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print;

public interface Doc {
  javax.print.attribute.DocAttributeSet getAttributes();
  javax.print.DocFlavor getDocFlavor();
  java.lang.Object getPrintData() throws java.io.IOException;
  java.io.InputStream getStreamForBytes() throws java.io.IOException;
  java.io.Reader getReaderForText() throws java.io.IOException;
}
