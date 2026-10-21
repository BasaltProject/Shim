/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

@java.lang.annotation.Target(value = {java.lang.annotation.ElementType.TYPE}) @java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME) @java.lang.annotation.Inherited() @jdk.jfr.MetadataDefinition() public @interface Enabled {
  public static final java.lang.String NAME = "enabled";
  public boolean value() default true;
}
