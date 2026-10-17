/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

public abstract class CollationKey implements Comparable<CollationKey> {
  private final String source;

  protected CollationKey(String source) {
    if (source == null) {
      throw new NullPointerException();
    }
    this.source = source;
  }

  @Override
  public abstract int compareTo(CollationKey target);

  public String getSourceString() {
    return source;
  }

  public abstract byte[] toByteArray();
}
