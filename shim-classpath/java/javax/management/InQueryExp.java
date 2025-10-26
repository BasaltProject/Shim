/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class InQueryExp extends QueryEval implements QueryExp {
  private static final long serialVersionUID = -5801329450358952434L;

  private ValueExp val;
  private ValueExp[] valueList;

  InQueryExp(ValueExp v1, ValueExp[] items) {
    val = v1;
    valueList = items;
  }

  @Override
  public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    if (valueList != null) {
      ValueExp v = val.apply(name);
      boolean numeric = v instanceof NumericValueExp;
      for (ValueExp element : valueList) {
        element = element.apply(name);
        if (numeric) {
          if (((NumericValueExp) element).doubleValue() == ((NumericValueExp) v).doubleValue()) {
            return true;
          }
        } else {
          if (((StringValueExp) element).getValue().equals(((StringValueExp) v).getValue())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return val + " in (" + joined() + ")";
  }

  private String joined() {
    StringBuilder b = new StringBuilder();
    if (valueList != null) {
      for (int i = 0; i < valueList.length; i++) {
        if (i > 0) {
          b.append(", ");
        }
        b.append(valueList[i]);
      }
    }
    return b.toString();
  }
}
