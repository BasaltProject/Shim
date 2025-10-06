/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.interfaces;

public interface PBEKey extends javax.crypto.SecretKey {
  @Deprecated
  long serialVersionUID = -1430015993304333921L;

  char[] getPassword();

  byte[] getSalt();

  int getIterationCount();
}
