/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.ch;

import com.sun.nio.sctp.Association;

final class SctpAssociationImpl extends Association {
  SctpAssociationImpl(int assocId, int maxIn, int maxOut) {
    super(assocId, maxIn, maxOut);
  }
}
