/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.catalog;

public class CatalogException extends java.lang.RuntimeException {
  private static final long serialVersionUID = 653231525876459057L;


  public CatalogException(java.lang.String string) {
    super(string);
  }
  public CatalogException(java.lang.String string, java.lang.Throwable throwable) {
    super(string, throwable);
  }

}
