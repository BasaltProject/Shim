/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

final class PropertiesXml {
  private static final String kDtdUri = "http://java.sun.com/dtd/properties.dtd";
  private static final String kPrefix = "jdk.internal.org.xml.sax.SAXParseException; ";

  private PropertiesXml() {
  }

  static void store(Properties props, OutputStream os, String comment, Charset charset)
      throws IOException {
    Objects.requireNonNull(os);
    Objects.requireNonNull(charset);
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"").append(charset.name()).append("\"?>\n");
    sb.append("<!DOCTYPE properties SYSTEM \"").append(kDtdUri).append("\">\n");
    sb.append("<properties>\n");
    CharsetEncoder enc = charset.newEncoder();
    if (comment != null && !comment.isEmpty()) {
      sb.append("<comment>");
      escape(sb, comment, false, enc);
      sb.append("</comment>\n");
    }
    synchronized (props) {
      for (Map.Entry<Object, Object> e : props.entrySet()) {
        String key = (String) e.getKey();
        String value = (String) e.getValue();
        sb.append("<entry key=\"");
        escape(sb, key, true, enc);
        sb.append("\">");
        escape(sb, value, false, enc);
        sb.append("</entry>\n");
      }
    }
    sb.append("</properties>\n");
    os.write(sb.toString().getBytes(charset));
    os.flush();
  }

  private static void escape(StringBuilder sb, String s, boolean attribute, CharsetEncoder enc) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '<') {
        sb.append("&lt;");
      } else if (c == '>') {
        sb.append("&gt;");
      } else if (c == '&') {
        sb.append("&amp;");
      } else if (c == '"' && attribute) {
        sb.append("&quot;");
      } else if (Character.isHighSurrogate(c) && i + 1 < s.length() && Character.isLowSurrogate(s.charAt(i + 1))) {
        sb.append("&#x").append(Integer.toHexString(Character.toCodePoint(c, s.charAt(i + 1)))).append(';');
        i++;
      } else if (c > 0x7f && !enc.canEncode(c)) {
        sb.append("&#x").append(Integer.toHexString(c)).append(';');
      } else {
        sb.append(c);
      }
    }
  }

  static Charset charsetFor(String name) throws UnsupportedEncodingException {
    Objects.requireNonNull(name);
    try {
      return Charset.forName(name);
    } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
      throw new UnsupportedEncodingException(name);
    }
  }

  private static final class Invalid extends SAXException {
    Invalid(String message) {
      super(message);
    }
  }

  private static final class Handler extends DefaultHandler implements LexicalHandler, DeclHandler {
    private final Properties props;
    private boolean doctype;
    private boolean subset;
    private boolean sawRoot;
    private boolean sawComment;
    private boolean inEntry;
    private String key;
    private final StringBuilder buf = new StringBuilder();

    Handler(Properties props) {
      this.props = props;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
      return new InputSource(new java.io.StringReader(""));
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
      if (kDtdUri.equals(systemId)) {
        doctype = true;
      }
    }

    private void declared() throws SAXException {
      subset = true;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException {
      if (!doctype) {
        throw new Invalid("An XML properties document must contain the DOCTYPE declaration as defined by "
            + "java.util.Properties.");
      }
      if (subset) {
        throw new Invalid("Internal DTD subset is not allowed. The Properties XML document must have the "
            + "following DOCTYPE declaration: \n<!DOCTYPE properties SYSTEM \"" + kDtdUri + "\">");
      }
      if (!sawRoot) {
        if (!qName.equals("properties")) {
          throw new Invalid("Document root element \"" + qName + "\", must match DOCTYPE root \"properties\"");
        }
        sawRoot = true;
        return;
      }
      if (qName.equals("comment")) {
        if (sawComment) {
          throw new Invalid("Only one comment element may be allowed. The content of element type "
              + "\"properties\" must match \"(comment?,entry*)\"");
        }
        sawComment = true;
      } else if (qName.equals("entry")) {
        key = attributes.getValue("key");
        if (key == null) {
          throw new Invalid("Attribute \"key\" is required and must be specified for element type \"entry\"");
        }
        inEntry = true;
        buf.setLength(0);
      } else {
        throw new Invalid("Element type \"" + qName + "\" must be declared.");
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
      if (inEntry) {
        buf.append(ch, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if (qName.equals("entry") && inEntry) {
        props.put(key, buf.toString());
        inEntry = false;
      }
    }

    @Override
    public void endDTD() {
    }

    @Override
    public void startEntity(String name) {
    }

    @Override
    public void endEntity(String name) {
    }

    @Override
    public void startCDATA() {
    }

    @Override
    public void endCDATA() {
    }

    @Override
    public void comment(char[] ch, int start, int length) {
    }

    @Override
    public void elementDecl(String name, String model) throws SAXException {
      declared();
    }

    @Override
    public void attributeDecl(String eName, String aName, String type, String mode, String value)
        throws SAXException {
      declared();
    }

    @Override
    public void internalEntityDecl(String name, String value) throws SAXException {
      declared();
    }

    @Override
    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
      declared();
    }
  }

  static void load(Properties props, InputStream in) throws IOException, InvalidPropertiesFormatException {
    Objects.requireNonNull(in);
    Handler handler = new Handler(props);
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(false);
      factory.setValidating(false);
      SAXParser parser = factory.newSAXParser();
      parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
      parser.setProperty("http://xml.org/sax/properties/declaration-handler", handler);
      parser.parse(new InputSource(in), handler);
    } catch (Invalid e) {
      throw new InvalidPropertiesFormatException(kPrefix + e.getMessage());
    } catch (SAXParseException e) {
      throw new InvalidPropertiesFormatException(kPrefix);
    } catch (SAXException e) {
      throw new InvalidPropertiesFormatException(kPrefix);
    } catch (ParserConfigurationException e) {
      throw new InvalidPropertiesFormatException(e);
    }
  }
}
