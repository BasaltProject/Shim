/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.HexFormat;
import javax.crypto.*;
import javax.crypto.spec.*;
public class KdfModern { public static void main(String[] a) throws Exception {
  HexFormat h = HexFormat.of();
  KDF k = KDF.getInstance("HKDF-SHA256");
  System.out.println(k.getAlgorithm() + " " + k.getProviderName() + " " + k.getParameters());
  byte[] ikm = h.parseHex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");
  byte[] salt = h.parseHex("000102030405060708090a0b0c");
  byte[] info = h.parseHex("f0f1f2f3f4f5f6f7f8f9");
  System.out.println(h.formatHex(k.deriveData(HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(salt).thenExpand(info, 42))));
  SecretKey prk = k.deriveKey("PRK", HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(salt).extractOnly());
  System.out.println(h.formatHex(prk.getEncoded()) + " " + prk.getAlgorithm());
  System.out.println(h.formatHex(k.deriveData(HKDFParameterSpec.expandOnly(prk, info, 42))));
  try { KDF.getInstance("HKDF-SHA999"); } catch (Exception e) { System.out.println(e); }
  try { k.deriveData(HKDFParameterSpec.expandOnly(prk, info, 255*32+1)); } catch (Exception e) { System.out.println(e); }
  try { k.deriveData(null); } catch (Exception e) { System.out.println(e); }
}}
