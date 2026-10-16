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

package java.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import shim.FileNative;

public class File implements Serializable, Comparable<File> {
  private static final long serialVersionUID = 301077366599181567L;

  public static final char separatorChar = '/';
  public static final String separator = "/";
  public static final char pathSeparatorChar = System.getProperty("path.separator", ":").charAt(0);
  public static final String pathSeparator = String.valueOf(pathSeparatorChar);

  private static final int kPermissionRead = 04;
  private static final int kPermissionWrite = 02;
  private static final int kPermissionExecute = 01;

  private final String path;

  private transient int prefixLength;

  int getPrefixLength() {
    return prefixLength;
  }

  private static String normalize(String pathname) {
    int n = pathname.length();
    char prev = 0;
    for (int i = 0; i < n; i++) {
      char c = pathname.charAt(i);
      if (prev == '/' && c == '/') {
        return normalize(pathname, n, i - 1);
      }
      prev = c;
    }
    if (prev == '/') {
      return normalize(pathname, n, n - 1);
    }
    return pathname;
  }

  private static String normalize(String pathname, int len, int off) {
    if (len == 0) {
      return pathname;
    }
    int n = len;
    while (n > 0 && pathname.charAt(n - 1) == '/') {
      n--;
    }
    if (n == 0) {
      return "/";
    }
    StringBuilder sb = new StringBuilder(pathname.length());
    if (off > 0) {
      sb.append(pathname, 0, off);
    }
    char prev = 0;
    for (int i = off; i < n; i++) {
      char c = pathname.charAt(i);
      if (prev == '/' && c == '/') {
        continue;
      }
      sb.append(c);
      prev = c;
    }
    return sb.toString();
  }

  private static String resolve(String parent, String child) {
    if (child.isEmpty()) {
      return parent;
    }
    if (child.charAt(0) == '/') {
      if (parent.equals("/")) {
        return child;
      }
      return parent + child;
    }
    if (parent.equals("/")) {
      return parent + child;
    }
    return parent + '/' + child;
  }

  private File(String pathname, int prefixLength) {
    this.path = pathname;
    this.prefixLength = prefixLength;
  }

  public File(String pathname) {
    if (pathname == null) {
      throw new NullPointerException();
    }
    this.path = normalize(pathname);
    this.prefixLength = path.startsWith("/") ? 1 : 0;
  }

  public File(String parent, String child) {
    if (child == null) {
      throw new NullPointerException();
    }
    if (parent != null) {
      if (parent.isEmpty()) {
        this.path = resolve("/", normalize(child));
      } else {
        this.path = resolve(normalize(parent), normalize(child));
      }
    } else {
      this.path = normalize(child);
    }
    this.prefixLength = path.startsWith("/") ? 1 : 0;
  }

  public File(File parent, String child) {
    if (child == null) {
      throw new NullPointerException();
    }
    if (parent != null) {
      if (parent.path.isEmpty()) {
        this.path = resolve("/", normalize(child));
      } else {
        this.path = resolve(parent.path, normalize(child));
      }
    } else {
      this.path = normalize(child);
    }
    this.prefixLength = path.startsWith("/") ? 1 : 0;
  }

  public File(URI uri) {
    Objects.requireNonNull(uri, "uri");
    if (!uri.isAbsolute()) {
      throw new IllegalArgumentException("URI is not absolute");
    }
    if (uri.isOpaque()) {
      throw new IllegalArgumentException("URI is not hierarchical");
    }
    String scheme = uri.getScheme();
    if (scheme == null || !scheme.equalsIgnoreCase("file")) {
      throw new IllegalArgumentException("URI scheme is not \"file\"");
    }
    if (uri.getRawAuthority() != null) {
      throw new IllegalArgumentException("URI has an authority component");
    }
    if (uri.getRawFragment() != null) {
      throw new IllegalArgumentException("URI has a fragment component");
    }
    if (uri.getRawQuery() != null) {
      throw new IllegalArgumentException("URI has a query component");
    }
    String p = uri.getPath();
    if (p.isEmpty()) {
      throw new IllegalArgumentException("URI path component is empty");
    }
    if (p.endsWith("/") && p.length() > 1) {
      p = p.substring(0, p.length() - 1);
    }
    this.path = normalize(p);
    this.prefixLength = path.startsWith("/") ? 1 : 0;
  }

  public String getName() {
    int index = path.lastIndexOf(separatorChar);
    if (index < prefixLength) {
      return path.substring(prefixLength);
    }
    return path.substring(index + 1);
  }

