/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.FindException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ModuleReference;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class Finders {
  private Finders() {
  }

  public static ModuleFinder system() {
    List<ModuleReference> refs = new ArrayList<ModuleReference>();
    for (Module m : ModuleLayer.boot().modules()) {
      if (m.getDescriptor() != null) {
        refs.add(new Ref(m.getDescriptor(), URI.create("jrt:/" + m.getName()), null, null));
      }
    }
    return new Fixed(refs);
  }

  public static ModuleFinder compose(ModuleFinder[] finders) {
    for (ModuleFinder f : finders) {
      java.util.Objects.requireNonNull(f);
    }
    return new ModuleFinder() {
      private Set<ModuleReference> all;

      @Override
      public Optional<ModuleReference> find(String name) {
        java.util.Objects.requireNonNull(name);
        for (ModuleFinder f : finders) {
          Optional<ModuleReference> r = f.find(name);
          if (r.isPresent()) {
            return r;
          }
        }
        return Optional.empty();
      }

      @Override
      public Set<ModuleReference> findAll() {
        if (all == null) {
          Map<String, ModuleReference> m = new LinkedHashMap<String, ModuleReference>();
          for (ModuleFinder f : finders) {
            for (ModuleReference r : f.findAll()) {
              m.putIfAbsent(r.descriptor().name(), r);
            }
          }
          all = Collections.unmodifiableSet(new HashSet<ModuleReference>(m.values()));
        }
        return all;
      }
    };
  }

  public static ModuleFinder ofPaths(Path[] entries) {
    for (Path p : entries) {
      java.util.Objects.requireNonNull(p);
    }
    return new PathFinder(entries);
  }

  private static final class Fixed implements ModuleFinder {
    private final Map<String, ModuleReference> byName = new LinkedHashMap<String, ModuleReference>();

    Fixed(List<ModuleReference> refs) {
      for (ModuleReference r : refs) {
        byName.putIfAbsent(r.descriptor().name(), r);
      }
    }

    @Override
    public Optional<ModuleReference> find(String name) {
      java.util.Objects.requireNonNull(name);
      return Optional.ofNullable(byName.get(name));
    }

    @Override
    public Set<ModuleReference> findAll() {
      return Collections.unmodifiableSet(new HashSet<ModuleReference>(byName.values()));
    }
  }

  private static final class PathFinder implements ModuleFinder {
    private final Path[] entries;
    private final Map<String, ModuleReference> cache = new LinkedHashMap<String, ModuleReference>();
    private boolean scanned;

    PathFinder(Path[] entries) {
      this.entries = entries;
    }

    private void scan() {
      if (scanned) {
        return;
      }
      for (Path e : entries) {
        scanEntry(e);
      }
      scanned = true;
    }

    private void add(ModuleReference r) {
      String n = r.descriptor().name();
      if (!cache.containsKey(n)) {
        cache.put(n, r);
      }
    }

    private void scanEntry(Path e) {
      try {
        if (!Files.exists(e)) {
          return;
        }
        if (Files.isDirectory(e)) {
          if (Files.exists(e.resolve("module-info.class"))) {
            add(exploded(e));
            return;
          }
          try (Stream<Path> s = Files.list(e)) {
            List<Path> children = new ArrayList<Path>();
            s.forEach(children::add);
            Collections.sort(children);
            for (Path c : children) {
              if (Files.isDirectory(c)) {
                add(exploded(c));
              } else if (c.getFileName().toString().endsWith(".jar")) {
                add(jar(c));
              }
            }
          }
        } else {
          add(jar(e));
        }
      } catch (IOException ex) {
        throw new FindException(ex);
      }
    }

    private ModuleReference exploded(Path dir) throws IOException {
      Path mi = dir.resolve("module-info.class");
      ModuleDescriptor d;
      if (Files.exists(mi)) {
        try (InputStream in = Files.newInputStream(mi)) {
          d = ModuleDescriptor.read(in, () -> explodedPackagesUnchecked(dir));
        }
      } else {
        d = automatic(dir.getFileName().toString(), null, explodedPackages(dir));
      }
      return new Ref(d, dir.toUri(), dir, null);
    }

    private Set<String> explodedPackagesUnchecked(Path dir) {
      try {
        return explodedPackages(dir);
      } catch (IOException e) {
        throw new java.io.UncheckedIOException(e);
      }
    }

    private Set<String> explodedPackages(Path dir) throws IOException {
      Set<String> pkgs = new HashSet<String>();
      try (Stream<Path> s = Files.walk(dir)) {
        s.filter(p -> p.getFileName().toString().endsWith(".class") && !p.getFileName().toString().equals(
            "module-info.class")).forEach(p -> {
              Path rel = dir.relativize(p).getParent();
              if (rel != null) {
                pkgs.add(rel.toString().replace(File.separatorChar, '.'));
              }
            });
      }
      return pkgs;
    }

    private ModuleReference jar(Path file) throws IOException {
      try (JarFile jf = new JarFile(file.toFile())) {
        Set<String> pkgs = new HashSet<String>();
        JarEntry mi = jf.getJarEntry("module-info.class");
        java.util.Enumeration<JarEntry> en = jf.entries();
        while (en.hasMoreElements()) {
          String n = en.nextElement().getName();
          int slash = n.lastIndexOf('/');
          if (n.endsWith(".class") && slash > 0 && !n.startsWith("META-INF/")) {
            pkgs.add(n.substring(0, slash).replace('/', '.'));
          }
        }
        ModuleDescriptor d;
        if (mi != null) {
          try (InputStream in = jf.getInputStream(mi)) {
            d = ModuleDescriptor.read(in, () -> pkgs);
          }
        } else {
          d = automatic(autoName(file.getFileName().toString()), autoVersion(file.getFileName().toString()), pkgs);
        }
        return new Ref(d, file.toUri(), null, file);
      }
    }

    private static String autoName(String fn) {
      String n = fn.endsWith(".jar") ? fn.substring(0, fn.length() - 4) : fn;
      int i = versionStart(n);
      if (i >= 0) {
        n = n.substring(0, i);
      }
      return n;
    }

    private static int versionStart(String n) {
      for (int i = 0; i + 1 < n.length(); i++) {
        if (n.charAt(i) == '-' && Character.isDigit(n.charAt(i + 1))) {
          return i;
        }
      }
      return -1;
    }

    private static String autoVersion(String fn) {
      String n = fn.endsWith(".jar") ? fn.substring(0, fn.length() - 4) : fn;
      int i = versionStart(n);
      return i < 0 ? null : n.substring(i + 1);
    }

    private static ModuleDescriptor automatic(String raw, String version, Set<String> pkgs) {
      String n = raw.replaceAll("[^A-Za-z0-9]", ".").replaceAll("(\\.)(\\1)+", ".");
      n = n.replaceAll("^\\.", "").replaceAll("\\.$", "");
      ModuleDescriptor.Builder b = ModuleDescriptor.newAutomaticModule(n).packages(pkgs);
      if (version != null) {
        try {
          b.version(version);
        } catch (IllegalArgumentException e) {
        }
      }
      return b.build();
    }

    @Override
    public Optional<ModuleReference> find(String name) {
      java.util.Objects.requireNonNull(name);
      scan();
      return Optional.ofNullable(cache.get(name));
    }

    @Override
    public Set<ModuleReference> findAll() {
      scan();
      return Collections.unmodifiableSet(new HashSet<ModuleReference>(cache.values()));
    }
  }

  private static final class Ref extends ModuleReference {
    private final Path dir;
    private final Path jar;

    Ref(ModuleDescriptor d, URI location, Path dir, Path jar) {
      super(d, location);
      this.dir = dir;
      this.jar = jar;
    }

    @Override
    public ModuleReader open() throws IOException {
      if (dir != null) {
        return new DirReader(dir);
      }
      return new JarReader(jar);
    }
  }

  private static final class DirReader implements ModuleReader {
    private final Path dir;
    private boolean closed;

    DirReader(Path dir) {
      this.dir = dir;
    }

    @Override
    public Optional<URI> find(String name) throws IOException {
      if (closed) {
        throw new IOException("ModuleReader is closed");
      }
      Path p = dir.resolve(name);
      return Files.exists(p) ? Optional.of(p.toUri()) : Optional.empty();
    }

    @Override
    public Stream<String> list() throws IOException {
      if (closed) {
        throw new IOException("ModuleReader is closed");
      }
      List<String> names = new ArrayList<String>();
      try (Stream<Path> s = Files.walk(dir)) {
        s.filter(p -> !p.equals(dir)).forEach(p -> names.add(dir.relativize(p).toString().replace(File.separatorChar,
            '/') + (Files.isDirectory(p) ? "/" : "")));
      }
      return names.stream();
    }

    @Override
    public void close() {
      closed = true;
    }
  }

  private static final class JarReader implements ModuleReader {
    private final Path file;
    private boolean closed;

    JarReader(Path file) {
      this.file = file;
    }

    @Override
    public Optional<URI> find(String name) throws IOException {
      if (closed) {
        throw new IOException("ModuleReader is closed");
      }
      try (JarFile jf = new JarFile(file.toFile())) {
        if (jf.getJarEntry(name) == null) {
          return Optional.empty();
        }
      }
      return Optional.of(URI.create("jar:" + file.toUri() + "!/" + name));
    }

    @Override
    public Optional<InputStream> open(String name) throws IOException {
      if (closed) {
        throw new IOException("ModuleReader is closed");
      }
      JarFile jf = new JarFile(file.toFile());
      JarEntry e = jf.getJarEntry(name);
      if (e == null) {
        jf.close();
        return Optional.empty();
      }
      byte[] data;
      try (InputStream in = jf.getInputStream(e)) {
        data = in.readAllBytes();
      } finally {
        jf.close();
      }
      return Optional.of(new java.io.ByteArrayInputStream(data));
    }

    @Override
    public Stream<String> list() throws IOException {
      if (closed) {
        throw new IOException("ModuleReader is closed");
      }
      List<String> names = new ArrayList<String>();
      try (JarFile jf = new JarFile(file.toFile())) {
        java.util.Enumeration<JarEntry> en = jf.entries();
        while (en.hasMoreElements()) {
          names.add(en.nextElement().getName());
        }
      }
      return names.stream();
    }

    @Override
    public void close() {
      closed = true;
    }
  }
}
