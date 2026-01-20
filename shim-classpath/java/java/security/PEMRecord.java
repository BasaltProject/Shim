/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.util.Arrays;
import java.util.Objects;

public final class PEMRecord implements DEREncodable {
  private final String type;
  private final String content;
  private final byte[] leadingData;

  public PEMRecord(String type, String content, byte[] leadingData) {
    Objects.requireNonNull(type, "\"type\" cannot be null.");
    Objects.requireNonNull(content, "\"content\" cannot be null.");
    this.type = type;
    this.content = content;
    this.leadingData = leadingData;
  }

  public PEMRecord(String type, String content) {
    this(type, content, null);
  }

  public String type() {
    return type;
  }

  public String content() {
    return content;
  }

  public byte[] leadingData() {
    return leadingData;
  }

  @Override
  public String toString() {
    return PEMEncoder.format(type, content);
  }

  @Override
  public int hashCode() {
    return 31 * (31 * type.hashCode() + content.hashCode()) + System.identityHashCode(leadingData);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PEMRecord)) {
      return false;
    }
    PEMRecord other = (PEMRecord) o;
    return type.equals(other.type) && content.equals(other.content) && leadingData == other.leadingData;
  }
}
