/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.Locator2;

public class SaxReader implements XMLReader, Locator2 {
  private static final String kFeatures = "http://xml.org/sax/features/";
  private static final String kProperties = "http://xml.org/sax/properties/";
  private static final String kApache = "http://apache.org/xml/features/";
  private static final String kSecure = "http://javax.xml.XMLConstants/feature/secure-processing";

  private ContentHandler contentHandler;
  private DTDHandler dtdHandler;
  private EntityResolver entityResolver;
  private ErrorHandler errorHandler;
  private LexicalHandler lexicalHandler;
  private DeclHandler declHandler;

  private boolean namespaces = true;
  private boolean namespacePrefixes = false;
  private boolean validation = false;
  private boolean externalGeneralEntities = true;
  private boolean externalParameterEntities = true;
  private boolean useEntityResolver2 = true;
  private boolean resolveDtdUris = true;
  private boolean loadExternalDtd = true;
  private boolean disallowDoctype = false;
  private boolean secureProcessing = true;
  private boolean lexicalParameterEntities = true;
  private final Map<String, Object> properties = new HashMap<String, Object>();

  private String actualEncoding;
  private String documentSystemId;
  private boolean parsing;
  private XmlPullParser parser;

  public SaxReader() {
  }

  public SaxReader(boolean namespaceAware) {
    this.namespaces = namespaceAware;
    this.namespacePrefixes = !namespaceAware;
  }

  private static boolean readOnlyFeature(String name) {
    return name.equals(kFeatures + "use-attributes2") || name.equals(kFeatures + "use-locator2")
        || name.equals(kFeatures + "xml-1.1") || name.equals(kFeatures + "is-standalone");
  }

