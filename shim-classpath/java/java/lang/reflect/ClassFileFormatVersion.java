/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

import java.util.Objects;

public enum ClassFileFormatVersion {
  RELEASE_0(45),
  RELEASE_1(45),
  RELEASE_2(46),
  RELEASE_3(47),
  RELEASE_4(48),
  RELEASE_5(49),
  RELEASE_6(50),
  RELEASE_7(51),
  RELEASE_8(52),
  RELEASE_9(53),
  RELEASE_10(54),
  RELEASE_11(55),
  RELEASE_12(56),
  RELEASE_13(57),
  RELEASE_14(58),
  RELEASE_15(59),
  RELEASE_16(60),
  RELEASE_17(61),
  RELEASE_18(62),
  RELEASE_19(63),
  RELEASE_20(64),
  RELEASE_21(65),
  RELEASE_22(66),
  RELEASE_23(67),
  RELEASE_24(68),
  RELEASE_25(69);

  private final int major;

  ClassFileFormatVersion(int major) {
    this.major = major;
  }

  public int major() {
    return major;
  }

  public static ClassFileFormatVersion latest() {
    return RELEASE_25;
  }

  public static ClassFileFormatVersion fromMajor(int major) {
    if (major < 45 || major > latest().major()) {
      throw new IllegalArgumentException("Out of range major class file version " + major);
    }
    return values()[major - 44];
  }

  public static ClassFileFormatVersion valueOf(Runtime.Version rv) {
    Objects.requireNonNull(rv);
    return valueOf("RELEASE_" + rv.feature());
  }

  public Runtime.Version runtimeVersion() {
    if (compareTo(RELEASE_6) < 0) {
      return null;
    }
    return Runtime.Version.parse(Integer.toString(ordinal()));
  }
}
