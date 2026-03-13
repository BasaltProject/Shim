/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Attributes2;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.Locator2;
import shim.xml.SaxReader;

public final class DomBuilder extends DefaultHandler2 implements DeclHandler {
  private final SaxReader reader;
  private final boolean namespaceAware;
  private final boolean ignoreComments;
  private final boolean coalescing;
  private final boolean ignoreElementContentWhitespace;
  private final boolean createEntityRefs;

  private DocumentImpl doc;
  private ParentNode current;
  private Locator locator;
  private boolean inCdata;
  private CDATASectionImpl cdata;
  private DocumentTypeImpl doctype;
  private StringBuilder subset;
  private boolean inExternalSubset;
  private int entityRefDepth;
  private final List<EntityReferenceImpl> entityRefs = new ArrayList<EntityReferenceImpl>();
  private int skipDepth;
  private final String systemId;

  public DomBuilder(SaxReader reader, boolean namespaceAware, boolean ignoreComments, boolean coalescing,
                    boolean ignoreElementContentWhitespace, boolean expandEntityReferences, String systemId) {
    this.reader = reader;
    this.namespaceAware = namespaceAware;
    this.ignoreComments = ignoreComments;
    this.coalescing = coalescing;
    this.ignoreElementContentWhitespace = ignoreElementContentWhitespace;
    this.createEntityRefs = !expandEntityReferences;
    this.systemId = systemId;
  }

