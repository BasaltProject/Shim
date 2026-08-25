/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.serial;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Arrays;

public class SerialClob implements Clob, Serializable, Cloneable {
  private static final long serialVersionUID = -1662519690087375313L;

  private char[] buf;
  private Clob clob;
  private long len;
  private long origLen;

  public SerialClob(char[] ch) throws SerialException, SQLException {
    len = ch.length;
    buf = new char[(int) len];
    for (int i = 0; i < len; i++) {
      buf[i] = ch[i];
    }
    origLen = len;
    clob = null;
  }

  public SerialClob(Clob clob) throws SerialException, SQLException {
    if (clob == null) {
      throw new SQLException("Cannot instantiate a SerialClob object with a null Clob object");
    }
    len = clob.length();
    this.clob = clob;
    buf = new char[(int) len];
    int read = 0;
    int offset = 0;
    try (Reader charStream = clob.getCharacterStream()) {
      if (charStream == null) {
        throw new SQLException("Invalid Clob object. Calls to getCharacterStream returned null which cannot be serialized.");
      }
      try (java.io.BufferedReader reader = new java.io.BufferedReader(charStream)) {
        do {
          read = reader.read(buf, offset, (int) (len - offset));
          offset += read;
        } while (read > 0);
      }
    } catch (java.io.IOException ex) {
      throw new SerialException("SerialClob: " + ex.getMessage());
    }
    origLen = len;
  }

  public long length() throws SerialException {
    isValid();
    return len;
  }

  public Reader getCharacterStream() throws SerialException {
    isValid();
    return new CharArrayReader(buf);
  }

  public InputStream getAsciiStream() throws SerialException, SQLException {
    isValid();
    if (this.clob != null) {
      return this.clob.getAsciiStream();
    } else {
      throw new SerialException("Unsupported operation. SerialClob cannot return the CLOB value as an ascii stream, unless instantiated with a fully implemented Clob object.");
    }
  }

  public String getSubString(long pos, int length) throws SerialException {
    isValid();
    if (pos < 1 || pos > this.length()) {
      throw new SerialException("Invalid position in SerialClob object set");
    }
    if ((pos - 1) + length > this.length()) {
      throw new SerialException("Invalid position and substring length");
    }
    try {
      return new String(buf, (int) pos - 1, length);
    } catch (StringIndexOutOfBoundsException e) {
      throw new SerialException("StringIndexOutOfBoundsException: " + e.getMessage());
    }
  }

  public long position(String searchStr, long start) throws SerialException, SQLException {
    isValid();
    if (start < 1 || start > len) {
      return -1;
    }
    char[] pattern = searchStr.toCharArray();
    int pos = (int) start - 1;
    int i = 0;
    long patlen = pattern.length;
    while (pos < len) {
      if (pattern[i] == buf[pos]) {
        if (i + 1 == patlen) {
          return (pos + 1) - (patlen - 1);
        }
        i++;
        pos++;
      } else if (pattern[i] != buf[pos]) {
        pos++;
      }
    }
    return -1;
  }

  public long position(Clob searchStr, long start) throws SerialException, SQLException {
    isValid();
    return position(searchStr.getSubString(1, (int) searchStr.length()), start);
  }

  public int setString(long pos, String str) throws SerialException {
    return setString(pos, str, 0, str.length());
  }

  public int setString(long pos, String str, int offset, int length) throws SerialException {
    isValid();
    String temp = str.substring(offset);
    char[] cPattern = temp.toCharArray();
    if (offset < 0 || offset > str.length()) {
      throw new SerialException("Invalid offset in byte array set");
    }
    if (pos < 1 || pos > this.length()) {
      throw new SerialException("Invalid position in Clob object set");
    }
    if ((long) (length) > origLen) {
      throw new SerialException("Buffer is not sufficient to hold the value");
    }
    if ((length + offset) > str.length()) {
      throw new SerialException("Invalid OffSet. Cannot have combined offset and length that is greater that the Blob buffer");
    }
    int i = 0;
    pos--;
    while (i < length || (offset + i + 1) < (str.length() - offset)) {
      this.buf[(int) pos + i] = cPattern[i];
      i++;
    }
    return i;
  }

  public OutputStream setAsciiStream(long pos) throws SerialException, SQLException {
    isValid();
    if (this.clob != null) {
      return this.clob.setAsciiStream(pos);
    } else {
      throw new SerialException("Unsupported operation. SerialClob cannot return a writable ascii stream\n unless instantiated with a Clob object that has a setAsciiStream() implementation");
    }
  }

  public Writer setCharacterStream(long pos) throws SerialException, SQLException {
    isValid();
    if (this.clob != null) {
      return this.clob.setCharacterStream(pos);
    } else {
      throw new SerialException("Unsupported operation. SerialClob cannot return a writable character stream\n unless instantiated with a Clob object that has a setCharacterStream () implementation");
    }
  }

  public void truncate(long length) throws SerialException {
    isValid();
    if (length > len) {
      throw new SerialException("Length more than what can be truncated");
    } else {
      len = length;
      char[] temp = new char[(int) len];
      System.arraycopy(buf, 0, temp, 0, (int) len);
      buf = temp;
    }
  }

  public Reader getCharacterStream(long pos, long length) throws SQLException {
    isValid();
    if (pos < 1 || pos > len) {
      throw new SerialException("Invalid position in Clob object set");
    }
    if (length < 1 || length > len - pos + 1) {
      throw new SerialException("length is < 1 or pos + length > total number of characters");
    }
    return new CharArrayReader(buf, (int) pos, (int) length);
  }

  public void free() throws SQLException {
    if (buf != null) {
      buf = null;
      if (clob != null) {
        clob.free();
      }
      clob = null;
    }
  }

  private void isValid() throws SerialException {
    if (buf == null) {
      throw new SerialException("Error: You cannot call a method on a SerialClob instance once free() has been called.");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof SerialClob) {
      SerialClob sc = (SerialClob) obj;
      if (this.len == sc.len) {
        return Arrays.equals(buf, sc.buf);
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ((31 + Arrays.hashCode(buf)) * 31 + (int) len) * 31 + (int) origLen;
  }

  @Override
  public Object clone() {
    try {
      SerialClob sc = (SerialClob) super.clone();
      sc.buf = (buf != null) ? Arrays.copyOf(buf, (int) len) : null;
      sc.clob = null;
      return sc;
    } catch (CloneNotSupportedException ex) {
      throw new InternalError();
    }
  }
}
