/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.transform;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.TransformerHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

final class TransformerHandlerImpl implements TransformerHandler, LexicalHandler {
  private final IdentityTransformer transformer;
  private IdentityTransformer snapshot;
  private IdentityTransformer.SerializationTarget target;
  private Result result;
  private String systemId;

  TransformerHandlerImpl(IdentityTransformer transformer) {
    this.transformer = transformer;
  }

  public void setResult(Result result) throws IllegalArgumentException {
    if (result == null) {
      throw new IllegalArgumentException("Result should not be null");
    }
    this.result = result;
    this.snapshot = transformer.copy();
    try {
      this.target = snapshot.openTarget(result);
    } catch (TransformerException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  public void setSystemId(String systemID) {
    this.systemId = systemID;
  }

  public String getSystemId() {
    return systemId;
  }

  public Transformer getTransformer() {
    return transformer;
  }

  private void check() throws SAXException {
    if (target == null) {
      throw new SAXException("result handler not set");
    }
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void startDocument() throws SAXException {
    check();
    target.handler.startDocument();
  }

  public void endDocument() throws SAXException {
    target.handler.endDocument();
    try {
      target.finish();
    } catch (java.io.IOException e) {
      throw new SAXException(e);
    } finally {
      target.close();
    }
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    target.handler.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    target.handler.endPrefixMapping(prefix);
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    target.handler.startElement(uri, localName, qName, atts);
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    target.handler.endElement(uri, localName, qName);
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    target.handler.characters(ch, start, length);
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    target.handler.ignorableWhitespace(ch, start, length);
  }

  public void processingInstruction(String t, String data) throws SAXException {
    target.handler.processingInstruction(t, data);
  }

  public void skippedEntity(String name) throws SAXException {
    target.handler.skippedEntity(name);
  }

  public void notationDecl(String name, String publicId, String systemId) throws SAXException {
  }

  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
      throws SAXException {
  }

  public void startDTD(String name, String publicId, String systemId) throws SAXException {
  }

  public void endDTD() throws SAXException {
  }

  public void startEntity(String name) throws SAXException {
  }

  public void endEntity(String name) throws SAXException {
  }

  public void startCDATA() throws SAXException {
    if (target.lexical != null) {
      target.lexical.startCDATA();
    }
  }

  public void endCDATA() throws SAXException {
    if (target.lexical != null) {
      target.lexical.endCDATA();
    }
  }

  public void comment(char[] ch, int start, int length) throws SAXException {
    if (target.lexical != null) {
      target.lexical.comment(ch, start, length);
    }
  }
}