  public String getParent() {
    int index = path.lastIndexOf(separatorChar);
    if (index < prefixLength) {
      if (prefixLength > 0 && path.length() > prefixLength) {
        return path.substring(0, prefixLength);
      }
      return null;
    }
    return path.substring(0, index);
  }

  public File getParentFile() {
    String p = this.getParent();
    if (p == null) {
      return null;
    }
    return new File(p, this.prefixLength);
  }

  public String getPath() {
    return path;
  }

  public boolean isAbsolute() {
    return prefixLength != 0;
  }

  public String getAbsolutePath() {
    if (isAbsolute()) {
      return path;
    }
    String userDir = System.getProperty("user.dir");
    if (userDir == null) {
      userDir = FileNative.cwd();
    }
    return resolve(userDir, path);
  }

  public File getAbsoluteFile() {
    String absPath = getAbsolutePath();
    return new File(absPath, absPath.startsWith("/") ? 1 : 0);
  }

  public String getCanonicalPath() throws IOException {
    if (isInvalid()) {
      throw new IOException("Invalid file path");
    }
    return canonicalize(getAbsolutePath());
  }

  public File getCanonicalFile() throws IOException {
    String canonPath = getCanonicalPath();
    return new File(canonPath, canonPath.startsWith("/") ? 1 : 0);
  }

  private static String canonicalize(String abs) {
    String p = abs;
    String tail = "";
    String resolved = null;
    for (;;) {
      String r = FileNative.realpath(p);
      if (r != null) {
        resolved = r;
        break;
      }
      int slash = p.lastIndexOf('/');
      if (slash < 0) {
        resolved = "";
        tail = "/" + p + tail;
        break;
      }
      tail = p.substring(slash) + tail;
      if (slash == 0) {
        resolved = "";
        break;
      }
      p = p.substring(0, slash);
    }
    String combined = (resolved.equals("/") ? "" : resolved) + tail;
    return collapse(combined.isEmpty() ? "/" : combined);
  }

  private static String collapse(String path) {
    String[] parts = path.split("/");
    List<String> stack = new ArrayList<String>();
    for (String part : parts) {
      if (part.isEmpty() || part.equals(".")) {
        continue;
      }
      if (part.equals("..")) {
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        continue;
      }
      stack.add(part);
    }
    if (stack.isEmpty()) {
      return "/";
    }
    StringBuilder sb = new StringBuilder();
    for (String part : stack) {
      sb.append('/').append(part);
    }
    return sb.toString();
  }

  private static String encode(String pathname) {
    StringBuilder sb = new StringBuilder();
    byte[] bytes = pathname.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    for (byte b : bytes) {
      int c = b & 0xff;
      boolean legal = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
          || "-_.!~*'()/;:@&=+$,".indexOf(c) >= 0;
      if (legal) {
        sb.append((char) c);
      } else {
        sb.append('%').append(Character.toUpperCase(Character.forDigit(c >> 4, 16)))
            .append(Character.toUpperCase(Character.forDigit(c & 0xf, 16)));
      }
    }
    return sb.toString();
  }

  public URI toURI() {
    try {
      File f = getAbsoluteFile();
      String sp = f.getPath();
      if (!sp.startsWith("/")) {
        sp = "/" + sp;
      }
      if (!sp.endsWith("/") && f.isDirectory()) {
        sp = sp + "/";
      }
      if (sp.startsWith("//")) {
        sp = "//" + sp;
      }
      return new URI("file:" + encode(sp));
    } catch (URISyntaxException x) {
      throw new Error(x);
    }
  }

  @Deprecated
  public URL toURL() throws java.net.MalformedURLException {
    if (isInvalid()) {
      throw new java.net.MalformedURLException("Invalid file path");
    }
    return toURI().toURL();
  }

  public Path toPath() {
    return FileSystems.getDefault().getPath(path);
  }

  final boolean isInvalid() {
    return path.indexOf('\u0000') >= 0;
  }

  private long[] stat() {
    if (isInvalid()) {
      return null;
    }
    long[] out = new long[4];
    return FileNative.stat(path, out, true) == 0 ? out : null;
  }

  public boolean canRead() {
    return !isInvalid() && FileNative.access(path, FileNative.R_OK) == 0;
  }

  public boolean canWrite() {
    return !isInvalid() && FileNative.access(path, FileNative.W_OK) == 0;
  }

  public boolean canExecute() {
    return !isInvalid() && FileNative.access(path, FileNative.X_OK) == 0;
  }

