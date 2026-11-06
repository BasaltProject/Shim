/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.security;

import java.security.Provider;

public final class XMLDSigProvider extends Provider {
  private static final long serialVersionUID = 1L;

  public XMLDSigProvider() {
    super("XMLDSig", "25", "XMLDSig (DOM XMLSignatureFactory; DOM KeyInfoFactory; "
        + "C14N 1.0, C14N 1.1, Exclusive C14N, Base64, Enveloped, XPath, XPath2, XSLT TransformServices)");
    putService(new Service(this, "XMLSignatureFactory", "DOM",
        "org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory", null, null));
    putService(new Service(this, "KeyInfoFactory", "DOM", "org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory", null,
        null));
  }
}
