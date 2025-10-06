/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security.cert;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

public class PolicyQualifierInfo {
  private final byte[] mEncoded;
  private final String mId;
  private final byte[] mData;

  public PolicyQualifierInfo(byte[] encoded) throws IOException {
    mEncoded = encoded.clone();
    DerValue val = new DerValue(mEncoded);
    if (val.getTag() != DerValue.tag_Sequence) {
      throw new IOException("Invalid encoding for PolicyQualifierInfo");
    }
    DerInputStream in = val.data();
    mId = in.getOID().toString();
    if (in.available() != 0) {
      mData = in.getDerValue().toByteArray();
    } else {
      mData = null;
    }
  }

  public final String getPolicyQualifierId() {
    return mId;
  }

  public final byte[] getEncoded() {
    return mEncoded.clone();
  }

  public final byte[] getPolicyQualifier() {
    return (mData == null ? null : mData.clone());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("PolicyQualifierInfo: [\n");
    sb.append("  qualifierID: " + mId + "\n");
    sb.append("  qualifier: " + (mData == null ? "null" : sun.security.util.HexDumpEncoder.describe("", mData)) + "\n");
    sb.append("]");
    return sb.toString();
  }
}
