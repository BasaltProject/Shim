/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.IOException;
import java.security.AlgorithmParametersSpi;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class OAEPParameters extends AlgorithmParametersSpi {
  private String mdName;
  private MGF1ParameterSpec mgfSpec;
  private byte[] p;

  public OAEPParameters() {
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (!(paramSpec instanceof OAEPParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    OAEPParameterSpec spec = (OAEPParameterSpec) paramSpec;
    mdName = spec.getDigestAlgorithm();
    String mgfName = spec.getMGFAlgorithm();
    if (!mgfName.equalsIgnoreCase("MGF1")) {
      throw new InvalidParameterSpecException("Unsupported mgf " + mgfName + "; MGF1 only");
    }
    AlgorithmParameterSpec mgfSpec = spec.getMGFParameters();
    if (!(mgfSpec instanceof MGF1ParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate mgf parameters; non-null MGF1ParameterSpec only");
    }
    this.mgfSpec = (MGF1ParameterSpec) mgfSpec;
    PSource pSrc = spec.getPSource();
    if (pSrc.getAlgorithm().equals("PSpecified")) {
      p = ((PSource.PSpecified) pSrc).getValue();
    } else {
      throw new InvalidParameterSpecException("Unsupported pSource " + pSrc.getAlgorithm() + "; PSpecified only");
    }
  }

  @Override
  protected void engineInit(byte[] encoded) throws IOException {
    DerValue seq = new DerValue(encoded);
    String md = "SHA-1";
    String mgfMd = "SHA-1";
    byte[] label = new byte[0];
    DerInputStream in = seq.data();
    while (in.available() > 0) {
      DerValue v = in.getDerValue();
      DerValue inner = new DerValue(v.getDataBytes());
      if (v.isContextSpecific((byte) 0)) {
        String name = digestNameForOid(inner.data().getOID().toString());
        if (name == null) {
          throw new IOException("Unsupported hash algorithm");
        }
        md = name;
      } else if (v.isContextSpecific((byte) 1)) {
        DerInputStream ai = inner.data();
        ai.getOID();
        mgfMd = digestNameForOid(ai.getDerValue().data().getOID().toString());
        if (mgfMd == null) {
          throw new IOException("Unsupported MGF1 hash algorithm");
        }
      } else if (v.isContextSpecific((byte) 2)) {
        DerInputStream ai = inner.data();
        ai.getOID();
        label = ai.getOctetString();
      }
    }
    mdName = md;
    mgfSpec = new MGF1ParameterSpec(mgfMd);
    p = label;
  }

  private static String digestNameForOid(String oid) {
    String[] names = {"SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512", "SHA-512/224", "SHA-512/256"};
    String[] oids = {"1.3.14.3.2.26", "2.16.840.1.101.3.4.2.4", "2.16.840.1.101.3.4.2.1", "2.16.840.1.101.3.4.2.2",
      "2.16.840.1.101.3.4.2.3", "2.16.840.1.101.3.4.2.5", "2.16.840.1.101.3.4.2.6"};
    for (int i = 0; i < oids.length; i++) {
      if (oids[i].equals(oid)) {
        return names[i];
      }
    }
    return null;
  }

  private static String oidForDigest(String name) {
    String[] names = {"SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512", "SHA-512/224", "SHA-512/256"};
    String[] oids = {"1.3.14.3.2.26", "2.16.840.1.101.3.4.2.4", "2.16.840.1.101.3.4.2.1", "2.16.840.1.101.3.4.2.2",
      "2.16.840.1.101.3.4.2.3", "2.16.840.1.101.3.4.2.5", "2.16.840.1.101.3.4.2.6"};
    for (int i = 0; i < names.length; i++) {
      if (names[i].equalsIgnoreCase(name)) {
        return oids[i];
      }
    }
    throw new ProviderException("Unsupported digest: " + name);
  }

  @Override
  protected void engineInit(byte[] encoded, String decodingMethod) throws IOException {
    engineInit(encoded);
  }

  @Override
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (OAEPParameterSpec.class.isAssignableFrom(paramSpec)) {
      return paramSpec.cast(new OAEPParameterSpec(mdName, "MGF1", mgfSpec, new PSource.PSpecified(p)));
    }
    throw new InvalidParameterSpecException("Inappropriate parameter specification");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    DerOutputStream body = new DerOutputStream();
    try {
      if (!mdName.equalsIgnoreCase("SHA-1")) {
        body.write((byte) 0xa0, algId(oidForDigest(mdName), true).toByteArray());
      }
      if (!mgfSpec.getDigestAlgorithm().equalsIgnoreCase("SHA-1")) {
        DerOutputStream mgf = new DerOutputStream();
        mgf.putOID(new ObjectIdentifier("1.2.840.113549.1.1.8"));
        mgf.putDerValue(algId(oidForDigest(mgfSpec.getDigestAlgorithm()), true));
        DerOutputStream wrapper = new DerOutputStream();
        wrapper.write(DerValue.tag_Sequence, mgf);
        body.write((byte) 0xa1, wrapper.toByteArray());
      }
      if (p.length != 0) {
        DerOutputStream ps = new DerOutputStream();
        ps.putOID(new ObjectIdentifier("1.2.840.113549.1.1.9"));
        ps.putOctetString(p);
        DerOutputStream wrapper = new DerOutputStream();
        wrapper.write(DerValue.tag_Sequence, ps);
        body.write((byte) 0xa2, wrapper.toByteArray());
      }
    } catch (IOException e) {
      throw new ProviderException(e);
    }
    DerOutputStream out = new DerOutputStream();
    out.write(DerValue.tag_Sequence, body);
    return out.toByteArray();
  }

  private static DerValue algId(String oid, boolean withNull) throws IOException {
    DerOutputStream out = new DerOutputStream();
    out.putOID(new ObjectIdentifier(oid));
    if (withNull) {
      out.putNull();
    }
    DerOutputStream seq = new DerOutputStream();
    seq.write(DerValue.tag_Sequence, out);
    return new DerValue(seq.toByteArray());
  }

  @Override
  protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
    return engineGetEncoded();
  }

  @Override
  protected String engineToString() {
    StringBuilder sb = new StringBuilder();
    sb.append("MD: ").append(mdName).append("\nMGF: MGF1").append(mgfSpec.getDigestAlgorithm()).append("\nPSource: PSpecified\n");
    return sb.toString();
  }
}
