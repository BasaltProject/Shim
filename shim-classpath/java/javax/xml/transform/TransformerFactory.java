/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.transform;

import shim.xml.FactoryFinder;

public abstract class TransformerFactory {
  private static final String kDefault = "shim.xml.transform.TransformerFactoryImpl";

  protected TransformerFactory() {
  }

  public static TransformerFactory newDefaultInstance() {
    return FactoryFinder.instantiate(TransformerFactory.class, kDefault, TransformerFactory.class.getClassLoader());
  }

  public static TransformerFactory newInstance() throws TransformerFactoryConfigurationError {
    try {
      return FactoryFinder.find(TransformerFactory.class, kDefault, null);
    } catch (FactoryFinder.FactoryError e) {
      throw new TransformerFactoryConfigurationError(e, e.getMessage());
    }
  }

  public static TransformerFactory newInstance(String factoryClassName, ClassLoader classLoader)
      throws TransformerFactoryConfigurationError {
    if (factoryClassName == null) {
      throw new TransformerFactoryConfigurationError("factoryClassName cannot be null.");
    }
    try {
      return FactoryFinder.instantiate(TransformerFactory.class, factoryClassName, classLoader);
    } catch (FactoryFinder.FactoryError e) {
      throw new TransformerFactoryConfigurationError(e, e.getMessage());
    }
  }

  public abstract Transformer newTransformer(Source source) throws TransformerConfigurationException;

  public abstract Transformer newTransformer() throws TransformerConfigurationException;

  public abstract Templates newTemplates(Source source) throws TransformerConfigurationException;

  public abstract Source getAssociatedStylesheet(Source source, String media, String title, String charset)
      throws TransformerConfigurationException;

  public abstract void setURIResolver(URIResolver resolver);

  public abstract URIResolver getURIResolver();

  public abstract void setFeature(String name, boolean value) throws TransformerConfigurationException;

  public abstract boolean getFeature(String name);

  public abstract void setAttribute(String name, Object value);

  public abstract Object getAttribute(String name);

  public abstract void setErrorListener(ErrorListener listener);

  public abstract ErrorListener getErrorListener();
}