  public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.equals(kFeatures + "namespaces")) {
      return namespaces;
    } else if (name.equals(kFeatures + "namespace-prefixes")) {
      return namespacePrefixes;
    } else if (name.equals(kFeatures + "validation")) {
      return validation;
    } else if (name.equals(kFeatures + "external-general-entities")) {
      return externalGeneralEntities;
    } else if (name.equals(kFeatures + "external-parameter-entities")) {
      return externalParameterEntities;
    } else if (name.equals(kFeatures + "use-entity-resolver2")) {
      return useEntityResolver2;
    } else if (name.equals(kFeatures + "resolve-dtd-uris")) {
      return resolveDtdUris;
    } else if (name.equals(kFeatures + "lexical-handler/parameter-entities")) {
      return lexicalParameterEntities;
    } else if (name.equals(kFeatures + "string-interning") || name.equals(kFeatures + "use-attributes2")
        || name.equals(kFeatures + "use-locator2") || name.equals(kFeatures + "xml-1.1")) {
      return true;
    } else if (name.equals(kFeatures + "is-standalone")) {
      if (!parsing || parser == null) {
        throw new SAXNotSupportedException("Feature '" + name + "' is not supported while parsing.");
      }
      return parser.standalone;
    } else if (name.equals(kFeatures + "unicode-normalization-checking") || name.equals(kFeatures + "xmlns-uris")
        || name.equals(kFeatures + "allow-java-encodings")) {
      return false;
    } else if (name.equals(kApache + "nonvalidating/load-external-dtd")) {
      return loadExternalDtd;
    } else if (name.equals(kApache + "disallow-doctype-decl")) {
      return disallowDoctype;
    } else if (name.equals(kApache + "continue-after-fatal-error")) {
      return false;
    } else if (name.equals(kSecure)) {
      return secureProcessing;
    }
    throw new SAXNotRecognizedException("Feature '" + name + "' is not recognized.");
  }

  public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (parsing && !name.equals(kFeatures + "use-entity-resolver2")) {
      throw new SAXNotSupportedException("Feature '" + name + "' is not supported while parsing.");
    }
    if (name.equals(kFeatures + "namespaces")) {
      namespaces = value;
    } else if (name.equals(kFeatures + "namespace-prefixes")) {
      namespacePrefixes = value;
    } else if (name.equals(kFeatures + "validation")) {
      validation = value;
    } else if (name.equals(kFeatures + "external-general-entities")) {
      externalGeneralEntities = value;
    } else if (name.equals(kFeatures + "external-parameter-entities")) {
      externalParameterEntities = value;
    } else if (name.equals(kFeatures + "use-entity-resolver2")) {
      useEntityResolver2 = value;
    } else if (name.equals(kFeatures + "resolve-dtd-uris")) {
      resolveDtdUris = value;
    } else if (name.equals(kFeatures + "lexical-handler/parameter-entities")) {
      lexicalParameterEntities = value;
    } else if (name.equals(kFeatures + "string-interning")) {
      if (!value) {
        throw new SAXNotSupportedException("False state for feature '" + name + "' is not supported.");
      }
    } else if (readOnlyFeature(name)) {
      throw new SAXNotSupportedException("Feature '" + name + "' is read only.");
    } else if (name.equals(kFeatures + "unicode-normalization-checking") || name.equals(kFeatures + "xmlns-uris")
        || name.equals(kFeatures + "allow-java-encodings")) {
      return;
    } else if (name.equals(kApache + "nonvalidating/load-external-dtd")) {
      loadExternalDtd = value;
    } else if (name.equals(kApache + "disallow-doctype-decl")) {
      disallowDoctype = value;
    } else if (name.equals(kApache + "continue-after-fatal-error")) {
      return;
    } else if (name.equals(kSecure)) {
      secureProcessing = value;
    } else {
      throw new SAXNotRecognizedException("Feature '" + name + "' is not recognized.");
    }
  }

  public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.equals(kProperties + "lexical-handler")) {
      return lexicalHandler;
    } else if (name.equals(kProperties + "declaration-handler")) {
      return declHandler;
    } else if (name.equals(kProperties + "document-xml-version")) {
      if (parser == null) {
        return "1.0";
      }
      return parser.version;
    } else if (name.equals(kProperties + "dom-node")) {
      throw new SAXNotSupportedException("Cannot read DOM node property. No DOM tree exists.");
    } else if (name.equals(kProperties + "xml-string")) {
      throw new SAXNotSupportedException("Property '" + name + "' is not supported.");
    } else if (name.startsWith("http://javax.xml.XMLConstants/property/") || name.startsWith("jdk.xml.")
        || name.startsWith("http://apache.org/xml/properties/") || name.startsWith("http://www.oracle.com/xml/")) {
      return properties.get(name);
    }
    throw new SAXNotRecognizedException("Property '" + name + "' is not recognized.");
  }

  public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (name == null) {
      throw new NullPointerException();
    }
    if (name.equals(kProperties + "lexical-handler")) {
      try {
        lexicalHandler = (LexicalHandler) value;
      } catch (ClassCastException e) {
        throw new SAXNotSupportedException("The value specified for property '" + name
            + "' cannot be casted to org.xml.sax.ext.LexicalHandler.");
      }
    } else if (name.equals(kProperties + "declaration-handler")) {
      try {
        declHandler = (DeclHandler) value;
      } catch (ClassCastException e) {
        throw new SAXNotSupportedException("The value specified for property '" + name
            + "' cannot be casted to org.xml.sax.ext.DeclHandler.");
      }
    } else if (name.equals(kProperties + "document-xml-version") || name.equals(kProperties + "dom-node")) {
      throw new SAXNotSupportedException("Property '" + name + "' is read only.");
    } else if (name.equals(kProperties + "xml-string")) {
      throw new SAXNotSupportedException("Property '" + name + "' is not supported.");
    } else if (name.startsWith("http://javax.xml.XMLConstants/property/") || name.startsWith("jdk.xml.")
        || name.startsWith("http://apache.org/xml/properties/") || name.startsWith("http://www.oracle.com/xml/")) {
      properties.put(name, value);
    } else {
      throw new SAXNotRecognizedException("Property '" + name + "' is not recognized.");
    }
  }

  public void setEntityResolver(EntityResolver resolver) {
    this.entityResolver = resolver;
  }

  public EntityResolver getEntityResolver() {
    return entityResolver;
  }

  public void setDTDHandler(DTDHandler handler) {
    this.dtdHandler = handler;
  }

  public DTDHandler getDTDHandler() {
    return dtdHandler;
  }

  public void setContentHandler(ContentHandler handler) {
    this.contentHandler = handler;
  }

  public ContentHandler getContentHandler() {
    return contentHandler;
  }

  public void setErrorHandler(ErrorHandler handler) {
    this.errorHandler = handler;
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void parse(String systemId) throws IOException, SAXException {
    parse(new InputSource(systemId));
  }

  private static final class Wrapped extends RuntimeException {
    private static final long serialVersionUID = 1L;
    final SAXException sax;

    Wrapped(SAXException sax) {
      super(sax);
      this.sax = sax;
    }
  }

  public void parse(InputSource source) throws IOException, SAXException {
    if (source == null) {
      throw new NullPointerException("The source specified cannot be null.");
    }
    if (parsing) {
      throw new SAXException("FWK005 parse may not be called while parsing.");
    }
    parsing = true;
    try {
      XmlInput input;
      try {
        input = XmlInput.open(source, null);
      } catch (XmlException e) {
        throw fatal(e);
      }
      XmlPullParser p = new XmlPullParser(input, new XmlPullParser.Resolver() {
        public XmlInput resolve(String name, String publicId, String systemId, String baseUri)
            throws IOException, XmlException {
          return resolveEntity(name, publicId, systemId, baseUri);
        }
      });
      actualEncoding = input.encodingName;
      documentSystemId = input.systemId;
      p.namespaceAware = namespaces;
      p.loadExternalDtd = loadExternalDtd && externalParameterEntities;
      p.externalGeneralEntities = externalGeneralEntities;
      parser = p;
      run(p);
    } catch (Wrapped w) {
      throw w.sax;
    } finally {
      parsing = false;
      if (parser != null) {
        parser.sc.closeAll();
      }
    }
  }

  private XmlInput resolveEntity(String name, String publicId, String systemId, String baseUri)
      throws IOException, XmlException {
    String expanded = XmlInput.expand(systemId, baseUri);
    try {
      InputSource src = null;
      if (entityResolver != null) {
        if (useEntityResolver2 && entityResolver instanceof EntityResolver2) {
          src = ((EntityResolver2) entityResolver).resolveEntity(name, publicId, baseUri, systemId);
        } else {
          src = entityResolver.resolveEntity(publicId, expanded);
        }
      }
      if (src != null) {
        if (src.getSystemId() == null && src.getByteStream() == null && src.getCharacterStream() == null) {
          src.setSystemId(expanded);
        }
        return XmlInput.open(src, baseUri);
      }
    } catch (SAXException e) {
      throw new Wrapped(e);
    }
    InputSource def = new InputSource(expanded);
    def.setPublicId(publicId);
    return XmlInput.open(def, null);
  }

  private SAXException fatal(XmlException e) throws SAXException {
    if (e.internal) {
      return new SAXException(e.getMessage());
    }
    SAXParseException ex = new SAXParseException(e.getMessage(), e.publicId, e.systemId, e.line, e.column);
    if (errorHandler != null) {
      errorHandler.fatalError(ex);
    } else {
      System.err.println("[Fatal Error] " + location(ex) + ": " + ex.getMessage());
    }
    return ex;
  }

  static String location(SAXParseException ex) {
    String id = ex.getSystemId();
    if (id == null) {
      id = "";
    } else {
      int slash = id.lastIndexOf('/');
      if (slash >= 0) {
        id = id.substring(slash + 1);
      }
    }
    return id + ":" + ex.getLineNumber() + ":" + ex.getColumnNumber();
  }

  private void run(XmlPullParser p) throws IOException, SAXException {
    try {
      Attributes2Impl attrs = new Attributes2Impl();
      while (true) {
        int kind = p.next();
        XmlPullParser.Ev ev = p.current();
        switch (kind) {
        case XmlPullParser.kStartDocument:
          if (contentHandler != null) {
            contentHandler.setDocumentLocator(this);
            contentHandler.startDocument();
          }
          break;
        case XmlPullParser.kEndDocument:
          if (contentHandler != null) {
            contentHandler.endDocument();
          }
          return;
        case XmlPullParser.kDtdStart:
          if (disallowDoctype) {
            throw new XmlException("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.",
                p.line(), p.column(), p.systemId(), p.publicId());
          }
          if (lexicalHandler != null) {
            lexicalHandler.startDTD(ev.a, ev.b, ev.c);
          }
          break;
        case XmlPullParser.kDtdEnd:
          if (lexicalHandler != null) {
            lexicalHandler.endDTD();
          }
          break;
        case XmlPullParser.kElementDecl:
          if (declHandler != null) {
            declHandler.elementDecl(ev.a, ev.b);
          }
          break;
        case XmlPullParser.kAttributeDecl:
          if (declHandler != null) {
            declHandler.attributeDecl(ev.a, ev.b, ev.c, ev.d, ev.e);
          }
          break;
        case XmlPullParser.kInternalEntityDecl:
          if (declHandler != null) {
            declHandler.internalEntityDecl(ev.a, ev.b);
          }
          break;
        case XmlPullParser.kExternalEntityDecl:
          if (declHandler != null) {
            declHandler.externalEntityDecl(ev.a, ev.b, ev.c);
          }
          break;
        case XmlPullParser.kUnparsedEntityDecl:
          if (dtdHandler != null) {
            dtdHandler.unparsedEntityDecl(ev.a, ev.b, ev.c, ev.d);
          }
          break;
        case XmlPullParser.kNotationDecl:
          if (dtdHandler != null) {
            dtdHandler.notationDecl(ev.a, ev.b, ev.c);
          }
          break;
        case XmlPullParser.kStartElement:
          startElement(ev, attrs);
          break;
        case XmlPullParser.kEndElement:
          endElement(ev);
          break;
        case XmlPullParser.kCharacters:
          if (contentHandler != null) {
            char[] c = ev.a.toCharArray();
            contentHandler.characters(c, 0, c.length);
          }
          break;
        case XmlPullParser.kIgnorableWhitespace:
          if (contentHandler != null) {
            char[] c = ev.a.toCharArray();
            contentHandler.ignorableWhitespace(c, 0, c.length);
          }
          break;
        case XmlPullParser.kCdataStart:
          if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
          }
          break;
        case XmlPullParser.kCdataEnd:
          if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
          }
          break;
        case XmlPullParser.kComment:
          if (lexicalHandler != null) {
            char[] c = ev.a.toCharArray();
            lexicalHandler.comment(c, 0, c.length);
          }
          break;
        case XmlPullParser.kProcessingInstruction:
          if (contentHandler != null) {
            contentHandler.processingInstruction(ev.a, ev.b);
          }
          break;
        case XmlPullParser.kEntityStart:
          if (lexicalHandler != null && (lexicalParameterEntities || !ev.a.startsWith("%"))) {
            lexicalHandler.startEntity(ev.a);
          }
          break;
        case XmlPullParser.kEntityEnd:
          if (lexicalHandler != null && (lexicalParameterEntities || !ev.a.startsWith("%"))) {
            lexicalHandler.endEntity(ev.a);
          }
          break;
        case XmlPullParser.kSkippedEntity:
          if (contentHandler != null) {
            contentHandler.skippedEntity(ev.a);
          }
          break;
        default:
          break;
        }
      }
    } catch (XmlException e) {
      throw fatal(e);
    } catch (XmlMalformedException e) {
      throw fatal(new XmlException(e.getMessage(), parser.sc.line(), parser.sc.col(), parser.systemId(),
          parser.publicId()));
    }
  }

  private void startElement(XmlPullParser.Ev ev, Attributes2Impl attrs) throws SAXException {
    if (contentHandler == null) {
      return;
    }
    attrs.clear();
    List<XmlPullParser.Attr> list = ev.attrs;
    if (namespaces) {
      for (int i = 0; i < ev.nsDecls.length; i += 2) {
        contentHandler.startPrefixMapping(ev.nsDecls[i], ev.nsDecls[i + 1]);
      }
      for (XmlPullParser.Attr a : list) {
        if (a.nsDecl) {
          if (!namespacePrefixes) {
            continue;
          }
          addAttr(attrs, "", "", a);
        } else {
          addAttr(attrs, a.uri, a.local, a);
        }
      }
      contentHandler.startElement(ev.uri, ev.local, ev.a, attrs);
    } else {
      for (XmlPullParser.Attr a : list) {
        addAttr(attrs, "", a.name, a);
      }
      contentHandler.startElement("", "", ev.a, attrs);
    }
  }

  private static void addAttr(Attributes2Impl attrs, String uri, String local, XmlPullParser.Attr a) {
    attrs.addAttribute(uri, local, a.name, a.type, a.value);
    int index = attrs.getLength() - 1;
    attrs.setSpecified(index, a.specified);
    attrs.setDeclared(index, a.declared);
  }

  private void endElement(XmlPullParser.Ev ev) throws SAXException {
    if (contentHandler == null) {
      return;
    }
    if (namespaces) {
      contentHandler.endElement(ev.uri, ev.local, ev.a);
      for (int i = 0; i < ev.nsDecls.length; i += 2) {
        contentHandler.endPrefixMapping(ev.nsDecls[i]);
      }
    } else {
      contentHandler.endElement("", "", ev.a);
    }
  }

  public String literalSystemId() {
    return parser == null || parser.current() == null ? null : parser.current().literal;
  }

  public boolean isStandalone() {
    return parser != null && parser.standalone;
  }

  public String getActualEncoding() {
    return actualEncoding;
  }

  public String getParsedSystemId() {
    return documentSystemId;
  }

  public String getPublicId() {
    return parser == null ? null : parser.publicId();
  }

  public String getSystemId() {
    return parser == null ? null : parser.systemId();
  }

  public int getLineNumber() {
    return parser == null ? -1 : parser.line();
  }

  public int getColumnNumber() {
    return parser == null ? -1 : parser.column();
  }

  public String getXMLVersion() {
    return parser == null ? null : parser.version;
  }

  public String getEncoding() {
    return parser == null ? null : parser.declaredEncoding;
  }
}
