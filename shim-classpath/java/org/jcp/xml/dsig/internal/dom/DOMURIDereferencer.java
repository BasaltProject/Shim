/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.crypto.Data;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dom.DOMURIReference;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class DOMURIDereferencer implements URIDereferencer {
  static final URIDereferencer INSTANCE = new DOMURIDereferencer();

  private DOMURIDereferencer() {
  }

  public Data dereference(URIReference uriRef, XMLCryptoContext context) throws URIReferenceException {
    if (uriRef == null) {
      throw new NullPointerException("uriRef cannot be null");
    }
    if (context == null) {
      throw new NullPointerException("context cannot be null");
    }
    String uri = uriRef.getURI();
    Node here = uriRef instanceof DOMURIReference ? ((DOMURIReference) uriRef).getHere() : null;
    Document doc = here == null ? null : DOMUtils.getOwnerDocument(here);
    if (uri == null || uri.length() == 0) {
      if (doc == null) {
        throw new URIReferenceException("Unable to dereference the same-document reference", null, uriRef);
      }
      return new DOMSubTreeData(doc, true);
    }
    if (uri.charAt(0) == '#') {
      String id = uri.substring(1);
      boolean comments = false;
      if (id.startsWith("xpointer(id(")) {
        int i1 = id.indexOf('\'');
        int i2 = id.indexOf('\'', i1 + 1);
        if (i1 >= 0 && i2 > i1) {
          id = id.substring(i1 + 1, i2);
          comments = true;
        }
      } else if (id.equals("xpointer(/)")) {
        if (doc == null) {
          throw new URIReferenceException("Unable to dereference the document", null, uriRef);
        }
        return new DOMSubTreeData(doc, false);
      }
      Element elem = null;
      if (context instanceof DOMCryptoContext) {
        elem = ((DOMCryptoContext) context).getElementById(id);
      }
      if (elem == null && doc != null) {
        elem = doc.getElementById(id);
      }
      if (elem == null) {
        throw new URIReferenceException("Could not find Element with ID: " + id, null, uriRef);
      }
      return new DOMSubTreeData(elem, !comments);
    }
    try {
      URI resolved = new URI(uri);
      String base = context.getBaseURI();
      if (!resolved.isAbsolute() && base != null) {
        resolved = new URI(base).resolve(resolved);
      }
      if (Boolean.TRUE.equals(context.getProperty("org.jcp.xml.dsig.secureValidation"))) {
        String scheme = resolved.getScheme();
        if (scheme != null && (scheme.equalsIgnoreCase("file") || scheme.equalsIgnoreCase("http")
            || scheme.equalsIgnoreCase("https"))) {
          throw new URIReferenceException("URI " + resolved + " is forbidden when secure validation is enabled",
              null, uriRef);
        }
      }
      return new OctetStreamData(new URL(resolved.toString()).openStream(), resolved.toString(), null);
    } catch (URISyntaxException e) {
      throw new URIReferenceException(e.getMessage(), e, uriRef);
    } catch (IOException e) {
      throw new URIReferenceException(e.getMessage(), e, uriRef);
    }
  }
}
