/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.xml.dom;

import org.w3c.dom.DOMException;
import shim.xml.XmlChars;

final class DomNames {
  static final String kXmlUri = "http://www.w3.org/XML/1998/namespace";
  static final String kXmlnsUri = "http://www.w3.org/2000/xmlns/";

  private DomNames() {
  }

  static boolean isName(String name) {
    return name != null && XmlChars.isXmlName(name);
  }

  static String[] split(String qualifiedName) {
    int colon = qualifiedName.indexOf(':');
    if (colon < 0) {
      return new String[] {null, qualifiedName};
    }
    return new String[] {qualifiedName.substring(0, colon), qualifiedName.substring(qualifiedName.lastIndexOf(':') + 1)};
  }

  static String normalizeNamespace(String uri) {
    return uri != null && uri.length() == 0 ? null : uri;
  }

  static void checkNamespaceWf(String qualifiedName, int colon1, int colon2) {
    if (colon1 == 0 || colon1 == qualifiedName.length() - 1 || colon2 != colon1) {
      throw DomMessages.error(DOMException.NAMESPACE_ERR);
    }
  }

  static String[] checkElement(String namespaceUri, String qualifiedName) {
    return checkQualified(namespaceUri, qualifiedName);
  }

  static String[] checkAttribute(String namespaceUri, String qualifiedName) {
    return checkQualified(namespaceUri, qualifiedName);
  }

  private static String[] checkQualified(String namespaceUri, String qualifiedName) {
    if (qualifiedName.length() == 0) {
      throw DomMessages.error(DOMException.NAMESPACE_ERR);
    }
    int colon1 = qualifiedName.indexOf(':');
    int colon2 = qualifiedName.lastIndexOf(':');
    if (colon1 < 0) {
      if (!isName(qualifiedName)) {
        throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
      }
      if ((qualifiedName.equals("xmlns") && (namespaceUri == null || !namespaceUri.equals(kXmlnsUri)))
          || (namespaceUri != null && namespaceUri.equals(kXmlnsUri) && !qualifiedName.equals("xmlns"))) {
        throw DomMessages.error(DOMException.NAMESPACE_ERR);
      }
      return new String[] {null, qualifiedName};
    }
    String prefix = qualifiedName.substring(0, colon1);
    String local = qualifiedName.substring(colon2 + 1);
    checkNamespaceWf(qualifiedName, colon1, colon2);
    if (namespaceUri == null || (prefix.equals("xml") && !namespaceUri.equals(kXmlUri))
        || (prefix.equals("xmlns") && !namespaceUri.equals(kXmlnsUri))
        || (!prefix.equals("xmlns") && namespaceUri.equals(kXmlnsUri))) {
      throw DomMessages.error(DOMException.NAMESPACE_ERR);
    }
    if (!isName(prefix) || !isName(local)) {
      throw DomMessages.error(DOMException.INVALID_CHARACTER_ERR);
    }
    return new String[] {prefix, local};
  }
}
