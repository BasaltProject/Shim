/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JarAndManifestModern {
  public static void main(String[] args) throws Exception {
    testManifestAndAttributes();
    testJarStreamingRoundtrip();
    testMultiReleaseJarFile();
    testJarEntryConstructors();
    System.out.println("PASS: JarAndManifestModern");
  }

  private static void testManifestAndAttributes() throws Exception {
    Manifest man = new Manifest();
    Attributes main = man.getMainAttributes();
    main.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    main.put(Attributes.Name.MAIN_CLASS, "com.example.Main");
    main.put(Attributes.Name.MULTI_RELEASE, "true");
    main.putValue("Custom-Header", "CustomValue");

    String veryLongValue = "A".repeat(120);
    main.putValue("Long-Header", veryLongValue);

    Attributes section = new Attributes();
    section.putValue("SHA-256-Digest", "dummyhash1234567890");
    man.getEntries().put("com/example/Main.class", section);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    man.write(baos);

    byte[] bytes = baos.toByteArray();
    Manifest parsed = new Manifest(new ByteArrayInputStream(bytes));

    assertEquals("1.0", parsed.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION));
    assertEquals("com.example.Main", parsed.getMainAttributes().getValue("main-class"));
    assertEquals("true", parsed.getMainAttributes().getValue(Attributes.Name.MULTI_RELEASE));
    assertEquals(veryLongValue, parsed.getMainAttributes().getValue("Long-Header"));

    Attributes parsedSection = parsed.getAttributes("com/example/Main.class");
    if (parsedSection == null) {
      throw new AssertionError("Section com/example/Main.class missing");
    }
    assertEquals("dummyhash1234567890", parsedSection.getValue("SHA-256-Digest"));
  }

  private static void testJarStreamingRoundtrip() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Manifest man = new Manifest();
    man.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    man.getMainAttributes().putValue("Created-By", "Basalt-Shim");

    try (JarOutputStream jos = new JarOutputStream(baos, man)) {
      JarEntry entry1 = new JarEntry("hello.txt");
      jos.putNextEntry(entry1);
      jos.write("Hello from Jar!".getBytes(StandardCharsets.UTF_8));
      jos.closeEntry();

      JarEntry entry2 = new JarEntry("data/config.properties");
      jos.putNextEntry(entry2);
      jos.write("key=value\n".getBytes(StandardCharsets.UTF_8));
      jos.closeEntry();
    }

    byte[] jarBytes = baos.toByteArray();
    try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jarBytes))) {
      Manifest readMan = jis.getManifest();
      if (readMan == null) {
        throw new AssertionError("Manifest was not parsed in JarInputStream");
      }
      assertEquals("1.0", readMan.getMainAttributes().getValue(Attributes.Name.MANIFEST_VERSION));
      assertEquals("Basalt-Shim", readMan.getMainAttributes().getValue("Created-By"));

      JarEntry e1 = jis.getNextJarEntry();
      if (e1 == null) {
        throw new AssertionError("Entry 1 was null");
      }
      assertEquals("hello.txt", e1.getName());
      String content1 = new String(readAllBytes(jis), StandardCharsets.UTF_8);
      assertEquals("Hello from Jar!", content1);

      JarEntry e2 = jis.getNextJarEntry();
      if (e2 == null) {
        throw new AssertionError("Entry 2 was null");
      }
      assertEquals("data/config.properties", e2.getName());
      String content2 = new String(readAllBytes(jis), StandardCharsets.UTF_8);
      assertEquals("key=value\n", content2);

      JarEntry e3 = jis.getNextJarEntry();
      if (e3 != null) {
        throw new AssertionError("Expected EOF but got " + e3.getName());
      }
    }
  }

  private static void testMultiReleaseJarFile() throws Exception {
    File tempJar = File.createTempFile("test-mr-", ".jar");
    tempJar.deleteOnExit();

    Manifest man = new Manifest();
    man.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    man.getMainAttributes().put(Attributes.Name.MULTI_RELEASE, "true");

    try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(tempJar), man)) {
      jos.putNextEntry(new JarEntry("com/example/Resource.txt"));
      jos.write("Base Content".getBytes(StandardCharsets.UTF_8));
      jos.closeEntry();

      jos.putNextEntry(new JarEntry("META-INF/versions/9/com/example/Resource.txt"));
      jos.write("Java 9 Content".getBytes(StandardCharsets.UTF_8));
      jos.closeEntry();

      jos.putNextEntry(new JarEntry("META-INF/versions/21/com/example/Resource.txt"));
      jos.write("Java 21 Content".getBytes(StandardCharsets.UTF_8));
      jos.closeEntry();

      jos.putNextEntry(new JarEntry("META-INF/versions/25/com/example/Resource.txt"));
      jos.write("Java 25 Content".getBytes(StandardCharsets.UTF_8));
      jos.closeEntry();
    }

    try (JarFile jfDefault = new JarFile(tempJar)) {
      if (!jfDefault.isMultiRelease()) {
        throw new AssertionError("Expected multi-release true");
      }
      JarEntry entry = jfDefault.getJarEntry("com/example/Resource.txt");
      if (entry == null) {
        throw new AssertionError("Resource entry missing");
      }
      assertEquals("com/example/Resource.txt", entry.getName());
      assertEquals("com/example/Resource.txt", entry.getRealName());

      try (InputStream is = jfDefault.getInputStream(entry)) {
        String content = new String(readAllBytes(is), StandardCharsets.UTF_8);
        assertEquals("Base Content", content);
      }
    }

    try (JarFile jfRuntime = new JarFile(tempJar, true, ZipFile.OPEN_READ, JarFile.runtimeVersion())) {
      JarEntry entry = jfRuntime.getJarEntry("com/example/Resource.txt");
      if (entry == null) {
        throw new AssertionError("Resource entry missing in runtime version");
      }
      assertEquals("com/example/Resource.txt", entry.getName());
      assertEquals("META-INF/versions/25/com/example/Resource.txt", entry.getRealName());

      try (InputStream is = jfRuntime.getInputStream(entry)) {
        String content = new String(readAllBytes(is), StandardCharsets.UTF_8);
        assertEquals("Java 25 Content", content);
      }

      long count = jfRuntime.stream().count();
      if (count < 5) {
        throw new AssertionError("Expected at least 5 entries, got " + count);
      }
    }

    try (JarFile jf21 = new JarFile(tempJar, true, ZipFile.OPEN_READ, Runtime.Version.parse("21"))) {
      JarEntry entry = jf21.getJarEntry("com/example/Resource.txt");
      assertEquals("com/example/Resource.txt", entry.getName());
      assertEquals("META-INF/versions/21/com/example/Resource.txt", entry.getRealName());
      try (InputStream is = jf21.getInputStream(entry)) {
        String content = new String(readAllBytes(is), StandardCharsets.UTF_8);
        assertEquals("Java 21 Content", content);
      }
    }

    try (JarFile jfBase = new JarFile(tempJar, true, ZipFile.OPEN_READ, JarFile.baseVersion())) {
      JarEntry entry = jfBase.getJarEntry("com/example/Resource.txt");
      assertEquals("com/example/Resource.txt", entry.getName());
      assertEquals("com/example/Resource.txt", entry.getRealName());
      try (InputStream is = jfBase.getInputStream(entry)) {
        String content = new String(readAllBytes(is), StandardCharsets.UTF_8);
        assertEquals("Base Content", content);
      }
    }
  }

  private static void testJarEntryConstructors() throws Exception {
    ZipEntry ze = new ZipEntry("test.dat");
    ze.setComment("a comment");
    ze.setSize(42);

    JarEntry je1 = new JarEntry(ze);
    assertEquals("test.dat", je1.getName());
    assertEquals("a comment", je1.getComment());
    assertEquals(42L, je1.getSize());

    JarEntry je2 = new JarEntry(je1);
    assertEquals("test.dat", je2.getName());
    assertEquals(42L, je2.getSize());
    assertEquals("test.dat", je2.getRealName());
  }

  private static byte[] readAllBytes(InputStream is) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[512];
    int n;
    while ((n = is.read(buf)) != -1) {
      baos.write(buf, 0, n);
    }
    return baos.toByteArray();
  }

  private static void assertEquals(Object expected, Object actual) {
    if (!java.util.Objects.equals(expected, actual)) {
      throw new AssertionError("Expected: " + expected + ", but got: " + actual);
    }
  }
}
