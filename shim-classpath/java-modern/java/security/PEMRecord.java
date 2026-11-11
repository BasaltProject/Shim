/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.util.Objects;

public record PEMRecord(String type, String content, byte[] leadingData) implements DEREncodable {
  public PEMRecord {
    Objects.requireNonNull(type, "\"type\" cannot be null.");
    Objects.requireNonNull(content, "\"content\" cannot be null.");
    if (type.startsWith("-") || type.startsWith("BEGIN") || type.startsWith("END")) {
      throw new IllegalArgumentException("PEM syntax labels found.  Only the PEM type identifier is allowed");
    }
  }

  public PEMRecord(String type, String content) {
    this(type, content, null);
  }

  @Override
  public String toString() {
    return sun.security.util.Pem.pemEncoded(this);
  }
}
