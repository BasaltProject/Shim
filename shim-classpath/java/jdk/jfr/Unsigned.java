/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

@jdk.jfr.MetadataDefinition() @jdk.jfr.ContentType() @jdk.jfr.Label(value = "Unsigned Value") @jdk.jfr.Description(value = "Value should be interpreted as unsigned data type") @java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME) @java.lang.annotation.Target(value = {java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.TYPE}) public @interface Unsigned {
}
