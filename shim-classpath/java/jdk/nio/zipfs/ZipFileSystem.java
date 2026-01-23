/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.nio.zipfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessMode;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

class ZipFileSystem extends FileSystem {
  static final class Node {
    String path;
    boolean dir;
    boolean implicit;
    byte[] data;
    ZipEntry source;
    FileTime mtime;
    FileTime atime;
    FileTime ctime;
    long crc = -1;
    int method = ZipEntry.DEFLATED;
    long csize = -1;
    String comment;
    byte[] extra;
    List<Node> children;
    int seq = -1;

    Node(String path, boolean dir) {
      this.path = path;
      this.dir = dir;
      if (dir) {
        children = new ArrayList<Node>();
      }
    }

    long size() {
      if (data != null) {
        return data.length;
      }
      return source != null ? source.getSize() : 0;
    }

    long compressedSize() {
      if (data != null) {
        return -1;
      }
      return source != null ? source.getCompressedSize() : 0;
    }

    long crc() {
      return source != null && data == null ? source.getCrc() : crc;
    }

    int method() {
      return source != null && data == null ? source.getMethod() : method;
    }

    FileTime lastModifiedTime() {
      return mtime;
    }

    FileTime lastAccessTime() {
      return atime != null ? atime : mtime;
    }

    FileTime creationTime() {
      return ctime != null ? ctime : mtime;
    }
  }

  private final ZipFileSystemProvider provider;
  private final Path zfpath;
  private final boolean readOnly;
  private final boolean noCompression;
  private final int compressionMethod;
  private final byte[] defaultDir = new byte[] {'/'};
  private final Map<String, Node> nodes = new LinkedHashMap<String, Node>();
  private final Node root;
  private ZipFile zipFile;
  private boolean modified;
  private volatile boolean isOpen = true;
  private final Object lock = new Object();
  private final FileTime zipTime;
  private int nextSeq;

