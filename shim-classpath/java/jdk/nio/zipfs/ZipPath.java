/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.nio.zipfs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

final class ZipPath implements Path {
  private final ZipFileSystem zfs;
  private final String path;
  private volatile int[] offsets;

  ZipPath(ZipFileSystem zfs, String path) {
    this(zfs, path, false);
  }

  ZipPath(ZipFileSystem zfs, String path, boolean normalized) {
    this.zfs = zfs;
    this.path = normalized ? path : normalize(path);
  }

  static String normalize(String path) {
    int n = path.length();
    if (n == 0) {
      return path;
    }
    char prev = 0;
    for (int i = 0; i < n; i++) {
      char c = path.charAt(i);
      if (c == '\u0000') {
        throw new InvalidPathException(path, "Path: nul character not allowed");
      }
      if (c == '/' && prev == '/') {
        return normalize(path, i - 1);
      }
      prev = c;
    }
    if (prev == '/') {
      return normalize(path, n - 1);
    }
    return path;
  }

  private static String normalize(String path, int off) {
    int n = path.length();
    while (n > off && path.charAt(n - 1) == '/') {
      n--;
    }
    if (n == 0) {
      return "/";
    }
    StringBuilder sb = new StringBuilder(n);
    if (off > 0) {
      sb.append(path, 0, off);
    }
    char prev = 0;
    for (int i = off; i < n; i++) {
      char c = path.charAt(i);
      if (c == '/' && prev == '/') {
        continue;
      }
      if (c == '\u0000') {
        throw new InvalidPathException(path, "Path: nul character not allowed");
      }
      sb.append(c);
      prev = c;
    }
    return sb.toString();
  }

  String getPathString() {
    return path;
  }

  private boolean isAbsolute0() {
    return path.length() > 0 && path.charAt(0) == '/';
  }

  private void initOffsets() {
    if (offsets == null) {
      int count = 0;
      int index = 0;
      if (path.isEmpty()) {
        count = 1;
      } else {
        while (index < path.length()) {
          char c = path.charAt(index++);
          if (c != '/') {
            count++;
            while (index < path.length() && path.charAt(index) != '/') {
              index++;
            }
          }
        }
      }
      int[] result = new int[count];
      count = 0;
      index = 0;
      while (index < path.length()) {
        char c = path.charAt(index);
        if (c == '/') {
          index++;
        } else {
          result[count++] = index++;
          while (index < path.length() && path.charAt(index) != '/') {
            index++;
          }
        }
      }
      offsets = result;
    }
  }

  @Override
  public ZipFileSystem getFileSystem() {
    return zfs;
  }

  @Override
  public boolean isAbsolute() {
    return isAbsolute0();
  }

  @Override
  public ZipPath getRoot() {
    if (isAbsolute0()) {
      return new ZipPath(zfs, "/", true);
    }
    return null;
  }

  @Override
  public Path getFileName() {
    initOffsets();
    int count = offsets.length;
    if (count == 0) {
      return null;
    }
    if (count == 1 && path.length() > 0 && path.charAt(0) != '/') {
      return this;
    }
    int lastOffset = offsets[count - 1];
    return new ZipPath(zfs, path.substring(lastOffset), true);
  }

  @Override
  public ZipPath getParent() {
    initOffsets();
    int count = offsets.length;
    if (count == 0) {
      return null;
    }
    int len = offsets[count - 1] - 1;
    if (len <= 0) {
      return getRoot();
    }
    return new ZipPath(zfs, path.substring(0, len), true);
  }

  @Override
  public int getNameCount() {
    initOffsets();
    return offsets.length;
  }

  @Override
  public Path getName(int index) {
    initOffsets();
    if (index < 0 || index >= offsets.length) {
      throw new IllegalArgumentException();
    }
    int begin = offsets[index];
    int end = index == offsets.length - 1 ? path.length() : offsets[index + 1] - 1;
    return new ZipPath(zfs, path.substring(begin, end), true);
  }

  @Override
  public Path subpath(int beginIndex, int endIndex) {
    initOffsets();
    if (beginIndex < 0 || beginIndex >= offsets.length || endIndex > offsets.length || beginIndex >= endIndex) {
      throw new IllegalArgumentException();
    }
    int begin = offsets[beginIndex];
    int end = endIndex == offsets.length ? path.length() : offsets[endIndex] - 1;
    return new ZipPath(zfs, path.substring(begin, end), true);
  }

  @Override
  public Path toRealPath(LinkOption... options) throws IOException {
    ZipPath realPath = new ZipPath(zfs, getResolvedPath()).toAbsolutePath();
    realPath.checkAccess();
    return realPath;
  }

  private String getResolvedPath() {
    String resolved = path;
    if (resolved.length() == 0) {
      return "/";
    }
    return normalizeDots(resolved);
  }

  private static String normalizeDots(String p) {
    boolean absolute = p.startsWith("/");
    String[] parts = p.split("/");
    List<String> out = new ArrayList<String>();
    for (String s : parts) {
      if (s.isEmpty() || s.equals(".")) {
        continue;
      }
      if (s.equals("..")) {
        if (!out.isEmpty() && !out.get(out.size() - 1).equals("..")) {
          out.remove(out.size() - 1);
        } else if (!absolute) {
          out.add(s);
        }
        continue;
      }
      out.add(s);
    }
    StringBuilder sb = new StringBuilder();
    if (absolute) {
      sb.append('/');
    }
    for (int i = 0; i < out.size(); i++) {
      if (i > 0) {
        sb.append('/');
      }
      sb.append(out.get(i));
    }
    if (sb.length() == 0) {
      return absolute ? "/" : "";
    }
    return sb.toString();
  }

