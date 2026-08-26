/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.catalog;

import java.util.EnumMap;
import java.util.Map;

public class CatalogFeatures {
  public enum Feature {
    FILES("javax.xml.catalog.files", null),
    PREFER("javax.xml.catalog.prefer", "public"),
    DEFER("javax.xml.catalog.defer", "true"),
    RESOLVE("javax.xml.catalog.resolve", "strict");

    private final String property;
    private final String defaultValue;

    Feature(String property, String defaultValue) {
      this.property = property;
      this.defaultValue = defaultValue;
    }

    public String getPropertyName() {
      return property;
    }

    public String defaultValue() {
      return defaultValue;
    }

    boolean isValid(String value) {
      if (this == FILES) {
        return true;
      }
      if (this == PREFER) {
        return value.equals("system") || value.equals("public");
      }
      if (this == DEFER) {
        return value.equals("true") || value.equals("false");
      }
      return value.equals("strict") || value.equals("continue") || value.equals("ignore");
    }
  }

  private final Map<Feature, String> values = new EnumMap<Feature, String>(Feature.class);

  private CatalogFeatures(Map<Feature, String> explicit) {
    for (Feature f : Feature.values()) {
      String v = explicit.get(f);
      if (v == null) {
        v = System.getProperty(f.getPropertyName());
      }
      if (v == null) {
        v = f.defaultValue();
      }
      values.put(f, v);
    }
  }

  public static CatalogFeatures defaults() {
    return new CatalogFeatures(new EnumMap<Feature, String>(Feature.class));
  }

  public static Builder builder() {
    return new Builder();
  }

  public String get(Feature cf) {
    if (cf == null) {
      throw new NullPointerException();
    }
    return values.get(cf);
  }

  public static class Builder {
    private final Map<Feature, String> explicit = new EnumMap<Feature, String>(Feature.class);

    private Builder() {
    }

    public Builder with(Feature feature, String value) {
      if (feature == null) {
        throw new NullPointerException("JAXP09020006: The argument 'Feature' can not be null.");
      }
      if (value == null) {
        throw new NullPointerException("JAXP09020006: The argument 'value' can not be null.");
      }
      if (!feature.isValid(value)) {
        throw new IllegalArgumentException(CatalogImpl.badArg(value, feature.name()));
      }
      explicit.put(feature, value);
      return this;
    }

    public CatalogFeatures build() {
      return new CatalogFeatures(explicit);
    }
  }
}
