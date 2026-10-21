/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr;

@jdk.jfr.MetadataDefinition() @jdk.jfr.Label(value = "Experimental") @jdk.jfr.Description(value = "Element is not to be shown to a user by default") @java.lang.annotation.Inherited() @java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME) @java.lang.annotation.Target(value = {java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.TYPE}) public @interface Experimental {
}
