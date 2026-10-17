/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public abstract class CharacterDataImpl extends NodeImpl implements CharacterData {
  private static final long serialVersionUID = 7931170150428474230L;

  String data;

  CharacterDataImpl(DocumentImpl ownerDocument, String data) {
    super(ownerDocument);
    this.data = data;
  }

  public String getNodeValue() {
    return data;
  }

  public void setNodeValue(String value) {
    setData(value);
  }

  public String getData() {
    return data;
  }

  public void setData(String value) throws DOMException {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    data = value;
    changed();
  }

  public int getLength() {
    return data.length();
  }

  public String substringData(int offset, int count) throws DOMException {
    if (count < 0 || offset < 0 || offset > data.length() - 1) {
      throw DomMessages.error(DOMException.INDEX_SIZE_ERR);
    }
    int end = Math.min(offset + count, data.length());
    return data.substring(offset, end);
  }

  public void appendData(String arg) {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    data = data + arg;
    changed();
  }

  public void insertData(int offset, String arg) throws DOMException {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    if (offset < 0 || offset > data.length()) {
      throw DomMessages.error(DOMException.INDEX_SIZE_ERR);
    }
    data = new StringBuilder(data).insert(offset, arg).toString();
    changed();
  }

  public void deleteData(int offset, int count) throws DOMException {
    if (ownerDocument.errorChecking) {
      if (readOnly) {
        throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
      }
      if (count < 0) {
        throw DomMessages.error(DOMException.INDEX_SIZE_ERR);
      }
    }
    if (offset < 0 || offset > data.length()) {
      throw DomMessages.error(DOMException.INDEX_SIZE_ERR);
    }
    int end = Math.min(offset + count, data.length());
    data = data.substring(0, offset) + data.substring(end);
    changed();
  }

  public void replaceData(int offset, int count, String arg) throws DOMException {
    if (ownerDocument.errorChecking && readOnly) {
      throw DomMessages.error(DOMException.NO_MODIFICATION_ALLOWED_ERR);
    }
    deleteData(offset, count);
    insertData(offset, arg);
  }

  public String getTextContent() {
    return getNodeValue();
  }

  public void setTextContent(String textContent) {
    setNodeValue(textContent);
  }

  void appendTextContent(StringBuilder buf) {
    buf.append(data);
  }
}
