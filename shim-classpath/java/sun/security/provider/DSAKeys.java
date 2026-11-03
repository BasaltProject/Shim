/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

final class DSAKeys {
  static final String OID_DSA = "1.2.840.10040.4.1";

  private DSAKeys() {
  }

  static byte[] algId(DSAParams params) throws IOException {
    DerOutputStream alg = new DerOutputStream();
    alg.putOID(new ObjectIdentifier(OID_DSA));
    if (params != null) {
      DerOutputStream p = new DerOutputStream();
      p.putInteger(params.getP());
      p.putInteger(params.getQ());
      p.putInteger(params.getG());
      alg.write(DerValue.tag_Sequence, p);
    }
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, alg);
    return out.toByteArray();
  }

  static DSAParams parseParams(DerValue algId) throws IOException {
    DerInputStream ai = algId.data();
    String oid = ai.getOID().toString();
    if (!oid.equals(OID_DSA)) {
      throw new IOException("Unsupported key algorithm: " + oid);
    }
    if (ai.available() == 0) {
      return null;
    }
    DerInputStream p = ai.getDerValue().data();
    BigInteger pp = p.getDerValue().getBigInteger();
    BigInteger q = p.getDerValue().getBigInteger();
    BigInteger g = p.getDerValue().getBigInteger();
    return new DSAParameterSpec(pp, q, g);
  }

  static final class Pub implements DSAPublicKey {
    private static final long serialVersionUID = 1234567890123456789L;
    private final BigInteger y;
    private final DSAParams params;
    private transient byte[] encoded;

    Pub(BigInteger y, BigInteger p, BigInteger q, BigInteger g) {
      this.y = y;
      this.params = new DSAParameterSpec(p, q, g);
    }

    Pub(byte[] enc) throws InvalidKeyException {
      try {
        DerInputStream in = new DerValue(enc).data();
        this.params = parseParams(in.getDerValue());
        byte[] bits = in.getDerValue().getBitString();
        this.y = new DerValue(bits).getBigInteger();
        this.encoded = enc.clone();
      } catch (IOException e) {
        throw new InvalidKeyException("Invalid DSA public key encoding", e);
      }
    }

    public BigInteger getY() {
      return y;
    }

    public DSAParams getParams() {
      return params;
    }

    public String getAlgorithm() {
      return "DSA";
    }

    public String getFormat() {
      return "X.509";
    }

    public synchronized byte[] getEncoded() {
      if (encoded == null) {
        try {
          DerOutputStream yi = new DerOutputStream();
          yi.putInteger(y);
          DerOutputStream body = new DerOutputStream();
          body.write(algId(params));
          body.putBitString(yi.toByteArray());
          DerOutputStream out = new DerOutputStream();
          out.write(DerValue.tag_Sequence, body);
          encoded = out.toByteArray();
        } catch (IOException e) {
          throw new ProviderException(e);
        }
      }
      return encoded.clone();
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof DSAPublicKey)) {
        return false;
      }
      DSAPublicKey k = (DSAPublicKey) o;
      return y.equals(k.getY()) && params.getP().equals(k.getParams().getP())
          && params.getQ().equals(k.getParams().getQ()) && params.getG().equals(k.getParams().getG());
    }

    public int hashCode() {
      return y.hashCode() ^ params.getP().hashCode();
    }

    public String toString() {
      return "Sun DSA Public Key\n    Parameters:DSA\n\tp: " + params.getP().toString(16) + "\n\tq: "
          + params.getQ().toString(16) + "\n\tg: " + params.getG().toString(16) + "\n  y:\n" + y.toString(16);
    }
  }

  static final class Priv implements DSAPrivateKey {
    private static final long serialVersionUID = 1234567890123456780L;
    private final BigInteger x;
    private final DSAParams params;
    private transient byte[] encoded;

    Priv(BigInteger x, BigInteger p, BigInteger q, BigInteger g) {
      this.x = x;
      this.params = new DSAParameterSpec(p, q, g);
    }

    Priv(byte[] enc) throws InvalidKeyException {
      try {
        DerInputStream in = new DerValue(enc).data();
        in.getDerValue();
        this.params = parseParams(in.getDerValue());
        this.x = new DerValue(in.getDerValue().getOctetString()).getBigInteger();
        this.encoded = enc.clone();
      } catch (IOException e) {
        throw new InvalidKeyException("Invalid DSA private key encoding", e);
      }
    }

    public BigInteger getX() {
      return x;
    }

    public DSAParams getParams() {
      return params;
    }

    public String getAlgorithm() {
      return "DSA";
    }

    public String getFormat() {
      return "PKCS#8";
    }

    public synchronized byte[] getEncoded() {
      if (encoded == null) {
        try {
          DerOutputStream xi = new DerOutputStream();
          xi.putInteger(x);
          DerOutputStream body = new DerOutputStream();
          body.putInteger(0);
          body.write(algId(params));
          body.putOctetString(xi.toByteArray());
          DerOutputStream out = new DerOutputStream();
          out.write(DerValue.tag_Sequence, body);
          encoded = out.toByteArray();
        } catch (IOException e) {
          throw new ProviderException(e);
        }
      }
      return encoded.clone();
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof DSAPrivateKey)) {
        return false;
      }
      DSAPrivateKey k = (DSAPrivateKey) o;
      return x.equals(k.getX()) && params.getP().equals(k.getParams().getP());
    }

    public int hashCode() {
      return x.hashCode() ^ params.getP().hashCode();
    }

    public String toString() {
      return "Sun DSA Private Key";
    }
  }
}
