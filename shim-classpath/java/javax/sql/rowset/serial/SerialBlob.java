/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.sql.rowset.serial;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Arrays;

public class SerialBlob implements Blob, Serializable, Cloneable {
  private static final long serialVersionUID = -8144641928112860441L;

  private byte[] buf;
  private Blob blob;
  private long len;
  private long origLen;

  public SerialBlob(byte[] b) throws SerialException, SQLException {
    len = b.length;
    buf = new byte[(int) len];
    for (int i = 0; i < len; i++) {
      buf[i] = b[i];
    }
    origLen = len;
  }

  public SerialBlob(Blob blob) throws SerialException, SQLException {
    if (blob == null) {
      throw new SQLException("Cannot instantiate a SerialBlob object with a null Blob object");
    }
    len = blob.length();
    buf = blob.getBytes(1, (int) len);
    this.blob = blob;
    origLen = len;
  }

  public byte[] getBytes(long pos, int length) throws SerialException {
    isValid();
    if (length > len) {
      length = (int) len;
    }
    if (pos < 1 || len - pos < 0) {
      throw new SerialException("Invalid arguments: position cannot be less than 1 or greater than the length of the SerialBlob");
    }
    pos--;
    byte[] data = new byte[length];
    System.arraycopy(buf, (int) pos, data, 0, length);
    return data;
  }

  public long length() throws SerialException {
    isValid();
    return len;
  }

  public InputStream getBinaryStream() throws SerialException {
    isValid();
    return new ByteArrayInputStream(buf);
  }

  public long position(byte[] pattern, long start) throws SerialException, SQLException {
    isValid();
    if (start < 1 || start > len) {
      return -1;
    }
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

  public long position(Blob pattern, long start) throws SerialException, SQLException {
    isValid();
    return position(pattern.getBytes(1, (int) (pattern.length())), start);
  }

  public int setBytes(long pos, byte[] bytes) throws SerialException, SQLException {
    return setBytes(pos, bytes, 0, bytes.length);
  }

  public int setBytes(long pos, byte[] bytes, int offset, int length) throws SerialException, SQLException {
    isValid();
    if (offset < 0 || offset > bytes.length) {
      throw new SerialException("Invalid offset in byte array set");
    }
    if (pos < 1 || pos > this.length()) {
      throw new SerialException("Invalid position in BLOB object set");
    }
    if ((long) (length) > origLen) {
      throw new SerialException("Buffer is not sufficient to hold the value");
    }
    if ((length + offset) > bytes.length) {
      throw new SerialException("Invalid OffSet. Cannot have combined offset and length that is greater that the Blob buffer");
    }
    int i = 0;
    pos--;
    while (i < length || (offset + i + 1) < (bytes.length - offset)) {
      this.buf[(int) pos + i] = bytes[offset + i];
      i++;
    }
    return i;
  }

  public OutputStream setBinaryStream(long pos) throws SerialException, SQLException {
    isValid();
    if (this.blob != null) {
      return this.blob.setBinaryStream(pos);
    } else {
      throw new SerialException("Unsupported operation. SerialBlob cannot return a writable binary stream, unless instantiated with a Blob object that provides a setBinaryStream() implementation");
    }
  }

  public void truncate(long length) throws SerialException {
    isValid();
    if (length > len) {
      throw new SerialException("Length more than what can be truncated");
    } else if ((int) length == 0) {
      buf = new byte[0];
      len = length;
    } else {
      len = length;
      buf = this.getBytes(1, (int) len);
    }
  }

  public InputStream getBinaryStream(long pos, long length) throws SQLException {
    isValid();
    if (pos < 1 || pos > len) {
      throw new SerialException("Invalid position in BLOB object set");
    }
    if (length < 1 || length > len - pos + 1) {
      throw new SerialException("length is < 1 or pos + length > total number of bytes");
    }
    return new ByteArrayInputStream(buf, (int) pos - 1, (int) length);
  }

  public void free() throws SQLException {
    if (this.buf != null) {
      this.buf = null;
      if (this.blob != null) {
        this.blob.free();
      }
      this.blob = null;
    }
  }

  private void isValid() throws SerialException {
    if (buf == null) {
      throw new SerialException("Error: You cannot call a method on a SerialBlob instance once free() has been called.");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof SerialBlob) {
      SerialBlob sb = (SerialBlob) obj;
      if (this.len == sb.len) {
        return Arrays.equals(buf, sb.buf);
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
      SerialBlob sb = (SerialBlob) super.clone();
      sb.buf = (buf != null) ? Arrays.copyOf(buf, (int) len) : null;
      sb.blob = null;
      return sb;
    } catch (CloneNotSupportedException ex) {
      throw new InternalError();
    }
  }
}
