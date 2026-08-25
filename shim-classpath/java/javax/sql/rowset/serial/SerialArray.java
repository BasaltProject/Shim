/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.serial;

import java.io.Serializable;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Types;
import java.util.Arrays;
import java.util.Map;

public class SerialArray implements Array, Serializable, Cloneable {
  private static final long serialVersionUID = -8466174297270688520L;

  private Object[] elements;
  private int baseType;
  private String baseTypeName;
  private int len;

  public SerialArray(Array array, Map<String, Class<?>> map) throws SerialException, SQLException {
    if ((array == null) || (map == null)) {
      throw new SQLException("Cannot instantiate a SerialArray object with null parameters");
    }
    if ((elements = (Object[]) array.getArray()) == null) {
      throw new SQLException("Invalid Array object. Calls to Array.getArray() return null value which cannot be serialized");
    }
    elements = (Object[]) array.getArray(map);
    baseType = array.getBaseType();
    baseTypeName = array.getBaseTypeName();
    len = elements.length;
    switch (baseType) {
      case Types.STRUCT:
      case Types.ARRAY:
      case Types.BLOB:
      case Types.CLOB:
      case Types.DATALINK:
      case Types.JAVA_OBJECT:
        for (int i = 0; i < len; i++) {
          elements[i] = SerialStruct.toSerial(elements[i], map);
        }
        break;
      default:
        break;
    }
  }

  public SerialArray(Array array) throws SerialException, SQLException {
    if (array == null) {
      throw new SQLException("Cannot instantiate a SerialArray object with a null Array object");
    }
    if ((elements = (Object[]) array.getArray()) == null) {
      throw new SQLException("Invalid Array object. Calls to Array.getArray() return null value which cannot be serialized");
    }
    baseType = array.getBaseType();
    baseTypeName = array.getBaseTypeName();
    len = elements.length;
  }

  public void free() throws SQLException {
    if (elements != null) {
      elements = null;
      baseTypeName = null;
    }
  }

  public Object getArray() throws SerialException {
    isValid();
    Object dst = new Object[len];
    System.arraycopy((Object) elements, 0, dst, 0, len);
    return dst;
  }

  public Object getArray(Map<String, Class<?>> map) throws SerialException {
    isValid();
    Object dst[] = new Object[len];
    System.arraycopy((Object) elements, 0, dst, 0, len);
    return dst;
  }

  public Object getArray(long index, int count) throws SerialException {
    isValid();
    Object dst = new Object[count];
    System.arraycopy((Object) elements, (int) index, dst, 0, count);
    return dst;
  }

  public Object getArray(long index, int count, Map<String, Class<?>> map) throws SerialException {
    isValid();
    Object dst = new Object[count];
    System.arraycopy((Object) elements, (int) index, dst, 0, count);
    return dst;
  }

  public int getBaseType() throws SerialException {
    isValid();
    return baseType;
  }

  public String getBaseTypeName() throws SerialException {
    isValid();
    return baseTypeName;
  }

  public ResultSet getResultSet(long index, int count) throws SerialException {
    SerialException se = new SerialException();
    se.initCause(new java.sql.SQLFeatureNotSupportedException("Operation not supported"));
    throw se;
  }

  public ResultSet getResultSet(Map<String, Class<?>> map) throws SerialException {
    SerialException se = new SerialException();
    se.initCause(new java.sql.SQLFeatureNotSupportedException("Operation not supported"));
    throw se;
  }

  public ResultSet getResultSet() throws SerialException {
    SerialException se = new SerialException();
    se.initCause(new java.sql.SQLFeatureNotSupportedException("Operation not supported"));
    throw se;
  }

  public ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) throws SerialException {
    SerialException se = new SerialException();
    se.initCause(new java.sql.SQLFeatureNotSupportedException("Operation not supported"));
    throw se;
  }

  private void isValid() throws SerialException {
    if (elements == null) {
      throw new SerialException("Error: You cannot call a method on a SerialArray instance once free() has been called.");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof SerialArray) {
      SerialArray sa = (SerialArray) obj;
      return baseType == sa.baseType && baseTypeName.equals(sa.baseTypeName) && Arrays.equals(elements, sa.elements);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (((31 + Arrays.hashCode(elements)) * 31 + len) * 31 + baseType) * 31 + baseTypeName.hashCode();
  }

  @Override
  public Object clone() {
    try {
      SerialArray sa = (SerialArray) super.clone();
      sa.elements = (elements != null) ? Arrays.copyOf(elements, len) : null;
      return sa;
    } catch (CloneNotSupportedException ex) {
      throw new InternalError();
    }
  }
}
