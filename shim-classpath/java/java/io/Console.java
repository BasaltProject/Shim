/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.io;

import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.Locale;

public final class Console implements Flushable {
  private static Console instance;
  private static boolean resolved;

  private final Charset charset;
  private final Reader reader;
  private final PrintWriter writer;
  private final Object readLock = new Object();
  private final Object writeLock = new Object();

  private Console(Charset charset) {
    this.charset = charset;
    this.reader = new InputStreamReader(System.in, charset);
    this.writer = new PrintWriter(new OutputStreamWriter(System.out, charset), true);
  }

  static synchronized Console instance() {
    if (!resolved) {
      resolved = true;
      if (shim.FileNative.isTerminal(0) && shim.FileNative.isTerminal(1)) {
        String name = System.getProperty("stdout.encoding");
        Charset cs;
        try {
          cs = name == null ? Charset.defaultCharset() : Charset.forName(name);
        } catch (RuntimeException e) {
          cs = Charset.defaultCharset();
        }
        instance = new Console(cs);
      }
    }
    return instance;
  }

  public PrintWriter writer() {
    return writer;
  }

  public Reader reader() {
    return reader;
  }

  public Console format(String fmt, Object... args) {
    return format(Locale.getDefault(Locale.Category.FORMAT), fmt, args);
  }

  public Console format(Locale locale, String fmt, Object... args) {
    synchronized (writeLock) {
      writer.format(locale, fmt, args);
      writer.flush();
    }
    return this;
  }

  public Console printf(String format, Object... args) {
    return format(format, args);
  }

  public Console printf(Locale locale, String format, Object... args) {
    return format(locale, format, args);
  }

  public String readLine(String fmt, Object... args) {
    return readLine(Locale.getDefault(Locale.Category.FORMAT), fmt, args);
  }

  public String readLine(Locale locale, String fmt, Object... args) {
    synchronized (writeLock) {
      synchronized (readLock) {
        if (!fmt.isEmpty()) {
          format(locale, fmt, args);
        }
        try {
          return readLineLocked();
        } catch (IOException e) {
          throw new IOError(e);
        }
      }
    }
  }

  public String readLine() {
    return readLine("");
  }

  private String readLineLocked() throws IOException {
    StringBuilder sb = new StringBuilder();
    int c;
    while ((c = reader.read()) != -1) {
      if (c == '\n') {
        return sb.toString();
      }
      if (c == '\r') {
        continue;
      }
      sb.append((char) c);
    }
    return sb.length() == 0 ? null : sb.toString();
  }

  public char[] readPassword(String fmt, Object... args) {
    return readPassword(Locale.getDefault(Locale.Category.FORMAT), fmt, args);
  }

  public char[] readPassword(Locale locale, String fmt, Object... args) {
    synchronized (writeLock) {
      synchronized (readLock) {
        boolean echo = shim.FileNative.setEcho(false);
        try {
          if (!fmt.isEmpty()) {
            format(locale, fmt, args);
          }
          String line = readLineLocked();
          writer.println();
          writer.flush();
          return line == null ? null : line.toCharArray();
        } catch (IOException e) {
          throw new IOError(e);
        } finally {
          shim.FileNative.setEcho(echo);
        }
      }
    }
  }

  public char[] readPassword() {
    return readPassword("");
  }

  public boolean isTerminal() {
    return true;
  }

  public Charset charset() {
    return charset;
  }

  @Override
  public void flush() {
    writer.flush();
  }
}
