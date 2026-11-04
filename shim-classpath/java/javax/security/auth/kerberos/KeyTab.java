/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth.kerberos;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class KeyTab {
  private final File file;
  private final KerberosPrincipal princ;
  private final boolean bound;

  private KeyTab(KerberosPrincipal princ, File file, boolean bound) {
    this.princ = princ;
    this.file = file;
    this.bound = bound;
  }

  public static KeyTab getInstance(File file) {
    if (file == null) {
      throw new NullPointerException("file must be non null");
    }
    return new KeyTab(null, file, true);
  }

  public static KeyTab getUnboundInstance(File file) {
    if (file == null) {
      throw new NullPointerException("file must be non null");
    }
    return new KeyTab(null, file, false);
  }

  public static KeyTab getInstance(KerberosPrincipal princ, File file) {
    if (princ == null) {
      throw new NullPointerException("princ must be non null");
    }
    if (file == null) {
      throw new NullPointerException("file must be non null");
    }
    return new KeyTab(princ, file, true);
  }

  public static KeyTab getInstance() {
    return new KeyTab(null, null, true);
  }

  public static KeyTab getUnboundInstance() {
    return new KeyTab(null, null, false);
  }

  public static KeyTab getInstance(KerberosPrincipal princ) {
    if (princ == null) {
      throw new NullPointerException("princ must be non null");
    }
    return new KeyTab(princ, null, true);
  }

  private File resolvedFile() {
    if (file != null) {
      return file;
    }
    String name = System.getProperty("java.security.krb5.keytab");
    if (name == null) {
      name = System.getenv("KRB5_KTNAME");
    }
    if (name == null) {
      name = System.getProperty("user.home") + File.separator + "krb5.keytab";
    }
    if (name.startsWith("FILE:")) {
      name = name.substring(5);
    }
    return new File(name);
  }

  public KerberosKey[] getKeys(KerberosPrincipal principal) {
    List<KerberosKey> out = new ArrayList<KerberosKey>();
    File f = resolvedFile();
    try (DataInputStream in = new DataInputStream(new java.io.BufferedInputStream(new FileInputStream(f)))) {
      int magic = in.readUnsignedByte();
      int version = in.readUnsignedByte();
      if (magic != 5 || (version != 1 && version != 2)) {
        return new KerberosKey[0];
      }
      boolean le = version == 1;
      while (true) {
        int size;
        try {
          size = readInt(in, le);
        } catch (java.io.EOFException e) {
          break;
        }
        if (size < 0) {
          in.skipBytes(-size);
          continue;
        }
        byte[] rec = new byte[size];
        in.readFully(rec);
        DataInputStream r = new DataInputStream(new java.io.ByteArrayInputStream(rec));
        int comps = readShort(r, le);
        if (version == 1) {
          comps -= 0;
        }
        String realm = readCounted(r, le);
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < comps; i++) {
          if (i > 0) {
            name.append('/');
          }
          name.append(readCounted(r, le));
        }
        if (version != 1) {
          readInt(r, le);
        }
        readInt(r, le);
        int kvno = r.readUnsignedByte();
        int keyType = readShort(r, le);
        int keyLen = readShort(r, le);
        byte[] key = new byte[keyLen];
        r.readFully(key);
        if (r.available() >= 4) {
          int kvno32 = readInt(r, le);
          if (kvno32 != 0) {
            kvno = kvno32;
          }
        }
        KerberosPrincipal p = new KerberosPrincipal(name + "@" + realm);
        if (p.equals(principal)) {
          out.add(new KerberosKey(p, key, keyType, kvno));
        }
      }
    } catch (IOException e) {
      return new KerberosKey[0];
    }
    return out.toArray(new KerberosKey[0]);
  }

  private static int readInt(DataInputStream in, boolean le) throws IOException {
    int v = in.readInt();
    return le ? Integer.reverseBytes(v) : v;
  }

  private static int readShort(DataInputStream in, boolean le) throws IOException {
    int v = in.readUnsignedShort();
    return le ? ((v & 0xff) << 8) | (v >> 8) : v;
  }

  private static String readCounted(DataInputStream in, boolean le) throws IOException {
    int n = readShort(in, le);
    byte[] b = new byte[n];
    in.readFully(b);
    return new String(b, "UTF-8");
  }

  public KerberosPrincipal getPrincipal() {
    return princ;
  }

  public boolean exists() {
    return resolvedFile().exists();
  }

  public boolean isBound() {
    return bound;
  }

  public String toString() {
    String s = (file == null) ? "Default keytab" : file.toString();
    if (!bound) {
      return s;
    } else if (princ == null) {
      return s + " for someone";
    } else {
      return s + " for " + princ;
    }
  }

  public int hashCode() {
    return Objects.hash(file, princ, bound);
  }

  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof KeyTab)) {
      return false;
    }
    KeyTab otherKtab = (KeyTab) other;
    return Objects.equals(otherKtab.princ, princ) && Objects.equals(otherKtab.file, file)
        && bound == otherKtab.bound;
  }
}
