/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public abstract class AbstractResourceBundleProvider implements ResourceBundleProvider {
  private static final String FORMAT_CLASS = "java.class";
  private static final String FORMAT_PROPERTIES = "java.properties";

  private final String[] formats;

  protected AbstractResourceBundleProvider() {
    this(FORMAT_PROPERTIES);
  }

  protected AbstractResourceBundleProvider(String... formats) {
    this.formats = formats.clone();
    for (String f : formats) {
      if (!f.equals(FORMAT_CLASS) && !f.equals(FORMAT_PROPERTIES)) {
        throw new IllegalArgumentException(f);
      }
    }
  }

  protected String toBundleName(String baseName, Locale locale) {
    return ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT).toBundleName(baseName, locale);
  }

  @Override
  public ResourceBundle getBundle(String baseName, Locale locale) {
    Objects.requireNonNull(baseName);
    Objects.requireNonNull(locale);
    String bundleName = toBundleName(baseName, locale);
    for (String format : formats) {
      try {
        if (format.equals(FORMAT_CLASS)) {
          Class<?> c = Class.forName(bundleName, false, getClass().getClassLoader());
          if (ResourceBundle.class.isAssignableFrom(c)) {
            return (ResourceBundle) c.getDeclaredConstructor().newInstance();
          }
        } else {
          String resource = bundleName.replace('.', '/') + ".properties";
          InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
          if (in != null) {
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
              return new PropertyResourceBundle(r);
            }
          }
        }
      } catch (IOException | ReflectiveOperationException e) {
        throw new java.util.MissingResourceException(e.toString(), bundleName, "");
      }
    }
    return null;
  }
}
