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

final class DHPublicKey implements javax.crypto.interfaces.DHPublicKey {
  @Serial
  private static final long serialVersionUID = 7647557958927458271L;
  private final BigInteger y;
  private final BigInteger p;
  private final BigInteger g;
  private final int l;
  private transient byte[] encoded;

  DHPublicKey(BigInteger y, BigInteger p, BigInteger g) {
    this(y, p, g, 0);
  }

  DHPublicKey(BigInteger y, BigInteger p, BigInteger g, int l) {
    this.y = y;
    this.p = p;
    this.g = g;
    this.l = l;
  }

  static DHPublicKey decode(byte[] enc) throws InvalidKeyException {
    try {
      DerInputStream top = new DerValue(enc).data();
      int[] l = new int[1];
      BigInteger[] pg = DHKeyCodec.readAlgorithmId(top.getDerValue(), l);
      byte[] bits = top.getDerValue().getBitString();
      BigInteger y = new DerValue(bits).getBigInteger();
      DHPublicKey k = new DHPublicKey(y, pg[0], pg[1], l[0]);
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
    return "X.509";
  }

  @Override
  public synchronized byte[] getEncoded() {
    if (encoded == null) {
      try {
        DerOutputStream out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, concat(DHKeyCodec.algorithmId(p, g, l), bitString()));
        encoded = out.toByteArray();
      } catch (IOException e) {
        return null;
      }
    }
    return encoded.clone();
  }

  private byte[] bitString() {
    DerOutputStream inner = new DerOutputStream();
    inner.putInteger(y);
    DerOutputStream bs = new DerOutputStream();
    bs.putBitString(inner.toByteArray());
    return bs.toByteArray();
  }

  private static DerOutputStream concat(byte[] a, byte[] b) {
    DerOutputStream o = new DerOutputStream();
    o.write(a);
    o.write(b);
    return o;
  }

  @Override
  public BigInteger getY() {
    return y;
  }

  @Override
  public DHParameterSpec getParams() {
    return l != 0 ? new DHParameterSpec(p, g, l) : new DHParameterSpec(p, g);
  }

  @Override
  public String toString() {
    String s = "SunJCE Diffie-Hellman Public Key:\ny:\n" + DHKeyCodec.hex(y) + "\np:\n" + DHKeyCodec.hex(p) + "\ng:\n"
        + DHKeyCodec.hex(g);
    return l != 0 ? s + "\nl:\n    " + l : s;
  }

  @Override
  public int hashCode() {
    return Objects.hash(y, p, g);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof javax.crypto.interfaces.DHPublicKey)) {
      return false;
    }
    javax.crypto.interfaces.DHPublicKey k = (javax.crypto.interfaces.DHPublicKey) o;
    DHParameterSpec s = k.getParams();
    return y.compareTo(k.getY()) == 0 && p.compareTo(s.getP()) == 0 && g.compareTo(s.getG()) == 0;
  }

  @Serial
  private Object writeReplace() throws ObjectStreamException {
    return new KeyRep(KeyRep.Type.PUBLIC, getAlgorithm(), getFormat(), getEncoded());
  }
}
