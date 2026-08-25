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
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

public class SerialStruct implements Struct, Serializable, Cloneable {
  private static final long serialVersionUID = -8322445504027483372L;

  private String SQLTypeName;
  private Object[] attribs;

  public SerialStruct(Struct in, Map<String, Class<?>> map) throws SerialException {
    try {
      SQLTypeName = in.getSQLTypeName();
      Object[] tmp = in.getAttributes(map);
      attribs = new Object[tmp.length];
      for (int i = 0; i < tmp.length; i++) {
        attribs[i] = toSerial(tmp[i], map);
      }
    } catch (SQLException e) {
      throw new SerialException(e.getMessage());
    }
  }

  public SerialStruct(SQLData in, Map<String, Class<?>> map) throws SerialException {
    try {
      SQLTypeName = in.getSQLTypeName();
      Vector<Object> tmp = new Vector<Object>();
      in.writeSQL(new SQLOutputImpl(tmp, map));
      attribs = tmp.toArray();
    } catch (SQLException e) {
      throw new SerialException(e.getMessage());
    }
  }

  public String getSQLTypeName() throws SerialException {
    return SQLTypeName;
  }

  public Object[] getAttributes() throws SerialException {
    Object[] val = this.attribs;
    return (val == null) ? null : Arrays.copyOf(val, val.length);
  }

  public Object[] getAttributes(Map<String, Class<?>> map) throws SerialException {
    Object[] val = this.attribs;
    return (val == null) ? null : Arrays.copyOf(val, val.length);
  }

  static Object toSerial(Object o, Map<String, Class<?>> map) throws SerialException, SQLException {
    if (o == null) {
      return null;
    }
    if (o instanceof Struct) {
      return new SerialStruct((Struct) o, map);
    } else if (o instanceof SQLData) {
      return new SerialStruct((SQLData) o, map);
    } else if (o instanceof Blob) {
      return new SerialBlob((Blob) o);
    } else if (o instanceof Clob) {
      return new SerialClob((Clob) o);
    } else if (o instanceof java.net.URL) {
      return new SerialDatalink((java.net.URL) o);
    } else if (o instanceof Array) {
      return new SerialArray((Array) o, map);
    } else if (o instanceof Ref) {
      return new SerialRef((Ref) o);
    } else if (!(o instanceof java.io.Serializable)) {
      return new SerialJavaObject(o);
    }
    return o;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof SerialStruct) {
      SerialStruct ss = (SerialStruct) obj;
      return SQLTypeName.equals(ss.SQLTypeName) && Arrays.equals(attribs, ss.attribs);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ((31 + Arrays.hashCode(attribs)) * 31 * 31) + SQLTypeName.hashCode();
  }

  @Override
  public Object clone() {
    try {
      SerialStruct ss = (SerialStruct) super.clone();
      ss.attribs = Arrays.copyOf(attribs, attribs.length);
      return ss;
    } catch (CloneNotSupportedException ex) {
      throw new InternalError();
    }
  }
}
