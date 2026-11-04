/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/** Minimal class file reader that extracts ConstantValue attributes of fields. */
final class ClassScan {
  private ClassScan() {
  }

  static byte[] readAll(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[8192];
    int n;
    while ((n = in.read(buf)) > 0) {
      out.write(buf, 0, n);
    }
    return out.toByteArray();
  }

  static void constants(byte[] bytes, Map<String, Object> out) throws IOException {
    DataInputStream d = new DataInputStream(new java.io.ByteArrayInputStream(bytes));
    d.readInt();
    d.readUnsignedShort();
    d.readUnsignedShort();
    int n = d.readUnsignedShort();
    Object[] pool = new Object[n];
    int[] tag = new int[n];
    for (int i = 1; i < n; i++) {
      int t = d.readUnsignedByte();
      tag[i] = t;
      switch (t) {
        case 1:
          pool[i] = d.readUTF();
          break;
        case 3:
          pool[i] = Integer.valueOf(d.readInt());
          break;
        case 4:
          pool[i] = Float.valueOf(d.readFloat());
          break;
        case 5:
          pool[i] = Long.valueOf(d.readLong());
          i++;
          break;
        case 6:
          pool[i] = Double.valueOf(d.readDouble());
          i++;
          break;
        case 7:
        case 8:
        case 16:
        case 19:
        case 20:
          pool[i] = Integer.valueOf(d.readUnsignedShort());
          break;
        case 9:
        case 10:
        case 11:
        case 12:
        case 17:
        case 18:
          d.readInt();
          break;
        case 15:
          d.readUnsignedByte();
          d.readUnsignedShort();
          break;
        default:
          throw new IOException("bad constant pool");
      }
    }
    d.readUnsignedShort();
    d.readUnsignedShort();
    d.readUnsignedShort();
    int ni = d.readUnsignedShort();
    for (int i = 0; i < ni; i++) {
      d.readUnsignedShort();
    }
    int nf = d.readUnsignedShort();
    for (int i = 0; i < nf; i++) {
      d.readUnsignedShort();
      String name = (String) pool[d.readUnsignedShort()];
      d.readUnsignedShort();
      int na = d.readUnsignedShort();
      for (int a = 0; a < na; a++) {
        String an = (String) pool[d.readUnsignedShort()];
        int len = d.readInt();
        if (an.equals("ConstantValue") && len == 2) {
          int idx = d.readUnsignedShort();
          Object v = pool[idx];
          if (tag[idx] == 8) {
            v = pool[((Integer) v).intValue()];
          }
          out.put(name, v);
        } else {
          d.skipBytes(len);
        }
      }
    }
  }
}
