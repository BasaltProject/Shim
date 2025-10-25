/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class SealedObject implements Serializable {
  private static final long serialVersionUID = 4482838265551344752L;

  private byte[] encryptedContent;
  private String sealAlg;
  private String paramsAlg;
  private byte[] encodedParams;

  public SealedObject(Serializable object, Cipher c) throws IOException, IllegalBlockSizeException {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    ObjectOutputStream a = new ObjectOutputStream(b);
    byte[] content;
    try {
      a.writeObject(object);
      a.flush();
      content = b.toByteArray();
    } finally {
      a.close();
    }
    try {
      this.encryptedContent = c.doFinal(content);
    } catch (BadPaddingException e) {
      // cannot happen when encrypting
    }
    if (c.getParameters() != null) {
      this.encodedParams = c.getParameters().getEncoded();
      this.paramsAlg = c.getParameters().getAlgorithm();
    }
    this.sealAlg = c.getAlgorithm();
  }

  protected SealedObject(SealedObject so) {
    this.encryptedContent = so.encryptedContent.clone();
    this.sealAlg = so.sealAlg;
    this.paramsAlg = so.paramsAlg;
    this.encodedParams = so.encodedParams == null ? null : so.encodedParams.clone();
  }

  public final String getAlgorithm() {
    return this.sealAlg;
  }

  public final Object getObject(Key key) throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
      InvalidKeyException {
    if (key == null) {
      throw new NullPointerException("key is null");
    }
    try {
      return unseal(key, null);
    } catch (NoSuchProviderException nspe) {
      throw new NoSuchAlgorithmException("algorithm not found");
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new InvalidKeyException(e.getMessage());
    }
  }

  public final Object getObject(Cipher c) throws IOException, ClassNotFoundException, IllegalBlockSizeException,
      BadPaddingException {
    byte[] content = c.doFinal(this.encryptedContent);
    return readObject(content);
  }

  public final Object getObject(Key key, String provider) throws IOException, ClassNotFoundException,
      NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
    if (key == null) {
      throw new NullPointerException("key is null");
    }
    if (provider == null || provider.isEmpty()) {
      throw new IllegalArgumentException("missing provider");
    }
    try {
      return unseal(key, provider);
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      throw new InvalidKeyException(e.getMessage());
    }
  }

  private Object unseal(Key key, String provider) throws IOException, ClassNotFoundException,
      NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException,
      BadPaddingException {
    AlgorithmParameters params = null;
    if (this.encodedParams != null) {
      params = provider != null ? AlgorithmParameters.getInstance(this.paramsAlg, provider)
          : AlgorithmParameters.getInstance(this.paramsAlg);
      params.init(this.encodedParams);
    }
    Cipher c;
    try {
      c = provider != null ? Cipher.getInstance(this.sealAlg, provider) : Cipher.getInstance(this.sealAlg);
    } catch (NoSuchPaddingException e) {
      throw new NoSuchAlgorithmException("Padding that was used in sealing operation not available");
    }
    try {
      if (params != null) {
        c.init(Cipher.DECRYPT_MODE, key, params);
      } else {
        c.init(Cipher.DECRYPT_MODE, key);
      }
    } catch (InvalidAlgorithmParameterException e) {
      throw new RuntimeException(e.getMessage());
    }
    return readObject(c.doFinal(this.encryptedContent));
  }

  private static Object readObject(byte[] content) throws IOException, ClassNotFoundException {
    ObjectInputStream a = new ObjectInputStream(new ByteArrayInputStream(content));
    try {
      return a.readObject();
    } finally {
      a.close();
    }
  }
}
