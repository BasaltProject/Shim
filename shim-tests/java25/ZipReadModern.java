/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.*;
import java.util.stream.*;
import java.util.zip.*;

public class ZipReadModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  static String show(ZipEntry e) { return e.getName() + " m=" + e.getMethod() + " sz=" + e.getSize() + " csz=" + e.getCompressedSize() + " crc=" + Long.toHexString(e.getCrc()) + " t=" + e.getTime() + " c=" + e.getComment() + " dir=" + e.isDirectory() + " x=" + (e.getExtra() == null ? "null" : e.getExtra().length) + " ma=" + e.getLastModifiedTime() + " aa=" + e.getLastAccessTime() + " ca=" + e.getCreationTime(); }
  public static void main(String[] a) throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Path dir = Files.createTempDirectory("zf1");
    Path z = dir.resolve("t.zip");
    final long T = 1700000000000L;
    try (ZipOutputStream o = new ZipOutputStream(Files.newOutputStream(z))) {
      o.setComment("archive comment");
      ZipEntry e1 = new ZipEntry("a.txt"); e1.setTime(T); e1.setComment("entry one"); o.putNextEntry(e1); o.write("hello hello hello hello".getBytes());
      ZipEntry e2 = new ZipEntry("dir/"); e2.setTime(T + 3000); o.putNextEntry(e2);
      ZipEntry e3 = new ZipEntry("dir/b.bin"); e3.setTime(T + 5000); e3.setLastAccessTime(FileTime.fromMillis(T)); e3.setExtra(new byte[] {(byte) 0xca, (byte) 0xfe, 2, 0, 1, 2}); o.putNextEntry(e3); o.write(new byte[1000]);
      ZipEntry e4 = new ZipEntry("s.txt"); e4.setTime(T); e4.setMethod(ZipEntry.STORED); byte[] d = "stored".getBytes(); CRC32 c = new CRC32(); c.update(d); e4.setSize(d.length); e4.setCrc(c.getValue()); o.putNextEntry(e4); o.write(d);
      ZipEntry e5 = new ZipEntry("é/ü.txt"); e5.setTime(T); o.putNextEntry(e5); o.write(7);
      ZipEntry e6 = new ZipEntry("empty"); e6.setTime(T); o.putNextEntry(e6);
    }
    try (ZipFile zf = new ZipFile(z.toFile())) {
      t("name", () -> zf.getName().equals(z.toString()) + " " + zf.getComment() + " " + zf.size());
      t("entries", () -> { StringBuilder sb = new StringBuilder(); Enumeration<? extends ZipEntry> en = zf.entries(); while (en.hasMoreElements()) sb.append(show(en.nextElement())).append('\n'); return sb; });
      t("stream", () -> zf.stream().map(ZipEntry::getName).collect(Collectors.toList()));
      t("getEntry", () -> show(zf.getEntry("a.txt")) + " | " + zf.getEntry("nope") + " | " + show(zf.getEntry("dir")) + " | " + show(zf.getEntry("dir/")) + " | " + zf.getEntry("/a.txt") + " | " + zf.getEntry(""));
      t("getEntryNull", () -> zf.getEntry(null));
      t("read", () -> { try (InputStream in = zf.getInputStream(zf.getEntry("a.txt"))) { return new String(in.readAllBytes()); } });
      t("readStored", () -> { try (InputStream in = zf.getInputStream(zf.getEntry("s.txt"))) { return new String(in.readAllBytes()) + " " + in.available(); } });
      t("readBig", () -> { try (InputStream in = zf.getInputStream(zf.getEntry("dir/b.bin"))) { int n = in.available(); byte[] b = in.readNBytes(10); return n + " " + b.length + " " + in.available() + " " + in.readAllBytes().length + " " + in.available() + " " + in.read(); } });
      t("readUni", () -> { try (InputStream in = zf.getInputStream(zf.getEntry("é/ü.txt"))) { return in.read(); } });
      t("readEmpty", () -> { try (InputStream in = zf.getInputStream(zf.getEntry("empty"))) { return in.read() + " " + in.available(); } });
      t("readDir", () -> { try (InputStream in = zf.getInputStream(zf.getEntry("dir/"))) { return in.read(); } });
      t("getInputStreamNull", () -> zf.getInputStream(null));
      t("getInputStreamForeign", () -> zf.getInputStream(new ZipEntry("nope")));
      t("skip", () -> { try (InputStream in = zf.getInputStream(zf.getEntry("a.txt"))) { return in.skip(6) + " " + (char) in.read(); } });
      t("readPartial", () -> { try (InputStream in = zf.getInputStream(zf.getEntry("a.txt"))) { byte[] b = new byte[5]; return in.read(b, 0, 3) + " " + new String(b, 0, 3) + " " + in.read(b, 0, 0) + " " + in.transferTo(OutputStream.nullOutputStream()); } });
      t("streamAfterClose", () -> { ZipFile z2 = new ZipFile(z.toFile()); InputStream in = z2.getInputStream(z2.getEntry("a.txt")); z2.close(); try { return in.read(); } catch (IOException e) { return e.getMessage(); } });
    }
    t("afterClose", () -> { ZipFile z2 = new ZipFile(z.toFile()); z2.close(); z2.close(); StringBuilder sb = new StringBuilder(); try { z2.size(); } catch (Exception e) { sb.append(e.getClass().getName() + ":" + e.getMessage() + " "); } try { z2.getEntry("a.txt"); } catch (Exception e) { sb.append(e.getMessage() + " "); } try { z2.entries(); } catch (Exception e) { sb.append(e.getMessage() + " "); } try { z2.stream(); } catch (Exception e) { sb.append(e.getMessage() + " "); } try { z2.getComment(); } catch (Exception e) { sb.append(e.getMessage() + " "); } sb.append(z2.getName().equals(z.toString())); return sb; });
    t("enumAfterClose", () -> { ZipFile z2 = new ZipFile(z.toFile()); Enumeration<? extends ZipEntry> en = z2.entries(); z2.close(); try { en.hasMoreElements(); return "no"; } catch (IllegalStateException e) { return e.getMessage(); } });
    t("enumExhaust", () -> { try (ZipFile z2 = new ZipFile(z.toFile())) { Enumeration<? extends ZipEntry> en = z2.entries(); while (en.hasMoreElements()) en.nextElement(); try { en.nextElement(); return "no"; } catch (NoSuchElementException e) { return "NSEE"; } } });
    t("missing", () -> { try { new ZipFile(dir.resolve("missing.zip").toString()); return 1; } catch (java.nio.file.NoSuchFileException e) { return e.getClass().getName(); } });
    
    
    Path empty = dir.resolve("empty.zip"); Files.write(empty, new byte[0]);
    t("emptyFile", () -> new ZipFile(empty.toFile()));
    Path junk = dir.resolve("junk.zip"); Files.write(junk, "this is not a zip file at all".getBytes());
    t("junk", () -> new ZipFile(junk.toFile()));
    Path emptyZip = dir.resolve("e.zip"); try (ZipOutputStream o = new ZipOutputStream(Files.newOutputStream(emptyZip))) { }
    t("emptyZip", () -> { try (ZipFile zf = new ZipFile(emptyZip.toFile())) { return zf.size() + " " + zf.getComment() + " " + zf.entries().hasMoreElements() + " " + zf.stream().count(); } });
    t("badMode", () -> new ZipFile(z.toFile(), 0));
    t("badMode2", () -> new ZipFile(z.toFile(), 2));
    t("badMode3", () -> new ZipFile(z.toFile(), 8));
    t("nullCharset", () -> new ZipFile(z.toFile(), (java.nio.charset.Charset) null));
    t("nullFile", () -> new ZipFile((File) null));
    t("nullName", () -> new ZipFile((String) null));
    t("delete", () -> { Path c2 = dir.resolve("del.zip"); Files.copy(z, c2); new ZipFile(c2.toFile(), ZipFile.OPEN_READ | ZipFile.OPEN_DELETE).close(); return Files.exists(c2); });
    t("latin1", () -> { Path l = dir.resolve("l.zip"); try (ZipOutputStream o = new ZipOutputStream(Files.newOutputStream(l), StandardCharsets.ISO_8859_1)) { o.putNextEntry(new ZipEntry("é.txt")); o.setComment("cé"); } try (ZipFile zf = new ZipFile(l.toFile(), StandardCharsets.ISO_8859_1)) { return zf.getEntry("é.txt") + " " + zf.getComment(); } });
    t("jar", () -> {
      Path j = dir.resolve("m.jar");
      Manifest mf = new Manifest(); mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); mf.getMainAttributes().putValue("Multi-Release", "true"); mf.getMainAttributes().putValue("Main-Class", "x.Y");
      Attributes ea = new Attributes(); ea.putValue("Foo", "bar"); mf.getEntries().put("a.txt", ea);
      try (JarOutputStream jo = new JarOutputStream(Files.newOutputStream(j), mf)) {
        jo.putNextEntry(new JarEntry("a.txt")); jo.write(1);
        jo.putNextEntry(new JarEntry("META-INF/versions/9/a.txt")); jo.write(9);
        jo.putNextEntry(new JarEntry("META-INF/versions/11/a.txt")); jo.write(11);
        jo.putNextEntry(new JarEntry("META-INF/versions/9/only9.txt")); jo.write(99);
        jo.putNextEntry(new JarEntry("META-INF/versions/30/future.txt")); jo.write(30);
        jo.putNextEntry(new JarEntry("b.txt")); jo.write(2);
      }
      StringBuilder sb = new StringBuilder();
      try (JarFile jf = new JarFile(j.toFile())) {
        sb.append(jf.isMultiRelease() + " " + jf.getVersion() + " " + jf.getManifest().getMainAttributes().getValue("Main-Class") + "\n");
        sb.append(jf.getJarEntry("a.txt").getName() + " " + jf.getJarEntry("a.txt").getRealName() + " " + jf.getEntry("a.txt").getClass().getSimpleName().length() + " " + jf.getJarEntry("a.txt").getAttributes().getValue("Foo") + " " + jf.getJarEntry("a.txt").getAttributes().hashCode() + " " + jf.getJarEntry("b.txt").getAttributes() + "\n");
        sb.append(jf.entries().nextElement().getName() + " " + jf.stream().count() + " " + jf.size() + "\n");
        try (InputStream in = jf.getInputStream(jf.getJarEntry("a.txt"))) { sb.append(in.read() + "\n"); }
      }
      try (JarFile jf = new JarFile(j.toFile(), true, ZipFile.OPEN_READ, Runtime.Version.parse("9"))) {
        sb.append(jf.getVersion() + " " + jf.getJarEntry("a.txt").getRealName() + " " + jf.getJarEntry("only9.txt") + "\n");
        try (InputStream in = jf.getInputStream(jf.getJarEntry("a.txt"))) { sb.append(in.read() + "\n"); }
      }
      try (JarFile jf = new JarFile(j.toFile(), true, ZipFile.OPEN_READ, Runtime.Version.parse("11"))) {
        try (InputStream in = jf.getInputStream(jf.getJarEntry("a.txt"))) { sb.append(in.read() + " " + jf.getJarEntry("a.txt").getRealName() + " " + jf.getJarEntry("future.txt") + " " + jf.getJarEntry("META-INF/versions/9/a.txt").getName() + "\n"); }
        sb.append(jf.versionedStream().map(JarEntry::getName).collect(Collectors.toList()) + "\n");
        sb.append(jf.stream().map(JarEntry::getName).sorted().collect(Collectors.toList()));
      }
      return sb;
    });
    t("jarBadVersion", () -> { try (JarFile jf = new JarFile(z.toFile(), true, ZipFile.OPEN_READ, Runtime.Version.parse("7"))) { return jf.getVersion(); } });
    for (Path p : Files.list(dir).toList()) Files.deleteIfExists(p);
    Files.delete(dir);
  }
}
