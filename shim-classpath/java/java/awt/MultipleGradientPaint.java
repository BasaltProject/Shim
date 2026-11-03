/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt;

import java.awt.geom.AffineTransform;
import java.util.Arrays;

public abstract class MultipleGradientPaint implements Paint {
  public enum CycleMethod {
    NO_CYCLE, REFLECT, REPEAT;
  }

  public enum ColorSpaceType {
    SRGB, LINEAR_RGB;
  }

  final float[] fractions;
  final Color[] colors;
  final AffineTransform gradientTransform;
  final CycleMethod cycleMethod;
  final ColorSpaceType colorSpace;
  private final int transparency;

  MultipleGradientPaint(float[] fractions, Color[] colors, CycleMethod cycleMethod, ColorSpaceType colorSpace,
      AffineTransform gradientTransform) {
    if (fractions == null) {
      throw new NullPointerException("Fractions array cannot be null");
    }
    if (colors == null) {
      throw new NullPointerException("Colors array cannot be null");
    }
    if (cycleMethod == null) {
      throw new NullPointerException("Cycle method cannot be null");
    }
    if (colorSpace == null) {
      throw new NullPointerException("Color space cannot be null");
    }
    if (gradientTransform == null) {
      throw new NullPointerException("Gradient transform cannot be null");
    }
    if (fractions.length != colors.length) {
      throw new IllegalArgumentException("Colors and fractions must have equal size");
    }
    if (colors.length < 2) {
      throw new IllegalArgumentException("User must specify at least 2 colors");
    }
    float previous = -1.0f;
    for (float f : fractions) {
      if (f < 0.0f || f > 1.0f) {
        throw new IllegalArgumentException("Fraction values must be in the range 0 to 1: " + f);
      }
      if (f <= previous) {
        throw new IllegalArgumentException("Keyframe fractions must be increasing: " + f);
      }
      previous = f;
    }
    this.fractions = fractions.clone();
    this.colors = colors.clone();
    this.cycleMethod = cycleMethod;
    this.colorSpace = colorSpace;
    this.gradientTransform = new AffineTransform(gradientTransform);
    boolean opaque = true;
    for (Color c : colors) {
      opaque = opaque && c.getAlpha() == 0xff;
    }
    this.transparency = opaque ? OPAQUE : TRANSLUCENT;
  }

  public final float[] getFractions() { return Arrays.copyOf(fractions, fractions.length); }

  public final Color[] getColors() { return Arrays.copyOf(colors, colors.length); }

  public final CycleMethod getCycleMethod() { return cycleMethod; }

  public final ColorSpaceType getColorSpace() { return colorSpace; }

  public final AffineTransform getTransform() { return new AffineTransform(gradientTransform); }

  public final int getTransparency() { return transparency; }
}
