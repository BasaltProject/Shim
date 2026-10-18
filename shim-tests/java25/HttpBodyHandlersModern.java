/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class HttpBodyHandlersModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { Throwable r = e instanceof ExecutionException ? e.getCause() : e; System.out.println(n + ": !" + r.getClass().getName() + ":" + (r instanceof NullPointerException ? null : r.getMessage())); } }
  static ResponseInfo info(Map<String, List<String>> h) { HttpHeaders hh = HttpHeaders.of(h, (a, b) -> true); return new ResponseInfo() { public int statusCode() { return 200; } public HttpHeaders headers() { return hh; } public HttpClient.Version version() { return HttpClient.Version.HTTP_1_1; } }; }
  static Flow.Subscription noop = new Flow.Subscription() { public void request(long n) { } public void cancel() { } };
  static <T> T run(BodyHandler<T> h, Map<String, List<String>> hd, byte[]... chunks) throws Exception {
    BodySubscriber<T> s = h.apply(info(hd));
    s.onSubscribe(noop);
    for (byte[] c : chunks) s.onNext(List.of(ByteBuffer.wrap(c)));
    s.onComplete();
    return s.getBody().toCompletableFuture().get(5, TimeUnit.SECONDS);
  }
  static byte[] b(String s) { return s.getBytes(StandardCharsets.UTF_8); }
  static Map<String, List<String>> none = Map.of();
  static class Rec<T> implements Flow.Subscriber<T> {
    final List<String> log = new ArrayList<>(); Flow.Subscription sub; final long demand;
    Rec(long demand) { this.demand = demand; }
    public void onSubscribe(Flow.Subscription s) { sub = s; log.add("sub"); if (demand > 0) s.request(demand); }
    public void onNext(T item) { log.add(item instanceof List ? "buf" + ((List<?>) item).stream().mapToInt(x -> ((ByteBuffer) x).remaining()).sum() : String.valueOf(item)); }
    public void onError(Throwable t) { log.add("err:" + t.getMessage()); }
    public void onComplete() { log.add("done"); }
  }
  public static void main(String[] a) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    t("string", () -> run(BodyHandlers.ofString(), none, b("héllo "), b("wörld")));
    t("stringCharset", () -> run(BodyHandlers.ofString(), Map.of("Content-Type", List.of("text/plain; charset=ISO-8859-1")), new byte[] {(byte) 0xe9}));
    t("stringCharsetQuoted", () -> run(BodyHandlers.ofString(), Map.of("Content-Type", List.of("text/plain; charset=\"UTF-16LE\"")), new byte[] {0x41, 0}));
    t("stringCharsetBad", () -> run(BodyHandlers.ofString(), Map.of("Content-Type", List.of("text/plain; charset=nope")), b("x")));
    t("stringExplicit", () -> run(BodyHandlers.ofString(StandardCharsets.UTF_16BE), none, new byte[] {0, 0x42}));
    t("stringNull", () -> BodyHandlers.ofString(null));
    t("bytes", () -> Arrays.toString(run(BodyHandlers.ofByteArray(), none, b("ab"), b("cd"))));
    t("input", () -> new String(run(BodyHandlers.ofInputStream(), none, b("xy")).readAllBytes()));
    t("discard", () -> run(BodyHandlers.discarding(), none, b("xy")));
    t("replace", () -> run(BodyHandlers.replacing("R"), none, b("xy")));
    t("consumer", () -> { List<String> l = new ArrayList<>(); Object r = run(BodyHandlers.ofByteArrayConsumer(o -> l.add(o.map(x -> new String(x)).orElse("EMPTY"))), none, b("ab"), b("cd")); return r + " " + l; });
    t("consumerNull", () -> BodyHandlers.ofByteArrayConsumer(null));
    t("fromSub", () -> { Rec<List<ByteBuffer>> r = new Rec<>(Long.MAX_VALUE); Object o = run(BodyHandlers.fromSubscriber(r), none, b("ab"), b("cde")); return o + " " + r.log; });
    t("fromSubFinisher", () -> { Rec<List<ByteBuffer>> r = new Rec<>(Long.MAX_VALUE); return run(BodyHandlers.fromSubscriber(r, x -> "fin:" + ((Rec<?>) x).log.size()), none, b("ab")); });
    t("fromSubNull", () -> BodyHandlers.fromSubscriber((Flow.Subscriber<List<ByteBuffer>>) null));
    t("fromLine", () -> { Rec<String> r = new Rec<>(Long.MAX_VALUE); Object o = run(BodyHandlers.fromLineSubscriber(r), none, b("a\nb\r"), b("\nc\rd")); return o + " " + r.log; });
    t("fromLineUtf8Split", () -> { Rec<String> r = new Rec<>(Long.MAX_VALUE); byte[] e = b("é\n"); run(BodyHandlers.fromLineSubscriber(r), none, new byte[] {e[0]}, new byte[] {e[1], e[2]}); return r.log; });
    t("fromLineSep", () -> { Rec<String> r = new Rec<>(Long.MAX_VALUE); Object o = run(BodyHandlers.fromLineSubscriber(r, x -> "F", "||"), none, b("a||b|"), b("|c")); return o + " " + r.log; });
    t("fromLineSepEmpty", () -> BodyHandlers.fromLineSubscriber(new Rec<String>(1), x -> 1, ""));
    t("fromLineNoDemand", () -> { Rec<String> r = new Rec<>(0); run(BodyHandlers.fromLineSubscriber(r), none, b("a\nb\n")); return r.log; });
    t("fromLineDemand1", () -> { Rec<String> r = new Rec<>(1); BodySubscriber<Void> s = BodyHandlers.fromLineSubscriber(r).apply(info(none)); s.onSubscribe(noop); s.onNext(List.of(ByteBuffer.wrap(b("a\nb\nc\n")))); String x = r.log.toString(); r.sub.request(1); x += r.log; s.onComplete(); x += r.log; r.sub.request(5); return x + r.log; });
    t("buffering", () -> { Rec<List<ByteBuffer>> r = new Rec<>(Long.MAX_VALUE); run(BodyHandlers.buffering(BodyHandlers.fromSubscriber(r), 3), none, b("ab"), b("cdefg"), b("h")); return r.log; });
    t("bufferingBad", () -> BodyHandlers.buffering(BodyHandlers.ofString(), 0));
    t("bufferingNull", () -> BodyHandlers.buffering(null, 1));
    t("limiting", () -> run(BodyHandlers.limiting(BodyHandlers.ofString(), 10), none, b("12345"), b("678")));
    t("limitingExceed", () -> run(BodyHandlers.limiting(BodyHandlers.ofString(), 5), none, b("123"), b("456")));
    t("limitingZero", () -> run(BodyHandlers.limiting(BodyHandlers.ofString(), 0), none));
    t("limitingBad", () -> BodyHandlers.limiting(BodyHandlers.ofString(), -1));
    Path dir = Files.createTempDirectory("hb1");
    t("file", () -> { Path f = dir.resolve("out.txt"); Object r = run(BodyHandlers.ofFile(f), none, b("data1"), b("data2")); return r.equals(f) + " " + Files.readString(f); });
    t("fileAppend", () -> { Path f = dir.resolve("out.txt"); run(BodyHandlers.ofFile(f, StandardOpenOption.CREATE, StandardOpenOption.APPEND), none, b("+x")); return Files.readString(f); });
    t("fileRead", () -> BodyHandlers.ofFile(dir.resolve("x"), StandardOpenOption.READ));
    t("fileDelete", () -> BodyHandlers.ofFile(dir.resolve("x"), StandardOpenOption.DELETE_ON_CLOSE));
    t("fileNull", () -> BodyHandlers.ofFile(null));
    t("downloadMissingDir", () -> { try { BodyHandlers.ofFileDownload(dir.resolve("nodir")); return 1; } catch (IllegalArgumentException e) { return e.getMessage().substring(0, e.getMessage().indexOf(':')); } });
    t("downloadNotDir", () -> { try { BodyHandlers.ofFileDownload(dir.resolve("out.txt")); return 1; } catch (IllegalArgumentException e) { return e.getMessage().substring(0, e.getMessage().indexOf(':')); } });
    t("errorString", () -> { BodySubscriber<String> s = BodyHandlers.ofString().apply(info(none)); s.onSubscribe(noop); s.onNext(List.of(ByteBuffer.wrap(b("x")))); s.onError(new IOException("boom")); return s.getBody().toCompletableFuture().get(); });
    t("pubFile", () -> { Path f = dir.resolve("pub.txt"); Files.writeString(f, "pubdata"); HttpRequest.BodyPublisher p = HttpRequest.BodyPublishers.ofFile(f); return p.contentLength(); });
    t("pubFileMissing", () -> { try { HttpRequest.BodyPublishers.ofFile(dir.resolve("nofile")); return 1; } catch (FileNotFoundException e) { return e.getClass().getName(); } });
    t("pubByteArrays", () -> HttpRequest.BodyPublishers.ofByteArrays(List.of(b("ab"), b("cde"))).contentLength());
    t("pubFromPublisher", () -> HttpRequest.BodyPublishers.fromPublisher(HttpRequest.BodyPublishers.ofString("abc")).contentLength() + " " + HttpRequest.BodyPublishers.fromPublisher(HttpRequest.BodyPublishers.ofString("abc"), 3).contentLength());
    t("pubFromPublisherBad", () -> HttpRequest.BodyPublishers.fromPublisher(HttpRequest.BodyPublishers.ofString("abc"), 0));
    t("reqHead", () -> { HttpRequest r = HttpRequest.newBuilder(URI.create("http://x/")).HEAD().build(); return r.method() + " " + r.bodyPublisher().isPresent(); });
    t("reqCopy", () -> { HttpRequest r = HttpRequest.newBuilder(URI.create("http://x/")).header("A", "1").header("B", "2").header("a", "3").POST(HttpRequest.BodyPublishers.ofString("z")).build(); HttpRequest c = HttpRequest.newBuilder(r, (k, v) -> !k.equals("B")).build(); return c.method() + " " + new TreeMap<>(c.headers().map()) + " " + c.bodyPublisher().get().contentLength() + " " + c.uri(); });
    t("reqCopyNull", () -> HttpRequest.newBuilder(null, (k, v) -> true));
    t("push", () -> { ConcurrentMap<HttpRequest, CompletableFuture<HttpResponse<String>>> m = new ConcurrentHashMap<>(); PushPromiseHandler<String> h = PushPromiseHandler.of(r -> BodyHandlers.ofString(), m); HttpRequest r0 = HttpRequest.newBuilder(URI.create("http://x/")).build(); h.applyPushPromise(r0, r0, bh -> new CompletableFuture<>()); return m.size() + " " + m.containsKey(r0); });
    t("pushNull", () -> PushPromiseHandler.of(r -> BodyHandlers.ofString(), new ConcurrentHashMap<HttpRequest, CompletableFuture<HttpResponse<String>>>()) != null);
    t("client", () -> { HttpClient c = HttpClient.newBuilder().build(); return (c.sslContext() != null) + " " + (c.sslParameters() != null) + " " + c.followRedirects() + " " + c.version(); });
    t("clientSsl", () -> { javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getDefault(); javax.net.ssl.SSLParameters sp = new javax.net.ssl.SSLParameters(); HttpClient c = HttpClient.newBuilder().sslContext(sc).sslParameters(sp).build(); return (c.sslContext() == sc) + " " + (c.sslParameters() != sp) + " " + (c.sslParameters().getProtocols() == null); });
    t("clientSslNull", () -> HttpClient.newBuilder().sslContext(null));
    t("noProxy", () -> HttpClient.Builder.NO_PROXY.select(URI.create("http://x/")));
    t("connLabel", () -> { HttpResponse<String> r = new HttpResponse<>() { public int statusCode() { return 200; } public HttpRequest request() { return null; } public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); } public HttpHeaders headers() { return null; } public String body() { return "b"; } public Optional<javax.net.ssl.SSLSession> sslSession() { return Optional.empty(); } public URI uri() { return null; } public HttpClient.Version version() { return null; } }; return r.connectionLabel(); });
    for (Path p : Files.list(dir).toList()) Files.deleteIfExists(p);
    Files.delete(dir);
  }
}
