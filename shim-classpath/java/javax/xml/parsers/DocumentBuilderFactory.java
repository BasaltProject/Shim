/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.parsers;

import javax.xml.validation.Schema;
import shim.xml.FactoryFinder;

public abstract class DocumentBuilderFactory {
  private static final String kDefault = "shim.xml.parsers.DocumentBuilderFactoryImpl";

  private boolean validating = false;
  private boolean namespaceAware = false;
  private boolean whitespace = false;
  private boolean expandEntityRef = true;
  private boolean ignoreComments = false;
  private boolean coalescing = false;

  protected DocumentBuilderFactory() {
  }

  public static DocumentBuilderFactory newDefaultNSInstance() {
    return makeNSAware(newDefaultInstance());
  }

  public static DocumentBuilderFactory newNSInstance() {
    return makeNSAware(newInstance());
  }

  public static DocumentBuilderFactory newNSInstance(String factoryClassName, ClassLoader classLoader) {
    return makeNSAware(newInstance(factoryClassName, classLoader));
  }

  private static DocumentBuilderFactory makeNSAware(DocumentBuilderFactory factory) {
    factory.setNamespaceAware(true);
    return factory;
  }

  public static DocumentBuilderFactory newDefaultInstance() {
    return FactoryFinder.instantiate(DocumentBuilderFactory.class, kDefault,
        DocumentBuilderFactory.class.getClassLoader());
  }

  public static DocumentBuilderFactory newInstance() {
    try {
      return FactoryFinder.find(DocumentBuilderFactory.class, kDefault, null);
    } catch (FactoryFinder.FactoryError e) {
      throw new FactoryConfigurationError(e, e.getMessage());
    }
  }

  public static DocumentBuilderFactory newInstance(String factoryClassName, ClassLoader classLoader) {
    if (factoryClassName == null) {
      throw new FactoryConfigurationError("factoryClassName cannot be null.");
    }
    try {
      return FactoryFinder.instantiate(DocumentBuilderFactory.class, factoryClassName, classLoader);
    } catch (FactoryFinder.FactoryError e) {
      throw new FactoryConfigurationError(e, e.getMessage());
    }
  }

  public abstract DocumentBuilder newDocumentBuilder() throws ParserConfigurationException;

  public void setNamespaceAware(boolean awareness) {
    this.namespaceAware = awareness;
  }

  public void setValidating(boolean validating) {
    this.validating = validating;
  }

  public void setIgnoringElementContentWhitespace(boolean whitespace) {
    this.whitespace = whitespace;
  }

  public void setExpandEntityReferences(boolean expandEntityRef) {
    this.expandEntityRef = expandEntityRef;
  }

  public void setIgnoringComments(boolean ignoreComments) {
    this.ignoreComments = ignoreComments;
  }

  public void setCoalescing(boolean coalescing) {
    this.coalescing = coalescing;
  }

  public boolean isNamespaceAware() {
    return namespaceAware;
  }

  public boolean isValidating() {
    return validating;
  }

  public boolean isIgnoringElementContentWhitespace() {
    return whitespace;
  }

  public boolean isExpandEntityReferences() {
    return expandEntityRef;
  }

  public boolean isIgnoringComments() {
    return ignoreComments;
  }

  public boolean isCoalescing() {
    return coalescing;
  }

  public abstract void setAttribute(String name, Object value) throws IllegalArgumentException;

  public abstract Object getAttribute(String name) throws IllegalArgumentException;

  public abstract void setFeature(String name, boolean value) throws ParserConfigurationException;

  public abstract boolean getFeature(String name) throws ParserConfigurationException;

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
