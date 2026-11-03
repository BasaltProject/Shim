/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.serial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.sql.SQLOutput;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Vector;

public class SQLOutputImpl implements SQLOutput {
  @SuppressWarnings("rawtypes")
  private Vector attribs;
  @SuppressWarnings("rawtypes")
  private Map map;

  @SuppressWarnings("rawtypes")
  public SQLOutputImpl(Vector<?> attributes, Map<String, ?> map) throws SQLException {
    if ((attributes == null) || (map == null)) {
      throw new SQLException("Cannot instantiate a SQLOutputImpl instance with null parameters");
    }
    this.attribs = attributes;
    this.map = map;
  }

  @SuppressWarnings("unchecked")
  private void add(Object o) {
    attribs.add(o);
  }

  public void writeString(String x) throws SQLException {
    add(x);
  }

  public void writeBoolean(boolean x) throws SQLException {
    add(Boolean.valueOf(x));
  }

  public void writeByte(byte x) throws SQLException {
    add(Byte.valueOf(x));
  }

  public void writeShort(short x) throws SQLException {
    add(Short.valueOf(x));
  }

  public void writeInt(int x) throws SQLException {
    add(Integer.valueOf(x));
  }

  public void writeLong(long x) throws SQLException {
    add(Long.valueOf(x));
  }

  public void writeFloat(float x) throws SQLException {
    add(Float.valueOf(x));
  }

  public void writeDouble(double x) throws SQLException {
    add(Double.valueOf(x));
  }

  public void writeBigDecimal(BigDecimal x) throws SQLException {
    add(x);
  }

  public void writeBytes(byte[] x) throws SQLException {
    add(x);
  }

  public void writeDate(Date x) throws SQLException {
    add(x);
  }

  public void writeTime(Time x) throws SQLException {
    add(x);
  }

  public void writeTimestamp(Timestamp x) throws SQLException {
    add(x);
  }

  private void writeLines(Reader in) throws SQLException {
    BufferedReader bufReader = new BufferedReader(in);
    try {
      int i;
      while ((i = bufReader.read()) != -1) {
        char ch = (char) i;
        StringBuilder strBuf = new StringBuilder();
        strBuf.append(ch);
        String str = new String(strBuf);
        String strLine = bufReader.readLine();
        if (strLine != null) {
          str = str.concat(strLine);
        }
        add(str);
      }
    } catch (IOException ioe) {
      throw new SQLException(ioe.getMessage());
    }
  }

  public void writeCharacterStream(Reader x) throws SQLException {
    writeLines(x);
  }

  public void writeAsciiStream(InputStream x) throws SQLException {
    try {
      writeLines(new InputStreamReader(x, "ASCII"));
    } catch (IOException ioe) {
      throw new SQLException(ioe.getMessage());
    }
  }

  public void writeBinaryStream(InputStream x) throws SQLException {
    writeLines(new InputStreamReader(x));
  }

  @SuppressWarnings("unchecked")
  public void writeObject(SQLData x) throws SQLException {
    if (x == null) {
      add(null);
    } else {
      add(new javax.sql.rowset.serial.SerialStruct(x, map));
    }
  }

  @SuppressWarnings("unchecked")
  public void writeRef(Ref x) throws SQLException {
    add(x == null ? null : new javax.sql.rowset.serial.SerialRef(x));
  }

  public void writeBlob(Blob x) throws SQLException {
    add(x == null ? null : new javax.sql.rowset.serial.SerialBlob(x));
  }

  public void writeClob(Clob x) throws SQLException {
    add(x == null ? null : new javax.sql.rowset.serial.SerialClob(x));
  }

  @SuppressWarnings("unchecked")
  public void writeStruct(Struct x) throws SQLException {
    add(new javax.sql.rowset.serial.SerialStruct(x, map));
  }

  @SuppressWarnings("unchecked")
  public void writeArray(Array x) throws SQLException {
    add(x == null ? null : new javax.sql.rowset.serial.SerialArray(x, map));
  }

  public void writeURL(URL url) throws SQLException {
    add(url == null ? null : new javax.sql.rowset.serial.SerialDatalink(url));
  }

  public void writeNString(String x) throws SQLException {
    add(x);
  }

  public void writeNClob(NClob x) throws SQLException {
    add(x);
  }

  public void writeRowId(RowId x) throws SQLException {
    add(x);
  }

  public void writeSQLXML(SQLXML x) throws SQLException {
    add(x);
  }
}
