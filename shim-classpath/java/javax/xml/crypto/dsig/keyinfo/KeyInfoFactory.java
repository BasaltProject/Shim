/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.xml.crypto.dsig.keyinfo;

import java.math.BigInteger;
import java.security.KeyException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.util.List;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NoSuchMechanismException;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.XMLStructure;

public abstract class KeyInfoFactory {
  private String mechanismType;
  private Provider provider;

  protected KeyInfoFactory() {
  }

  public static KeyInfoFactory getInstance(String mechanismType) {
    if (mechanismType == null) {
      throw new NullPointerException("mechanismType cannot be null");
    }
    return find(mechanismType, null);
  }

  public static KeyInfoFactory getInstance(String mechanismType, Provider provider) {
    if (mechanismType == null) {
      throw new NullPointerException("mechanismType cannot be null");
    } else if (provider == null) {
      throw new NullPointerException("provider cannot be null");
    }
    return find(mechanismType, provider);
  }

  public static KeyInfoFactory getInstance(String mechanismType, String provider) throws NoSuchProviderException {
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

  public static KeyInfoFactory getInstance() {
    return getInstance("DOM");
  }

  private static KeyInfoFactory find(String mechanismType, Provider provider) {
    KeyInfoFactory f = null;
    Provider used = provider;
    try {
      Provider[] candidates = provider != null ? new Provider[] {provider} : Security.getProviders();
      for (Provider p : candidates) {
        Provider.Service s = p.getService("KeyInfoFactory", mechanismType);
        if (s != null) {
          Object o = Class.forName(s.getClassName()).getDeclaredConstructor().newInstance();
          if (o instanceof KeyInfoFactory) {
            f = (KeyInfoFactory) o;
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
      f = new org.jcp.xml.dsig.internal.dom.DOMKeyInfoFactory();
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

  @SuppressWarnings("rawtypes")
  public abstract KeyInfo newKeyInfo(List content);

  @SuppressWarnings("rawtypes")
  public abstract KeyInfo newKeyInfo(List content, String id);

  public abstract KeyName newKeyName(String name);

  public abstract KeyValue newKeyValue(PublicKey key) throws KeyException;

  public abstract PGPData newPGPData(byte[] keyId);

  @SuppressWarnings("rawtypes")
  public abstract PGPData newPGPData(byte[] keyId, byte[] keyPacket, List other);

  @SuppressWarnings("rawtypes")
  public abstract PGPData newPGPData(byte[] keyPacket, List other);

  public abstract RetrievalMethod newRetrievalMethod(String uri);

  @SuppressWarnings("rawtypes")
  public abstract RetrievalMethod newRetrievalMethod(String uri, String type, List transforms);

  @SuppressWarnings("rawtypes")
  public abstract X509Data newX509Data(List content);

  public abstract X509IssuerSerial newX509IssuerSerial(String issuerName, BigInteger serialNumber);

  public abstract boolean isFeatureSupported(String feature);

  public abstract URIDereferencer getURIDereferencer();

  public abstract KeyInfo unmarshalKeyInfo(XMLStructure xmlStructure) throws MarshalException;
}
