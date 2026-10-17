/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

public class DomImplementation implements DOMImplementation, DOMImplementationLS {
  private static final DomImplementation kInstance = new DomImplementation();

  public static DomImplementation getInstance() {
    return kInstance;
  }

  public boolean hasFeature(String feature, String version) {
    if (feature == null || feature.length() == 0) {
      return false;
    }
    boolean anyVersion = version == null || version.length() == 0;
    if (feature.charAt(0) == '+') {
      feature = feature.substring(1);
    }
    feature = feature.toLowerCase();
    if (feature.equals("core") || feature.equals("xml")) {
      return anyVersion || version.equals("1.0") || version.equals("2.0") || version.equals("3.0");
    }
    if (feature.equals("xmlversion")) {
      return anyVersion || version.equals("1.0") || version.equals("1.1");
    }
    if (feature.equals("events") || feature.equals("mutationevents") || feature.equals("traversal")
        || feature.equals("range")) {
      return anyVersion || version.equals("2.0");
    }
    if (feature.equals("ls")) {
      return anyVersion || version.equals("3.0");
    }
    if (feature.equals("elementtraversal")) {
      return anyVersion || version.equals("1.0");
    }
    return false;
  }

  public DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) {
    if (qualifiedName.length() == 0) {
      throw DomMessages.error(DOMException.NAMESPACE_ERR);
    }
    if (!DomNames.isName(qualifiedName)) {
      throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
    }
    int colon1 = qualifiedName.indexOf(':');
    int colon2 = qualifiedName.lastIndexOf(':');
    DomNames.checkNamespaceWf(qualifiedName, colon1 < 0 ? -1 : colon1, colon2);
    if (colon1 >= 0 && !DomNames.isName(qualifiedName.substring(0, colon1))) {
      throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
    }
    DocumentTypeImpl doctype = new DocumentTypeImpl(null, qualifiedName);
    doctype.publicId = publicId;
    doctype.systemId = systemId;
    return doctype;
  }

  public Document createDocument(String namespaceURI, String qualifiedName, DocumentType doctype) throws DOMException {
    if (doctype != null && doctype.getOwnerDocument() != null) {
      throw DomMessages.error(DOMException.WRONG_DOCUMENT_ERR);
    }
    DocumentImpl doc = new DocumentImpl();
    if (doctype != null) {
      doc.appendChild(doctype);
    }
    if (qualifiedName != null || namespaceURI != null) {
      Element e = doc.createElementNS(namespaceURI, qualifiedName);
      doc.appendChild(e);
    }
    return doc;
  }

  public Object getFeature(String feature, String version) {
    if (feature.equalsIgnoreCase("+Core") || feature.equalsIgnoreCase("Core") || feature.equalsIgnoreCase("XML")
        || feature.equalsIgnoreCase("+XML") || feature.equalsIgnoreCase("LS") || feature.equalsIgnoreCase("+LS")) {
      if (hasFeature(feature, version)) {
        return this;
      }
    }
    return null;
  }

  public LSParser createLSParser(short mode, String schemaType) throws DOMException {
    if (mode != DOMImplementationLS.MODE_SYNCHRONOUS) {
      throw DomMessages.error(DOMException.NOT_SUPPORTED_ERR);
    }
    if (schemaType != null && !schemaType.equals("http://www.w3.org/2001/XMLSchema")
        && !schemaType.equals("http://www.w3.org/TR/REC-xml")) {
      throw DomMessages.error(DOMException.NOT_SUPPORTED_ERR);
    }
    return new DomLsParser();
  }

  public LSSerializer createLSSerializer() {
    return new DomLsSerializer();
  }

  public LSInput createLSInput() {
    return new DomLsInput();
  }

  public LSOutput createLSOutput() {
    return new DomLsOutput();
  }
}
