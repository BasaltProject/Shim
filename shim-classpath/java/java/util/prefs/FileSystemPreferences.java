/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.prefs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class FileSystemPreferences extends AbstractPreferences {
  private static final String kAltAlphabet =
      "!\"#$%&'(),-.:;<>@[]^`_{|}~abcdefghijklmnopqrstuvwxyz0123456789+?";
  private static final char kPad = '=';
  private static FileSystemPreferences userRoot;
  private static FileSystemPreferences systemRoot;

  private final File dir;
  private final boolean user;
  private final boolean unusable;
  private TreeMap<String, String> prefsCache = new TreeMap<String, String>();
  private boolean dirty;
  private boolean createPending;

  static synchronized Preferences getUserRoot() {
    if (userRoot == null) {
      File base = new File(System.getProperty("java.util.prefs.userRoot", System.getProperty("user.home")),
          ".java/.userPrefs");
      userRoot = new FileSystemPreferences(base, true, "user");
    }
    return userRoot;
  }

  static synchronized Preferences getSystemRoot() {
    if (systemRoot == null) {
      File base = new File(System.getProperty("java.util.prefs.systemRoot", "/etc/.java"), ".systemPrefs");
      systemRoot = new FileSystemPreferences(base, false, "system");
    }
    return systemRoot;
  }

  private FileSystemPreferences(File base, boolean user, String kind) {
    super(null, "");
    this.user = user;
    this.dir = base;
    boolean failed = false;
    if (!base.isDirectory()) {
      if (base.mkdirs()) {
        base.setReadable(true, true);
        base.setWritable(true, true);
        base.setExecutable(true, true);
        System.getLogger("java.util.prefs.FileSystemPreferences").log(System.Logger.Level.INFO,
            "Created " + kind + " preferences directory.");
      } else {
        failed = true;
        System.getLogger("java.util.prefs.FileSystemPreferences").log(System.Logger.Level.WARNING,
            "Couldn't create " + kind + " preferences directory. " + (user ? "User" : "System")
                + " preferences are unusable.");
      }
    }
    this.unusable = failed;
    newNode = !new File(base, "prefs.xml").exists();
    load();
  }

  private FileSystemPreferences(FileSystemPreferences parent, String name) {
    super(parent, name);
    this.user = parent.user;
    this.unusable = parent.unusable;
    this.dir = new File(parent.dir, dirName(name));
    newNode = !dir.exists();
    createPending = newNode;
    load();
  }

  public boolean isUserNode() {
    return user;
  }

  private File prefsFile() {
    return new File(dir, "prefs.xml");
  }

  private void load() {
    TreeMap<String, String> loaded = new TreeMap<String, String>();
    File file = prefsFile();
    if (file.exists()) {
      try {
        PreferencesXml.importMap(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8), loaded);
      } catch (IOException | InvalidPreferencesFormatException e) {
        System.getLogger("java.util.prefs.FileSystemPreferences").log(System.Logger.Level.WARNING,
            "Prefs file corrupted: " + file);
      }
    }
    prefsCache = loaded;
    dirty = false;
  }

  protected void putSpi(String key, String value) {
    prefsCache.put(key, value);
    dirty = true;
  }

  protected String getSpi(String key) {
    return prefsCache.get(key);
  }

  protected void removeSpi(String key) {
    prefsCache.remove(key);
    dirty = true;
  }

  protected void removeNodeSpi() throws BackingStoreException {
    if (createPending) {
      createPending = false;
      prefsCache.clear();
      dirty = false;
      return;
    }
    deleteTree(dir);
    prefsCache.clear();
    dirty = false;
  }

  private static void deleteTree(File file) {
    File[] files = file.listFiles();
    if (files != null) {
      for (File f : files) {
        deleteTree(f);
      }
    }
    file.delete();
  }

  protected String[] keysSpi() throws BackingStoreException {
    return prefsCache.keySet().toArray(new String[prefsCache.size()]);
  }

  protected String[] childrenNamesSpi() throws BackingStoreException {
    List<String> names = new ArrayList<String>();
    File[] files = dir.listFiles();
    if (files != null) {
      for (File f : files) {
        if (f.isDirectory()) {
          names.add(nodeName(f.getName()));
        }
      }
    }
    return names.toArray(new String[names.size()]);
  }

  protected AbstractPreferences childSpi(String name) {
    return new FileSystemPreferences(this, name);
  }

  protected void syncSpi() throws BackingStoreException {
    if (isRemoved()) {
      return;
    }
    if (dirty) {
      flushSpi();
    }
    load();
  }

  protected void flushSpi() throws BackingStoreException {
    if (isRemoved() || !(dirty || createPending)) {
      return;
    }
    if (unusable) {
      throw new BackingStoreException("Couldn't get file lock.");
    }
    try {
      Files.createDirectories(dir.toPath());
      Files.write(prefsFile().toPath(), PreferencesXml.exportMap(prefsCache).getBytes(StandardCharsets.UTF_8));
      dirty = false;
      createPending = false;
    } catch (IOException e) {
      throw new BackingStoreException(e);
    }
  }

  private static boolean isDirChar(char ch) {
    return ch > 0x1f && ch < 0x7f && ch != '/' && ch != '.' && ch != '_';
  }

  private static String dirName(String nodeName) {
    for (int i = 0, n = nodeName.length(); i < n; i++) {
      if (!isDirChar(nodeName.charAt(i))) {
        return "_" + altBase64Encode(nodeName);
      }
    }
    return nodeName;
  }

  private static String nodeName(String dirName) {
    if (dirName.charAt(0) != '_') {
      return dirName;
    }
    return altBase64Decode(dirName.substring(1));
  }

  private static String altBase64Encode(String name) {
    byte[] bytes = name.getBytes(StandardCharsets.UTF_16BE);
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (; i + 2 < bytes.length; i += 3) {
      int v = ((bytes[i] & 0xff) << 16) | ((bytes[i + 1] & 0xff) << 8) | (bytes[i + 2] & 0xff);
      sb.append(kAltAlphabet.charAt(v >> 18)).append(kAltAlphabet.charAt((v >> 12) & 63))
          .append(kAltAlphabet.charAt((v >> 6) & 63)).append(kAltAlphabet.charAt(v & 63));
    }
    int rest = bytes.length - i;
    if (rest == 1) {
      int v = (bytes[i] & 0xff) << 16;
      sb.append(kAltAlphabet.charAt(v >> 18)).append(kAltAlphabet.charAt((v >> 12) & 63)).append(kPad).append(kPad);
    } else if (rest == 2) {
      int v = ((bytes[i] & 0xff) << 16) | ((bytes[i + 1] & 0xff) << 8);
      sb.append(kAltAlphabet.charAt(v >> 18)).append(kAltAlphabet.charAt((v >> 12) & 63))
          .append(kAltAlphabet.charAt((v >> 6) & 63)).append(kPad);
    }
    return sb.toString();
  }

  private static String altBase64Decode(String encoded) {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    int acc = 0;
    int bits = 0;
    for (int i = 0; i < encoded.length(); i++) {
      char c = encoded.charAt(i);
      if (c == kPad) {
        break;
      }
      int v = kAltAlphabet.indexOf(c);
      if (v < 0 || v > 63) {
        throw new IllegalArgumentException("Illegal character in directory name: " + c);
      }
      acc = (acc << 6) | v;
      bits += 6;
      if (bits >= 8) {
        bits -= 8;
        out.write((acc >> bits) & 0xff);
      }
    }
    return new String(out.toByteArray(), StandardCharsets.UTF_16BE);
  }
}
