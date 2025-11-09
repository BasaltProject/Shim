/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

public class ServiceLoaderAndPropertiesModern {
  public interface MessageCodec {
    String encode(String message);
  }

  public static class Rot13Codec implements MessageCodec {
    public Rot13Codec() {}

    @Override
    public String encode(String message) {
      return "rot13:" + message;
    }
  }

  public static class UpperCaseCodec implements MessageCodec {
    public UpperCaseCodec() {}

    @Override
    public String encode(String message) {
      return message.toUpperCase();
    }
  }

  public static void main(String[] args) throws Exception {
    testPropertiesWithDefaults();
    testPropertiesStoreAndLoadWriter();
    testServiceLoaderWithMockJar();
    System.out.println("PASS: ServiceLoaderAndPropertiesModern");
  }

  private static void testPropertiesWithDefaults() {
    Properties defaults = new Properties();
    defaults.setProperty("timeout", "5000");
    defaults.setProperty("retries", "3");
    defaults.setProperty("env", "production");

    Properties props = new Properties(defaults);
    props.setProperty("timeout", "10000");
    props.setProperty("user", "admin");

    assertEquals("10000", props.getProperty("timeout"));
    assertEquals("3", props.getProperty("retries"));
    assertEquals("production", props.getProperty("env"));
    assertEquals("admin", props.getProperty("user"));
    assertEquals("default-val", props.getProperty("non-existent", "default-val"));

    Set<String> names = props.stringPropertyNames();
    if (!names.contains("timeout") || !names.contains("retries") || !names.contains("env") || !names.contains("user")) {
      throw new AssertionError("Missing keys in stringPropertyNames: " + names);
    }
  }

  private static void testPropertiesStoreAndLoadWriter() throws Exception {
    Properties props = new Properties();
    props.setProperty("server.host", "localhost");
    props.setProperty("server.port", "8080");

    StringWriter sw = new StringWriter();
    props.store(sw, "Server Configuration");

    String content = sw.toString();
    if (!content.contains("Server Configuration") || !content.contains("server.host=localhost")) {
      throw new AssertionError("Invalid store output: " + content);
    }

    Properties reloaded = new Properties();
    reloaded.load(new StringReader(content));

    assertEquals("localhost", reloaded.getProperty("server.host"));
    assertEquals("8080", reloaded.getProperty("server.port"));
  }

  private static void testServiceLoaderWithMockJar() throws Exception {
    File jarFile = File.createTempFile("test-service-", ".jar");
    jarFile.deleteOnExit();

    try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
      String serviceName = "META-INF/services/" + MessageCodec.class.getName();
      jos.putNextEntry(new JarEntry(serviceName));
      String configContent = "# Config\n" + Rot13Codec.class.getName() + "\n" + UpperCaseCodec.class.getName() + "\n";
      jos.write(configContent.getBytes(StandardCharsets.UTF_8));
      jos.closeEntry();
    }

    URLClassLoader ucl = new URLClassLoader(
        new URL[] { jarFile.toURI().toURL() },
        ServiceLoaderAndPropertiesModern.class.getClassLoader()
    );

    ServiceLoader<MessageCodec> loader = ServiceLoader.load(MessageCodec.class, ucl);

    List<String> types = loader.stream()
        .map(p -> p.type().getName())
        .collect(Collectors.toList());

    if (types.size() != 2) {
      throw new AssertionError("Expected 2 providers, got " + types.size() + ": " + types);
    }
    assertEquals(Rot13Codec.class.getName(), types.get(0));
    assertEquals(UpperCaseCodec.class.getName(), types.get(1));

    Optional<MessageCodec> first = loader.findFirst();
    if (first.isEmpty()) {
      throw new AssertionError("findFirst was empty");
    }
    assertEquals("rot13:hello", first.get().encode("hello"));

    List<String> encoded = new ArrayList<String>();
    for (MessageCodec codec : loader) {
      encoded.add(codec.encode("test"));
    }
    assertEquals(2, encoded.size());
    assertEquals("rot13:test", encoded.get(0));
    assertEquals("TEST", encoded.get(1));
  }

  private static void assertEquals(Object expected, Object actual) {
    if (!java.util.Objects.equals(expected, actual)) {
      throw new AssertionError("Expected: " + expected + ", but got: " + actual);
    }
  }
}
