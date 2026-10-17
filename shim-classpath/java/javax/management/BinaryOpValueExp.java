/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

class BinaryOpValueExp extends QueryEval implements ValueExp {
  private static final long serialVersionUID = 1216286847881456786L;

  private int op;
  private ValueExp exp1;
  private ValueExp exp2;

  BinaryOpValueExp(int op, ValueExp exp1, ValueExp exp2) {
    this.op = op;
    this.exp1 = exp1;
    this.exp2 = exp2;
  }

  @Override
  public ValueExp apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
      BadAttributeValueExpException, InvalidApplicationException {
    ValueExp val1 = exp1.apply(name);
    ValueExp val2 = exp2.apply(name);
    String sval1;
    String sval2;
    NumericValueExp nval1;
    NumericValueExp nval2;
    if (val1 instanceof NumericValueExp) {
      if (((NumericValueExp) val1).isInteger()) {
        long l1 = ((NumericValueExp) val1).longValue();
        long l2 = ((NumericValueExp) val2).longValue();
        switch (op) {
        case Query.PLUS:
          return new NumericValueExp(l1 + l2);
        case Query.TIMES:
          return new NumericValueExp(l1 * l2);
        case Query.MINUS:
          return new NumericValueExp(l1 - l2);
        case Query.DIV:
          return new NumericValueExp(l1 / l2);
        default:
          break;
        }
      } else {
        double d1 = ((NumericValueExp) val1).doubleValue();
        double d2 = ((NumericValueExp) val2).doubleValue();
        switch (op) {
        case Query.PLUS:
          return new NumericValueExp(d1 + d2);
        case Query.TIMES:
          return new NumericValueExp(d1 * d2);
        case Query.MINUS:
          return new NumericValueExp(d1 - d2);
        case Query.DIV:
          return new NumericValueExp(d1 / d2);
        default:
          break;
        }
      }
      throw new BadBinaryOpValueExpException(this);
    } else {
      sval1 = ((StringValueExp) val1).getValue();
      sval2 = ((StringValueExp) val2).getValue();
      if (op == Query.PLUS) {
        return new StringValueExp(sval1 + sval2);
      }
      throw new BadStringOperationException(opString());
    }
  }

  @Override
  public String toString() {
    try {
      return parens(exp1, null) + " " + opString() + " " + parens(exp2, null);
    } catch (BadBinaryOpValueExpException ex) {
      return "invalid expression";
    }
  }

  private String parens(ValueExp exp, Object unused) throws BadBinaryOpValueExpException {
    boolean needParens = exp instanceof BinaryOpValueExp;
    String s = exp.toString();
    return needParens ? "(" + s + ")" : s;
  }

  private String opString() throws BadBinaryOpValueExpException {
    switch (op) {
    case Query.PLUS:
      return "+";
    case Query.TIMES:
      return "*";
    case Query.MINUS:
      return "-";
    case Query.DIV:
      return "/";
    default:
      throw new BadBinaryOpValueExpException(this);
    }
  }

  @Override
  @Deprecated
  public void setMBeanServer(MBeanServer s) {
    super.setMBeanServer(s);
  }
}
