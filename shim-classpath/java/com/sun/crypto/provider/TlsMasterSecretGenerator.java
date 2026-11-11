/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.crypto.provider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.ProviderException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import sun.security.internal.interfaces.TlsMasterSecret;
import sun.security.internal.spec.TlsMasterSecretParameterSpec;

public final class TlsMasterSecretGenerator extends KeyGeneratorSpi {
  private static final String kMsg = "TlsMasterSecretGenerator must be initialized using a "
      + "TlsMasterSecretParameterSpec";

  private TlsMasterSecretParameterSpec spec;
  private int protocolVersion;

  @Override
  protected void engineInit(SecureRandom random) {
    throw new InvalidParameterException(kMsg);
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    if (!(params instanceof TlsMasterSecretParameterSpec)) {
      throw new InvalidAlgorithmParameterException(kMsg);
    }
    TlsMasterSecretParameterSpec s = (TlsMasterSecretParameterSpec) params;
    if (!"RAW".equals(s.getPremasterSecret().getFormat())) {
      throw new InvalidAlgorithmParameterException("Key format must be RAW");
    }
    int version = s.getMajorVersion() << 8 | s.getMinorVersion();
    if (version < 0x0300 || version > 0x0303) {
      throw new InvalidAlgorithmParameterException("Only SSL 3.0, TLS 1.0/1.1/1.2 supported");
    }
    protocolVersion = version;
    spec = s;
  }

  @Override
  protected void engineInit(int keysize, SecureRandom random) {
    throw new InvalidParameterException(kMsg);
  }

  @Override
  protected SecretKey engineGenerateKey() {
    if (spec == null) {
      throw new IllegalStateException("TlsMasterSecretGenerator must be initialized");
    }
    SecretKey premaster = spec.getPremasterSecret();
    byte[] pre = premaster.getEncoded();
    int major = -1;
    int minor = -1;
    if ("TlsRsaPremasterSecret".equals(premaster.getAlgorithm()) && pre != null && pre.length > 1) {
      major = pre[0] & 0xFF;
      minor = pre[1] & 0xFF;
    }
    byte[] cr = spec.getClientRandom();
    byte[] sr = spec.getServerRandom();
    byte[] session = spec.getExtendedMasterSecretSessionHash();
    try {
      byte[] master;
      if (protocolVersion == 0x0300) {
        master = TlsKdf.ssl3(pre, TlsKdf.concat(cr, sr), 48);
      } else if (session.length != 0) {
        master = TlsKdf.prf(protocolVersion, pre, "extended master secret", session, 48, spec.getPRFHashAlg(),
            spec.getPRFBlockSize());
      } else {
        master = TlsKdf.prf(protocolVersion, pre, "master secret", TlsKdf.concat(cr, sr), 48, spec.getPRFHashAlg(),
            spec.getPRFBlockSize());
      }
      return new TlsMasterSecretKey(master, major, minor);
    } catch (java.security.GeneralSecurityException e) {
      throw new ProviderException("Could not generate master secret", e);
    }
  }

  private static final class TlsMasterSecretKey extends SecretKeySpec implements TlsMasterSecret {
    private static final long serialVersionUID = 1019571680375368880L;
    private final int major;
    private final int minor;

    TlsMasterSecretKey(byte[] key, int major, int minor) {
      super(key, "TlsMasterSecret");
      this.major = major;
      this.minor = minor;
    }

    @Override
    public int getMajorVersion() {
      return major;
    }

    @Override
    public int getMinorVersion() {
      return minor;
    }
  }
}