  void checkAccess() throws IOException {
    if (!zfs.exists(this)) {
      throw new NoSuchFileException(toString());
    }
  }

  @Override
  public ZipPath toAbsolutePath() {
    if (isAbsolute()) {
      return this;
    }
    byte[] defaultdir = zfs.getDefaultDir();
    String d = new String(defaultdir, java.nio.charset.StandardCharsets.UTF_8);
    if (d.equals("/")) {
      return new ZipPath(zfs, "/" + path, true);
    }
    return new ZipPath(zfs, d + "/" + path, true);
  }

  @Override
  public URI toUri() {
    try {
      return new URI("jar", ZipFileSystem.decodeUri(zfs.getZipFile().toUri().toString()) + "!"
          + toAbsolutePath().path, null);
    } catch (Exception ex) {
      throw new AssertionError(ex);
    }
  }

  @Override
  public Path relativize(Path other) {
    final ZipPath o = checkPath(other);
    if (o.equals(this)) {
      return new ZipPath(getFileSystem(), "", true);
    }
    if (path.length() == 0) {
      return o;
    }
    if (this.isAbsolute() != o.isAbsolute()) {
      throw new IllegalArgumentException();
    }
    if (o.path.length() == 0) {
      StringBuilder sb = new StringBuilder();
      int n = getNameCount();
      for (int i = 0; i < n; i++) {
        if (i > 0) {
          sb.append('/');
        }
        sb.append("..");
      }
      return new ZipPath(zfs, sb.toString(), true);
    }
    int thisN = getNameCount();
    int otherN = o.getNameCount();
    int common = 0;
    while (common < thisN && common < otherN && getName(common).toString().equals(o.getName(common).toString())) {
      common++;
    }
    StringBuilder sb = new StringBuilder();
    for (int i = common; i < thisN; i++) {
      if (sb.length() > 0) {
        sb.append('/');
      }
      sb.append("..");
    }
    for (int i = common; i < otherN; i++) {
      if (sb.length() > 0) {
        sb.append('/');
      }
      sb.append(o.getName(i).toString());
    }
    return new ZipPath(zfs, sb.toString(), true);
  }

  @Override
  public Path normalize() {
    String r = normalizeDots(path);
    if (r.equals(path)) {
      return this;
    }
    return new ZipPath(zfs, r, true);
  }

  @Override
  public Path resolve(Path other) {
    ZipPath o = checkPath(other);
    if (o.isAbsolute()) {
      return o;
    }
    if (o.path.length() == 0) {
      return this;
    }
    if (path.length() == 0) {
      return o;
    }
    if (path.equals("/")) {
      return new ZipPath(zfs, "/" + o.path, true);
    }
    return new ZipPath(zfs, path + "/" + o.path, true);
  }

  @Override
  public Path resolveSibling(Path other) {
    Objects.requireNonNull(other, "other");
    Path parent = getParent();
    return (parent == null) ? other : parent.resolve(other);
  }

  @Override
  public boolean startsWith(Path other) {
    if (!(Objects.requireNonNull(other) instanceof ZipPath)) {
      return false;
    }
    final ZipPath that = (ZipPath) other;
    if (that.zfs != zfs) {
      return false;
    }
    if (that.path.length() > path.length()) {
      return false;
    }
    if (this.isAbsolute() != that.isAbsolute()) {
      return false;
    }
    int thatN = that.getNameCount();
    int thisN = getNameCount();
    if (that.path.length() == 0) {
      return path.length() == 0;
    }
    if (thatN > thisN) {
      return false;
    }
    if (that.path.equals("/")) {
      return this.isAbsolute();
    }
    for (int i = 0; i < thatN; i++) {
      if (!getName(i).toString().equals(that.getName(i).toString())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean endsWith(Path other) {
    if (!(Objects.requireNonNull(other) instanceof ZipPath)) {
      return false;
    }
    final ZipPath that = (ZipPath) other;
    if (that.zfs != zfs) {
      return false;
    }
    int thatLen = that.path.length();
    if (thatLen == 0) {
      return path.length() == 0;
    }
    if (that.isAbsolute()) {
      return this.isAbsolute() && this.path.equals(that.path);
    }
    int thatN = that.getNameCount();
    int thisN = getNameCount();
    if (thatN > thisN) {
      return false;
    }
    for (int i = 0; i < thatN; i++) {
      if (!getName(thisN - thatN + i).toString().equals(that.getName(i).toString())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int compareTo(Path other) {
    final ZipPath o = checkPath(other);
    return path.compareTo(o.path);
  }

  @Override
  public boolean equals(Object ob) {
    return ob instanceof ZipPath && ((ZipPath) ob).zfs == zfs && compareTo((Path) ob) == 0;
  }

  @Override
  public int hashCode() {
    return path.hashCode();
  }

  @Override
  public String toString() {
    return path;
  }

  @Override
  public File toFile() {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers)
      throws IOException {
    if (watcher == null || events == null || modifiers == null) {
      throw new NullPointerException();
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
    return register(watcher, events, new WatchEvent.Modifier[0]);
  }

  @Override
  public Iterator<Path> iterator() {
    return new Iterator<Path>() {
      private int i = 0;

      @Override
      public boolean hasNext() {
        return (i < getNameCount());
      }

      @Override
      public Path next() {
        if (i < getNameCount()) {
          Path result = getName(i);
          i++;
          return result;
        } else {
          throw new java.util.NoSuchElementException();
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    };
  }

  private ZipPath checkPath(Path path) {
    if (path == null) {
      throw new NullPointerException();
    }
    if (!(path instanceof ZipPath)) {
      throw new ProviderMismatchException();
    }
    return (ZipPath) path;
  }
}
