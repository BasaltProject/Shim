/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.security.auth.module;

public class NTSystem {
  private String userName;
  private String domain;
  private String domainSID;
  private String userSID;
  private String groupIDs[];
  private String primaryGroupID;
  private long impersonationToken;

  public NTSystem() {
    throw new UnsatisfiedLinkError("no jaas in java.library.path");
  }

  public String getName() {
    return userName;
  }

  public String getDomain() {
    return domain;
  }

  public String getDomainSID() {
    return domainSID;
  }

  public String getUserSID() {
    return userSID;
  }

  public String getPrimaryGroupID() {
    return primaryGroupID;
  }

  public String[] getGroupIDs() {
    return groupIDs == null ? null : groupIDs.clone();
  }

  public synchronized long getImpersonationToken() {
    return impersonationToken;
  }
}
