/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.color;

public class ICC_ColorSpace extends ColorSpace {
  private static final double[] kToXyz = {
      0.4360747, 0.3850649, 0.1430804,
      0.2225045, 0.7168786, 0.0606169,
      0.0139322, 0.0971045, 0.7141733};
  private static final double[] kFromXyz = {
      3.1338561, -1.6168667, -0.4906146,
      -0.9787684, 1.9161415, 0.0334540,
      0.0719453, -0.2289914, 1.4052427};

  private ICC_Profile thisProfile;
  private float[] minVal;
  private float[] maxVal;
  private final int kind;

  public ICC_ColorSpace(ICC_Profile profile) {
    super(profile.getColorSpaceType(), profile.getNumComponents());
    int pclass = profile.getProfileClass();
    if (pclass != ICC_Profile.CLASS_INPUT && pclass != ICC_Profile.CLASS_DISPLAY && pclass != ICC_Profile.CLASS_OUTPUT
        && pclass != ICC_Profile.CLASS_COLORSPACECONVERSION && pclass != ICC_Profile.CLASS_ABSTRACT) {
      throw new IllegalArgumentException("Invalid profile type");
    }
    thisProfile = profile;
    kind = profile.kind;
    setMinMax();
  }

  public ICC_Profile getProfile() {
    return thisProfile;
  }

  private static double decode(double v) {
    return v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
  }

  private static double encode(double v) {
    return v <= 0.0031308 ? v * 12.92 : 1.055 * Math.pow(v, 1 / 2.4) - 0.055;
  }

  private static float clamp(double v) {
    return (float) (v < 0 ? 0 : v > 1 ? 1 : v);
  }

  public float[] toRGB(float[] colorvalue) {
    int n = getNumComponents();
    if (colorvalue.length < n) {
      throw new ArrayIndexOutOfBoundsException("Number of input components is " + n);
    }
    switch (kind) {
    case ICC_Profile.kSrgb:
      return new float[] {clamp(colorvalue[0]), clamp(colorvalue[1]), clamp(colorvalue[2])};
    case ICC_Profile.kLinearRgb:
      return new float[] {clamp(encode(colorvalue[0])), clamp(encode(colorvalue[1])), clamp(encode(colorvalue[2]))};
    case ICC_Profile.kGray: {
      float g = clamp(encode(colorvalue[0]));
      return new float[] {g, g, g};
    }
    case ICC_Profile.kXyz:
      return xyzToRgb(colorvalue);
    default:
      return pyccToRgb(colorvalue);
    }
  }

  private static float[] xyzToRgb(float[] xyz) {
    double r = kFromXyz[0] * xyz[0] + kFromXyz[1] * xyz[1] + kFromXyz[2] * xyz[2];
    double g = kFromXyz[3] * xyz[0] + kFromXyz[4] * xyz[1] + kFromXyz[5] * xyz[2];
    double b = kFromXyz[6] * xyz[0] + kFromXyz[7] * xyz[1] + kFromXyz[8] * xyz[2];
    return new float[] {clamp(encode(clamp(r))), clamp(encode(clamp(g))), clamp(encode(clamp(b)))};
  }

  private static float[] pyccToRgb(float[] c) {
    double y = c[0];
    double cb = c[1] - 0.5;
    double cr = c[2] - 0.5;
    return new float[] {clamp(y + 1.402 * cr), clamp(y - 0.344136 * cb - 0.714136 * cr), clamp(y + 1.772 * cb)};
  }

  public float[] fromRGB(float[] rgbvalue) {
    if (rgbvalue.length < 3) {
      throw new ArrayIndexOutOfBoundsException("Number of input components is 3");
    }
    switch (kind) {
    case ICC_Profile.kSrgb:
      return new float[] {clamp(rgbvalue[0]), clamp(rgbvalue[1]), clamp(rgbvalue[2])};
    case ICC_Profile.kLinearRgb:
      return new float[] {clamp(decode(rgbvalue[0])), clamp(decode(rgbvalue[1])), clamp(decode(rgbvalue[2]))};
    case ICC_Profile.kGray: {
      double lum = 0.2125 * decode(rgbvalue[0]) + 0.7154 * decode(rgbvalue[1]) + 0.0721 * decode(rgbvalue[2]);
      return new float[] {clamp(lum)};
    }
    case ICC_Profile.kXyz:
      return rgbToXyz(rgbvalue);
    default: {
      double r = rgbvalue[0];
      double g = rgbvalue[1];
      double b = rgbvalue[2];
      double y = 0.299 * r + 0.587 * g + 0.114 * b;
      return new float[] {clamp(y), clamp(0.5 + (b - y) / 1.772), clamp(0.5 + (r - y) / 1.402)};
    }
    }
  }

  private static float[] rgbToXyz(float[] rgb) {
    double r = decode(rgb[0]);
    double g = decode(rgb[1]);
    double b = decode(rgb[2]);
    return new float[] {(float) (kToXyz[0] * r + kToXyz[1] * g + kToXyz[2] * b),
        (float) (kToXyz[3] * r + kToXyz[4] * g + kToXyz[5] * b),
        (float) (kToXyz[6] * r + kToXyz[7] * g + kToXyz[8] * b)};
  }

  public float[] toCIEXYZ(float[] colorvalue) {
    if (kind == ICC_Profile.kXyz) {
      return new float[] {colorvalue[0], colorvalue[1], colorvalue[2]};
    }
    return rgbToXyz(toRGB(colorvalue));
  }

  public float[] fromCIEXYZ(float[] colorvalue) {
    if (kind == ICC_Profile.kXyz) {
      return new float[] {colorvalue[0], colorvalue[1], colorvalue[2]};
    }
    return fromRGB(xyzToRgb(colorvalue));
  }

  public float getMinValue(int component) {
    if (component < 0 || component > getNumComponents() - 1) {
      throw new IllegalArgumentException("Component index out of range: " + component);
    }
    return minVal[component];
  }

  public float getMaxValue(int component) {
    if (component < 0 || component > getNumComponents() - 1) {
      throw new IllegalArgumentException("Component index out of range: " + component);
    }
    return maxVal[component];
  }

  private void setMinMax() {
    int nc = getNumComponents();
    minVal = new float[nc];
    maxVal = new float[nc];
    int type = getType();
    for (int i = 0; i < nc; i++) {
      minVal[i] = 0.0f;
      maxVal[i] = 1.0f;
    }
    if (type == ICC_ColorSpace.TYPE_XYZ) {
      for (int i = 0; i < nc; i++) {
        maxVal[i] = 1.0f + 32767.0f / 32768.0f;
      }
    } else if (type == ICC_ColorSpace.TYPE_Lab) {
      minVal[0] = 0.0f;
      maxVal[0] = 100.0f;
      minVal[1] = -128.0f;
      maxVal[1] = 127.0f;
      minVal[2] = -128.0f;
      maxVal[2] = 127.0f;
    }
  }
}
