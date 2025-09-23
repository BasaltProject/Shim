/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.function.Supplier;

public class NetTypesModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  @Retention(RetentionPolicy.RUNTIME) @Inherited @interface Tag { }
  @Retention(RetentionPolicy.RUNTIME) @Repeatable(Tags.class) @interface Mark { String value(); }
  @Retention(RetentionPolicy.RUNTIME) @interface Tags { Mark[] value(); }

  public static void main(String[] args) throws Exception {
    probe("hierarchy", () -> (new ConnectException("c") instanceof SocketException) + " " + (new NoRouteToHostException() instanceof SocketException) + " " + (new PortUnreachableException("p") instanceof SocketException) + " " + (new SocketTimeoutException("t") instanceof InterruptedIOException) + " " + (new HttpRetryException("d", 301) instanceof IOException) + " " + (new SocketException() instanceof IOException));
    probe("messages", () -> new ConnectException("refused").getMessage() + " " + new NoRouteToHostException().getMessage() + " " + new SocketTimeoutException("timed out").getMessage() + " " + new SocketTimeoutException().getMessage() + " " + new PortUnreachableException("pu").getMessage());
    probe("http-retry", () -> { HttpRetryException e = new HttpRetryException("moved", 301, "http://x/y"); HttpRetryException f = new HttpRetryException("gone", 410); return e.getMessage() + " " + e.responseCode() + " " + e.getReason() + " " + e.getLocation() + " " + f.getLocation() + " " + f.responseCode(); });
    probe("socket-options", () -> StandardSocketOptions.SO_KEEPALIVE.name() + ":" + StandardSocketOptions.SO_KEEPALIVE.type().getSimpleName() + " " + StandardSocketOptions.SO_RCVBUF + ":" + StandardSocketOptions.SO_RCVBUF.type().getSimpleName() + " " + StandardSocketOptions.TCP_NODELAY.type().getSimpleName() + " " + StandardSocketOptions.SO_LINGER.type().getSimpleName() + " " + StandardSocketOptions.IP_TOS + " " + StandardSocketOptions.SO_REUSEADDR + " " + StandardSocketOptions.SO_REUSEPORT + " " + StandardSocketOptions.SO_BROADCAST + " " + StandardSocketOptions.IP_MULTICAST_TTL + " " + StandardSocketOptions.IP_MULTICAST_LOOP + " " + StandardSocketOptions.SO_SNDBUF);
    probe("net-permission", () -> new NetPermission("setDefaultAuthenticator").getName() + " " + new NetPermission("x").getActions().isEmpty() + " " + new NetPermission("a").equals(new NetPermission("a")) + " " + new NetPermission("a").equals(new NetPermission("b")));
    probe("reflect-permission", () -> new ReflectPermission("suppressAccessChecks").getName() + " " + (new ReflectPermission("a").hashCode() == "a".hashCode()));
    probe("inaccessible", () -> { InaccessibleObjectException e = new InaccessibleObjectException("no"); return e.getMessage() + " " + new InaccessibleObjectException().getMessage() + " " + (e instanceof RuntimeException); });
    probe("annotation-errors", () -> new AnnotationFormatError("bad").getMessage() + " " + new AnnotationFormatError("x", new RuntimeException("c")).getCause().getMessage() + " " + new AnnotationFormatError(new RuntimeException("cc")).getMessage() + " " + (new AnnotationFormatError("m") instanceof Error));
    probe("incomplete-annotation", () -> { IncompleteAnnotationException e = new IncompleteAnnotationException(Tags.class, "value"); return e.getMessage().replace('$', '.') + " " + e.annotationType().getSimpleName() + " " + e.elementName(); });
    probe("annotation-mismatch", () -> { try { Method m = Tags.class.getMethod("value"); AnnotationTypeMismatchException e = new AnnotationTypeMismatchException(m, "java.lang.String"); return e.element().getName() + " " + e.foundType() + " " + (e.getMessage() != null); } catch (NoSuchMethodException ex) { return ex; } });
    probe("meta-annotations", () -> Tag.class.isAnnotationPresent(Inherited.class) + " " + Mark.class.isAnnotationPresent(Repeatable.class) + " " + Mark.class.getAnnotation(Repeatable.class).value().getSimpleName() + " " + Inherited.class.isAnnotation() + " " + Native.class.getAnnotation(Retention.class).value() + " " + Inherited.class.getAnnotation(Retention.class).value());
    probe("response-cache", () -> { ResponseCache c = new ResponseCache() { public CacheResponse get(URI u, String m, Map<String, List<String>> h) { return null; } public CacheRequest put(URI u, URLConnection c) { return null; } }; ResponseCache.setDefault(c); boolean same = ResponseCache.getDefault() == c; ResponseCache.setDefault(null); return same + " " + ResponseCache.getDefault(); });
    probe("content-handler", () -> { try { ContentHandler h = new ContentHandler() { public Object getContent(URLConnection c) { return "text"; } }; return h.getContent(null, new Class[] {Integer.class, String.class}) + " " + h.getContent(null, new Class[] {Integer.class}); } catch (IOException e) { return e; } });
    probe("factories", () -> { URLStreamHandlerFactory f = p -> null; ContentHandlerFactory g = m -> null; FileNameMap n = fn -> "text/plain"; return f.createURLStreamHandler("x") + " " + g.createContentHandler("y") + " " + n.getContentTypeFor("a.txt"); });
  }
}
