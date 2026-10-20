/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

public final class JarCache {
  private static final class Entry {
    final JarFile file;
    final long modified;
    final long length;

    Entry(JarFile file, long modified, long length) {
      this.file = file;
      this.modified = modified;
      this.length = length;
    }
  }

  private static final Map<String, Entry> kEntries = new HashMap<String, Entry>();

  private JarCache() { }

  public static JarFile open(File file) throws IOException {
    String key = file.getAbsolutePath();
    long modified = file.lastModified();
    long length = file.length();
    synchronized (kEntries) {
      Entry e = kEntries.get(key);
      if (e != null && e.modified == modified && e.length == length) {
        return e.file;
      }
    }
    JarFile jar = new JarFile(file);
    synchronized (kEntries) {
      Entry e = kEntries.get(key);
      if (e != null && e.modified == modified && e.length == length) {
        jar.close();
        return e.file;
      }
      kEntries.put(key, new Entry(jar, modified, length));
    }
    return jar;
  }
}
