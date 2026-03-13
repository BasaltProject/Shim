/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

@java.lang.annotation.Target(value = {java.lang.annotation.ElementType.TYPE}) @java.lang.annotation.Inherited() @java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME) public @interface Registered {
  public boolean value() default true;
}
