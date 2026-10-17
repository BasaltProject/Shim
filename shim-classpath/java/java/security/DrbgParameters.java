/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.util.Objects;

public class DrbgParameters {
  private DrbgParameters() {
  }

  public enum Capability {
    PR_AND_RESEED, RESEED_ONLY, NONE;

    @Override
    public String toString() {
      return name().toLowerCase(java.util.Locale.ROOT);
    }

    public boolean supportsReseeding() {
      return this != NONE;
    }

    public boolean supportsPredictionResistance() {
      return this == PR_AND_RESEED;
    }
  }

  public static final class Instantiation implements SecureRandomParameters {
    private final int strength;
    private final Capability capability;
    private final byte[] personalizationString;

    Instantiation(int strength, Capability capability, byte[] personalizationString) {
      if (strength < -1) {
        throw new IllegalArgumentException("Illegal security strength: " + strength);
      }
      this.strength = strength;
      this.capability = capability;
      this.personalizationString = personalizationString == null ? null : personalizationString.clone();
    }

    public byte[] getPersonalizationString() {
      return personalizationString == null ? null : personalizationString.clone();
    }

    public int getStrength() {
      return strength;
    }

    public Capability getCapability() {
      return capability;
    }

    @Override
    public String toString() {
      return strength + "," + capability + "," + java.util.Arrays.toString(personalizationString);
    }
  }

  public static final class NextBytes implements SecureRandomParameters {
    private final int strength;
    private final boolean predictionResistance;
    private final byte[] additionalInput;

    NextBytes(int strength, boolean predictionResistance, byte[] additionalInput) {
      if (strength < -1) {
        throw new IllegalArgumentException("Illegal security strength: " + strength);
      }
      this.strength = strength;
      this.predictionResistance = predictionResistance;
      this.additionalInput = additionalInput == null ? null : additionalInput.clone();
    }

    public int getStrength() {
      return strength;
    }

    public boolean getPredictionResistance() {
      return predictionResistance;
    }

    public byte[] getAdditionalInput() {
      return additionalInput == null ? null : additionalInput.clone();
    }
  }

  public static final class Reseed implements SecureRandomParameters {
    private final byte[] additionalInput;
    private final boolean predictionResistance;

    Reseed(boolean predictionResistance, byte[] additionalInput) {
      this.predictionResistance = predictionResistance;
      this.additionalInput = additionalInput == null ? null : additionalInput.clone();
    }

    public byte[] getAdditionalInput() {
      return additionalInput == null ? null : additionalInput.clone();
    }

    public boolean getPredictionResistance() {
      return predictionResistance;
    }
  }

  public static Instantiation instantiation(int strength, Capability capability, byte[] personalizationString) {
    return new Instantiation(strength, Objects.requireNonNull(capability), personalizationString);
  }

  public static NextBytes nextBytes(int strength, boolean predictionResistance, byte[] additionalInput) {
    return new NextBytes(strength, predictionResistance, additionalInput);
  }

  public static Reseed reseed(boolean predictionResistance, byte[] additionalInput) {
    return new Reseed(predictionResistance, additionalInput);
  }
}
