/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public interface DomainCombiner {
  ProtectionDomain[] combine(ProtectionDomain[] currentDomains, ProtectionDomain[] assignedDomains);
}
