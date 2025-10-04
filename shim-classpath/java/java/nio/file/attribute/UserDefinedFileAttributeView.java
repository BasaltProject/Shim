/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file.attribute;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public interface UserDefinedFileAttributeView extends FileAttributeView {
  @Override
  String name();

  List<String> list() throws IOException;

  int size(String name) throws IOException;

  int read(String name, ByteBuffer dst) throws IOException;

  int write(String name, ByteBuffer src) throws IOException;

  void delete(String name) throws IOException;
}
