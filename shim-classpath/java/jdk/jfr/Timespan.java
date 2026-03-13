/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

@jdk.jfr.MetadataDefinition() @jdk.jfr.ContentType() @jdk.jfr.Label(value = "Timespan") @jdk.jfr.Description(value = "A duration, measured in nanoseconds by default") @java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME) @java.lang.annotation.Target(value = {java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD}) public @interface Timespan {
  public static final java.lang.String TICKS = "TICKS";
  public static final java.lang.String SECONDS = "SECONDS";
  public static final java.lang.String MILLISECONDS = "MILLISECONDS";
  public static final java.lang.String NANOSECONDS = "NANOSECONDS";
  public static final java.lang.String MICROSECONDS = "MICROSECONDS";
  public java.lang.String value() default "NANOSECONDS";
}
