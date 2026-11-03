/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.serial;

import java.io.Serializable;
import java.sql.Ref;
import java.sql.SQLException;
import java.util.Map;

public class SerialRef implements Ref, Serializable, Cloneable {
  private static final long serialVersionUID = -4727123500609662274L;

  private Object object;
  private Ref reference;
  private String baseTypeName;

  public SerialRef(Ref ref) throws SerialException, SQLException {
    if (ref == null) {
      throw new SQLException("Cannot instantiate a SerialRef object with a null Ref object");
    }
    reference = ref;
    object = ref;
    if (ref.getBaseTypeName() == null) {
      throw new SQLException("Cannot instantiate a SerialRef object that returns a null base type name");
    } else {
      baseTypeName = ref.getBaseTypeName();
    }
  }

  public String getBaseTypeName() throws SerialException {
    return baseTypeName;
  }

  public Object getObject(Map<String, Class<?>> map) throws SerialException {
    map = new java.util.HashMap<String, Class<?>>(map);
    if (object != null) {
      return map.get(object);
    } else {
      throw new SerialException("The object is not set");
    }
  }

  public Object getObject() throws SerialException {
    if (reference != null) {
      try {
        return reference.getObject();
      } catch (SQLException e) {
        throw new SerialException("SQLException: " + e.getMessage());
      }
    }
    if (object != null) {
      return object;
    }
    throw new SerialException("The object is not set");
  }

  public void setObject(Object obj) throws SerialException {
    try {
      reference.setObject(obj);
    } catch (SQLException e) {
      throw new SerialException("SQLException: " + e.getMessage());
    }
    object = obj;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof SerialRef) {
      SerialRef ref = (SerialRef) obj;
      return baseTypeName.equals(ref.baseTypeName) && object.equals(ref.object);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (31 + object.hashCode()) * 31 + baseTypeName.hashCode();
  }

  @Override
  public Object clone() {
    try {
      SerialRef ref = (SerialRef) super.clone();
      ref.reference = null;
      return ref;
    } catch (CloneNotSupportedException ex) {
      throw new InternalError();
    }
  }
}
