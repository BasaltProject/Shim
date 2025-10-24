/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSParserFilter;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import shim.xml.parsers.DocumentBuilderFactoryImpl;

public class DomLsParser implements LSParser {
  private final DomParamConfig config = DomParamConfig.forParser();
  private LSParserFilter filter;

  public DOMConfiguration getDomConfig() {
    return config;
  }

  public LSParserFilter getFilter() {
    return filter;
  }

  public void setFilter(LSParserFilter filter) {
    this.filter = filter;
  }

  public boolean getAsync() {
    return false;
  }

  public boolean getBusy() {
    return false;
  }

  private static InputSource source(LSInput input) throws LSException {
    InputSource src = new InputSource();
    if (input.getCharacterStream() != null) {
      src.setCharacterStream(input.getCharacterStream());
    } else if (input.getByteStream() != null) {
      src.setByteStream(input.getByteStream());
      if (input.getEncoding() != null) {
        src.setEncoding(input.getEncoding());
      }
    } else if (input.getStringData() != null && !input.getStringData().isEmpty()) {
      src.setCharacterStream(new StringReader(input.getStringData()));
    } else if (input.getSystemId() == null || input.getSystemId().isEmpty()) {
      throw new LSException(LSException.PARSE_ERR, "no-input-specified");
    }
    if (input.getSystemId() != null) {
      src.setSystemId(input.getSystemId());
    }
    if (input.getPublicId() != null) {
      src.setPublicId(input.getPublicId());
    }
    return src;
  }

  public Document parse(LSInput input) throws DOMException, LSException {
    if (input == null) {
      throw new NullPointerException("Cannot invoke \"org.w3c.dom.ls.LSInput.getCharacterStream()\" because \"is\" "
          + "is null");
    }
    return parse(source(input), input.getCharacterStream() != null || input.getStringData() != null
        && !input.getStringData().isEmpty());
  }

  public Document parseURI(String uri) throws DOMException, LSException {
    if (uri == null) {
      throw new LSException(LSException.PARSE_ERR, "Cannot invoke \"String.length()\" because \"spec\" is null");
    }
    InputSource src = new InputSource(uri);
    return parse(src, false);
  }

  private Document parse(InputSource src, boolean chars) throws LSException {
    boolean viaBytes = !chars && src.getByteStream() != null;
    String explicitEncoding = src.getEncoding();
    try {
      DocumentBuilderFactoryImpl factory = new DocumentBuilderFactoryImpl();
      factory.setNamespaceAware(config.bool("namespaces"));
      factory.setCoalescing(!config.bool("cdata-sections"));
      factory.setIgnoringComments(!config.bool("comments"));
      factory.setIgnoringElementContentWhitespace(!config.bool("element-content-whitespace"));
      factory.setExpandEntityReferences(!config.bool("entities"));
      if (config.bool("disallow-doctype")) {
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      }
      DocumentBuilder builder = factory.newDocumentBuilder();
      final DOMErrorHandler handler = (DOMErrorHandler) config.value("error-handler");
      if (handler != null) {
        builder.setErrorHandler(new ErrorHandler() {
          public void warning(SAXParseException e) throws SAXException {
            report(handler, DOMError.SEVERITY_WARNING, e);
          }

          public void error(SAXParseException e) throws SAXException {
            report(handler, DOMError.SEVERITY_ERROR, e);
          }

          public void fatalError(SAXParseException e) throws SAXException {
            report(handler, DOMError.SEVERITY_FATAL_ERROR, e);
            throw e;
          }
        });
      }
      final LSResourceResolver resolver = (LSResourceResolver) config.value("resource-resolver");
      if (resolver != null) {
        builder.setEntityResolver(new EntityResolver() {
          public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            String base = "file:" + System.getProperty("user.dir") + "/";
            String written = systemId != null && systemId.startsWith(base) ? systemId.substring(base.length())
                : systemId != null && systemId.startsWith("file://" + System.getProperty("user.dir") + "/")
                ? systemId.substring(("file://" + System.getProperty("user.dir") + "/").length()) : systemId;
            LSInput in = resolver.resolveResource("http://www.w3.org/TR/REC-xml", null, publicId, written, null);
            return in == null ? null : source(in);
          }
        });
      }
      Document doc = builder.parse(src);
      if (chars && doc instanceof DocumentImpl) {
        ((DocumentImpl) doc).setInputEncoding("UTF-16");
      } else if (viaBytes && doc instanceof DocumentImpl && doc.getInputEncoding() != null
          && !doc.getInputEncoding().startsWith("UTF-16")) {
        ((DocumentImpl) doc).setInputEncoding(explicitEncoding != null ? explicitEncoding : "UTF-8");
      }
      if (filter != null) {
        applyFilter(doc);
      }
      return doc;
    } catch (SAXException e) {
      throw new LSException(LSException.PARSE_ERR, e.getMessage());
    } catch (IOException e) {
      throw new LSException(LSException.PARSE_ERR, e.getMessage());
    } catch (ParserConfigurationException e) {
      throw new LSException(LSException.PARSE_ERR, e.getMessage());
    }
  }

  private static void report(DOMErrorHandler handler, short severity, SAXParseException e) {
    handler.handleError(new DomError(severity, e.getMessage(), "xml-parse-error", e, e.getLineNumber(),
        e.getColumnNumber(), e.getSystemId()));
  }

  private void applyFilter(Node parent) {
    Node child = parent.getFirstChild();
    while (child != null) {
      Node next = child.getNextSibling();
      short verdict = LSParserFilter.FILTER_ACCEPT;
      if (child.getNodeType() == Node.ELEMENT_NODE && shows(child)) {
        verdict = filter.startElement((Element) child);
      }
      if (verdict == LSParserFilter.FILTER_REJECT) {
        parent.removeChild(child);
      } else {
        applyFilter(child);
        if (shows(child)) {
          short second = child.getNodeType() == Node.ELEMENT_NODE && verdict == LSParserFilter.FILTER_SKIP
              ? LSParserFilter.FILTER_SKIP : filter.acceptNode(child);
          if (second == LSParserFilter.FILTER_REJECT) {
            parent.removeChild(child);
          } else if (second == LSParserFilter.FILTER_SKIP) {
            Node kid = child.getFirstChild();
            while (kid != null) {
              Node nextKid = kid.getNextSibling();
              parent.insertBefore(kid, child);
              kid = nextKid;
            }
            parent.removeChild(child);
          }
        }
      }
      child = next;
    }
  }

  private boolean shows(Node n) {
    return (filter.getWhatToShow() & (1 << (n.getNodeType() - 1))) != 0;
  }

  public Node parseWithContext(LSInput input, Node contextArg, short action) throws DOMException, LSException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not supported");
  }

  public void abort() {
  }
}
