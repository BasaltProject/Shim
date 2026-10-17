/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.transform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import shim.xml.SaxReader;
import shim.xml.dom.DomBuilder;
import shim.xml.dom.DocumentImpl;
import shim.xml.ser.DomToSax;
import shim.xml.ser.XmlSerializer;

public class IdentityTransformer extends Transformer {
  private static final String kIndentAmount = "{http://xml.apache.org/xslt}indent-amount";

  private final TransformerFactoryImpl factory;
  private boolean domSourceMarker;
  private Properties outputProperties = new Properties();
  private final Map<String, Object> parameters = new HashMap<String, Object>();
  private URIResolver uriResolver;
  private ErrorListener errorListener;

  IdentityTransformer(TransformerFactoryImpl factory) {
    this.factory = factory;
    this.uriResolver = factory.getURIResolver();
    this.errorListener = factory.getErrorListener();
  }

  IdentityTransformer copy() {
    IdentityTransformer c = new IdentityTransformer(factory);
    c.outputProperties.putAll(outputProperties);
    c.parameters.putAll(parameters);
    c.uriResolver = uriResolver;
    c.errorListener = errorListener;
    return c;
  }

  public void reset() {
    outputProperties = new Properties();
    parameters.clear();
    uriResolver = factory.getURIResolver();
    errorListener = factory.getErrorListener();
  }

  static String defaultOf(String name) {
    switch (name) {
    case OutputKeys.METHOD: return "xml";
    case OutputKeys.VERSION: return "1.0";
    case OutputKeys.ENCODING: return "UTF-8";
    case OutputKeys.OMIT_XML_DECLARATION: return "no";
    case OutputKeys.STANDALONE: return "no";
    case OutputKeys.INDENT: return "no";
    case OutputKeys.MEDIA_TYPE: return "text/xml";
    default: return null;
    }
  }

  private static boolean validProperty(String name) {
    if (name.startsWith("{")) {
      return name.indexOf('}') > 0;
    }
    return name.equals(OutputKeys.METHOD) || name.equals(OutputKeys.VERSION) || name.equals(OutputKeys.ENCODING)
        || name.equals(OutputKeys.OMIT_XML_DECLARATION) || name.equals(OutputKeys.STANDALONE)
        || name.equals(OutputKeys.DOCTYPE_PUBLIC) || name.equals(OutputKeys.DOCTYPE_SYSTEM)
        || name.equals(OutputKeys.CDATA_SECTION_ELEMENTS) || name.equals(OutputKeys.INDENT)
        || name.equals(OutputKeys.MEDIA_TYPE);
  }

  public void setOutputProperties(Properties oformat) throws IllegalArgumentException {
    if (oformat == null) {
      outputProperties = new Properties();
      return;
    }
    for (String name : oformat.stringPropertyNames()) {
      if (!validProperty(name)) {
        throw new IllegalArgumentException("Attempting to access invalid Transformer property '" + name + "'.");
      }
    }
    outputProperties = new Properties();
    outputProperties.putAll(oformat);
  }

  public Properties getOutputProperties() {
    Properties copy = new Properties();
    copy.putAll(outputProperties);
    return copy;
  }

  public void setOutputProperty(String name, String value) throws IllegalArgumentException {
    if (!validProperty(name)) {
      throw new IllegalArgumentException("Attempting to access invalid Transformer property '" + name + "'.");
    }
    outputProperties.setProperty(name, value);
  }

  public String getOutputProperty(String name) throws IllegalArgumentException {
    if (!validProperty(name)) {
      throw new IllegalArgumentException("Attempting to access invalid Transformer property '" + name + "'.");
    }
    String v = outputProperties.getProperty(name);
    if (v != null) {
      return v;
    }
    return defaultOf(name);
  }

  public void setParameter(String name, Object value) {
    if (name == null) {
      throw new NullPointerException("Invalid parameter name 'null'");
    }
    if (value == null) {
      throw new IllegalArgumentException("The value of param " + name + " must be a valid Java Object");
    }
    parameters.put(name, value);
  }

  public Object getParameter(String name) {
    return parameters.get(name);
  }

  public void clearParameters() {
    parameters.clear();
  }

  public void setURIResolver(URIResolver resolver) {
    this.uriResolver = resolver;
  }

  public URIResolver getURIResolver() {
    return uriResolver;
  }

