/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package jdk.net;

import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.util.Objects;

public record UnixDomainPrincipal(UserPrincipal user, GroupPrincipal group) {
  public UnixDomainPrincipal {
    Objects.requireNonNull(user);
    Objects.requireNonNull(group);
  }
}
