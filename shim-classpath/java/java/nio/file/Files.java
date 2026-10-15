/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.nio.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Files {
  private static final int BUFFER_SIZE = 8192;

  private Files() {}

  public static InputStream newInputStream(Path path, OpenOption... options) throws IOException {
    Objects.requireNonNull(path, "path");
    File f = path.toFile();
    if (!f.exists()) {
      throw new NoSuchFileException(path.toString());
    }
    if (f.isDirectory()) {
      throw new FileSystemException(path.toString(), null, "Is a directory");
    }
    return new FileInputStream(f);
  }

  public static OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
    Objects.requireNonNull(path, "path");
    boolean append = false;
    boolean create = true;
    boolean createNew = false;
    boolean truncate = false;
    for (OpenOption opt : options) {
      if (opt == StandardOpenOption.APPEND) append = true;
      if (opt == StandardOpenOption.CREATE) create = true;
      if (opt == StandardOpenOption.CREATE_NEW) createNew = true;
      if (opt == StandardOpenOption.TRUNCATE_EXISTING) truncate = true;
    }
    File f = path.toFile();
    if (createNew && f.exists()) {
      throw new FileAlreadyExistsException(path.toString());
    }
    if (!create && !createNew && !f.exists()) {
      throw new NoSuchFileException(path.toString());
    }
    return new FileOutputStream(f, append);
  }

  public static BufferedReader newBufferedReader(Path path, Charset cs) throws IOException {
    return new BufferedReader(new InputStreamReader(newInputStream(path), cs));
  }

  public static BufferedReader newBufferedReader(Path path) throws IOException {
    return newBufferedReader(path, StandardCharsets.UTF_8);
  }

  public static BufferedWriter newBufferedWriter(Path path, Charset cs, OpenOption... options) throws IOException {
    return new BufferedWriter(new OutputStreamWriter(newOutputStream(path, options), cs));
  }

  public static BufferedWriter newBufferedWriter(Path path, OpenOption... options) throws IOException {
    return newBufferedWriter(path, StandardCharsets.UTF_8, options);
  }

  public static Path createFile(Path path, FileAttribute<?>... attrs) throws IOException {
    Objects.requireNonNull(path, "path");
    File f = path.toFile();
    if (f.exists()) {
      throw new FileAlreadyExistsException(path.toString());
    }
    if (!f.createNewFile()) {
      throw new FileAlreadyExistsException(path.toString());
    }
    return path;
  }

  public static Path createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
    Objects.requireNonNull(dir, "dir");
    File f = dir.toFile();
    if (f.exists()) {
      throw new FileAlreadyExistsException(dir.toString());
    }
    if (!f.mkdir()) {
      throw new FileSystemException(dir.toString(), null, "Failed to create directory");
    }
    return dir;
  }

  public static Path createDirectories(Path dir, FileAttribute<?>... attrs) throws IOException {
    Objects.requireNonNull(dir, "dir");
    File f = dir.toFile();
    if (f.exists()) {
      if (!f.isDirectory()) {
        throw new FileAlreadyExistsException(dir.toString());
      }
      return dir;
    }
    f.mkdirs();
    if (!f.exists() || !f.isDirectory()) {
      throw new FileSystemException(dir.toString(), null, "Failed to create directories");
    }
    return dir;
  }

  public static Path createTempFile(String prefix, String suffix, FileAttribute<?>... attrs) throws IOException {
    File f = File.createTempFile(prefix, suffix);
    return f.toPath();
  }

  public static Path createTempFile(Path dir, String prefix, String suffix, FileAttribute<?>... attrs) throws IOException {
    File parent = (dir != null) ? dir.toFile() : null;
    File f = File.createTempFile(prefix, suffix, parent);
    return f.toPath();
  }

  public static Path createTempDirectory(String prefix, FileAttribute<?>... attrs) throws IOException {
    File f = File.createTempFile(prefix, ".tmp");
    f.delete();
    f.mkdir();
    return f.toPath();
  }

  public static void delete(Path path) throws IOException {
    Objects.requireNonNull(path, "path");
    File f = path.toFile();
    if (!f.exists()) {
      throw new NoSuchFileException(path.toString());
    }
    if (f.isDirectory()) {
      String[] children = f.list();
      if (children != null && children.length > 0) {
        throw new DirectoryNotEmptyException(path.toString());
      }
    }
    if (!f.delete()) {
      throw new FileSystemException(path.toString(), null, "Deletion failed");
    }
  }

  public static boolean deleteIfExists(Path path) throws IOException {
    Objects.requireNonNull(path, "path");
    File f = path.toFile();
    if (!f.exists()) {
      return false;
    }
    delete(path);
    return true;
  }

  public static Path copy(Path source, Path target, CopyOption... options) throws IOException {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(target, "target");
    boolean replaceExisting = false;
    for (CopyOption opt : options) {
      if (opt == StandardCopyOption.REPLACE_EXISTING) replaceExisting = true;
    }
    File srcFile = source.toFile();
    File dstFile = target.toFile();
    if (!srcFile.exists()) {
      throw new NoSuchFileException(source.toString());
    }
    if (dstFile.exists()) {
      if (!replaceExisting) {
        throw new FileAlreadyExistsException(target.toString());
      }
      dstFile.delete();
    }
    try (InputStream in = new FileInputStream(srcFile);
         OutputStream out = new FileOutputStream(dstFile)) {
      byte[] buf = new byte[BUFFER_SIZE];
      int n;
      while ((n = in.read(buf)) >= 0) {
        out.write(buf, 0, n);
      }
    }
    return target;
  }

  public static long copy(InputStream in, Path target, CopyOption... options) throws IOException {
    Objects.requireNonNull(in, "in");
    Objects.requireNonNull(target, "target");
    boolean replaceExisting = false;
    for (CopyOption opt : options) {
      if (opt == StandardCopyOption.REPLACE_EXISTING) replaceExisting = true;
    }
    File dstFile = target.toFile();
    if (dstFile.exists()) {
      if (!replaceExisting) {
        throw new FileAlreadyExistsException(target.toString());
      }
      dstFile.delete();
    }
    long count = 0;
    try (OutputStream out = new FileOutputStream(dstFile)) {
      byte[] buf = new byte[BUFFER_SIZE];
      int n;
      while ((n = in.read(buf)) >= 0) {
        out.write(buf, 0, n);
        count += n;
      }
    }
    return count;
  }

  public static long copy(Path source, OutputStream out) throws IOException {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(out, "out");
    File srcFile = source.toFile();
    if (!srcFile.exists()) {
      throw new NoSuchFileException(source.toString());
    }
    long count = 0;
    try (InputStream in = new FileInputStream(srcFile)) {
      byte[] buf = new byte[BUFFER_SIZE];
      int n;
      while ((n = in.read(buf)) >= 0) {
        out.write(buf, 0, n);
        count += n;
      }
    }
    return count;
  }

  public static Path move(Path source, Path target, CopyOption... options) throws IOException {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(target, "target");
    boolean replaceExisting = false;
    for (CopyOption opt : options) {
      if (opt == StandardCopyOption.REPLACE_EXISTING) replaceExisting = true;
    }
    File srcFile = source.toFile();
    File dstFile = target.toFile();
    if (!srcFile.exists()) {
      throw new NoSuchFileException(source.toString());
    }
    if (dstFile.exists()) {
      if (!replaceExisting) {
        throw new FileAlreadyExistsException(target.toString());
      }
      dstFile.delete();
    }
    if (srcFile.renameTo(dstFile)) {
      return target;
    }
    copy(source, target, options);
    delete(source);
    return target;
  }

  public static boolean exists(Path path, LinkOption... options) {
    Objects.requireNonNull(path, "path");
    return path.toFile().exists();
  }

  public static boolean notExists(Path path, LinkOption... options) {
    return !exists(path, options);
  }

  public static boolean isDirectory(Path path, LinkOption... options) {
    Objects.requireNonNull(path, "path");
    return path.toFile().isDirectory();
  }

  public static boolean isRegularFile(Path path, LinkOption... options) {
    Objects.requireNonNull(path, "path");
    return path.toFile().isFile();
  }

  public static boolean isReadable(Path path) {
    Objects.requireNonNull(path, "path");
    return path.toFile().canRead();
  }

  public static boolean isWritable(Path path) {
    Objects.requireNonNull(path, "path");
    return path.toFile().canWrite();
  }

  public static boolean isExecutable(Path path) {
    Objects.requireNonNull(path, "path");
    return path.toFile().canExecute();
  }

  public static boolean isSameFile(Path path, Path path2) throws IOException {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(path2, "path2");
    if (path.equals(path2)) {
      return true;
    }
    return path.toRealPath().equals(path2.toRealPath());
  }

  public static long size(Path path) throws IOException {
    Objects.requireNonNull(path, "path");
    File f = path.toFile();
    if (!f.exists()) {
      throw new NoSuchFileException(path.toString());
    }
    return f.length();
  }

  public static FileTime getLastModifiedTime(Path path, LinkOption... options) throws IOException {
    Objects.requireNonNull(path, "path");
    File f = path.toFile();
    if (!f.exists()) {
      throw new NoSuchFileException(path.toString());
    }
    return FileTime.fromMillis(f.lastModified());
  }

  public static <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(type, "type");
    if (type == BasicFileAttributes.class) {
      File f = path.toFile();
      if (!f.exists()) {
        throw new NoSuchFileException(path.toString());
      }
      return (A) new DefaultBasicFileAttributes(f);
    }
    throw new UnsupportedOperationException("Unsupported attributes type: " + type);
  }

  public static byte[] readAllBytes(Path path) throws IOException {
    Objects.requireNonNull(path, "path");
    File f = path.toFile();
    if (!f.exists()) {
      throw new NoSuchFileException(path.toString());
    }
    long len = f.length();
    if (len > Integer.MAX_VALUE - 8) {
      throw new OutOfMemoryError("File size exceeds max array size");
    }
    try (InputStream in = new FileInputStream(f)) {
      if (len > 0) {
        byte[] bytes = new byte[(int) len];
        int read = 0;
        while (read < len) {
          int n = in.read(bytes, read, (int) len - read);
          if (n < 0) break;
          read += n;
        }
        return bytes;
      } else {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buf)) >= 0) {
          baos.write(buf, 0, n);
        }
        return baos.toByteArray();
      }
    }
  }

  public static String readString(Path path) throws IOException {
    return readString(path, StandardCharsets.UTF_8);
  }

  public static String readString(Path path, Charset cs) throws IOException {
    return new String(readAllBytes(path), cs);
  }

  public static List<String> readAllLines(Path path, Charset cs) throws IOException {
    try (BufferedReader reader = newBufferedReader(path, cs)) {
      List<String> list = new ArrayList<String>();
      String line;
      while ((line = reader.readLine()) != null) {
        list.add(line);
      }
      return list;
    }
  }

  public static List<String> readAllLines(Path path) throws IOException {
    return readAllLines(path, StandardCharsets.UTF_8);
  }

  public static Path write(Path path, byte[] bytes, OpenOption... options) throws IOException {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(bytes, "bytes");
    try (OutputStream out = newOutputStream(path, options)) {
      out.write(bytes);
    }
    return path;
  }

  public static Path write(Path path, Iterable<? extends CharSequence> lines, Charset cs, OpenOption... options) throws IOException {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(lines, "lines");
    Objects.requireNonNull(cs, "cs");
    try (BufferedWriter writer = newBufferedWriter(path, cs, options)) {
      for (CharSequence line : lines) {
        writer.append(line);
        writer.newLine();
      }
    }
    return path;
  }

  public static Path write(Path path, Iterable<? extends CharSequence> lines, OpenOption... options) throws IOException {
    return write(path, lines, StandardCharsets.UTF_8, options);
  }

  public static Path writeString(Path path, CharSequence csq, OpenOption... options) throws IOException {
    return writeString(path, csq, StandardCharsets.UTF_8, options);
  }

  public static Path writeString(Path path, CharSequence csq, Charset cs, OpenOption... options) throws IOException {
    Objects.requireNonNull(csq, "csq");
    return write(path, csq.toString().getBytes(cs), options);
  }

  public static Stream<Path> list(Path dir) throws IOException {
    Objects.requireNonNull(dir, "dir");
    File f = dir.toFile();
    if (!f.exists()) {
      throw new NoSuchFileException(dir.toString());
    }
    if (!f.isDirectory()) {
      throw new NotDirectoryException(dir.toString());
    }
    String[] names = f.list();
    if (names == null) {
      return Stream.empty();
    }
    List<Path> paths = new ArrayList<Path>(names.length);
    for (String name : names) {
      paths.add(dir.resolve(name));
    }
    return paths.stream();
  }

  public static Stream<Path> walk(Path start, int maxDepth, FileVisitOption... options) throws IOException {
    Objects.requireNonNull(start, "start");
    if (maxDepth < 0) {
      throw new IllegalArgumentException("maxDepth must be >= 0");
    }
    List<Path> result = new ArrayList<Path>();
    collectWalk(start, 0, maxDepth, result);
    return result.stream();
  }

  private static void collectWalk(Path current, int depth, int maxDepth, List<Path> result) {
    result.add(current);
    if (depth >= maxDepth) {
      return;
    }
    File f = current.toFile();
    if (f.isDirectory()) {
      String[] names = f.list();
      if (names != null) {
        for (String name : names) {
          collectWalk(current.resolve(name), depth + 1, maxDepth, result);
        }
      }
    }
  }

  public static Stream<Path> walk(Path start, FileVisitOption... options) throws IOException {
    return walk(start, Integer.MAX_VALUE, options);
  }

  public static Stream<String> lines(Path path, Charset cs) throws IOException {
    return newBufferedReader(path, cs).lines();
  }

  public static Stream<String> lines(Path path) throws IOException {
    return lines(path, StandardCharsets.UTF_8);
  }

  public static DirectoryStream<Path> newDirectoryStream(Path dir) throws IOException {
    return newDirectoryStream(dir, entry -> true);
  }

  public static DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
    Objects.requireNonNull(dir, "dir");
    Objects.requireNonNull(filter, "filter");
    File f = dir.toFile();
    if (!f.exists()) {
      throw new NoSuchFileException(dir.toString());
    }
    if (!f.isDirectory()) {
      throw new NotDirectoryException(dir.toString());
    }
    String[] names = f.list();
    List<Path> entries = new ArrayList<Path>();
    if (names != null) {
      for (String name : names) {
        Path p = dir.resolve(name);
        if (filter.accept(p)) {
          entries.add(p);
        }
      }
    }
    return new SimpleDirectoryStream(entries);
  }

  public static Path walkFileTree(Path start, FileVisitor<? super Path> visitor) throws IOException {
    Objects.requireNonNull(start, "start");
    Objects.requireNonNull(visitor, "visitor");
    walkFileTreeInternal(start, visitor);
    return start;
  }

  private static void walkFileTreeInternal(Path current, FileVisitor<? super Path> visitor) throws IOException {
    File f = current.toFile();
    BasicFileAttributes attrs = new DefaultBasicFileAttributes(f);
    if (attrs.isDirectory()) {
      FileVisitResult pre = visitor.preVisitDirectory(current, attrs);
      if (pre == FileVisitResult.TERMINATE) return;
      if (pre == FileVisitResult.CONTINUE) {
        IOException exc = null;
        try {
          String[] names = f.list();
          if (names != null) {
            for (String name : names) {
              walkFileTreeInternal(current.resolve(name), visitor);
            }
          }
        } catch (IOException e) {
          exc = e;
        }
        visitor.postVisitDirectory(current, exc);
      }
    } else {
      visitor.visitFile(current, attrs);
    }
  }

  private static final class SimpleDirectoryStream implements DirectoryStream<Path> {
    private final List<Path> entries;
    private boolean closed = false;

    SimpleDirectoryStream(List<Path> entries) {
      this.entries = entries;
    }

    @Override
    public Iterator<Path> iterator() {
      if (closed) {
        throw new IllegalStateException("Stream is closed");
      }
      return entries.iterator();
    }

    @Override
    public void close() throws IOException {
      closed = true;
    }
  }

  private static final class DefaultBasicFileAttributes implements BasicFileAttributes {
    private final File file;

    DefaultBasicFileAttributes(File file) {
      this.file = file;
    }

    @Override
    public FileTime lastModifiedTime() {
      return FileTime.fromMillis(file.lastModified());
    }

    @Override
    public FileTime lastAccessTime() {
      return lastModifiedTime();
    }

    @Override
    public FileTime creationTime() {
      return lastModifiedTime();
    }

    @Override
    public boolean isRegularFile() {
      return file.isFile();
    }

    @Override
    public boolean isDirectory() {
      return file.isDirectory();
    }

    @Override
    public boolean isSymbolicLink() {
      return false;
    }

    @Override
    public boolean isOther() {
      return false;
    }

    @Override
    public long size() {
      return file.length();
    }

    @Override
    public Object fileKey() {
      return null;
    }
  }
}
