/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSSerializerFilter;
import org.xml.sax.SAXException;
import shim.xml.ser.DomToSax;
import shim.xml.ser.XmlSerializer;

public class DomLsSerializer implements LSSerializer {
  private final DomParamConfig config = DomParamConfig.forSerializer();
  private String newLine = "\n";
  private LSSerializerFilter filter;

  public DOMConfiguration getDomConfig() {
    return config;
  }

  public String getNewLine() {
    return newLine;
  }

  public void setNewLine(String newLine) {
    this.newLine = newLine == null ? "\n" : newLine;
  }

  public LSSerializerFilter getFilter() {
    return filter;
  }

  public void setFilter(LSSerializerFilter filter) {
    this.filter = filter;
  }

  private static String doctypeText(DocumentType dt) {
    StringBuilder sb = new StringBuilder("<!DOCTYPE ").append(dt.getName());
    if (dt.getPublicId() != null) {
      sb.append(" PUBLIC \"").append(dt.getPublicId()).append('"');
      if (dt.getSystemId() != null) {
        sb.append(" \"").append(dt.getSystemId()).append('"');
      }
    } else if (dt.getSystemId() != null) {
      sb.append(" SYSTEM \"").append(dt.getSystemId()).append('"');
    }
    String internal = dt.getInternalSubset();
    if (internal != null && !internal.isEmpty()) {
      sb.append(" [ \n").append(internal).append(internal.endsWith("\n") ? "" : "\n").append(']');
    }
    return sb.append(">\n").toString();
  }

  private void serialize(Node node, Writer out, String encoding) throws IOException, SAXException {
    XmlSerializer ser = new XmlSerializer(out);
    ser.setEncoding(encoding);
    ser.setLsMode(true);
    ser.setOmitDeclaration(!config.bool("xml-declaration"));
    boolean pretty = config.bool("format-pretty-print");
    ser.setIndent(pretty);
    ser.setSkipBlankText(pretty);
    Document owner = node.getNodeType() == Node.DOCUMENT_NODE ? (Document) node : node.getOwnerDocument();
    ser.setVersion(owner == null ? "1.0" : owner.getXmlVersion());
    DomToSax walker = new DomToSax(ser, ser, true);
    walker.lsMode = true;
    walker.comments = config.bool("comments");
    walker.cdataSections = config.bool("cdata-sections");
    walker.entities = config.bool("entities");
    walker.namespaceDeclarations = config.bool("namespace-declarations");
    walker.attributeFixup = config.bool("namespaces");
    walker.discardDefaults = config.bool("discard-default-content");
    walker.splitCdata = config.bool("split-cdata-sections");
    walker.filter = filter;
    if (node.getNodeType() == Node.DOCUMENT_NODE) {
      DocumentType dt = ((Document) node).getDoctype();
      if (dt != null) {
        ser.setRawDoctype(doctypeText(dt));
      }
      walker.parse(node);
    } else {
      ser.startDocument();
      walker.parse(node);
      ser.endDocument();
    }
    out.flush();
  }

  private String replaceNewLines(String s) {
    return newLine.equals("\n") ? s : s.replace("\n", newLine);
  }

  public boolean write(Node nodeArg, LSOutput destination) throws LSException {
    if (nodeArg == null) {
      return false;
    }
    try {
      Writer target = null;
      OutputStream owned = null;
      String encoding = destination.getEncoding();
      if (destination.getCharacterStream() != null) {
        target = destination.getCharacterStream();
        if (encoding == null) {
          encoding = "UTF-16";
        }
      } else if (destination.getByteStream() != null) {
        if (encoding == null) {
          throw new NullPointerException("charsetName");
        }
        target = new OutputStreamWriter(destination.getByteStream(), Charset.forName(encoding));
      } else if (destination.getSystemId() != null) {
        owned = openUri(destination.getSystemId());
        if (encoding == null) {
          encoding = "UTF-8";
        }
        target = new OutputStreamWriter(owned, Charset.forName(encoding));
      } else {
        throw new LSException(LSException.SERIALIZE_ERR, "The output destination for data to be written to was null.");
      }
      StringWriter buffer = new StringWriter();
      serialize(nodeArg, buffer, encoding);
      target.write(replaceNewLines(buffer.toString()));
      target.flush();
      if (owned != null) {
        owned.close();
      }
      return true;
    } catch (LSException e) {
      throw e;
    } catch (Exception e) {
      throw new LSException(LSException.SERIALIZE_ERR, e.getMessage());
    }
  }

  private static OutputStream openUri(String uri) throws IOException {
    if (uri.startsWith("file:")) {
      try {
        return new FileOutputStream(new File(new URI(uri)));
      } catch (java.net.URISyntaxException e) {
        throw new IOException(e.getMessage());
      }
    }
    URL url = new URL(uri);
    return url.openConnection().getOutputStream();
  }

  public boolean writeToURI(Node nodeArg, String uri) throws LSException {
    try {
      OutputStream out = openUri(uri);
      Writer w = new OutputStreamWriter(out, "UTF-8");
      StringWriter buffer = new StringWriter();
      serialize(nodeArg, buffer, "UTF-8");
      w.write(replaceNewLines(buffer.toString()));
      w.close();
      return true;
    } catch (Exception e) {
      throw new LSException(LSException.SERIALIZE_ERR, e.toString());
    }
  }

  public String writeToString(Node nodeArg) throws LSException {
    if (nodeArg == null) {
      return null;
    }
    try {
      StringWriter sw = new StringWriter();
      serialize(nodeArg, sw, "UTF-16");
      return replaceNewLines(sw.toString());
    } catch (Exception e) {
      throw new LSException(LSException.SERIALIZE_ERR, e.toString());
    }
  }
}
