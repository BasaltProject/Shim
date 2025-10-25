/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.crypto.SecretKey;

public interface HKDFParameterSpec extends AlgorithmParameterSpec {
  static Builder ofExtract() {
    return new Builder();
  }

  static Expand expandOnly(SecretKey prk, byte[] info, int length) {
    if (prk == null) {
      throw new NullPointerException("prk must not be null");
    }
    return new Expand(prk, info, length);
  }

  final class Builder {
    private final List<SecretKey> ikms = new ArrayList<SecretKey>();
    private final List<SecretKey> salts = new ArrayList<SecretKey>();

    private Builder() {
    }

    public Builder addIKM(SecretKey ikm) {
      ikms.add(Objects.requireNonNull(ikm, "ikm must not be null"));
      return this;
    }

    public Builder addIKM(byte[] ikm) {
      Objects.requireNonNull(ikm, "ikm must not be null");
      if (ikm.length != 0) {
        ikms.add(new SecretKeySpec(ikm.clone(), "Generic"));
      }
      return this;
    }

    public Builder addSalt(SecretKey salt) {
      salts.add(Objects.requireNonNull(salt, "salt must not be null"));
      return this;
    }

    public Builder addSalt(byte[] salt) {
      Objects.requireNonNull(salt, "salt must not be null");
      if (salt.length != 0) {
        salts.add(new SecretKeySpec(salt.clone(), "Generic"));
      }
      return this;
    }

    public Extract extractOnly() {
      return new Extract(ikms, salts);
    }

    public ExtractThenExpand thenExpand(byte[] info, int length) {
      return new ExtractThenExpand(ikms, salts, info, length);
    }
  }

  final class Extract implements HKDFParameterSpec {
    private final List<SecretKey> ikms;
    private final List<SecretKey> salts;

    private Extract(List<SecretKey> ikms, List<SecretKey> salts) {
      this.ikms = Collections.unmodifiableList(new ArrayList<SecretKey>(ikms));
      this.salts = Collections.unmodifiableList(new ArrayList<SecretKey>(salts));
    }

    public List<SecretKey> ikms() {
      return ikms;
    }

    public List<SecretKey> salts() {
      return salts;
    }
  }

  final class Expand implements HKDFParameterSpec {
    private final SecretKey prk;
    private final byte[] info;
    private final int length;

    private Expand(SecretKey prk, byte[] info, int length) {
      this.prk = prk;
      this.info = info == null ? new byte[0] : info.clone();
      if (length < 0) {
        throw new IllegalArgumentException("length must be non-negative");
      }
      this.length = length;
    }

    public SecretKey prk() {
      return prk;
    }

    public byte[] info() {
      return info.clone();
    }

    public int length() {
      return length;
    }
  }

  final class ExtractThenExpand implements HKDFParameterSpec {
    private final List<SecretKey> ikms;
    private final List<SecretKey> salts;
    private final byte[] info;
    private final int length;

    private ExtractThenExpand(List<SecretKey> ikms, List<SecretKey> salts, byte[] info, int length) {
      this.ikms = Collections.unmodifiableList(new ArrayList<SecretKey>(ikms));
      this.salts = Collections.unmodifiableList(new ArrayList<SecretKey>(salts));
      this.info = info == null ? new byte[0] : info.clone();
      if (length < 0) {
        throw new IllegalArgumentException("length must be non-negative");
      }
      this.length = length;
    }

    public List<SecretKey> ikms() {
      return ikms;
    }

    public List<SecretKey> salts() {
      return salts;
    }

    public byte[] info() {
      return info.clone();
    }

    public int length() {
      return length;
    }
  }
}
