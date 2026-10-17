/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.parsers;

import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import shim.xml.SaxReader;
import shim.xml.dom.DomBuilder;
import shim.xml.dom.DomImplementation;

public class DocumentBuilderImpl extends DocumentBuilder {
  private final boolean namespaceAware;
  private final boolean validating;
  private final boolean ignoreComments;
  private final boolean coalescing;
  private final boolean ignoreWhitespace;
  private final boolean expandEntityReferences;
  private final Map<String, Boolean> features;
  private final Map<String, Object> attributes;
  private EntityResolver entityResolver;
  private ErrorHandler errorHandler;

  DocumentBuilderImpl(DocumentBuilderFactory factory, Map<String, Object> attributes, Map<String, Boolean> features)
      throws ParserConfigurationException {
    this.namespaceAware = factory.isNamespaceAware();
    this.validating = factory.isValidating();
    this.ignoreComments = factory.isIgnoringComments();
    this.coalescing = factory.isCoalescing();
    this.ignoreWhitespace = factory.isIgnoringElementContentWhitespace();
    this.expandEntityReferences = factory.isExpandEntityReferences();
    this.features = new java.util.HashMap<String, Boolean>(features);
    this.attributes = new java.util.HashMap<String, Object>(attributes);
  }

  public Document parse(InputSource is) throws SAXException, IOException {
    if (is == null) {
      throw new IllegalArgumentException("The source specified cannot be null.");
    }
    SaxReader reader = new SaxReader(namespaceAware);
    reader.setFeature("http://xml.org/sax/features/namespaces", namespaceAware);
    reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    reader.setFeature("http://xml.org/sax/features/validation", validating);
    for (Map.Entry<String, Boolean> e : features.entrySet()) {
      if (e.getKey().equals("http://apache.org/xml/features/dom/create-entity-ref-nodes")) {
        continue;
      }
      reader.setFeature(e.getKey(), e.getValue());
    }
    for (Map.Entry<String, Object> e : attributes.entrySet()) {
      try {
        reader.setProperty(e.getKey(), e.getValue());
      } catch (SAXException ignored) {
      }
    }
    boolean createRefs = features.containsKey("http://apache.org/xml/features/dom/create-entity-ref-nodes")
        ? features.get("http://apache.org/xml/features/dom/create-entity-ref-nodes") : !expandEntityReferences;
    DomBuilder builder = new DomBuilder(reader, namespaceAware, ignoreComments, coalescing, ignoreWhitespace,
        !createRefs, null);
    reader.setContentHandler(builder);
    reader.setProperty("http://xml.org/sax/properties/lexical-handler", builder);
    reader.setProperty("http://xml.org/sax/properties/declaration-handler", builder);
    reader.setDTDHandler(builder);
    reader.setEntityResolver(entityResolver);
    reader.setErrorHandler(errorHandler != null ? errorHandler : new PrintingErrorHandler());
    reader.parse(is);
    org.w3c.dom.Document doc = builder.document();
    ((shim.xml.dom.DocumentImpl) doc).setDocumentURI(reader.getParsedSystemId());
    return doc;
  }

  public boolean isNamespaceAware() {
    return namespaceAware;
  }

  public boolean isValidating() {
    return validating;
  }

  public void setEntityResolver(EntityResolver er) {
    this.entityResolver = er;
  }

  public void setErrorHandler(ErrorHandler eh) {
    this.errorHandler = eh;
  }

  public Document newDocument() {
    return new shim.xml.dom.DocumentImpl();
  }

  public DOMImplementation getDOMImplementation() {
    return DomImplementation.getInstance();
  }

  public void reset() {
    entityResolver = null;
    errorHandler = null;
  }

  public boolean isXIncludeAware() {
    return false;
  }

  static final class PrintingErrorHandler implements ErrorHandler {
    public void warning(SAXParseException e) throws SAXException {
      print("Warning", e);
    }

    public void error(SAXParseException e) throws SAXException {
      print("Error", e);
    }

    public void fatalError(SAXParseException e) throws SAXException {
      print("Fatal Error", e);
      throw e;
    }

    private static void print(String kind, SAXParseException e) {
      String id = e.getSystemId();
      if (id == null) {
        id = "";
      } else {
        int slash = id.lastIndexOf('/');
        if (slash >= 0) {
          id = id.substring(slash + 1);
        }
      }
      System.err.println("[" + kind + "] " + id + ":" + e.getLineNumber() + ":" + e.getColumnNumber() + ": "
          + e.getMessage());
    }
  }
}
