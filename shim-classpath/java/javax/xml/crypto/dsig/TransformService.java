/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

public abstract class TransformService implements Transform {
  private String algorithm;
  private String mechanism;
  private Provider provider;

  protected TransformService() {
  }

  public static TransformService getInstance(String algorithm, String mechanismType)
      throws NoSuchAlgorithmException {
    if (mechanismType == null || algorithm == null) {
      throw new NullPointerException();
    }
    return find(algorithm, mechanismType, null);
  }

  public static TransformService getInstance(String algorithm, String mechanismType, Provider provider)
      throws NoSuchAlgorithmException {
    if (mechanismType == null || algorithm == null || provider == null) {
      throw new NullPointerException();
    }
    return find(algorithm, mechanismType, provider);
  }

  public static TransformService getInstance(String algorithm, String mechanismType, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    if (mechanismType == null || algorithm == null || provider == null) {
      throw new NullPointerException();
    }
    Provider p = Security.getProvider(provider);
    if (p == null) {
      if (provider.equals("XMLDSig")) {
        p = new org.jcp.xml.dsig.internal.dom.XMLDSigRI();
      } else {
        throw new NoSuchProviderException("No such provider: " + provider);
      }
    }
    return find(algorithm, mechanismType, p);
  }

  private static TransformService find(String algorithm, String mechanismType, Provider provider)
      throws NoSuchAlgorithmException {
    if (mechanismType.equals("DOM") && org.jcp.xml.dsig.internal.dom.DOMTransform.isKnown(algorithm)
        && (provider == null || provider.getName().equals("XMLDSig"))) {
      TransformService ts = new org.jcp.xml.dsig.internal.dom.DOMTransformService(algorithm);
      ts.algorithm = algorithm;
      ts.mechanism = mechanismType;
      ts.provider = provider != null ? provider : new org.jcp.xml.dsig.internal.dom.XMLDSigRI();
      return ts;
    }
    throw new NoSuchAlgorithmException(algorithm + " algorithm and " + mechanismType + " mechanism not available");
  }

  public final String getMechanismType() {
    return mechanism;
  }

  public final String getAlgorithm() {
    return algorithm;
  }

  public final Provider getProvider() {
    return provider;
  }

  public abstract void init(TransformParameterSpec params) throws InvalidAlgorithmParameterException;

  public abstract void marshalParams(XMLStructure parent, XMLCryptoContext context) throws MarshalException;

  public abstract void init(XMLStructure parent, XMLCryptoContext context) throws InvalidAlgorithmParameterException;
}