  public boolean exists() {
    return stat() != null;
  }

  public boolean isDirectory() {
    long[] st = stat();
    return st != null && (st[0] & FileNative.S_IFMT) == FileNative.S_IFDIR;
  }

  public boolean isFile() {
    long[] st = stat();
    return st != null && (st[0] & FileNative.S_IFMT) == FileNative.S_IFREG;
  }

  public boolean isHidden() {
    return getName().startsWith(".");
  }

  public long lastModified() {
    long[] st = stat();
    return st == null ? 0L : st[2];
  }

  public long length() {
    long[] st = stat();
    return st == null ? 0L : st[1];
  }

  public boolean createNewFile() throws IOException {
    if (isInvalid()) {
      throw new IOException("Invalid file path");
    }
    int fd = FileNative.open(path, FileNative.WRITE | FileNative.CREATE_NEW, 0666);
    if (fd >= 0) {
      FileNative.close(fd);
      return true;
    }
    if (fd == -FileNative.EEXIST) {
      return false;
    }
    throw new IOException(FileNative.message(fd));
  }

  public boolean delete() {
    if (isInvalid()) {
      return false;
    }
    long[] st = new long[4];
    if (FileNative.stat(path, st, false) != 0) {
      return false;
    }
    if ((st[0] & FileNative.S_IFMT) == FileNative.S_IFDIR) {
      return FileNative.rmdir(path) == 0;
    }
    return FileNative.unlink(path) == 0;
  }

  private static Set<String> deleteOnExitFiles;

