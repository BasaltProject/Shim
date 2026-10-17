/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.crypto;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;
import sun.security.jca.GetInstance;

public class ExemptionMechanism {
  private Provider provider;
  private ExemptionMechanismSpi exmechSpi;
  private String mechanism;
  private boolean done = false;
  private boolean initialized = false;
  private Key keyStored = null;

  protected ExemptionMechanism(ExemptionMechanismSpi exmechSpi, Provider provider, String mechanism) {
    this.exmechSpi = exmechSpi;
    this.provider = provider;
    this.mechanism = mechanism;
  }

  public final String getName() {
    return this.mechanism;
  }

  public static final ExemptionMechanism getInstance(String algorithm) throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("ExemptionMechanism", ExemptionMechanismSpi.class,
        algorithm);
    return new ExemptionMechanism((ExemptionMechanismSpi) instance.impl, instance.provider, algorithm);
  }

  public static final ExemptionMechanism getInstance(String algorithm, String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("ExemptionMechanism", ExemptionMechanismSpi.class,
        algorithm, provider);
    return new ExemptionMechanism((ExemptionMechanismSpi) instance.impl, instance.provider, algorithm);
  }

  public static final ExemptionMechanism getInstance(String algorithm, Provider provider)
      throws NoSuchAlgorithmException {
    Objects.requireNonNull(algorithm, "null algorithm name");
    GetInstance.Instance instance = GetInstance.getInstance("ExemptionMechanism", ExemptionMechanismSpi.class,
        algorithm, provider);
    return new ExemptionMechanism((ExemptionMechanismSpi) instance.impl, instance.provider, algorithm);
  }

  public final Provider getProvider() {
    return this.provider;
  }

  public final boolean isCryptoAllowed(Key key) throws ExemptionMechanismException {
    boolean ret = false;
    if (done && (key != null)) {
      ret = keyStored.equals(key);
    }
    return ret;
  }

  public final int getOutputSize(int inputLen) throws IllegalStateException {
    if (!initialized) {
      throw new IllegalStateException("ExemptionMechanism not initialized");
    }
    if (inputLen < 0) {
      throw new IllegalArgumentException("Input size must be equal to or greater than zero");
    }
    return exmechSpi.engineGetOutputSize(inputLen);
  }

  public final void init(Key key) throws InvalidKeyException, ExemptionMechanismException {
    done = false;
    initialized = false;
    keyStored = key;
    exmechSpi.engineInit(key);
    initialized = true;
  }

  public final void init(Key key, AlgorithmParameterSpec params)
      throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException {
    done = false;
    initialized = false;
    keyStored = key;
    exmechSpi.engineInit(key, params);
    initialized = true;
  }

  public final void init(Key key, AlgorithmParameters params)
      throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException {
    done = false;
    initialized = false;
    keyStored = key;
    exmechSpi.engineInit(key, params);
    initialized = true;
  }

  public final byte[] genExemptionBlob() throws IllegalStateException, ExemptionMechanismException {
    if (!initialized) {
      throw new IllegalStateException("ExemptionMechanism not initialized");
    }
    byte[] blob = exmechSpi.engineGenExemptionBlob();
    done = true;
    return blob;
  }

  public final int genExemptionBlob(byte[] output)
      throws IllegalStateException, ShortBufferException, ExemptionMechanismException {
    return genExemptionBlob(output, 0);
  }

  public final int genExemptionBlob(byte[] output, int outputOffset)
      throws IllegalStateException, ShortBufferException, ExemptionMechanismException {
    if (!initialized) {
      throw new IllegalStateException("ExemptionMechanism not initialized");
    }
    int blobSize = exmechSpi.engineGenExemptionBlob(output, outputOffset);
    done = true;
    return blobSize;
  }
}
