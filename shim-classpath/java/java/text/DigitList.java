/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

final class DigitList {
  private static final char[] kLongMinRep = "9223372036854775808".toCharArray();

  char[] digits = new char[24];
  int count;
  int decimalAt;
  private boolean exact = true;
  private boolean alreadyRounded;
  private RoundingMode roundingMode = RoundingMode.HALF_EVEN;
  private boolean negative;

  boolean isZero() {
    for (int i = 0; i < count; i++) {
      if (digits[i] != '0') {
        return false;
      }
    }
    return true;
  }

  void clear() {
    count = 0;
    decimalAt = 0;
  }

  void append(char c) {
    if (count == digits.length) {
      char[] grown = new char[count + 24];
      System.arraycopy(digits, 0, grown, 0, count);
      digits = grown;
    }
    digits[count++] = c;
  }

  void setRounding(RoundingMode mode, boolean isNegative) {
    roundingMode = mode;
    negative = isNegative;
  }

  private void load(BigDecimal value) {
    count = 0;
    decimalAt = 0;
    if (value.signum() == 0) {
      return;
    }
    BigDecimal stripped = value.stripTrailingZeros();
    String unscaled = stripped.unscaledValue().abs().toString();
    for (int i = 0; i < unscaled.length(); i++) {
      append(unscaled.charAt(i));
    }
    decimalAt = unscaled.length() - stripped.scale();
  }

  void setDouble(double abs, int maximumDigits, boolean fixedPoint) {
    alreadyRounded = false;
    exact = true;
    if (abs == 0) {
      clear();
      return;
    }
    BigDecimal shortest = new BigDecimal(Double.toString(abs));
    int cmp = shortest.compareTo(new BigDecimal(abs));
    exact = cmp == 0;
    alreadyRounded = cmp > 0;
    load(shortest);
    finish(maximumDigits, fixedPoint);
  }

  void setLong(long value, int maximumDigits) {
    alreadyRounded = false;
    exact = true;
    count = 0;
    decimalAt = 0;
    if (value == 0) {
      return;
    }
    String text = value == Long.MIN_VALUE ? new String(kLongMinRep) : Long.toString(Math.abs(value));
    for (int i = 0; i < text.length(); i++) {
      append(text.charAt(i));
    }
    decimalAt = text.length();
    while (count > 1 && digits[count - 1] == '0') {
      count--;
    }
    if (maximumDigits > 0) {
      round(maximumDigits);
    }
  }

  void setBigDecimal(BigDecimal abs, int maximumDigits, boolean fixedPoint) {
    alreadyRounded = false;
    exact = true;
    load(abs);
    if (count == 0) {
      return;
    }
    finish(maximumDigits, fixedPoint);
  }

  void setBigInteger(BigInteger abs, int maximumDigits) {
    alreadyRounded = false;
    exact = true;
    load(new BigDecimal(abs));
    if (count > 0 && maximumDigits > 0) {
      round(maximumDigits);
    }
  }

  private void finish(int maximumDigits, boolean fixedPoint) {
    if (fixedPoint) {
      if (-decimalAt > maximumDigits) {
        count = 0;
        return;
      } else if (-decimalAt == maximumDigits) {
        if (shouldRoundUp(0)) {
          count = 1;
          decimalAt++;
          digits[0] = '1';
        } else {
          count = 0;
        }
        return;
      }
    }
    while (count > 1 && digits[count - 1] == '0') {
      count--;
    }
    round(fixedPoint ? maximumDigits + decimalAt : (maximumDigits == 0 ? -1 : maximumDigits));
  }

  private void round(int maximumDigits) {
    if (maximumDigits >= 0 && maximumDigits < count) {
      if (shouldRoundUp(maximumDigits)) {
        for (;;) {
          --maximumDigits;
          if (maximumDigits < 0) {
            digits[0] = '1';
            ++decimalAt;
            maximumDigits = 0;
            break;
          }
          ++digits[maximumDigits];
          if (digits[maximumDigits] <= '9') {
            break;
          }
        }
        ++maximumDigits;
      }
      count = maximumDigits;
      while (count > 1 && digits[count - 1] == '0') {
        --count;
      }
    }
  }

  private boolean shouldRoundUp(int maximumDigits) {
    if (maximumDigits >= count) {
      return false;
    }
    switch (roundingMode) {
      case UP:
        for (int i = maximumDigits; i < count; ++i) {
          if (digits[i] != '0') {
            return true;
          }
        }
        break;
      case DOWN:
        break;
      case CEILING:
        for (int i = maximumDigits; i < count; ++i) {
          if (digits[i] != '0') {
            return !negative;
          }
        }
        break;
      case FLOOR:
        for (int i = maximumDigits; i < count; ++i) {
          if (digits[i] != '0') {
            return negative;
          }
        }
        break;
      case HALF_UP:
      case HALF_DOWN:
        if (digits[maximumDigits] > '5') {
          return true;
        } else if (digits[maximumDigits] == '5') {
          if (maximumDigits != (count - 1)) {
            return true;
          }
          return exact ? roundingMode == RoundingMode.HALF_UP : !alreadyRounded;
        }
        break;
      case HALF_EVEN:
        if (digits[maximumDigits] > '5') {
          return true;
        } else if (digits[maximumDigits] == '5') {
          if (maximumDigits == (count - 1)) {
            if (alreadyRounded) {
              return false;
            }
            if (!exact) {
              return true;
            }
            return maximumDigits > 0 && (digits[maximumDigits - 1] % 2 != 0);
          }
          for (int i = maximumDigits + 1; i < count; ++i) {
            if (digits[i] != '0') {
              return true;
            }
          }
        }
        break;
      case UNNECESSARY:
        for (int i = maximumDigits; i < count; ++i) {
          if (digits[i] != '0') {
            throw new ArithmeticException(
                "Rounding needed with the rounding mode being set to RoundingMode.UNNECESSARY");
          }
        }
        break;
      default:
        break;
    }
    return false;
  }

  boolean fitsIntoLong(boolean isPositive, boolean ignoreNegativeZero) {
    while (count > 0 && digits[count - 1] == '0') {
      --count;
    }
    if (count == 0) {
      return isPositive || ignoreNegativeZero;
    }
    if (decimalAt < count || decimalAt > 19) {
      return false;
    }
    if (decimalAt < 19) {
      return true;
    }
    for (int i = 0; i < count; ++i) {
      char dig = digits[i];
      char max = kLongMinRep[i];
      if (dig > max) {
        return false;
      }
      if (dig < max) {
        return true;
      }
    }
    if (count < decimalAt) {
      return true;
    }
    return !isPositive;
  }

  long getLong() {
    if (count == 0) {
      return 0;
    }
    if (isLongMin()) {
      return Long.MIN_VALUE;
    }
    StringBuilder sb = new StringBuilder(count);
    for (int i = 0; i < decimalAt; ++i) {
      sb.append(i < count ? digits[i] : '0');
    }
    return Long.parseLong(sb.toString());
  }

  private boolean isLongMin() {
    if (decimalAt != 19 || count != 19) {
      return false;
    }
    for (int i = 0; i < 19; i++) {
      if (digits[i] != kLongMinRep[i]) {
        return false;
      }
    }
    return true;
  }

  double getDouble() {
    if (count == 0) {
      return 0.0;
    }
    return Double.parseDouble("0." + new String(digits, 0, count) + "E" + decimalAt);
  }

  BigDecimal getBigDecimal() {
    if (count == 0) {
      return BigDecimal.ZERO;
    }
    return new BigDecimal(new BigInteger(new String(digits, 0, count)), count - decimalAt);
  }
}
