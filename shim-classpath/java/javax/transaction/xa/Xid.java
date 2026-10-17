/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.transaction.xa;

public interface Xid {
  int MAXGTRIDSIZE = 64;
  int MAXBQUALSIZE = 64;

  int getFormatId();

  byte[] getGlobalTransactionId();

  byte[] getBranchQualifier();
}
