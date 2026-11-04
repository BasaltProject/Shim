/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

public final class FileManagerImpl implements StandardJavaFileManager {
  private final Charset charset;
  private final Map<Location, List<Path>> locations = new HashMap<Location, List<Path>>();
  private boolean open = true;

  public FileManagerImpl(DiagnosticListener<? super JavaFileObject> listener, java.util.Locale locale,
      Charset charset) {
    this.charset = charset == null ? Charset.defaultCharset() : charset;
  }

  private static JavaFileObject.Kind kindOf(String name) {
    if (name.endsWith(".java")) {
      return JavaFileObject.Kind.SOURCE;
    }
    if (name.endsWith(".class")) {
      return JavaFileObject.Kind.CLASS;
    }
    if (name.endsWith(".html")) {
      return JavaFileObject.Kind.HTML;
    }
    return JavaFileObject.Kind.OTHER;
  }

  public JavaFileObject fileObject(Path p) {
    return new PathFileObject(p, kindOf(p.getFileName().toString()), charset);
  }

  public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
    List<JavaFileObject> l = new ArrayList<JavaFileObject>();
    for (File f : files) {
      l.add(fileObject(f.toPath()));
    }
    return l;
  }

  public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
    return getJavaFileObjectsFromFiles(java.util.Arrays.asList(files));
  }

  public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
    List<File> l = new ArrayList<File>();
    for (String n : names) {
      l.add(new File(n));
    }
    return getJavaFileObjectsFromFiles(l);
  }

  public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
    return getJavaFileObjectsFromStrings(java.util.Arrays.asList(names));
  }

  public Iterable<? extends JavaFileObject> getJavaFileObjectsFromPaths(Collection<? extends Path> paths) {
    List<JavaFileObject> l = new ArrayList<JavaFileObject>();
    for (Path p : paths) {
      l.add(fileObject(p));
    }
    return l;
  }

  public void setLocation(Location location, Iterable<? extends File> files) throws IOException {
    List<Path> l = new ArrayList<Path>();
    if (files != null) {
      for (File f : files) {
        l.add(f.toPath());
      }
    }
    if (location.isOutputLocation() && l.size() == 1 && !Files.isDirectory(l.get(0))) {
      throw new java.io.FileNotFoundException(l.get(0) + ": does not exist");
    }
    locations.put(location, l);
  }

  public void setLocationFromPaths(Location location, Collection<? extends Path> paths) throws IOException {
    List<Path> l = new ArrayList<Path>(paths);
    locations.put(location, l);
  }

  public Iterable<? extends File> getLocation(Location location) {
    List<Path> l = pathsFor(location);
    if (l == null) {
      return null;
    }
    List<File> files = new ArrayList<File>();
    for (Path p : l) {
      files.add(p.toFile());
    }
    return files;
  }

  public Iterable<? extends Path> getLocationAsPaths(Location location) {
    return pathsFor(location);
  }

  private List<Path> pathsFor(Location location) {
    List<Path> l = locations.get(location);
    if (l != null) {
      return l;
    }
    if (location == StandardLocation.CLASS_PATH) {
      List<Path> cp = new ArrayList<Path>();
      String s = System.getProperty("java.class.path", ".");
      for (String e : s.split(File.pathSeparator)) {
        if (!e.isEmpty()) {
          cp.add(Paths.get(e));
        }
      }
      return cp;
    }
    if (location == StandardLocation.SOURCE_PATH || location == StandardLocation.CLASS_OUTPUT
        || location == StandardLocation.SOURCE_OUTPUT) {
      return null;
    }
    return null;
  }

  public ClassLoader getClassLoader(Location location) {
    List<Path> l = pathsFor(location);
    if (l == null) {
      return null;
    }
    java.net.URL[] urls = new java.net.URL[l.size()];
    try {
      for (int i = 0; i < urls.length; i++) {
        urls[i] = l.get(i).toUri().toURL();
      }
    } catch (java.net.MalformedURLException e) {
      throw new IllegalStateException(e);
    }
    return new java.net.URLClassLoader(urls);
  }

  public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds,
      boolean recurse) throws IOException {
    List<JavaFileObject> out = new ArrayList<JavaFileObject>();
    List<Path> roots = pathsFor(location);
    if (roots == null) {
      return out;
    }
    String rel = packageName.replace('.', '/');
    for (Path root : roots) {
      Path dir = rel.isEmpty() ? root : root.resolve(rel);
      if (Files.isDirectory(dir)) {
        collect(dir, kinds, recurse, out);
      }
    }
    return out;
  }

  private void collect(Path dir, Set<JavaFileObject.Kind> kinds, boolean recurse, List<JavaFileObject> out)
      throws IOException {
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
      List<Path> sorted = new ArrayList<Path>();
      for (Path p : ds) {
        sorted.add(p);
      }
      java.util.Collections.sort(sorted);
      for (Path p : sorted) {
        if (Files.isDirectory(p)) {
          if (recurse) {
            collect(p, kinds, true, out);
          }
        } else if (kinds.contains(kindOf(p.getFileName().toString()))) {
          out.add(fileObject(p));
        }
      }
    }
  }

  public String inferBinaryName(Location location, JavaFileObject file) {
    if (!(file instanceof PathFileObject)) {
      return null;
    }
    Path p = ((PathFileObject) file).path().toAbsolutePath().normalize();
    List<Path> roots = pathsFor(location);
    if (roots != null) {
      for (Path r : roots) {
        Path root = r.toAbsolutePath().normalize();
        if (p.startsWith(root)) {
          String s = root.relativize(p).toString().replace(File.separatorChar, '.');
          int dot = s.lastIndexOf('.');
          return dot > 0 ? s.substring(0, dot) : s;
        }
      }
    }
    return null;
  }

  public boolean isSameFile(FileObject a, FileObject b) {
    return a.equals(b);
  }

  public boolean handleOption(String current, Iterator<String> remaining) {
    String c = current;
    if (c.equals("-d") || c.equals("-cp") || c.equals("-classpath") || c.equals("--class-path")
        || c.equals("-sourcepath") || c.equals("--source-path") || c.equals("-s")) {
      if (!remaining.hasNext()) {
        throw new IllegalArgumentException(c + " requires an argument");
      }
      String v = remaining.next();
      List<Path> l = new ArrayList<Path>();
      for (String e : v.split(File.pathSeparator)) {
        if (!e.isEmpty()) {
          l.add(Paths.get(e));
        }
      }
      Location loc = c.equals("-d") ? StandardLocation.CLASS_OUTPUT
          : c.equals("-s") ? StandardLocation.SOURCE_OUTPUT
          : c.contains("source") ? StandardLocation.SOURCE_PATH : StandardLocation.CLASS_PATH;
      locations.put(loc, l);
      return true;
    }
    return false;
  }

  public boolean hasLocation(Location location) {
    return pathsFor(location) != null;
  }

  public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind)
      throws IOException {
    List<Path> roots = pathsFor(location);
    if (roots == null) {
      return null;
    }
    String rel = className.replace('.', '/') + kind.extension;
    for (Path r : roots) {
      Path p = r.resolve(rel);
      if (Files.isRegularFile(p)) {
        return fileObject(p);
      }
    }
    return null;
  }

  public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
      FileObject sibling) throws IOException {
    List<Path> roots = pathsFor(location);
    Path root;
    if (roots != null && !roots.isEmpty()) {
      root = roots.get(0);
    } else if (sibling instanceof PathFileObject) {
      Path sp = ((PathFileObject) sibling).path().toAbsolutePath().getParent();
      return fileObject(sp.resolve(className.substring(className.lastIndexOf('.') + 1) + kind.extension));
    } else {
      root = Paths.get(".");
    }
    return fileObject(root.resolve(className.replace('.', '/') + kind.extension));
  }

  public FileObject getFileForInput(Location location, String packageName, String relativeName)
      throws IOException {
    List<Path> roots = pathsFor(location);
    if (roots == null) {
      return null;
    }
    String rel = packageName.isEmpty() ? relativeName : packageName.replace('.', '/') + "/" + relativeName;
    for (Path r : roots) {
      Path p = r.resolve(rel);
      if (Files.isRegularFile(p)) {
        return fileObject(p);
      }
    }
    return null;
  }

  public FileObject getFileForOutput(Location location, String packageName, String relativeName,
      FileObject sibling) throws IOException {
    List<Path> roots = pathsFor(location);
    Path root = roots != null && !roots.isEmpty() ? roots.get(0) : Paths.get(".");
    String rel = packageName.isEmpty() ? relativeName : packageName.replace('.', '/') + "/" + relativeName;
    return fileObject(root.resolve(rel));
  }

  public void flush() throws IOException {
  }

  public void close() throws IOException {
    open = false;
  }

  public int isSupportedOption(String option) {
    switch (option) {
      case "-d":
      case "-cp":
      case "-classpath":
      case "--class-path":
      case "-sourcepath":
      case "--source-path":
      case "-s":
        return 1;
      default:
        return -1;
    }
  }

  public boolean contains(Location location, FileObject fo) throws IOException {
    if (!(fo instanceof PathFileObject)) {
      return false;
    }
    List<Path> roots = pathsFor(location);
    if (roots == null) {
      return false;
    }
    Path p = ((PathFileObject) fo).path().toAbsolutePath().normalize();
    for (Path r : roots) {
      if (p.startsWith(r.toAbsolutePath().normalize())) {
        return true;
      }
    }
    return false;
  }

  public java.nio.file.Path asPath(FileObject file) {
    if (file instanceof PathFileObject) {
      return ((PathFileObject) file).path();
    }
    throw new IllegalArgumentException(file.getName());
  }
}
