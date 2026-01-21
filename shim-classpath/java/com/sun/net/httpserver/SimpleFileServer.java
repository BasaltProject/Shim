/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package com.sun.net.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import sun.net.httpserver.simpleserver.FileServerHandler;
import sun.net.httpserver.simpleserver.OutputFilter;

public final class SimpleFileServer {
  private SimpleFileServer() { }

  public enum OutputLevel {
    NONE,
    INFO,
    VERBOSE;
  }

  public static HttpServer createFileServer(InetSocketAddress addr, Path rootDirectory, OutputLevel outputLevel) {
    Objects.requireNonNull(addr);
    Objects.requireNonNull(rootDirectory);
    Objects.requireNonNull(outputLevel);
    checkRoot(rootDirectory);
    try {
      HttpServer server = HttpServer.create(addr, 0);
      HttpHandler handler = new FileServerHandler(rootDirectory);
      if (outputLevel.equals(OutputLevel.NONE)) {
        server.createContext("/", handler);
      } else {
        Filter outputFilter = createOutputFilter(System.out, outputLevel);
        server.createContext("/", handler).getFilters().add(outputFilter);
      }
      return server;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static HttpHandler createFileHandler(Path rootDirectory) {
    Objects.requireNonNull(rootDirectory);
    checkRoot(rootDirectory);
    return new FileServerHandler(rootDirectory);
  }

  public static Filter createOutputFilter(OutputStream out, OutputLevel outputLevel) {
    Objects.requireNonNull(out);
    Objects.requireNonNull(outputLevel);
    return OutputFilter.create(out, outputLevel);
  }

  private static void checkRoot(Path root) {
    Path normalized = root.normalize();
    if (!Files.exists(normalized)) {
      throw new IllegalArgumentException("Path does not exist: " + normalized);
    }
    if (!normalized.isAbsolute()) {
      throw new IllegalArgumentException("Path is not absolute: " + normalized);
    }
    if (!Files.isDirectory(normalized)) {
      throw new IllegalArgumentException("Path is not a directory: " + normalized);
    }
    if (!Files.isReadable(normalized)) {
      throw new IllegalArgumentException("Path is not readable: " + normalized);
    }
  }
}
