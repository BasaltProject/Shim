/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth;

import java.security.DomainCombiner;
import java.security.ProtectionDomain;

public class SubjectDomainCombiner implements DomainCombiner {
  private final Subject subject;

  public SubjectDomainCombiner(Subject subject) {
    this.subject = subject;
  }

  public Subject getSubject() {
    return subject;
  }

  public ProtectionDomain[] combine(ProtectionDomain[] currentDomains, ProtectionDomain[] assignedDomains) {
    if (currentDomains == null || currentDomains.length == 0) {
      return assignedDomains == null ? null : assignedDomains.clone();
    }
    return currentDomains.clone();
  }
}
