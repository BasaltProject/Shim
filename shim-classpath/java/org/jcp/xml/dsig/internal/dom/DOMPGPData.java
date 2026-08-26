/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.keyinfo.PGPData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMPGPData implements PGPData {
  private final byte[] keyId;
  private final byte[] keyPacket;
  private final List<XMLStructure> externalElements;

  @SuppressWarnings("rawtypes")
  public DOMPGPData(byte[] keyPacket, List other) {
    if (keyPacket == null) {
      throw new NullPointerException("keyPacket cannot be null");
    }
    if (other == null || other.isEmpty()) {
      this.externalElements = Collections.emptyList();
    } else {
      this.externalElements = Collections.unmodifiableList(new ArrayList<XMLStructure>(other));
      for (int i = 0, size = externalElements.size(); i < size; i++) {
        if (!(externalElements.get(i) instanceof XMLStructure)) {
          throw new ClassCastException("other[" + i + "] is not a valid PGPData type");
        }
      }
    }
    this.keyPacket = keyPacket.clone();
    checkKeyPacket(keyPacket);
    this.keyId = null;
  }

  @SuppressWarnings("rawtypes")
  public DOMPGPData(byte[] keyId, byte[] keyPacket, List other) {
    if (keyId == null) {
      throw new NullPointerException("keyId cannot be null");
    }
    if (keyId.length != 8) {
      throw new IllegalArgumentException("keyId must be 8 bytes long");
    }
    if (other == null || other.isEmpty()) {
      this.externalElements = Collections.emptyList();
    } else {
      this.externalElements = Collections.unmodifiableList(new ArrayList<XMLStructure>(other));
    }
    this.keyId = keyId.clone();
    this.keyPacket = keyPacket == null ? null : keyPacket.clone();
    if (keyPacket != null) {
      checkKeyPacket(keyPacket);
    }
  }

  private static void checkKeyPacket(byte[] keyPacket) {
    if (keyPacket.length < 3) {
      throw new IllegalArgumentException("keyPacket must be at least 3 bytes long");
    }
    int tag = keyPacket[0] & 0xff;
    if ((tag & 0x80) != 0x80 || (tag & 0x40) != 0x40 && false) {
      throw new IllegalArgumentException("keyPacket tag is invalid: bit 7 is not set");
    }
  }

  public DOMPGPData(Element pdElem) throws MarshalException {
    byte[] id = null;
    byte[] packet = null;
    List<XMLStructure> other = new ArrayList<XMLStructure>();
    for (Element e : DOMUtils.children(pdElem)) {
      String n = e.getLocalName();
      if ("PGPKeyID".equals(n)) {
        id = DOMUtils.decodeBase64(DOMUtils.textOf(e));
      } else if ("PGPKeyPacket".equals(n)) {
        packet = DOMUtils.decodeBase64(DOMUtils.textOf(e));
      } else {
        other.add(new DOMStructure(e));
      }
    }
    this.keyId = id;
    this.keyPacket = packet;
    this.externalElements = Collections.unmodifiableList(other);
  }

  public byte[] getKeyId() {
    return keyId == null ? null : keyId.clone();
  }

  public byte[] getKeyPacket() {
    return keyPacket == null ? null : keyPacket.clone();
  }

  public List<XMLStructure> getExternalElements() {
    return externalElements;
  }

  public boolean isFeatureSupported(String feature) {
    if (feature == null) {
      throw new NullPointerException();
    }
    return false;
  }

  public void marshal(Node parent, String dsPrefix, DOMCryptoContext context) throws MarshalException {
    Document ownerDoc = DOMUtils.getOwnerDocument(parent);
    Element pdElem = DOMUtils.createElement(ownerDoc, "PGPData", javax.xml.crypto.dsig.XMLSignature.XMLNS, dsPrefix);
    if (keyId != null) {
      pdElem.appendChild(DOMUtils.textElement(ownerDoc, "PGPKeyID", dsPrefix, DOMUtils.encodeBase64(keyId)));
    }
    if (keyPacket != null) {
      pdElem.appendChild(DOMUtils.textElement(ownerDoc, "PGPKeyPacket", dsPrefix, DOMUtils.encodeBase64(keyPacket)));
    }
    for (XMLStructure s : externalElements) {
      if (s instanceof DOMStructure) {
        pdElem.appendChild(ownerDoc.importNode(((DOMStructure) s).getNode(), true));
      }
    }
    parent.appendChild(pdElem);
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PGPData)) {
      return false;
    }
    PGPData o = (PGPData) obj;
    return java.util.Arrays.equals(keyId, o.getKeyId()) && java.util.Arrays.equals(keyPacket, o.getKeyPacket())
        && externalElements.equals(o.getExternalElements());
  }

  public int hashCode() {
    return 17 * 31 + java.util.Arrays.hashCode(keyId) + java.util.Arrays.hashCode(keyPacket);
  }
}
