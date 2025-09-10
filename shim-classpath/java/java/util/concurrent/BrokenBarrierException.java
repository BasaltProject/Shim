/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.concurrent;

public class BrokenBarrierException extends Exception {
  private static final long serialVersionUID = 7117394618823254244L;

  public BrokenBarrierException() {
    super();
  }

  public BrokenBarrierException(String message) {
    super(message);
  }
}
