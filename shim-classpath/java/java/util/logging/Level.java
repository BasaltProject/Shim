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

package java.util.logging;

import java.io.Serializable;

public class Level implements Serializable {
  public static final Level OFF = new Level("OFF", Integer.MAX_VALUE);
  public static final Level SEVERE = new Level("SEVERE", 1000);
  public static final Level WARNING = new Level("WARNING", 900);
  public static final Level INFO = new Level("INFO", 800);
  public static final Level CONFIG = new Level("CONFIG", 700);
  public static final Level FINE = new Level("FINE", 500);
  public static final Level FINER = new Level("FINER", 400);
  public static final Level FINEST = new Level("FINEST", 300);
  public static final Level ALL = new Level("ALL", Integer.MIN_VALUE);

  private final int value;
  private final String name;

  protected Level(String name, int value) {
    if (name == null) {
      throw new NullPointerException();
    }
    this.name = name;
    this.value = value;
  }

  public int intValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public String getResourceBundleName() {
    return null;
  }

  public String getLocalizedName() {
    return name;
  }

  public static Level parse(String name) throws IllegalArgumentException {
    if (name == null) {
      throw new NullPointerException();
    }
    try {
      int val = Integer.parseInt(name);
      return new Level(name, val);
    } catch (NumberFormatException ignored) {
    }
    if (name.equalsIgnoreCase("OFF")) return OFF;
    if (name.equalsIgnoreCase("SEVERE")) return SEVERE;
    if (name.equalsIgnoreCase("WARNING")) return WARNING;
    if (name.equalsIgnoreCase("INFO")) return INFO;
    if (name.equalsIgnoreCase("CONFIG")) return CONFIG;
    if (name.equalsIgnoreCase("FINE")) return FINE;
    if (name.equalsIgnoreCase("FINER")) return FINER;
    if (name.equalsIgnoreCase("FINEST")) return FINEST;
    if (name.equalsIgnoreCase("ALL")) return ALL;
    throw new IllegalArgumentException("Unknown level: " + name);
  }

  public boolean equals(Object o) {
    if (o instanceof Level) {
      return ((Level) o).value == this.value;
    }
    return false;
  }

  public int hashCode() {
    return this.value;
  }

  public String toString() {
    return name;
  }
}
