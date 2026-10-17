/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream;

import java.io.OutputStream;
import java.io.Writer;
import javax.xml.transform.Result;
import shim.xml.FactoryFinder;

public abstract class XMLOutputFactory {
  public static final String IS_REPAIRING_NAMESPACES = "javax.xml.stream.isRepairingNamespaces";
  private static final String kDefault = "shim.xml.stream.XMLOutputFactoryImpl";

  protected XMLOutputFactory() {
  }

  public static XMLOutputFactory newDefaultFactory() {
    return FactoryFinder.instantiate(XMLOutputFactory.class, kDefault, XMLOutputFactory.class.getClassLoader());
  }

  public static XMLOutputFactory newInstance() throws FactoryConfigurationError {
    return newFactory();
  }

  public static XMLOutputFactory newFactory() throws FactoryConfigurationError {
    try {
      return FactoryFinder.find(XMLOutputFactory.class, kDefault, null);
    } catch (FactoryFinder.FactoryError e) {
      throw new FactoryConfigurationError(e.getMessage(), (Exception) e.getCause());
    }
  }

  public static XMLInputFactory newInstance(String factoryId, ClassLoader classLoader)
      throws FactoryConfigurationError {
    throw new UnsupportedOperationException("This method is deprecated; use newFactory instead");
  }

  public static XMLOutputFactory newFactory(String factoryId, ClassLoader classLoader)
      throws FactoryConfigurationError {
    if (factoryId == null) {
      throw new FactoryConfigurationError("factoryId cannot be null");
    }
    try {
      return FactoryFinder.instantiate(XMLOutputFactory.class, factoryId, classLoader);
    } catch (FactoryFinder.FactoryError e) {
      throw new FactoryConfigurationError(e.getMessage(), (Exception) e.getCause());
    }
  }

  public abstract XMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException;

  public abstract XMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException;

  public abstract XMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding)
      throws XMLStreamException;

  public abstract XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException;

  public abstract XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException;

  public abstract XMLEventWriter createXMLEventWriter(OutputStream stream) throws XMLStreamException;

  public abstract XMLEventWriter createXMLEventWriter(OutputStream stream, String encoding)
      throws XMLStreamException;

  public abstract XMLEventWriter createXMLEventWriter(Writer stream) throws XMLStreamException;

  public abstract void setProperty(String name, Object value) throws IllegalArgumentException;

  public abstract Object getProperty(String name) throws IllegalArgumentException;

  public abstract boolean isPropertySupported(String name);
}
