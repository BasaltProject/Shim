/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.jar;

import java.util.zip.ZipException;

public class JarException extends ZipException {
  public JarException() {
    super();
  }

  public JarException(String s) {
    super(s);
  }
}
