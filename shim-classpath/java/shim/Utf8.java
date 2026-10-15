/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package shim;

import java.io.ByteArrayOutputStream;

public class Utf8 {
  public static boolean test(Object data) {
    if (!(data instanceof byte[])) return false;
    byte[] b = (byte[])data;
    for (int i = 0; i < b.length; ++i) {
      if (((int)b[i] & 0x080) != 0) return true;
    }
    return false;
  }

  public static byte[] encode(char[] s16, int offset, int length) {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    for (int i = offset; i < offset+length; ++i) {
      char c = s16[i];
      if (c >= 0xD800 && c <= 0xDBFF && i + 1 < offset + length) {
        char low = s16[i + 1];
        if (low >= 0xDC00 && low <= 0xDFFF) {
          int cp = (((c & 0x3FF) << 10) | (low & 0x3FF)) + 0x10000;
          buf.write(0xF0 | ((cp >>> 18) & 0x07));
          buf.write(0x80 | ((cp >>> 12) & 0x3F));
          buf.write(0x80 | ((cp >>> 6) & 0x3F));
          buf.write(0x80 | (cp & 0x3F));
          ++i;
          continue;
        }
      }
      if (c < 0x080) {  // 1 byte char
        buf.write(c);
      } else if (c < 0x0800) { // 2 byte char
        buf.write(0x0c0 | (c >>> 6));
        buf.write(0x080 | (c & 0x03f));
      } else {                // 3 byte char
        buf.write(0x0e0 | ((c >>> 12) & 0x0f));
        buf.write(0x080 | ((c >>> 6) & 0x03f));
        buf.write(0x080 | (c & 0x03f));
      }
    }
    return buf.toByteArray();
  }

  public static Object decode(byte[] s8, int offset, int length) {
    Object buf = new byte[length];
    boolean isMultiByte = false;
    int i=offset, j=0;
    while (i < offset+length) {
      int x = s8[i++];
      if ((x & 0x080) == 0x0) {          // 1 byte char
        if (x == 0) {                    // 2 byte null char
          if (i == offset + length) {
            return null;
          }
          ++ i;
        }
        cram(buf, j++, x);
      } else if ((x & 0x0e0) == 0x0c0) { // 2 byte char
        if (i == offset + length) {
          return null;
        }

        if (!isMultiByte) {
          buf = widen(buf, j, length-1);
          isMultiByte = true;
        }
        int y = s8[i++];
        cram(buf, j++, ((x & 0x1f) << 6) | (y & 0x3f));
      } else if ((x & 0x0f0) == 0x0e0) { // 3 byte char
        if (i + 1 >= offset + length) {
          return null;
        }

        if (!isMultiByte) {
          buf = widen(buf, j, length-2);
          isMultiByte = true;
        }
        int y = s8[i++]; int z = s8[i++];
        cram(buf, j++, ((x & 0xf) << 12) | ((y & 0x3f) << 6) | (z & 0x3f));
      } else if ((x & 0x0f8) == 0x0f0) { // 4 byte char
        if (i + 2 >= offset + length) {
          return null;
        }

        if (!isMultiByte) {
          buf = widen(buf, j, length);
          isMultiByte = true;
        }
        int y = s8[i++]; int z = s8[i++]; int w = s8[i++];
        int cp = ((x & 0x07) << 18) | ((y & 0x3f) << 12) | ((z & 0x3f) << 6) | (w & 0x3f);
        int cpPrime = cp - 0x10000;
        int high = 0xD800 | ((cpPrime >> 10) & 0x3FF);
        int low = 0xDC00 | (cpPrime & 0x3FF);
        cram(buf, j++, high);
        cram(buf, j++, low);
      }
    }

    return trim(buf, j);
  }

  public static char[] decode16(byte[] s8, int offset, int length) {
    Object decoded = decode(s8, offset, length);
    if (decoded == null) {
      return null;
    } else if (decoded instanceof char[]) {
      return (char[])decoded;
    } else {
      return (char[])widen(decoded, length, length);
    }
  }

  private static void cram(Object data, int index, int val) {
    if (data instanceof byte[]) ((byte[])data)[index] = (byte)val;
    else                        ((char[])data)[index] = (char)val;
  }

  private static Object widen(Object data, int length, int capacity) {
    byte[] src = (byte[])data;
    char[] result = new char[capacity];
    for (int i = 0; i < length; ++i) result[i] = (char)((int)src[i] & 0x0ff);
    return result;
  }

  private static Object trim(Object data, int length) {
    if (data instanceof byte[]) return data;
    if (((char[])data).length == length) return data;
    char[] result = new char[length];
    System.arraycopy(data, 0, result, 0, length);
    return result;
  }
}
