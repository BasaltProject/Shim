/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

public final class StandardWatchEventKinds {
  private StandardWatchEventKinds() { }

  private static final class StdKind<T> implements WatchEvent.Kind<T> {
    private final String name;
    private final Class<T> type;

    StdKind(String name, Class<T> type) {
      this.name = name;
      this.type = type;
    }

    public String name() {
      return name;
    }

    public Class<T> type() {
      return type;
    }

    public String toString() {
      return name;
    }
  }

  public static final WatchEvent.Kind<Object> OVERFLOW = new StdKind<Object>("OVERFLOW", Object.class);
  public static final WatchEvent.Kind<Path> ENTRY_CREATE = new StdKind<Path>("ENTRY_CREATE", Path.class);
  public static final WatchEvent.Kind<Path> ENTRY_DELETE = new StdKind<Path>("ENTRY_DELETE", Path.class);
  public static final WatchEvent.Kind<Path> ENTRY_MODIFY = new StdKind<Path>("ENTRY_MODIFY", Path.class);
}
