/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyRep;
import java.util.Objects;
import javax.crypto.spec.DHParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

final class DHPrivateKey implements javax.crypto.interfaces.DHPrivateKey {
  @Serial
  private static final long serialVersionUID = 7565477590005668886L;
  private final BigInteger x;
  private final BigInteger p;
  private final BigInteger g;
  private final int l;
  private transient byte[] encoded;

  DHPrivateKey(BigInteger x, BigInteger p, BigInteger g) {
    this(x, p, g, 0);
  }

  DHPrivateKey(BigInteger x, BigInteger p, BigInteger g, int l) {
    this.x = x;
    this.p = p;
    this.g = g;
    this.l = l;
  }

  static DHPrivateKey decode(byte[] enc) throws InvalidKeyException {
    try {
      DerInputStream top = new DerValue(enc).data();
      top.getBigInteger();
      int[] l = new int[1];
      BigInteger[] pg = DHKeyCodec.readAlgorithmId(top.getDerValue(), l);
      BigInteger x = new DerValue(top.getOctetString()).getBigInteger();
      DHPrivateKey k = new DHPrivateKey(x, pg[0], pg[1], l[0]);
      k.encoded = enc.clone();
      return k;
    } catch (IOException | RuntimeException e) {
      throw new InvalidKeyException("Invalid key format");
    }
  }

  @Override
  public String getAlgorithm() {
    return "DH";
  }

  @Override
  public String getFormat() {
    return "PKCS#8";
  }

  @Override
  public synchronized byte[] getEncoded() {
    if (encoded == null) {
      try {
        DerOutputStream key = new DerOutputStream();
        key.putInteger(x);
        DerOutputStream body = new DerOutputStream();
        body.putInteger(0);
        body.write(DHKeyCodec.algorithmId(p, g, l));
        body.putOctetString(key.toByteArray());
        DerOutputStream out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, body);
        encoded = out.toByteArray();
      } catch (IOException e) {
        return null;
      }
    }
    return encoded.clone();
  }

  @Override
  public BigInteger getX() {
    return x;
  }

  @Override
  public DHParameterSpec getParams() {
    return l != 0 ? new DHParameterSpec(p, g, l) : new DHParameterSpec(p, g);
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, p, g);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof javax.crypto.interfaces.DHPrivateKey)) {
      return false;
    }
    javax.crypto.interfaces.DHPrivateKey k = (javax.crypto.interfaces.DHPrivateKey) o;
    DHParameterSpec s = k.getParams();
    return x.compareTo(k.getX()) == 0 && p.compareTo(s.getP()) == 0 && g.compareTo(s.getG()) == 0;
  }

  @Serial
  private Object writeReplace() throws ObjectStreamException {
    return new KeyRep(KeyRep.Type.PRIVATE, getAlgorithm(), getFormat(), getEncoded());
  }
}
