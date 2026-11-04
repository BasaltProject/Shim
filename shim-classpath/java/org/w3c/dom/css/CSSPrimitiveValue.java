/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package org.w3c.dom.css;

public interface CSSPrimitiveValue extends org.w3c.dom.css.CSSValue {
  short CSS_UNKNOWN = (short) 0;
  short CSS_NUMBER = (short) 1;
  short CSS_PERCENTAGE = (short) 2;
  short CSS_EMS = (short) 3;
  short CSS_EXS = (short) 4;
  short CSS_PX = (short) 5;
  short CSS_CM = (short) 6;
  short CSS_MM = (short) 7;
  short CSS_IN = (short) 8;
  short CSS_PT = (short) 9;
  short CSS_PC = (short) 10;
  short CSS_DEG = (short) 11;
  short CSS_RAD = (short) 12;
  short CSS_GRAD = (short) 13;
  short CSS_MS = (short) 14;
  short CSS_S = (short) 15;
  short CSS_HZ = (short) 16;
  short CSS_KHZ = (short) 17;
  short CSS_DIMENSION = (short) 18;
  short CSS_STRING = (short) 19;
  short CSS_URI = (short) 20;
  short CSS_IDENT = (short) 21;
  short CSS_ATTR = (short) 22;
  short CSS_COUNTER = (short) 23;
  short CSS_RECT = (short) 24;
  short CSS_RGBCOLOR = (short) 25;

  org.w3c.dom.css.Counter getCounterValue() throws org.w3c.dom.DOMException;
  float getFloatValue(short s) throws org.w3c.dom.DOMException;
  short getPrimitiveType();
  org.w3c.dom.css.RGBColor getRGBColorValue() throws org.w3c.dom.DOMException;
  org.w3c.dom.css.Rect getRectValue() throws org.w3c.dom.DOMException;
  java.lang.String getStringValue() throws org.w3c.dom.DOMException;
  void setFloatValue(short s, float f) throws org.w3c.dom.DOMException;
  void setStringValue(short s, java.lang.String string) throws org.w3c.dom.DOMException;
}
