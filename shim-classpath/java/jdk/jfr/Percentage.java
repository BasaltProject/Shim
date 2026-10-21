/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

@jdk.jfr.MetadataDefinition() @jdk.jfr.ContentType() @jdk.jfr.Label(value = "Percentage") @jdk.jfr.Description(value = "Percentage, represented as a number between 0 and 1") @java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME) @java.lang.annotation.Target(value = {java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD}) public @interface Percentage {
}
