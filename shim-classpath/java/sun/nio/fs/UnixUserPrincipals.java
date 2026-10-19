/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.nio.fs;

import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import shim.NioNative;

public final class UnixUserPrincipals {
  private UnixUserPrincipals() { }

  public static class User implements UserPrincipal {
    private final int id;
    private final boolean isGroup;
    private final String name;

    private User(int id, boolean isGroup, String name) {
      this.id = id;
      this.isGroup = isGroup;
      this.name = name;
    }

    User(int id, String name) {
      this(id, false, name);
    }

    int uid() {
      if (isGroup) {
        throw new AssertionError();
      }
      return id;
    }

    int gid() {
      if (isGroup) {
        return id;
      }
      throw new AssertionError();
    }

    boolean isSpecial() {
      return id == -1;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof User)) {
        return false;
      }
      User other = (User) obj;
      if ((this.id != other.id) || (this.isGroup != other.isGroup)) {
        return false;
      }
      if (this.id == -1) {
        return this.name.equals(other.name);
      }
      return true;
    }

    @Override
    public int hashCode() {
      return (id != -1) ? id : name.hashCode();
    }
  }

  public static class Group extends User implements GroupPrincipal {
    Group(int id, String name) {
      super(id, true, name);
    }
  }

  public static User fromUid(int uid) {
    String name = NioNative.userName(uid);
    if (name == null) {
      name = Integer.toString(uid);
    }
    return new User(uid, name);
  }

  public static Group fromGid(int gid) {
    String name = NioNative.groupName(gid);
    if (name == null) {
      name = Integer.toString(gid);
    }
    return new Group(gid, name);
  }

  private static int lookupName(String name, boolean isGroup) throws IOException {
    long id = isGroup ? NioNative.groupId(name) : NioNative.userId(name);
    if (id < 0) {
      int num = -1;
      try {
        num = Integer.parseInt(name);
      } catch (NumberFormatException ignore) {
      }
      if (num >= 0) {
        return num;
      }
      throw new UserPrincipalNotFoundException(name);
    }
    return (int) id;
  }

  static UserPrincipal lookupUser(String name) throws IOException {
    int id = lookupName(name, false);
    return new User(id, name);
  }

  static GroupPrincipal lookupGroup(String group) throws IOException {
    int gid = lookupName(group, true);
    return new Group(gid, group);
  }

  static final UserPrincipalLookupService LOOKUP_SERVICE = new UserPrincipalLookupService() {
    @Override
    public UserPrincipal lookupPrincipalByName(String name) throws IOException {
      return lookupUser(name);
    }

    @Override
    public GroupPrincipal lookupPrincipalByGroupName(String group) throws IOException {
      return lookupGroup(group);
    }
  };
}
