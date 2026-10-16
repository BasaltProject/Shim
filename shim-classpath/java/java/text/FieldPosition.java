/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.text;

public class FieldPosition {
  int field = 0;
  int endIndex = 0;
  int beginIndex = 0;

  public FieldPosition(int field) {
    this.field = field;
  }

  public int getField() {
    return field;
  }

  public int getBeginIndex() {
    return beginIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

  public void setBeginIndex(int bi) {
    beginIndex = bi;
  }

  public void setEndIndex(int ei) {
    endIndex = ei;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof FieldPosition)) {
      return false;
    }
    FieldPosition other = (FieldPosition) obj;
    return beginIndex == other.beginIndex && endIndex == other.endIndex && field == other.field;
  }

  @Override
  public int hashCode() {
    return (field << 24) | (beginIndex << 16) | endIndex;
  }

  @Override
  public String toString() {
    return getClass().getName() + "[field=" + field + ",attribute=null,beginIndex=" + beginIndex + ",endIndex="
        + endIndex + ']';
  }
}
