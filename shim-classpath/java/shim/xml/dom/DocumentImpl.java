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
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

public class DocumentImpl extends ParentNode implements Document {
  private static final long serialVersionUID = 5898329356594610927L;

  boolean errorChecking = true;
  int modCount;
  boolean replacedNode;
  String encoding;
  String actualEncoding;
  String version = "1.0";
  boolean standalone;
  String documentUri;
  DomConfiguration config;

  public DocumentImpl() {
    super(null);
    ownerDocument = this;
  }

  public short getNodeType() {
    return Node.DOCUMENT_NODE;
  }

  public String getNodeName() {
    return "#document";
  }

  public Document getOwnerDocument() {
    return null;
  }

  public String getNodeValue() {
    return null;
  }

  public void setNodeValue(String value) {
  }

  public String getTextContent() {
    return null;
  }

  public void setTextContent(String textContent) {
  }

  void changed() {
    modCount++;
  }

  void insertedNode(NodeImpl node) {
  }

  void removedNode(NodeImpl node, NodeImpl from) {
  }

  void callUserDataHandlers(Node source, Node dest, short operation) {
    NodeImpl s = (NodeImpl) source;
    if (s.userData == null) {
      return;
    }
    for (java.util.Map.Entry<String, Object[]> e : new java.util.ArrayList<java.util.Map.Entry<String, Object[]>>(
        s.userData.entrySet())) {
      Object[] entry = e.getValue();
      if (operation == UserDataHandler.NODE_CLONED || operation == UserDataHandler.NODE_IMPORTED) {
        if (dest != null) {
          ((NodeImpl) dest).setUserDataNoHandler(e.getKey(), entry[0], (UserDataHandler) entry[1]);
        }
      }
      if (entry[1] != null) {
        ((UserDataHandler) entry[1]).handle(operation, e.getKey(), entry[0], source, dest);
      }
    }
  }

  private boolean cloningDoc;

