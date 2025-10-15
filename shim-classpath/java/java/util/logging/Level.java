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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class Level implements Serializable {
  private static final String kDefaultBundle = "sun.util.logging.resources.logging";

  private static final List<Level> kKnownLevels = new ArrayList<Level>();

  private final String name;
  private final int value;
  private final String resourceBundleName;
  private transient String localizedLevelName;
  private transient Locale cachedLocale;

  public static final Level OFF = new Level("OFF", Integer.MAX_VALUE, kDefaultBundle);
  public static final Level SEVERE = new Level("SEVERE", 1000, kDefaultBundle);
  public static final Level WARNING = new Level("WARNING", 900, kDefaultBundle);
  public static final Level INFO = new Level("INFO", 800, kDefaultBundle);
  public static final Level CONFIG = new Level("CONFIG", 700, kDefaultBundle);
  public static final Level FINE = new Level("FINE", 500, kDefaultBundle);
  public static final Level FINER = new Level("FINER", 400, kDefaultBundle);
  public static final Level FINEST = new Level("FINEST", 300, kDefaultBundle);
  public static final Level ALL = new Level("ALL", Integer.MIN_VALUE, kDefaultBundle);

  private static final long serialVersionUID = -8176160795706313070L;

  protected Level(String name, int value) {
    this(name, value, null);
  }

  protected Level(String name, int value, String resourceBundleName) {
    this(name, value, resourceBundleName, true);
  }

  private Level(String name, int value, String resourceBundleName, boolean visible) {
    if (name == null) {
      throw new NullPointerException();
    }
    this.name = name;
    this.value = value;
    this.resourceBundleName = resourceBundleName;
    this.localizedLevelName = resourceBundleName == null ? name : null;
    this.cachedLocale = null;
    if (visible) {
      synchronized (Level.class) {
        kKnownLevels.add(this);
      }
    }
  }

  public String getResourceBundleName() {
    return resourceBundleName;
  }

  public String getName() {
    return name;
  }

  public String getLocalizedName() {
    return getLocalizedLevelName();
  }

  final String getLevelName() {
    return this.name;
  }

  private String computeLocalizedLevelName(Locale newLocale) {
    ResourceBundle rb = ResourceBundle.getBundle(resourceBundleName, newLocale);
    final String localizedName = rb.getString(name);
    final boolean isDefaultBundle = kDefaultBundle.equals(resourceBundleName);
    if (!isDefaultBundle) {
      return localizedName;
    }
    final Locale rbLocale = rb.getLocale();
    final Locale locale = Locale.ROOT.equals(rbLocale) || name.equals(localizedName.toUpperCase(Locale.ROOT))
        ? Locale.ROOT : rbLocale;
    return localizedName.toUpperCase(locale);
  }

  final String getCachedLocalizedLevelName() {
    if (localizedLevelName != null) {
      if (cachedLocale != null) {
        if (cachedLocale.equals(Locale.getDefault())) {
          return localizedLevelName;
        }
      }
    }
    if (resourceBundleName == null) {
      return name;
    }
    return null;
  }

  final synchronized String getLocalizedLevelName() {
    final String cachedLocalizedName = getCachedLocalizedLevelName();
    if (cachedLocalizedName != null) {
      return cachedLocalizedName;
    }
    final Locale newLocale = Locale.getDefault();
    try {
      localizedLevelName = computeLocalizedLevelName(newLocale);
    } catch (Exception ex) {
      localizedLevelName = name;
    }
    cachedLocale = newLocale;
    return localizedLevelName;
  }

  static synchronized Level findLevel(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    for (Level l : kKnownLevels) {
      if (name.equals(l.name)) {
        return l;
      }
    }
    try {
      int x = Integer.parseInt(name);
      for (Level l : kKnownLevels) {
        if (l.value == x) {
          return l;
        }
      }
      return new Level(name, x);
    } catch (NumberFormatException ex) {
      // not an integer value
    }
    for (Level l : kKnownLevels) {
      if (name.equals(l.getLocalizedLevelName())) {
        return l;
      }
    }
    return null;
  }

  @Override
  public final String toString() {
    return name;
  }

  public final int intValue() {
    return value;
  }

  private Object readResolve() {
    synchronized (Level.class) {
      for (Level l : kKnownLevels) {
        if (this.value == l.value && this.name.equals(l.name) && Objects.equals(this.resourceBundleName,
            l.resourceBundleName)) {
          return l;
        }
      }
    }
    return new Level(this.name, this.value, this.resourceBundleName);
  }

  public static synchronized Level parse(String name) throws IllegalArgumentException {
    name.length();
    for (Level l : kKnownLevels) {
      if (name.equals(l.name)) {
        return l;
      }
    }
    try {
      int x = Integer.parseInt(name);
      for (Level l : kKnownLevels) {
        if (l.value == x) {
          return l;
        }
      }
      return new Level(name, x);
    } catch (NumberFormatException ex) {
      // not an integer value
    }
    for (Level l : kKnownLevels) {
      if (name.equals(l.getLocalizedLevelName())) {
        return l;
      }
    }
    throw new IllegalArgumentException("Bad level \"" + name + "\"");
  }

  @Override
  public boolean equals(Object ox) {
    try {
      Level lx = (Level) ox;
      return lx.value == this.value;
    } catch (Exception ex) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return this.value;
  }
}
