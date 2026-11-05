/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.catalog;

class GroupEntry {
  public enum PreferType {
    PUBLIC,
    SYSTEM;

    public boolean prefer(String prefer) {
      return prefer != null && name().equalsIgnoreCase(prefer);
    }
  }

  GroupEntry() {}
}
