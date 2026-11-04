/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.catalog;

import java.net.URI;

public final class CatalogManager {
  private CatalogManager() {
  }

  public static Catalog catalog(CatalogFeatures features, URI... uris) {
    if (features == null) {
      throw new NullPointerException("JAXP09020006: The argument 'CatalogFeatures' can not be null.");
    }
    return new CatalogImpl(features, uris);
  }

  public static CatalogResolver catalogResolver(CatalogFeatures features, URI... uris) {
    return new CatalogResolverImpl(catalog(features, uris), notFoundAction(features));
  }

  public static CatalogResolver catalogResolver(Catalog catalog, CatalogResolver.NotFoundAction action) {
    if (catalog == null || action == null) {
      throw new NullPointerException();
    }
    return new CatalogResolverImpl(catalog, action);
  }

  public static CatalogResolver catalogResolver(Catalog catalog) {
    if (catalog == null) {
      throw new NullPointerException();
    }
    return new CatalogResolverImpl(catalog, CatalogResolver.NotFoundAction.STRICT);
  }

  private static CatalogResolver.NotFoundAction notFoundAction(CatalogFeatures features) {
    return CatalogResolver.NotFoundAction.getType(features.get(CatalogFeatures.Feature.RESOLVE));
  }
}
