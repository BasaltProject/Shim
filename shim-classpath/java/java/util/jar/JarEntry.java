/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.jar;

import java.io.IOException;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.zip.ZipEntry;

public class JarEntry extends ZipEntry {
  Attributes attr;
  Certificate[] certs;
  CodeSigner[] signers;

  public JarEntry() {
    super("");
  }

  public JarEntry(String name) {
    super(name);
  }

  public JarEntry(ZipEntry ze) {
    super(ze);
  }

  public JarEntry(JarEntry je) {
    super((ZipEntry) je);
    this.attr = je.attr;
    this.certs = je.certs != null ? je.certs.clone() : null;
    this.signers = je.signers != null ? je.signers.clone() : null;
  }

  public Attributes getAttributes() throws IOException {
    return attr;
  }

  public Certificate[] getCertificates() {
    return certs == null ? null : certs.clone();
  }

  public CodeSigner[] getCodeSigners() {
    return signers == null ? null : signers.clone();
  }

  public String getRealName() {
    return super.getName();
  }
}
