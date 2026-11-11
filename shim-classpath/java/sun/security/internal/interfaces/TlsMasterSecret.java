/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.internal.interfaces;

import javax.crypto.SecretKey;

public interface TlsMasterSecret extends SecretKey {
  long serialVersionUID = 1019571680375368880L;

  int getMajorVersion();

  int getMinorVersion();
}
