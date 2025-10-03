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

package java.io;

import java.nio.charset.Charset;

public class FileWriter extends OutputStreamWriter {
  public FileWriter(String fileName) throws IOException {
    super(open(new File(fileName), false));
  }

  public FileWriter(String fileName, boolean append) throws IOException {
    super(open(new File(fileName), append));
  }

  public FileWriter(File file) throws IOException {
    super(open(file, false));
  }

  public FileWriter(File file, boolean append) throws IOException {
    super(open(file, append));
  }

  public FileWriter(FileDescriptor fd) {
    super(new FileOutputStream(fd));
  }

  public FileWriter(String fileName, Charset charset) throws IOException {
    super(open(new File(fileName), false), charset);
  }

  public FileWriter(String fileName, Charset charset, boolean append) throws IOException {
    super(open(new File(fileName), append), charset);
  }

  public FileWriter(File file, Charset charset) throws IOException {
    super(open(file, false), charset);
  }

  public FileWriter(File file, Charset charset, boolean append) throws IOException {
    super(open(file, append), charset);
  }

  private static FileOutputStream open(File file, boolean append) throws IOException {
    return new FileOutputStream(file, append);
  }
}
