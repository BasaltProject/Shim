/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.xpath;

import java.util.Iterator;
import java.util.ServiceLoader;
import shim.xml.FactoryFinder;

public abstract class XPathFactory {
  public static final String DEFAULT_PROPERTY_NAME = "javax.xml.xpath.XPathFactory";
  public static final String DEFAULT_OBJECT_MODEL_URI = "http://java.sun.com/jaxp/xpath/dom";
  private static final String kDefault = "shim.xml.xpath.XPathFactoryImpl";

  protected XPathFactory() {
  }

  public static XPathFactory newDefaultInstance() {
    return FactoryFinder.instantiate(XPathFactory.class, kDefault, XPathFactory.class.getClassLoader());
  }

  public static XPathFactory newInstance() {
    try {
      return newInstance(DEFAULT_OBJECT_MODEL_URI);
    } catch (XPathFactoryConfigurationException xpathFactoryConfigurationException) {
      throw new RuntimeException("XPathFactory#newInstance() failed to create an XPathFactory for the default object model: "
          + DEFAULT_OBJECT_MODEL_URI + " with the XPathFactoryConfigurationException: "
          + xpathFactoryConfigurationException.toString());
    }
  }

  public static XPathFactory newInstance(final String uri) throws XPathFactoryConfigurationException {
    if (uri == null) {
      throw new NullPointerException("XPathFactory#newInstance(String uri) cannot be called with uri == null");
    }
    if (uri.length() == 0) {
      throw new IllegalArgumentException("XPathFactory#newInstance(String uri) cannot be called with uri == \"\"");
    }
    XPathFactory factory = null;
    String name = System.getProperty(DEFAULT_PROPERTY_NAME + ":" + uri);
    if (name == null) {
      name = System.getProperty(DEFAULT_PROPERTY_NAME);
    }
    if (name != null) {
      try {
        factory = FactoryFinder.instantiate(XPathFactory.class, name, null);
      } catch (FactoryFinder.FactoryError e) {
        throw new XPathFactoryConfigurationException(e);
      }
      if (factory != null && factory.isObjectModelSupported(uri)) {
        return factory;
      }
    }
    try {
      Iterator<XPathFactory> it = ServiceLoader.load(XPathFactory.class).iterator();
      while (it.hasNext()) {
        XPathFactory candidate = it.next();
        if (candidate.isObjectModelSupported(uri)) {
          return candidate;
        }
      }
    } catch (Throwable ignored) {
    }
    XPathFactory builtin = newDefaultInstance();
    if (builtin.isObjectModelSupported(uri)) {
      return builtin;
    }
    throw new XPathFactoryConfigurationException("No XPathFactory implementation found for the object model: " + uri);
  }

  public static XPathFactory newInstance(final String uri, final String factoryClassName,
                                         ClassLoader classLoader) throws XPathFactoryConfigurationException {
    if (uri == null) {
      throw new NullPointerException("XPathFactory#newInstance(String uri) cannot be called with uri == null");
    }
    if (uri.length() == 0) {
      throw new IllegalArgumentException("XPathFactory#newInstance(String uri) cannot be called with uri == \"\"");
    }
    if (factoryClassName == null) {
      throw new XPathFactoryConfigurationException("factoryClassName cannot be null.");
    }
    XPathFactory factory;
    try {
      factory = FactoryFinder.instantiate(XPathFactory.class, factoryClassName, classLoader);
    } catch (FactoryFinder.FactoryError e) {
      throw new XPathFactoryConfigurationException(e);
    }
    if (factory.isObjectModelSupported(uri)) {
      return factory;
    }
    throw new XPathFactoryConfigurationException("No XPathFactory implementation found for the object model: " + uri);
  }

  public abstract boolean isObjectModelSupported(String objectModel);

  public abstract void setFeature(String name, boolean value) throws XPathFactoryConfigurationException;

  public abstract boolean getFeature(String name) throws XPathFactoryConfigurationException;

  public abstract void setXPathVariableResolver(XPathVariableResolver resolver);

  public abstract void setXPathFunctionResolver(XPathFunctionResolver resolver);

  public abstract XPath newXPath();

  public void setProperty(String name, String value) {
    if (name == null) {
      throw new NullPointerException("the name parameter is null");
    }
    throw new UnsupportedOperationException("Property " + name + " is not supported");
  }

  public String getProperty(String name) {
    if (name == null) {
      throw new NullPointerException("the name parameter is null");
    }
    throw new UnsupportedOperationException("Property " + name + " is not supported");
  }
}
