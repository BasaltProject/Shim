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

public final class UnixDomainPrincipal {
  private final UserPrincipal user;
  private final GroupPrincipal group;

  public UnixDomainPrincipal(UserPrincipal user, GroupPrincipal group) {
    this.user = Objects.requireNonNull(user);
    this.group = Objects.requireNonNull(group);
  }

  public UserPrincipal user() {
    return user;
  }

  public GroupPrincipal group() {
    return group;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnixDomainPrincipal)) {
      return false;
    }
    UnixDomainPrincipal other = (UnixDomainPrincipal) o;
    return user.equals(other.user) && group.equals(other.group);
  }

  @Override
  public int hashCode() {
    return 31 * user.hashCode() + group.hashCode();
  }

  @Override
  public String toString() {
    return "UnixDomainPrincipal[user=" + user + ", group=" + group + "]";
  }
}
