/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.parsers;

import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderAdapter;
import shim.xml.SaxReader;

public class SAXParserImpl extends SAXParser {
  private final SaxReader reader;
  private final boolean namespaceAware;
  private final boolean validating;
  private final Map<String, Object> properties = new HashMap<String, Object>();

  SAXParserImpl(SAXParserFactory factory, Map<String, Boolean> features) throws SAXException {
    this.namespaceAware = factory.isNamespaceAware();
    this.validating = factory.isValidating();
    this.reader = new SaxReader(namespaceAware);
    reader.setFeature("http://xml.org/sax/features/validation", validating);
    for (Map.Entry<String, Boolean> e : features.entrySet()) {
      if (e.getKey().equals("http://xml.org/sax/features/namespaces")) {
        continue;
      }
      reader.setFeature(e.getKey(), e.getValue());
    }
  }

  public Parser getParser() throws SAXException {
    return new XMLReaderAdapter(getXMLReader());
  }

  public XMLReader getXMLReader() throws SAXException {
    return reader;
  }

  public boolean isNamespaceAware() {
    return namespaceAware;
  }

  public boolean isValidating() {
    return validating;
  }

  public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    reader.setProperty(name, value);
  }

  public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    return reader.getProperty(name);
  }

  public void reset() {
    reader.setContentHandler(null);
    reader.setDTDHandler(null);
    reader.setEntityResolver(null);
    reader.setErrorHandler(null);
  }

  public boolean isXIncludeAware() {
    return false;
  }
}
