/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.util.Iterator;
import java.util.Set;

public interface PolicyNode {
  PolicyNode getParent();

  Iterator<? extends PolicyNode> getChildren();

  int getDepth();

  String getValidPolicy();

  Set<? extends PolicyQualifierInfo> getPolicyQualifiers();

  Set<String> getExpectedPolicies();

  boolean isCritical();
}
