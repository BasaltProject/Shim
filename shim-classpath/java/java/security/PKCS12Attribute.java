/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class PKCS12Attribute implements KeyStore.Entry.Attribute {
  private static final Pattern COLON_SEPARATED_HEX_PAIRS = Pattern.compile("^[0-9a-fA-F]{2}(:[0-9a-fA-F]{2})+$");

  private final String name;
  private final String value;
  private final byte[] encoded;
  private int hashValue = -1;

  public PKCS12Attribute(String name, String value) {
    if (name == null || value == null) {
      throw new NullPointerException();
    }
    if (!name.matches("[0-2](\\.[0-9]+)+")) {
      throw new IllegalArgumentException("Incorrect format: name");
    }
    this.name = name;
    String[] values;
    int length = value.length();
    String[] unwrapped = null;
    if (length > 1 && value.charAt(0) == '[' && value.charAt(length - 1) == ']') {
      unwrapped = value.substring(1, length - 1).split(", ");
    } else {
      unwrapped = new String[] {value};
    }
    values = unwrapped;
    this.value = value;
    this.encoded = encode(name, values);
  }

  public PKCS12Attribute(byte[] encoded) {
    if (encoded == null) {
      throw new NullPointerException();
    }
    this.encoded = encoded.clone();
    String[] parsed = decode(this.encoded);
    this.name = parsed[0];
    this.value = parsed[1];
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getValue() {
    return value;
  }

  public byte[] getEncoded() {
    return encoded.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PKCS12Attribute)) {
      return false;
    }
    return Arrays.equals(encoded, ((PKCS12Attribute) obj).encoded);
  }

  @Override
  public int hashCode() {
    if (hashValue == -1) {
      Arrays.hashCode(encoded);
      hashValue = Arrays.hashCode(encoded);
    }
    return hashValue;
  }

  @Override
  public String toString() {
    return name + "=" + value;
  }

  private static void writeLength(ByteArrayOutputStream out, int len) {
    if (len < 128) {
      out.write(len);
    } else if (len < 256) {
      out.write(0x81);
      out.write(len);
    } else {
      out.write(0x82);
      out.write(len >> 8);
      out.write(len & 0xff);
    }
  }

  private static byte[] tlv(int tag, byte[] body) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(tag);
    writeLength(out, body.length);
    out.write(body, 0, body.length);
    return out.toByteArray();
  }

  private static byte[] oid(String name) {
    String[] parts = name.split("\\.");
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    body.write(Integer.parseInt(parts[0]) * 40 + Integer.parseInt(parts[1]));
    for (int i = 2; i < parts.length; i++) {
      long v = Long.parseLong(parts[i]);
      byte[] tmp = new byte[10];
      int n = 0;
      do {
        tmp[n++] = (byte) (v & 0x7f);
        v >>= 7;
      } while (v != 0);
      for (int k = n - 1; k >= 0; k--) {
        body.write(tmp[k] | (k == 0 ? 0 : 0x80));
      }
    }
    return tlv(0x06, body.toByteArray());
  }

  private static byte[] encode(String name, String[] values) {
    ByteArrayOutputStream set = new ByteArrayOutputStream();
    for (String v : values) {
      byte[] item;
      if (COLON_SEPARATED_HEX_PAIRS.matcher(v).matches()) {
        String[] pairs = v.split(":");
        byte[] raw = new byte[pairs.length];
        for (int i = 0; i < pairs.length; i++) {
          raw[i] = (byte) Integer.parseInt(pairs[i], 16);
        }
        item = raw;
      } else {
        item = tlv(0x0c, v.getBytes(StandardCharsets.UTF_8));
      }
      set.write(item, 0, item.length);
    }
    ByteArrayOutputStream seq = new ByteArrayOutputStream();
    byte[] o = oid(name);
    seq.write(o, 0, o.length);
    byte[] s = tlv(0x31, set.toByteArray());
    seq.write(s, 0, s.length);
    return tlv(0x30, seq.toByteArray());
  }

  private static String[] decode(byte[] der) {
    try {
      int pos = 0;
      if ((der[pos++] & 0xff) != 0x30) {
        throw new IOException("not a SEQUENCE");
      }
      int[] l = readLength(der, pos);
      pos = l[1];
      if ((der[pos++] & 0xff) != 0x06) {
        throw new IOException("not an OID");
      }
      l = readLength(der, pos);
      int oidLen = l[0];
      pos = l[1];
      StringBuilder name = new StringBuilder();
      int first = der[pos] & 0xff;
      name.append(first / 40).append('.').append(first % 40);
      long v = 0;
      for (int i = 1; i < oidLen; i++) {
        int b = der[pos + i] & 0xff;
        v = (v << 7) | (b & 0x7f);
        if ((b & 0x80) == 0) {
          name.append('.').append(v);
          v = 0;
        }
      }
      pos += oidLen;
      if ((der[pos++] & 0xff) != 0x31) {
        throw new IOException("not a SET");
      }
      l = readLength(der, pos);
      int end = l[1] + l[0];
      pos = l[1];
      List<String> vals = new ArrayList<String>();
      while (pos < end) {
        int tag = der[pos++] & 0xff;
        l = readLength(der, pos);
        pos = l[1];
        if (tag == 0x0c || tag == 0x13 || tag == 0x16) {
          vals.add(new String(der, pos, l[0], StandardCharsets.UTF_8));
        } else {
          StringBuilder hex = new StringBuilder();
          for (int i = 0; i < l[0]; i++) {
            if (i > 0) {
              hex.append(':');
            }
            hex.append(String.format("%02X", der[pos + i]));
          }
          vals.add(hex.toString());
        }
        pos += l[0];
      }
      return new String[] {name.toString(), vals.size() == 1 ? vals.get(0) : vals.toString()};
    } catch (IOException | RuntimeException e) {
      throw new IllegalArgumentException("Incorrect format: encoded", e);
    }
  }

  private static int[] readLength(byte[] der, int pos) throws IOException {
    int b = der[pos++] & 0xff;
    if (b < 128) {
      return new int[] {b, pos};
    }
    int n = b & 0x7f;
    int len = 0;
    for (int i = 0; i < n; i++) {
      len = (len << 8) | (der[pos++] & 0xff);
    }
    return new int[] {len, pos};
  }
}
