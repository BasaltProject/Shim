/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package com.sun.jdi.connect;

public interface Connector {
  java.util.Map<java.lang.String, com.sun.jdi.connect.Connector.Argument> defaultArguments();
  java.lang.String description();
  java.lang.String name();
  com.sun.jdi.connect.Transport transport();

  public interface Argument extends java.io.Serializable {
    java.lang.String description();
    boolean isValid(java.lang.String string);
    java.lang.String label();
    boolean mustSpecify();
    java.lang.String name();
    void setValue(java.lang.String string);
    java.lang.String value();
  }

  public interface BooleanArgument extends com.sun.jdi.connect.Connector.Argument {
    boolean booleanValue();
    boolean isValid(java.lang.String string);
    void setValue(boolean flag);
    java.lang.String stringValueOf(boolean flag);
  }

  public interface IntegerArgument extends com.sun.jdi.connect.Connector.Argument {
    int intValue();
    boolean isValid(java.lang.String string);
    boolean isValid(int i);
    int max();
    int min();
    void setValue(int i);
    java.lang.String stringValueOf(int i);
  }

  public interface SelectedArgument extends com.sun.jdi.connect.Connector.Argument {
    java.util.List<java.lang.String> choices();
    boolean isValid(java.lang.String string);
  }

  public interface StringArgument extends com.sun.jdi.connect.Connector.Argument {
    boolean isValid(java.lang.String string);
  }
}
