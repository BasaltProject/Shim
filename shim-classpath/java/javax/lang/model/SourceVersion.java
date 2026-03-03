/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model;

public enum SourceVersion {
  RELEASE_0, RELEASE_1, RELEASE_2, RELEASE_3, RELEASE_4, RELEASE_5, RELEASE_6, RELEASE_7, RELEASE_8, RELEASE_9,
  RELEASE_10, RELEASE_11, RELEASE_12, RELEASE_13, RELEASE_14, RELEASE_15, RELEASE_16, RELEASE_17, RELEASE_18,
  RELEASE_19, RELEASE_20, RELEASE_21, RELEASE_22, RELEASE_23, RELEASE_24, RELEASE_25;

  public static SourceVersion latest() {
    return RELEASE_25;
  }

  public static SourceVersion latestSupported() {
    return RELEASE_25;
  }

  public static SourceVersion valueOf(java.lang.Runtime.Version rv) {
    return valueOf("RELEASE_" + rv.feature());
  }

  public java.lang.Runtime.Version runtimeVersion() {
    return ordinal() >= 6 ? java.lang.Runtime.Version.parse(Integer.toString(ordinal())) : null;
  }
}
