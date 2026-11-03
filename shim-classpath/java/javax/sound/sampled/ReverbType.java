/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sound.sampled;

public class ReverbType {
  private final String name;
  private final int earlyReflectionDelay;
  private final float earlyReflectionIntensity;
  private final int lateReflectionDelay;
  private final float lateReflectionIntensity;
  private final int decayTime;

  protected ReverbType(String name, int earlyReflectionDelay, float earlyReflectionIntensity,
      int lateReflectionDelay, float lateReflectionIntensity, int decayTime) {
    this.name = name;
    this.earlyReflectionDelay = earlyReflectionDelay;
    this.earlyReflectionIntensity = earlyReflectionIntensity;
    this.lateReflectionDelay = lateReflectionDelay;
    this.lateReflectionIntensity = lateReflectionIntensity;
    this.decayTime = decayTime;
  }

  public String getName() { return name; }

  public final int getEarlyReflectionDelay() { return earlyReflectionDelay; }

  public final float getEarlyReflectionIntensity() { return earlyReflectionIntensity; }

  public final int getLateReflectionDelay() { return lateReflectionDelay; }

  public final float getLateReflectionIntensity() { return lateReflectionIntensity; }

  public final int getDecayTime() { return decayTime; }

  public final boolean equals(Object obj) { return super.equals(obj); }

  public final int hashCode() { return super.hashCode(); }

  public final String toString() {
    return (name + ", early reflection delay " + earlyReflectionDelay + " ns, early reflection intensity "
        + earlyReflectionIntensity + " dB, late deflection delay " + lateReflectionDelay
        + " ns, late reflection intensity " + lateReflectionIntensity + " dB, decay time " + decayTime);
  }
}
