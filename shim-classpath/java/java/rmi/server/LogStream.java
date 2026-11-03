/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.rmi.server;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;

@Deprecated
public class LogStream extends PrintStream {
  private static Hashtable<String, LogStream> known = new Hashtable<String, LogStream>(5);
  private static PrintStream defaultStream = System.err;
  private String name;
  private OutputStream logOut;

  public static final int SILENT = 0;
  public static final int BRIEF = 10;
  public static final int VERBOSE = 20;

  private LogStream(String name, OutputStream out) {
    super(new java.io.ByteArrayOutputStream());
    logOut = out;
    this.name = name;
  }

  @Deprecated
  public static LogStream log(String name) {
    LogStream stream;
    synchronized (known) {
      stream = known.get(name);
      if (stream == null) {
        stream = new LogStream(name, defaultStream);
      }
      known.put(name, stream);
    }
    return stream;
  }

  @Deprecated
  public static synchronized PrintStream getDefaultStream() {
    return defaultStream;
  }

  @Deprecated
  public static synchronized void setDefaultStream(PrintStream newDefault) {
    defaultStream = newDefault;
  }

  @Deprecated
  public synchronized OutputStream getOutputStream() {
    return logOut;
  }

  @Deprecated
  public synchronized void setOutputStream(OutputStream out) {
    logOut = out;
  }

  @Deprecated
  public void write(int b) {
    try {
      if (logOut != null) {
        logOut.write(b);
      }
    } catch (java.io.IOException e) {
      setError();
    }
  }

  @Deprecated
  public void write(byte[] b, int off, int len) {
    try {
      if (logOut != null) {
        logOut.write(b, off, len);
      }
    } catch (java.io.IOException e) {
      setError();
    }
  }

  @Deprecated
  public String toString() {
    return name;
  }

  @Deprecated
  public static int parseLevel(String s) {
    if ((s == null) || (s.length() < 1)) {
      return -1;
    }
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
    }
    if (s.length() < 1) {
      return -1;
    }
    if ("SILENT".startsWith(s.toUpperCase())) {
      return SILENT;
    } else if ("BRIEF".startsWith(s.toUpperCase())) {
      return BRIEF;
    } else if ("VERBOSE".startsWith(s.toUpperCase())) {
      return VERBOSE;
    }
    return -1;
  }
}
