/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.constant;

public interface DirectMethodHandleDesc extends MethodHandleDesc {

  enum Kind {
    STATIC(6, false),
    INTERFACE_STATIC(6, true),
    VIRTUAL(5, false),
    INTERFACE_VIRTUAL(9, true),
    SPECIAL(7, false),
    INTERFACE_SPECIAL(7, true),
    CONSTRUCTOR(8, false),
    GETTER(1, false),
    SETTER(2, false),
    STATIC_GETTER(3, false),
    STATIC_SETTER(4, false);

    public final int refKind;
    public final boolean isInterface;

    Kind(int refKind, boolean isInterface) {
      this.refKind = refKind;
      this.isInterface = isInterface;
    }

    public static Kind valueOf(int refKind) {
      return valueOf(refKind, false);
    }

    public static Kind valueOf(int refKind, boolean isInterface) {
      for (Kind k : values()) {
        if (k.refKind == refKind && k.isInterface == isInterface) {
          return k;
        }
      }
      throw new IllegalArgumentException("Unknown kind: refKind=" + refKind + ", isInterface=" + isInterface);
    }
  }

  Kind kind();

  int refKind();

  boolean isOwnerInterface();

  ClassDesc owner();

  String methodName();
}
