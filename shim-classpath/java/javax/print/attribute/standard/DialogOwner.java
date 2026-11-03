/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.print.attribute.standard;

public final class DialogOwner implements javax.print.attribute.PrintRequestAttribute {
  private java.awt.Window owner;
  public DialogOwner(java.awt.Window p0) {
    owner = p0;
  }
  public DialogOwner() {
  }
  public final java.lang.String getName() { return "dialog-owner"; }
  public java.awt.Window getOwner() { return owner; }
  public final java.lang.Class<? extends javax.print.attribute.Attribute> getCategory() { return DialogOwner.class; }
}
