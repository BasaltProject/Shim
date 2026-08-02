/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing.text;

public interface Position {
  int getOffset();

  public static final class Bias {
    public static final Bias Forward = new Bias("Forward");
    public static final Bias Backward = new Bias("Backward");

    private final String name;

    private Bias(String name) { this.name = name; }

    public String toString() { return name; }
  }
}