  ZipFileSystem(ZipFileSystemProvider provider, Path zfpath, Map<String, ?> env) throws IOException {
    String accessMode = (String) env.get("accessMode");
    boolean forceReadOnly = false;
    if (accessMode != null) {
      if (accessMode.equals("readOnly")) {
        forceReadOnly = true;
      } else if (!accessMode.equals("readWrite")) {
        throw new IllegalArgumentException("Unknown file system access mode: " + accessMode);
      }
    }
    boolean createNew = isTrue(env, "create");
    this.noCompression = isTrue(env, "noCompression");
    Object cm = env.get("compressionMethod");
    if (cm != null) {
      if ("STORED".equals(cm)) {
        compressionMethod = ZipEntry.STORED;
      } else if ("DEFLATED".equals(cm)) {
        compressionMethod = ZipEntry.DEFLATED;
      } else {
        throw new IllegalArgumentException("The value for the compressionMethod property must be STORED or DEFLATED");
      }
    } else {
      compressionMethod = noCompression ? ZipEntry.STORED : ZipEntry.DEFLATED;
    }
    this.provider = provider;
    this.zfpath = zfpath;
    if (Files.notExists(zfpath)) {
      if (createNew) {
        try (OutputStream os = Files.newOutputStream(zfpath, StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE)) {
          os.write(new byte[] {0x50, 0x4b, 0x05, 0x06, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        }
      } else {
        throw new NoSuchFileException(zfpath.toString());
      }
    }
    zfpath.getFileSystem().provider().checkAccess(zfpath, AccessMode.READ);
    boolean writeable = Files.isWritable(zfpath);
    this.readOnly = forceReadOnly || !writeable;
    this.zipTime = Files.getLastModifiedTime(zfpath);
    root = new Node("/", true);
    root.implicit = true;
    root.mtime = zipTime;
    nodes.put("/", root);
    try {
      zipFile = new ZipFile(zfpath.toFile());
    } catch (IOException e) {
      if (e instanceof ZipException) {
        throw e;
      }
      throw e;
    }
    java.util.Enumeration<? extends ZipEntry> en = zipFile.entries();
    while (en.hasMoreElements()) {
      ZipEntry e = en.nextElement();
      String name = e.getName();
      boolean dir = name.endsWith("/");
      String p = "/" + (dir ? name.substring(0, name.length() - 1) : name);
      p = ZipPath.normalize(p);
      if (p.equals("/")) {
        continue;
      }
      Node n = new Node(p, dir);
      n.source = e;
      n.mtime = e.getLastModifiedTime();
      n.atime = e.getLastAccessTime();
      n.ctime = e.getCreationTime();
      n.comment = e.getComment();
      n.extra = e.getExtra();
      n.seq = nextSeq++;
      addNode(n);
    }
  }

  private static boolean isTrue(Map<String, ?> env, String name) {
    return "true".equals(env.get(name)) || Boolean.TRUE.equals(env.get(name));
  }

  private void addNode(Node n) {
    Node existing = nodes.get(n.path);
    if (existing != null) {
      if (existing.implicit && n.dir) {
        existing.implicit = false;
        existing.source = n.source;
        existing.mtime = n.mtime;
        existing.atime = n.atime;
        existing.ctime = n.ctime;
        existing.comment = n.comment;
        existing.extra = n.extra;
        existing.seq = n.seq;
      }
      return;
    }
    String parentPath = parentOf(n.path);
    Node parent = nodes.get(parentPath);
    if (parent == null) {
      parent = new Node(parentPath, true);
      parent.implicit = true;
      parent.mtime = zipTime;
      addNode(parent);
    }
    nodes.put(n.path, n);
    parent.children.add(n);
  }

  private static String parentOf(String p) {
    int i = p.lastIndexOf('/');
    return i <= 0 ? "/" : p.substring(0, i);
  }

  @Override
  public FileSystemProvider provider() {
    return provider;
  }

  @Override
  public String getSeparator() {
    return "/";
  }

  @Override
  public boolean isOpen() {
    return isOpen;
  }

  @Override
  public boolean isReadOnly() {
    return readOnly;
  }

  private void checkOpen() throws IOException {
    if (!isOpen) {
      throw new ClosedFileSystemException();
    }
  }

  private void checkWritable() {
    if (readOnly) {
      throw new ReadOnlyFileSystemException();
    }
  }

  @Override
  public void close() throws IOException {
    synchronized (lock) {
      if (!isOpen) {
        return;
      }
      isOpen = false;
    }
    try {
      if (modified && !readOnly) {
        sync();
      }
    } finally {
      if (zipFile != null) {
        zipFile.close();
      }
      provider.removeFileSystem(zfpath, this);
    }
  }

  @Override
  public Iterable<Path> getRootDirectories() {
    List<Path> list = new ArrayList<Path>();
    list.add(new ZipPath(this, "/", true));
    return Collections.unmodifiableList(list);
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    List<FileStore> list = new ArrayList<FileStore>();
    list.add(new ZipFileStore(new ZipPath(this, "/", true)));
    return Collections.unmodifiableList(list);
  }

  private static final Set<String> kViews = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("basic",
      "zip")));

  @Override
  public Set<String> supportedFileAttributeViews() {
    return kViews;
  }

  @Override
  public Path getPath(String first, String... more) {
    String path;
    if (more.length == 0) {
      path = first;
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append(first);
      for (String segment : more) {
        if (segment.length() > 0) {
          if (sb.length() > 0) {
            sb.append('/');
          }
          sb.append(segment);
        }
      }
      path = sb.toString();
    }
    return new ZipPath(this, path);
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    throw new UnsupportedOperationException();
  }

  @Override
  public WatchService newWatchService() {
    throw new UnsupportedOperationException();
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndInput) {
    int pos = syntaxAndInput.indexOf(':');
    if (pos <= 0) {
      throw new IllegalArgumentException();
    }
    String syntax = syntaxAndInput.substring(0, pos);
    String input = syntaxAndInput.substring(pos + 1);
    String expr;
    if (syntax.equalsIgnoreCase("glob")) {
      expr = sun.nio.fs.Globs.toUnixRegexPattern(input);
    } else if (syntax.equalsIgnoreCase("regex")) {
      expr = input;
    } else {
      throw new UnsupportedOperationException("Syntax '" + syntax + "' not recognized");
    }
    final Pattern pattern = Pattern.compile(expr);
    return new PathMatcher() {
      @Override
      public boolean matches(Path path) {
        return pattern.matcher(path.toString()).matches();
      }
    };
  }

