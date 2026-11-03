/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.awt.color;

public abstract class ColorSpace implements java.io.Serializable {
  public static final int TYPE_XYZ = 0;
  public static final int TYPE_Lab = 1;
  public static final int TYPE_Luv = 2;
  public static final int TYPE_YCbCr = 3;
  public static final int TYPE_Yxy = 4;
  public static final int TYPE_RGB = 5;
  public static final int TYPE_GRAY = 6;
  public static final int TYPE_HSV = 7;
  public static final int TYPE_HLS = 8;
  public static final int TYPE_CMYK = 9;
  public static final int TYPE_CMY = 11;
  public static final int TYPE_2CLR = 12;
  public static final int TYPE_3CLR = 13;
  public static final int TYPE_4CLR = 14;
  public static final int TYPE_5CLR = 15;
  public static final int TYPE_6CLR = 16;
  public static final int TYPE_7CLR = 17;
  public static final int TYPE_8CLR = 18;
  public static final int TYPE_9CLR = 19;
  public static final int TYPE_ACLR = 20;
  public static final int TYPE_BCLR = 21;
  public static final int TYPE_CCLR = 22;
  public static final int TYPE_DCLR = 23;
  public static final int TYPE_ECLR = 24;
  public static final int TYPE_FCLR = 25;
  public static final int CS_sRGB = 1000;
  public static final int CS_LINEAR_RGB = 1004;
  public static final int CS_CIEXYZ = 1001;
  public static final int CS_PYCC = 1002;
  public static final int CS_GRAY = 1003;

  private int type;
  private int numComponents;
  private transient String[] compName;

  private static ColorSpace sRGBspace;
  private static ColorSpace linearRGBspace;
  private static ColorSpace xyzSpace;
  private static ColorSpace pyccSpace;
  private static ColorSpace grayspace;

  protected ColorSpace(int type, int numComponents) {
    this.type = type;
    this.numComponents = numComponents;
  }

  public static ColorSpace getInstance(int colorspace) {
    switch (colorspace) {
    case CS_sRGB:
      synchronized (ColorSpace.class) {
        if (sRGBspace == null) {
          sRGBspace = new ICC_ColorSpace(ICC_Profile.getInstance(CS_sRGB));
        }
        return sRGBspace;
      }
    case CS_LINEAR_RGB:
      synchronized (ColorSpace.class) {
        if (linearRGBspace == null) {
          linearRGBspace = new ICC_ColorSpace(ICC_Profile.getInstance(CS_LINEAR_RGB));
        }
        return linearRGBspace;
      }
    case CS_CIEXYZ:
      synchronized (ColorSpace.class) {
        if (xyzSpace == null) {
          xyzSpace = new ICC_ColorSpace(ICC_Profile.getInstance(CS_CIEXYZ));
        }
        return xyzSpace;
      }
    case CS_PYCC:
      synchronized (ColorSpace.class) {
        if (pyccSpace == null) {
          pyccSpace = new ICC_ColorSpace(ICC_Profile.getInstance(CS_PYCC));
        }
        return pyccSpace;
      }
    case CS_GRAY:
      synchronized (ColorSpace.class) {
        if (grayspace == null) {
          grayspace = new ICC_ColorSpace(ICC_Profile.getInstance(CS_GRAY));
        }
        return grayspace;
      }
    default:
      throw new IllegalArgumentException("Unknown color space");
    }
  }

  public boolean isCS_sRGB() {
    return this == sRGBspace;
  }

  public abstract float[] toRGB(float[] colorvalue);

  public abstract float[] fromRGB(float[] rgbvalue);

  public abstract float[] toCIEXYZ(float[] colorvalue);

  public abstract float[] fromCIEXYZ(float[] colorvalue);

  public int getType() {
    return type;
  }

  public int getNumComponents() {
    return numComponents;
  }

  public String getName(int idx) {
    if (idx < 0 || idx > numComponents - 1) {
      throw new IllegalArgumentException("Component index out of range: " + idx);
    }
    if (compName == null) {
      switch (type) {
      case TYPE_XYZ:
        compName = new String[] {"X", "Y", "Z"};
        break;
      case TYPE_Lab:
        compName = new String[] {"L", "a", "b"};
        break;
      case TYPE_Luv:
        compName = new String[] {"L", "u", "v"};
        break;
      case TYPE_YCbCr:
        compName = new String[] {"Y", "Cb", "Cr"};
        break;
      case TYPE_Yxy:
        compName = new String[] {"Y", "x", "y"};
        break;
      case TYPE_RGB:
        compName = new String[] {"Red", "Green", "Blue"};
        break;
      case TYPE_GRAY:
        compName = new String[] {"Gray"};
        break;
      case TYPE_HSV:
        compName = new String[] {"Hue", "Saturation", "Value"};
        break;
      case TYPE_HLS:
        compName = new String[] {"Hue", "Lightness", "Saturation"};
        break;
      case TYPE_CMYK:
        compName = new String[] {"Cyan", "Magenta", "Yellow", "Black"};
        break;
      case TYPE_CMY:
        compName = new String[] {"Cyan", "Magenta", "Yellow"};
        break;
      default:
        String[] tmp = new String[numComponents];
        for (int i = 0; i < tmp.length; i++) {
          tmp[i] = Integer.toString(i);
        }
        compName = tmp;
        break;
      }
    }
    return compName[idx];
  }

  public float getMinValue(int component) {
    if (component < 0 || component > numComponents - 1) {
      throw new IllegalArgumentException("Component index out of range: " + component);
    }
    return 0.0f;
  }

  public float getMaxValue(int component) {
    if (component < 0 || component > numComponents - 1) {
      throw new IllegalArgumentException("Component index out of range: " + component);
    }
    return 1.0f;
  }
}
