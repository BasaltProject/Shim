/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.ProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;
import sun.security.util.CurveDB;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.NamedCurve;
import sun.security.util.ObjectIdentifier;

public final class ECPrivateKeyImpl implements ECPrivateKey {
  private static final long serialVersionUID = 88695385615075129L;

  private final BigInteger s;
  private final NamedCurve params;
  private transient byte[] encoded;

  public ECPrivateKeyImpl(BigInteger s, ECParameterSpec params) throws InvalidKeyException {
    this.s = s;
    this.params = ECUtil.namedCurve(params);
  }

  public ECPrivateKeyImpl(byte[] encoded) throws InvalidKeyException {
    try {
      DerValue pkcs8 = new DerValue(encoded);
      DerInputStream in = pkcs8.data();
      int version = in.getInteger();
      if (version != 0 && version != 1) {
        throw new InvalidKeyException("Unsupported PKCS#8 version: " + version);
      }
      DerValue algId = in.getDerValue();
      DerInputStream ai = algId.data();
      String oid = ai.getOID().toString();
      if (!oid.equals(ECUtil.kOidEc)) {
        throw new InvalidKeyException("Unsupported key algorithm: " + oid);
      }
      NamedCurve nc = null;
      if (ai.available() > 0) {
        String curveOid = ai.getOID().toString();
        nc = CurveDB.lookup(curveOid);
        if (nc == null) {
          throw new InvalidKeyException("Unknown named curve: " + curveOid);
        }
      }
      byte[] inner = in.getOctetString();
      DerValue ecKey = new DerValue(inner);
      DerInputStream k = ecKey.data();
      int ecVersion = k.getInteger();
      if (ecVersion != 1) {
        throw new InvalidKeyException("Version must be 1");
      }
      byte[] priv = k.getOctetString();
      while (k.available() > 0) {
        DerValue opt = k.getDerValue();
        if (opt.isContextSpecific((byte) 0) && nc == null) {
          DerValue pv = new DerValue(opt.getDataBytes());
          nc = CurveDB.lookup(pv.getOID().toString());
        }
      }
      if (nc == null) {
        throw new InvalidKeyException("Missing curve parameters");
      }
      this.params = nc;
      this.s = new BigInteger(1, priv);
      this.encoded = encoded.clone();
    } catch (IOException e) {
      throw new InvalidKeyException("Unable to decode key", e);
    }
  }

  @Override
  public BigInteger getS() {
    return s;
  }

  @Override
  public ECParameterSpec getParams() {
    return params;
  }

  @Override
  public String getAlgorithm() {
    return "EC";
  }

  @Override
  public String getFormat() {
    return "PKCS#8";
  }

  @Override
  public synchronized byte[] getEncoded() {
    if (encoded == null) {
      try {
        int fieldBytes = (params.getCurve().getField().getFieldSize() + 7) >> 3;
        DerOutputStream ecBody = new DerOutputStream();
        ecBody.putInteger(1);
        ecBody.putOctetString(ECUtil.unsigned(s, fieldBytes));
        DerOutputStream ecKey = new DerOutputStream();
        ecKey.write(DerValue.tag_Sequence, ecBody);
        DerOutputStream alg = new DerOutputStream();
        alg.putOID(new ObjectIdentifier(ECUtil.kOidEc));
        alg.putOID(new ObjectIdentifier(params.getObjectId()));
        DerOutputStream body = new DerOutputStream();
        body.putInteger(0);
        body.write(DerValue.tag_Sequence, alg);
        body.putOctetString(ecKey.toByteArray());
        DerOutputStream out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, body);
        encoded = out.toByteArray();
      } catch (IOException e) {
        throw new ProviderException(e);
      }
    }
    return encoded.clone();
  }

  @Override
  public int hashCode() {
    return s.hashCode() * 31 + params.getOrder().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ECPrivateKey)) {
      return false;
    }
    ECPrivateKey other = (ECPrivateKey) obj;
    return s.equals(other.getS()) && params.getOrder().equals(other.getParams().getOrder())
        && params.getCurve().equals(other.getParams().getCurve());
  }
}
