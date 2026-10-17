/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.ProviderException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import sun.security.util.CurveDB;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.NamedCurve;
import sun.security.util.ObjectIdentifier;

public final class ECPublicKeyImpl implements ECPublicKey {
  private static final long serialVersionUID = -2462037275160462289L;

  private final ECPoint w;
  private final NamedCurve params;
  private transient byte[] encoded;

  public ECPublicKeyImpl(ECPoint w, ECParameterSpec params) throws InvalidKeyException {
    this.w = w;
    this.params = ECUtil.namedCurve(params);
  }

  public ECPublicKeyImpl(byte[] encoded) throws InvalidKeyException {
    try {
      DerValue spki = new DerValue(encoded);
      DerInputStream in = spki.data();
      DerValue algId = in.getDerValue();
      DerInputStream ai = algId.data();
      String oid = ai.getOID().toString();
      if (!oid.equals(ECUtil.kOidEc)) {
        throw new InvalidKeyException("Unsupported key algorithm: " + oid);
      }
      String curveOid = ai.getOID().toString();
      NamedCurve nc = CurveDB.lookup(curveOid);
      if (nc == null) {
        throw new InvalidKeyException("Unknown named curve: " + curveOid);
      }
      byte[] point = in.getDerValue().getBitString();
      this.params = nc;
      this.w = ECUtil.decodePoint(point, (nc.getCurve().getField().getFieldSize() + 7) >> 3);
      this.encoded = encoded.clone();
    } catch (IOException e) {
      throw new InvalidKeyException("Unable to decode key", e);
    }
  }

  @Override
  public ECPoint getW() {
    return w;
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
    return "X.509";
  }

  @Override
  public synchronized byte[] getEncoded() {
    if (encoded == null) {
      try {
        DerOutputStream alg = new DerOutputStream();
        alg.putOID(new ObjectIdentifier(ECUtil.kOidEc));
        alg.putOID(new ObjectIdentifier(params.getObjectId()));
        DerOutputStream body = new DerOutputStream();
        body.write(DerValue.tag_Sequence, alg);
        body.putBitString(ECUtil.encodePoint(w, (params.getCurve().getField().getFieldSize() + 7) >> 3));
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
    return java.util.Arrays.hashCode(getEncoded());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ECPublicKey)) {
      return false;
    }
    ECPublicKey other = (ECPublicKey) obj;
    return w.equals(other.getW()) && params.getOrder().equals(other.getParams().getOrder())
        && params.getCurve().equals(other.getParams().getCurve());
  }

  @Override
  public String toString() {
    return "Sun EC public key, " + params.getCurve().getField().getFieldSize() + " bits\n  public x coord: "
        + w.getAffineX() + "\n  public y coord: " + w.getAffineY() + "\n  parameters: " + params;
  }
}
