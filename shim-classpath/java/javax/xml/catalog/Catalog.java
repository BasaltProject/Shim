/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.catalog;

public interface Catalog {
  java.util.stream.Stream<javax.xml.catalog.Catalog> catalogs();
  java.lang.String matchPublic(java.lang.String string);
  java.lang.String matchSystem(java.lang.String string);
  java.lang.String matchURI(java.lang.String string);
}
