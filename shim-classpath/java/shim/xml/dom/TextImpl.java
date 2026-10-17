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
import org.w3c.dom.Text;

public class TextImpl extends CharacterDataImpl implements Text {
  private static final long serialVersionUID = 4300453429790428419L;

  boolean ignorableWhitespace;

  TextImpl(DocumentImpl ownerDocument, String data) {
    super(ownerDocument, data);
  }

  public short getNodeType() {
    return Node.TEXT_NODE;
  }

  public String getNodeName() {
    return "#text";
  }

  public boolean isElementContentWhitespace() {
    return ignorableWhitespace;
  }

  public void setIgnorableWhitespace(boolean ignorable) {
    ignorableWhitespace = ignorable;
  }

  public String getWholeText() {
    StringBuilder buffer = new StringBuilder();
    if (data != null && data.length() != 0) {
      buffer.append(data);
    }
    getWholeTextBackward(previousSibling, buffer, parentNode);
    String temp = buffer.toString();
    buffer.setLength(0);
    getWholeTextForward(nextSibling, buffer, parentNode);
    return temp + buffer;
  }

  private boolean getWholeTextForward(NodeImpl node, StringBuilder buffer, Node parent) {
    boolean inEntRef = false;
    if (parent != null && parent.getNodeType() == ENTITY_REFERENCE_NODE) {
      inEntRef = true;
    }
    while (node != null) {
      short type = node.getNodeType();
      if (type == ENTITY_REFERENCE_NODE) {
        if (getWholeTextForward((NodeImpl) node.getFirstChild(), buffer, node)) {
          return true;
        }
      } else if (type == TEXT_NODE || type == CDATA_SECTION_NODE) {
        buffer.append(node.getNodeValue());
      } else {
        return true;
      }
      node = node.nextSibling;
    }
    if (inEntRef) {
      getWholeTextForward(((NodeImpl) parent).nextSibling, buffer, parent.getParentNode());
      return true;
    }
    return false;
  }

  private boolean getWholeTextBackward(NodeImpl node, StringBuilder buffer, Node parent) {
    boolean inEntRef = false;
    if (parent != null && parent.getNodeType() == ENTITY_REFERENCE_NODE) {
      inEntRef = true;
    }
    while (node != null) {
      short type = node.getNodeType();
      if (type == ENTITY_REFERENCE_NODE) {
        if (getWholeTextBackward((NodeImpl) node.getLastChild(), buffer, node)) {
          return true;
        }
      } else if (type == TEXT_NODE || type == CDATA_SECTION_NODE) {
        buffer.insert(0, node.getNodeValue());
      } else {
        return true;
      }
      node = node.previousSibling;
    }
    if (inEntRef) {
      getWholeTextBackward(((NodeImpl) parent).previousSibling, buffer, parent.getParentNode());
      return true;
    }
    return false;
  }

  public Text replaceWholeText(String content) throws DOMException {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    Node parent = this.getParentNode();
    if (content == null || content.length() == 0) {
      if (parent != null) {
        parent.removeChild(this);
      }
      return null;
    }
    if (ownerDocument.errorChecking) {
      if (!canModifyPrev(this) || !canModifyNext(this)) {
        throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
      }
    }
    Text currentNode;
    if (isReadOnly()) {
      Text newNode = ownerDocument.createTextNode(content);
      if (parent != null) {
        parent.insertBefore(newNode, this);
        parent.removeChild(this);
      }
      currentNode = newNode;
    } else {
      setData(content);
      currentNode = this;
    }
    Node prev = currentNode.getPreviousSibling();
    while (prev != null && (prev.getNodeType() == TEXT_NODE || prev.getNodeType() == CDATA_SECTION_NODE
        || (prev.getNodeType() == ENTITY_REFERENCE_NODE && hasTextOnlyChildren(prev)))) {
      Node toRemove = prev;
      prev = prev.getPreviousSibling();
      toRemove.getParentNode().removeChild(toRemove);
    }
    Node next = currentNode.getNextSibling();
    while (next != null && (next.getNodeType() == TEXT_NODE || next.getNodeType() == CDATA_SECTION_NODE
        || (next.getNodeType() == ENTITY_REFERENCE_NODE && hasTextOnlyChildren(next)))) {
      Node toRemove = next;
      next = next.getNextSibling();
      toRemove.getParentNode().removeChild(toRemove);
    }
    return currentNode;
  }

  private boolean canModifyPrev(Node node) {
    boolean textLastChild = false;
    Node prev = node.getPreviousSibling();
    while (prev != null) {
      short type = prev.getNodeType();
      if (type == ENTITY_REFERENCE_NODE) {
        Node lastChild = prev.getLastChild();
        if (lastChild == null) {
          return false;
        }
        while (lastChild != null) {
          short lType = lastChild.getNodeType();
          if (lType == TEXT_NODE || lType == CDATA_SECTION_NODE) {
            textLastChild = true;
          } else if (lType == ENTITY_REFERENCE_NODE) {
            if (!canModifyPrev(lastChild)) {
              return false;
            }
            textLastChild = true;
          } else {
            return !textLastChild;
          }
          lastChild = lastChild.getPreviousSibling();
        }
      } else if (type == TEXT_NODE || type == CDATA_SECTION_NODE) {
        textLastChild = true;
      } else {
        return true;
      }
      prev = prev.getPreviousSibling();
    }
    return true;
  }

  private boolean canModifyNext(Node node) {
    boolean textFirstChild = false;
    Node next = node.getNextSibling();
    while (next != null) {
      short type = next.getNodeType();
      if (type == ENTITY_REFERENCE_NODE) {
        Node firstChild = next.getFirstChild();
        if (firstChild == null) {
          return false;
        }
        while (firstChild != null) {
          short lType = firstChild.getNodeType();
          if (lType == TEXT_NODE || lType == CDATA_SECTION_NODE) {
            textFirstChild = true;
          } else if (lType == ENTITY_REFERENCE_NODE) {
            if (!canModifyNext(firstChild)) {
              return false;
            }
            textFirstChild = true;
          } else {
            return !textFirstChild;
          }
          firstChild = firstChild.getNextSibling();
        }
      } else if (type == TEXT_NODE || type == CDATA_SECTION_NODE) {
        textFirstChild = true;
      } else {
        return true;
      }
      next = next.getNextSibling();
    }
    return true;
  }

  private boolean hasTextOnlyChildren(Node node) {
    Node child = node;
    if (child == null) {
      return false;
    }
    child = child.getFirstChild();
    while (child != null) {
      int type = child.getNodeType();
      if (type == ENTITY_REFERENCE_NODE) {
        return hasTextOnlyChildren(child);
      } else if (type != TEXT_NODE && type != CDATA_SECTION_NODE && type != ENTITY_REFERENCE_NODE) {
        return false;
      }
      child = child.getNextSibling();
    }
    return true;
  }

  public Text splitText(int offset) throws DOMException {
    if (ownerDocument.errorChecking) {
      if (readOnly) {
        throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
      }
      if (offset < 0 || offset > data.length()) {
        throw DomMessages.error(DOMException.INDEX_SIZE_ERR);
      }
    }
    TextImpl newText = (TextImpl) ownerDocument.createTextNode(data.substring(offset));
    setNodeValue(data.substring(0, offset));
    if (parentNode != null) {
      parentNode.insertBefore(newText, nextSibling);
    }
    return newText;
  }

  public void replaceData(int offset, int count, String arg) throws DOMException {
    super.replaceData(offset, count, arg);
  }
}
