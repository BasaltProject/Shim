/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.stream;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public final class XMLOutputFactoryImpl extends XMLOutputFactory {
  private final Map<String, Object> properties = new HashMap<String, Object>();

  public XMLOutputFactoryImpl() {
    properties.put(IS_REPAIRING_NAMESPACES, Boolean.FALSE);
    properties.put("escapeCharacters", Boolean.TRUE);
  }

  private boolean bool(String name) {
    Object v = properties.get(name);
    return v instanceof Boolean ? ((Boolean) v).booleanValue() : Boolean.parseBoolean(String.valueOf(v));
  }

  public XMLStreamWriter createXMLStreamWriter(Writer stream) throws XMLStreamException {
    return new StaxWriter(stream, null, null, bool(IS_REPAIRING_NAMESPACES), bool("escapeCharacters"));
  }

  public XMLStreamWriter createXMLStreamWriter(OutputStream stream) throws XMLStreamException {
    return StaxWriter.forStream(stream, "UTF-8", bool(IS_REPAIRING_NAMESPACES), bool("escapeCharacters"));
  }

  public XMLStreamWriter createXMLStreamWriter(OutputStream stream, String encoding) throws XMLStreamException {
    return StaxWriter.forStream(stream, encoding == null ? "UTF-8" : encoding, bool(IS_REPAIRING_NAMESPACES),
        bool("escapeCharacters"));
  }

  public XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException {
    if (result == null) {
      throw new NullPointerException("Cannot invoke \"javax.xml.transform.Result.getSystemId()\" because "
          + "\"result\" is null");
    }
    if (result instanceof StreamResult) {
      StreamResult sr = (StreamResult) result;
      if (sr.getWriter() != null) {
        return createXMLStreamWriter(sr.getWriter());
      }
      if (sr.getOutputStream() != null) {
        return createXMLStreamWriter(sr.getOutputStream());
      }
      if (sr.getSystemId() != null) {
        try {
          java.io.File f = new java.io.File(new java.net.URI(sr.getSystemId()));
          return createXMLStreamWriter(new java.io.FileOutputStream(f));
        } catch (Exception e) {
          throw new XMLStreamException(e);
        }
      }
      return createXMLStreamWriter((Writer) null);
    }
    throw new UnsupportedOperationException("Result of type " + result.getClass().getName() + " is not supported");
  }

  public XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException {
    return new StaxEventWriter(createXMLStreamWriter(result));
  }

  public XMLEventWriter createXMLEventWriter(OutputStream stream) throws XMLStreamException {
    return new StaxEventWriter(createXMLStreamWriter(stream));
  }

  public XMLEventWriter createXMLEventWriter(OutputStream stream, String encoding) throws XMLStreamException {
    return new StaxEventWriter(createXMLStreamWriter(stream, encoding));
  }

  public XMLEventWriter createXMLEventWriter(Writer stream) throws XMLStreamException {
    return new StaxEventWriter(createXMLStreamWriter(stream));
  }

  public void setProperty(String name, Object value) throws IllegalArgumentException {
    if (name == null || !properties.containsKey(name)) {
      throw new IllegalArgumentException("Property " + name + "is not supported");
    }
    if (value == null) {
      throw new IllegalArgumentException("Property " + name + "is not supported");
    }
    properties.put(name, value);
  }

  public Object getProperty(String name) throws IllegalArgumentException {
    if (name == null || !properties.containsKey(name)) {
      throw new IllegalArgumentException("Property not supported");
    }
    return properties.get(name);
  }

  public boolean isPropertySupported(String name) {
    return name != null && properties.containsKey(name);
  }
}
