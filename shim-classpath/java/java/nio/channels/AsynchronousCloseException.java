/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.channels;

public class AsynchronousCloseException extends ClosedChannelException {
  private static final long serialVersionUID = 6891178312432313966L;

  public AsynchronousCloseException() {
    super();
  }

  public AsynchronousCloseException(String message) {
    super(message);
  }
}
