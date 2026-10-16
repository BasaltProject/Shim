/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.util.*;
import java.util.function.Supplier;

public class IoStreamsModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + t.getMessage();
    }
    System.out.println(name + "=" + out);
  }

  static String tokens(String text, boolean eol, boolean slash, boolean lower) throws IOException {
    StreamTokenizer t = new StreamTokenizer(new StringReader(text));
    t.eolIsSignificant(eol);
    t.slashSlashComments(slash);
    t.slashStarComments(slash);
    t.lowerCaseMode(lower);
    StringBuilder sb = new StringBuilder();
    while (t.nextToken() != StreamTokenizer.TT_EOF) {
      sb.append(t).append(" | ");
    }
    return sb.toString();
  }

  public static void main(String[] args) throws Exception {
    probe("chararray-reader", () -> {
      try {
        CharArrayReader r = new CharArrayReader("hello world".toCharArray(), 2, 6);
        char[] buf = new char[10];
        int n = r.read(buf, 0, 3);
        r.mark(0);
        long s = r.skip(2);
        int c = r.read();
        r.reset();
        return n + " " + new String(buf, 0, n) + " " + s + " " + (char) c + " " + (char) r.read() + " " + r.ready() + " " + r.markSupported() + " " + r.skip(100) + " " + r.read() + " " + r.read(buf, 0, 1);
      } catch (IOException e) { return e; }
    });
    probe("chararray-reader-closed", () -> {
      try {
        CharArrayReader r = new CharArrayReader(new char[] {'a'});
        r.close();
        return r.read();
      } catch (IOException e) { return e.getMessage(); }
    });
    probe("chararray-writer", () -> {
      try {
        CharArrayWriter w = new CharArrayWriter(1);
        w.write("abcdef", 1, 3);
        w.write('z');
        w.append("xyz").append("hello", 1, 3).append('!');
        StringWriter sw = new StringWriter();
        w.writeTo(sw);
        return w + " " + w.size() + " " + sw + " " + new String(w.toCharArray());
      } catch (IOException e) { return e; }
    });
    probe("chararray-writer-bad", () -> new CharArrayWriter(-1));
    probe("pushback", () -> {
      try {
        PushbackInputStream p = new PushbackInputStream(new ByteArrayInputStream(new byte[] {1, 2, 3, 4, 5}), 3);
        int a = p.read();
        p.unread(a);
        p.unread(9);
        byte[] b = new byte[10];
        int n = p.read(b, 0, 10);
        String r = a + " " + n + " " + Arrays.toString(b) + " " + p.available() + " " + p.markSupported();
        try { p.unread(new byte[] {1, 2, 3, 4}); } catch (IOException e) { r += " " + e.getMessage(); }
        p.close();
        try { p.read(); } catch (IOException e) { r += " " + e.getMessage(); }
        return r;
      } catch (IOException e) { return e; }
    });
    probe("sequence", () -> {
      try {
        SequenceInputStream s = new SequenceInputStream(new ByteArrayInputStream("abc".getBytes()), new ByteArrayInputStream("def".getBytes()));
        byte[] b = new byte[10];
        int n = s.read(b, 0, 10);
        int m = s.read(b, n, 10 - n);
        return n + " " + m + " " + new String(b, 0, n + m) + " " + s.read() + " " + s.available();
      } catch (IOException e) { return e; }
    });
    probe("sequence-enum", () -> {
      try {
        Vector<InputStream> v = new Vector<>();
        v.add(new ByteArrayInputStream("12".getBytes()));
        v.add(new ByteArrayInputStream("34".getBytes()));
        v.add(new ByteArrayInputStream("56".getBytes()));
        return new String(new SequenceInputStream(v.elements()).readAllBytes());
      } catch (IOException e) { return e; }
    });
    probe("stringbuffer-is", () -> {
      @SuppressWarnings("deprecation") StringBufferInputStream s = new StringBufferInputStream("héllo");
      byte[] b = new byte[10];
      int n = s.read(b, 0, 10);
      return n + " " + Arrays.toString(Arrays.copyOf(b, n)) + " " + s.available() + " " + s.read();
    });
    probe("lineno-is", () -> {
      try {
        @SuppressWarnings("deprecation") LineNumberInputStream s = new LineNumberInputStream(new ByteArrayInputStream("a\r\nb\rc\nd".getBytes()));
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = s.read()) != -1) { sb.append(c).append(','); }
        return sb + " " + s.getLineNumber();
      } catch (IOException e) { return e; }
    });
    probe("piped-stream", () -> {
      try {
        PipedInputStream in = new PipedInputStream(16);
        PipedOutputStream out = new PipedOutputStream(in);
        Thread w = new Thread(() -> {
          try {
            for (int i = 0; i < 100; i++) { out.write(i); }
            out.write(new byte[] {7, 8, 9}, 0, 3);
            out.close();
          } catch (IOException e) { throw new RuntimeException(e); }
        });
        w.start();
        int sum = 0;
        int count = 0;
        int c;
        while ((c = in.read()) != -1) { sum += c; count++; }
        w.join();
        return sum + " " + count;
      } catch (Exception e) { return e; }
    });
    probe("piped-stream-bulk", () -> {
      try {
        PipedInputStream in = new PipedInputStream(8);
        PipedOutputStream out = new PipedOutputStream();
        out.connect(in);
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) { data[i] = (byte) i; }
        Thread w = new Thread(() -> {
          try { out.write(data, 0, data.length); out.close(); } catch (IOException e) { throw new RuntimeException(e); }
        });
        w.start();
        byte[] got = in.readAllBytes();
        w.join();
        return Arrays.equals(data, got) + " " + got.length;
      } catch (Exception e) { return e; }
    });
    probe("piped-unconnected", () -> { try { new PipedOutputStream().write(1); return "no"; } catch (IOException e) { return e.getMessage(); } });
    probe("piped-twice", () -> { try { PipedInputStream i = new PipedInputStream(); PipedOutputStream o = new PipedOutputStream(i); o.connect(i); return "no"; } catch (IOException e) { return e.getMessage(); } });
    probe("piped-reader", () -> {
      try {
        PipedReader r = new PipedReader(8);
        PipedWriter w = new PipedWriter(r);
        Thread t = new Thread(() -> {
          try { w.write("hello piped world, long enough to wrap"); w.close(); } catch (IOException e) { throw new RuntimeException(e); }
        });
        t.start();
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[5];
        int n;
        while ((n = r.read(buf, 0, 5)) != -1) { sb.append(buf, 0, n); }
        t.join();
        return sb;
      } catch (Exception e) { return e; }
    });
    probe("piped-reader-closed", () -> { try { PipedReader r = new PipedReader(); PipedWriter w = new PipedWriter(r); r.close(); w.write(1); return "no"; } catch (IOException e) { return e.getMessage(); } });
    probe("filter-writer", () -> {
      try {
        StringWriter sw = new StringWriter();
        FilterWriter fw = new FilterWriter(sw) {
          @Override public void write(int c) throws IOException { super.write(Character.toUpperCase(c)); }
          @Override public void write(String s, int o, int l) throws IOException { super.write(s.toUpperCase(), o, l); }
        };
        fw.write('a');
        fw.write("bcd", 1, 2);
        fw.flush();
        fw.close();
        return sw;
      } catch (IOException e) { return e; }
    });
    probe("file-filter", () -> {
      try {
        File dir = File.createTempFile("iofilter", "");
        dir.delete();
        dir.mkdir();
        new File(dir, "a.txt").createNewFile();
        new File(dir, "b.log").createNewFile();
        new File(dir, "c.txt").createNewFile();
        FileFilter ff = f -> f.getName().endsWith(".txt");
        File[] r = dir.listFiles(ff);
        List<String> names = new ArrayList<>();
        for (File f : r) { names.add(f.getName()); f.delete(); }
        new File(dir, "b.log").delete();
        dir.delete();
        Collections.sort(names);
        return names + " " + new File("/definitely/not/here").listFiles(ff);
      } catch (IOException e) { return e; }
    });
    probe("tokenizer-basic", () -> { try { return tokens("foo 12.5 -3 \"str\\n\" 'q' + bar", false, false, false); } catch (IOException e) { return e; } });
    probe("tokenizer-eol", () -> { try { return tokens("a b\nc\r\nd", true, false, false); } catch (IOException e) { return e; } });
    probe("tokenizer-comments", () -> { try { return tokens("a // c1\nb /* c2 \n */ c / d # x", false, true, false); } catch (IOException e) { return e; } });
    probe("tokenizer-lower", () -> { try { return tokens("Hello WORLD", false, false, true); } catch (IOException e) { return e; } });
    probe("tokenizer-numbers", () -> { try { return tokens("1 2.5 .5 -.5 - 1e5 007 1.2.3", false, false, false); } catch (IOException e) { return e; } });
    probe("tokenizer-escapes", () -> { try { return tokens("\"a\\tb\\101\\x\" \"unterminated\nnext", false, false, false); } catch (IOException e) { return e; } });
    probe("tokenizer-syntax", () -> {
      try {
        StreamTokenizer t = new StreamTokenizer(new StringReader("a-b_c d.e"));
        t.resetSyntax();
        t.wordChars('a', 'z');
        t.wordChars('_', '_');
        t.whitespaceChars(' ', ' ');
        t.ordinaryChar('-');
        StringBuilder sb = new StringBuilder();
        while (t.nextToken() != StreamTokenizer.TT_EOF) { sb.append(t.ttype).append(':').append(t.sval).append(' '); }
        t.pushBack();
        return sb + " " + t.lineno() + " " + t;
      } catch (IOException e) { return e; }
    });
    probe("tokenizer-pushback", () -> {
      try {
        StreamTokenizer t = new StreamTokenizer(new StringReader("x y"));
        t.nextToken();
        t.pushBack();
        t.nextToken();
        t.nextToken();
        return t.sval + " " + t.lineno();
      } catch (IOException e) { return e; }
    });
    probe("tokenizer-null", () -> new StreamTokenizer((Reader) null));
  }
}
