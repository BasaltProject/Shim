/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ec;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import sun.security.util.CurveDB;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.NamedCurve;
import sun.security.util.ObjectIdentifier;

public final class ECParameters extends AlgorithmParametersSpi {
  private CurveDB.Info info;

  public ECParameters() {
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (paramSpec == null) {
      throw new InvalidParameterSpecException("paramSpec must not be null");
    }
    if (paramSpec instanceof NamedCurve) {
      info = CurveDB.lookupInfo(((NamedCurve) paramSpec).getObjectId());
      return;
    }
    if (paramSpec instanceof ECParameterSpec) {
      NamedCurve nc = CurveDB.lookup((ECParameterSpec) paramSpec);
      if (nc == null) {
        throw new InvalidParameterSpecException("Not a supported curve: " + paramSpec);
      }
      info = CurveDB.lookupInfo(nc.getObjectId());
    } else if (paramSpec instanceof ECGenParameterSpec) {
      String name = ((ECGenParameterSpec) paramSpec).getName();
      CurveDB.Info i = CurveDB.lookupInfo(name);
      if (i == null) {
        throw new InvalidParameterSpecException("Not a supported curve: " + paramSpec);
      }
      info = i;
    } else {
      throw new InvalidParameterSpecException(
          "Only ECParameterSpec, ECGenParameterSpec and ECKeySizeParameterSpec supported");
    }
  }

  @Override
  protected void engineInit(byte[] encoded) throws IOException {
    DerValue val = new DerValue(encoded);
    if (val.getTag() != DerValue.tag_ObjectId) {
      throw new IOException("Only named ECParameters supported");
    }
    String oid = val.getOID().toString();
    CurveDB.Info i = CurveDB.lookupInfo(oid);
    if (i == null) {
      throw new IOException("Unknown named curve: " + oid);
    }
    info = i;
  }

  @Override
  protected void engineInit(byte[] encoded, String decodingMethod) throws IOException {
    engineInit(encoded);
  }

  @Override
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> spec)
      throws InvalidParameterSpecException {
    if (spec.isAssignableFrom(ECParameterSpec.class)) {
      NamedCurve nc = info.supported ? CurveDB.curve(info) : null;
      if (nc == null) {
        throw new InvalidParameterSpecException("Not a supported curve: " + info);
      }
      return spec.cast(nc);
    }
    if (spec.isAssignableFrom(ECGenParameterSpec.class)) {
      return spec.cast(new ECGenParameterSpec(info.name));
    }
    throw new InvalidParameterSpecException("Only ECParameterSpec and ECGenParameterSpec supported");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    DerOutputStream out = new DerOutputStream();
    out.putOID(new ObjectIdentifier(info.oid));
    return out.toByteArray();
  }

  @Override
  protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
    return engineGetEncoded();
  }

  @Override
  protected String engineToString() {
    return info == null ? "Not initialized" : info.toString();
  }
}
