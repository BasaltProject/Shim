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
    String[][] transforms = {
        {"http://www.w3.org/2000/09/xmldsig#base64", "DOMBase64Transform"},
        {"http://www.w3.org/2000/09/xmldsig#enveloped-signature", "DOMEnvelopedTransform"},
        {"http://www.w3.org/2001/10/xml-exc-c14n#", "DOMExcC14NMethod"},
        {"http://www.w3.org/2001/10/xml-exc-c14n#WithComments", "DOMExcC14NMethod"},
        {"http://www.w3.org/2002/06/xmldsig-filter2", "DOMXPathFilter2Transform"},
        {"http://www.w3.org/2006/12/xml-c14n11", "DOMCanonicalXMLC14N11Method"},
        {"http://www.w3.org/2006/12/xml-c14n11#WithComments", "DOMCanonicalXMLC14N11Method"},
        {"http://www.w3.org/TR/1999/REC-xpath-19991116", "DOMXPathTransform"},
        {"http://www.w3.org/TR/1999/REC-xslt-19991116", "DOMXSLTTransform"},
        {"http://www.w3.org/TR/2001/REC-xml-c14n-20010315", "DOMCanonicalXMLC14NMethod"},
        {"http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments", "DOMCanonicalXMLC14NMethod"}};
    java.util.Map<String, String> attrs = java.util.Collections.singletonMap("MechanismType", "DOM");
    for (String[] t : transforms) {
      putService(new Service(this, "TransformService", t[0], "org.jcp.xml.dsig.internal.dom." + t[1], null, attrs));
    }
  }
}
