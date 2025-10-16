/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

final class DomNormalizer {
  private DomNormalizer() {
  }

  static void normalize(DocumentImpl doc, DomConfiguration config) {
    if (!config.bool("cdata-sections")) {
      convertCdata(doc);
    }
    if (!config.bool("comments")) {
      removeComments(doc);
    }
    if (!config.bool("entities")) {
      expandEntityReferences(doc);
    }
    if (!config.bool("element-content-whitespace")) {
      removeIgnorableWhitespace(doc);
    }
    doc.normalize();
    if (config.bool("namespaces")) {
      Map<String, String> scope = new HashMap<String, String>();
      scope.put("xml", DomNames.kXmlUri);
      fixup((ElementImpl) doc.getDocumentElement(), scope, new int[1], config.bool("namespace-declarations"));
    }
  }

  private static void convertCdata(NodeImpl parent) {
    for (NodeImpl n = parent.firstChildNode_(); n != null; ) {
      NodeImpl next = n.nextSibling;
      if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
        Node text = parent.ownerDocument_().createTextNode(n.getNodeValue());
        parent.replaceChild(text, n);
      } else {
        convertCdata(n);
      }
      n = next;
    }
  }

  private static void removeComments(NodeImpl parent) {
    for (NodeImpl n = parent.firstChildNode_(); n != null; ) {
      NodeImpl next = n.nextSibling;
      if (n.getNodeType() == Node.COMMENT_NODE) {
        parent.removeChild(n);
      } else {
        removeComments(n);
      }
      n = next;
    }
  }

  private static void expandEntityReferences(NodeImpl parent) {
    for (NodeImpl n = parent.firstChildNode_(); n != null; ) {
      NodeImpl next = n.nextSibling;
      if (n.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
        NodeImpl insertBefore = n;
        for (NodeImpl c = n.firstChildNode_(); c != null; c = c.nextSibling) {
          parent.insertBefore(c.cloneNode(true), insertBefore);
        }
        parent.removeChild(n);
      } else {
        expandEntityReferences(n);
      }
      n = next;
    }
  }

  private static void removeIgnorableWhitespace(NodeImpl parent) {
    for (NodeImpl n = parent.firstChildNode_(); n != null; ) {
      NodeImpl next = n.nextSibling;
      if (n.getNodeType() == Node.TEXT_NODE && ((TextImpl) n).ignorableWhitespace) {
        parent.removeChild(n);
      } else {
        removeIgnorableWhitespace(n);
      }
      n = next;
    }
  }

  private static void fixup(ElementImpl element, Map<String, String> outer, int[] counter, boolean declarations) {
    if (element == null) {
      return;
    }
    Map<String, String> scope = new HashMap<String, String>(outer);
    if (element.attributes != null) {
      for (NodeImpl a : new java.util.ArrayList<NodeImpl>(element.attributes.nodes)) {
        String an = a.getNodeName();
        if (an.equals("xmlns")) {
          scope.put("", a.getNodeValue());
        } else if (an.startsWith("xmlns:")) {
          scope.put(an.substring(6), a.getNodeValue());
        }
      }
    }
    String ns = element.getNamespaceURI();
    String prefix = element.getPrefix();
    if (element.localName != null) {
      String key = prefix == null ? "" : prefix;
      String bound = scope.get(key);
      if (ns != null) {
        if (!ns.equals(bound)) {
          if (declarations) {
            element.setAttributeNS(DomNames.kXmlnsUri, key.isEmpty() ? "xmlns" : "xmlns:" + key, ns);
          }
          scope.put(key, ns);
        }
      } else if (bound != null && !bound.isEmpty() && prefix == null) {
        if (declarations) {
          element.setAttributeNS(DomNames.kXmlnsUri, "xmlns", "");
        }
        scope.put("", "");
      }
    }
    if (element.attributes != null) {
      for (NodeImpl a : new java.util.ArrayList<NodeImpl>(element.attributes.nodes)) {
        AttrImpl attr = (AttrImpl) a;
        String ans = attr.getNamespaceURI();
        if (ans == null || ans.equals(DomNames.kXmlnsUri) || ans.equals(DomNames.kXmlUri)) {
          continue;
        }
        String ap = attr.getPrefix();
        String bound = ap == null ? null : scope.get(ap);
        if (ap == null || !ans.equals(bound)) {
          String found = null;
          for (Map.Entry<String, String> e : scope.entrySet()) {
            if (!e.getKey().isEmpty() && ans.equals(e.getValue())) {
              found = e.getKey();
              break;
            }
          }
          if (found == null) {
            found = ap != null && scope.get(ap) == null ? ap : "NS" + (++counter[0]);
            if (declarations) {
              element.setAttributeNS(DomNames.kXmlnsUri, "xmlns:" + found, ans);
            }
            scope.put(found, ans);
          }
          attr.setPrefix(found);
        }
      }
    }
    for (NodeImpl c = element.firstChildNode_(); c != null; c = c.nextSibling) {
      if (c.getNodeType() == Node.ELEMENT_NODE) {
        fixup((ElementImpl) c, scope, counter, declarations);
      }
    }
  }
}
