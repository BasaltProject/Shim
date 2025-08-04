/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FilesModern {
    public static void main(String[] args) throws Exception {
        testPathBasics();
        testCharsetsAndStrings();
        testURI();
        testFilesIO();
        testPathMatcher();
        System.out.println("ALL FILES MODERN TESTS PASSED");
    }

    private static void testPathBasics() {
        Path p = Path.of("alpha", "beta", "gamma");
        System.out.println("p: " + p);
        System.out.println("p nameCount: " + p.getNameCount());
        System.out.println("p getName(0): " + p.getName(0));
        System.out.println("p getName(1): " + p.getName(1));
        System.out.println("p getName(2): " + p.getName(2));
        System.out.println("p fileName: " + p.getFileName());
        System.out.println("p parent: " + p.getParent());
        System.out.println("p isAbsolute: " + p.isAbsolute());
        System.out.println("p root: " + p.getRoot());
        System.out.println("p subpath(0,2): " + p.subpath(0, 2));
        System.out.println("p startsWith alpha: " + p.startsWith("alpha"));
        System.out.println("p startsWith alpha/beta: " + p.startsWith("alpha/beta"));
        System.out.println("p endsWith gamma: " + p.endsWith("gamma"));
        System.out.println("p endsWith beta/gamma: " + p.endsWith("beta/gamma"));
        System.out.println("p endsWith alpha: " + p.endsWith("alpha"));
        System.out.println("p resolve delta: " + p.resolve("delta"));
        System.out.println("p resolveSibling sibling: " + p.resolveSibling("sibling"));

        Path single = Path.of("single");
        System.out.println("single parent: " + single.getParent());
        System.out.println("single resolveSibling other: " + single.resolveSibling("other"));

        Path abs = Path.of("/var", "log", "app.log");
        System.out.println("abs: " + abs);
        System.out.println("abs isAbsolute: " + abs.isAbsolute());
        System.out.println("abs root: " + abs.getRoot());
        System.out.println("abs parent: " + abs.getParent());
        System.out.println("abs fileName: " + abs.getFileName());

        Path norm1 = Path.of("a/./b/../c/d").normalize();
        System.out.println("norm1: " + norm1);
        Path norm2 = Path.of("/a/../b").normalize();
        System.out.println("norm2: " + norm2);

        Path r1 = Path.of("a/b").relativize(Path.of("a/b/c/d"));
        System.out.println("r1: " + r1);
        Path r2 = Path.of("/a/b").relativize(Path.of("/a/b"));
        System.out.println("r2: " + r2);

        Path fromPaths = Paths.get("x", "y", "z");
        System.out.println("fromPaths equals: " + fromPaths.equals(Path.of("x/y/z")));
    }

    private static void testCharsetsAndStrings() {
        System.out.println("utf8 name: " + StandardCharsets.UTF_8.name());
        System.out.println("iso name: " + StandardCharsets.ISO_8859_1.name());
        System.out.println("ascii name: " + StandardCharsets.US_ASCII.name());

        Charset c1 = Charset.forName("utf-8");
        System.out.println("c1 equals utf8: " + (c1 == StandardCharsets.UTF_8));
        Charset c2 = Charset.forName("iso-8859-1");
        System.out.println("c2 equals iso: " + (c2 == StandardCharsets.ISO_8859_1));

        String text = "Hello Modern NIO! Special chars: cafe \u00e9";
        byte[] b = text.getBytes(StandardCharsets.UTF_8);
        String decoded = new String(b, StandardCharsets.UTF_8);
        System.out.println("string roundtrip: " + text.equals(decoded));
        System.out.println("utf8 length: " + b.length);
    }

    private static void testURI() {
        URI u = URI.create("https://admin:token@example.com:8443/data/items?sort=asc&page=2#heading");
        System.out.println("uri scheme: " + u.getScheme());
        System.out.println("uri authority: " + u.getAuthority());
        System.out.println("uri userInfo: " + u.getUserInfo());
        System.out.println("uri host: " + u.getHost());
        System.out.println("uri port: " + u.getPort());
        System.out.println("uri path: " + u.getPath());
        System.out.println("uri query: " + u.getQuery());
        System.out.println("uri fragment: " + u.getFragment());
        System.out.println("uri isAbsolute: " + u.isAbsolute());

        URI fileUri = URI.create("file:///tmp/basalt-test.txt");
        System.out.println("fileUri scheme: " + fileUri.getScheme());
        System.out.println("fileUri authority: " + fileUri.getAuthority());
        System.out.println("fileUri path: " + fileUri.getPath());
        Path p = Path.of(fileUri);
        System.out.println("path from fileUri: " + p);
    }

    private static void testFilesIO() throws IOException {
        Path tempDir = Files.createTempDirectory("shim-modern-test-");
        try {
            System.out.println("tempDir exists: " + Files.exists(tempDir));
            System.out.println("tempDir isDir: " + Files.isDirectory(tempDir));
            System.out.println("tempDir isRegFile: " + Files.isRegularFile(tempDir));

            Path f = tempDir.resolve("content.txt");
            System.out.println("f notExists: " + Files.notExists(f));

            Files.writeString(f, "line 1\nline 2\nline 3\n");
            System.out.println("f exists: " + Files.exists(f));
            System.out.println("f isRegFile: " + Files.isRegularFile(f));
            System.out.println("f isDir: " + Files.isDirectory(f));
            System.out.println("f size: " + Files.size(f));

            String content = Files.readString(f);
            System.out.println("content matches: " + content.equals("line 1\nline 2\nline 3\n"));

            List<String> lines = Files.readAllLines(f);
            System.out.println("lines size: " + lines.size() + ", line 0: " + lines.get(0));

            long count = Files.lines(f).count();
            System.out.println("stream lines count: " + count);

            BasicFileAttributes attrs = Files.readAttributes(f, BasicFileAttributes.class);
            System.out.println("attrs isReg: " + attrs.isRegularFile());
            System.out.println("attrs isDir: " + attrs.isDirectory());
            System.out.println("attrs size: " + attrs.size());

            Files.writeString(f, "line 4\n", StandardOpenOption.APPEND);
            System.out.println("appended lines size: " + Files.readAllLines(f).size());

            Path copyF = tempDir.resolve("content-copy.txt");
            Files.copy(f, copyF);
            System.out.println("copy exists: " + Files.exists(copyF));
            System.out.println("copy content matches: " + Files.readString(copyF).equals(Files.readString(f)));

            Path movedF = tempDir.resolve("content-moved.txt");
            Files.move(copyF, movedF);
            System.out.println("copy exists after move: " + Files.exists(copyF));
            System.out.println("moved exists: " + Files.exists(movedF));
            System.out.println("moved content matches: " + Files.readString(movedF).equals(Files.readString(f)));

            Path subDir = Files.createDirectories(tempDir.resolve("nested/level2"));
            System.out.println("nested exists: " + Files.isDirectory(subDir));

            Path leaf = subDir.resolve("leaf.txt");
            Files.writeString(leaf, "leaf data");

            List<String> listNames = Files.list(tempDir)
                .map(Path::getFileName)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());
            System.out.println("list names: " + listNames);

            List<String> walkRel = Files.walk(tempDir)
                .map(p2 -> tempDir.relativize(p2).toString())
                .sorted()
                .collect(Collectors.toList());
            System.out.println("walk count: " + walkRel.size());
            System.out.println("walk contains nested/level2/leaf.txt: " + walkRel.contains("nested/level2/leaf.txt"));

            Files.delete(leaf);
            Files.delete(subDir);
            Files.delete(tempDir.resolve("nested"));
            Files.delete(movedF);
            Files.delete(f);
        } finally {
            Files.deleteIfExists(tempDir);
            System.out.println("tempDir cleaned: " + !Files.exists(tempDir));
        }
    }

    private static void testPathMatcher() {
        PathMatcher pm = FileSystems.getDefault().getPathMatcher("glob:*.txt");
        System.out.println("matches report.txt: " + pm.matches(Path.of("report.txt")));
        System.out.println("matches report.csv: " + pm.matches(Path.of("report.csv")));

        PathMatcher pmRegex = FileSystems.getDefault().getPathMatcher("regex:.*\\.java");
        System.out.println("matches Main.java: " + pmRegex.matches(Path.of("Main.java")));
        System.out.println("matches Main.kt: " + pmRegex.matches(Path.of("Main.kt")));
    }
}
