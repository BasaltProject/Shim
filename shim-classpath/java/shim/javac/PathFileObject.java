/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

public class PathFileObject extends SimpleJavaFileObject {
  private final Path path;
  private final Charset charset;

  public PathFileObject(Path path, Kind kind, Charset charset) {
    super(path.toAbsolutePath().normalize().toUri(), kind);
    this.path = path;
    this.charset = charset;
  }

  public Path path() {
    return path;
  }

  public String getName() {
    return path.toString();
  }

  public InputStream openInputStream() throws IOException {
    return Files.newInputStream(path);
  }

  public OutputStream openOutputStream() throws IOException {
    Path parent = path.toAbsolutePath().getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    return Files.newOutputStream(path);
  }

  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    return new String(Files.readAllBytes(path), charset);
  }

  public long getLastModified() {
    try {
      return Files.getLastModifiedTime(path).toMillis();
    } catch (IOException e) {
      return 0L;
    }
  }

  public boolean delete() {
    try {
      return Files.deleteIfExists(path);
    } catch (IOException e) {
      return false;
    }
  }

  public boolean isNameCompatible(String simpleName, JavaFileObject.Kind kind) {
    String base = simpleName + kind.extension;
    return kind == getKind() && path.getFileName().toString().equals(base);
  }

  public boolean equals(Object o) {
    return o instanceof PathFileObject && ((PathFileObject) o).path.toAbsolutePath().normalize()
        .equals(path.toAbsolutePath().normalize());
  }

  public int hashCode() {
    return path.toAbsolutePath().normalize().hashCode();
  }

  public String toString() {
    return getClass().getSimpleName() + "[" + path + "]";
  }
}
