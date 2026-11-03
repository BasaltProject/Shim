/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute;

public interface PrintRequestAttributeSet extends javax.print.attribute.AttributeSet {
  boolean add(javax.print.attribute.Attribute p0);
  boolean addAll(javax.print.attribute.AttributeSet p0);
}