  public void deleteOnExit() {
    if (isInvalid()) {
      return;
    }
    synchronized (File.class) {
      if (deleteOnExitFiles == null) {
        deleteOnExitFiles = new LinkedHashSet<String>();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
          public void run() {
            List<String> files;
            synchronized (File.class) {
              files = new ArrayList<String>(deleteOnExitFiles);
            }
            Collections.reverse(files);
            for (String f : files) {
              new File(f).delete();
            }
          }
        }, "File-DeleteOnExit"));
      }
      deleteOnExitFiles.add(path);
    }
  }

  public String[] list() {
    if (isInvalid()) {
      return null;
    }
    return FileNative.list(path);
  }

  public String[] list(FilenameFilter filter) {
    String[] names = list();
    if (names == null || filter == null) {
      return names;
    }
    List<String> v = new ArrayList<String>();
    for (int i = 0; i < names.length; i++) {
      if (filter.accept(this, names[i])) {
        v.add(names[i]);
      }
    }
    return v.toArray(new String[v.size()]);
  }

  public File[] listFiles() {
    String[] ss = list();
    if (ss == null) {
      return null;
    }
    int n = ss.length;
    File[] fs = new File[n];
    for (int i = 0; i < n; i++) {
      fs[i] = new File(this, ss[i]);
    }
    return fs;
  }

  public File[] listFiles(FilenameFilter filter) {
    String[] ss = list();
    if (ss == null) {
      return null;
    }
    List<File> files = new ArrayList<File>();
    for (String s : ss) {
      if (filter == null || filter.accept(this, s)) {
        files.add(new File(this, s));
      }
    }
    return files.toArray(new File[files.size()]);
  }

  public File[] listFiles(FileFilter filter) {
    String[] ss = list();
    if (ss == null) {
      return null;
    }
    List<File> files = new ArrayList<File>();
    for (String s : ss) {
      File f = new File(this, s);
      if (filter == null || filter.accept(f)) {
        files.add(f);
      }
    }
    return files.toArray(new File[files.size()]);
  }

  public boolean mkdir() {
    if (isInvalid()) {
      return false;
    }
    return FileNative.mkdir(path, 0777) == 0;
  }

  public boolean mkdirs() {
    if (exists()) {
      return false;
    }
    if (mkdir()) {
      return true;
    }
    File canonFile;
    try {
      canonFile = getCanonicalFile();
    } catch (IOException e) {
      return false;
    }
    File parent = canonFile.getParentFile();
    return parent != null && (parent.mkdirs() || parent.exists()) && canonFile.mkdir();
  }

  public boolean renameTo(File dest) {
    if (dest == null) {
      throw new NullPointerException();
    }
    if (this.isInvalid() || dest.isInvalid()) {
      return false;
    }
    return FileNative.rename(path, dest.path) == 0;
  }

  public boolean setLastModified(long time) {
    if (time < 0) {
      throw new IllegalArgumentException("Negative time");
    }
    if (isInvalid()) {
      return false;
    }
    return FileNative.setModified(path, time) == 0;
  }

  private boolean setPermission(int access, boolean enable, boolean ownerOnly) {
    long[] st = stat();
    if (st == null) {
      return false;
    }
    int mask = ownerOnly ? (access << 6) : ((access << 6) | (access << 3) | access);
    int mode = (int) (st[0] & 07777);
    mode = enable ? (mode | mask) : (mode & ~mask);
    return FileNative.chmod(path, mode) == 0;
  }

  public boolean setReadOnly() {
    long[] st = stat();
    if (st == null) {
      return false;
    }
    int mode = (int) (st[0] & 07777) & ~0222;
    return FileNative.chmod(path, mode) == 0;
  }

  public boolean setWritable(boolean writable, boolean ownerOnly) {
    return setPermission(kPermissionWrite, writable, ownerOnly);
  }

  public boolean setWritable(boolean writable) {
    return setWritable(writable, true);
  }

  public boolean setReadable(boolean readable, boolean ownerOnly) {
    return setPermission(kPermissionRead, readable, ownerOnly);
  }

  public boolean setReadable(boolean readable) {
    return setReadable(readable, true);
  }

  public boolean setExecutable(boolean executable, boolean ownerOnly) {
    return setPermission(kPermissionExecute, executable, ownerOnly);
  }

  public boolean setExecutable(boolean executable) {
    return setExecutable(executable, true);
  }

  public static File[] listRoots() {
    return new File[] {new File("/")};
  }

  private long[] space() {
    if (isInvalid()) {
      return null;
    }
    long[] out = new long[3];
    return FileNative.space(path, out) == 0 ? out : null;
  }

  public long getTotalSpace() {
    long[] s = space();
    return s == null ? 0L : s[0];
  }

  public long getFreeSpace() {
    long[] s = space();
    return s == null ? 0L : s[1];
  }

  public long getUsableSpace() {
    long[] s = space();
    return s == null ? 0L : s[2];
  }

  private static final class TempDirectory {
    private TempDirectory() { }

    static final File kTmpdir = new File(System.getProperty("java.io.tmpdir", "/tmp"));
    static final SecureRandom kRandom = new SecureRandom();

    static File generateFile(String prefix, String suffix, File dir) throws IOException {
      long n = kRandom.nextLong();
      String nus = Long.toUnsignedString(n);
      String name = prefix + nus + suffix;
      File f = new File(dir, name);
      if (!name.equals(f.getName()) || f.isInvalid()) {
        throw new IOException("Unable to create temporary file");
      }
      return f;
    }
  }

  public static File createTempFile(String prefix, String suffix, File directory) throws IOException {
    if (prefix.length() < 3) {
      throw new IllegalArgumentException("Prefix string \"" + prefix + "\" too short: length must be at least 3");
    }
    if (suffix == null) {
      suffix = ".tmp";
    }
    File tmpdir = (directory != null) ? directory : TempDirectory.kTmpdir;
    File f;
    do {
      f = TempDirectory.generateFile(prefix, suffix, tmpdir);
    } while (!f.createNewFile0());
    return f;
  }

  public static File createTempFile(String prefix, String suffix) throws IOException {
    return createTempFile(prefix, suffix, null);
  }

  private boolean createNewFile0() throws IOException {
    try {
      return createNewFile();
    } catch (IOException e) {
      if (!getParentFile().exists()) {
        throw e;
      }
      throw e;
    }
  }

  public int compareTo(File pathname) {
    return path.compareTo(pathname.path);
  }

  public boolean equals(Object obj) {
    if (obj instanceof File) {
      return compareTo((File) obj) == 0;
    }
    return false;
  }

  public int hashCode() {
    return path.hashCode() ^ 1234321;
  }

  public String toString() {
    return getPath();
  }

  private synchronized void writeObject(ObjectOutputStream s) throws IOException {
    s.defaultWriteObject();
    s.writeChar(separatorChar);
  }

  private synchronized void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
    ObjectInputStream.GetField fields = s.readFields();
    String pathField = (String) fields.get("path", null);
    char sep = s.readChar();
    if (sep != separatorChar) {
      pathField = pathField.replace(sep, separatorChar);
    }
    String normalized = normalize(pathField);
    try {
      java.lang.reflect.Field f = File.class.getDeclaredField("path");
      f.setAccessible(true);
      f.set(this, normalized);
    } catch (ReflectiveOperationException e) {
      throw new InternalError(e);
    }
    prefixLength = normalized.startsWith("/") ? 1 : 0;
  }
}
