/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.parsers;

import javax.xml.validation.Schema;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import shim.xml.FactoryFinder;

public abstract class SAXParserFactory {
  private static final String kDefault = "shim.xml.parsers.SAXParserFactoryImpl";

  private boolean validating = false;
  private boolean namespaceAware = false;

  protected SAXParserFactory() {
  }

  public static SAXParserFactory newDefaultNSInstance() {
    return makeNSAware(newDefaultInstance());
  }

  public static SAXParserFactory newNSInstance() {
    return makeNSAware(newInstance());
  }

  public static SAXParserFactory newNSInstance(String factoryClassName, ClassLoader classLoader) {
    return makeNSAware(newInstance(factoryClassName, classLoader));
  }

  private static SAXParserFactory makeNSAware(SAXParserFactory factory) {
    factory.setNamespaceAware(true);
    return factory;
  }

  public static SAXParserFactory newDefaultInstance() {
    return FactoryFinder.instantiate(SAXParserFactory.class, kDefault, SAXParserFactory.class.getClassLoader());
  }

  public static SAXParserFactory newInstance() {
    try {
      return FactoryFinder.find(SAXParserFactory.class, kDefault, null);
    } catch (FactoryFinder.FactoryError e) {
      throw new FactoryConfigurationError(e, e.getMessage());
    }
  }

  public static SAXParserFactory newInstance(String factoryClassName, ClassLoader classLoader) {
    if (factoryClassName == null) {
      throw new FactoryConfigurationError("factoryClassName cannot be null.");
    }
    try {
      return FactoryFinder.instantiate(SAXParserFactory.class, factoryClassName, classLoader);
    } catch (FactoryFinder.FactoryError e) {
      throw new FactoryConfigurationError(e, e.getMessage());
    }
  }

  public abstract SAXParser newSAXParser() throws ParserConfigurationException, SAXException;

  public void setNamespaceAware(boolean awareness) {
    this.namespaceAware = awareness;
  }

  public void setValidating(boolean validating) {
    this.validating = validating;
  }

  public boolean isNamespaceAware() {
    return namespaceAware;
  }

  public boolean isValidating() {
    return validating;
  }

  public abstract void setFeature(String name, boolean value) throws ParserConfigurationException,
      SAXNotRecognizedException, SAXNotSupportedException;

  public abstract boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException,
      SAXNotSupportedException;

  public Schema getSchema() {
    throw new UnsupportedOperationException("This parser does not support specification \""
        + this.getClass().getPackage().getSpecificationTitle() + "\" version \""
        + this.getClass().getPackage().getSpecificationVersion() + "\"");
  }

  public void setSchema(Schema schema) {
    throw new UnsupportedOperationException("This parser does not support specification \""
        + this.getClass().getPackage().getSpecificationTitle() + "\" version \""
        + this.getClass().getPackage().getSpecificationVersion() + "\"");
  }

  public void setXIncludeAware(final boolean state) {
    if (state) {
      throw new UnsupportedOperationException(" setXIncludeAware is not supported on this JAXP implementation or earlier: "
          + this.getClass());
    }
  }

  public boolean isXIncludeAware() {
    throw new UnsupportedOperationException("This parser does not support specification \""
        + this.getClass().getPackage().getSpecificationTitle() + "\" version \""
        + this.getClass().getPackage().getSpecificationVersion() + "\"");
  }
}
