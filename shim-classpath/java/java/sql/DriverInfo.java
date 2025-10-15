/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.sql;

class DriverInfo {
  final Driver driver;
  DriverAction da;

  DriverInfo(Driver driver, DriverAction action) {
    this.driver = driver;
    da = action;
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof DriverInfo) && this.driver == ((DriverInfo) other).driver;
  }

  @Override
  public int hashCode() {
    return driver.hashCode();
  }

  @Override
  public String toString() {
    return "driver[className=" + driver + "]";
  }

  DriverAction action() {
    return da;
  }
}