  public Node cloneNode(boolean deep) {
    DocumentImpl newdoc = new DocumentImpl();
    newdoc.cloningDoc = true;
    newdoc.errorChecking = errorChecking;
    newdoc.version = version;
    newdoc.standalone = standalone;
    newdoc.encoding = encoding;
    newdoc.actualEncoding = actualEncoding;
    newdoc.documentUri = documentUri;
    if (deep) {
      for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
        NodeImpl c;
        if (n.getNodeType() == DOCUMENT_TYPE_NODE) {
          c = (NodeImpl) n.cloneNode(true);
          ((DocumentTypeImpl) c).setOwnerDocument(newdoc);
        } else {
          c = (NodeImpl) newdoc.importNode(n, true);
        }
        newdoc.appendCloneChild(c);
        c.ownerDocument = newdoc;
      }
    }
    newdoc.cloningDoc = false;
    callUserDataHandlers(this, newdoc, UserDataHandler.NODE_CLONED);
    return newdoc;
  }

  private void checkStructure(Node newChild, Node oldChild) {
    if (!errorChecking) {
      return;
    }
    if (newChild.getNodeType() == ELEMENT_NODE) {
      for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
        if (n.getNodeType() == ELEMENT_NODE && n != oldChild) {
          throw DomMessages.error(DOMException.HIERARCHY_REQUEST_ERR);
        }
      }
    } else if (newChild.getNodeType() == DOCUMENT_TYPE_NODE) {
      for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
        if (n.getNodeType() == DOCUMENT_TYPE_NODE && n != oldChild) {
          throw DomMessages.error(DOMException.HIERARCHY_REQUEST_ERR);
        }
      }
    }
  }

  public Node insertBefore(Node newChild, Node refChild) throws DOMException {
    if (newChild.getNodeType() == DOCUMENT_FRAGMENT_NODE) {
      int elements = 0;
      for (Node n = newChild.getFirstChild(); n != null; n = n.getNextSibling()) {
        if (n.getNodeType() == ELEMENT_NODE) {
          elements++;
        }
      }
      if (errorChecking && elements > 0) {
        checkStructure(ELEMENT_PROBE, null);
        if (elements > 1) {
          throw DomMessages.error(DOMException.HIERARCHY_REQUEST_ERR);
        }
      }
      return super.insertBefore(newChild, refChild);
    }
    if (newChild != refChild) {
      checkStructure(newChild, null);
    }
    if (newChild.getNodeType() == DOCUMENT_TYPE_NODE && ((NodeImpl) newChild).ownerDocument == null) {
      ((DocumentTypeImpl) newChild).setOwnerDocument(this);
    }
    return super.insertBefore(newChild, refChild);
  }

  private static final Node ELEMENT_PROBE = new ElementProbe();

  private static final class ElementProbe extends NodeImpl {
    ElementProbe() {
      super(null);
    }

    public short getNodeType() {
      return ELEMENT_NODE;
    }

    public String getNodeName() {
      return "probe";
    }
  }

  public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
    if (newChild.getOwnerDocument() == null && newChild instanceof DocumentTypeImpl) {
      ((DocumentTypeImpl) newChild).setOwnerDocument(this);
    }
    checkStructure(newChild, oldChild);
    return super.replaceChild(newChild, oldChild);
  }

  public Node removeChild(Node oldChild) throws DOMException {
    return super.removeChild(oldChild);
  }

  public DocumentType getDoctype() {
    for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
      if (n.getNodeType() == DOCUMENT_TYPE_NODE) {
        return (DocumentType) n;
      }
    }
    return null;
  }

  public Element getDocumentElement() {
    for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
      if (n.getNodeType() == ELEMENT_NODE) {
        return (Element) n;
      }
    }
    return null;
  }

  public DOMImplementation getImplementation() {
    return DomImplementation.getInstance();
  }

  public Element createElement(String tagName) throws DOMException {
    if (errorChecking && !DomNames.isName(tagName)) {
      throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
    }
    return new ElementImpl(this, tagName);
  }

  public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
    String ns = DomNames.normalizeNamespace(namespaceURI);
    String[] parts = errorChecking ? DomNames.checkElement(namespaceURI, qualifiedName) : DomNames.split(qualifiedName);
    return new ElementImpl(this, ns, qualifiedName, parts[1]);
  }

  public Attr createAttribute(String name) throws DOMException {
    if (errorChecking && !DomNames.isName(name)) {
      throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
    }
    return new AttrImpl(this, name);
  }

  public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
    String ns = DomNames.normalizeNamespace(namespaceURI);
    String[] parts = errorChecking ? DomNames.checkAttribute(namespaceURI, qualifiedName)
        : DomNames.split(qualifiedName);
    return new AttrImpl(this, ns, qualifiedName, parts[1]);
  }

  public Text createTextNode(String data) {
    return new TextImpl(this, data);
  }

  public Comment createComment(String data) {
    return new CommentImpl(this, data);
  }

  public CDATASection createCDATASection(String data) throws DOMException {
    return new CDATASectionImpl(this, data);
  }

  public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
    if (errorChecking && !DomNames.isName(target)) {
      throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
    }
    return new ProcessingInstructionImpl(this, target, data);
  }

  public DocumentFragment createDocumentFragment() {
    return new DocumentFragmentImpl(this);
  }

  public EntityReference createEntityReference(String name) throws DOMException {
    if (errorChecking && !DomNames.isName(name)) {
      throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
    }
    EntityReferenceImpl ref = new EntityReferenceImpl(this, name);
    DocumentType doctype = getDoctype();
    if (doctype != null) {
      NamedNodeMap entities = doctype.getEntities();
      EntityImpl entity = entities == null ? null : (EntityImpl) entities.getNamedItem(name);
      if (entity != null) {
        for (NodeImpl c = entity.firstChildNode; c != null; c = c.nextSibling) {
          ref.appendCloneChild((NodeImpl) c.cloneNode(true));
        }
        ref.setReadOnlyRecursive(true);
      }
    }
    return ref;
  }

  public NodeList getElementsByTagName(String tagname) {
    return new DeepNodeList(this, tagname);
  }

  public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
    return new DeepNodeList(this, namespaceURI, localName);
  }

  public Element getElementById(String elementId) {
    return findById(this, elementId);
  }

  private Element findById(NodeImpl parent, String id) {
    for (NodeImpl n = parent.firstChildNode_(); n != null; n = n.nextSibling) {
      if (n.getNodeType() == ELEMENT_NODE) {
        ElementImpl el = (ElementImpl) n;
        if (el.attributes != null) {
          for (NodeImpl a : el.attributes.nodes) {
            if (((AttrImpl) a).idAttribute && id.equals(((AttrImpl) a).getValue())) {
              return el;
            }
          }
        }
        Element found = findById(el, id);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  public Node importNode(Node source, boolean deep) throws DOMException {
    Node result = importNode(source, deep, null);
    return result;
  }

  private Node importNode(Node source, boolean deep, java.util.Map<Node, Node> reversed) throws DOMException {
    Node newnode = null;
    short type = source.getNodeType();
    switch (type) {
    case ELEMENT_NODE: {
      Element newElement;
      boolean domLevel20 = source.getOwnerDocument().getImplementation().hasFeature("XML", "2.0");
      if (!domLevel20 || source.getLocalName() == null) {
        newElement = createElement(source.getNodeName());
      } else {
        newElement = createElementNS(source.getNamespaceURI(), source.getNodeName());
      }
      NamedNodeMap sourceAttrs = source.getAttributes();
      if (sourceAttrs != null) {
        int length = sourceAttrs.getLength();
        for (int index = 0; index < length; index++) {
          Attr attr = (Attr) sourceAttrs.item(index);
          if (attr.getSpecified() || cloningDoc) {
            Attr newAttr = (Attr) importNode(attr, true, reversed);
            if (domLevel20 && attr.getLocalName() != null) {
              newElement.setAttributeNodeNS(newAttr);
            } else {
              newElement.setAttributeNode(newAttr);
            }
          }
        }
      }
      newnode = newElement;
      break;
    }
    case ATTRIBUTE_NODE: {
      if (source.getOwnerDocument().getImplementation().hasFeature("XML", "2.0")) {
        if (source.getLocalName() == null) {
          newnode = createAttribute(source.getNodeName());
        } else {
          newnode = createAttributeNS(source.getNamespaceURI(), source.getNodeName());
        }
      } else {
        newnode = createAttribute(source.getNodeName());
      }
      if (source instanceof AttrImpl) {
        AttrImpl attr = (AttrImpl) source;
        if (attr.idAttribute) {
          ((AttrImpl) newnode).idAttribute = true;
        }
      }
      deep = true;
      break;
    }
    case TEXT_NODE:
      newnode = createTextNode(source.getNodeValue());
      break;
    case CDATA_SECTION_NODE:
      newnode = createCDATASection(source.getNodeValue());
      break;
    case ENTITY_REFERENCE_NODE:
      newnode = createEntityReference(source.getNodeName());
      deep = false;
      break;
    case ENTITY_NODE: {
      org.w3c.dom.Entity srcentity = (org.w3c.dom.Entity) source;
      EntityImpl newentity = new EntityImpl(this, source.getNodeName());
      newentity.publicId = srcentity.getPublicId();
      newentity.systemId = srcentity.getSystemId();
      newentity.notationName = srcentity.getNotationName();
      newentity.encoding = srcentity.getXmlEncoding();
      newentity.version = srcentity.getXmlVersion();
      newentity.inputEncoding = srcentity.getInputEncoding();
      newnode = newentity;
      break;
    }
    case PROCESSING_INSTRUCTION_NODE:
      newnode = createProcessingInstruction(source.getNodeName(), source.getNodeValue());
      break;
    case COMMENT_NODE:
      newnode = createComment(source.getNodeValue());
      break;
    case DOCUMENT_TYPE_NODE:
      throw DomMessages.error(DOMException.NOT_SUPPORTED_ERR);
    case DOCUMENT_FRAGMENT_NODE:
      newnode = createDocumentFragment();
      break;
    case NOTATION_NODE: {
      org.w3c.dom.Notation srcnotation = (org.w3c.dom.Notation) source;
      NotationImpl newnotation = new NotationImpl(this, source.getNodeName());
      newnotation.publicId = srcnotation.getPublicId();
      newnotation.systemId = srcnotation.getSystemId();
      newnode = newnotation;
      break;
    }
    case DOCUMENT_NODE:
    default:
      throw DomMessages.error(DOMException.NOT_SUPPORTED_ERR);
    }
    if (deep) {
      for (Node srckid = source.getFirstChild(); srckid != null; srckid = srckid.getNextSibling()) {
        newnode.appendChild(importNode(srckid, true, reversed));
      }
    }
    if (newnode.getNodeType() == ENTITY_NODE) {
      ((NodeImpl) newnode).setReadOnlyRecursive(true);
    }
    callUserDataHandlers(source, newnode, UserDataHandler.NODE_IMPORTED);
    return newnode;
  }

  public Node adoptNode(Node source) {
    if (!(source instanceof NodeImpl)) {
      return null;
    }
    NodeImpl node = (NodeImpl) source;
    switch (node.getNodeType()) {
    case ATTRIBUTE_NODE: {
      AttrImpl attr = (AttrImpl) node;
      if (attr.ownerElement != null) {
        attr.ownerElement.removeAttributeNode(attr);
      }
      attr.specified = true;
      break;
    }
    case DOCUMENT_NODE:
    case DOCUMENT_TYPE_NODE:
      throw DomMessages.error(DOMException.NOT_SUPPORTED_ERR);
    case ENTITY_NODE:
    case NOTATION_NODE:
      return null;
    default: {
      if (node.getNodeType() == ENTITY_REFERENCE_NODE) {
        // entity references keep their children only when the new document declares the entity
      }
      NodeImpl parent = node.parentNode;
      if (parent != null) {
        parent.removeChild(source);
      }
    }
    }
    if (node.readOnly && errorChecking) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    DocumentImpl old = node.ownerDocument;
    setOwner(node, this);
    callUserDataHandlers(source, null, UserDataHandler.NODE_ADOPTED);
    return node;
  }

  private static void setOwner(NodeImpl node, DocumentImpl doc) {
    node.ownerDocument = doc;
    if (node instanceof ElementImpl && ((ElementImpl) node).attributes != null) {
      for (NodeImpl a : ((ElementImpl) node).attributes.nodes) {
        setOwner(a, doc);
      }
    }
    for (NodeImpl c = node.firstChildNode_(); c != null; c = c.nextSibling) {
      setOwner(c, doc);
    }
  }

  public Node renameNode(Node n, String namespaceURI, String name) throws DOMException {
    if (errorChecking && n.getOwnerDocument() != this && n != this) {
      throw DomMessages.error(DOMException.WRONG_DOCUMENT_ERR);
    }
    String ns = DomNames.normalizeNamespace(namespaceURI);
    switch (n.getNodeType()) {
    case ELEMENT_NODE: {
      ElementImpl el = (ElementImpl) n;
      if (el.localName != null) {
        String[] parts = errorChecking ? DomNames.checkElement(ns, name) : DomNames.split(name);
        el.name = name;
        el.namespaceUri = ns;
        el.localName = parts[1];
        callUserDataHandlers(n, null, UserDataHandler.NODE_RENAMED);
        changed();
        return el;
      }
      if (ns == null) {
        if (errorChecking && !DomNames.isName(name)) {
          throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
        }
        if (errorChecking && name.indexOf(':') >= 0) {
          throw DomMessages.error(DOMException.NAMESPACE_ERR);
        }
        el.name = name;
        callUserDataHandlers(n, null, UserDataHandler.NODE_RENAMED);
        changed();
        return el;
      }
      ElementImpl repl = (ElementImpl) createElementNS(ns, name);
      if (el.attributes != null) {
        repl.attributes = new AttributeMap(repl);
        for (NodeImpl a : el.attributes.nodes) {
          repl.attributes.nodes.add(a);
          ((AttrImpl) a).ownerElement = repl;
        }
        el.attributes = null;
      }
      while (el.firstChildNode != null) {
        Node child = el.firstChildNode;
        el.removeChild(child);
        repl.appendChild(child);
      }
      if (el.parentNode != null) {
        el.parentNode.replaceChild(repl, el);
      }
      callUserDataHandlers(n, repl, UserDataHandler.NODE_RENAMED);
      return repl;
    }
    case ATTRIBUTE_NODE: {
      AttrImpl at = (AttrImpl) n;
      ElementImpl owner = at.ownerElement;
      if (owner != null) {
        owner.removeAttributeNode(at);
      }
      if (at.localName != null) {
        String[] parts = errorChecking ? DomNames.checkAttribute(ns, name) : DomNames.split(name);
        at.name = name;
        at.namespaceUri = ns;
        at.localName = parts[1];
      } else if (ns != null) {
        AttrImpl repl = (AttrImpl) createAttributeNS(ns, name);
        for (NodeImpl c = at.firstChildNode; c != null; c = c.nextSibling) {
          repl.appendCloneChild((NodeImpl) c.cloneNode(true));
        }
        repl.specified = at.specified;
        if (owner != null) {
          owner.setAttributeNodeNS(repl);
        }
        callUserDataHandlers(n, repl, UserDataHandler.NODE_RENAMED);
        return repl;
      } else {
        if (errorChecking && !DomNames.isName(name)) {
          throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
        }
        if (errorChecking && name.indexOf(':') >= 0) {
          throw DomMessages.error(DOMException.NAMESPACE_ERR);
        }
        at.name = name;
      }
      if (owner != null) {
        owner.setAttributeNodeNS(at);
      }
      callUserDataHandlers(n, null, UserDataHandler.NODE_RENAMED);
      return at;
    }
    default:
      throw DomMessages.error(DOMException.NOT_SUPPORTED_ERR);
    }
  }

  public String getInputEncoding() {
    return actualEncoding;
  }

  public void setInputEncoding(String value) {
    actualEncoding = value;
  }

  public String getXmlEncoding() {
    return encoding;
  }

  public void setXmlEncoding(String value) {
    encoding = value;
  }

  public boolean getXmlStandalone() {
    return standalone;
  }

  public void setXmlStandalone(boolean value) throws DOMException {
    standalone = value;
  }

  public String getXmlVersion() {
    return version;
  }

  public void setXmlVersion(String value) throws DOMException {
    if (value.equals("1.0") || value.equals("1.1")) {
      version = value;
    } else {
      throw DomMessages.error(DOMException.NOT_SUPPORTED_ERR);
    }
  }

  public boolean getStrictErrorChecking() {
    return errorChecking;
  }

  public void setStrictErrorChecking(boolean value) {
    errorChecking = value;
  }

  public String getDocumentURI() {
    return documentUri;
  }

  public void setDocumentURI(String uri) {
    documentUri = uri;
  }

  public String getBaseURI() {
    return documentUri;
  }

  public DOMConfiguration getDomConfig() {
    if (config == null) {
      config = new DomConfiguration(this);
    }
    return config;
  }

  public void normalizeDocument() {
    getDomConfig();
    DomNormalizer.normalize(this, config);
  }

  public boolean isEqualNode(Node arg) {
    return super.isEqualNode(arg);
  }
}
