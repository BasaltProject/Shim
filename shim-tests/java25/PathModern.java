/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

public class PathModern {
  interface Body {
    Object get() throws Exception;
  }

  static void probe(String name, Body body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage());
    }
    System.out.println(name + "=" + out);
  }

  static String q(Path p) {
    return p == null ? "null" : "[" + p + "]";
  }

  public static void main(String[] args) throws Exception {
    String[] raw = {"", "/", "a", "a/b", "/a/b/", "a//b", "./a", "../a", "a/./b", "a/../b", "/..", "/../a", "a/b/../..", "//a", "///", "a/", ".", "..", "a b", "é/中", "/a/b/c/d", "a/b/c", "/a", "b", "a/b/c/", "..//..", "/./", "a/.", "a/..", "/a/./../b//c/../d/", "x/y/z/w"};
    FileSystem fs = FileSystems.getDefault();
    for (String s : raw) {
      probe("p [" + s + "]", () -> {
        Path p = Path.of(s);
        StringBuilder sb = new StringBuilder();
        sb.append(q(p)).append(" fn=").append(q(p.getFileName())).append(" par=").append(q(p.getParent())).append(" root=").append(q(p.getRoot())).append(" n=").append(p.getNameCount()).append(" abs=").append(p.isAbsolute()).append(" norm=").append(q(p.normalize()));
        for (int i = 0; i < p.getNameCount(); i++) {
          sb.append(" name").append(i).append('=').append(q(p.getName(i)));
        }
        for (int i = 0; i < p.getNameCount(); i++) {
          for (int j = i + 1; j <= p.getNameCount(); j++) {
            sb.append(" sub").append(i).append(j).append('=').append(q(p.subpath(i, j)));
          }
        }
        sb.append(" hash=").append(p.hashCode()).append(" uri=").append(p.toUri().toString().replace(System.getProperty("user.dir"), "<CWD>"));
        sb.append(" it=");
        for (Path e : p) {
          sb.append(q(e));
        }
        return sb;
      });
    }
    probe("p subpath bad", () -> Path.of("a/b").subpath(1, 1));
    probe("p subpath bad2", () -> Path.of("a/b").subpath(0, 3));
    probe("p subpath bad3", () -> Path.of("/").subpath(0, 1));
    probe("p getName bad", () -> Path.of("a/b").getName(2));
    probe("p getName neg", () -> Path.of("a/b").getName(-1));
    probe("p getName root", () -> Path.of("/").getName(0));
    for (int i = 0; i < raw.length; i++) {
      for (int j = 0; j < raw.length; j += 2) {
        final String a = raw[i], b = raw[(i + j) % raw.length];
        probe("pair [" + a + "] [" + b + "]", () -> {
          Path p = Path.of(a), o = Path.of(b);
          StringBuilder sb = new StringBuilder();
          sb.append("res=").append(q(p.resolve(o))).append(" ress=").append(q(p.resolve(b))).append(" sib=").append(q(p.resolveSibling(o))).append(" sibs=").append(q(p.resolveSibling(b))).append(" sw=").append(p.startsWith(o)).append(p.startsWith(b)).append(" ew=").append(p.endsWith(o)).append(p.endsWith(b)).append(" eq=").append(p.equals(o)).append(" cmp=").append(Integer.signum(p.compareTo(o)));
          try {
            sb.append(" rel=").append(q(p.relativize(o)));
          } catch (IllegalArgumentException e) {
            sb.append(" rel=!").append(e.getMessage());
          }
          return sb;
        });
      }
    }
    probe("of multi", () -> q(Path.of("a", "b", "", "c")) + q(Path.of("", "a")) + q(Path.of("/", "a")) + q(Path.of("a/", "/b")) + q(Path.of("")) + q(Path.of("a", "")));
    probe("of nul", () -> Path.of("a\0b"));
    probe("of nul multi", () -> Path.of("a", "b\0c"));
    probe("of null", () -> Path.of(null));
    probe("of uri", () -> q(Path.of(URI.create("file:///tmp/x y"))) + q(Path.of(URI.create("file:/a/b/"))) + q(Path.of(URI.create("file:///"))) + q(Path.of(URI.create("file:///a%2Fb"))));
    probe("of uri bad", () -> Path.of(URI.create("http://x/y")));
    probe("of uri bad2", () -> Path.of(URI.create("file:rel")));
    probe("of uri bad3", () -> Path.of(URI.create("file://host/x")));
    probe("of uri bad4", () -> Path.of(URI.create("file:/x?q")));
    probe("of uri bad5", () -> Path.of(URI.create("file:/x#f")));
    probe("of uri bad6", () -> Path.of(URI.create("foo:/x")));
    probe("paths.get", () -> q(Paths.get("a", "b")) + q(Paths.get(URI.create("file:///x"))));
    probe("fs props", () -> fs.getSeparator() + fs.isOpen() + fs.isReadOnly() + fs.supportedFileAttributeViews().stream().sorted().toList() + fs.provider().getScheme() + " " + (fs.provider() == FileSystems.getDefault().provider()) + " " + fs.getRootDirectories().iterator().next());
    probe("fs getPath", () -> q(fs.getPath("a", "b")) + q(fs.getPath("")) + q(fs.getPath("/", "x")));
    probe("fs close", () -> { fs.close(); return "closed"; });
    probe("matchers", () -> { StringBuilder sb = new StringBuilder(); String[][] cases = {{"glob:*.txt", "a.txt"}, {"glob:*.txt", "d/a.txt"}, {"glob:**.txt", "d/a.txt"}, {"glob:**/*.txt", "d/a.txt"}, {"glob:**/*.txt", "a.txt"}, {"glob:*.{java,class}", "A.class"}, {"glob:*.{java,class}", "A.cl"}, {"glob:a?c", "abc"}, {"glob:a?c", "a/c"}, {"glob:[a-c]x", "bx"}, {"glob:[!a-c]x", "bx"}, {"glob:[!a-c]x", "dx"}, {"glob:\\*", "*"}, {"glob:{a,b{c,d}}", "bd"}, {"glob:/a/*", "/a/b"}, {"glob:*", ""}, {"glob:", ""}, {"glob:a.b", "a.b"}, {"glob:a.b", "axb"}, {"regex:a.*", "abc"}, {"regex:a\\.b", "a.b"}, {"glob:**", "a/b/c"}, {"glob:a**b", "a/x/b"}, {"glob:a*b", "a/b"}, {"glob:[*]", "*"}, {"glob:[a-]", "-"}}; for (String[] c : cases) { try { sb.append(fs.getPathMatcher(c[0]).matches(Path.of(c[1]))).append(' '); } catch (Exception e) { sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()).append(' '); } } return sb; });
    probe("matcher bad", () -> fs.getPathMatcher("nosyntax"));
    probe("matcher bad2", () -> fs.getPathMatcher("foo:bar"));
    probe("matcher bad3", () -> fs.getPathMatcher("glob:{a,{b}}"));
    probe("matcher bad4", () -> fs.getPathMatcher("glob:[abc"));
    probe("matcher bad5", () -> fs.getPathMatcher("glob:{a"));
    probe("toAbsolute", () -> q(Path.of("a/b").toAbsolutePath()).replace(System.getProperty("user.dir"), "<CWD>") + q(Path.of("").toAbsolutePath()).replace(System.getProperty("user.dir"), "<CWD>") + q(Path.of("/x").toAbsolutePath()));
    probe("toFile", () -> Path.of("a/b").toFile().getPath() + " " + Path.of("/x/y").toFile().toPath() + " " + new File("q//r/").toPath() + " " + new File("").toPath().toString().isEmpty());
    probe("watch", () -> { try (WatchService ws = fs.newWatchService()) { return ws.getClass().getSimpleName().length() > 0; } });
    probe("lookup", () -> fs.getUserPrincipalLookupService().getClass().getSimpleName().length() > 0);
    probe("stores", () -> { int n = 0; for (FileStore s : fs.getFileStores()) { n++; } return n > 0; });
    probe("hashes equal", () -> Path.of("a//b").equals(Path.of("a/b")) + " " + (Path.of("a//b").hashCode() == Path.of("a/b").hashCode()) + " " + Path.of("a").equals("a") + " " + Path.of("A").compareTo(Path.of("a")) + " " + Path.of("a").compareTo(Path.of("b")) + " " + new TreeSet<>(List.of(Path.of("b"), Path.of("a/b"), Path.of("a"), Path.of("/"), Path.of(""))));
    probe("resolve null", () -> Path.of("a").resolve((Path) null));
    probe("resolve null str", () -> Path.of("a").resolve((String) null));
    probe("startsWith null", () -> Path.of("a").startsWith((Path) null));
    probe("relativize null", () -> Path.of("a").relativize(null));
    probe("compare null", () -> Path.of("a").compareTo(null));
    probe("mixed provider", () -> Path.of("a").resolve(new Path() {
      public FileSystem getFileSystem() { return fs; } public boolean isAbsolute() { return false; } public Path getRoot() { return null; } public Path getFileName() { return null; } public Path getParent() { return null; } public int getNameCount() { return 0; } public Path getName(int i) { return null; } public Path subpath(int a, int b) { return null; } public boolean startsWith(Path o) { return false; } public boolean endsWith(Path o) { return false; } public Path normalize() { return null; } public Path resolve(Path o) { return null; } public Path relativize(Path o) { return null; } public URI toUri() { return null; } public Path toAbsolutePath() { return null; } public Path toRealPath(LinkOption... o) { return null; } public WatchKey register(WatchService w, WatchEvent.Kind<?>[] e, WatchEvent.Modifier... m) { return null; } public int compareTo(Path o) { return 0; } public boolean equals(Object o) { return false; } public int hashCode() { return 0; } public String toString() { return "x"; }
    }));
  }
}
