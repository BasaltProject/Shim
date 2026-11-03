/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

public interface Attribute extends java.io.Serializable {
  java.lang.String getName();
  java.lang.Class<? extends javax.print.attribute.Attribute> getCategory();
}