  public DocumentImpl document() {
    return doc;
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void startDocument() throws SAXException {
    doc = new DocumentImpl();
    current = doc;
    doc.documentUri = systemId;
    if (locator instanceof Locator2) {
      Locator2 l2 = (Locator2) locator;
      String v = l2.getXMLVersion();
      if (v != null) {
        doc.version = v;
      }
      doc.encoding = l2.getEncoding();
    }
  }

  public void endDocument() throws SAXException {
    if (locator instanceof Locator2) {
      Locator2 l2 = (Locator2) locator;
      String v = l2.getXMLVersion();
      if (v != null) {
        doc.version = v;
      }
      doc.encoding = l2.getEncoding();
    }
    if (reader != null) {
      doc.standalone = reader.isStandalone();
      doc.actualEncoding = reader.getActualEncoding();
    }
    doc.errorChecking = true;
  }

  private void add(NodeImpl child) {
    current.appendCloneChild(child);
    child.ownerDocument = doc;
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if (skipDepth > 0) {
      skipDepth++;
      return;
    }
    if (entityRefDepth > 0) {
      skipDepth = 1;
      return;
    }
    ElementImpl el;
    if (namespaceAware) {
      String ns = uri != null && uri.length() > 0 ? uri : null;
      el = new ElementImpl(doc, ns, qName, localName);
    } else {
      el = new ElementImpl(doc, qName);
    }
    int length = atts.getLength();
    if (length > 0) {
      el.attributes = new AttributeMap(el);
    }
    String pendingType = null;
    List<AttrImpl> pendingUndeclared = new ArrayList<AttrImpl>();
    for (int i = 0; i < length; i++) {
      String aq = atts.getQName(i);
      AttrImpl attr;
      if (namespaceAware) {
        String au = atts.getURI(i);
        String al = atts.getLocalName(i);
        if (aq.equals("xmlns")) {
          attr = new AttrImpl(doc, DomNames.kXmlnsUri, aq, "xmlns");
        } else if (aq.startsWith("xmlns:")) {
          attr = new AttrImpl(doc, DomNames.kXmlnsUri, aq, aq.substring(6));
        } else {
          attr = new AttrImpl(doc, au != null && au.length() > 0 ? au : null, aq, al);
        }
      } else {
        attr = new AttrImpl(doc, aq);
      }
      attr.setValueQuiet(atts.getValue(i));
      attr.ownerElement = el;
      String type = atts.getType(i);
      boolean declared = atts instanceof Attributes2 && ((Attributes2) atts).isDeclared(i);
      if (atts instanceof Attributes2 && !((Attributes2) atts).isSpecified(i)) {
        attr.specified = false;
      }
      if (declared) {
        attr.dtdType = type;
        if (type.equals("ID")) {
          attr.idAttribute = true;
        }
        for (AttrImpl waiting : pendingUndeclared) {
          waiting.dtdType = type;
        }
        pendingUndeclared.clear();
      } else if (atts instanceof Attributes2) {
        pendingUndeclared.add(attr);
      }
      int pos = el.attributes.findName(aq);
      el.attributes.nodes.add(pos >= 0 ? pos : -(pos + 1), attr);
    }
    add(el);
    current = el;
    if (recordPositions && locator != null) {
      el.setUserData("shim.position", new int[] {locator.getLineNumber(), locator.getColumnNumber()}, null);
    }
  }

  private boolean recordPositions;

  public void recordPositions() {
    recordPositions = true;
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (skipDepth > 0) {
      skipDepth--;
      return;
    }
    current = (ParentNode) current.parentNode;
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    if (skipDepth > 0 || entityRefDepth > 0) {
      return;
    }
    String s = new String(ch, start, length);
    if (inCdata && !coalescing) {
      if (cdata == null) {
        cdata = new CDATASectionImpl(doc, s);
        add(cdata);
      } else {
        cdata.data = cdata.data + s;
      }
      return;
    }
    NodeImpl last = current.lastChildNodeOrNull();
    if (last != null && last.getNodeType() == Node.TEXT_NODE && !((TextImpl) last).ignorableWhitespace) {
      ((TextImpl) last).data = ((TextImpl) last).data + s;
    } else {
      add(new TextImpl(doc, s));
    }
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    if (skipDepth > 0 || entityRefDepth > 0 || ignoreElementContentWhitespace) {
      return;
    }
    TextImpl t = new TextImpl(doc, new String(ch, start, length));
    t.ignorableWhitespace = true;
    add(t);
  }

  public void processingInstruction(String target, String data) throws SAXException {
    if (skipDepth > 0 || entityRefDepth > 0) {
      return;
    }
    add(new ProcessingInstructionImpl(doc, target, data));
  }

  public void comment(char[] ch, int start, int length) throws SAXException {
    if (doctype != null && !doctypeClosed) {
      if (!inExternalSubset) {
        subset.append("<!--").append(ch, start, length).append("-->");
      }
      return;
    }
    if (ignoreComments || skipDepth > 0 || entityRefDepth > 0) {
      return;
    }
    add(new CommentImpl(doc, new String(ch, start, length)));
  }

  public void startCDATA() throws SAXException {
    inCdata = true;
    cdata = null;
    if (!coalescing && entityRefDepth == 0 && skipDepth == 0) {
      cdata = new CDATASectionImpl(doc, "");
      add(cdata);
    }
  }

  public void endCDATA() throws SAXException {
    inCdata = false;
    cdata = null;
  }

  private boolean doctypeClosed;

  public void startDTD(String name, String publicId, String systemId) throws SAXException {
    doctype = new DocumentTypeImpl(doc, name);
    doctype.publicId = publicId;
    doctype.systemId = systemId;
    subset = new StringBuilder();
    doctypeClosed = false;
    doc.appendCloneChild(doctype);
    doctype.ownerDocument = doc;
  }

  public void endDTD() throws SAXException {
    doctypeClosed = true;
    if (subset.length() > 0) {
      doctype.internalSubset = subset.toString();
    }
  }

  public void startEntity(String name) throws SAXException {
    if (name.equals("[dtd]")) {
      inExternalSubset = true;
      return;
    }
    if (name.startsWith("%")) {
      return;
    }
    if (createEntityRefs && !isPredefined(name) && skipDepth == 0 && !doctypeOpen()) {
      EntityReferenceImpl ref = new EntityReferenceImpl(doc, name);
      add(ref);
      entityRefs.add(ref);
      entityRefDepth++;
    }
  }

  private boolean doctypeOpen() {
    return doctype != null && !doctypeClosed;
  }

  public void endEntity(String name) throws SAXException {
    if (name.equals("[dtd]")) {
      inExternalSubset = false;
      return;
    }
    if (name.startsWith("%")) {
      return;
    }
    if (entityRefDepth > 0 && !entityRefs.isEmpty()
        && entityRefs.get(entityRefs.size() - 1).getNodeName().equals(name)) {
      EntityReferenceImpl ref = entityRefs.remove(entityRefs.size() - 1);
      entityRefDepth--;
      ref.setReadOnlyRecursive(true);
    }
  }

  private String literal(String expanded) {
    String lit = reader == null ? null : reader.literalSystemId();
    return lit != null ? lit : expanded;
  }

  private static boolean isPredefined(String name) {
    return name.equals("lt") || name.equals("gt") || name.equals("amp") || name.equals("quot") || name.equals("apos");
  }

  public void elementDecl(String name, String model) throws SAXException {
    if (!inExternalSubset) {
      subset.append("<!ELEMENT ").append(name).append(' ').append(model).append(">\n");
    }
  }

  public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException {
    if (inExternalSubset) {
      return;
    }
    subset.append("<!ATTLIST ").append(eName).append(' ').append(aName).append(' ');
    if (type.startsWith("NOTATION")) {
      subset.append("NOTATION");
    } else {
      subset.append(type);
    }
    if (mode != null) {
      subset.append(' ').append(mode);
    }
    if (value != null) {
      subset.append(' ').append(quote(value, '\'', "'"));
    }
    subset.append(">\n");
  }

  private static String quote(String value, char preferred, String unused) {
    if (value.indexOf('\'') >= 0) {
      return "\"" + value + "\"";
    }
    return "'" + value + "'";
  }

  public void internalEntityDecl(String name, String value) throws SAXException {
    if (!inExternalSubset) {
      subset.append("<!ENTITY ").append(name.startsWith("%") ? "% " + name.substring(1) : name).append(' ')
          .append(quote(value, '\'', "'")).append(">\n");
    }
    if (name.startsWith("%")) {
      return;
    }
    EntityImpl entity = new EntityImpl(doc, name);
    entity.readOnly = true;
    addEntity(entity);
  }

  public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
    if (!inExternalSubset) {
      subset.append("<!ENTITY ").append(name.startsWith("%") ? "% " + name.substring(1) : name);
      if (publicId != null) {
        subset.append(" PUBLIC \"").append(publicId).append("\" \"").append(literal(systemId)).append('"');
      } else {
        subset.append(" SYSTEM \"").append(literal(systemId)).append('"');
      }
      subset.append(">\n");
    }
    if (name.startsWith("%")) {
      return;
    }
    EntityImpl entity = new EntityImpl(doc, name);
    entity.publicId = publicId;
    entity.systemId = literal(systemId);
    entity.readOnly = true;
    addEntity(entity);
  }

  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
      throws SAXException {
    if (!inExternalSubset) {
      subset.append("<!ENTITY ").append(name);
      if (publicId != null) {
        subset.append(" PUBLIC \"").append(publicId).append("\" \"").append(literal(systemId)).append('"');
      } else {
        subset.append(" SYSTEM \"").append(literal(systemId)).append('"');
      }
      subset.append(" NDATA ").append(notationName).append(">\n");
    }
    EntityImpl entity = new EntityImpl(doc, name);
    entity.publicId = publicId;
    entity.systemId = literal(systemId);
    entity.notationName = notationName;
    entity.readOnly = true;
    addEntity(entity);
  }

  private void addEntity(EntityImpl entity) {
    int pos = doctype.entities.findName(entity.getNodeName());
    if (pos < 0) {
      doctype.entities.nodes.add(-(pos + 1), entity);
    }
  }

  public void notationDecl(String name, String publicId, String systemId) throws SAXException {
    if (!inExternalSubset) {
      subset.append("<!NOTATION ").append(name);
      if (publicId != null) {
        subset.append(" PUBLIC \"").append(publicId).append('"');
        if (systemId != null) {
          subset.append(" \"").append(literal(systemId)).append('"');
        }
      } else {
        subset.append(" SYSTEM \"").append(literal(systemId)).append('"');
      }
      subset.append(">\n");
    }
    NotationImpl notation = new NotationImpl(doc, name);
    notation.publicId = publicId;
    notation.systemId = literal(systemId);
    notation.readOnly = true;
    int pos = doctype.notations.findName(name);
    if (pos < 0) {
      doctype.notations.nodes.add(-(pos + 1), notation);
    }
  }
}
