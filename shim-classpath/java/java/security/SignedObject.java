/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class SignedObject implements Serializable {
  private static final long serialVersionUID = 720502720485447167L;

  private byte[] content;
  private byte[] signature;
  private String thealgorithm;

  public SignedObject(Serializable object, PrivateKey signingKey, Signature signingEngine)
      throws IOException, InvalidKeyException, SignatureException {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    ObjectOutputStream a = new ObjectOutputStream(b);
    a.writeObject(object);
    a.flush();
    a.close();
    this.content = b.toByteArray();
    b.close();
    this.sign(signingKey, signingEngine);
  }

  public Object getObject() throws IOException, ClassNotFoundException {
    ByteArrayInputStream b = new ByteArrayInputStream(this.content);
    ObjectInputStream a = new ObjectInputStream(b);
    Object obj = a.readObject();
    b.close();
    a.close();
    return obj;
  }

  public byte[] getSignature() {
    return this.signature.clone();
  }

  public String getAlgorithm() {
    return this.thealgorithm;
  }

  public boolean verify(PublicKey verificationKey, Signature verificationEngine)
      throws InvalidKeyException, SignatureException {
    verificationEngine.initVerify(verificationKey);
    verificationEngine.update(this.content.clone());
    return verificationEngine.verify(this.signature.clone());
  }

  private void sign(PrivateKey signingKey, Signature signingEngine) throws InvalidKeyException, SignatureException {
    signingEngine.initSign(signingKey);
    signingEngine.update(this.content.clone());
    this.signature = signingEngine.sign().clone();
    this.thealgorithm = signingEngine.getAlgorithm();
  }
}
