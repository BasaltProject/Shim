/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.util.Set;

public interface X509Extension {
  boolean hasUnsupportedCriticalExtension();

  Set<String> getCriticalExtensionOIDs();

  Set<String> getNonCriticalExtensionOIDs();

  byte[] getExtensionValue(String oid);
}
