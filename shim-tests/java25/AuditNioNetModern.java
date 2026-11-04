/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 21

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.util.zip.*;
import java.util.jar.*;

public class AuditNioNetModern {
    static void p(Object s) { System.out.println(s); }

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("audit");
        try {
            files(dir);
            nets();
            charsets();
            zips(dir);
        } finally {
            try (Stream<Path> w = Files.walk(dir)) { w.sorted(Comparator.reverseOrder()).forEach(q -> q.toFile().delete()); }
        }
    }

    static void files(Path dir) throws Exception {
        Path a = Files.createDirectories(dir.resolve("a/b/c"));
        Files.writeString(a.resolve("x.txt"), "hello\nworld\n");
        Files.write(dir.resolve("a/y.bin"), new byte[]{1, 2, 3});
        Files.createSymbolicLink(dir.resolve("a/link"), dir.resolve("a/y.bin"));
        Files.createLink(dir.resolve("a/hard"), dir.resolve("a/y.bin"));
        try (Stream<Path> w = Files.walk(dir)) { p(w.map(q -> dir.relativize(q).toString()).sorted().toList()); }
        try (Stream<Path> w = Files.walk(dir, 1, FileVisitOption.FOLLOW_LINKS)) { p(w.count()); }
        try (Stream<Path> w = Files.find(dir, 5, (q, at) -> at.isRegularFile() && q.toString().endsWith(".txt"))) { p(w.count()); }
        List<String> seen = new ArrayList<>();
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes at) { seen.add("D" + dir.relativize(d)); return d.endsWith("c") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE; }
            public FileVisitResult visitFile(Path f, BasicFileAttributes at) { seen.add("F" + dir.relativize(f) + (at.isSymbolicLink() ? 0 : at.size()) + at.isSymbolicLink()); return FileVisitResult.CONTINUE; }
            public FileVisitResult postVisitDirectory(Path d, IOException e) { seen.add("P" + dir.relativize(d)); return FileVisitResult.CONTINUE; }
        });
        Collections.sort(seen); p(seen);
        BasicFileAttributes at = Files.readAttributes(dir.resolve("a/y.bin"), BasicFileAttributes.class);
        p(at.size() + " " + at.isRegularFile() + " " + at.isDirectory() + " " + at.isOther() + " " + (at.lastModifiedTime().toMillis() > 0) + " " + (at.fileKey() != null));
        PosixFileAttributes pa = Files.readAttributes(dir.resolve("a/y.bin"), PosixFileAttributes.class);
        Files.setPosixFilePermissions(dir.resolve("a/y.bin"), PosixFilePermissions.fromString("rwxr-x---"));
        p(PosixFilePermissions.toString(Files.getPosixFilePermissions(dir.resolve("a/y.bin"))) + " " + (pa.owner() != null) + " " + Files.getAttribute(dir.resolve("a/y.bin"), "unix:nlink") + " " + Files.getAttribute(dir.resolve("a/y.bin"), "posix:size"));
        Files.setLastModifiedTime(dir.resolve("a/y.bin"), FileTime.fromMillis(1_000_000_000_000L));
        p(Files.getLastModifiedTime(dir.resolve("a/y.bin")) + " " + Files.readAttributes(dir.resolve("a/y.bin"), "size,lastModifiedTime").keySet().stream().sorted().toList());
        p(Files.isSameFile(dir.resolve("a/link"), dir.resolve("a/y.bin")) + " " + Files.isSymbolicLink(dir.resolve("a/link")) + " " + dir.relativize(Files.readSymbolicLink(dir.resolve("a/link"))) + " " + Files.size(dir.resolve("a/link")));
        UserDefinedFileAttributeView ud = Files.getFileAttributeView(dir.resolve("a/y.bin"), UserDefinedFileAttributeView.class);
        try { ud.write("k", java.nio.ByteBuffer.wrap(new byte[]{9})); p("xattr " + ud.list()); } catch (Exception e) { p("xattr unsupported"); }
        p(Files.probeContentType(dir.resolve("a/b/c/x.txt")) + " " + Files.isReadable(a) + Files.isWritable(a) + Files.isExecutable(a) + Files.isHidden(a));
        try { Files.createDirectory(a); } catch (FileAlreadyExistsException e) { p("FAE " + dir.relativize(Path.of(e.getFile()))); }
        try { Files.delete(dir.resolve("a")); } catch (DirectoryNotEmptyException e) { p("DNE"); }
        try { Files.readString(dir.resolve("nope")); } catch (NoSuchFileException e) { p("NSF " + dir.relativize(Path.of(e.getMessage()))); }
        Files.copy(a.resolve("x.txt"), dir.resolve("copy.txt"), StandardCopyOption.COPY_ATTRIBUTES);
        Files.move(dir.resolve("copy.txt"), dir.resolve("moved.txt"), StandardCopyOption.ATOMIC_MOVE);
        try { Files.copy(a.resolve("x.txt"), dir.resolve("moved.txt")); } catch (FileAlreadyExistsException e) { p("copy exists"); }
        p(Files.mismatch(a.resolve("x.txt"), dir.resolve("moved.txt")) + " " + Files.readAllLines(dir.resolve("moved.txt")) + " " + Files.lines(dir.resolve("moved.txt")).count());
        try (var ch = java.nio.channels.FileChannel.open(dir.resolve("ch.dat"), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            ch.write(java.nio.ByteBuffer.wrap("0123456789".getBytes()), 5);
            var lock = ch.tryLock(0, 4, false);
            p(ch.size() + " " + lock.isValid() + " " + lock.isShared() + " " + ch.position());
            var mb = ch.map(java.nio.channels.FileChannel.MapMode.READ_WRITE, 0, 15); mb.put(0, (byte) 'Z'); mb.force();
            ch.truncate(12); p(ch.size() + " " + Arrays.toString(Files.readAllBytes(dir.resolve("ch.dat"))));
        }
        FileStore fs = Files.getFileStore(dir); p((fs.getTotalSpace() > 0) + " " + fs.supportsFileAttributeView("posix"));
        Path pp = Path.of("/a/./b/../c//d.txt");
        p(pp.normalize() + " " + pp.getNameCount() + " " + pp.getFileName() + " " + pp.getParent() + " " + pp.getRoot() + " " + pp.subpath(1, 3) + " " + Path.of("x").resolveSibling("y") + " " + Path.of("a/b").relativize(Path.of("a/c/d")) + " " + Path.of("/a/b").startsWith("/a") + " " + pp.toUri() + " " + Path.of("a").compareTo(Path.of("b")) + " " + FileSystems.getDefault().getPathMatcher("glob:**/*.{txt,md}").matches(Path.of("q/w/e.md")) + FileSystems.getDefault().getPathMatcher("regex:.*\\d").matches(Path.of("a1")));
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            Path wd = Files.createDirectory(dir.resolve("watched"));
            WatchKey k = wd.register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            Files.writeString(wd.resolve("n.txt"), "1");
            WatchKey got = ws.poll(5, TimeUnit.SECONDS);
            Set<String> kinds = new TreeSet<>();
            if (got != null) { for (WatchEvent<?> ev : got.pollEvents()) kinds.add(ev.kind().name() + ":" + ev.context()); got.reset(); }
            p("watch " + (got == k) + " " + kinds.contains("ENTRY_CREATE:n.txt") + " " + k.isValid());
            k.cancel(); p("cancelled " + k.isValid());
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir.resolve("a"), "*.{bin,txt}")) { List<String> l = new ArrayList<>(); ds.forEach(q -> l.add(q.getFileName().toString())); Collections.sort(l); p(l); }
        File f = dir.resolve("a/y.bin").toFile();
        p(f.length() + " " + f.canExecute() + " " + f.getName() + " " + f.getParentFile().getName() + " " + (f.lastModified() == 1_000_000_000_000L) + " " + new File("a/../b").getCanonicalFile().getName() + " " + f.toPath().equals(dir.resolve("a/y.bin")));
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) { raf.seek(10); raf.writeUTF("é"); raf.writeLong(-2); raf.seek(0); p(raf.length() + " " + raf.readByte() + " " + raf.getFilePointer()); }
    }

    static void nets() throws Exception {
        String[] us = {"http://user:pw@Example.COM:8080/a/./b/../c%20d?q=1&r=%E2%82%AC#frag", "file:///tmp/a b", "mailto:a@b.c?subject=x", "jar:file:/x.jar!/p/q", "http://[::1]:80/", "//host/path", "../rel/path?x", "urn:isbn:0451450523", "http://a.b/%zz", "HTTP://EXAMPLE.com/%7Efoo", "ftp://h/;type=a"};
        for (String s : us) {
            try {
                URI u = new URI(s);
                p(u + " | " + u.getScheme() + "," + u.getUserInfo() + "," + u.getHost() + "," + u.getPort() + "," + u.getPath() + "," + u.getRawPath() + "," + u.getQuery() + "," + u.getRawQuery() + "," + u.getFragment() + "," + u.getAuthority() + "," + u.isOpaque() + "," + u.isAbsolute() + " | " + u.normalize() + " | " + u.resolve("x/../y?z") + " | " + URI.create("http://a/b/c/d").relativize(u) + " | " + u.getSchemeSpecificPart() + " | " + u.parseServerAuthority().getHost());
            } catch (Exception e) { p("URI " + s + " " + e.getClass().getSimpleName() + " " + e.getMessage()); }
        }
        for (String s : new String[]{"http://a.b:81/p?q#r", "file:/x", "jar:file:/a.jar!/b", "ftp://u@h/x", "bogus:x", "http://", "https://h"}) {
            try { URL u = URI.create(s).toURL(); p(u + " " + u.getProtocol() + "," + u.getHost() + "," + u.getPort() + "," + u.getDefaultPort() + "," + u.getFile() + "," + u.getRef() + "," + u.getUserInfo() + "," + u.getAuthority()); }
            catch (Exception e) { p("URL " + s + " " + e.getClass().getSimpleName() + " " + e.getMessage()); }
        }
        p(URLEncoder.encode("a b&c=d/é€~*", "UTF-8") + " " + URLDecoder.decode("a+b%26%E2%82%AC", StandardCharsets.UTF_8) + " " + new URI("http", "h", "/p a", "q=é", "f g"));
        p(InetAddress.getByName("127.0.0.1") + " " + InetAddress.getByAddress(new byte[]{1, 2, 3, 4}).getHostAddress() + " " + InetAddress.getByName("::1").getHostAddress() + " " + InetAddress.getByName("::ffff:1.2.3.4") + " " + Inet6Address.getByName("fe80::1%1").getHostAddress().startsWith("fe80") + " " + InetAddress.getByName("224.0.0.1").isMulticastAddress() + " " + InetAddress.getLoopbackAddress() + " " + new InetSocketAddress("localhost", 9).getPort() + " " + InetSocketAddress.createUnresolved("h", 5));
        try { InetAddress.getByName("no such host.invalid"); } catch (UnknownHostException e) { p("UHE"); }
        try (ServerSocket ss = new ServerSocket(0, 5, InetAddress.getLoopbackAddress())) {
            Thread srv = new Thread(() -> { try (Socket c = ss.accept(); var in = c.getInputStream(); var out = c.getOutputStream()) { int n; byte[] b = new byte[100]; n = in.read(b); out.write(("echo:" + new String(b, 0, n)).getBytes()); c.shutdownOutput(); } catch (IOException e) { p(e); } });
            srv.start();
            try (Socket s = new Socket(InetAddress.getLoopbackAddress(), ss.getLocalPort())) {
                s.setTcpNoDelay(true); s.setSoTimeout(5000); s.setKeepAlive(true); s.setReceiveBufferSize(65536);
                s.getOutputStream().write("ping".getBytes());
                p(new String(s.getInputStream().readAllBytes()) + " " + s.getTcpNoDelay() + s.getKeepAlive() + " " + s.isConnected() + s.isClosed() + " " + s.getInetAddress().isLoopbackAddress());
            }
            srv.join();
            ss.setSoTimeout(50); try { ss.accept(); } catch (SocketTimeoutException e) { p("accept timeout " + e.getMessage()); }
        }
        try (Socket s = new Socket()) { s.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), 1), 500); } catch (ConnectException e) { p("CE " + e.getMessage()); }
        try (DatagramSocket d1 = new DatagramSocket(0, InetAddress.getLoopbackAddress()); DatagramSocket d2 = new DatagramSocket(0, InetAddress.getLoopbackAddress())) {
            byte[] m = "dgram".getBytes(); d1.send(new DatagramPacket(m, m.length, d2.getLocalSocketAddress()));
            DatagramPacket in = new DatagramPacket(new byte[10], 10); d2.setSoTimeout(2000); d2.receive(in); p(new String(in.getData(), 0, in.getLength()) + " " + in.getLength());
        }
        var hs = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        hs.createContext("/e", ex -> { byte[] r = (ex.getRequestMethod() + " " + ex.getRequestURI() + " " + ex.getRequestHeaders().getFirst("X-T") + " " + new String(ex.getRequestBody().readAllBytes())).getBytes(); ex.getResponseHeaders().add("X-R", "1"); ex.sendResponseHeaders(r.length == 0 ? 204 : 201, r.length); ex.getResponseBody().write(r); ex.close(); });
        hs.start();
        try {
            HttpClient hc = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(java.time.Duration.ofSeconds(5)).build();
            String base = "http://127.0.0.1:" + hs.getAddress().getPort();
            HttpResponse<String> r = hc.send(HttpRequest.newBuilder(URI.create(base + "/e?x=1")).header("X-T", "tt").POST(HttpRequest.BodyPublishers.ofString("body")).build(), HttpResponse.BodyHandlers.ofString());
            p(r.statusCode() + " " + r.body() + " " + r.headers().firstValue("x-r").orElse("?") + " " + r.version() + " " + r.uri().getPath());
            HttpResponse<String> r404 = hc.sendAsync(HttpRequest.newBuilder(URI.create(base + "/zz")).build(), HttpResponse.BodyHandlers.ofString()).get();
            p(r404.statusCode() + " " + r404.body());
            HttpURLConnection c = (HttpURLConnection) URI.create(base + "/e").toURL().openConnection(); c.setRequestProperty("X-T", "u"); c.setRequestMethod("PUT"); c.setDoOutput(true); c.getOutputStream().write("pp".getBytes());
            p(c.getResponseCode() + " " + new String(c.getInputStream().readAllBytes()) + " " + c.getHeaderField("X-R") + " " + c.getContentLength());
        } finally { hs.stop(0); }
    }

    static void charsets() throws Exception {
        String s = "aé€😀\u0000z";
        for (String cn : new String[]{"UTF-8", "UTF-16", "UTF-16LE", "UTF-32", "ISO-8859-1", "US-ASCII", "windows-1252", "Shift_JIS", "EUC-JP", "GBK", "GB18030", "Big5", "EUC-KR", "ISO-8859-15", "IBM437", "KOI8-R", "UTF-16BE"}) {
            Charset cs = Charset.forName(cn);
            byte[] b = s.getBytes(cs);
            StringBuilder sb = new StringBuilder(cn + " " + cs.name() + " " + HexFormat.of().formatHex(b) + " " + new String(b, cs).length() + " " + cs.aliases().size());
            CharsetEncoder enc = cs.newEncoder().onUnmappableCharacter(CodingErrorAction.REPORT);
            sb.append(" ").append(enc.canEncode('€')).append(enc.canEncode("😀")).append(" ").append(enc.maxBytesPerChar()).append(" ").append(enc.averageBytesPerChar());
            try { enc.encode(java.nio.CharBuffer.wrap(s)); sb.append(" enc-ok"); } catch (CharacterCodingException e) { sb.append(" ").append(e.getClass().getSimpleName()).append(e.getMessage()); }
            CharsetDecoder dec = cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            try { sb.append(" ").append(dec.decode(java.nio.ByteBuffer.wrap(new byte[]{(byte) 0xff, (byte) 0xfe, (byte) 0xc3, 0x28, (byte) 0x81})).length()); } catch (CharacterCodingException e) { sb.append(" ").append(e.getClass().getSimpleName()).append(e.getMessage()); }
            sb.append(" ").append(new String(new byte[]{(byte) 0xff, 'a', (byte) 0xc3, (byte) 0xa9, (byte) 0xe2, (byte) 0x82}, cs).codePoints().boxed().toList());
            p(sb);
        }
        p(Charset.isSupported("x-foo") + " " + Charset.defaultCharset() + " " + Charset.availableCharsets().size() / 10);
        try { Charset.forName("bad name!"); } catch (IllegalCharsetNameException e) { p("ICN " + e.getMessage()); }
        try { Charset.forName("nonexistent"); } catch (UnsupportedCharsetException e) { p("UC " + e.getMessage()); }
    }

    static void zips(Path dir) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (GZIPOutputStream g = new GZIPOutputStream(bo)) { g.write("repeat repeat repeat repeat".repeat(50).getBytes()); }
        byte[] gz = bo.toByteArray();
        p(gz.length + " " + (gz[0] & 0xff) + " " + (gz[1] & 0xff) + " " + new String(new GZIPInputStream(new ByteArrayInputStream(gz)).readAllBytes()).length());
        try { new GZIPInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10})); } catch (ZipException e) { p("ZE " + e.getMessage()); }
        try { new GZIPInputStream(new ByteArrayInputStream(Arrays.copyOf(gz, gz.length - 4))).readAllBytes(); } catch (IOException e) { p(e.getClass().getSimpleName() + " " + e.getMessage()); }
        for (int lvl : new int[]{0, 1, 6, 9}) for (int strat : new int[]{Deflater.DEFAULT_STRATEGY, Deflater.FILTERED, Deflater.HUFFMAN_ONLY}) {
            Deflater d = new Deflater(lvl, true); d.setStrategy(strat); d.setInput("abcabcabcabc12345 abcabc".repeat(20).getBytes()); d.finish();
            byte[] out = new byte[1000]; int n = d.deflate(out); CRC32 crc = new CRC32(); crc.update(out, 0, n);
            p("deflate " + lvl + " " + strat + " " + n + " " + Long.toHexString(crc.getValue()) + " " + d.getTotalIn() + " " + d.getBytesWritten()); d.end();
        }
        Path zp = dir.resolve("t.zip");
        try (ZipOutputStream zo = new ZipOutputStream(Files.newOutputStream(zp))) {
            zo.setComment("zc"); zo.setLevel(9);
            ZipEntry e1 = new ZipEntry("dir/"); zo.putNextEntry(e1); zo.closeEntry();
            ZipEntry e2 = new ZipEntry("dir/a.txt"); e2.setTime(1_000_000_000_000L); e2.setComment("cm"); e2.setExtra(new byte[]{(byte) 0xCA, (byte) 0xFE, 0, 0}); zo.putNextEntry(e2); zo.write("data data data".getBytes()); zo.closeEntry();
            ZipEntry e3 = new ZipEntry("s.bin"); e3.setMethod(ZipEntry.STORED); byte[] sb = {1, 2, 3}; CRC32 c = new CRC32(); c.update(sb); e3.setSize(3); e3.setCompressedSize(3); e3.setCrc(c.getValue()); zo.putNextEntry(e3); zo.write(sb); zo.closeEntry();
            ZipEntry e4 = new ZipEntry("ünï.txt"); zo.putNextEntry(e4); zo.write(1); zo.closeEntry();
            try { zo.putNextEntry(new ZipEntry("s.bin")); } catch (ZipException e) { p("dup " + e.getMessage()); }
        }
        try (ZipFile zf = new ZipFile(zp.toFile())) {
            p(zf.size() + " " + zf.getComment() + " " + zf.stream().map(ZipEntry::getName).toList());
            ZipEntry e = zf.getEntry("dir/a.txt"); p(e.getSize() + " " + e.getCompressedSize() + " " + e.getMethod() + " " + e.getComment() + " " + Long.toHexString(e.getCrc()) + " " + e.getTime() + " " + e.getExtra().length + " " + e.isDirectory() + " " + zf.getEntry("dir").getName());
            p(new String(zf.getInputStream(e).readAllBytes()) + " " + zf.getEntry("s.bin").getMethod());
        }
        try (ZipInputStream zi = new ZipInputStream(Files.newInputStream(zp))) { ZipEntry e; List<String> l = new ArrayList<>(); while ((e = zi.getNextEntry()) != null) l.add(e.getName() + ":" + e.getSize() + ":" + zi.readAllBytes().length); p(l); }
        Manifest mf = new Manifest(); mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); mf.getMainAttributes().putValue("Main-Class", "a.B"); mf.getMainAttributes().putValue("Long-Line", "x".repeat(150));
        Path jp = dir.resolve("t.jar");
        try (JarOutputStream jo = new JarOutputStream(Files.newOutputStream(jp), mf)) { jo.putNextEntry(new JarEntry("p/q.txt")); jo.write(5); }
        try (JarFile jf = new JarFile(jp.toFile())) { p(jf.getManifest().getMainAttributes().getValue("Long-Line").length() + " " + jf.getManifest().getMainAttributes().getValue("main-class") + " " + jf.stream().map(JarEntry::getName).toList() + " " + jf.isMultiRelease()); }
        try (FileSystem zfs = FileSystems.newFileSystem(zp)) { p(Files.readString(zfs.getPath("dir/a.txt")) + " " + Files.walk(zfs.getPath("/")).count()); }
        Inflater inf = new Inflater(); inf.setInput(new byte[]{1, 2, 3}); try { inf.inflate(new byte[10]); } catch (DataFormatException e) { p("DFE " + e.getMessage()); }
        Adler32 ad = new Adler32(); ad.update("wikipedia".getBytes()); CRC32C c32 = new CRC32C(); c32.update("123456789".getBytes()); p(ad.getValue() + " " + c32.getValue());
    }
}
