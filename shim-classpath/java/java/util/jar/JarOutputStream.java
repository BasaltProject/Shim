/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.jar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JarOutputStream extends ZipOutputStream {
  public JarOutputStream(OutputStream out, Manifest man) throws IOException {
    super(out);
    if (man == null) {
      throw new NullPointerException("man");
    }
    ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
    putNextEntry(e);
    man.write(this);
    closeEntry();
  }

  public JarOutputStream(OutputStream out) throws IOException {
    super(out);
  }

  @Override
  public void putNextEntry(ZipEntry ze) throws IOException {
    super.putNextEntry(ze);
  }
}
