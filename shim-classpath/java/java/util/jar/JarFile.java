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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
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
  private static final String kVersionsPrefix = "META-INF/versions/";

  private Manifest manifest;
  private boolean manifestLoaded;
  private Boolean multiRelease;
  private final Runtime.Version version;
  private final int versionFeature;

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
    if (version.feature() <= 8) {
      this.version = BASE_VERSION;
      this.versionFeature = 8;
    } else {
      this.version = version;
      this.versionFeature = version.feature();
    }
  }

  public static Runtime.Version baseVersion() {
    return BASE_VERSION;
  }

  public static Runtime.Version runtimeVersion() {
    return Runtime.version();
  }

  public final Runtime.Version getVersion() {
    return version;
  }

  public final boolean isMultiRelease() {
    if (multiRelease == null) {
      boolean mr = false;
      try {
        Manifest man = getManifest();
        if (man != null) {
          String v = man.getMainAttributes().getValue(Attributes.Name.MULTI_RELEASE);
          mr = v != null && "true".equalsIgnoreCase(v.trim());
        }
      } catch (IOException ignored) {
      }
      multiRelease = mr;
    }
    return multiRelease;
  }

  public Manifest getManifest() throws IOException {
    synchronized (this) {
      if (!manifestLoaded) {
        ZipEntry ze = super.getEntry(MANIFEST_NAME);
        if (ze != null) {
          try (InputStream is = super.getInputStream(ze)) {
            manifest = new Manifest(is);
          }
        }
        manifestLoaded = true;
      }
      return manifest;
    }
  }

  public JarEntry getJarEntry(String name) {
    return (JarEntry) getEntry(name);
  }

  @Override
  public ZipEntry getEntry(String name) {
    ZipEntry ze = super.getEntry(name);
    JarEntry je = ze == null ? null : new JarFileEntry(ze);
    if (isMultiRelease()) {
      return getVersionedEntry(name, je);
    }
    return je;
  }

  private JarEntry getVersionedEntry(String name, JarEntry je) {
    if (versionFeature > 8 && !name.startsWith("META-INF/")) {
      for (int v = versionFeature; v >= 9; v--) {
        ZipEntry vze = super.getEntry(kVersionsPrefix + v + "/" + name);
        if (vze != null) {
          return new JarFileEntry(name, vze);
        }
      }
    }
    return je;
  }

  @Override
  public Enumeration<JarEntry> entries() {
    final Enumeration<? extends ZipEntry> en = super.entries();
    return new Enumeration<JarEntry>() {
      @Override
      public boolean hasMoreElements() {
        return en.hasMoreElements();
      }

      @Override
      public JarEntry nextElement() {
        if (!en.hasMoreElements()) {
          throw new NoSuchElementException();
        }
        return new JarFileEntry(en.nextElement());
      }
    };
  }

  @Override
  public Stream<JarEntry> stream() {
    final Enumeration<JarEntry> en = entries();
    return StreamSupport.stream(
        Spliterators.spliterator(new java.util.Iterator<JarEntry>() {
          @Override
          public boolean hasNext() {
            return en.hasMoreElements();
          }

          @Override
          public JarEntry next() {
            return en.nextElement();
          }
        }, size(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL),
        false);
  }

  public Stream<JarEntry> versionedStream() {
    if (isMultiRelease()) {
      List<String> names = new ArrayList<String>();
      Enumeration<? extends ZipEntry> en = super.entries();
      while (en.hasMoreElements()) {
        String base = getBasename(en.nextElement().getName());
        if (base != null && !names.contains(base)) {
          names.add(base);
        }
      }
      List<JarEntry> out = new ArrayList<JarEntry>();
      for (String n : names) {
        JarEntry je = getJarEntry(n);
        if (je != null) {
          out.add(je);
        }
      }
      return out.stream();
    }
    return stream();
  }

  private String getBasename(String name) {
    if (name.startsWith(kVersionsPrefix)) {
      int off = kVersionsPrefix.length();
      int index = name.indexOf('/', off);
      try {
        if (index == -1 || index == name.length() - 1) {
          return null;
        }
        int v = Integer.parseInt(name.substring(off, index));
        if (v > 0 && v <= versionFeature) {
          return name.substring(index + 1);
        }
      } catch (NumberFormatException ignored) {
      }
      return null;
    }
    return name;
  }

  @Override
  public InputStream getInputStream(ZipEntry ze) throws IOException {
    Objects.requireNonNull(ze, "entry");
    return super.getInputStream(ze);
  }

  private final class JarFileEntry extends JarEntry {
    private final String basename;

    JarFileEntry(ZipEntry ze) {
      super(ze);
      this.basename = null;
    }

    JarFileEntry(String name, ZipEntry vze) {
      super(vze);
      this.basename = name;
    }

    @Override
    public String getName() {
      return basename != null ? basename : super.getName();
    }

    @Override
    public String getRealName() {
      return super.getName();
    }

    @Override
    public Attributes getAttributes() throws IOException {
      Manifest man = JarFile.this.getManifest();
      return man == null ? null : man.getAttributes(getName());
    }
  }
}
