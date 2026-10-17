/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom;

public interface DOMImplementation {
  boolean hasFeature(String p0, String p1);

  DocumentType createDocumentType(String p0, String p1, String p2) throws DOMException;

  Document createDocument(String p0, String p1, DocumentType p2) throws DOMException;

  Object getFeature(String p0, String p1);
}
