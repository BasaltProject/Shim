/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Set;

public interface SecureDirectoryStream<T> extends DirectoryStream<T> {
  SecureDirectoryStream<T> newDirectoryStream(T path, LinkOption... options) throws IOException;

  SeekableByteChannel newByteChannel(T path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException;

  void deleteFile(T path) throws IOException;

  void deleteDirectory(T path) throws IOException;

  void move(T srcpath, SecureDirectoryStream<T> targetdir, T targetpath) throws IOException;

  <V extends FileAttributeView> V getFileAttributeView(Class<V> type);

  <V extends FileAttributeView> V getFileAttributeView(T path, Class<V> type, LinkOption... options);
}
