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

package java.util.jar;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarFile extends ZipFile {
  public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";

  private static final Runtime.Version BASE_VERSION = Runtime.Version.parse("8");

  private Manifest manifest;
  private boolean manifestLoaded;
  private final Runtime.Version version;

  public JarFile(String name) throws IOException {
    this(new File(name), true, ZipFile.OPEN_READ, baseVersion());
  }

  public JarFile(String name, boolean verify) throws IOException {
    this(new File(name), verify, ZipFile.OPEN_READ, baseVersion());
  }

  public JarFile(File file) throws IOException {
    this(file, true, ZipFile.OPEN_READ, baseVersion());
  }

  public JarFile(File file, boolean verify) throws IOException {
    this(file, verify, ZipFile.OPEN_READ, baseVersion());
  }

  public JarFile(File file, boolean verify, int mode) throws IOException {
    this(file, verify, mode, baseVersion());
  }

  public JarFile(File file, boolean verify, int mode, Runtime.Version version) throws IOException {
    super(file, mode);
    Objects.requireNonNull(version, "version");
    if (version.feature() < 8) {
      throw new IllegalArgumentException("Version feature must be at least 8: " + version);
    }
    this.version = version;
  }

  public static Runtime.Version baseVersion() {
    return BASE_VERSION;
  }

  public static Runtime.Version runtimeVersion() {
    return Runtime.version();
  }

  public Runtime.Version getVersion() {
    return version;
  }

  public Manifest getManifest() throws IOException {
    if (!manifestLoaded) {
      ZipEntry ze = super.getEntry(MANIFEST_NAME);
      if (ze != null) {
        try (InputStream is = getInputStream(ze)) {
          manifest = new Manifest(is);
        }
      }
      manifestLoaded = true;
    }
    return manifest;
  }

  public boolean isMultiRelease() {
    try {
      Manifest man = getManifest();
      if (man != null) {
        Attributes attr = man.getMainAttributes();
        String mr = attr.getValue(Attributes.Name.MULTI_RELEASE);
        return mr != null && "true".equalsIgnoreCase(mr.trim());
      }
    } catch (IOException ignored) {
    }
    return false;
  }

  @Override
  public ZipEntry getEntry(String name) {
    return getJarEntry(name);
  }

  public JarEntry getJarEntry(String name) {
    if (name == null) {
      throw new NullPointerException("name");
    }
    while (name.startsWith("/")) {
      name = name.substring(1);
    }
    if (isMultiRelease() && version.feature() >= 9 && !name.startsWith("META-INF/")) {
      int targetVer = version.feature();
      for (int v = targetVer; v >= 9; v--) {
        String versionedName = "META-INF/versions/" + v + "/" + name;
        JarEntry ve = (JarEntry) super.getEntry(new JarEntryFactory(this, name), versionedName);
        if (ve != null) {
          return ve;
        }
      }
    }
    return (JarEntry) super.getEntry(new JarEntryFactory(this, name), name);
  }

  @Override
  public Enumeration<JarEntry> entries() {
    return (Enumeration<JarEntry>) makeEnumeration(new JarEntryFactory(this));
  }

  @Override
  public Stream<JarEntry> stream() {
    return StreamSupport.stream(
        Spliterators.spliterator(
            new Iterator<JarEntry>() {
              private final Enumeration<JarEntry> e = entries();
              public boolean hasNext() { return e.hasMoreElements(); }
              public JarEntry next() { return e.nextElement(); }
            },
            size(),
            Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL),
        false);
  }

  private static class MyJarEntry extends JarEntry implements MyEntry {
    private final JarFile jarFile;
    private final String baseName;
    public final Window window;
    public final int pointer;

    public MyJarEntry(JarFile jarFile, String baseName, Window window, int pointer) {
      super();
      this.jarFile = jarFile;
      this.baseName = baseName;
      this.window = window;
      this.pointer = pointer;
    }

    @Override
    public String getName() {
      return baseName != null ? baseName : getRealName();
    }

    @Override
    public String getRealName() {
      try {
        return entryName(window, pointer);
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    public Attributes getAttributes() throws IOException {
      if (attr != null) {
        return attr;
      }
      if (jarFile != null) {
        Manifest man = jarFile.getManifest();
        if (man != null) {
          attr = man.getAttributes(getName());
        }
      }
      return attr;
    }

    @Override
    public long getCompressedSize() {
      try {
        return compressedSize(window, pointer);
      } catch (IOException e) {
        return 0;
      }
    }

    @Override
    public long getSize() {
      try {
        return uncompressedSize(window, pointer);
      } catch (IOException e) {
        return 0;
      }
    }

    @Override
    public int pointer() {
      return pointer;
    }
  }

  private static class JarEntryFactory implements EntryFactory {
    private final JarFile jarFile;
    private final String baseName;

    public JarEntryFactory(JarFile jarFile) {
      this(jarFile, null);
    }

    public JarEntryFactory(JarFile jarFile, String baseName) {
      this.jarFile = jarFile;
      this.baseName = baseName;
    }

    public ZipEntry makeEntry(Window window, int pointer) {
      return new MyJarEntry(jarFile, baseName, window, pointer);
    }
  }
}
