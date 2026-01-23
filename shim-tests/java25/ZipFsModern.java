/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipFsModern {
  static Path dir;

  static void t(String name, Callable<Object> c) {
    try {
      Object o = c.call();
      System.out.println(name + " = " + clean(String.valueOf(o)));
    } catch (Throwable e) {
      System.out.println(name + " ! " + e.getClass().getName() + ": " + clean(String.valueOf(e.getMessage())));
    }
  }

  static String clean(String s) {
    return s.replace(dir.toString(), "$DIR").replace(dir.toUri().toString(), "$DIRURI");
  }

  static Path makeZip(String name) throws IOException {
    Path p = dir.resolve(name);
    try (ZipOutputStream z = new ZipOutputStream(Files.newOutputStream(p))) {
      add(z, "a.txt", "hello", ZipEntry.DEFLATED);
      add(z, "dir/", null, ZipEntry.STORED);
      add(z, "dir/b.txt", "bee bee bee bee bee bee", ZipEntry.DEFLATED);
      add(z, "dir/sub/c.bin", "ccc", ZipEntry.STORED);
      add(z, "implicit/deep/d.txt", "deep", ZipEntry.DEFLATED);
      add(z, "z.txt", "zed", ZipEntry.DEFLATED);
      add(z, "Ünï.txt", "unicode", ZipEntry.DEFLATED);
    }
    return p;
  }

  static void add(ZipOutputStream z, String name, String content, int method) throws IOException {
    ZipEntry e = new ZipEntry(name);
    e.setTime(1700000000000L);
    byte[] data = content == null ? new byte[0] : content.getBytes(StandardCharsets.UTF_8);
    if (method == ZipEntry.STORED) {
      e.setMethod(ZipEntry.STORED);
      e.setSize(data.length);
      e.setCompressedSize(data.length);
      java.util.zip.CRC32 crc = new java.util.zip.CRC32();
      crc.update(data);
      e.setCrc(crc.getValue());
    }
    z.putNextEntry(e);
    z.write(data);
    z.closeEntry();
  }

  static String list(Path p) throws IOException {
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(p)) {
      List<String> l = new ArrayList<>();
      for (Path c : ds) {
        l.add(c.toString());
      }
      Collections.sort(l);
      return l.toString();
    }
  }

  static String unsortedList(Path p) throws IOException {
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(p)) {
      List<String> l = new ArrayList<>();
      for (Path c : ds) {
        l.add(c.getFileName().toString());
      }
      return l.toString();
    }
  }

  static String entries(Path zip) throws IOException {
    try (ZipFile zf = new ZipFile(zip.toFile())) {
      StringBuilder sb = new StringBuilder();
      Enumeration<? extends ZipEntry> en = zf.entries();
      while (en.hasMoreElements()) {
        ZipEntry e = en.nextElement();
        sb.append(e.getName()).append(':').append(e.getSize()).append(':').append(e.isDirectory()).append(' ');
        if (!e.isDirectory()) {
          try (InputStream in = zf.getInputStream(e)) {
            sb.append(new String(in.readAllBytes(), StandardCharsets.UTF_8)).append(' ');
          }
        }
      }
      return sb.toString();
    }
  }

  public static void main(String[] args) throws Exception {
    dir = Files.createTempDirectory("zipfsmodern");
    Path zip = makeZip("t.zip");
    URI uri = URI.create("jar:" + zip.toUri());

    t("providers", () -> {
      List<String> l = new ArrayList<>();
      for (var p : java.nio.file.spi.FileSystemProvider.installedProviders()) {
        if (!p.getScheme().equals("jrt")) {
          l.add(p.getScheme());
        }
      }
      return l;
    });
    t("uri", () -> clean(uri.toString()));
    t("get-before-open", () -> FileSystems.getFileSystem(uri));
    FileSystem fs = FileSystems.newFileSystem(uri, new HashMap<String, Object>());
    t("open-twice", () -> FileSystems.newFileSystem(uri, new HashMap<String, Object>()));
    t("get-after-open", () -> FileSystems.getFileSystem(uri) == fs);
    t("fs-props", () -> fs.isOpen() + " " + fs.isReadOnly() + " " + fs.getSeparator() + " "
        + fs.supportedFileAttributeViews() + " " + fs.provider().getScheme());
    t("roots", () -> {
      List<String> l = new ArrayList<>();
      for (Path r : fs.getRootDirectories()) {
        l.add(r.toString());
      }
      return l;
    });
    t("stores", () -> {
      int n = 0;
      for (var s : fs.getFileStores()) {
        n++;
      }
      return n;
    });
    t("list-root", () -> list(fs.getPath("/")));
    t("list-root-order", () -> unsortedList(fs.getPath("/")));
    t("list-dir-order", () -> unsortedList(fs.getPath("/dir")));
    t("list-implicit-order", () -> unsortedList(fs.getPath("/implicit")));
    t("list-dir", () -> list(fs.getPath("/dir")));
    t("list-rel", () -> list(fs.getPath("dir")));
    t("list-implicit", () -> list(fs.getPath("implicit/deep")));
    t("list-file", () -> list(fs.getPath("/a.txt")));
    t("list-missing", () -> list(fs.getPath("/nope")));
    t("read", () -> Files.readString(fs.getPath("/a.txt")) + "|" + Files.readString(fs.getPath("dir/b.txt")) + "|"
        + Files.readString(fs.getPath("/Ünï.txt")));
    t("read-missing", () -> Files.readString(fs.getPath("/nope")));
    t("read-dir", () -> Files.readString(fs.getPath("/dir")));
    t("read-bytes", () -> Files.readAllBytes(fs.getPath("/dir/sub/c.bin")).length);
    t("exists", () -> Files.exists(fs.getPath("/a.txt")) + " " + Files.exists(fs.getPath("/nope")) + " "
        + Files.exists(fs.getPath("/dir")) + " " + Files.exists(fs.getPath("/implicit")) + " "
        + Files.exists(fs.getPath("/")) + " " + Files.exists(fs.getPath("")) + " " + Files.exists(fs.getPath("dir/../a"
        + ".txt")));
    t("isdir", () -> Files.isDirectory(fs.getPath("/dir")) + " " + Files.isDirectory(fs.getPath("/implicit")) + " "
        + Files.isDirectory(fs.getPath("/a.txt")) + " " + Files.isRegularFile(fs.getPath("/a.txt")) + " "
        + Files.isSymbolicLink(fs.getPath("/a.txt")) + " " + Files.isHidden(fs.getPath("/a.txt")));
    t("size", () -> Files.size(fs.getPath("/a.txt")) + " " + Files.size(fs.getPath("/dir")) + " " + Files.size(fs
        .getPath("/implicit")) + " " + Files.size(fs.getPath("/dir/b.txt")));
    t("attrs", () -> {
      BasicFileAttributes a = Files.readAttributes(fs.getPath("/a.txt"), BasicFileAttributes.class);
      return a.size() + " " + a.isRegularFile() + " " + a.isDirectory() + " " + a.isOther() + " " + a
          .isSymbolicLink() + " " + a.lastModifiedTime().toMillis() + " " + a.creationTime().toMillis() + " "
          + a.lastAccessTime().toMillis() + " " + a.fileKey();
    });
    t("attrs-dir", () -> {
      BasicFileAttributes a = Files.readAttributes(fs.getPath("/dir"), BasicFileAttributes.class);
      return a.size() + " " + a.isRegularFile() + " " + a.isDirectory() + " " + a.lastModifiedTime().toMillis();
    });
    t("attrs-implicit", () -> {
      BasicFileAttributes a = Files.readAttributes(fs.getPath("/implicit"), BasicFileAttributes.class);
      return a.size() + " " + a.isDirectory() + " " + (a.lastModifiedTime() != null);
    });
    t("attrs-root", () -> {
      BasicFileAttributes a = Files.readAttributes(fs.getPath("/"), BasicFileAttributes.class);
      return a.size() + " " + a.isDirectory() + " " + (a.lastModifiedTime() != null);
    });
    t("attrs-missing", () -> Files.readAttributes(fs.getPath("/nope"), BasicFileAttributes.class));
    t("attrs-map", () -> new TreeMap<>(Files.readAttributes(fs.getPath("/a.txt"), "*")));
    t("attrs-map-zip", () -> new TreeMap<>(Files.readAttributes(fs.getPath("/a.txt"), "zip:*")));
    t("attrs-map-some", () -> new TreeMap<>(Files.readAttributes(fs.getPath("/a.txt"), "size,isDirectory")));
    t("attrs-map-badview", () -> Files.readAttributes(fs.getPath("/a.txt"), "posix:*"));
    t("attr-single", () -> Files.getAttribute(fs.getPath("/dir/b.txt"), "size") + " " + Files.getAttribute(fs
        .getPath("/dir/b.txt"), "zip:compressedSize") + " " + Files.getAttribute(fs.getPath("/dir/b.txt"),
        "zip:method") + " " + Files.getAttribute(fs.getPath("/dir/sub/c.bin"), "zip:method"));
    t("lastmodified", () -> Files.getLastModifiedTime(fs.getPath("/a.txt")).toMillis());
    t("access", () -> {
      fs.provider().checkAccess(fs.getPath("/a.txt"));
      return "ok";
    });
    t("access-missing", () -> {
      fs.provider().checkAccess(fs.getPath("/nope"));
      return "ok";
    });
    t("readable", () -> Files.isReadable(fs.getPath("/a.txt")) + " " + Files.isWritable(fs.getPath("/a.txt")) + " "
        + Files.isExecutable(fs.getPath("/a.txt")));
    t("walk", () -> {
      try (Stream<Path> s = Files.walk(fs.getPath("/"))) {
        return s.map(Path::toString).sorted().collect(Collectors.toList());
      }
    });
    t("walk-order", () -> {
      try (Stream<Path> s = Files.walk(fs.getPath("/"))) {
        return s.map(Path::toString).collect(Collectors.toList());
      }
    });
    t("list-stream", () -> {
      try (Stream<Path> s = Files.list(fs.getPath("/dir"))) {
        return s.map(Path::toString).sorted().collect(Collectors.toList());
      }
    });
    t("visit", () -> {
      List<String> l = new ArrayList<>();
      Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
          l.add("D" + d);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
          l.add("F" + f + ":" + a.size());
          return FileVisitResult.CONTINUE;
        }
      });
      return l;
    });
    t("find", () -> {
      try (Stream<Path> s = Files.find(fs.getPath("/"), 10, (p, a) -> a.isRegularFile() && p.toString().endsWith(
          ".txt"))) {
        return s.map(Path::toString).sorted().collect(Collectors.toList());
      }
    });
    t("matcher", () -> {
      PathMatcher m = fs.getPathMatcher("glob:**/*.txt");
      PathMatcher r = fs.getPathMatcher("regex:/dir/.*");
      return m.matches(fs.getPath("/dir/b.txt")) + " " + m.matches(fs.getPath("/a.txt")) + " " + r.matches(fs
          .getPath("/dir/x")) + " " + fs.getPathMatcher("glob:*.txt").matches(fs.getPath("a.txt"));
    });
    t("matcher-bad", () -> fs.getPathMatcher("nope:x"));
    t("matcher-nosyntax", () -> fs.getPathMatcher("x"));
    t("watch", () -> fs.newWatchService());
    t("principals", () -> fs.getUserPrincipalLookupService());

    t("path-basics", () -> {
      Path p = fs.getPath("/a/b/c.txt");
      return p.getFileName() + " " + p.getParent() + " " + p.getRoot() + " " + p.getNameCount() + " " + p.getName(1)
          + " " + p.subpath(1, 3) + " " + p.isAbsolute() + " " + p.toUri() .toString().replace(clean(zip.toUri()
          .toString()), "$ZIP");
    });
    t("path-relative", () -> {
      Path p = fs.getPath("a/b");
      return p.getFileName() + " " + p.getParent() + " " + p.getRoot() + " " + p.getNameCount() + " "
          + p.toAbsolutePath() + " " + p.isAbsolute();
    });
    t("path-empty", () -> {
      Path p = fs.getPath("");
      return "[" + p + "] " + p.getNameCount() + " " + p.getFileName() + " " + p.getParent() + " " + p.getRoot() + " "
          + p.isAbsolute() + " " + p.toAbsolutePath();
    });
    t("path-root", () -> {
      Path p = fs.getPath("/");
      return p.getNameCount() + " " + p.getFileName() + " " + p.getParent() + " " + p.getRoot() + " " + p
          .isAbsolute();
    });
    t("path-normalize-input", () -> fs.getPath("//a///b//").toString() + " " + fs.getPath("a", "", "b", "/c") + " "
        + fs.getPath("/", "x"));
    t("path-nul", () -> fs.getPath("a\u0000b"));
    t("path-normalize", () -> fs.getPath("/a/./b/../c/").normalize() + " " + fs.getPath("a/../../b").normalize() + " "
        + fs.getPath("/../a").normalize() + " " + fs.getPath("./.").normalize() + "|" + fs.getPath("").normalize());
    t("path-resolve", () -> fs.getPath("/a").resolve("b") + " " + fs.getPath("/a").resolve("/b") + " "
        + fs.getPath("a").resolve("") + " " + fs.getPath("").resolve("b") + " " + fs.getPath("/").resolve("b") + " "
        + fs.getPath("/a/b").resolveSibling("c") + " " + fs.getPath("a").resolveSibling("c") + " "
        + fs.getPath("/").resolveSibling("c"));
    t("path-relativize", () -> fs.getPath("/a/b").relativize(fs.getPath("/a/b/c/d")) + " " + fs.getPath("/a/b/c")
        .relativize(fs.getPath("/a/x")) + " " + fs.getPath("a").relativize(fs.getPath("a")) + "|" + fs.getPath("")
        .relativize(fs.getPath("a/b")) + " " + fs.getPath("a/b").relativize(fs.getPath("")));
    t("path-relativize-mixed", () -> fs.getPath("/a").relativize(fs.getPath("b")));
    t("path-starts-ends", () -> {
      Path p = fs.getPath("/a/b/c");
      return p.startsWith("/a") + " " + p.startsWith("/a/b/c") + " " + p.startsWith("a") + " " + p.startsWith("/a/b/c/d")
          + " " + p.endsWith("c") + " " + p.endsWith("b/c") + " " + p.endsWith("/a/b/c") + " " + p.endsWith("/b/c") + " "
          + p.startsWith("/") + " " + p.startsWith("") + " " + p.endsWith("") + " " + fs.getPath("").startsWith("")
          + " " + p.startsWith(fs.getPath("/a/b")) + " " + p.startsWith(Paths.get("/a"));
    });
    t("path-compare", () -> fs.getPath("/a").compareTo(fs.getPath("/b")) + " " + fs.getPath("/a").equals(fs.getPath(
        "/a/")) + " " + (fs.getPath("/a").hashCode() == fs.getPath("/a").hashCode()) + " " + fs.getPath("/a").equals(
        Paths.get("/a")));
    t("path-compare-foreign", () -> fs.getPath("/a").compareTo(Paths.get("/a")));
    t("path-resolve-foreign", () -> fs.getPath("/a").resolve(Paths.get("b")));
    t("path-iterator", () -> {
      List<String> l = new ArrayList<>();
      for (Path p : fs.getPath("/x/y/z")) {
        l.add(p.toString());
      }
      return l;
    });
    t("path-getname-bad", () -> fs.getPath("/a").getName(1));
    t("path-subpath-bad", () -> fs.getPath("/a/b").subpath(1, 1));
    t("path-tofile", () -> fs.getPath("/a").toFile());
    t("path-realpath", () -> fs.getPath("/dir/../a.txt").toRealPath() + " " + fs.getPath("dir").toRealPath());
    t("path-realpath-missing", () -> fs.getPath("/nope").toRealPath());
    t("path-uri-roundtrip", () -> Paths.get(fs.getPath("/dir/b.txt").toUri()).toString());
    t("path-uri-bad", () -> Paths.get(URI.create("jar:file:///nonexistent/x.zip!/a")));
    t("path-uri-nopath", () -> Paths.get(URI.create(uri.toString())));
    t("path-register", () -> fs.getPath("/").register(null, (java.nio.file.WatchEvent.Kind<?>[]) null));
    t("same-file", () -> Files.isSameFile(fs.getPath("/a.txt"), fs.getPath("a.txt")) + " " + Files.isSameFile(fs
        .getPath("/a.txt"), fs.getPath("/dir/b.txt")));
    t("same-file-missing", () -> Files.isSameFile(fs.getPath("/a.txt"), fs.getPath("/nope")));

    t("channel-read", () -> {
      try (SeekableByteChannel ch = Files.newByteChannel(fs.getPath("/dir/b.txt"))) {
        ByteBuffer bb = ByteBuffer.allocate(8);
        int n = ch.read(bb);
        long pos = ch.position();
        ch.position(4);
        bb.clear();
        int m = ch.read(bb);
        return n + " " + pos + " " + m + " " + ch.size() + " " + new String(bb.array(), 0, m);
      }
    });
    t("channel-write-on-read", () -> {
      try (SeekableByteChannel ch = Files.newByteChannel(fs.getPath("/a.txt"))) {
        return ch.write(ByteBuffer.wrap(new byte[1]));
      }
    });
    t("channel-truncate-on-read", () -> {
      try (SeekableByteChannel ch = Files.newByteChannel(fs.getPath("/a.txt"))) {
        return ch.truncate(1);
      }
    });
    t("channel-closed", () -> {
      SeekableByteChannel ch = Files.newByteChannel(fs.getPath("/a.txt"));
      ch.close();
      return ch.isOpen() + " " + ch.read(ByteBuffer.allocate(1));
    });
    t("inputstream-write-opt", () -> fs.provider().newInputStream(fs.getPath("/a.txt"), StandardOpenOption.WRITE));
    t("reader-lines", () -> Files.readAllLines(fs.getPath("/dir/b.txt")));

    Path out = fs.getPath("/new");
    t("mkdir", () -> {
      Files.createDirectory(out);
      return Files.isDirectory(out);
    });
    t("mkdir-exists", () -> Files.createDirectory(out));
    t("mkdir-noparent", () -> Files.createDirectory(fs.getPath("/p/q")));
    t("mkdirs", () -> {
      Files.createDirectories(fs.getPath("/m/n/o"));
      return Files.isDirectory(fs.getPath("/m/n/o"));
    });
    t("write", () -> {
      Files.writeString(out.resolve("w.txt"), "written");
      return Files.readString(out.resolve("w.txt")) + " " + Files.size(out.resolve("w.txt"));
    });
    t("write-overwrite", () -> {
      Files.writeString(out.resolve("w.txt"), "over");
      return Files.readString(out.resolve("w.txt"));
    });
    t("write-append", () -> {
      Files.writeString(out.resolve("w.txt"), "+more", StandardOpenOption.APPEND);
      return Files.readString(out.resolve("w.txt"));
    });
    t("write-createnew", () -> Files.writeString(out.resolve("w.txt"), "x", StandardOpenOption.CREATE_NEW));
    t("write-nocreate", () -> Files.writeString(out.resolve("nocreate.txt"), "x", StandardOpenOption.WRITE));
    t("write-noparent", () -> Files.writeString(fs.getPath("/zz/yy.txt"), "x"));
    t("write-dir", () -> Files.writeString(out, "x"));
    t("write-bytes-stream", () -> {
      try (OutputStream os = Files.newOutputStream(out.resolve("s.bin"))) {
        os.write(1);
        os.write(new byte[] {2, 3, 4}, 1, 2);
      }
      return java.util.Arrays.toString(Files.readAllBytes(out.resolve("s.bin")));
    });
    t("write-channel", () -> {
      try (SeekableByteChannel ch = Files.newByteChannel(out.resolve("ch.bin"), StandardOpenOption.CREATE,
          StandardOpenOption.WRITE, StandardOpenOption.READ)) {
        ch.write(ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5}));
        ch.position(1);
        ch.write(ByteBuffer.wrap(new byte[] {9}));
        ch.position(0);
        ByteBuffer bb = ByteBuffer.allocate(10);
        int n = ch.read(bb);
        ch.truncate(3);
        return n + " " + java.util.Arrays.toString(java.util.Arrays.copyOf(bb.array(), n)) + " " + ch.size() + " "
            + ch.position();
      }
    });
    t("write-channel-readonly-opt", () -> {
      try (SeekableByteChannel ch = Files.newByteChannel(out.resolve("ch2.bin"), StandardOpenOption.CREATE,
          StandardOpenOption.WRITE)) {
        return ch.read(ByteBuffer.allocate(1));
      }
    });
    t("write-append-read", () -> Files.newByteChannel(out.resolve("ch.bin"), StandardOpenOption.READ,
        StandardOpenOption.APPEND));
    t("write-append-truncate", () -> Files.newByteChannel(out.resolve("ch.bin"), StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.APPEND));
    t("write-modified-time", () -> Files.getLastModifiedTime(out.resolve("w.txt")).toMillis() > 1700000000000L);
    t("set-times", () -> {
      Files.setLastModifiedTime(out.resolve("w.txt"), FileTime.fromMillis(1600000000000L));
      return Files.getLastModifiedTime(out.resolve("w.txt")).toMillis();
    });
    t("set-attr", () -> {
      Files.setAttribute(out.resolve("w.txt"), "lastAccessTime", FileTime.fromMillis(1500000000000L));
      return Files.getAttribute(out.resolve("w.txt"), "lastAccessTime");
    });
    t("set-attr-bad", () -> {
      Files.setAttribute(out.resolve("w.txt"), "size", 5L);
      return "ok";
    });
    t("set-attr-view", () -> {
      Files.setAttribute(out.resolve("w.txt"), "posix:permissions", null);
      return "ok";
    });
    t("copy-in", () -> {
      Files.copy(out.resolve("w.txt"), out.resolve("w2.txt"));
      return Files.readString(out.resolve("w2.txt"));
    });
    t("copy-exists", () -> Files.copy(out.resolve("w.txt"), out.resolve("w2.txt")));
    t("copy-replace", () -> {
      Files.copy(out.resolve("s.bin"), out.resolve("w2.txt"), StandardCopyOption.REPLACE_EXISTING);
      return Files.size(out.resolve("w2.txt"));
    });
    t("copy-missing", () -> Files.copy(out.resolve("nope"), out.resolve("x")));
    t("copy-noparent", () -> Files.copy(out.resolve("w.txt"), fs.getPath("/zz/y")));
    t("copy-dir", () -> {
      Files.copy(out, fs.getPath("/newcopy"));
      return Files.isDirectory(fs.getPath("/newcopy")) + " " + list(fs.getPath("/newcopy"));
    });
    t("copy-self", () -> {
      Files.copy(out.resolve("w.txt"), out.resolve("w.txt"));
      return "ok";
    });
    t("copy-out", () -> {
      Path ext = dir.resolve("ext.txt");
      Files.copy(out.resolve("w.txt"), ext);
      return Files.readString(ext);
    });
    t("copy-from-stream", () -> {
      Files.copy(new java.io.ByteArrayInputStream("streamed".getBytes()), out.resolve("fromstream.txt"));
      return Files.readString(out.resolve("fromstream.txt"));
    });
    t("copy-to-stream", () -> {
      java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
      Files.copy(out.resolve("w.txt"), bos);
      return bos.toString();
    });
    t("copy-from-outside", () -> {
      Path ext = dir.resolve("ext2.txt");
      Files.writeString(ext, "external");
      Files.copy(ext, out.resolve("ext.txt"));
      return Files.readString(out.resolve("ext.txt"));
    });
    t("move", () -> {
      Files.move(out.resolve("w2.txt"), out.resolve("moved.txt"));
      return Files.exists(out.resolve("w2.txt")) + " " + Files.exists(out.resolve("moved.txt"));
    });
    t("move-exists", () -> Files.move(out.resolve("moved.txt"), out.resolve("w.txt")));
    t("move-replace", () -> {
      Files.move(out.resolve("moved.txt"), out.resolve("w.txt"), StandardCopyOption.REPLACE_EXISTING);
      return Files.readAllBytes(out.resolve("w.txt")).length;
    });
    t("move-dir-nonempty", () -> Files.move(out, fs.getPath("/renamed")));
    t("move-missing", () -> Files.move(fs.getPath("/nope"), fs.getPath("/nope2")));
    t("delete", () -> {
      Files.delete(out.resolve("s.bin"));
      return Files.exists(out.resolve("s.bin"));
    });
    t("delete-missing", () -> {
      Files.delete(out.resolve("s.bin"));
      return "ok";
    });
    t("delete-if-exists", () -> Files.deleteIfExists(out.resolve("s.bin")));
    t("delete-nonempty", () -> {
      Files.delete(out);
      return "ok";
    });
    t("delete-root", () -> {
      Files.delete(fs.getPath("/"));
      return "ok";
    });
    t("delete-implicit", () -> {
      Files.delete(fs.getPath("/implicit"));
      return "ok";
    });
    t("delete-recursive", () -> {
      Files.walkFileTree(fs.getPath("/implicit"), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException {
          Files.delete(f);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
          Files.delete(d);
          return FileVisitResult.CONTINUE;
        }
      });
      return Files.exists(fs.getPath("/implicit"));
    });
    t("tempfile-in", () -> Files.createTempFile(fs.getPath("/new"), "pre", ".suf").getFileName().toString()
        .startsWith("pre"));
    t("list-new-after", () -> list(fs.getPath("/new")).replaceAll("pre[0-9]+\\.suf", "preN.suf"));
    t("attrs-new-file", () -> {
      BasicFileAttributes a = Files.readAttributes(out.resolve("w.txt"), BasicFileAttributes.class);
      return a.size() + " " + a.isRegularFile();
    });

    fs.close();
    t("after-close-open", () -> fs.isOpen());
    t("after-close-read", () -> Files.readString(fs.getPath("/a.txt")));
    t("after-close-getpath", () -> fs.getPath("/a.txt"));
    t("after-close-close", () -> {
      fs.close();
      return "ok";
    });
    t("after-close-get", () -> FileSystems.getFileSystem(uri));
    t("saved", () -> entries(zip).replaceAll("pre[0-9]+\\.suf", "preN.suf"));
    t("reopen", () -> {
      try (FileSystem f2 = FileSystems.newFileSystem(uri, new HashMap<String, Object>())) {
        return Files.readString(f2.getPath("/new/w.txt")) + " " + Files.exists(f2.getPath("/implicit"));
      }
    });

    Path ro = makeZip("ro.zip");
    t("env-readonly", () -> {
      Map<String, Object> env = new HashMap<>();
      env.put("accessMode", "readOnly");
      try (FileSystem f = FileSystems.newFileSystem(ro, env)) {
        return f.isReadOnly();
      }
    });
    t("env-readonly-write", () -> {
      Map<String, Object> env = new HashMap<>();
      env.put("accessMode", "readOnly");
      try (FileSystem f = FileSystems.newFileSystem(ro, env)) {
        Files.writeString(f.getPath("/x.txt"), "x");
        return "ok";
      }
    });
    t("env-readonly-delete", () -> {
      Map<String, Object> env = new HashMap<>();
      env.put("accessMode", "readOnly");
      try (FileSystem f = FileSystems.newFileSystem(ro, env)) {
        Files.delete(f.getPath("/a.txt"));
        return "ok";
      }
    });
    t("env-readonly-mkdir", () -> {
      Map<String, Object> env = new HashMap<>();
      env.put("accessMode", "readOnly");
      try (FileSystem f = FileSystems.newFileSystem(ro, env)) {
        Files.createDirectory(f.getPath("/x"));
        return "ok";
      }
    });
    t("env-readonly-access", () -> {
      Map<String, Object> env = new HashMap<>();
      env.put("accessMode", "readOnly");
      try (FileSystem f = FileSystems.newFileSystem(ro, env)) {
        f.provider().checkAccess(f.getPath("/a.txt"), java.nio.file.AccessMode.WRITE);
        return "ok";
      }
    });
    t("env-bad-mode", () -> {
      Map<String, Object> env = new HashMap<>();
      env.put("accessMode", "weird");
      return FileSystems.newFileSystem(ro, env);
    });
    t("env-bad-method", () -> {
      Map<String, Object> env = new HashMap<>();
      env.put("compressionMethod", "LZMA");
      return FileSystems.newFileSystem(ro, env);
    });
    t("path-open", () -> {
      try (FileSystem f = FileSystems.newFileSystem(ro)) {
        return Files.readString(f.getPath("a.txt")) + " " + f.provider().getScheme();
      }
    });
    t("path-open-loader", () -> {
      try (FileSystem f = FileSystems.newFileSystem(ro, (ClassLoader) null)) {
        return Files.readString(f.getPath("a.txt"));
      }
    });
    t("path-open-missing", () -> FileSystems.newFileSystem(dir.resolve("missing.zip")));
    t("uri-open-missing", () -> FileSystems.newFileSystem(URI.create("jar:" + dir.resolve("missing.zip").toUri()),
        new HashMap<String, Object>()));
    t("not-a-zip", () -> {
      Path p = dir.resolve("plain.txt");
      Files.writeString(p, "just text");
      return FileSystems.newFileSystem(p);
    });
    t("not-a-zip-jar-name", () -> {
      Path p = dir.resolve("bad.jar");
      Files.writeString(p, "just text");
      return FileSystems.newFileSystem(p);
    });
    t("open-dir", () -> FileSystems.newFileSystem(dir));
    Path created = dir.resolve("created.zip");
    URI curi = URI.create("jar:" + created.toUri());
    t("create", () -> {
      Map<String, Object> env = new HashMap<>();
      env.put("create", "true");
      try (FileSystem f = FileSystems.newFileSystem(curi, env)) {
        Files.writeString(f.getPath("/hello.txt"), "hi there");
        Files.createDirectories(f.getPath("/sub/dir"));
        Files.writeString(f.getPath("/sub/dir/x.txt"), "xx");
        return Files.exists(created);
      }
    });
    t("create-result", () -> entries(created));
    t("create-bool", () -> {
      Path c2 = dir.resolve("created2.zip");
      Map<String, Object> env = new HashMap<>();
      env.put("create", true);
      try (FileSystem f = FileSystems.newFileSystem(c2, env)) {
        Files.writeString(f.getPath("/k"), "v");
      }
      return entries(c2);
    });
    t("create-empty", () -> {
      Path c3 = dir.resolve("created3.zip");
      Map<String, Object> env = new HashMap<>();
      env.put("create", "true");
      try (FileSystem f = FileSystems.newFileSystem(c3, env)) {
        return Files.size(c3);
      }
    });
    t("nocompression", () -> {
      Path c4 = dir.resolve("created4.zip");
      Map<String, Object> env = new HashMap<>();
      env.put("create", "true");
      env.put("noCompression", "true");
      try (FileSystem f = FileSystems.newFileSystem(c4, env)) {
        Files.writeString(f.getPath("/k"), "valuevaluevaluevalue");
      }
      try (ZipFile zf = new ZipFile(c4.toFile())) {
        ZipEntry e = zf.getEntry("k");
        return e.getMethod() + " " + e.getSize() + " " + e.getCompressedSize();
      }
    });
    t("compressed", () -> {
      Path c5 = dir.resolve("created5.zip");
      Map<String, Object> env = new HashMap<>();
      env.put("create", "true");
      try (FileSystem f = FileSystems.newFileSystem(c5, env)) {
        Files.writeString(f.getPath("/k"), "valuevaluevaluevaluevaluevaluevaluevaluevaluevaluevalue");
      }
      try (ZipFile zf = new ZipFile(c5.toFile())) {
        ZipEntry e = zf.getEntry("k");
        return e.getMethod() + " " + e.getSize() + " " + (e.getCompressedSize() < e.getSize());
      }
    });
    t("modify-existing", () -> {
      Path m = makeZip("modify.zip");
      try (FileSystem f = FileSystems.newFileSystem(m)) {
        Files.writeString(f.getPath("/a.txt"), "changed");
        Files.delete(f.getPath("/z.txt"));
        Files.writeString(f.getPath("/dir/new.txt"), "new");
      }
      return entries(m);
    });
    t("unmodified-keeps-file", () -> {
      Path m = makeZip("keep.zip");
      long before = Files.size(m);
      try (FileSystem f = FileSystems.newFileSystem(m)) {
        Files.readString(f.getPath("/a.txt"));
      }
      return before == Files.size(m);
    });
    t("jar-uri-path-form", () -> {
      Path m = makeZip("form.zip");
      try (FileSystem f = FileSystems.newFileSystem(URI.create("jar:" + m.toUri() + "!/"), new HashMap<String, Object>())) {
        return f.getPath("/a.txt").toUri().toString().endsWith("form.zip!/a.txt");
      }
    });
    t("bad-scheme", () -> FileSystems.newFileSystem(URI.create("zip:file:///x.zip"), new HashMap<String, Object>()));
    t("files-newfs-outside-getpath", () -> {
      Path m = makeZip("outside.zip");
      try (FileSystem f = FileSystems.newFileSystem(m)) {
        Path p = f.getPath("dir", "b.txt");
        return p + " " + p.getFileSystem().equals(f) + " " + p.getFileSystem().provider().getClass().getName();
      }
    });
    t("sorted-names", () -> {
      Path m = makeZip("names.zip");
      try (FileSystem f = FileSystems.newFileSystem(m)) {
        try (Stream<Path> s = Files.walk(f.getPath("/"))) {
          return s.map(Path::toString).sorted().collect(Collectors.joining(","));
        }
      }
    });
    Files.walk(dir).sorted(Comparator_reverse()).forEach(p -> p.toFile().delete());
  }

  static java.util.Comparator<Path> Comparator_reverse() {
    return java.util.Comparator.reverseOrder();
  }
}
