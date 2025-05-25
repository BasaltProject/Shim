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

package java.util;

public class TimeZone {

  // for now, we only support GMT
  private static final TimeZone GMT = new TimeZone("GMT");

  private final String name;

  private TimeZone(String name) {
    this.name = name;
  }

  public static TimeZone getTimeZone(String id) {
    // technically the Java spec says that this method
    // returns GMT if it can't understand the "id" parameter
    // sigh.
    return GMT;
  }

  public String getDisplayName() {
    return name;
  }

}
