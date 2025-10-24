/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.transform;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

final class SaxResultAdapter implements ContentHandler, LexicalHandler {
  private final ContentHandler out;
  private final LexicalHandler lexical;
  private final List<List<String>> declared = new ArrayList<List<String>>();
  private List<String> pending = new ArrayList<String>();
  private boolean pendingEndCdata;

  SaxResultAdapter(ContentHandler out, LexicalHandler lexical) {
    this.out = out;
    this.lexical = lexical;
  }

  private void flushCdata() throws SAXException {
    if (pendingEndCdata) {
      pendingEndCdata = false;
      if (lexical != null) {
        lexical.endCDATA();
      }
    }
  }

  public void setDocumentLocator(Locator locator) {
    out.setDocumentLocator(locator);
  }

  public void startDocument() throws SAXException {
    out.startDocument();
  }

  public void endDocument() throws SAXException {
    flushCdata();
    out.endDocument();
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    flushCdata();
    pending.add(prefix);
    out.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    flushCdata();
    declared.add(pending);
    pending = new ArrayList<String>();
    out.startElement(uri, localName, qName, atts);
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    flushCdata();
    out.endElement(uri, localName, qName);
    List<String> list = declared.remove(declared.size() - 1);
    for (int i = list.size() - 1; i >= 0; i--) {
      out.endPrefixMapping(list.get(i));
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    out.characters(ch, start, length);
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    out.ignorableWhitespace(ch, start, length);
  }

  public void processingInstruction(String target, String data) throws SAXException {
    flushCdata();
    out.processingInstruction(target, data);
  }

  public void skippedEntity(String name) throws SAXException {
    flushCdata();
    out.skippedEntity(name);
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
    flushCdata();
    if (lexical != null) {
      lexical.startCDATA();
    }
  }

  public void endCDATA() throws SAXException {
    pendingEndCdata = true;
  }

  public void comment(char[] ch, int start, int length) throws SAXException {
    flushCdata();
    if (lexical != null) {
      lexical.comment(ch, start, length);
    }
  }
}
