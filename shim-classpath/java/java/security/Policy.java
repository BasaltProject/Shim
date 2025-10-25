/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

public abstract class Policy {
  public static final PermissionCollection UNSUPPORTED_EMPTY_COLLECTION = new UnsupportedEmptyCollection();

  private static final Policy kDefault = new Policy() {
  };

  public interface Parameters {
  }

  protected Policy() {
  }

  public static Policy getPolicy() {
    return kDefault;
  }

  public static void setPolicy(Policy p) {
    throw new UnsupportedOperationException("Setting a system-wide Policy object is not supported");
  }

  public static Policy getInstance(String type, Policy.Parameters params) throws NoSuchAlgorithmException {
    if (type == null) {
      throw new NullPointerException("null type name");
    }
    throw new NoSuchAlgorithmException(type + " Policy not available");
  }

  public static Policy getInstance(String type, Policy.Parameters params, String provider)
      throws NoSuchProviderException, NoSuchAlgorithmException {
    if (provider == null || provider.isEmpty()) {
      throw new IllegalArgumentException("missing provider");
    }
    throw new NoSuchProviderException("no such provider: " + provider);
  }

  public static Policy getInstance(String type, Policy.Parameters params, Provider provider)
      throws NoSuchAlgorithmException {
    if (provider == null) {
      throw new IllegalArgumentException("missing provider");
    }
    return getInstance(type, params);
  }

  public Provider getProvider() {
    return null;
  }

  public String getType() {
    return null;
  }

  public Policy.Parameters getParameters() {
    return null;
  }

  public PermissionCollection getPermissions(CodeSource codesource) {
    return UNSUPPORTED_EMPTY_COLLECTION;
  }

  public PermissionCollection getPermissions(ProtectionDomain domain) {
    return UNSUPPORTED_EMPTY_COLLECTION;
  }

  public boolean implies(ProtectionDomain domain, Permission permission) {
    return false;
  }

  public void refresh() {
  }

  private static final class UnsupportedEmptyCollection extends PermissionCollection {
    private static final long serialVersionUID = -8492269157353014774L;

    private final Permissions perms = new Permissions();

    UnsupportedEmptyCollection() {
      perms.setReadOnly();
    }

    @Override
    public void add(Permission permission) {
      perms.add(permission);
    }

    @Override
    public boolean implies(Permission permission) {
      return perms.implies(permission);
    }

    @Override
    public java.util.Enumeration<Permission> elements() {
      return perms.elements();
    }
  }
}
