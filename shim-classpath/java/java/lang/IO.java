/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class IO {
  private static BufferedReader br;

  private IO() {
    throw new Error("no instances");
  }

  public static void println(Object obj) {
    System.out.println(obj);
  }

  public static void println() {
    System.out.println();
  }

  public static void print(Object obj) {
    PrintStream out = System.out;
    out.print(obj);
    out.flush();
  }

  public static String readln() {
    try {
      return reader().readLine();
    } catch (IOException e) {
      throw new IOError(e);
    }
  }

  public static String readln(String prompt) {
    print(prompt);
    return readln();
  }

  static synchronized BufferedReader reader() {
    if (br == null) {
      String enc = System.getProperty("stdin.encoding", "");
      Charset cs = Charset.forName(enc, StandardCharsets.UTF_8);
      br = new BufferedReader(new InputStreamReader(System.in, cs));
    }
    return br;
  }
}
