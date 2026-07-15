/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.datatransfer;

public interface ClipboardOwner {
  void lostOwnership(java.awt.datatransfer.Clipboard p0, java.awt.datatransfer.Transferable p1);
}
