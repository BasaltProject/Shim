/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

@jdk.jfr.MetadataDefinition() public abstract class SettingControl {
  protected SettingControl() { throw new UnsupportedOperationException("Flight Recorder is not available"); }
  public abstract java.lang.String getValue();
  public abstract void setValue(java.lang.String arg0);
  public abstract java.lang.String combine(java.util.Set<java.lang.String> arg0);
}
