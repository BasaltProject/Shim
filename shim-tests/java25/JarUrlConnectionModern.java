/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.*;
public class JarUrlConnectionModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage()).toString().replaceAll("/tmp/ju[0-9]+", "/tmp/juN")); } }
  public static void main(String[] a) throws Exception {
    FileUrlHeaders.run();
    FileUrlDirectory.run();
    Path dir = Files.createTempDirectory("ju");
    Path jar = dir.resolve("t.jar");
    Manifest mf = new Manifest(); mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); mf.getMainAttributes().putValue("Main-Class", "x.Y");
    Attributes ea = new Attributes(); ea.putValue("Foo", "bar"); mf.getEntries().put("a.txt", ea);
    try (JarOutputStream jo = new JarOutputStream(Files.newOutputStream(jar), mf)) {
      JarEntry e = new JarEntry("a.txt"); e.setTime(1700000000000L); jo.putNextEntry(e); jo.write("hello".getBytes());
      jo.putNextEntry(new JarEntry("dir/")); jo.putNextEntry(new JarEntry("dir/b file.html")); jo.write("<b/>".getBytes());
      jo.putNextEntry(new JarEntry("é.txt")); jo.write(1);
    }
    String base = "jar:" + jar.toUri() + "!/";
    t("entry", () -> { JarURLConnection c = (JarURLConnection) new URL(base + "a.txt").openConnection(); return c.getEntryName() + " " + c.getJarFileURL().getProtocol() + " " + c.getContentLength() + " " + c.getContentType() + " " + c.getContentLengthLong() + " " + (c.getLastModified() == Files.getLastModifiedTime(jar).toMillis() / 1000 * 1000) + " " + c.getJarEntry().getName() + " " + c.getAttributes().getValue("Foo") + " " + c.getMainAttributes().getValue("Main-Class") + " " + c.getManifest().getEntries().keySet() + " " + c.getCertificates() + " " + new String(c.getInputStream().readAllBytes()); });
    t("noEntry", () -> { JarURLConnection c = (JarURLConnection) new URL(base).openConnection(); return c.getEntryName() + " " + c.getContentType() + " " + (c.getContentLength() > 0) + " " + c.getJarEntry() + " " + c.getAttributes() + " " + c.getMainAttributes().getValue("Main-Class") + " " + c.getJarFile().getName().endsWith("t.jar"); });
    t("noEntryStream", () -> new URL(base).openConnection().getInputStream());
    t("missingEntry", () -> new URL(base + "nope.txt").openConnection().getInputStream());
    t("missingEntryConnect", () -> { new URL(base + "nope.txt").openConnection().connect(); return 1; });
    t("missingJar", () -> new URL("jar:" + dir.resolve("none.jar").toUri() + "!/a.txt").openConnection().getInputStream());
    t("dir", () -> { JarURLConnection c = (JarURLConnection) new URL(base + "dir/").openConnection(); return c.getEntryName() + " " + c.getContentType() + " " + c.getContentLength(); });
    t("encoded", () -> { JarURLConnection c = (JarURLConnection) new URL(base + "dir/b%20file.html").openConnection(); return c.getEntryName() + " " + c.getContentType() + " " + new String(c.getInputStream().readAllBytes()); });
    t("unicode", () -> { JarURLConnection c = (JarURLConnection) new URL(base + "%C3%A9.txt").openConnection(); return c.getEntryName() + " " + c.getInputStream().read(); });
    t("plus", () -> ((JarURLConnection) new URL(base + "a+b").openConnection()).getEntryName());
    t("badEscape", () -> ((JarURLConnection) new URL(base + "a%2").openConnection()).getEntryName());
    t("noBang", () -> new URL("jar:file:/x.jar"));
    t("nested", () -> new URL("jar:jar:file:/x.jar!/y.jar!/z.txt").getFile());
    t("urlStream", () -> new String(new URL(base + "a.txt").openStream().readAllBytes()));
    t("getContent", () -> new URL(base + "dir/b%20file.html").getContent().getClass().getSimpleName().length() > 0);
    t("context", () -> new URL(new URL(base + "dir/b%20file.html"), "c.txt").toString().replace(dir.toString(), "DIR"));
    t("contextAbs", () -> new URL(new URL(base + "dir/x"), "/a.txt").toString().replace(dir.toString(), "DIR"));
    t("props", () -> { URLConnection c = new URL(base + "a.txt").openConnection(); c.connect(); return c.getHeaderField("content-length") + " " + c.getHeaderField("content-type") + " " + c.getUseCaches() + " " + c.getDoInput() + " " + c.getURL().getProtocol(); });
    t("jarFile", () -> { JarURLConnection c = (JarURLConnection) new URL(base + "a.txt").openConnection(); JarFile f1 = c.getJarFile(); JarFile f2 = c.getJarFile(); return f1 == f2; });
    for (Path p : Files.list(dir).toList()) Files.deleteIfExists(p);
    Files.delete(dir);
  }
}

class FileUrlHeaders { static void run() throws Exception {
  Path p = Files.createTempFile("fu", ".jar"); Files.write(p, new byte[100]);
  URLConnection c = p.toUri().toURL().openConnection();
  c.connect();
  System.out.println(c.getHeaderField("content-length") + " " + c.getHeaderField("content-type") + " " + (c.getHeaderField("last-modified") != null) + " " + c.getContentLength() + " " + c.getContentType() + " " + c.getHeaderFields().keySet() + " " + c.getHeaderField(0) + " " + c.getHeaderFieldKey(0) + " " + c.getHeaderField(1) + " " + c.getHeaderFieldKey(1) + " " + c.getClass().getSimpleName());
  Path d = Files.createTempDirectory("fud");
  URLConnection c2 = d.toUri().toURL().openConnection(); c2.connect();
  System.out.println(c2.getContentType() + " " + c2.getHeaderField("content-type") + " " + c2.getContentLength() + " " + c2.getHeaderFields().keySet());
  Files.delete(p); Files.delete(d);
}}

class FileUrlDirectory { static void run() throws Exception {
  Path d = Files.createTempDirectory("fud");
  Files.write(d.resolve("b.txt"), new byte[3]); Files.write(d.resolve("a.txt"), new byte[3]); Files.createDirectory(d.resolve("sub"));
  URLConnection c2 = d.toUri().toURL().openConnection();
  System.out.println(c2.getContentLength() + " " + new String(c2.getInputStream().readAllBytes()).replace("\n", "|") + " " + c2.getContentLength());
  Path e = Files.createTempDirectory("fue");
  URLConnection c3 = e.toUri().toURL().openConnection(); System.out.println(c3.getContentLength() + " [" + new String(c3.getInputStream().readAllBytes()) + "] " + c3.getContentLength());
  URLConnection c4 = e.toUri().toURL().openConnection(); System.out.println(c4.getContentLength() + " " + c4.getContentLengthLong());
}}
