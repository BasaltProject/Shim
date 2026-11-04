/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.security.Provider;

public final class XMLDSigRI extends Provider {
  private static final long serialVersionUID = -5049765099299494554L;

  private static final String INFO = "XMLDSig (DOM XMLSignatureFactory; DOM KeyInfoFactory; "
      + "C14N 1.0, C14N 1.1, Exclusive C14N, Base64, Enveloped, XPath, XPath2, XSLT TransformServices)";

  public XMLDSigRI() {
    super("XMLDSig", "25", INFO);
    putService(new Service(this, "XMLSignatureFactory", "DOM", DOMXMLSignatureFactory.class.getName(), null, null));
    putService(new Service(this, "KeyInfoFactory", "DOM", DOMKeyInfoFactory.class.getName(), null, null));
  }
}
