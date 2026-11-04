/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Objects;

public class ForwardingFileObject<F extends FileObject> implements FileObject {
  protected final F fileObject;

  protected ForwardingFileObject(F fileObject) {
    this.fileObject = Objects.requireNonNull(fileObject);
  }

  public URI toUri() {
    return fileObject.toUri();
  }

  public String getName() {
    return fileObject.getName();
  }

  public InputStream openInputStream() throws IOException {
    return fileObject.openInputStream();
  }

  public OutputStream openOutputStream() throws IOException {
    return fileObject.openOutputStream();
  }

  public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    return fileObject.openReader(ignoreEncodingErrors);
  }

  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return fileObject.getCharContent(ignoreEncodingErrors);
  }

  public Writer openWriter() throws IOException {
    return fileObject.openWriter();
  }

  public long getLastModified() {
    return fileObject.getLastModified();
  }

  public boolean delete() {
    return fileObject.delete();
  }
}
