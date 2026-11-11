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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import sun.security.internal.spec.TlsKeyMaterialParameterSpec;
import sun.security.internal.spec.TlsKeyMaterialSpec;

public final class TlsKeyMaterialGenerator extends KeyGeneratorSpi {
  private static final String kMsg = "TlsKeyMaterialGenerator must be initialized using a "
      + "TlsKeyMaterialParameterSpec";

  private TlsKeyMaterialParameterSpec spec;
  private int protocolVersion;

  @Override
  protected void engineInit(SecureRandom random) {
    throw new InvalidParameterException(kMsg);
  }

  @Override
  protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    if (!(params instanceof TlsKeyMaterialParameterSpec)) {
      throw new InvalidAlgorithmParameterException(kMsg);
    }
    TlsKeyMaterialParameterSpec s = (TlsKeyMaterialParameterSpec) params;
    if (!"RAW".equals(s.getMasterSecret().getFormat())) {
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
      throw new IllegalStateException("TlsKeyMaterialGenerator must be initialized");
    }
    try {
      return generate();
    } catch (java.security.GeneralSecurityException e) {
      throw new ProviderException("Could not generate key material", e);
    }
  }

  private SecretKey generate() throws java.security.GeneralSecurityException {
    byte[] master = spec.getMasterSecret().getEncoded();
    byte[] cr = spec.getClientRandom();
    byte[] sr = spec.getServerRandom();
    int mac = spec.getMacKeyLength();
    int keyLen = spec.getCipherKeyLength();
    int iv = spec.getIvLength();
    int expanded = protocolVersion >= 0x0302 ? 0 : spec.getExpandedCipherKeyLength();
    boolean export = expanded > 0;
    int ivInBlock = export ? 0 : iv;
    int total = 2 * (mac + keyLen + ivInBlock);
    byte[] srcr = TlsKdf.concat(sr, cr);
    byte[] block;
    if (protocolVersion == 0x0300) {
      if (total > 160) {
        throw new ArrayIndexOutOfBoundsException("Index 10 out of bounds for length 10");
      }
      block = TlsKdf.ssl3(master, srcr, total);
    } else {
      block = TlsKdf.prf(protocolVersion, master, "key expansion", srcr, total, spec.getPRFHashAlg(),
          spec.getPRFBlockSize());
    }
    int pos = 0;
    SecretKey clientMac = null;
    SecretKey serverMac = null;
    if (mac != 0) {
      clientMac = new SecretKeySpec(block, pos, mac, "Mac");
      serverMac = new SecretKeySpec(block, pos + mac, mac, "Mac");
      pos += 2 * mac;
    }
    if (keyLen == 0) {
      return new TlsKeyMaterialSpec(clientMac, serverMac);
    }
    String alg = spec.getCipherAlgorithm();
    byte[] ck = java.util.Arrays.copyOfRange(block, pos, pos + keyLen);
    byte[] sk = java.util.Arrays.copyOfRange(block, pos + keyLen, pos + 2 * keyLen);
    pos += 2 * keyLen;
    IvParameterSpec civ = null;
    IvParameterSpec siv = null;
    byte[] crsr = TlsKdf.concat(cr, sr);
    if (export) {
      if (protocolVersion == 0x0300) {
        for (int n : new int[] {expanded, iv}) {
          if (n > 16) {
            throw new ArrayIndexOutOfBoundsException("arraycopy: last source index " + n + " out of bounds for byte[16]");
          }
        }
        java.security.MessageDigest md5 = java.security.MessageDigest.getInstance("MD5");
        ck = java.util.Arrays.copyOf(md5.digest(TlsKdf.concat(ck, cr, sr)), expanded);
        sk = java.util.Arrays.copyOf(md5.digest(TlsKdf.concat(sk, sr, cr)), expanded);
        if (iv != 0) {
          civ = new IvParameterSpec(md5.digest(crsr), 0, iv);
          siv = new IvParameterSpec(md5.digest(srcr), 0, iv);
        }
      } else {
        byte[] ckx = TlsKdf.prf10(ck, TlsKdf.concat("client write key".getBytes(), crsr), expanded);
        byte[] skx = TlsKdf.prf10(sk, TlsKdf.concat("server write key".getBytes(), crsr), expanded);
        ck = ckx;
        sk = skx;
        if (iv != 0) {
          byte[] ivs = TlsKdf.prf10(new byte[0], TlsKdf.concat("IV block".getBytes(), crsr), 2 * iv);
          civ = new IvParameterSpec(ivs, 0, iv);
          siv = new IvParameterSpec(ivs, iv, iv);
        }
      }
    } else if (iv != 0) {
      civ = new IvParameterSpec(block, pos, iv);
      siv = new IvParameterSpec(block, pos + iv, iv);
    }
    return new TlsKeyMaterialSpec(clientMac, serverMac, new SecretKeySpec(ck, alg), civ, new SecretKeySpec(sk, alg),
        siv);
  }
}
