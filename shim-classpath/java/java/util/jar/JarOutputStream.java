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
  private static final byte[] JAR_MAGIC = {(byte) 0xCA, (byte) 0xFE, 0, 0};
  private boolean firstEntry = true;

  public JarOutputStream(OutputStream out, Manifest man) throws IOException {
    super(out);
    if (man == null) {
      throw new NullPointerException("man");
    }
    ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
    putNextEntry(e);
    java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(this);
    man.write(bos);
    bos.flush();
    closeEntry();
  }

  public JarOutputStream(OutputStream out) throws IOException {
    super(out);
  }

  @Override
  public void putNextEntry(ZipEntry ze) throws IOException {
    if (firstEntry) {
      byte[] edata = ze.getExtra();
      if (edata == null || !hasMagic(edata)) {
        if (edata == null) {
          edata = JAR_MAGIC;
        } else {
          byte[] tmp = new byte[edata.length + JAR_MAGIC.length];
          System.arraycopy(JAR_MAGIC, 0, tmp, 0, JAR_MAGIC.length);
          System.arraycopy(edata, 0, tmp, JAR_MAGIC.length, edata.length);
          edata = tmp;
        }
        ze.setExtra(edata);
      }
      firstEntry = false;
    }
    super.putNextEntry(ze);
  }

  private static boolean hasMagic(byte[] edata) {
    int i = 0;
    while (i + 4 <= edata.length) {
      if ((edata[i] & 0xff) == 0xCA && (edata[i + 1] & 0xff) == 0xFE) {
        return true;
      }
      i += (edata[i + 2] & 0xff) + ((edata[i + 3] & 0xff) << 8) + 4;
    }
    return false;
  }
}
