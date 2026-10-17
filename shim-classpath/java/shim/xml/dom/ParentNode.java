/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ParentNode extends NodeImpl {
  private static final long serialVersionUID = 2815829867152120872L;

  NodeImpl firstChildNode;
  NodeImpl lastChildNode;
  private int cachedLength = -1;
  private NodeImpl cachedNode;
  private int cachedIndex = -1;
  private int cacheStamp = -1;

  ParentNode(DocumentImpl ownerDocument) {
    super(ownerDocument);
  }

  public Node cloneNode(boolean deep) {
    ParentNode copy = (ParentNode) super.cloneNode(deep);
    copy.firstChildNode = null;
    copy.lastChildNode = null;
    copy.cachedLength = -1;
    copy.cachedNode = null;
    copy.cachedIndex = -1;
    copy.cacheStamp = -1;
    if (deep) {
      for (NodeImpl child = firstChildNode; child != null; child = child.nextSibling) {
        copy.appendCloneChild((NodeImpl) child.cloneNode(true));
      }
    }
    return copy;
  }

  void appendCloneChild(NodeImpl child) {
    child.parentNode = copyParent();
    child.ownerDocument = ownerDocument;
    child.previousSibling = lastChildNode;
    child.nextSibling = null;
    if (lastChildNode == null) {
      firstChildNode = child;
    } else {
      lastChildNode.nextSibling = child;
    }
    lastChildNode = child;
  }

  private ParentNode copyParent() {
    return this;
  }

  NodeImpl lastChildNodeOrNull() {
    return lastChildNode;
  }

  public Node getFirstChild() {
    return firstChildNode;
  }

  public Node getLastChild() {
    return lastChildNode;
  }

  public boolean hasChildNodes() {
    return firstChildNode != null;
  }

  public NodeList getChildNodes() {
    return this;
  }

  public int getLength() {
    if (ownerDocument != null && cacheStamp != ownerDocument.modCount) {
      cachedLength = -1;
      cachedNode = null;
      cachedIndex = -1;
    }
    if (cachedLength < 0) {
      int count = 0;
      for (NodeImpl n = firstChildNode; n != null; n = n.nextSibling) {
        count++;
      }
      cachedLength = count;
      cacheStamp = ownerDocument == null ? 0 : ownerDocument.modCount;
    }
    return cachedLength;
  }

  public Node item(int index) {
    if (index < 0) {
      return null;
    }
    if (ownerDocument != null && cacheStamp != ownerDocument.modCount) {
      cachedLength = -1;
      cachedNode = null;
      cachedIndex = -1;
      cacheStamp = ownerDocument.modCount;
    }
    NodeImpl n;
    int i;
    if (cachedNode != null && cachedIndex >= 0 && index >= cachedIndex) {
      n = cachedNode;
      i = cachedIndex;
    } else {
      n = firstChildNode;
      i = 0;
    }
    while (n != null && i < index) {
      n = n.nextSibling;
      i++;
    }
    if (n != null) {
      cachedNode = n;
      cachedIndex = index;
    }
    return n;
  }

  private static boolean kidOk(NodeImpl parent, NodeImpl child) {
    short p = parent.getNodeType();
    short c = child.getNodeType();
    switch (p) {
    case DOCUMENT_NODE:
      return c == ELEMENT_NODE || c == PROCESSING_INSTRUCTION_NODE || c == COMMENT_NODE || c == DOCUMENT_TYPE_NODE;
    case DOCUMENT_FRAGMENT_NODE:
    case ENTITY_NODE:
    case ENTITY_REFERENCE_NODE:
    case ELEMENT_NODE:
      return c == ELEMENT_NODE || c == PROCESSING_INSTRUCTION_NODE || c == COMMENT_NODE || c == TEXT_NODE
          || c == CDATA_SECTION_NODE || c == ENTITY_REFERENCE_NODE;
    case ATTRIBUTE_NODE:
      return c == TEXT_NODE || c == ENTITY_REFERENCE_NODE;
    default:
      return false;
    }
  }

  public Node insertBefore(Node newChild, Node refChild) throws DOMException {
    return internalInsertBefore(newChild, refChild, false);
  }

  Node internalInsertBefore(Node newChild, Node refChild, boolean replace) throws DOMException {
    boolean errorChecking = ownerDocument.errorChecking;
    if (newChild.getNodeType() == DOCUMENT_FRAGMENT_NODE) {
      if (errorChecking) {
        for (Node kid = newChild.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
          if (!kidOk(this, (NodeImpl) kid)) {
            throw DomMessages.error(DOMException.HIERARCHY_REQUEST_ERR);
          }
        }
      }
      while (newChild.hasChildNodes()) {
        insertBefore(newChild.getFirstChild(), refChild);
      }
      return newChild;
    }
    if (newChild == refChild) {
      refChild = refChild.getNextSibling();
      removeChild(newChild);
      insertBefore(newChild, refChild);
      return newChild;
    }
    if (errorChecking) {
      if (readOnly) {
        throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
      }
      if (newChild.getOwnerDocument() != ownerDocument && newChild != ownerDocument) {
        if (!(getNodeType() == DOCUMENT_NODE && newChild.getNodeType() == DOCUMENT_TYPE_NODE
            && newChild.getOwnerDocument() == null)) {
          throw DomMessages.error(DOMException.WRONG_DOCUMENT_ERR);
        }
      }
      if (!(newChild instanceof NodeImpl) || !kidOk(this, (NodeImpl) newChild)) {
        throw DomMessages.error(DOMException.HIERARCHY_REQUEST_ERR);
      }
      if (refChild != null && refChild.getParentNode() != this) {
        throw DomMessages.error(DOMException.NOT_FOUND_ERR);
      }
      boolean treeSafe = true;
      for (NodeImpl a = this; treeSafe && a != null; a = a.parentNode) {
        treeSafe = newChild != a;
      }
      if (!treeSafe) {
        throw DomMessages.error(DOMException.HIERARCHY_REQUEST_ERR);
      }
    }
    NodeImpl newInternal = (NodeImpl) newChild;
    NodeImpl oldparent = newInternal.parentNode;
    if (oldparent != null) {
      oldparent.removeChild(newInternal);
    }
    NodeImpl refInternal = (NodeImpl) refChild;
    newInternal.ownerDocument = ownerDocument;
    newInternal.parentNode = this;
    if (firstChildNode == null) {
      firstChildNode = newInternal;
      lastChildNode = newInternal;
      newInternal.previousSibling = null;
      newInternal.nextSibling = null;
    } else if (refInternal == null) {
      newInternal.previousSibling = lastChildNode;
      newInternal.nextSibling = null;
      lastChildNode.nextSibling = newInternal;
      lastChildNode = newInternal;
    } else {
      newInternal.nextSibling = refInternal;
      newInternal.previousSibling = refInternal.previousSibling;
      if (refInternal.previousSibling == null) {
        firstChildNode = newInternal;
      } else {
        refInternal.previousSibling.nextSibling = newInternal;
      }
      refInternal.previousSibling = newInternal;
    }
    changed();
    ownerDocument.insertedNode(newInternal);
    return newChild;
  }

  public Node removeChild(Node oldChild) throws DOMException {
    return internalRemoveChild(oldChild, false);
  }

  Node internalRemoveChild(Node oldChild, boolean replace) throws DOMException {
    if (oldChild == null) {
      throw new NullPointerException();
    }
    if (ownerDocument.errorChecking) {
      if (readOnly) {
        throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
      }
      if (oldChild != null && oldChild.getParentNode() != this) {
        throw DomMessages.error(DOMException.NOT_FOUND_ERR);
      }
    }
    NodeImpl oldInternal = (NodeImpl) oldChild;
    if (oldInternal == null) {
      throw DomMessages.error(DOMException.NOT_FOUND_ERR);
    }
    if (oldInternal == firstChildNode) {
      firstChildNode = oldInternal.nextSibling;
      if (firstChildNode != null) {
        firstChildNode.previousSibling = null;
      } else {
        lastChildNode = null;
      }
    } else {
      NodeImpl prev = oldInternal.previousSibling;
      NodeImpl next = oldInternal.nextSibling;
      prev.nextSibling = next;
      if (next == null) {
        lastChildNode = prev;
      } else {
        next.previousSibling = prev;
      }
    }
    oldInternal.parentNode = null;
    oldInternal.previousSibling = null;
    oldInternal.nextSibling = null;
    changed();
    ownerDocument.removedNode(oldInternal, this);
    return oldInternal;
  }

  public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
    ownerDocument.replacedNode = true;
    try {
      if (newChild != oldChild) {
        internalInsertBefore(newChild, oldChild, true);
        if (newChild != oldChild) {
          internalRemoveChild(oldChild, true);
        }
      }
    } finally {
      ownerDocument.replacedNode = false;
    }
    return oldChild;
  }

  public String getTextContent() throws DOMException {
    Node child = getFirstChild();
    if (child != null) {
      Node next = child.getNextSibling();
      if (next == null) {
        return hasTextContent(child) ? ((NodeImpl) child).getTextContent() : "";
      }
      StringBuilder buf = new StringBuilder();
      getTextContent(buf);
      return buf.toString();
    }
    return "";
  }

  void getTextContent(StringBuilder buf) throws DOMException {
    Node child = getFirstChild();
    while (child != null) {
      if (hasTextContent(child)) {
        ((NodeImpl) child).appendTextContent(buf);
      }
      child = child.getNextSibling();
    }
  }

  void appendTextContent(StringBuilder buf) {
    buf.append(getTextContent());
  }

  final boolean hasTextContent(Node child) {
    return child.getNodeType() != COMMENT_NODE && child.getNodeType() != PROCESSING_INSTRUCTION_NODE
        && (child.getNodeType() != TEXT_NODE || !((org.w3c.dom.Text) child).isElementContentWhitespace());
  }

  public void setTextContent(String textContent) throws DOMException {
    Node child;
    while ((child = getFirstChild()) != null) {
      removeChild(child);
    }
    if (textContent != null && textContent.length() != 0) {
      appendChild(ownerDocument.createTextNode(textContent));
    }
  }

  public void normalize() {
    if (readOnly) {
      return;
    }
    NodeImpl kid = firstChildNode;
    while (kid != null) {
      NodeImpl next = kid.nextSibling;
      if (kid.getNodeType() == TEXT_NODE) {
        while (next != null && next.getNodeType() == TEXT_NODE) {
          ((TextImpl) kid).appendData(next.getNodeValue());
          removeChild(next);
          next = kid.nextSibling;
        }
        if (kid.getNodeValue() == null || kid.getNodeValue().length() == 0) {
          removeChild(kid);
        }
      } else if (kid.getNodeType() == ELEMENT_NODE || kid.getNodeType() == ENTITY_REFERENCE_NODE) {
        kid.normalize();
      }
      kid = next;
    }
    if (getNodeType() == ELEMENT_NODE) {
      org.w3c.dom.NamedNodeMap attrs = getAttributes();
      if (attrs != null) {
        for (int i = 0; i < attrs.getLength(); ++i) {
          attrs.item(i).normalize();
        }
      }
    }
  }
}
