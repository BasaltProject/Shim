/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class BinaryRelQueryExp extends QueryEval implements QueryExp {
  private static final long serialVersionUID = -5690656271650491000L;

  private int relOp;
  private ValueExp exp1;
  private ValueExp exp2;

  BinaryRelQueryExp(int op, ValueExp v1, ValueExp v2) {
    relOp = op;
    exp1 = v1;
    exp2 = v2;
  }

  @Override
  public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    Object val1 = exp1.apply(name);
    Object val2 = exp2.apply(name);
    String sval1;
    String sval2;
    double dval1;
    double dval2;
    long lval1;
    long lval2;
    boolean bval1;
    boolean bval2;
    boolean numeric = val1 instanceof NumericValueExp;
    boolean bool = val1 instanceof BooleanValueExp;
    if (numeric) {
      if (((NumericValueExp) val1).isInteger()) {
        lval1 = ((NumericValueExp) val1).longValue();
        lval2 = ((NumericValueExp) val2).longValue();
        switch (relOp) {
        case Query.GT:
          return lval1 > lval2;
        case Query.LT:
          return lval1 < lval2;
        case Query.GE:
          return lval1 >= lval2;
        case Query.LE:
          return lval1 <= lval2;
        case Query.EQ:
          return lval1 == lval2;
        default:
          break;
        }
      } else {
        dval1 = ((NumericValueExp) val1).doubleValue();
        dval2 = ((NumericValueExp) val2).doubleValue();
        switch (relOp) {
        case Query.GT:
          return dval1 > dval2;
        case Query.LT:
          return dval1 < dval2;
        case Query.GE:
          return dval1 >= dval2;
        case Query.LE:
          return dval1 <= dval2;
        case Query.EQ:
          return dval1 == dval2;
        default:
          break;
        }
      }
    } else if (bool) {
      bval1 = ((BooleanValueExp) val1).getValue();
      bval2 = ((BooleanValueExp) val2).getValue();
      switch (relOp) {
      case Query.GT:
        return bval1 && !bval2;
      case Query.LT:
        return !bval1 && bval2;
      case Query.GE:
        return bval1 || !bval2;
      case Query.LE:
        return !bval1 || bval2;
      case Query.EQ:
        return bval1 == bval2;
      default:
        break;
      }
    } else {
      sval1 = ((StringValueExp) val1).getValue();
      sval2 = ((StringValueExp) val2).getValue();
      switch (relOp) {
      case Query.GT:
        return sval1.compareTo(sval2) > 0;
      case Query.LT:
        return sval1.compareTo(sval2) < 0;
      case Query.GE:
        return sval1.compareTo(sval2) >= 0;
      case Query.LE:
        return sval1.compareTo(sval2) <= 0;
      case Query.EQ:
        return sval1.compareTo(sval2) == 0;
      default:
        break;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "(" + exp1 + ") " + relOpString() + " (" + exp2 + ")";
  }

  private String relOpString() {
    switch (relOp) {
    case Query.GT:
      return ">";
    case Query.LT:
      return "<";
    case Query.GE:
      return ">=";
    case Query.LE:
      return "<=";
    case Query.EQ:
      return "=";
    default:
      return "=";
    }
  }
}
