/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AlgorithmParametersSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class PBES2Parameters extends AlgorithmParametersSpi {
  private static final String kPbkdf2 = "1.2.840.113549.1.5.12";
  private static final String[][] kPrfs = {
      {"1.2.840.113549.2.7", "SHA1"}, {"1.2.840.113549.2.8", "SHA224"}, {"1.2.840.113549.2.9", "SHA256"},
      {"1.2.840.113549.2.10", "SHA384"}, {"1.2.840.113549.2.11", "SHA512"}, {"1.2.840.113549.2.12", "SHA512/224"},
      {"1.2.840.113549.2.13", "SHA512/256"}};
  private static final String kAes128 = "2.16.840.1.101.3.4.1.2";
  private static final String kAes256 = "2.16.840.1.101.3.4.1.42";

  private String pbeAlgo;
  private String prf = "SHA1";
  private int keyLength = 16;
  private byte[] salt;
  private int iterCount;
  private byte[] iv;

  public PBES2Parameters() {
  }

  PBES2Parameters(String pbeAlgo) {
    this.pbeAlgo = pbeAlgo;
    int under = pbeAlgo.lastIndexOf("And");
    this.prf = pbeAlgo.substring("PBEWithHmac".length(), under);
    this.keyLength = pbeAlgo.endsWith("AES_256") ? 32 : 16;
  }

  private void name() {
    pbeAlgo = "PBEWithHmac" + prf + "AndAES_" + (keyLength * 8);
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (!(paramSpec instanceof PBEParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    PBEParameterSpec spec = (PBEParameterSpec) paramSpec;
    this.salt = spec.getSalt().clone();
    this.iterCount = spec.getIterationCount();
    AlgorithmParameterSpec inner = spec.getParameterSpec();
    if (!(inner instanceof IvParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    this.iv = ((IvParameterSpec) inner).getIV();
    if (pbeAlgo == null) {
      name();
    }
  }

  @Override
  protected void engineInit(byte[] encoded) throws IOException {
    DerValue val = new DerValue(encoded);
    if (val.getTag() != DerValue.tag_Sequence) {
      throw new IOException("PBE parameter parsing error: not a sequence");
    }
    DerInputStream in = val.data();
    DerValue kdf = in.getDerValue();
    DerValue cipher = in.getDerValue();
    if (in.available() != 0) {
      throw new IOException("PBE parameter parsing error: extra data");
    }
    DerInputStream kdfIn = kdf.data();
    String kdfOid = kdfIn.getDerValue().getOID().toString();
    if (!kdfOid.equals(kPbkdf2)) {
      throw new IOException("PBE parameter parsing error: expecting the object identifier for PBKDF2");
    }
    DerValue kdfParams = kdfIn.getDerValue();
    DerInputStream p = kdfParams.data();
    this.salt = p.getDerValue().getOctetString();
    this.iterCount = p.getDerValue().getInteger();
    this.prf = "SHA1";
    this.keyLength = 0;
    while (p.available() != 0) {
      DerValue next = p.getDerValue();
      if (next.getTag() == DerValue.tag_Integer) {
        this.keyLength = next.getInteger();
      } else if (next.getTag() == DerValue.tag_Sequence) {
        String prfOid = next.data().getDerValue().getOID().toString();
        String found = null;
        for (String[] e : kPrfs) {
          if (e[0].equals(prfOid)) {
            found = e[1];
          }
        }
        if (found == null) {
          throw new IOException("PBE parameter parsing error: unsupported PRF " + prfOid);
        }
        this.prf = found;
      }
    }
    DerInputStream cipherIn = cipher.data();
    String cipherOid = cipherIn.getDerValue().getOID().toString();
    if (cipherOid.equals(kAes128)) {
      if (keyLength == 0) {
        keyLength = 16;
      }
    } else if (cipherOid.equals(kAes256)) {
      if (keyLength == 0) {
        keyLength = 32;
      }
    } else {
      throw new IOException("PBE parameter parsing error: unsupported encryption scheme " + cipherOid);
    }
    this.iv = cipherIn.getDerValue().getOctetString();
    name();
  }

  @Override
  protected void engineInit(byte[] encoded, String decodingMethod) throws IOException {
    engineInit(encoded);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (paramSpec.isAssignableFrom(PBEParameterSpec.class)) {
      return (T) new PBEParameterSpec(salt.clone(), iterCount, new IvParameterSpec(iv.clone()));
    }
    throw new InvalidParameterSpecException("Inappropriate parameter specification");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    String prfOid = null;
    for (String[] e : kPrfs) {
      if (e[1].equals(prf)) {
        prfOid = e[0];
      }
    }
    DerOutputStream kdfParams = new DerOutputStream();
    kdfParams.putOctetString(salt);
    kdfParams.putInteger(iterCount);
    kdfParams.putInteger(keyLength);
    DerOutputStream prfSeq = new DerOutputStream();
    prfSeq.putOID(ObjectIdentifier.of(prfOid));
    prfSeq.putNull();
    kdfParams.putDerValue(new DerValue(DerValue.tag_Sequence, prfSeq.toByteArray()));
    DerOutputStream kdf = new DerOutputStream();
    kdf.putOID(ObjectIdentifier.of(kPbkdf2));
    kdf.putDerValue(new DerValue(DerValue.tag_Sequence, kdfParams.toByteArray()));
    DerOutputStream enc = new DerOutputStream();
    enc.putOID(ObjectIdentifier.of(keyLength == 32 ? kAes256 : kAes128));
    enc.putOctetString(iv);
    DerOutputStream all = new DerOutputStream();
    all.putDerValue(new DerValue(DerValue.tag_Sequence, kdf.toByteArray()));
    all.putDerValue(new DerValue(DerValue.tag_Sequence, enc.toByteArray()));
    return new DerValue(DerValue.tag_Sequence, all.toByteArray()).toByteArray();
  }

  @Override
  protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
    return engineGetEncoded();
  }

  @Override
  protected String engineToString() {
    return pbeAlgo;
  }


  public static final class HmacSHA1AndAES_128 extends PBES2Parameters {
    public HmacSHA1AndAES_128() {
      super("PBEWithHmacSHA1AndAES_128");
    }
  }

  public static final class HmacSHA1AndAES_256 extends PBES2Parameters {
    public HmacSHA1AndAES_256() {
      super("PBEWithHmacSHA1AndAES_256");
    }
  }

  public static final class HmacSHA224AndAES_128 extends PBES2Parameters {
    public HmacSHA224AndAES_128() {
      super("PBEWithHmacSHA224AndAES_128");
    }
  }

  public static final class HmacSHA224AndAES_256 extends PBES2Parameters {
    public HmacSHA224AndAES_256() {
      super("PBEWithHmacSHA224AndAES_256");
    }
  }

  public static final class HmacSHA256AndAES_128 extends PBES2Parameters {
    public HmacSHA256AndAES_128() {
      super("PBEWithHmacSHA256AndAES_128");
    }
  }

  public static final class HmacSHA256AndAES_256 extends PBES2Parameters {
    public HmacSHA256AndAES_256() {
      super("PBEWithHmacSHA256AndAES_256");
    }
  }

  public static final class HmacSHA384AndAES_128 extends PBES2Parameters {
    public HmacSHA384AndAES_128() {
      super("PBEWithHmacSHA384AndAES_128");
    }
  }

  public static final class HmacSHA384AndAES_256 extends PBES2Parameters {
    public HmacSHA384AndAES_256() {
      super("PBEWithHmacSHA384AndAES_256");
    }
  }

  public static final class HmacSHA512AndAES_128 extends PBES2Parameters {
    public HmacSHA512AndAES_128() {
      super("PBEWithHmacSHA512AndAES_128");
    }
  }

  public static final class HmacSHA512AndAES_256 extends PBES2Parameters {
    public HmacSHA512AndAES_256() {
      super("PBEWithHmacSHA512AndAES_256");
    }
  }

  public static final class HmacSHA512_224AndAES_128 extends PBES2Parameters {
    public HmacSHA512_224AndAES_128() {
      super("PBEWithHmacSHA512/224AndAES_128");
    }
  }

  public static final class HmacSHA512_224AndAES_256 extends PBES2Parameters {
    public HmacSHA512_224AndAES_256() {
      super("PBEWithHmacSHA512/224AndAES_256");
    }
  }

  public static final class HmacSHA512_256AndAES_128 extends PBES2Parameters {
    public HmacSHA512_256AndAES_128() {
      super("PBEWithHmacSHA512/256AndAES_128");
    }
  }

  public static final class HmacSHA512_256AndAES_256 extends PBES2Parameters {
    public HmacSHA512_256AndAES_256() {
      super("PBEWithHmacSHA512/256AndAES_256");
    }
  }
}
