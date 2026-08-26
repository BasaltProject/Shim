/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig;

public interface CanonicalizationMethod extends javax.xml.crypto.dsig.Transform {
  java.lang.String INCLUSIVE = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
  java.lang.String INCLUSIVE_WITH_COMMENTS = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments";
  java.lang.String EXCLUSIVE = "http://www.w3.org/2001/10/xml-exc-c14n#";
  java.lang.String EXCLUSIVE_WITH_COMMENTS = "http://www.w3.org/2001/10/xml-exc-c14n#WithComments";
  java.lang.String INCLUSIVE_11 = "http://www.w3.org/2006/12/xml-c14n11";
  java.lang.String INCLUSIVE_11_WITH_COMMENTS = "http://www.w3.org/2006/12/xml-c14n11#WithComments";

  java.security.spec.AlgorithmParameterSpec getParameterSpec();
}
