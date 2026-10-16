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
import java.util.Objects;

public class PrintWriter extends Writer {
  private static final char[] newline
    = System.getProperty("line.separator").toCharArray();

  private final Writer out;
  private final boolean autoFlush;

  public PrintWriter(Writer out, boolean autoFlush) {
    this.out = out;
    this.autoFlush = autoFlush;
  }

  public PrintWriter(Writer out) {
    this(out, false);
  }

  public PrintWriter(OutputStream out, boolean autoFlush) {
    this(new OutputStreamWriter(out), autoFlush);
  }

  public PrintWriter(OutputStream out) {
    this(out, false);
  }

  private static OutputStream openFile(String fileName) throws FileNotFoundException {
    try {
      return new FileOutputStream(fileName);
    } catch (FileNotFoundException e) {
      throw e;
    } catch (IOException e) {
      throw new FileNotFoundException(e.getMessage());
    }
  }

  private static OutputStream openFile(File file) throws FileNotFoundException {
    return openFile(file.getPath());
  }

  public PrintWriter(OutputStream out, boolean autoFlush, Charset charset) {
    this(new OutputStreamWriter(out, charset), autoFlush);
  }

  public PrintWriter(String fileName) throws FileNotFoundException {
    this(openFile(fileName), false);
  }

  public PrintWriter(String fileName, String csn)
      throws FileNotFoundException, UnsupportedEncodingException {
    this(new OutputStreamWriter(openFile(fileName), csn), false);
  }

  public PrintWriter(String fileName, Charset charset) throws IOException {
    this(new OutputStreamWriter(openFile(fileName), charset), false);
  }

  public PrintWriter(File file) throws FileNotFoundException {
    this(openFile(file), false);
  }

  public PrintWriter(File file, String csn)
      throws FileNotFoundException, UnsupportedEncodingException {
    this(new OutputStreamWriter(openFile(file), csn), false);
  }

  public PrintWriter(File file, Charset charset) throws IOException {
    this(new OutputStreamWriter(openFile(file), charset), false);
  }

  public synchronized void print(String s) {
    try {
      out.write(s.toCharArray());
    } catch (IOException e) { }
  }

  public void print(Object o) {
    print(o.toString());
  }

  public void print(char c) {
    print(String.valueOf(c));
  }

  public synchronized void println(String s) {
    try {
      out.write(s.toCharArray());    
      out.write(newline);
      if (autoFlush) flush();
    } catch (IOException e) { }
  }

  public synchronized void println() {
    try {
      out.write(newline);
      if (autoFlush) flush();
    } catch (IOException e) { }
  }

  public void println(Object o) {
    println(o.toString());
  }

  public void println(char c) {
    println(String.valueOf(c));
  }

  public void write(char[] buffer, int offset, int length) throws IOException {
    out.write(buffer, offset, length);
    if (autoFlush) flush();
  }

  public void flush() {
    try {
      out.flush();
    } catch (IOException e) { }
  }

  public PrintWriter format(String format, Object... args) {
    print(String.format(format, args));
    return this;
  }

  public PrintWriter printf(String format, Object... args) {
    return format(format, args);
  }

  public void close() {
    try {
      out.close();
    } catch (IOException e) { }
  }
}
