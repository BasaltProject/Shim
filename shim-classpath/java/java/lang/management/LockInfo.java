/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.management;

import javax.management.openmbean.CompositeData;

public class LockInfo {
  private final String className;
  private final int identityHashCode;

  public LockInfo(String className, int identityHashCode) {
    if (className == null) {
      throw new NullPointerException("Parameter className cannot be null");
    }
    this.className = className;
    this.identityHashCode = identityHashCode;
  }

  LockInfo(Object lock) {
    this.className = lock.getClass().getName();
    this.identityHashCode = System.identityHashCode(lock);
  }

  public String getClassName() {
    return className;
  }

  public int getIdentityHashCode() {
    return identityHashCode;
  }

  public static LockInfo from(CompositeData cd) {
    if (cd == null) {
      return null;
    }
    return new LockInfo((String) cd.get("className"), ((Integer) cd.get("identityHashCode")).intValue());
  }

  @Override
  public String toString() {
    return className + '@' + Integer.toHexString(identityHashCode);
  }
}
