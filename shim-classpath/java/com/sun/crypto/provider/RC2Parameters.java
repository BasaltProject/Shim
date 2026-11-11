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
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import javax.crypto.spec.RC2ParameterSpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.HexDumpEncoder;

public final class RC2Parameters extends AlgorithmParametersSpi {
  private static final int[] kVersionTable = {
      189, 86, 234, 242, 162, 241, 172, 42, 176, 147, 209, 156,
      27, 51, 253, 208, 48, 4, 182, 220, 125, 223, 50, 75,
      247, 203, 69, 155, 49, 187, 33, 90, 65, 159, 225, 217,
      74, 77, 158, 218, 160, 104, 44, 195, 39, 95, 128, 54,
      62, 238, 251, 149, 26, 254, 206, 168, 52, 169, 19, 240,
      166, 63, 216, 12, 120, 36, 175, 35, 82, 193, 103, 23,
      245, 102, 144, 231, 232, 7, 184, 96, 72, 230, 30, 83,
      243, 146, 164, 114, 140, 8, 21, 110, 134, 0, 132, 250,
      244, 127, 138, 66, 25, 246, 219, 205, 20, 141, 80, 18,
      186, 60, 6, 78, 236, 179, 53, 17, 161, 136, 142, 43,
      148, 153, 183, 113, 116, 211, 228, 191, 58, 222, 150, 14,
      188, 10, 237, 119, 252, 55, 107, 3, 121, 137, 98, 198,
      215, 192, 210, 124, 106, 139, 34, 163, 91, 5, 93, 2,
      117, 213, 97, 227, 24, 143, 85, 81, 173, 31, 11, 94,
      133, 229, 194, 87, 99, 202, 61, 108, 180, 197, 204, 112,
      178, 145, 89, 13, 71, 32, 200, 79, 88, 224, 1, 226,
      22, 56, 196, 111, 59, 15, 101, 70, 190, 126, 45, 123,
      130, 249, 64, 181, 29, 115, 248, 235, 38, 199, 135, 151,
      37, 84, 177, 40, 170, 152, 157, 165, 100, 109, 122, 212,
      16, 129, 68, 239, 73, 214, 174, 46, 221, 118, 92, 47,
      167, 28, 201, 9, 105, 154, 131, 207, 41, 57, 185, 233,
      76, 255, 67, 171};

  private byte[] iv;
  private int keyBits;

  @Override
  protected void engineInit(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
    if (!(paramSpec instanceof RC2ParameterSpec)) {
      throw new InvalidParameterSpecException("Inappropriate parameter specification");
    }
    RC2ParameterSpec spec = (RC2ParameterSpec) paramSpec;
    this.keyBits = spec.getEffectiveKeyBits();
    this.iv = spec.getIV();
  }

  @Override
  protected void engineInit(byte[] encoded) throws IOException {
    DerValue val = new DerValue(encoded);
    if (val.tag != DerValue.tag_Sequence) {
      throw new IOException("RC2 parameter parsing error: SEQUENCE tag expected");
    }
    DerInputStream in = new DerInputStream(val.getDataBytes());
    int version = 0;
    int bits = 32;
    DerValue next = in.getDerValue();
    if (next.tag == DerValue.tag_Integer) {
      version = next.getInteger();
      next = in.getDerValue();
      bits = version >= 256 ? version : decodeVersion(version);
    }
    if (next.tag != DerValue.tag_OctetString) {
      throw new IOException("RC2 parameter parsing error: OCTET STRING tag expected");
    }
    byte[] tmp = next.getOctetString();
    if (tmp.length != 8) {
      throw new IOException("IV not 8 bytes long");
    }
    this.keyBits = bits;
    this.iv = tmp;
  }

  private static int decodeVersion(int version) throws IOException {
    for (int bits = 0; bits < kVersionTable.length; bits++) {
      if (kVersionTable[bits] == version) {
        return bits;
      }
    }
    throw new IOException("RC2 parameter parsing error: unknown version " + version);
  }

  @Override
  protected void engineInit(byte[] encoded, String decodingMethod) throws IOException {
    engineInit(encoded);
  }

  @Override
  protected <T extends AlgorithmParameterSpec> T engineGetParameterSpec(Class<T> paramSpec)
      throws InvalidParameterSpecException {
    if (paramSpec.isAssignableFrom(RC2ParameterSpec.class) && paramSpec != AlgorithmParameterSpec.class) {
      return paramSpec.cast(new RC2ParameterSpec(keyBits, iv));
    }
    throw new InvalidParameterSpecException("Inappropriate parameter specification");
  }

  @Override
  protected byte[] engineGetEncoded() throws IOException {
    DerOutputStream out = new DerOutputStream();
    DerOutputStream bytes = new DerOutputStream();
    if (keyBits > 0) {
      bytes.putInteger(keyBits >= 256 ? keyBits : kVersionTable[keyBits]);
    }
    bytes.putOctetString(iv);
    out.write(DerValue.tag_Sequence, bytes);
    return out.toByteArray();
  }

  @Override
  protected byte[] engineGetEncoded(String encodingMethod) throws IOException {
    return engineGetEncoded();
  }

  @Override
  protected String engineToString() {
    String text = HexDumpEncoder.describe("iv", iv);
    return text + "version:\n" + (keyBits >= 256 ? keyBits : kVersionTable[keyBits]) + "\n";
  }
}
