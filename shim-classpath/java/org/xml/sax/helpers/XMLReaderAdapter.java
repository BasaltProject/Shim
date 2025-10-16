/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.xml.sax.helpers;

import java.io.IOException;
import java.util.Locale;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

@Deprecated
public class XMLReaderAdapter implements Parser, ContentHandler {
  XMLReader xmlReader;
  DocumentHandler documentHandler;
  AttributesAdapter qAtts;

  public XMLReaderAdapter() throws SAXException {
    setup(XMLReaderFactory.createXMLReader());
  }

  public XMLReaderAdapter(XMLReader xmlReader) {
    setup(xmlReader);
  }

  private void setup(XMLReader xmlReader) {
    if (xmlReader == null) {
      throw new NullPointerException("XMLReader must not be null");
    }
    this.xmlReader = xmlReader;
    qAtts = new AttributesAdapter();
  }

  public void setLocale(Locale locale) throws SAXException {
    throw new SAXNotSupportedException("setLocale not supported");
  }

  public void setEntityResolver(EntityResolver resolver) {
    xmlReader.setEntityResolver(resolver);
  }

  public void setDTDHandler(DTDHandler handler) {
    xmlReader.setDTDHandler(handler);
  }

  public void setDocumentHandler(DocumentHandler handler) {
    documentHandler = handler;
  }

  public void setErrorHandler(ErrorHandler handler) {
    xmlReader.setErrorHandler(handler);
  }

  public void parse(String systemId) throws IOException, SAXException {
    parse(new InputSource(systemId));
  }

  public void parse(InputSource input) throws IOException, SAXException {
    setupXMLReader();
    xmlReader.parse(input);
  }

  private void setupXMLReader() throws SAXException {
    xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    try {
      xmlReader.setFeature("http://xml.org/sax/features/namespaces", false);
    } catch (SAXException e) {
    }
    xmlReader.setContentHandler(this);
  }

  public void setDocumentLocator(Locator locator) {
    if (documentHandler != null) {
      documentHandler.setDocumentLocator(locator);
    }
  }

  public void startDocument() throws SAXException {
    if (documentHandler != null) {
      documentHandler.startDocument();
    }
  }

  public void endDocument() throws SAXException {
    if (documentHandler != null) {
      documentHandler.endDocument();
    }
  }

  public void startPrefixMapping(String prefix, String uri) {
  }

  public void endPrefixMapping(String prefix) {
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if (documentHandler != null) {
      qAtts.setAttributes(atts);
      documentHandler.startElement(qName, qAtts);
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (documentHandler != null) {
      documentHandler.endElement(qName);
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    if (documentHandler != null) {
      documentHandler.characters(ch, start, length);
    }
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    if (documentHandler != null) {
      documentHandler.ignorableWhitespace(ch, start, length);
    }
  }

  public void processingInstruction(String target, String data) throws SAXException {
    if (documentHandler != null) {
      documentHandler.processingInstruction(target, data);
    }
  }

  public void skippedEntity(String name) throws SAXException {
  }

  final class AttributesAdapter implements AttributeList {
    private Attributes attributes;

    void setAttributes(Attributes attributes) {
      this.attributes = attributes;
    }

    public int getLength() {
      return attributes.getLength();
    }

    public String getName(int i) {
      return attributes.getQName(i);
    }

    public String getType(int i) {
      return attributes.getType(i);
    }

    public String getValue(int i) {
      return attributes.getValue(i);
    }

    public String getType(String name) {
      return attributes.getType(name);
    }

    public String getValue(String name) {
      return attributes.getValue(name);
    }
  }
}
