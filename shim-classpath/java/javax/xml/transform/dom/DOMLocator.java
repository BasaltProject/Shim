/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform.dom;

public interface DOMLocator extends javax.xml.transform.SourceLocator {
  org.w3c.dom.Node getOriginatingNode();
}
