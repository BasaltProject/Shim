/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

public class NonReadableChannelException extends IllegalStateException {
  private static final long serialVersionUID = -3200915679294993514L;

  public NonReadableChannelException() {
    super();
  }
}
