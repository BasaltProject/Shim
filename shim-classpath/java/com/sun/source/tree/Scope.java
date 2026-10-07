/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.source.tree;

public interface Scope {
  javax.lang.model.element.TypeElement getEnclosingClass();
  javax.lang.model.element.ExecutableElement getEnclosingMethod();
  com.sun.source.tree.Scope getEnclosingScope();
  java.lang.Iterable<? extends javax.lang.model.element.Element> getLocalElements();
}
