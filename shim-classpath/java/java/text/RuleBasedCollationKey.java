/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

final class RuleBasedCollationKey extends CollationKey {
  private final String key;

  RuleBasedCollationKey(String source, String key) {
    super(source);
    this.key = key;
  }

  @Override
  public int compareTo(CollationKey target) {
    int result = key.compareTo(((RuleBasedCollationKey) target).key);
    if (result <= Integer.MIN_VALUE) {
      return -1;
    } else if (result >= Integer.MAX_VALUE) {
      return 1;
    }
    return result < 0 ? -1 : result > 0 ? 1 : 0;
  }

  @Override
  public boolean equals(Object target) {
    if (this == target) {
      return true;
    }
    if (target == null || !getClass().equals(target.getClass())) {
      return false;
    }
    return key.equals(((RuleBasedCollationKey) target).key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public byte[] toByteArray() {
    char[] src = key.toCharArray();
    byte[] dest = new byte[2 * src.length];
    int j = 0;
    for (int i = 0; i < src.length; i++) {
      dest[j++] = (byte) (src[i] >>> 8);
      dest[j++] = (byte) (src[i] & 0x00ff);
    }
    return dest;
  }
}
