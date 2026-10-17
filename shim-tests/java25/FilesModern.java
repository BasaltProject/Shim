/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.stream.*;

public class FilesModern {
  static Path T;

  interface Body {
    Object get() throws Exception;
  }

  static String norm(String s) {
    return s.replace(T.toString(), "<T>");
  }

  static void probe(String name, Body body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage());
    }
    System.out.println(name + "=" + norm(out).replaceAll("\\d{9,}", "N"));
  }

  static String list(Path d) throws IOException {
    try (Stream<Path> s = Files.list(d)) {
      return s.map(p -> d.relativize(p).toString()).sorted().collect(Collectors.toList()).toString();
    }
  }

  static String tree(Path d) throws IOException {
    try (Stream<Path> s = Files.walk(d)) {
      return s.map(p -> d.relativize(p).toString()).sorted().collect(Collectors.toList()).toString();
    }
  }

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.US);
    T = Files.createTempDirectory("fm").toRealPath();
    Path f = T.resolve("a.txt");
    probe("createFile", () -> Files.createFile(f) + " " + Files.size(f) + " " + Files.exists(f) + " " + Files.isRegularFile(f));
    probe("createFile again", () -> Files.createFile(f));
    probe("createFile nodir", () -> Files.createFile(T.resolve("no/such/x")));
    probe("createDirectory", () -> Files.createDirectory(T.resolve("d1")) + " " + Files.isDirectory(T.resolve("d1")));
    probe("createDirectory again", () -> Files.createDirectory(T.resolve("d1")));
    probe("createDirectory nodir", () -> Files.createDirectory(T.resolve("x/y/z")));
    probe("createDirectories", () -> Files.createDirectories(T.resolve("p/q/r")) + " " + tree(T.resolve("p")));
    probe("createDirectories existing", () -> Files.createDirectories(T.resolve("p/q/r")));
    probe("createDirectories over file", () -> Files.createDirectories(f));
    probe("createDirectories under file", () -> Files.createDirectories(f.resolve("sub")));
    probe("createDirectories attrs", () -> { Path d = Files.createDirectories(T.resolve("m/n"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---"))); return PosixFilePermissions.toString(Files.getPosixFilePermissions(d)) + " " + PosixFilePermissions.toString(Files.getPosixFilePermissions(T.resolve("m"))); });
    probe("createFile attrs", () -> { Path p = Files.createFile(T.resolve("perm.txt"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r-----"))); return PosixFilePermissions.toString(Files.getPosixFilePermissions(p)); });
    probe("createFile bad attr", () -> Files.createFile(T.resolve("bad.txt"), new FileAttribute<String>() { public String name() { return "basic:size"; } public String value() { return "x"; } }));
    probe("write/read", () -> { Files.write(f, "hello\nworld\n".getBytes()); return new String(Files.readAllBytes(f)) + Files.size(f) + Files.readAllLines(f) + Files.readString(f).length(); });
    probe("write append", () -> { Files.write(f, "more".getBytes(), StandardOpenOption.APPEND); return Files.readString(f); });
    probe("write truncate", () -> { Files.write(f, "x".getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE); return Files.readString(f); });
    probe("write createnew exists", () -> Files.write(f, "x".getBytes(), StandardOpenOption.CREATE_NEW));
    probe("write read+append", () -> Files.write(f, "x".getBytes(), StandardOpenOption.READ, StandardOpenOption.APPEND));
    probe("write append+truncate", () -> Files.write(f, "x".getBytes(), StandardOpenOption.APPEND, StandardOpenOption.TRUNCATE_EXISTING));
    probe("write nocreate", () -> Files.write(T.resolve("nocreate.txt"), "x".getBytes(), StandardOpenOption.WRITE));
    probe("write bad option", () -> Files.write(f, "x".getBytes(), LinkOption.NOFOLLOW_LINKS, StandardOpenOption.WRITE));
    probe("write read option", () -> Files.newOutputStream(f, StandardOpenOption.READ));
    probe("read option append", () -> Files.newInputStream(f, StandardOpenOption.APPEND));
    probe("read missing", () -> Files.readAllBytes(T.resolve("missing")));
    probe("read dir", () -> Files.readAllBytes(T.resolve("d1")));
    probe("newInputStream dir", () -> { try (InputStream in = Files.newInputStream(T.resolve("d1"))) { return in.read(); } });
    probe("readString utf8 bad", () -> { Path b = T.resolve("bad.bin"); Files.write(b, new byte[] {(byte) 0xff, (byte) 0xfe, 'a'}); return Files.readString(b); });
    probe("readString latin1", () -> Files.readString(T.resolve("bad.bin"), StandardCharsets.ISO_8859_1).length());
    probe("writeString bad", () -> Files.writeString(T.resolve("ws.txt"), "a\ud800b"));
    probe("writeString ascii bad", () -> Files.writeString(T.resolve("ws2.txt"), "caf\u00e9", StandardCharsets.US_ASCII));
    probe("writeString", () -> { Files.writeString(T.resolve("ws3.txt"), "caf\u00e9\n\u4e2d", StandardCharsets.UTF_8); return Files.size(T.resolve("ws3.txt")) + Files.readString(T.resolve("ws3.txt")); });
    probe("lines", () -> { Files.write(T.resolve("l.txt"), Arrays.asList("one", "two", "", "three")); try (Stream<String> s = Files.lines(T.resolve("l.txt"))) { return s.collect(Collectors.toList()); } });
    probe("lines bad", () -> { try (Stream<String> s = Files.lines(T.resolve("bad.bin"))) { return s.collect(Collectors.toList()); } });
    probe("lines missing", () -> Files.lines(T.resolve("missing")));
    probe("write lines cs", () -> { Files.write(T.resolve("l2.txt"), Arrays.asList("\u00e9"), StandardCharsets.ISO_8859_1); return Arrays.toString(Files.readAllBytes(T.resolve("l2.txt"))); });
    probe("bufwriter", () -> { try (BufferedWriter w = Files.newBufferedWriter(T.resolve("bw.txt"))) { w.write("x\ny"); } try (BufferedReader r = Files.newBufferedReader(T.resolve("bw.txt"))) { return r.readLine() + r.readLine() + r.readLine(); } });
    probe("bufwriter exists new", () -> Files.newBufferedWriter(T.resolve("bw.txt"), StandardOpenOption.CREATE_NEW));
    probe("mismatch", () -> { Path a = T.resolve("m1"), b = T.resolve("m2"), c = T.resolve("m3"); Files.writeString(a, "abcdef"); Files.writeString(b, "abcxef"); Files.writeString(c, "abc"); return Files.mismatch(a, a) + " " + Files.mismatch(a, b) + " " + Files.mismatch(a, c) + " " + Files.mismatch(c, a) + " " + Files.mismatch(a, T.resolve("m4")); });
    probe("mismatch big", () -> { Path a = T.resolve("mb1"), b = T.resolve("mb2"); byte[] x = new byte[20000]; Files.write(a, x); x[17000] = 1; Files.write(b, x); return Files.mismatch(a, b) + " " + Files.mismatch(b, a); });
    probe("copy file", () -> { Path t = T.resolve("copy.txt"); Files.copy(T.resolve("l.txt"), t); return Files.readAllLines(t) + " " + Files.copy(T.resolve("l.txt"), T.resolve("copy2.txt")); });
    probe("copy exists", () -> Files.copy(T.resolve("l.txt"), T.resolve("copy.txt")));
    probe("copy replace", () -> { Files.writeString(T.resolve("copy.txt"), "old"); Files.copy(T.resolve("l.txt"), T.resolve("copy.txt"), StandardCopyOption.REPLACE_EXISTING); return Files.readAllLines(T.resolve("copy.txt")); });
    probe("copy missing", () -> Files.copy(T.resolve("missing"), T.resolve("zzz")));
    probe("copy same", () -> Files.copy(f, f));
    probe("copy dir", () -> { Files.createDirectories(T.resolve("cd/sub")); Files.writeString(T.resolve("cd/sub/f"), "x"); Files.copy(T.resolve("cd"), T.resolve("cd2")); return tree(T.resolve("cd2")); });
    probe("copy dir replace nonempty", () -> Files.copy(T.resolve("cd"), T.resolve("cd2"), StandardCopyOption.REPLACE_EXISTING));
    probe("copy attrs", () -> { Path s = T.resolve("ca.txt"); Files.writeString(s, "x"); Files.setLastModifiedTime(s, FileTime.fromMillis(1000000000123L)); Files.setPosixFilePermissions(s, PosixFilePermissions.fromString("rwxr-----")); Path t = T.resolve("ca2.txt"); Files.copy(s, t, StandardCopyOption.COPY_ATTRIBUTES); return Files.getLastModifiedTime(t) + " " + PosixFilePermissions.toString(Files.getPosixFilePermissions(t)); });
    probe("copy no attrs perms", () -> { Path t = T.resolve("ca3.txt"); Files.copy(T.resolve("ca.txt"), t); return PosixFilePermissions.toString(Files.getPosixFilePermissions(t)).substring(0, 1) + " " + (Files.getLastModifiedTime(t).toMillis() != 1000000000123L); });
    probe("copy bad option", () -> Files.copy(f, T.resolve("zz"), StandardCopyOption.ATOMIC_MOVE));
    probe("copy stream", () -> { long n = Files.copy(new ByteArrayInputStream("streamdata".getBytes()), T.resolve("cs.txt")); return n + Files.readString(T.resolve("cs.txt")); });
    probe("copy stream exists", () -> Files.copy(new ByteArrayInputStream("x".getBytes()), T.resolve("cs.txt")));
    probe("copy stream replace", () -> Files.copy(new ByteArrayInputStream("yy".getBytes()), T.resolve("cs.txt"), StandardCopyOption.REPLACE_EXISTING) + Files.readString(T.resolve("cs.txt")));
    probe("copy to stream", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); long n = Files.copy(T.resolve("cs.txt"), bo); return n + bo.toString(); });
    probe("move file", () -> { Files.writeString(T.resolve("mv1"), "m"); Files.move(T.resolve("mv1"), T.resolve("mv2")); return Files.exists(T.resolve("mv1")) + " " + Files.readString(T.resolve("mv2")); });
    probe("move exists", () -> { Files.writeString(T.resolve("mv3"), "m"); return Files.move(T.resolve("mv3"), T.resolve("mv2")); });
    probe("move replace", () -> { Files.move(T.resolve("mv3"), T.resolve("mv2"), StandardCopyOption.REPLACE_EXISTING); return Files.readString(T.resolve("mv2")) + Files.exists(T.resolve("mv3")); });
    probe("move atomic", () -> { Files.writeString(T.resolve("mv4"), "a"); Files.move(T.resolve("mv4"), T.resolve("mv5"), StandardCopyOption.ATOMIC_MOVE); return Files.exists(T.resolve("mv5")); });
    probe("move missing", () -> Files.move(T.resolve("missing"), T.resolve("zzz")));
    probe("move dir", () -> { Files.createDirectories(T.resolve("md/s")); Files.move(T.resolve("md"), T.resolve("md2")); return tree(T.resolve("md2")); });
    probe("move over nonempty dir", () -> { Files.createDirectories(T.resolve("md3")); return Files.move(T.resolve("md3"), T.resolve("md2"), StandardCopyOption.REPLACE_EXISTING); });
    probe("move bad option", () -> Files.move(f, T.resolve("zz"), StandardCopyOption.COPY_ATTRIBUTES));
    probe("delete", () -> { Files.writeString(T.resolve("del"), "x"); Files.delete(T.resolve("del")); return Files.exists(T.resolve("del")); });
    probe("delete missing", () -> { Files.delete(T.resolve("del")); return "deleted"; });
    probe("deleteIfExists", () -> Files.deleteIfExists(T.resolve("del")) + " " + Files.deleteIfExists(f.resolveSibling("copy2.txt")));
    probe("delete nonempty dir", () -> { Files.delete(T.resolve("md2")); return "deleted"; });
    probe("deleteIfExists nonempty dir", () -> Files.deleteIfExists(T.resolve("md2")));
    probe("delete empty dir", () -> { Files.createDirectory(T.resolve("ed")); Files.delete(T.resolve("ed")); return Files.notExists(T.resolve("ed")); });
    probe("exists family", () -> Files.exists(T) + " " + Files.notExists(T) + " " + Files.exists(T.resolve("nope")) + " " + Files.notExists(T.resolve("nope")) + " " + Files.isDirectory(T) + Files.isRegularFile(T) + Files.isSymbolicLink(T) + " " + Files.isReadable(T) + Files.isWritable(T) + Files.isExecutable(T) + " " + Files.isReadable(T.resolve("nope")) + " " + Files.exists(Path.of("")) + Files.isDirectory(Path.of("")));
    probe("exists under file", () -> Files.exists(f.resolve("x")) + " " + Files.notExists(f.resolve("x")));
    probe("symlinks", () -> { Path l = T.resolve("lnk"); Files.createSymbolicLink(l, f.getFileName()); return Files.isSymbolicLink(l) + " " + Files.readSymbolicLink(l) + " " + Files.exists(l) + " " + Files.exists(l, LinkOption.NOFOLLOW_LINKS) + " " + Files.isRegularFile(l) + " " + Files.isRegularFile(l, LinkOption.NOFOLLOW_LINKS) + " " + Files.size(l) + " " + l.toRealPath().getFileName() + " " + l.toRealPath(LinkOption.NOFOLLOW_LINKS).getFileName() + " " + Files.isSameFile(l, f) + " " + Files.readAttributes(l, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).isSymbolicLink(); });
    probe("symlink exists", () -> Files.createSymbolicLink(T.resolve("lnk"), f));
    probe("symlink attrs", () -> Files.createSymbolicLink(T.resolve("lnk2"), f, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))));
    probe("broken symlink", () -> { Path l = T.resolve("broken"); Files.createSymbolicLink(l, T.resolve("nowhere")); return Files.exists(l) + " " + Files.notExists(l) + " " + Files.exists(l, LinkOption.NOFOLLOW_LINKS) + " " + Files.isSymbolicLink(l) + " " + Files.readSymbolicLink(l); });
    probe("broken symlink read", () -> Files.readAllBytes(T.resolve("broken")));
    probe("broken symlink toRealPath", () -> T.resolve("broken").toRealPath());
    probe("broken symlink toRealPath nofollow", () -> T.resolve("broken").toRealPath(LinkOption.NOFOLLOW_LINKS).getFileName());
    probe("readSymbolicLink notlink", () -> Files.readSymbolicLink(f));
    probe("readSymbolicLink missing", () -> Files.readSymbolicLink(T.resolve("missing")));
    probe("delete symlink", () -> { Files.createSymbolicLink(T.resolve("dl"), T.resolve("d1")); Files.delete(T.resolve("dl")); return Files.exists(T.resolve("d1")) + " " + Files.exists(T.resolve("dl"), LinkOption.NOFOLLOW_LINKS); });
    probe("hardlink", () -> { Files.writeString(T.resolve("h1"), "hh"); Files.createLink(T.resolve("h2"), T.resolve("h1")); return Files.isSameFile(T.resolve("h1"), T.resolve("h2")) + " " + Files.getAttribute(T.resolve("h1"), "unix:nlink") + " " + Files.readString(T.resolve("h2")); });
    probe("hardlink exists", () -> Files.createLink(T.resolve("h2"), T.resolve("h1")));
    probe("hardlink missing", () -> Files.createLink(T.resolve("h3"), T.resolve("missing")));
    probe("isSameFile", () -> Files.isSameFile(f, f) + " " + Files.isSameFile(f, T.resolve("./a.txt")) + " " + Files.isSameFile(f, T.resolve("d1")));
    probe("isSameFile missing", () -> Files.isSameFile(f, T.resolve("missing")));
    probe("isSameFile missing equal", () -> Files.isSameFile(T.resolve("missing"), T.resolve("missing")));
    probe("isHidden", () -> Files.isHidden(f) + " " + Files.isHidden(T.resolve(".hid")) + " " + Files.isHidden(Path.of("/")));
    probe("attrs basic", () -> { BasicFileAttributes a = Files.readAttributes(f, BasicFileAttributes.class); return a.isRegularFile() + " " + a.isDirectory() + " " + a.isSymbolicLink() + " " + a.isOther() + " " + a.size() + " " + (a.fileKey() != null) + " " + a.fileKey().getClass().getSimpleName() + " " + (a.creationTime() != null) + " " + (a.lastAccessTime() != null); });
    probe("attrs posix", () -> { PosixFileAttributes a = Files.readAttributes(f, PosixFileAttributes.class); return PosixFilePermissions.toString(a.permissions()).length() + " " + (a.owner().getName().length() > 0) + " " + (a.group().getName().length() > 0) + " " + a.owner().equals(Files.getOwner(f)); });
    probe("attrs dos", () -> { DosFileAttributes a = Files.readAttributes(f, DosFileAttributes.class); return a.isReadOnly() + " " + a.isHidden() + " " + a.isArchive() + " " + a.isSystem(); });
    probe("attrs bad type", () -> Files.readAttributes(f, AclFileAttributeView.class.asSubclass(BasicFileAttributes.class)));
    probe("attrs missing", () -> Files.readAttributes(T.resolve("missing"), BasicFileAttributes.class));
    probe("attrs string", () -> new TreeMap<>(Files.readAttributes(f, "size,isDirectory,isRegularFile")).toString() + " " + new TreeMap<>(Files.readAttributes(f, "basic:*")).keySet() + " " + new TreeMap<>(Files.readAttributes(f, "posix:*")).keySet() + " " + new TreeMap<>(Files.readAttributes(f, "unix:*")).keySet() + " " + new TreeMap<>(Files.readAttributes(f, "dos:*")).keySet() + " " + new TreeMap<>(Files.readAttributes(f, "owner:*")).keySet());
    probe("attrs string bad name", () -> Files.readAttributes(f, "size,nope"));
    probe("attrs string bad view", () -> Files.readAttributes(f, "nope:size"));
    probe("attrs string empty view", () -> Files.readAttributes(f, ":size"));
    probe("attrs string empty list", () -> Files.readAttributes(f, "basic:"));
    probe("attrs string acl", () -> Files.readAttributes(f, "acl:*"));
    probe("getAttribute", () -> Files.getAttribute(f, "size") + " " + Files.getAttribute(f, "basic:isDirectory") + " " + Files.getAttribute(f, "unix:mode").getClass().getSimpleName() + " " + (Files.getAttribute(f, "posix:permissions") instanceof Set));
    probe("getAttribute star", () -> Files.getAttribute(f, "*"));
    probe("getAttribute comma", () -> Files.getAttribute(f, "size,isDirectory"));
    probe("getAttribute unknown", () -> Files.getAttribute(f, "basic:bogus"));
    probe("times", () -> { Path p = T.resolve("times"); Files.writeString(p, "t"); FileTime t1 = FileTime.from(java.time.Instant.parse("2001-02-03T04:05:06.123456789Z")); Files.setLastModifiedTime(p, t1); FileTime back = Files.getLastModifiedTime(p); return back + " " + back.equals(t1) + " " + back.to(java.util.concurrent.TimeUnit.NANOSECONDS); });
    probe("times millis", () -> { Path p = T.resolve("times"); Files.setLastModifiedTime(p, FileTime.fromMillis(-1500)); return Files.getLastModifiedTime(p) + " " + Files.getLastModifiedTime(p).toMillis(); });
    probe("times via view", () -> { Path p = T.resolve("times"); Files.getFileAttributeView(p, BasicFileAttributeView.class).setTimes(FileTime.from(100, java.util.concurrent.TimeUnit.SECONDS), FileTime.from(200, java.util.concurrent.TimeUnit.SECONDS), null); BasicFileAttributes a = Files.readAttributes(p, BasicFileAttributes.class); return a.lastModifiedTime() + " " + a.lastAccessTime(); });
    probe("times only access", () -> { Path p = T.resolve("times"); Files.getFileAttributeView(p, BasicFileAttributeView.class).setTimes(null, FileTime.from(300, java.util.concurrent.TimeUnit.SECONDS), null); BasicFileAttributes a = Files.readAttributes(p, BasicFileAttributes.class); return a.lastModifiedTime() + " " + a.lastAccessTime(); });
    probe("times attr string", () -> { Path p = T.resolve("times"); Files.setAttribute(p, "lastModifiedTime", FileTime.fromMillis(5000)); Files.setAttribute(p, "basic:lastAccessTime", FileTime.fromMillis(6000)); Files.setAttribute(p, "creationTime", FileTime.fromMillis(7000)); return Files.getAttribute(p, "lastModifiedTime") + " " + Files.getAttribute(p, "lastAccessTime"); });
    probe("times bad value", () -> { try { return Files.setAttribute(T.resolve("times"), "lastModifiedTime", "notatime"); } catch (ClassCastException e) { return "cce"; } });
    probe("times null", () -> Files.setLastModifiedTime(T.resolve("times"), null));
    probe("times missing", () -> Files.setLastModifiedTime(T.resolve("missing"), FileTime.fromMillis(0)));
    probe("times symlink", () -> { Path l = T.resolve("tl"); Files.createSymbolicLink(l, T.resolve("times")); Files.getFileAttributeView(l, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setTimes(FileTime.fromMillis(9000000), null, null); return Files.getLastModifiedTime(l, LinkOption.NOFOLLOW_LINKS) + " " + Files.getLastModifiedTime(l); });
    probe("perms", () -> { Path p = T.resolve("perms"); Files.writeString(p, "p"); Files.setPosixFilePermissions(p, PosixFilePermissions.fromString("rwxrwxrwx")); String a = PosixFilePermissions.toString(Files.getPosixFilePermissions(p)); Files.setPosixFilePermissions(p, PosixFilePermissions.fromString("---------")); String b = PosixFilePermissions.toString(Files.getPosixFilePermissions(p)); Files.setPosixFilePermissions(p, EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)); return a + " " + b + " " + PosixFilePermissions.toString(Files.getPosixFilePermissions(p)) + " " + Files.getAttribute(p, "unix:mode") + " " + Files.isReadable(p) + Files.isWritable(p) + Files.isExecutable(p); });
    probe("perms attr string", () -> { Path p = T.resolve("perms"); Files.setAttribute(p, "posix:permissions", PosixFilePermissions.fromString("r--r-----")); return PosixFilePermissions.toString(Files.getPosixFilePermissions(p)) + " " + Files.getAttribute(p, "permissions"); });
    probe("perms unix mode", () -> { Path p = T.resolve("perms"); Files.setAttribute(p, "unix:mode", 0640); return PosixFilePermissions.toString(Files.getPosixFilePermissions(p)); });
    probe("perms missing", () -> Files.setPosixFilePermissions(T.resolve("missing"), EnumSet.noneOf(PosixFilePermission.class)));
    probe("perms null element", () -> Files.setPosixFilePermissions(T.resolve("perms"), new HashSet<>(Arrays.asList((PosixFilePermission) null))));
    probe("perms fromString bad", () -> PosixFilePermissions.fromString("rwxrwx"));
    probe("perms fromString bad2", () -> PosixFilePermissions.fromString("rwxrwxrwz"));
    probe("perms toString", () -> PosixFilePermissions.toString(EnumSet.allOf(PosixFilePermission.class)) + PosixFilePermissions.toString(EnumSet.noneOf(PosixFilePermission.class)));
    probe("perms asFileAttribute", () -> { FileAttribute<Set<PosixFilePermission>> a = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")); try { a.value().add(PosixFilePermission.OTHERS_READ); return "mutable"; } catch (UnsupportedOperationException e) { return a.name() + " immutable"; } });
    probe("owner view", () -> { FileOwnerAttributeView v = Files.getFileAttributeView(f, FileOwnerAttributeView.class); return v.name() + " " + (v.getOwner() != null) + " " + (v instanceof PosixFileAttributeView) + " " + (Files.getFileAttributeView(f, PosixFileAttributeView.class) instanceof FileOwnerAttributeView); });
    probe("owner set same", () -> { Path p = T.resolve("perms"); UserPrincipal o = Files.getOwner(p); Files.setOwner(p, o); return o.equals(Files.getOwner(p)); });
    probe("owner lookup", () -> { UserPrincipalLookupService s = FileSystems.getDefault().getUserPrincipalLookupService(); UserPrincipal root = s.lookupPrincipalByName("root"); GroupPrincipal g = s.lookupPrincipalByGroupName("root"); return root.getName() + " " + g.getName() + " " + (root instanceof GroupPrincipal) + " " + (g instanceof GroupPrincipal) + " " + root.equals(s.lookupPrincipalByName("root")) + " " + root.equals(g); });
    probe("owner lookup missing", () -> FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName("no-such-user-xyz"));
    probe("owner lookup numeric", () -> FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName("0").getName());
    probe("owner set group principal", () -> Files.setOwner(T.resolve("perms"), FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByGroupName("root")));
    probe("owner set denied", () -> Files.setOwner(T.resolve("perms"), FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName("root")));
    probe("view types", () -> { StringBuilder sb = new StringBuilder(); for (Class<? extends FileAttributeView> c : List.of(BasicFileAttributeView.class, PosixFileAttributeView.class, FileOwnerAttributeView.class, DosFileAttributeView.class, UserDefinedFileAttributeView.class, AclFileAttributeView.class, FileAttributeView.class)) { FileAttributeView v = Files.getFileAttributeView(f, c); sb.append(v == null ? "null" : v.name()).append(' '); } return sb; });
    probe("view null", () -> Files.getFileAttributeView(f, null));
    probe("dos set", () -> { Path p = T.resolve("dos"); Files.writeString(p, "d"); DosFileAttributeView v = Files.getFileAttributeView(p, DosFileAttributeView.class); try { v.setHidden(true); v.setReadOnly(true); DosFileAttributes a = v.readAttributes(); return a.isHidden() + " " + a.isReadOnly() + " " + a.isArchive() + " " + a.isSystem(); } catch (IOException e) { return "io:" + e.getClass().getSimpleName(); } });
    probe("user attrs", () -> { Path p = T.resolve("dos"); UserDefinedFileAttributeView v = Files.getFileAttributeView(p, UserDefinedFileAttributeView.class); try { v.write("note", ByteBuffer.wrap("hello".getBytes())); List<String> names = v.list(); int sz = v.size("note"); ByteBuffer bb = ByteBuffer.allocate(sz); v.read("note", bb); v.delete("note"); return names + " " + sz + " " + new String(bb.array()) + " " + v.list(); } catch (IOException e) { return "io:" + e.getClass().getSimpleName(); } });
    probe("user attrs missing", () -> { UserDefinedFileAttributeView v = Files.getFileAttributeView(T.resolve("dos"), UserDefinedFileAttributeView.class); try { return v.size("nope"); } catch (IOException e) { return e.getClass().getSimpleName(); } });
    probe("temp file", () -> { Path p = Files.createTempFile("pre", ".suf"); try { return p.getFileName().toString().startsWith("pre") + " " + p.getFileName().toString().endsWith(".suf") + " " + PosixFilePermissions.toString(Files.getPosixFilePermissions(p)) + " " + p.getParent().equals(Path.of(System.getProperty("java.io.tmpdir"))); } finally { Files.delete(p); } });
    probe("temp file default", () -> { Path p = Files.createTempFile(null, null); try { return p.getFileName().toString().endsWith(".tmp") + " " + Files.size(p); } finally { Files.delete(p); } });
    probe("temp file dir", () -> { Path p = Files.createTempFile(T, "x", "y"); return p.getParent().equals(T) + " " + p.getFileName().toString().matches("x\\d+y"); });
    probe("temp file attrs", () -> { Path p = Files.createTempFile(T, "x", "y", PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-----"))); return PosixFilePermissions.toString(Files.getPosixFilePermissions(p)); });
    probe("temp dir", () -> { Path p = Files.createTempDirectory(T, "td"); return Files.isDirectory(p) + " " + PosixFilePermissions.toString(Files.getPosixFilePermissions(p)) + " " + p.getFileName().toString().matches("td\\d+"); });
    probe("temp dir default", () -> { Path p = Files.createTempDirectory(null); try { return p.getFileName().toString().matches("\\d+"); } finally { Files.delete(p); } });
    probe("temp bad prefix", () -> Files.createTempFile(T, "a/b", "x"));
    probe("temp bad suffix", () -> Files.createTempFile(T, "a", "b/c"));
    probe("temp missing dir", () -> Files.createTempFile(T.resolve("missing"), "a", "b"));
    probe("temp nul", () -> Files.createTempFile(T, "a\0", "b"));
    probe("list", () -> list(T.resolve("p")) + " " + list(T.resolve("p/q")));
    probe("list file", () -> Files.list(f));
    probe("list missing", () -> Files.list(T.resolve("missing")));
    probe("walk", () -> tree(T.resolve("cd")) + " " + Files.walk(T.resolve("cd"), 1).count() + " " + Files.walk(T.resolve("cd"), 0).count());
    probe("walk neg depth", () -> Files.walk(T, -1));
    probe("walk missing", () -> Files.walk(T.resolve("missing")));
    probe("walk file", () -> Files.walk(f).count());
    probe("walk follow links", () -> { Files.createDirectories(T.resolve("wl/a")); Files.writeString(T.resolve("wl/a/x"), "x"); Files.createSymbolicLink(T.resolve("wl/link"), T.resolve("wl/a")); try (Stream<Path> s = Files.walk(T.resolve("wl"), FileVisitOption.FOLLOW_LINKS)) { return s.map(p -> T.resolve("wl").relativize(p).toString()).sorted().collect(Collectors.toList()); } });
    probe("walk loop", () -> { Files.createSymbolicLink(T.resolve("wl/a/loop"), T.resolve("wl")); try (Stream<Path> s = Files.walk(T.resolve("wl"), FileVisitOption.FOLLOW_LINKS)) { return s.count(); } catch (UncheckedIOException e) { return e.getCause().getClass().getSimpleName() + ":" + norm(e.getCause().getMessage()); } });
    probe("walk nofollow loop", () -> { try (Stream<Path> s = Files.walk(T.resolve("wl"))) { return s.map(p -> T.resolve("wl").relativize(p).toString()).sorted().collect(Collectors.toList()); } });
    probe("find", () -> { try (Stream<Path> s = Files.find(T.resolve("cd"), 5, (p, a) -> a.isRegularFile())) { return s.map(p -> T.resolve("cd").relativize(p).toString()).collect(Collectors.toList()); } });
    probe("walkFileTree", () -> { StringBuilder sb = new StringBuilder(); Files.walkFileTree(T.resolve("cd"), new SimpleFileVisitor<Path>() { public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) { sb.append("pre:").append(T.relativize(d)).append(' '); return FileVisitResult.CONTINUE; } public FileVisitResult visitFile(Path p, BasicFileAttributes a) { sb.append("file:").append(T.relativize(p)).append(' '); return FileVisitResult.CONTINUE; } public FileVisitResult postVisitDirectory(Path d, IOException e) { sb.append("post:").append(T.relativize(d)).append(' '); return FileVisitResult.CONTINUE; } }); return sb; });
    probe("walkFileTree skip", () -> { Files.createDirectories(T.resolve("wt/a")); Files.createDirectories(T.resolve("wt/b")); Files.writeString(T.resolve("wt/a/1"), "1"); Files.writeString(T.resolve("wt/b/2"), "2"); Files.writeString(T.resolve("wt/z"), "z"); StringBuilder sb = new StringBuilder(); Files.walkFileTree(T.resolve("wt"), new SimpleFileVisitor<Path>() { public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) { sb.append("pre:").append(T.relativize(d)).append(' '); return d.getFileName().toString().equals("a") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE; } public FileVisitResult visitFile(Path p, BasicFileAttributes a) { sb.append("file:").append(T.relativize(p)).append(' '); return FileVisitResult.CONTINUE; } public FileVisitResult postVisitDirectory(Path d, IOException e) { sb.append("post:").append(T.relativize(d)).append(' '); return FileVisitResult.CONTINUE; } }); return sb.toString().replaceAll("(?<=\\s|^)(file|pre|post):wt/[ab]/?[12]? ", "$0").length() > 0; });
    probe("walkFileTree terminate", () -> { int[] n = {0}; Files.walkFileTree(T.resolve("wt"), new SimpleFileVisitor<Path>() { public FileVisitResult visitFile(Path p, BasicFileAttributes a) { n[0]++; return FileVisitResult.TERMINATE; } }); return n[0]; });
    probe("walkFileTree siblings", () -> { Files.createDirectories(T.resolve("ws/d")); for (String n : new String[] {"1", "2", "3"}) Files.writeString(T.resolve("ws/d/" + n), n); int[] c = {0}; Files.walkFileTree(T.resolve("ws"), new SimpleFileVisitor<Path>() { public FileVisitResult visitFile(Path p, BasicFileAttributes a) { c[0]++; return FileVisitResult.SKIP_SIBLINGS; } }); return c[0]; });
    probe("walkFileTree missing", () -> { StringBuilder sb = new StringBuilder(); Files.walkFileTree(T.resolve("missing"), new SimpleFileVisitor<Path>() { public FileVisitResult visitFileFailed(Path p, IOException e) { sb.append(e.getClass().getSimpleName()).append(':').append(norm(e.getMessage())); return FileVisitResult.CONTINUE; } }); return sb; });
    probe("walkFileTree null result", () -> Files.walkFileTree(T.resolve("wt"), new SimpleFileVisitor<Path>() { public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) { return null; } }));
    probe("walkFileTree depth", () -> { int[] n = {0}; Files.walkFileTree(T.resolve("wt"), EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() { public FileVisitResult visitFile(Path p, BasicFileAttributes a) { n[0]++; return FileVisitResult.CONTINUE; } }); return n[0]; });
    probe("walkFileTree bad depth", () -> Files.walkFileTree(T, EnumSet.noneOf(FileVisitOption.class), -1, new SimpleFileVisitor<Path>() { }));
    probe("directory stream", () -> { Files.createDirectories(T.resolve("ds")); for (String n : new String[] {"a.java", "b.txt", "c.java", ".hid"}) Files.writeString(T.resolve("ds/" + n), n); List<String> all = new ArrayList<>(); try (DirectoryStream<Path> s = Files.newDirectoryStream(T.resolve("ds"))) { for (Path p : s) all.add(p.getFileName().toString()); } Collections.sort(all); List<String> globbed = new ArrayList<>(); try (DirectoryStream<Path> s = Files.newDirectoryStream(T.resolve("ds"), "*.java")) { for (Path p : s) globbed.add(p.getFileName().toString()); } Collections.sort(globbed); List<String> filtered = new ArrayList<>(); try (DirectoryStream<Path> s = Files.newDirectoryStream(T.resolve("ds"), p -> p.toString().endsWith("txt"))) { for (Path p : s) filtered.add(p.getFileName().toString()); } return all + " " + globbed + " " + filtered; });
    probe("directory stream twice", () -> { try (DirectoryStream<Path> s = Files.newDirectoryStream(T.resolve("ds"))) { s.iterator(); return s.iterator(); } });
    probe("directory stream closed", () -> { DirectoryStream<Path> s = Files.newDirectoryStream(T.resolve("ds")); s.close(); return s.iterator(); });
    probe("directory stream closed during", () -> { DirectoryStream<Path> s = Files.newDirectoryStream(T.resolve("ds")); Iterator<Path> it = s.iterator(); s.close(); return it.hasNext(); });
    probe("directory stream remove", () -> { try (DirectoryStream<Path> s = Files.newDirectoryStream(T.resolve("ds"))) { Iterator<Path> it = s.iterator(); it.next(); it.remove(); return "removed"; } });
    probe("directory stream file", () -> Files.newDirectoryStream(f));
    probe("directory stream missing", () -> Files.newDirectoryStream(T.resolve("missing")));
    probe("directory stream filter ioe", () -> { try (DirectoryStream<Path> s = Files.newDirectoryStream(T.resolve("ds"), p -> { throw new IOException("boom"); })) { return s.iterator().hasNext(); } catch (DirectoryIteratorException e) { return e.getCause().getMessage(); } });
    probe("directory stream exhausted", () -> { try (DirectoryStream<Path> s = Files.newDirectoryStream(T.resolve("p/q/r"))) { Iterator<Path> it = s.iterator(); boolean h = it.hasNext(); try { it.next(); } catch (NoSuchElementException e) { return h + " nsee"; } return h + " none"; } });
    probe("probeContentType", () -> { StringBuilder sb = new StringBuilder(); for (String n : new String[] {"a.txt", "a.html", "a.png", "a.xyz", "a.tar.gz", "a", "A.TXT", "a.json", "a.java", "a.css", "a.csv", "a.pdf", "a.zip", "a.jpeg", "a.mp3", "a.svg", "a.xml", "a.js", "a.md"}) { sb.append(Files.probeContentType(Path.of(n))).append(' '); } return sb; });
    probe("file store", () -> { FileStore s = Files.getFileStore(T); return (s.name() != null) + " " + (s.type() != null) + " " + (s.getTotalSpace() > 0) + " " + (s.getUsableSpace() > 0) + " " + (s.getUnallocatedSpace() >= s.getUsableSpace()) + " " + (s.getBlockSize() > 0) + " " + s.supportsFileAttributeView("basic") + s.supportsFileAttributeView("posix") + s.supportsFileAttributeView("owner") + s.supportsFileAttributeView("unix") + s.supportsFileAttributeView("nope") + " " + s.supportsFileAttributeView(BasicFileAttributeView.class) + s.supportsFileAttributeView(PosixFileAttributeView.class) + " " + (s.getFileStoreAttributeView(FileStoreAttributeView.class) == null) + " " + (s.getAttribute("totalSpace") instanceof Long) + " " + s.equals(Files.getFileStore(f)); });
    probe("file store attr bad", () -> Files.getFileStore(T).getAttribute("nope"));
    probe("file store missing", () -> Files.getFileStore(T.resolve("missing")));
    probe("file stores iter", () -> { int n = 0; for (FileStore s : FileSystems.getDefault().getFileStores()) { n++; s.name(); } return n > 0; });
    probe("channel basic", () -> { Path p = T.resolve("ch"); try (FileChannel c = FileChannel.open(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) { int w = c.write(ByteBuffer.wrap("0123456789".getBytes())); long pos = c.position(); c.position(2); ByteBuffer bb = ByteBuffer.allocate(4); int r = c.read(bb); long pos2 = c.position(); ByteBuffer bb2 = ByteBuffer.allocate(3); int r2 = c.read(bb2, 7); long pos3 = c.position(); int w2 = c.write(ByteBuffer.wrap("AB".getBytes()), 0); c.truncate(8); long sz = c.size(); c.position(20); ByteBuffer bb3 = ByteBuffer.allocate(1); int r3 = c.read(bb3); c.force(true); return w + " " + pos + " " + r + " " + pos2 + " " + new String(bb.array()) + " " + r2 + " " + new String(bb2.array(), 0, 1) + " " + pos3 + " " + w2 + " " + sz + " " + r3 + " " + c.position() + " " + new String(Files.readAllBytes(p)); } });
    probe("channel write beyond", () -> { Path p = T.resolve("ch2"); try (FileChannel c = FileChannel.open(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) { c.position(5); c.write(ByteBuffer.wrap("x".getBytes())); return c.size() + " " + Arrays.toString(Files.readAllBytes(p)); } });
    probe("channel read-only write", () -> { try (FileChannel c = FileChannel.open(f)) { return c.write(ByteBuffer.wrap("x".getBytes())); } });
    probe("channel write-only read", () -> { try (FileChannel c = FileChannel.open(f, StandardOpenOption.WRITE)) { return c.read(ByteBuffer.allocate(1)); } });
    probe("channel closed", () -> { FileChannel c = FileChannel.open(f); c.close(); return c.size(); });
    probe("channel closed isOpen", () -> { FileChannel c = FileChannel.open(f); boolean a = c.isOpen(); c.close(); c.close(); return a + " " + c.isOpen(); });
    probe("channel neg pos", () -> { try (FileChannel c = FileChannel.open(f)) { return c.position(-1); } });
    probe("channel neg read pos", () -> { try (FileChannel c = FileChannel.open(f)) { return c.read(ByteBuffer.allocate(1), -1); } });
    probe("channel missing", () -> FileChannel.open(T.resolve("missing")));
    probe("channel create new exists", () -> FileChannel.open(f, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE));
    probe("channel append", () -> { Path p = T.resolve("cha"); Files.writeString(p, "base"); try (FileChannel c = FileChannel.open(p, StandardOpenOption.APPEND)) { c.write(ByteBuffer.wrap("+x".getBytes())); long pos = c.position(); long sz = c.size(); return pos + " " + sz + " " + Files.readString(p); } });
    probe("channel delete on close", () -> { Path p = T.resolve("choc"); try (FileChannel c = FileChannel.open(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.DELETE_ON_CLOSE)) { c.write(ByteBuffer.wrap("x".getBytes())); } return Files.exists(p); });
    probe("channel sparse option", () -> { try (FileChannel c = FileChannel.open(T.resolve("sp"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SPARSE)) { return c.size(); } });
    probe("channel nofollow", () -> FileChannel.open(T.resolve("lnk"), LinkOption.NOFOLLOW_LINKS));
    probe("channel nofollow plain", () -> { try (FileChannel c = FileChannel.open(f, LinkOption.NOFOLLOW_LINKS)) { return c.size(); } });
    probe("channel sync", () -> { try (FileChannel c = FileChannel.open(T.resolve("syn"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SYNC, StandardOpenOption.DSYNC)) { c.write(ByteBuffer.wrap("s".getBytes())); return c.size(); } });
    probe("channel transfer", () -> { Path a = T.resolve("ta"), b = T.resolve("tb"); Files.writeString(a, "transfer-me-please"); try (FileChannel in = FileChannel.open(a); FileChannel out = FileChannel.open(b, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) { long n = in.transferTo(3, 8, out); long m = out.transferFrom(in, n, 4); return n + " " + m + " " + Files.readString(b) + " " + in.transferTo(100, 5, out) + " " + in.position(); } });
    probe("channel transfer neg", () -> { try (FileChannel in = FileChannel.open(f)) { return in.transferTo(-1, 1, in); } });
    probe("channel transfer nonwritable", () -> { try (FileChannel in = FileChannel.open(f); FileChannel out = FileChannel.open(T.resolve("ta"))) { return in.transferTo(0, 1, out); } });
    probe("channel scatter gather", () -> { Path p = T.resolve("sg"); try (FileChannel c = FileChannel.open(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) { long w = c.write(new ByteBuffer[] {ByteBuffer.wrap("ab".getBytes()), ByteBuffer.wrap("cde".getBytes())}); c.position(0); ByteBuffer x = ByteBuffer.allocate(3), y = ByteBuffer.allocate(4); long r = c.read(new ByteBuffer[] {x, y}); return w + " " + r + " " + new String(x.array()) + " " + new String(y.array(), 0, 2) + " " + y.remaining(); } });
    probe("channel direct buffer", () -> { Path p = T.resolve("db"); try (FileChannel c = FileChannel.open(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) { ByteBuffer d = ByteBuffer.allocateDirect(8); d.put("direct!!".getBytes()).flip(); int w = c.write(d); c.position(0); ByteBuffer e = ByteBuffer.allocateDirect(8); int r = c.read(e); e.flip(); byte[] out = new byte[e.remaining()]; e.get(out); return w + " " + r + " " + new String(out); } });
    probe("channel readonly buffer", () -> { try (FileChannel c = FileChannel.open(f)) { return c.read(ByteBuffer.allocate(1).asReadOnlyBuffer()); } });
    probe("channel lock", () -> { Path p = T.resolve("lk"); try (FileChannel c = FileChannel.open(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) { FileLock l = c.lock(); boolean v1 = l.isValid(); String s = l.toString().replaceAll("sun.nio.ch.\\w+", "L"); String ov; try { c.tryLock(0, 5, false); ov = "no"; } catch (OverlappingFileLockException e) { ov = "overlap"; } l.release(); boolean v2 = l.isValid(); FileLock l2 = c.tryLock(0, 10, true); return v1 + " " + s + " " + ov + " " + v2 + " " + (l2 != null) + " " + l2.isShared() + " " + l2.position() + " " + l2.size() + " " + l2.channel().equals(c) + " " + l2.acquiredBy().equals(c); } });
    probe("channel lock shared nonreadable", () -> { try (FileChannel c = FileChannel.open(T.resolve("lk"), StandardOpenOption.WRITE)) { return c.lock(0, 1, true); } });
    probe("channel lock excl nonwritable", () -> { try (FileChannel c = FileChannel.open(T.resolve("lk"))) { return c.lock(); } });
    probe("channel lock neg", () -> { try (FileChannel c = FileChannel.open(T.resolve("lk"), StandardOpenOption.WRITE)) { return c.lock(-1, 1, false); } });
    probe("channel lock release closed", () -> { FileChannel c = FileChannel.open(T.resolve("lk"), StandardOpenOption.WRITE); FileLock l = c.lock(); c.close(); return l.isValid(); });
    probe("channel lock two channels", () -> { try (FileChannel a = FileChannel.open(T.resolve("lk"), StandardOpenOption.WRITE); FileChannel b = FileChannel.open(T.resolve("lk"), StandardOpenOption.WRITE)) { FileLock l = a.lock(); try { b.tryLock(); return "got"; } catch (OverlappingFileLockException e) { return "overlap"; } finally { l.close(); } } });
    probe("channel map rw", () -> { Path p = T.resolve("map"); Files.write(p, "0123456789".getBytes()); try (FileChannel c = FileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE)) { MappedByteBuffer m = c.map(FileChannel.MapMode.READ_WRITE, 2, 5); byte b0 = m.get(0); m.put(1, (byte) 'X'); m.force(); String content = Files.readString(p); return b0 + " " + m.capacity() + " " + m.isDirect() + " " + m.isReadOnly() + " " + content; } });
    probe("channel map grow", () -> { Path p = T.resolve("map2"); Files.write(p, "ab".getBytes()); try (FileChannel c = FileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE)) { MappedByteBuffer m = c.map(FileChannel.MapMode.READ_WRITE, 0, 6); m.put(5, (byte) 'z'); m.force(); return c.size() + " " + Arrays.toString(Files.readAllBytes(p)); } });
    probe("channel map ro", () -> { try (FileChannel c = FileChannel.open(T.resolve("map"), StandardOpenOption.READ)) { MappedByteBuffer m = c.map(FileChannel.MapMode.READ_ONLY, 0, 4); try { m.put(0, (byte) 1); return "wrote"; } catch (java.nio.ReadOnlyBufferException e) { return m.isReadOnly() + " " + (char) m.get(0); } } });
    probe("channel map private", () -> { Path p = T.resolve("map"); try (FileChannel c = FileChannel.open(p, StandardOpenOption.READ, StandardOpenOption.WRITE)) { String before = Files.readString(p); MappedByteBuffer m = c.map(FileChannel.MapMode.PRIVATE, 0, 3); m.put(0, (byte) 'Q'); m.force(); return (char) m.get(0) + " " + before.equals(Files.readString(p)); } });
    probe("channel map ro on rw channel needs write", () -> { try (FileChannel c = FileChannel.open(T.resolve("map"), StandardOpenOption.READ)) { return c.map(FileChannel.MapMode.READ_WRITE, 0, 1); } });
    probe("channel map bad args", () -> { try (FileChannel c = FileChannel.open(T.resolve("map"), StandardOpenOption.READ)) { StringBuilder sb = new StringBuilder(); for (long[] a : new long[][] {{-1, 1}, {0, -1}, {0, Integer.MAX_VALUE + 1L}, {Long.MAX_VALUE, 5}}) { try { c.map(FileChannel.MapMode.READ_ONLY, a[0], a[1]); sb.append("ok "); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(' '); } } try { c.map(null, 0, 1); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()); } return sb; } });
    probe("channel map empty", () -> { try (FileChannel c = FileChannel.open(T.resolve("map"), StandardOpenOption.READ)) { MappedByteBuffer m = c.map(FileChannel.MapMode.READ_ONLY, 0, 0); return m.capacity() + " " + m.isDirect(); } });
    probe("channel map read beyond", () -> { try (FileChannel c = FileChannel.open(T.resolve("map"), StandardOpenOption.READ)) { return c.map(FileChannel.MapMode.READ_ONLY, 0, 100); } });
    probe("channel truncate readonly", () -> { try (FileChannel c = FileChannel.open(f)) { return c.truncate(0); } });
    probe("channel truncate neg", () -> { try (FileChannel c = FileChannel.open(T.resolve("ch"), StandardOpenOption.WRITE)) { return c.truncate(-1); } });
    probe("channel truncate position", () -> { Path p = T.resolve("tp"); Files.writeString(p, "0123456789"); try (FileChannel c = FileChannel.open(p, StandardOpenOption.WRITE)) { c.position(9); c.truncate(4); long a = c.position(); c.truncate(100); return a + " " + c.size() + " " + c.position(); } });
    probe("byte channel", () -> { try (SeekableByteChannel c = Files.newByteChannel(T.resolve("bc"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) { c.write(ByteBuffer.wrap("seekable".getBytes())); c.position(4); ByteBuffer b = ByteBuffer.allocate(4); c.read(b); return c.getClass().getSimpleName().length() > 0 && c instanceof FileChannel ? new String(b.array()) + " " + c.size() : "other"; } });
    probe("byte channel no options", () -> { try (SeekableByteChannel c = Files.newByteChannel(f)) { return c.size() >= 0; } });
    probe("channels streams", () -> { Path p = T.resolve("cs2"); try (OutputStream o = Channels.newOutputStream(FileChannel.open(p, StandardOpenOption.CREATE, StandardOpenOption.WRITE))) { o.write("chan-out".getBytes()); } try (InputStream i = Channels.newInputStream(FileChannel.open(p))) { return new String(i.readAllBytes()); } });
    probe("fis channel", () -> { try (FileInputStream in = new FileInputStream(T.resolve("cs2").toFile())) { FileChannel c = in.getChannel(); ByteBuffer b = ByteBuffer.allocate(4); c.read(b); int next = in.read(); return new String(b.array()) + " " + (char) next + " " + c.position() + " " + c.size(); } });
    probe("fos channel", () -> { Path p = T.resolve("fos"); try (FileOutputStream out = new FileOutputStream(p.toFile())) { out.write('a'); FileChannel c = out.getChannel(); c.write(ByteBuffer.wrap("bc".getBytes())); out.write('d'); return c.position() + " " + Files.readString(p); } });
    probe("fos append channel", () -> { Path p = T.resolve("fos"); try (FileOutputStream out = new FileOutputStream(p.toFile(), true)) { FileChannel c = out.getChannel(); c.write(ByteBuffer.wrap("Z".getBytes())); return c.position() + " " + Files.readString(p); } });
    probe("raf channel", () -> { Path p = T.resolve("raf"); try (RandomAccessFile r = new RandomAccessFile(p.toFile(), "rw")) { r.writeBytes("raf-data"); FileChannel c = r.getChannel(); c.position(4); ByteBuffer b = ByteBuffer.allocate(4); c.read(b); return new String(b.array()) + " " + r.getFilePointer() + " " + c.size(); } });
    probe("stream closes channel", () -> { FileInputStream in = new FileInputStream(f.toFile()); FileChannel c = in.getChannel(); in.close(); return c.isOpen(); });
    probe("channel closes stream", () -> { FileInputStream in = new FileInputStream(f.toFile()); FileChannel c = in.getChannel(); c.close(); try { in.read(); return "read"; } catch (IOException e) { return e.getMessage(); } });
    probe("path io interplay", () -> { Path p = T.resolve("io.txt"); p.toFile().createNewFile(); return Files.exists(p) + " " + p.toFile().length() + " " + new File(p.toString()).toPath().equals(p) + " " + p.toFile().toPath().getFileSystem().equals(FileSystems.getDefault()); });
    probe("watch register", () -> { try (WatchService ws = FileSystems.getDefault().newWatchService()) { WatchKey k = T.resolve("ds").register(ws, StandardWatchEventKinds.ENTRY_CREATE); String r = k.isValid() + " " + k.watchable().equals(T.resolve("ds")) + " " + k.pollEvents().size() + " " + (ws.poll() == null); k.cancel(); return r + " " + k.isValid(); } });
    probe("watch register file", () -> { try (WatchService ws = FileSystems.getDefault().newWatchService()) { return f.register(ws, StandardWatchEventKinds.ENTRY_CREATE); } });
    probe("watch register missing", () -> { try (WatchService ws = FileSystems.getDefault().newWatchService()) { return T.resolve("missing").register(ws, StandardWatchEventKinds.ENTRY_CREATE); } });
    probe("watch register no kinds", () -> { try (WatchService ws = FileSystems.getDefault().newWatchService()) { return T.register(ws); } });
    probe("watch register overflow only", () -> { try (WatchService ws = FileSystems.getDefault().newWatchService()) { return T.register(ws, StandardWatchEventKinds.OVERFLOW); } });
    probe("watch closed", () -> { WatchService ws = FileSystems.getDefault().newWatchService(); ws.close(); try { ws.poll(); return "polled"; } catch (ClosedWatchServiceException e) { return "closed"; } });
    probe("watch closed register", () -> { WatchService ws = FileSystems.getDefault().newWatchService(); ws.close(); return T.register(ws, StandardWatchEventKinds.ENTRY_CREATE); });
    probe("watch events", () -> { Path d = Files.createDirectory(T.resolve("wev")); try (WatchService ws = FileSystems.getDefault().newWatchService()) { d.register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY); Files.writeString(d.resolve("n1"), "x"); WatchKey k = ws.poll(15, java.util.concurrent.TimeUnit.SECONDS); if (k == null) return "no event"; Set<String> kinds = new TreeSet<>(); for (WatchEvent<?> e : k.pollEvents()) kinds.add(e.kind().name() + ":" + e.context()); boolean again = k.reset(); Files.delete(d.resolve("n1")); WatchKey k2 = ws.poll(15, java.util.concurrent.TimeUnit.SECONDS); Set<String> kinds2 = new TreeSet<>(); if (k2 != null) for (WatchEvent<?> e : k2.pollEvents()) kinds2.add(e.kind().name() + ":" + e.context()); return kinds.contains("ENTRY_CREATE:n1") + " " + again + " " + kinds2.contains("ENTRY_DELETE:n1") + " " + (k == k2); } });
    probe("fs provider", () -> FileSystems.getDefault().provider().getScheme() + " " + FileSystemProvider_installed());
    probe("fs getFileSystem uri", () -> (FileSystems.getFileSystem(java.net.URI.create("file:///")) == FileSystems.getDefault()));
    probe("fs getFileSystem bad", () -> FileSystems.getFileSystem(java.net.URI.create("file:///x")));
    probe("fs getFileSystem unknown", () -> FileSystems.getFileSystem(java.net.URI.create("nope:///")));
    probe("fs newFileSystem uri", () -> FileSystems.newFileSystem(java.net.URI.create("file:///"), Map.of()));
    probe("fs newFileSystem unknown", () -> FileSystems.newFileSystem(java.net.URI.create("nope:///"), Map.of()));
    probe("fs newFileSystem path", () -> FileSystems.newFileSystem(f));
    probe("fs newFileSystem path null", () -> FileSystems.newFileSystem((Path) null));
    probe("path of uri roundtrip", () -> { Path p = T.resolve("sp ace#?%"); Files.writeString(p, "x"); return Path.of(p.toUri()).equals(p) + " " + norm(p.toUri().toString()); });
    probe("path toUri dir", () -> norm(T.resolve("d1").toUri().toString()) + " " + norm(T.resolve("missing").toUri().toString()) + " " + norm(f.toUri().toString()));
    probe("symlink loop io", () -> { Path a = T.resolve("loopa"); Path b = T.resolve("loopb"); Files.createSymbolicLink(a, b); Files.createSymbolicLink(b, a); return Files.readAllBytes(a); });
    probe("long name", () -> Files.createFile(T.resolve("x".repeat(300))));
    probe("unicode names", () -> { Path p = T.resolve("caf\u00e9-\u4e2d-\ud83d\ude00"); Files.writeString(p, "u"); boolean e = Files.exists(p); String l = list(T).contains("caf\u00e9-\u4e2d-\ud83d\ude00") + ""; return e + " " + l + " " + p.getFileName().toString().length() + " " + Files.readString(p); });
    probe("empty path ops", () -> Files.isDirectory(Path.of("")) + " " + Files.exists(Path.of("")) + " " + Path.of("").toAbsolutePath().equals(Path.of(System.getProperty("user.dir"))) + " " + Files.isSameFile(Path.of(""), Path.of(".")));
    probe("relative io", () -> { Path p = Path.of("fm-relative-test.tmp"); Files.writeString(p, "rel"); try { return Files.readString(p) + " " + Files.size(p) + " " + p.toAbsolutePath().getFileName() + " " + Files.exists(p.toAbsolutePath()); } finally { Files.delete(p); } });
    probe("nul in names", () -> Files.exists(Path.of("a\0b")));
    probe("null args", () -> { StringBuilder sb = new StringBuilder(); Body[] bs = {() -> Files.exists(null), () -> Files.size(null), () -> Files.readAllBytes(null), () -> Files.copy(f, (Path) null), () -> Files.createFile(null), () -> Files.newDirectoryStream(f, (String) null), () -> Files.walk(null), () -> Files.write(f, (byte[]) null), () -> Files.readString(f, null)}; for (Body b : bs) { try { b.get(); sb.append("ok "); } catch (Throwable t) { sb.append(t.getClass().getSimpleName()).append(' '); } } return sb; });
    try (Stream<Path> s = Files.walk(T)) {
      s.sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException e) { } });
    }
    System.out.println("cleaned=" + Files.notExists(T));
  }

  static String FileSystemProvider_installed() {
    return java.nio.file.spi.FileSystemProvider.installedProviders().get(0).getScheme();
  }
}