  static String decodeUri(String s) {
    return s;
  }

  Path getZipFile() {
    return zfpath;
  }

  byte[] getDefaultDir() {
    return defaultDir;
  }

  private String resolve(ZipPath path) {
    String p = path.toAbsolutePath().getPathString();
    if (p.length() > 1 && (p.contains("/./") || p.contains("/../") || p.endsWith("/.") || p.endsWith("/.."))) {
      p = ((ZipPath) path.normalize()).toAbsolutePath().getPathString();
    }
    return p;
  }

  boolean exists(ZipPath path) throws IOException {
    synchronized (lock) {
      checkOpen();
      return nodes.containsKey(resolve(path));
    }
  }

  private Node getNode(ZipPath path) throws IOException {
    return nodes.get(resolve(path));
  }

  ZipFileAttributes getFileAttributes(ZipPath path) throws IOException {
    synchronized (lock) {
      checkOpen();
      Node n = getNode(path);
      if (n == null) {
        throw new NoSuchFileException(path.toString());
      }
      return new ZipFileAttributes(n);
    }
  }

  void checkAccess(ZipPath path, AccessMode... modes) throws IOException {
    synchronized (lock) {
      checkOpen();
      Node n = getNode(path);
      if (n == null) {
        throw new NoSuchFileException(zfpath.toString());
      }
      for (AccessMode mode : modes) {
        switch (mode) {
        case READ:
          break;
        case WRITE:
          if (readOnly) {
            throw new java.nio.file.AccessDeniedException(path.toString());
          }
          break;
        case EXECUTE:
          throw new java.nio.file.AccessDeniedException(path.toString());
        default:
          throw new UnsupportedOperationException();
        }
      }
    }
  }

  void setTimes(ZipPath path, FileTime mtime, FileTime atime, FileTime ctime) throws IOException {
    synchronized (lock) {
      checkOpen();
      checkWritable();
      Node n = getNode(path);
      if (n == null) {
        throw new NoSuchFileException(path.toString());
      }
      if (mtime != null) {
        n.mtime = mtime;
      }
      if (atime != null) {
        n.atime = atime;
      }
      if (ctime != null) {
        n.ctime = ctime;
      }
      materialize(n);
      modified = true;
    }
  }

  private void materialize(Node n) throws IOException {
    if (!n.dir && n.data == null) {
      n.data = readSource(n);
    }
    n.implicit = false;
  }

