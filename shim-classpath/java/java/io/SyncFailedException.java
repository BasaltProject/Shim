/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.io;

public class SyncFailedException extends IOException {
  public SyncFailedException(String detailMessage) {
    super(detailMessage);
  }
}
