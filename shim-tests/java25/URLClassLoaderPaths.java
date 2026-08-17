/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class URLClassLoaderPaths {
  public static class Payload {
    public static int answer() { return 42; }
  }

  private static void check(Path root) throws Exception {
    try (URLClassLoader loader = new URLClassLoader(new URL[] {root.toUri().toURL()}, null)) {
      Class<?> type = Class.forName("URLClassLoaderPaths$Payload", true, loader);
      if (type.getClassLoader() != loader || !Integer.valueOf(42).equals(type.getMethod("answer").invoke(null))) {
        throw new AssertionError("class was not loaded from the supplied root");
      }
      if (loader.findResource("URLClassLoaderPaths$Payload.class") == null) {
        throw new AssertionError("class resource is missing");
      }
      try (InputStream resource = loader.getResourceAsStream("URLClassLoaderPaths$Payload.class")) {
        if (resource == null || resource.read() != 0xCA) {
          throw new AssertionError("class resource could not be opened");
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    byte[] bytes;
    try (InputStream in = URLClassLoaderPaths.class.getResourceAsStream("URLClassLoaderPaths$Payload.class")) {
      bytes = in.readAllBytes();
    }
    Path temp = Files.createTempDirectory("url-loader-paths");
    try {
      for (String name : new String[] {"plain", "with spaces", "plus+percent%hash#café"}) {
        Path directory = Files.createDirectory(temp.resolve(name));
        Path file = directory.resolve("URLClassLoaderPaths$Payload.class");
        Files.write(file, bytes);
        Path jar = temp.resolve(name + ".jar");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
          out.putNextEntry(new JarEntry("URLClassLoaderPaths$Payload.class"));
          out.write(bytes);
          out.closeEntry();
        }
        check(directory);
        check(jar);
        Files.delete(file);
        Files.delete(directory);
        Files.delete(jar);
      }
      System.out.println("URLClassLoader escaped directory and jar paths: OK");
    } finally {
      // Do not mask a failed assertion with a cleanup error.
      temp.toFile().delete();
    }
  }
}
