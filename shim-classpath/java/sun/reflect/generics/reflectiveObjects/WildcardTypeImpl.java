/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.reflect.generics.reflectiveObjects;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

public final class WildcardTypeImpl implements WildcardType {
  private final Type[] upperBounds;
  private final Type[] lowerBounds;

  public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
    this.upperBounds = upperBounds;
    this.lowerBounds = lowerBounds;
  }

  @Override
  public Type[] getUpperBounds() {
    return upperBounds.clone();
  }

  @Override
  public Type[] getLowerBounds() {
    return lowerBounds.clone();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof WildcardType) {
      WildcardType that = (WildcardType) o;
      return Arrays.equals(this.getLowerBounds(), that.getLowerBounds())
          && Arrays.equals(this.getUpperBounds(), that.getUpperBounds());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
  }

  @Override
  public String toString() {
    Type[] lower = getLowerBounds();
    Type[] bounds = lower;
    StringBuilder sb = new StringBuilder();
    if (lower.length > 0) {
      sb.append("? super ");
    } else {
      Type[] upper = getUpperBounds();
      if (upper.length > 0 && !Object.class.equals(upper[0])) {
        bounds = upper;
        sb.append("? extends ");
      } else {
        return "?";
      }
    }
    for (int i = 0; i < bounds.length; i++) {
      if (i > 0) {
        sb.append(" & ");
      }
      sb.append(ParameterizedTypeImpl.typeName(bounds[i]));
    }
    return sb.toString();
  }
}
