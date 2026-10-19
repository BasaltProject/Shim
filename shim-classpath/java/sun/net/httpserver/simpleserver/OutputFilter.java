/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package sun.net.httpserver.simpleserver;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.SimpleFileServer.OutputLevel;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public final class OutputFilter extends Filter {
  private static final DateTimeFormatter kTimestamp = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

  private final PrintStream printStream;
  private final OutputLevel outputLevel;

  private OutputFilter(OutputStream os, OutputLevel outputLevel) {
    this.printStream = new PrintStream(os, true, StandardCharsets.UTF_8);
    this.outputLevel = outputLevel;
  }

  public static OutputFilter create(OutputStream os, OutputLevel outputLevel) {
    if (outputLevel.equals(OutputLevel.NONE)) {
      throw new IllegalArgumentException("Not a valid outputLevel: " + outputLevel);
    }
    return new OutputFilter(os, outputLevel);
  }

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    chain.doFilter(exchange);
    StringBuilder out = new StringBuilder();
    out.append(exchange.getRemoteAddress().getAddress().getHostAddress()).append(" - - [")
        .append(ZonedDateTime.now().format(kTimestamp)).append("] \"").append(exchange.getRequestMethod())
        .append(' ').append(exchange.getRequestURI().getPath()).append(' ').append(exchange.getProtocol())
        .append("\" ").append(exchange.getResponseCode()).append(" -\n");
    if (outputLevel.equals(OutputLevel.VERBOSE)) {
      Object path = exchange.getAttribute("request-path");
      out.append("Resource requested: ").append(path == null ? "could not resolve request URI path" : path)
          .append('\n');
      appendHeaders(out, "> ", exchange.getRequestHeaders());
      appendHeaders(out, "< ", exchange.getResponseHeaders());
    }
    synchronized (printStream) {
      printStream.print(out);
      printStream.flush();
    }
  }

  private static void appendHeaders(StringBuilder out, String prefix, Map<String, List<String>> headers) {
    for (Map.Entry<String, List<String>> e : headers.entrySet()) {
      for (String v : e.getValue()) {
        out.append(prefix).append(e.getKey()).append(": ").append(v).append('\n');
      }
    }
    out.append(prefix.trim()).append('\n');
  }

  @Override
  public String description() {
    return "HttpExchange OutputFilter (outputLevel: " + outputLevel + ")";
  }
}
