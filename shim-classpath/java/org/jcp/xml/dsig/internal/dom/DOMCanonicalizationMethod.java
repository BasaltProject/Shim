/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.jcp.xml.dsig.internal.dom;

import java.security.InvalidAlgorithmParameterException;
import java.security.spec.AlgorithmParameterSpec;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import org.w3c.dom.Element;

public final class DOMCanonicalizationMethod extends DOMTransform implements CanonicalizationMethod {
  public DOMCanonicalizationMethod(String algorithm, AlgorithmParameterSpec spec)
      throws InvalidAlgorithmParameterException, java.security.NoSuchAlgorithmException {
    super(check(algorithm), spec);
    this.elementName = "CanonicalizationMethod";
  }

  private static String check(String algorithm) throws InvalidAlgorithmParameterException,
      java.security.NoSuchAlgorithmException {
    if (!DOMTransform.isKnown(algorithm)) {
      throw new java.security.NoSuchAlgorithmException(algorithm + " algorithm and DOM mechanism not available");
    }
    if (!algorithm.equals(INCLUSIVE) && !algorithm.equals(INCLUSIVE_WITH_COMMENTS) && !algorithm.equals(EXCLUSIVE)
        && !algorithm.equals(EXCLUSIVE_WITH_COMMENTS) && !algorithm.equals(INCLUSIVE_11)
        && !algorithm.equals(INCLUSIVE_11_WITH_COMMENTS)) {
      throw new InvalidAlgorithmParameterException("Illegal CanonicalizationMethod");
    }
    return algorithm;
  }

  public DOMCanonicalizationMethod(Element elem) throws MarshalException {
    super(elem);
    this.elementName = "CanonicalizationMethod";
  }
}
