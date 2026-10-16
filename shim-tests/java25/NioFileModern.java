/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NioFileModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  static String ROOT = "";

  interface IoBody { Object run() throws Exception; }

  static void io(String name, IoBody body) {
    probe(name, () -> {
      try { return body.run(); } catch (Exception e) { return "!" + e.getClass().getName() + ":" + String.valueOf(e.getMessage()).replace(ROOT, "<root>").replaceAll("x[0-9]{6,}", "xN"); }
    });
  }

  static String rel(Path root, Path p) {
    return root.relativize(p).toString();
  }

  public static void main(String[] args) throws Exception {
    Path root = Files.createTempDirectory("nioroot");
    ROOT = root.toString();
    Files.createDirectories(root.resolve("a/b/c"));
    Files.createDirectories(root.resolve("d"));
    Files.writeString(root.resolve("a/one.txt"), "hello");
    Files.writeString(root.resolve("a/b/two.log"), "world!");
    Files.writeString(root.resolve("a/b/c/three.txt"), "x");
    Files.writeString(root.resolve("d/.hidden"), "h");
    Files.writeString(root.resolve("d/same1"), "abcdef");
    Files.writeString(root.resolve("d/same2"), "abcdef");
    Files.writeString(root.resolve("d/diff"), "abcxef");
    Files.writeString(root.resolve("d/short"), "abc");

    io("walk-events", () -> {
      List<String> events = new ArrayList<>();
      Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
        public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) { events.add("pre:" + rel(root, d)); return FileVisitResult.CONTINUE; }
        public FileVisitResult visitFile(Path f, BasicFileAttributes a) { events.add("file:" + rel(root, f) + ":" + a.size()); return FileVisitResult.CONTINUE; }
        public FileVisitResult postVisitDirectory(Path d, IOException e) { events.add("post:" + rel(root, d) + ":" + e); return FileVisitResult.CONTINUE; }
      });
      Collections.sort(events);
      return events;
    });
    io("walk-skip-subtree", () -> {
      List<String> events = new ArrayList<>();
      Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
        public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) { events.add("pre:" + rel(root, d)); return d.endsWith("b") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE; }
        public FileVisitResult visitFile(Path f, BasicFileAttributes a) { events.add("file:" + rel(root, f)); return FileVisitResult.CONTINUE; }
        public FileVisitResult postVisitDirectory(Path d, IOException e) { events.add("post:" + rel(root, d)); return FileVisitResult.CONTINUE; }
      });
      Collections.sort(events);
      return events;
    });
    io("walk-terminate", () -> {
      int[] count = {0};
      Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
        public FileVisitResult visitFile(Path f, BasicFileAttributes a) { return ++count[0] == 2 ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE; }
      });
      return count[0];
    });
    io("walk-depth", () -> {
      List<String> events = new ArrayList<>();
      Files.walkFileTree(root, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
        public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) { events.add("pre:" + rel(root, d)); return FileVisitResult.CONTINUE; }
        public FileVisitResult visitFile(Path f, BasicFileAttributes a) { events.add("file:" + rel(root, f) + ":" + a.isDirectory()); return FileVisitResult.CONTINUE; }
      });
      Collections.sort(events);
      return events;
    });
    io("walk-missing", () -> { Files.walkFileTree(root.resolve("nope"), new SimpleFileVisitor<Path>() { }); return "no"; });
    io("walk-bad-depth", () -> { Files.walkFileTree(root, EnumSet.noneOf(FileVisitOption.class), -1, new SimpleFileVisitor<Path>() { }); return "no"; });
    io("find", () -> Files.find(root, 10, (p, a) -> a.isRegularFile() && p.toString().endsWith(".txt")).map(p -> rel(root, p)).sorted().collect(Collectors.toList()));
    io("find-depth", () -> Files.find(root, 1, (p, a) -> true).map(p -> rel(root, p)).sorted().collect(Collectors.toList()));
    io("find-bad", () -> Files.find(root, -1, (p, a) -> true));
    io("glob-stream", () -> { List<String> r = new ArrayList<>(); try (DirectoryStream<Path> s = Files.newDirectoryStream(root.resolve("d"), "same*")) { for (Path p : s) r.add(p.getFileName().toString()); } Collections.sort(r); return r; });
    io("glob-star", () -> { List<String> r = new ArrayList<>(); try (DirectoryStream<Path> s = Files.newDirectoryStream(root.resolve("d"), "*")) { for (Path p : s) r.add(p.getFileName().toString()); } Collections.sort(r); return r; });
    io("glob-braces", () -> { List<String> r = new ArrayList<>(); try (DirectoryStream<Path> s = Files.newDirectoryStream(root.resolve("d"), "{diff,short}")) { for (Path p : s) r.add(p.getFileName().toString()); } Collections.sort(r); return r; });
    io("glob-missing", () -> Files.newDirectoryStream(root.resolve("zzz"), "*"));
    io("glob-notdir", () -> Files.newDirectoryStream(root.resolve("d/same1"), "*"));
    io("hidden", () -> Files.isHidden(root.resolve("d/.hidden")) + " " + Files.isHidden(root.resolve("d/same1")) + " " + Files.isHidden(root.resolve("missing/.x")));
    io("mismatch", () -> Files.mismatch(root.resolve("d/same1"), root.resolve("d/same2")) + " " + Files.mismatch(root.resolve("d/same1"), root.resolve("d/diff")) + " " + Files.mismatch(root.resolve("d/same1"), root.resolve("d/short")) + " " + Files.mismatch(root.resolve("d/short"), root.resolve("d/same1")) + " " + Files.mismatch(root.resolve("d/same1"), root.resolve("d/same1")));
    io("mismatch-missing", () -> Files.mismatch(root.resolve("d/same1"), root.resolve("nope")));
    io("set-mtime", () -> { Path p = root.resolve("d/same1"); Files.setLastModifiedTime(p, FileTime.fromMillis(1700000000000L)); return Files.getLastModifiedTime(p).toMillis(); });
    io("set-mtime-missing", () -> Files.setLastModifiedTime(root.resolve("nope"), FileTime.fromMillis(0)));
    io("attrs-string", () -> new TreeMap<>(Files.readAttributes(root.resolve("a/one.txt"), "size,isDirectory,isRegularFile,isOther,isSymbolicLink")));
    io("attrs-all", () -> new TreeSet<>(Files.readAttributes(root.resolve("a/one.txt"), "basic:*").keySet()));
    io("attrs-bad-name", () -> Files.readAttributes(root.resolve("a/one.txt"), "bogus"));
    io("attrs-bad-view", () -> Files.readAttributes(root.resolve("a/one.txt"), "zip:size"));
    io("attrs-missing", () -> Files.readAttributes(root.resolve("nope"), "size"));
    io("get-attribute", () -> Files.getAttribute(root.resolve("a/b/two.log"), "size") + " " + Files.getAttribute(root.resolve("a/b"), "basic:isDirectory"));
    io("get-attribute-bad", () -> Files.getAttribute(root.resolve("a/b/two.log"), "*"));
    io("temp-dir-in", () -> { Path t = Files.createTempDirectory(root, "pre-"); boolean ok = Files.isDirectory(t) && t.getFileName().toString().startsWith("pre-") && t.getParent().equals(root); Files.delete(t); return ok; });
    io("temp-dir-missing", () -> Files.createTempDirectory(root.resolve("nope"), "x"));
    io("glob-class", () -> { List<String> r = new ArrayList<>(); try (DirectoryStream<Path> s = Files.newDirectoryStream(root.resolve("d"), "same[12]")) { for (Path p : s) r.add(p.getFileName().toString()); } Collections.sort(r); return r; });
    io("glob-negated", () -> { List<String> r = new ArrayList<>(); try (DirectoryStream<Path> s = Files.newDirectoryStream(root.resolve("d"), "same[!1]")) { for (Path p : s) r.add(p.getFileName().toString()); } Collections.sort(r); return r; });
    io("glob-bad", () -> Files.newDirectoryStream(root.resolve("d"), "{a"));
    io("resolve-varargs", () -> {
      Path p = Path.of("/base");
      return p.resolve("a", "b", "c") + " " + p.resolve("a") + " " + p.resolve(Path.of("x"), Path.of("y")) + " " + p.resolve("a", "/abs", "z") + " " + p.resolve("a", new String[0]);
    });
    probe("exc-invalid-path", () -> { InvalidPathException e = new InvalidPathException("a\0b", "Nul character not allowed", 1); return e.getMessage() + "|" + e.getIndex() + "|" + e.getInput() + "|" + e.getReason() + "|" + new InvalidPathException("x", "bad").getMessage() + "|" + new InvalidPathException("x", "bad").getIndex(); });
    probe("exc-invalid-path-bad", () -> new InvalidPathException("x", "bad", -2));
    probe("exc-invalid-path-null", () -> new InvalidPathException(null, "bad"));
    probe("exc-dir-iter", () -> { DirectoryIteratorException e = new DirectoryIteratorException(new IOException("boom")); return e.getCause().getMessage() + " " + (e instanceof ConcurrentModificationException); });
    probe("exc-dir-iter-null", () -> new DirectoryIteratorException(null));
    probe("exc-atomic", () -> new AtomicMoveNotSupportedException("a", "b", "nope").getMessage());
    probe("exc-notlink", () -> new NotLinkException("a").getMessage() + "|" + new NotLinkException("a", "b", "r").getMessage());
    probe("exc-loop", () -> new FileSystemLoopException("p").getMessage() + "|" + new FileSystemLoopException("p").getFile());
    probe("exc-simple", () -> new ClosedDirectoryStreamException().getMessage() + new ClosedFileSystemException().getMessage() + new ClosedWatchServiceException().getMessage() + new FileSystemAlreadyExistsException("x").getMessage() + new FileSystemNotFoundException().getMessage() + new ProviderMismatchException("m").getMessage() + new ProviderNotFoundException("p").getMessage() + new ReadOnlyFileSystemException().getMessage());
    probe("exc-hierarchy", () -> (new ClosedFileSystemException() instanceof IllegalStateException) + " " + (new ProviderMismatchException() instanceof IllegalArgumentException) + " " + (new ReadOnlyFileSystemException() instanceof UnsupportedOperationException) + " " + (new NotLinkException("a") instanceof FileSystemException) + " " + (new InvalidPathException("a", "b") instanceof IllegalArgumentException));
    probe("link-permission", () -> new LinkPermission("hard").getName() + " " + new LinkPermission("symbolic", null).getName() + " " + new LinkPermission("hard").getActions().isEmpty());
    probe("link-permission-bad", () -> new LinkPermission("other"));
    probe("access-mode", () -> Arrays.toString(AccessMode.values()) + " " + AccessMode.valueOf("WRITE").ordinal());
    probe("watch-kinds", () -> StandardWatchEventKinds.ENTRY_CREATE.name() + " " + StandardWatchEventKinds.ENTRY_DELETE + " " + StandardWatchEventKinds.ENTRY_MODIFY.type().getSimpleName() + " " + StandardWatchEventKinds.OVERFLOW.type().getSimpleName() + " " + StandardWatchEventKinds.OVERFLOW.name());

    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
      public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException { Files.delete(f); return FileVisitResult.CONTINUE; }
      public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException { Files.delete(d); return FileVisitResult.CONTINUE; }
    });
    System.out.println("cleaned=" + Files.notExists(root));
  }
}