  private byte[] readSource(Node n) throws IOException {
    if (n.source == null) {
      return new byte[0];
    }
    try (InputStream in = zipFile.getInputStream(n.source)) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream((int) Math.max(32, n.source.getSize()));
      byte[] buf = new byte[8192];
      int r;
      while ((r = in.read(buf)) > 0) {
        bos.write(buf, 0, r);
      }
      return bos.toByteArray();
    }
  }

  FileAttributeView getView(ZipPath path, String type) {
    if (type == null) {
      throw new NullPointerException();
    }
    if (type.equals("basic")) {
      return new ZipFileAttributeView(path, false);
    }
    if (type.equals("zip")) {
      return new ZipFileAttributeView(path, true);
    }
    return null;
  }

  Iterator<Path> iteratorOf(ZipPath dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
    synchronized (lock) {
      checkOpen();
      Node n = getNode(dir);
      if (n == null || !n.dir) {
        throw new NotDirectoryException(dir.toString());
      }
      List<Path> list = new ArrayList<Path>();
      List<Node> kids = new ArrayList<Node>(n.children);
      Collections.reverse(kids);
      for (Node c : kids) {
        String cp = c.path;
        ZipPath child;
        if (dir.isAbsolute()) {
          child = new ZipPath(this, cp, true);
        } else {
          String d = dir.getPathString();
          String name = cp.substring(cp.lastIndexOf('/') + 1);
          child = new ZipPath(this, d.isEmpty() ? name : d + "/" + name, true);
        }
        if (filter == null || filter.accept(child)) {
          list.add(child);
        }
      }
      return list.iterator();
    }
  }

  void createDirectory(ZipPath dir, FileAttribute<?>... attrs) throws IOException {
    synchronized (lock) {
      checkOpen();
      checkWritable();
      String p = resolve(dir);
      if (nodes.containsKey(p)) {
        throw new FileAlreadyExistsException(dir.toString());
      }
      Node parent = nodes.get(parentOf(p));
      if (parent == null) {
        throw new NoSuchFileException(parentOf(p));
      }
      Node n = new Node(p, true);
      n.mtime = FileTime.fromMillis(System.currentTimeMillis());
      n.seq = nextSeq++;
      addNode(n);
      modified = true;
    }
  }

  void deleteFile(ZipPath path, boolean failIfNotExists) throws IOException {
    synchronized (lock) {
      checkOpen();
      checkWritable();
      String p = resolve(path);
      Node n = nodes.get(p);
      if (n == null) {
        if (p.equals("/")) {
          throw new ZipException("root directory </> can't not be delete");
        }
        if (failIfNotExists) {
          throw new NoSuchFileException(path.toString());
        }
        return;
      }
      if (n.dir && !n.children.isEmpty()) {
        throw new DirectoryNotEmptyException(path.toString());
      }
      if (p.equals("/")) {
        throw new ZipException("root directory </> can't not be delete");
      }
      Node parent = nodes.get(parentOf(p));
      parent.children.remove(n);
      nodes.remove(p);
      modified = true;
    }
  }

  InputStream newInputStream(ZipPath path) throws IOException {
    synchronized (lock) {
      checkOpen();
      Node n = getNode(path);
      if (n == null) {
        throw new NoSuchFileException(path.toString());
      }
      if (n.dir) {
        throw new NoSuchFileException(path.toString());
      }
      byte[] data = n.data != null ? n.data : readSource(n);
      return new ByteArrayInputStream(data);
    }
  }

  OutputStream newOutputStream(ZipPath path, OpenOption... options) throws IOException {
    if (options.length == 0) {
      return newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE);
    }
    Set<OpenOption> set = new HashSet<OpenOption>(Arrays.asList(options));
    set.add(StandardOpenOption.WRITE);
    final SeekableByteChannel ch = newByteChannel(path, set);
    return new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        ch.write(ByteBuffer.wrap(new byte[] {(byte) b}));
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        ch.write(ByteBuffer.wrap(b, off, len));
      }

      @Override
      public void close() throws IOException {
        ch.close();
      }
    };
  }

  SeekableByteChannel newByteChannel(ZipPath path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
      throws IOException {
    synchronized (lock) {
      checkOpen();
      for (OpenOption o : options) {
        if (o == null) {
          throw new NullPointerException();
        }
        if (o == StandardOpenOption.APPEND && options.contains(StandardOpenOption.TRUNCATE_EXISTING)) {
          throw new IllegalArgumentException("APPEND + TRUNCATE_EXISTING not allowed");
        }
      }
      boolean forWrite = options.contains(StandardOpenOption.WRITE) || options.contains(StandardOpenOption.APPEND);
      String p = resolve(path);
      Node n = nodes.get(p);
      if (forWrite) {
        checkWritable();
        boolean create = options.contains(StandardOpenOption.CREATE);
        boolean createNew = options.contains(StandardOpenOption.CREATE_NEW);
        if (n != null) {
          if (n.dir || createNew) {
            throw new FileAlreadyExistsException(path.toString());
          }
        } else {
          if (!create && !createNew) {
            throw new NoSuchFileException(path.toString());
          }
          if (nodes.get(parentOf(p)) == null) {
            throw new NoSuchFileException(parentOf(p));
          }
        }
        byte[] initial = new byte[0];
        if (n != null && options.contains(StandardOpenOption.APPEND)) {
          initial = n.data != null ? n.data : readSource(n);
        }
        return new WriteChannel(p, initial, options.contains(StandardOpenOption.APPEND));
      }
      if (n == null || n.dir) {
        throw new NoSuchFileException(path.toString());
      }
      byte[] data = n.data != null ? n.data : readSource(n);
      return new ReadChannel(data);
    }
  }

  private static final class ReadChannel implements SeekableByteChannel {
    private final byte[] data;
    private long pos;
    private boolean open = true;

    ReadChannel(byte[] data) {
      this.data = data;
    }

    @Override
    public boolean isOpen() {
      return open;
    }

    @Override
    public void close() {
      open = false;
    }

    private void ensureOpen() throws IOException {
      if (!open) {
        throw new java.nio.channels.ClosedChannelException();
      }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
      ensureOpen();
      if (pos >= data.length) {
        return -1;
      }
      int n = (int) Math.min(dst.remaining(), data.length - pos);
      dst.put(data, (int) pos, n);
      pos += n;
      return n;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
      ensureOpen();
      throw new NonWritableChannelException();
    }

    @Override
    public long position() throws IOException {
      ensureOpen();
      return pos;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
      ensureOpen();
      if (newPosition < 0) {
        throw new IllegalArgumentException("Illegal position " + newPosition);
      }
      pos = Math.min(newPosition, data.length);
      return this;
    }

    @Override
    public long size() throws IOException {
      ensureOpen();
      return data.length;
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
      throw new NonWritableChannelException();
    }
  }

  private final class WriteChannel implements SeekableByteChannel {
    private final String path;
    private final boolean append;
    private byte[] buf;
    private int len;
    private long pos;
    private boolean open = true;

    WriteChannel(String path, byte[] initial, boolean append) {
      this.path = path;
      this.buf = initial.clone();
      this.len = initial.length;
      this.append = append;
      this.pos = append ? len : 0;
    }

    private void ensureOpen() throws IOException {
      if (!open) {
        throw new java.nio.channels.ClosedChannelException();
      }
    }

    @Override
    public boolean isOpen() {
      return open;
    }

    @Override
    public void close() throws IOException {
      if (!open) {
        return;
      }
      open = false;
      synchronized (lock) {
        Node n = nodes.get(path);
        if (n == null) {
          n = new Node(path, false);
          n.seq = nextSeq++;
          addNode(n);
        }
        n.data = Arrays.copyOf(buf, len);
        n.source = null;
        n.mtime = FileTime.fromMillis(System.currentTimeMillis());
        n.implicit = false;
        modified = true;
      }
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
      ensureOpen();
      if (pos >= len) {
        return -1;
      }
      int n = (int) Math.min(dst.remaining(), len - pos);
      dst.put(buf, (int) pos, n);
      pos += n;
      return n;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
      ensureOpen();
      int n = src.remaining();
      if (append) {
        pos = len;
      }
      long end = pos + n;
      if (end > buf.length) {
        buf = Arrays.copyOf(buf, (int) Math.max(end, Math.min((long) buf.length * 2, Integer.MAX_VALUE - 8)));
      }
      if (pos > len) {
        Arrays.fill(buf, len, (int) pos, (byte) 0);
      }
      src.get(buf, (int) pos, n);
      pos = end;
      if (end > len) {
        len = (int) end;
      }
      return n;
    }

    @Override
    public long position() throws IOException {
      ensureOpen();
      return pos;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
      ensureOpen();
      if (newPosition < 0) {
        throw new IllegalArgumentException();
      }
      pos = newPosition;
      return this;
    }

    @Override
    public long size() throws IOException {
      ensureOpen();
      return len;
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
      throw new UnsupportedOperationException();
    }
  }

  void copyFile(boolean deleteSource, ZipPath src, ZipPath dst, CopyOption... options) throws IOException {
    synchronized (lock) {
      checkOpen();
      checkWritable();
      boolean replace = false;
      boolean copyAttrs = false;
      for (CopyOption opt : options) {
        if (opt == StandardCopyOption.REPLACE_EXISTING) {
          replace = true;
        } else if (opt == StandardCopyOption.COPY_ATTRIBUTES) {
          copyAttrs = true;
        }
      }
      String sp = resolve(src);
      String dp = resolve(dst);
      Node s = nodes.get(sp);
      if (s == null) {
        throw new NoSuchFileException(src.toString());
      }
      Node d = nodes.get(dp);
      if (d != null) {
        if (sp.equals(dp)) {
          return;
        }
        if (!replace) {
          throw new FileAlreadyExistsException(dst.toString());
        }
        if (d.dir && !d.children.isEmpty()) {
          throw new DirectoryNotEmptyException(dst.toString());
        }
        nodes.get(parentOf(dp)).children.remove(d);
        nodes.remove(dp);
      }
      if (nodes.get(parentOf(dp)) == null) {
        throw new NoSuchFileException(parentOf(dp));
      }
      Node c = new Node(dp, s.dir);
      if (!s.dir) {
        c.data = s.data != null ? s.data : readSource(s);
      }
      c.mtime = copyAttrs ? s.mtime : FileTime.fromMillis(System.currentTimeMillis());
      if (copyAttrs) {
        c.atime = s.atime;
        c.ctime = s.ctime;
      }
      c.seq = nextSeq++;
      addNode(c);
      if (deleteSource && !(s.dir && !s.children.isEmpty())) {
        nodes.get(parentOf(sp)).children.remove(s);
        nodes.remove(sp);
      }
      modified = true;
    }
  }

  private void sync() throws IOException {
    Path tmp = zfpath.resolveSibling(zfpath.getFileName() + "." + System.nanoTime() + ".tmp");
    try (OutputStream os = Files.newOutputStream(tmp)) {
      ZipOutputStream zos = new ZipOutputStream(os);
      writeNodes(zos, root);
      zos.finish();
    } catch (IOException | RuntimeException e) {
      Files.deleteIfExists(tmp);
      throw e;
    }
    if (zipFile != null) {
      zipFile.close();
      zipFile = null;
    }
    Files.move(tmp, zfpath, StandardCopyOption.REPLACE_EXISTING);
  }

  private void writeNodes(ZipOutputStream zos, Node ignored) throws IOException {
    List<Node> explicit = new ArrayList<Node>();
    for (Node n : nodes.values()) {
      if (n.seq >= 0 && n != root) {
        explicit.add(n);
      }
    }
    Collections.sort(explicit, new java.util.Comparator<Node>() {
      @Override
      public int compare(Node a, Node b) {
        return Integer.compare(a.seq, b.seq);
      }
    });
    for (Node n : explicit) {
      String name = n.path.substring(1) + (n.dir ? "/" : "");
      ZipEntry e = new ZipEntry(name);
      if (n.mtime != null) {
        e.setLastModifiedTime(n.mtime);
      }
      if (n.atime != null) {
        e.setLastAccessTime(n.atime);
      }
      if (n.ctime != null) {
        e.setCreationTime(n.ctime);
      }
      if (n.comment != null) {
        e.setComment(n.comment);
      }
      if (n.dir) {
        e.setMethod(ZipEntry.STORED);
        e.setSize(0);
        e.setCompressedSize(0);
        e.setCrc(0);
        zos.putNextEntry(e);
        zos.closeEntry();
        continue;
      }
      byte[] data = n.data != null ? n.data : readSource(n);
      int method = n.data != null ? compressionMethod : (n.source != null ? n.source.getMethod() : compressionMethod);
      e.setMethod(method);
      if (method == ZipEntry.STORED) {
        CRC32 crc = new CRC32();
        crc.update(data, 0, data.length);
        e.setSize(data.length);
        e.setCompressedSize(data.length);
        e.setCrc(crc.getValue());
      }
      zos.putNextEntry(e);
      zos.write(data, 0, data.length);
      zos.closeEntry();
    }
  }

  void flush() throws IOException {
    synchronized (lock) {
      checkOpen();
      if (modified && !readOnly) {
        sync();
        modified = false;
        try {
          zipFile = new ZipFile(zfpath.toFile());
          nodes.clear();
          root.children.clear();
          nodes.put("/", root);
          reload();
        } catch (IOException e) {
          throw e;
        }
      }
    }
  }

  private void reload() {
    java.util.Enumeration<? extends ZipEntry> en = zipFile.entries();
    while (en.hasMoreElements()) {
      ZipEntry e = en.nextElement();
      String name = e.getName();
      boolean dir = name.endsWith("/");
      String p = ZipPath.normalize("/" + (dir ? name.substring(0, name.length() - 1) : name));
      if (p.equals("/")) {
        continue;
      }
      Node n = new Node(p, dir);
      n.source = e;
      n.mtime = e.getLastModifiedTime();
      n.atime = e.getLastAccessTime();
      n.ctime = e.getCreationTime();
      n.comment = e.getComment();
      n.extra = e.getExtra();
      n.seq = nextSeq++;
      addNode(n);
    }
  }
}