  public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
    if (listener == null) {
      throw new IllegalArgumentException("Attempting to set ErrorListener for 'Transformer' to null");
    }
    this.errorListener = listener;
  }

  public ErrorListener getErrorListener() {
    return errorListener;
  }

  String property(String name) {
    String v = outputProperties.getProperty(name);
    return v != null ? v : defaultOf(name);
  }

  public void transform(Source source, Result result) throws TransformerException {
    if (source == null) {
      throw new NullPointerException("Source object passed to 'transform' is null.");
    }
    domSourceMarker = !(source instanceof StreamSource);
    if (source instanceof SAXSource && ((SAXSource) source).getXMLReader() != null) {
      try {
        domSourceMarker = !((SAXSource) source).getXMLReader().getFeature("http://xml.org/sax/features/namespaces");
      } catch (SAXException e) {
        domSourceMarker = true;
      }
    }
    SerializationTarget target = openTarget(result);
    try {
      ContentHandler handler = target.handler;
      LexicalHandler lexical = target.lexical;
      if (source instanceof DOMSource) {
        Node node = ((DOMSource) source).getNode();
        if (node == null) {
          if (target.serializer != null) {
            target.serializer.setDocumentStandalone("no");
          }
          handler.startDocument();
          handler.endDocument();
          target.finish();
          return;
        }
        boolean isDocument = node.getNodeType() == Node.DOCUMENT_NODE;
        if (target.serializer != null && isDocument) {
          Document document = (Document) node;
          target.serializer.setDocumentStandalone(document.getXmlStandalone() ? "yes" : "no");
          target.serializer.setDocumentStandaloneIsDefault(document.getXmlStandalone());
          target.serializer.setVersion(document.getXmlVersion());
        }
        if (!isDocument) {
          handler.startDocument();
        }
        try {
          new DomToSax(handler, lexical, true).parse(node);
        } catch (RuntimeException e) {
          throw new TransformerException(e);
        }
        if (!isDocument) {
          handler.endDocument();
        }
      } else if (source instanceof SAXSource) {
        SAXSource ss = (SAXSource) source;
        XMLReader reader = ss.getXMLReader();
        if (reader == null) {
          reader = new SaxReader(true);
          try {
            reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
          } catch (SAXException ignored) {
          }
        }
        reader.setContentHandler(handler);
        try {
          reader.setProperty("http://xml.org/sax/properties/lexical-handler", lexical);
        } catch (SAXException ignored) {
        }
        reader.parse(ss.getInputSource());
      } else if (source instanceof StreamSource) {
        StreamSource st = (StreamSource) source;
        InputSource is = SAXSource.sourceToInputSource(st);
        if (is == null || (is.getByteStream() == null && is.getCharacterStream() == null && is.getSystemId() == null)) {
          throw new TransformerException(new TransformerException(
              "Source object passed to ''{0}'' has no contents."));
        }
        SaxReader reader = new SaxReader(true);
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        reader.setContentHandler(handler);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", lexical);
        reader.setErrorHandler(new PrintingHandler());
        reader.parse(is);
      } else {
        throw new TransformerException("Source object passed to 'transform' is invalid.");
      }
      target.finish();
    } catch (TransformerException e) {
      throw e;
    } catch (SAXParseException e) {
      throw new TransformerException(e);
    } catch (SAXException e) {
      throw new TransformerException(e);
    } catch (IOException e) {
      throw new TransformerException(e);
    } finally {
      target.close();
    }
  }

  private static final class PrintingHandler implements org.xml.sax.ErrorHandler {
    public void warning(SAXParseException e) {
    }

    public void error(SAXParseException e) {
    }

    public void fatalError(SAXParseException e) throws SAXException {
      System.err.println("[Fatal Error] " + location(e) + ": " + e.getMessage());
      throw e;
    }

    private static String location(SAXParseException e) {
      String id = e.getSystemId();
      if (id == null) {
        id = "";
      } else {
        int slash = id.lastIndexOf('/');
        if (slash >= 0) {
          id = id.substring(slash + 1);
        }
      }
      return id + ":" + e.getLineNumber() + ":" + e.getColumnNumber();
    }
  }

  static final class SerializationTarget {
    ContentHandler handler;
    LexicalHandler lexical;
    XmlSerializer serializer;
    Writer writer;
    OutputStream owned;
    DomBuilder builder;
    DOMResult domResult;

    void finish() throws IOException {
      if (writer != null) {
        writer.flush();
      }
      if (builder != null) {
        org.w3c.dom.Document doc = builder.document();
        deliver(doc);
      }
    }

    private void deliver(org.w3c.dom.Document doc) {
      Node node = domResult.getNode();
      if (node == null) {
        domResult.setNode(doc);
        return;
      }
      Node next = domResult.getNextSibling();
      org.w3c.dom.Document owner = node.getNodeType() == Node.DOCUMENT_NODE ? (org.w3c.dom.Document) node
          : node.getOwnerDocument();
      for (Node c = doc.getFirstChild(); c != null; ) {
        Node following = c.getNextSibling();
        if (c.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
          Node imported = owner.importNode(c, true);
          node.insertBefore(imported, next);
        }
        c = following;
      }
    }

    void close() {
      try {
        if (writer != null) {
          writer.flush();
        }
        if (owned != null) {
          owned.close();
        }
      } catch (IOException ignored) {
      }
    }
  }

  SerializationTarget openTarget(Result result) throws TransformerException {
    if (result == null) {
      throw new NullPointerException("Result object passed to 'transform' is null.");
    }
    SerializationTarget t = new SerializationTarget();
    if (result instanceof StreamResult) {
      StreamResult sr = (StreamResult) result;
      Writer w = sr.getWriter();
      String encoding = property(OutputKeys.ENCODING);
      if (w == null) {
        OutputStream os = sr.getOutputStream();
        if (os == null && sr.getSystemId() != null) {
          try {
            String id = sr.getSystemId();
            File f = id.startsWith("file:") ? new File(URI.create(id.replace(" ", "%20"))) : new File(id);
            os = new FileOutputStream(f);
            t.owned = os;
          } catch (IOException e) {
            throw new TransformerException(e);
          }
        }
        if (os == null) {
          throw new TransformerException("Result object passed to ''{0}'' is invalid.");
        }
        Charset cs;
        try {
          cs = Charset.forName(encoding);
        } catch (RuntimeException e) {
          cs = Charset.forName("UTF-8");
        }
        w = new OutputStreamWriter(os, cs);
      }
      t.writer = w;
      XmlSerializer ser = new XmlSerializer(w);
      configure(ser);
      t.serializer = ser;
      t.handler = ser;
      t.lexical = ser;
      return t;
    }
    if (result instanceof SAXResult) {
      SAXResult sr = (SAXResult) result;
      if (sr.getHandler() == null) {
        throw new TransformerException("Result object passed to ''{0}'' is invalid.");
      }
      LexicalHandler lex = sr.getLexicalHandler() != null ? sr.getLexicalHandler()
          : (sr.getHandler() instanceof LexicalHandler ? (LexicalHandler) sr.getHandler() : null);
      SaxResultAdapter adapter = new SaxResultAdapter(sr.getHandler(), lex);
      t.handler = adapter;
      t.lexical = adapter;
      return t;
    }
    if (result instanceof DOMResult) {
      DOMResult dr = (DOMResult) result;
      DomBuilder builder = new DomBuilder(null, true, false, true, false, true, null);
      t.builder = builder;
      t.domResult = dr;
      t.handler = builder;
      t.lexical = builder;
      return t;
    }
    throw new TransformerException("No defined output handler for transformation result.");
  }

  private void configure(XmlSerializer ser) throws TransformerException {
    ser.setMethod(property(OutputKeys.METHOD));
    ser.setVersion(property(OutputKeys.VERSION));
    ser.setEncoding(property(OutputKeys.ENCODING));
    ser.setOmitDeclaration("yes".equals(property(OutputKeys.OMIT_XML_DECLARATION)));
    ser.setStandalone(outputProperties.getProperty(OutputKeys.STANDALONE));
    ser.setIndent("yes".equals(property(OutputKeys.INDENT)));
    ser.setHoistDefaultNamespace(!(domSourceMarker));
    ser.setMediaType(property(OutputKeys.MEDIA_TYPE));
    ser.setDoctype(property(OutputKeys.DOCTYPE_PUBLIC), property(OutputKeys.DOCTYPE_SYSTEM));
    String cdata = property(OutputKeys.CDATA_SECTION_ELEMENTS);
    if (cdata != null) {
      for (String n : cdata.trim().split("\\s+")) {
        if (!n.isEmpty()) {
          ser.addCdataElement(n);
        }
      }
    }
    Object amount = outputProperties.getProperty(kIndentAmount);
    if (amount == null) {
      Object fromFactory = factory.attribute("indent-number");
      if (fromFactory != null) {
        amount = fromFactory.toString();
      }
    }
    if (amount != null) {
      ser.setIndentAmount(Integer.parseInt(amount.toString()));
    }
  }
}
