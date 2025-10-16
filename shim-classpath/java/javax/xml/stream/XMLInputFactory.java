/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.stream;

import java.io.InputStream;
import java.io.Reader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import shim.xml.FactoryFinder;

public abstract class XMLInputFactory {
  public static final String IS_NAMESPACE_AWARE = "javax.xml.stream.isNamespaceAware";
  public static final String IS_VALIDATING = "javax.xml.stream.isValidating";
  public static final String IS_COALESCING = "javax.xml.stream.isCoalescing";
  public static final String IS_REPLACING_ENTITY_REFERENCES = "javax.xml.stream.isReplacingEntityReferences";
  public static final String IS_SUPPORTING_EXTERNAL_ENTITIES = "javax.xml.stream.isSupportingExternalEntities";
  public static final String SUPPORT_DTD = "javax.xml.stream.supportDTD";
  public static final String REPORTER = "javax.xml.stream.reporter";
  public static final String RESOLVER = "javax.xml.stream.resolver";
  public static final String ALLOCATOR = "javax.xml.stream.allocator";
  private static final String kDefault = "shim.xml.stream.XMLInputFactoryImpl";

  protected XMLInputFactory() {
  }

  public static XMLInputFactory newDefaultFactory() {
    return FactoryFinder.instantiate(XMLInputFactory.class, kDefault, XMLInputFactory.class.getClassLoader());
  }

  public static XMLInputFactory newInstance() throws FactoryConfigurationError {
    return newFactory();
  }

  public static XMLInputFactory newFactory() throws FactoryConfigurationError {
    try {
      return FactoryFinder.find(XMLInputFactory.class, kDefault, null);
    } catch (FactoryFinder.FactoryError e) {
      throw new FactoryConfigurationError(e.getMessage(), (Exception) e.getCause());
    }
  }

  public static XMLInputFactory newInstance(String factoryId, ClassLoader classLoader)
      throws FactoryConfigurationError {
    return newFactory(factoryId, classLoader);
  }

  public static XMLInputFactory newFactory(String factoryId, ClassLoader classLoader)
      throws FactoryConfigurationError {
    if (factoryId == null) {
      throw new FactoryConfigurationError("factoryId cannot be null");
    }
    try {
      return FactoryFinder.instantiate(XMLInputFactory.class, factoryId, classLoader);
    } catch (FactoryFinder.FactoryError e) {
      throw new FactoryConfigurationError(e.getMessage(), (Exception) e.getCause());
    }
  }

  public abstract XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException;

  public abstract XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException;

  public abstract XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException;

  public abstract XMLStreamReader createXMLStreamReader(InputStream stream, String encoding)
      throws XMLStreamException;

  public abstract XMLStreamReader createXMLStreamReader(String systemId, InputStream stream)
      throws XMLStreamException;

  public abstract XMLStreamReader createXMLStreamReader(String systemId, Reader reader) throws XMLStreamException;

  public abstract XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException;

  public abstract XMLEventReader createXMLEventReader(String systemId, Reader reader) throws XMLStreamException;

  public abstract XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException;

  public abstract XMLEventReader createXMLEventReader(Source source) throws XMLStreamException;

  public abstract XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException;

  public abstract XMLEventReader createXMLEventReader(InputStream stream, String encoding)
      throws XMLStreamException;

  public abstract XMLEventReader createXMLEventReader(String systemId, InputStream stream)
      throws XMLStreamException;

  public abstract XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter)
      throws XMLStreamException;

  public abstract XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter)
      throws XMLStreamException;

  public abstract XMLResolver getXMLResolver();

  public abstract void setXMLResolver(XMLResolver resolver);

  public abstract XMLReporter getXMLReporter();

  public abstract void setXMLReporter(XMLReporter reporter);

  public abstract void setProperty(String name, Object value) throws IllegalArgumentException;

  public abstract Object getProperty(String name) throws IllegalArgumentException;

  public abstract boolean isPropertySupported(String name);

  public abstract void setEventAllocator(XMLEventAllocator allocator);

  public abstract XMLEventAllocator getEventAllocator();
}
