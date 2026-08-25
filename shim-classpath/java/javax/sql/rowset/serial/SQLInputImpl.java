/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.serial;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;

public class SQLInputImpl implements SQLInput {
  private boolean lastValueWasNull;
  private int idx;
  private Object attrib[];
  private Map<String, Class<?>> udtMap;

  public SQLInputImpl(Object[] attributes, Map<String, Class<?>> map) throws SQLException {
    if ((attributes == null) || (map == null)) {
      throw new SQLException("Cannot instantiate a SQLInputImpl object with null parameters");
    }
    attrib = Arrays.copyOf(attributes, attributes.length);
    idx = -1;
    udtMap = map;
  }

  private Object getNextAttribute() throws SQLException {
    if (++idx >= attrib.length) {
      throw new SQLException("SQLInputImpl exception: Invalid read position");
    } else {
      lastValueWasNull = attrib[idx] == null;
      return attrib[idx];
    }
  }

  public String readString() throws SQLException {
    return (String) getNextAttribute();
  }

  public boolean readBoolean() throws SQLException {
    Boolean attrib = (Boolean) getNextAttribute();
    return (attrib == null) ? false : attrib.booleanValue();
  }

  public byte readByte() throws SQLException {
    Byte attrib = (Byte) getNextAttribute();
    return (attrib == null) ? 0 : attrib.byteValue();
  }

  public short readShort() throws SQLException {
    Short attrib = (Short) getNextAttribute();
    return (attrib == null) ? 0 : attrib.shortValue();
  }

  public int readInt() throws SQLException {
    Integer attrib = (Integer) getNextAttribute();
    return (attrib == null) ? 0 : attrib.intValue();
  }

  public long readLong() throws SQLException {
    Long attrib = (Long) getNextAttribute();
    return (attrib == null) ? 0 : attrib.longValue();
  }

  public float readFloat() throws SQLException {
    Float attrib = (Float) getNextAttribute();
    return (attrib == null) ? 0 : attrib.floatValue();
  }

  public double readDouble() throws SQLException {
    Double attrib = (Double) getNextAttribute();
    return (attrib == null) ? 0 : attrib.doubleValue();
  }

  public BigDecimal readBigDecimal() throws SQLException {
    return (BigDecimal) getNextAttribute();
  }

  public byte[] readBytes() throws SQLException {
    return (byte[]) getNextAttribute();
  }

  public Date readDate() throws SQLException {
    return (Date) getNextAttribute();
  }

  public Time readTime() throws SQLException {
    return (Time) getNextAttribute();
  }

  public Timestamp readTimestamp() throws SQLException {
    return (Timestamp) getNextAttribute();
  }

  public Reader readCharacterStream() throws SQLException {
    return (Reader) getNextAttribute();
  }

  public InputStream readAsciiStream() throws SQLException {
    return (InputStream) getNextAttribute();
  }

  public InputStream readBinaryStream() throws SQLException {
    return (InputStream) getNextAttribute();
  }

  public Object readObject() throws SQLException {
    Object attrib = getNextAttribute();
    if (attrib instanceof Struct) {
      Struct s = (Struct) attrib;
      Class<?> c = udtMap.get(s.getSQLTypeName());
      if (c != null) {
        SQLData obj = null;
        try {
          obj = (SQLData) c.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
          throw new SQLException("Unable to Instantiate: ", ex);
        }
        Object[] attribs = s.getAttributes(udtMap);
        SQLInputImpl sqlInput = new SQLInputImpl(attribs, udtMap);
        obj.readSQL(sqlInput, s.getSQLTypeName());
        return obj;
      }
    }
    return attrib;
  }

  public Ref readRef() throws SQLException {
    return (Ref) getNextAttribute();
  }

  public Blob readBlob() throws SQLException {
    return (Blob) getNextAttribute();
  }

  public Clob readClob() throws SQLException {
    return (Clob) getNextAttribute();
  }

  public Array readArray() throws SQLException {
    return (Array) getNextAttribute();
  }

  public boolean wasNull() throws SQLException {
    return lastValueWasNull;
  }

  public URL readURL() throws SQLException {
    return (URL) getNextAttribute();
  }

  public NClob readNClob() throws SQLException {
    return (NClob) getNextAttribute();
  }

  public String readNString() throws SQLException {
    return (String) getNextAttribute();
  }

  public SQLXML readSQLXML() throws SQLException {
    return (SQLXML) getNextAttribute();
  }

  public RowId readRowId() throws SQLException {
    return (RowId) getNextAttribute();
  }
}
