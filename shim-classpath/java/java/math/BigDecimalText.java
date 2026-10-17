/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.math;

final class BigDecimalText {
  private BigDecimalText() { }

  static String layout(BigDecimal d, boolean sci) {
    BigInteger unscaled = d.unscaledValue();
    int scale = d.scale();
    if (scale == 0) {
      return unscaled.toString();
    }
    String coeff = unscaled.abs().toString();
    StringBuilder buf = new StringBuilder();
    if (unscaled.signum() < 0) {
      buf.append('-');
    }
    int coeffLen = coeff.length();
    long adjusted = -(long) scale + (coeffLen - 1);
    if ((scale >= 0) && (adjusted >= -6)) {
      int pad = scale - coeffLen;
      if (pad >= 0) {
        buf.append('0');
        buf.append('.');
        for (; pad > 0; pad--) {
          buf.append('0');
        }
        buf.append(coeff);
      } else {
        buf.append(coeff, 0, -pad);
        buf.append('.');
        buf.append(coeff, -pad, scale - pad);
      }
    } else {
      if (sci) {
        buf.append(coeff.charAt(0));
        if (coeffLen > 1) {
          buf.append('.');
          buf.append(coeff, 1, coeffLen);
        }
        if (adjusted != 0) {
          buf.append('E');
          if (adjusted > 0) {
            buf.append('+');
          }
          buf.append(adjusted);
        }
      } else {
        int sig = (int) (adjusted % 3);
        if (sig < 0) {
          sig += 3;
        }
        adjusted -= sig;
        sig++;
        if (unscaled.signum() == 0) {
          switch (sig) {
            case 1:
              buf.append('0');
              break;
            case 2:
              buf.append("0.00");
              adjusted += 3;
              break;
            case 3:
              buf.append("0.0");
              adjusted += 3;
              break;
            default:
              throw new AssertionError("Unexpected sig value " + sig);
          }
        } else if (sig >= coeffLen) {
          buf.append(coeff, 0, coeffLen);
          for (int i = sig - coeffLen; i > 0; i--) {
            buf.append('0');
          }
        } else {
          buf.append(coeff, 0, sig);
          buf.append('.');
          buf.append(coeff, sig, coeffLen);
        }
        if (adjusted != 0) {
          buf.append('E');
          if (adjusted > 0) {
            buf.append('+');
          }
          buf.append(adjusted);
        }
      }
    }
    return buf.toString();
  }

  static String plain(BigDecimal d) {
    BigInteger unscaled = d.unscaledValue();
    int scale = d.scale();
    if (scale == 0) {
      return unscaled.toString();
    }
    if (scale < 0) {
      if (unscaled.signum() == 0) {
        return "0";
      }
      long trailing = -(long) scale;
      String str = unscaled.toString();
      StringBuilder buf = new StringBuilder(str.length() + (int) Math.min(trailing, Integer.MAX_VALUE));
      buf.append(str);
      for (long i = 0; i < trailing; i++) {
        buf.append('0');
      }
      return buf.toString();
    }
    String str = unscaled.abs().toString();
    int insertionPoint = str.length() - scale;
    if (insertionPoint == 0) {
      return (unscaled.signum() < 0 ? "-0." : "0.") + str;
    }
    StringBuilder buf;
    if (insertionPoint > 0) {
      buf = new StringBuilder(str);
      buf.insert(insertionPoint, '.');
      if (unscaled.signum() < 0) {
        buf.insert(0, '-');
      }
    } else {
      buf = new StringBuilder(3 - insertionPoint + str.length());
      buf.append(unscaled.signum() < 0 ? "-0." : "0.");
      for (int i = 0; i < -insertionPoint; i++) {
        buf.append('0');
      }
      buf.append(str);
    }
    return buf.toString();
  }
}
