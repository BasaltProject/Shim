/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.jar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarInputStream extends ZipInputStream {
  private Manifest manifest;
  private JarEntry first;
  private boolean hasChecked;

  public JarInputStream(InputStream in) throws IOException {
    this(in, true);
  }

  public JarInputStream(InputStream in, boolean verify) throws IOException {
    super(in);
    checkManifest();
  }

  private void checkManifest() throws IOException {
    if (hasChecked) return;
    hasChecked = true;
    JarEntry e = (JarEntry) super.getNextEntry();
    if (e != null && e.getName().equalsIgnoreCase("META-INF/")) {
      e = (JarEntry) super.getNextEntry();
    }
    if (e != null && JarFile.MANIFEST_NAME.equalsIgnoreCase(e.getName())) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n;
      while ((n = super.read(buf, 0, buf.length)) != -1) {
        baos.write(buf, 0, n);
      }
      manifest = new Manifest(new ByteArrayInputStream(baos.toByteArray()));
      first = null;
    } else {
      first = e;
    }
  }

  public Manifest getManifest() {
    return manifest;
  }

  public JarEntry getNextJarEntry() throws IOException {
    return (JarEntry) getNextEntry();
  }

  @Override
  public ZipEntry getNextEntry() throws IOException {
    if (first != null) {
      JarEntry e = first;
      first = null;
      return e;
    }
    return super.getNextEntry();
  }

  @Override
  protected ZipEntry createZipEntry(String name) {
    return new JarEntry(name);
  }
}
