/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

import java.util.Arrays;

public class BatchUpdateException extends SQLException {
  private static final long serialVersionUID = 5977529877145521757L;

  private int[] updateCounts;
  private long[] longUpdateCounts;

  public BatchUpdateException(String reason, String SQLState, int vendorCode, int[] updateCounts) {
    super(reason, SQLState, vendorCode);
    this.updateCounts = (updateCounts == null) ? null : Arrays.copyOf(updateCounts, updateCounts.length);
    this.longUpdateCounts = (updateCounts == null) ? null : copyUpdateCount(updateCounts);
  }

  public BatchUpdateException(String reason, String SQLState, int[] updateCounts) {
    this(reason, SQLState, 0, updateCounts);
  }

  public BatchUpdateException(String reason, int[] updateCounts) {
    this(reason, null, 0, updateCounts);
  }

  public BatchUpdateException(int[] updateCounts) {
    this(null, null, 0, updateCounts);
  }

  public BatchUpdateException() {
    this(null, null, 0, null);
  }

  public BatchUpdateException(Throwable cause) {
    this(cause == null ? null : cause.toString(), null, 0, (int[]) null, cause);
  }

  public BatchUpdateException(int[] updateCounts, Throwable cause) {
    this(cause == null ? null : cause.toString(), null, 0, updateCounts, cause);
  }

  public BatchUpdateException(String reason, int[] updateCounts, Throwable cause) {
    this(reason, null, 0, updateCounts, cause);
  }

  public BatchUpdateException(String reason, String SQLState, int[] updateCounts, Throwable cause) {
    this(reason, SQLState, 0, updateCounts, cause);
  }

  public BatchUpdateException(String reason, String SQLState, int vendorCode, int[] updateCounts, Throwable cause) {
    super(reason, SQLState, vendorCode, cause);
    this.updateCounts = (updateCounts == null) ? null : Arrays.copyOf(updateCounts, updateCounts.length);
    this.longUpdateCounts = (updateCounts == null) ? null : copyUpdateCount(updateCounts);
  }

  public BatchUpdateException(String reason, String SQLState, int vendorCode, long[] longUpdateCounts,
                              Throwable cause) {
    super(reason, SQLState, vendorCode, cause);
    this.longUpdateCounts = (longUpdateCounts == null) ? null : Arrays.copyOf(longUpdateCounts,
        longUpdateCounts.length);
    this.updateCounts = (longUpdateCounts == null) ? null : copyUpdateCount(longUpdateCounts);
  }

  public int[] getUpdateCounts() {
    return (updateCounts == null) ? null : Arrays.copyOf(updateCounts, updateCounts.length);
  }

  public long[] getLargeUpdateCounts() {
    return (longUpdateCounts == null) ? null : Arrays.copyOf(longUpdateCounts, longUpdateCounts.length);
  }

  private static long[] copyUpdateCount(int[] counts) {
    long[] copy = new long[counts.length];
    for (int i = 0; i < counts.length; i++) {
      copy[i] = counts[i];
    }
    return copy;
  }

  private static int[] copyUpdateCount(long[] counts) {
    int[] copy = new int[counts.length];
    for (int i = 0; i < counts.length; i++) {
      copy[i] = (int) counts[i];
    }
    return copy;
  }
}
