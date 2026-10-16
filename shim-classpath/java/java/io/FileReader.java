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

public class FileReader extends InputStreamReader {
  public FileReader(String fileName) throws FileNotFoundException {
    super(open(new File(fileName)));
  }

  public FileReader(File file) throws FileNotFoundException {
    super(open(file));
  }

  public FileReader(FileDescriptor fd) {
    super(new FileInputStream(fd));
  }

  public FileReader(String fileName, Charset charset) throws IOException {
    super(open(new File(fileName)), charset);
  }

  public FileReader(File file, Charset charset) throws IOException {
    super(open(file), charset);
  }

  FileReader(FileInputStream in) {
    super(in);
  }

  private static FileInputStream open(File file) throws FileNotFoundException {
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      throw e;
    } catch (IOException e) {
      throw new FileNotFoundException(e.getMessage());
    }
  }
}
