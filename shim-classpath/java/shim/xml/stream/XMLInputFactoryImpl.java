/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.InputSource;
import shim.xml.XmlException;
import shim.xml.XmlInput;
import shim.xml.XmlMalformedException;
import shim.xml.XmlPullParser;

public final class XMLInputFactoryImpl extends XMLInputFactory {
  private static final String kCdata = "http://java.sun.com/xml/stream/properties/report-cdata-event";
  private static final String kIgnoreDtd = "http://java.sun.com/xml/stream/properties/ignore-external-dtd";
  private static final String kNullSpec =
      "Cannot invoke \"String.length()\" because \"spec\" is null";

  private final Map<String, Object> properties = new HashMap<String, Object>();
  private XMLReporter reporter;
  private XMLResolver resolver;
  private XMLEventAllocator allocator;

  public XMLInputFactoryImpl() {
    properties.put(IS_NAMESPACE_AWARE, Boolean.TRUE);
    properties.put(IS_VALIDATING, Boolean.FALSE);
    properties.put(IS_COALESCING, Boolean.FALSE);
    properties.put(IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
    properties.put(IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
    properties.put(SUPPORT_DTD, Boolean.TRUE);
    properties.put(kCdata, Boolean.FALSE);
    properties.put(kIgnoreDtd, Boolean.FALSE);
    properties.put("jdk.xml.entityExpansionLimit", "2500");
    properties.put("jdk.xml.elementAttributeLimit", "10000");
    properties.put("jdk.xml.maxOccurLimit", "5000");
    properties.put("jdk.xml.totalEntitySizeLimit", "100000000");
    properties.put("jdk.xml.maxGeneralEntitySizeLimit", "100000");
    properties.put("jdk.xml.maxParameterEntitySizeLimit", "15000");
    properties.put("jdk.xml.entityReplacementLimit", "2000000");
    properties.put("jdk.xml.maxElementDepth", "0");
    properties.put("jdk.xml.maxXMLNameLimit", "1000");
    properties.put("http://javax.xml.XMLConstants/property/accessExternalDTD", "all");
    properties.put("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.FALSE);
  }

  private StaxConfig config() {
    StaxConfig c = new StaxConfig();
    c.namespaceAware = bool(IS_NAMESPACE_AWARE);
    c.validating = bool(IS_VALIDATING);
    c.coalescing = bool(IS_COALESCING);
    c.replacingEntities = bool(IS_REPLACING_ENTITY_REFERENCES);
    c.externalEntities = bool(IS_SUPPORTING_EXTERNAL_ENTITIES);
    c.supportDtd = bool(SUPPORT_DTD);
    c.reportCdata = bool(kCdata);
    c.ignoreExternalDtd = bool(kIgnoreDtd);
    c.reporter = reporter;
    c.resolver = resolver;
    c.allocator = allocator;
    return c;
  }

  private boolean bool(String name) {
    Object v = properties.get(name);
    if (v instanceof Boolean) {
      return ((Boolean) v).booleanValue();
    }
    return v != null && Boolean.parseBoolean(v.toString());
  }

  private XMLStreamReader open(InputSource src) throws XMLStreamException {
    StaxConfig cfg = config();
    StaxResolver bridge = new StaxResolver(resolver, cfg.ignoreExternalDtd);
    XmlInput input;
    try {
      input = XmlInput.open(src, null);
    } catch (XmlException e) {
      throw new XMLStreamException(e.getMessage(), new StaxLocation(e.line, e.column, -1, null, null));
    } catch (IOException e) {
      throw new XMLStreamException(e);
    }
    if (input.encodingProblem != null) {
      throw new XMLStreamException(new UnsupportedEncodingException(input.encodingProblem.toUpperCase(Locale.ROOT)));
    }
    XmlPullParser p = new XmlPullParser(input, bridge);
    p.namespaceAware = cfg.namespaceAware;
    p.splitCharRefs = true;
    p.reportEntityRefs = !cfg.replacingEntities;
    p.loadExternalDtd = cfg.externalEntities && cfg.supportDtd;
    p.externalGeneralEntities = cfg.externalEntities;
    return new StaxReader(p, cfg, input.encodingName, bridge);
  }

  private XMLStreamException nullSpec() {
    return new XMLStreamException(new MalformedURLException(kNullSpec));
  }

  private InputSource source(String systemId, Reader reader, InputStream stream, String encoding)
      throws XMLStreamException {
    InputSource src = new InputSource();
    if (reader != null) {
      src.setCharacterStream(reader);
    } else if (stream != null) {
      src.setByteStream(stream);
      if (encoding != null) {
        src.setEncoding(encoding);
      }
    } else if (systemId == null) {
      throw nullSpec();
    }
    if (systemId != null) {
      src.setSystemId(systemId);
    }
    return src;
  }

  public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
    return open(source(null, reader, null, null));
  }

  public XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException {
    if (source == null) {
      throw new NullPointerException("Cannot invoke \"javax.xml.transform.Source.getClass()\" because \"source\" "
          + "is null");
    }
    if (source instanceof StreamSource) {
      StreamSource ss = (StreamSource) source;
      InputSource src = source(ss.getSystemId(), ss.getReader(), ss.getInputStream(), null);
      if (ss.getPublicId() != null) {
        src.setPublicId(ss.getPublicId());
      }
      return open(src);
    }
    throw new UnsupportedOperationException("Cannot create XMLStreamReader or XMLEventReader from a "
        + source.getClass().getName());
  }

  public XMLStreamReader createXMLStreamReader(InputStream stream) throws XMLStreamException {
    return open(source(null, null, stream, null));
  }

  public XMLStreamReader createXMLStreamReader(InputStream stream, String encoding) throws XMLStreamException {
    return open(source(null, null, stream, encoding));
  }

  public XMLStreamReader createXMLStreamReader(String systemId, InputStream stream) throws XMLStreamException {
    return open(source(systemId, null, stream, null));
  }

  public XMLStreamReader createXMLStreamReader(String systemId, Reader reader) throws XMLStreamException {
    return open(source(systemId, reader, null, null));
  }

  private XMLEventReader events(XMLStreamReader reader) {
    XMLEventAllocator a = allocator != null ? allocator.newInstance() : new StaxAllocator();
    return new StaxEventReader(reader, a);
  }

  public XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException {
    return events(createXMLStreamReader(reader));
  }

  public XMLEventReader createXMLEventReader(String systemId, Reader reader) throws XMLStreamException {
    return events(createXMLStreamReader(systemId, reader));
  }

  public XMLEventReader createXMLEventReader(XMLStreamReader reader) throws XMLStreamException {
    return events(reader);
  }

  public XMLEventReader createXMLEventReader(Source source) throws XMLStreamException {
    return events(createXMLStreamReader(source));
  }

  public XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException {
    return events(createXMLStreamReader(stream));
  }

  public XMLEventReader createXMLEventReader(InputStream stream, String encoding) throws XMLStreamException {
    return events(createXMLStreamReader(stream, encoding));
  }

  public XMLEventReader createXMLEventReader(String systemId, InputStream stream) throws XMLStreamException {
    return events(createXMLStreamReader(systemId, stream));
  }

  public XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter)
      throws XMLStreamException {
    return new StaxFilters.StreamFilterReader(reader, filter);
  }

  public XMLEventReader createFilteredReader(XMLEventReader reader, EventFilter filter) throws XMLStreamException {
    return new StaxFilters.EventFilterReader(reader, filter);
  }

  public XMLResolver getXMLResolver() {
    return resolver;
  }

  public void setXMLResolver(XMLResolver resolver) {
    this.resolver = resolver;
    properties.put(RESOLVER, resolver);
  }

  public XMLReporter getXMLReporter() {
    return reporter;
  }

  public void setXMLReporter(XMLReporter reporter) {
    this.reporter = reporter;
    properties.put(REPORTER, reporter);
  }

  public void setProperty(String name, Object value) throws IllegalArgumentException {
    if (name == null || !isPropertySupported(name)) {
      throw new IllegalArgumentException("Property " + name + " is not supported");
    }
    if (name.equals(REPORTER)) {
      setXMLReporter((XMLReporter) value);
      return;
    }
    if (name.equals(RESOLVER)) {
      setXMLResolver((XMLResolver) value);
      return;
    }
    if (name.equals(ALLOCATOR)) {
      setEventAllocator((XMLEventAllocator) value);
      return;
    }
    if (value == null) {
      throw new IllegalArgumentException("Property " + name + " is not supported");
    }
    if (name.equals(IS_VALIDATING) && bool(value)) {
      throw new IllegalArgumentException("true value of isValidating not supported");
    }
    properties.put(name, value);
  }

  private static boolean bool(Object v) {
    return v instanceof Boolean ? ((Boolean) v).booleanValue() : Boolean.parseBoolean(v.toString());
  }

  public Object getProperty(String name) throws IllegalArgumentException {
    if (name == null || !isPropertySupported(name)) {
      throw new IllegalArgumentException("Property not supported");
    }
    return properties.get(name);
  }

  public boolean isPropertySupported(String name) {
    if (name == null) {
      return false;
    }
    return properties.containsKey(name) || name.equals(REPORTER) || name.equals(RESOLVER) || name.equals(ALLOCATOR);
  }

  public void setEventAllocator(XMLEventAllocator allocator) {
    this.allocator = allocator;
    properties.put(ALLOCATOR, allocator);
  }

  public XMLEventAllocator getEventAllocator() {
    return allocator;
  }
}
