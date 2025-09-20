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

public class PrintStream extends OutputStream implements Appendable {
  private final OutputStream out;
  private final boolean autoFlush;

  private static class Static {
    private static final byte[] newline
      = System.getProperty("line.separator").getBytes();
  }

  public PrintStream(OutputStream out, boolean autoFlush) {
    this.out = out;
    this.autoFlush = autoFlush;
  }

  public PrintStream(OutputStream out, boolean autoFlush, String encoding)
    throws UnsupportedEncodingException
  {
    this.out = out;
    this.autoFlush = autoFlush;

    if (! (encoding.equals("UTF-8") || encoding.equals("ISO-8859-1"))) {
      throw new UnsupportedEncodingException(encoding);
    }
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

  public PrintStream(OutputStream out, boolean autoFlush, Charset charset) {
    this.out = out;
    this.autoFlush = autoFlush;
    Objects.requireNonNull(charset);
  }

  public PrintStream(OutputStream out) {
    this(out, false);
  }

  public PrintStream(String fileName) throws FileNotFoundException {
    this(openFile(fileName), false);
  }

  public PrintStream(String fileName, String csn)
      throws FileNotFoundException, UnsupportedEncodingException {
    this(openFile(fileName), false, csn);
  }

  public PrintStream(String fileName, Charset charset) throws IOException {
    this(openFile(fileName), false, charset);
  }

  public PrintStream(File file) throws FileNotFoundException {
    this(openFile(file), false);
  }

  public PrintStream(File file, String csn)
      throws FileNotFoundException, UnsupportedEncodingException {
    this(openFile(file), false, csn);
  }

  public PrintStream(File file, Charset charset) throws IOException {
    this(openFile(file), false, charset);
  }

  public synchronized void print(String s) {
    try {
      out.write((s == null ? "null" : s).getBytes());
      if (autoFlush) flush();
    } catch (IOException e) { }
  }

  public void print(Object o) {
    print(String.valueOf(o));
  }

  public void print(boolean v) {
    print(String.valueOf(v));
  }

  public void print(char c) {
    print(String.valueOf(c));
  }

  public void print(int v) {
    print(String.valueOf(v));
  }

  public void print(long v) {
    print(String.valueOf(v));
  }

  public void print(float v) {
    print(String.valueOf(v));
  }

  public void print(double v) {
    print(String.valueOf(v));
  }

  public void print(char[] s) {
    print(String.valueOf(s));
  }

  public synchronized PrintStream printf(java.util.Locale locale, String format, Object... args) {
    // should this be cached in an instance variable??
    final java.util.Formatter formatter = new java.util.Formatter(this);
    formatter.format(locale, format, args);
    return this;
  }

  public synchronized PrintStream printf(String format, Object... args) {
    final java.util.Formatter formatter = new java.util.Formatter(this);
    formatter.format(format, args);
    return this;
  }

  public PrintStream format(String format, Object... args) {
    return printf(format, args);
  }

  public PrintStream format(java.util.Locale locale, String format, Object... args) {
    return printf(locale, format, args);
  }

  public synchronized void println(String s) {
    try {
      out.write((s == null ? "null" : s).getBytes());
      out.write(Static.newline);
      if (autoFlush) flush();
    } catch (IOException e) { }
  }

  public synchronized void println() {
    try {
      out.write(Static.newline);
      if (autoFlush) flush();
    } catch (IOException e) { }
  }

  public void println(Object o) {
    println(String.valueOf(o));
  }

  public void println(boolean v) {
    println(String.valueOf(v));
  }

  public void println(char c) {
    println(String.valueOf(c));
  }

  public void println(int v) {
    println(String.valueOf(v));
  }

  public void println(long v) {
    println(String.valueOf(v));
  }

  public void println(float v) {
    println(String.valueOf(v));
  }

  public void println(double v) {
    println(String.valueOf(v));
  }

  public void println(char[] s) {
    println(String.valueOf(s));
  }
  
  public void write(int c) throws IOException {
    out.write(c);
    if (autoFlush && c == '\n') flush();
  }

  public void write(byte[] buffer, int offset, int length) throws IOException {
    out.write(buffer, offset, length);
    if (autoFlush) flush();
  }

  public void flush() {
    try {
      out.flush();
    } catch (IOException e) { }
  }

  public void close() {
    try {
      out.close();
    } catch (IOException e) { }
  }

  public PrintStream append(CharSequence csq) {
    print(String.valueOf(csq));
    return this;
  }

  public PrintStream append(CharSequence csq, int start, int end) {
    if (csq == null) {
      csq = "null";
    }
    return append(csq.subSequence(start, end));
  }

  public PrintStream append(char c) {
    print(c);
    return this;
  }
}
