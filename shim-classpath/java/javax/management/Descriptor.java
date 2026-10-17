/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management;

public interface Descriptor extends java.io.Serializable, Cloneable {
  Object getFieldValue(String p0) throws RuntimeOperationsException;

  void setField(String p0, Object p1) throws RuntimeOperationsException;

  String[] getFields();

  String[] getFieldNames();

  Object[] getFieldValues(String... p0);

  void removeField(String p0);

  void setFields(String[] p0, Object[] p1) throws RuntimeOperationsException;

  Object clone() throws RuntimeOperationsException;

  boolean isValid() throws RuntimeOperationsException;

  boolean equals(Object p0);

  int hashCode();
}
