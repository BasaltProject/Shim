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
import java.util.List;
import javax.xml.crypto.Data;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NoSuchMechanismException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.DigestMethodParameterSpec;
import javax.xml.crypto.dsig.spec.SignatureMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

public abstract class XMLSignatureFactory {
  private String mechanismType;
  private Provider provider;

  protected XMLSignatureFactory() {
  }

  public static XMLSignatureFactory getInstance(String mechanismType) {
    if (mechanismType == null) {
      throw new NullPointerException("mechanismType cannot be null");
    }
    return find(mechanismType, null);
  }

  public static XMLSignatureFactory getInstance(String mechanismType, Provider provider) {
    if (mechanismType == null) {
      throw new NullPointerException("mechanismType cannot be null");
    } else if (provider == null) {
      throw new NullPointerException("provider cannot be null");
    }
    return find(mechanismType, provider);
  }

  public static XMLSignatureFactory getInstance(String mechanismType, String provider)
      throws NoSuchProviderException {
    if (mechanismType == null) {
      throw new NullPointerException("mechanismType cannot be null");
    } else if (provider == null) {
      throw new NullPointerException("provider cannot be null");
    }
    Provider p = Security.getProvider(provider);
    if (p == null) {
      if (provider.equals("XMLDSig")) {
        p = new org.jcp.xml.dsig.internal.dom.XMLDSigRI();
      } else {
        throw new NoSuchProviderException("No such provider: " + provider);
      }
    }
    return find(mechanismType, p);
  }

  public static XMLSignatureFactory getInstance() {
    return getInstance("DOM");
  }

  private static XMLSignatureFactory find(String mechanismType, Provider provider) {
    XMLSignatureFactory f = null;
    Provider used = provider;
    try {
      Provider[] candidates = provider != null ? new Provider[] {provider} : Security.getProviders();
      for (Provider p : candidates) {
        Provider.Service s = p.getService("XMLSignatureFactory", mechanismType);
        if (s != null) {
          Object o = Class.forName(s.getClassName()).getDeclaredConstructor().newInstance();
          if (o instanceof XMLSignatureFactory) {
            f = (XMLSignatureFactory) o;
            used = p;
            break;
          }
        }
      }
    } catch (ReflectiveOperationException e) {
      f = null;
    }
    if (f == null && mechanismType.equals("DOM")
        && (provider == null || provider.getName().equals("XMLDSig"))) {
      f = new org.jcp.xml.dsig.internal.dom.DOMXMLSignatureFactory();
      used = provider != null ? provider : new org.jcp.xml.dsig.internal.dom.XMLDSigRI();
    }
    if (f == null) {
      throw new NoSuchMechanismException("Mechanism " + mechanismType + " not available");
    }
    f.mechanismType = mechanismType;
    f.provider = used;
    return f;
  }

  public final String getMechanismType() {
    return mechanismType;
  }

  public final Provider getProvider() {
    return provider;
  }

  public abstract XMLSignature newXMLSignature(SignedInfo si, KeyInfo ki);

  @SuppressWarnings("rawtypes")
  public abstract XMLSignature newXMLSignature(SignedInfo si, KeyInfo ki, List objects, String id,
      String signatureValueId);

  public abstract Reference newReference(String uri, DigestMethod dm);

  @SuppressWarnings("rawtypes")
  public abstract Reference newReference(String uri, DigestMethod dm, List transforms, String type, String id);

  @SuppressWarnings("rawtypes")
  public abstract Reference newReference(String uri, DigestMethod dm, List transforms, String type, String id,
      byte[] digestValue);

  @SuppressWarnings("rawtypes")
  public abstract Reference newReference(String uri, DigestMethod dm, List appliedTransforms, Data result,
      List transforms, String type, String id);

  @SuppressWarnings("rawtypes")
  public abstract SignedInfo newSignedInfo(CanonicalizationMethod cm, SignatureMethod sm, List references);

  @SuppressWarnings("rawtypes")
  public abstract SignedInfo newSignedInfo(CanonicalizationMethod cm, SignatureMethod sm, List references,
      String id);

  @SuppressWarnings("rawtypes")
  public abstract XMLObject newXMLObject(List content, String id, String mimeType, String encoding);

  @SuppressWarnings("rawtypes")
  public abstract Manifest newManifest(List references);

  @SuppressWarnings("rawtypes")
  public abstract Manifest newManifest(List references, String id);

  @SuppressWarnings("rawtypes")
  public abstract SignatureProperty newSignatureProperty(List content, String target, String id);

  @SuppressWarnings("rawtypes")
  public abstract SignatureProperties newSignatureProperties(List props, String id);

  public abstract DigestMethod newDigestMethod(String algorithm, DigestMethodParameterSpec params)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

  public abstract SignatureMethod newSignatureMethod(String algorithm, SignatureMethodParameterSpec params)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

  public abstract Transform newTransform(String algorithm, TransformParameterSpec params)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

  public abstract Transform newTransform(String algorithm, XMLStructure params) throws NoSuchAlgorithmException,
      InvalidAlgorithmParameterException;

  public abstract CanonicalizationMethod newCanonicalizationMethod(String algorithm,
      C14NMethodParameterSpec params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

  public abstract CanonicalizationMethod newCanonicalizationMethod(String algorithm, XMLStructure params)
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

  public final KeyInfoFactory getKeyInfoFactory() {
    return KeyInfoFactory.getInstance(getMechanismType(), getProvider());
  }

  public abstract XMLSignature unmarshalXMLSignature(javax.xml.crypto.dsig.XMLValidateContext context)
      throws MarshalException;

  public abstract XMLSignature unmarshalXMLSignature(XMLStructure xmlStructure) throws MarshalException;

  public abstract boolean isFeatureSupported(String feature);

  public abstract URIDereferencer getURIDereferencer();
}
