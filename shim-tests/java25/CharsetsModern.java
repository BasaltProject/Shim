/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;

public class CharsetsModern {
  static void probe(String name, Supplier<Object> body) {
    String out;
    try {
      out = String.valueOf(body.get());
    } catch (Throwable t) {
      out = "!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage());
    }
    System.out.println(name + "=" + out);
  }

  interface IoBody { Object run() throws Exception; }

  static void io(String name, IoBody body) {
    probe(name, () -> {
      try { return body.run(); } catch (Exception e) { return "!" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? "npe" : e.getMessage()); }
    });
  }

  static final String[] kSingleByte = {"IBM-Thai", "IBM00858", "IBM01140", "IBM01141", "IBM01142", "IBM01143", "IBM01144", "IBM01145", "IBM01146", "IBM01147", "IBM01148", "IBM01149", "IBM037", "IBM1026", "IBM1047", "IBM273", "IBM277", "IBM278", "IBM280", "IBM284", "IBM285", "IBM290", "IBM297", "IBM420", "IBM424", "IBM437", "IBM500", "IBM775", "IBM850", "IBM852", "IBM855", "IBM857", "IBM860", "IBM861", "IBM862", "IBM863", "IBM864", "IBM865", "IBM866", "IBM868", "IBM869", "IBM870", "IBM871", "IBM918", "ISO-8859-13", "ISO-8859-15", "ISO-8859-16", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8", "ISO-8859-9", "JIS_X0201", "KOI8-R", "KOI8-U", "TIS-620", "windows-1250", "windows-1251", "windows-1252", "windows-1253", "windows-1254", "windows-1255", "windows-1256", "windows-1257", "windows-1258", "x-IBM1006", "x-IBM1025", "x-IBM1046", "x-IBM1097", "x-IBM1098", "x-IBM1112", "x-IBM1122", "x-IBM1123", "x-IBM1124", "x-IBM1129", "x-IBM1166", "x-IBM737", "x-IBM833", "x-IBM856", "x-IBM874", "x-IBM875", "x-IBM921", "x-IBM922", "x-iso-8859-11", "x-MacArabic", "x-MacCentralEurope", "x-MacCroatian", "x-MacCyrillic", "x-MacDingbat", "x-MacGreek", "x-MacHebrew", "x-MacIceland", "x-MacRoman", "x-MacRomania", "x-MacSymbol", "x-MacThai", "x-MacTurkish", "x-MacUkraine", "x-windows-874"};
  static final String[] kUnicode = {"UTF-8", "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-32", "UTF-32BE", "UTF-32LE", "x-UTF-16LE-BOM", "X-UTF-32BE-BOM", "X-UTF-32LE-BOM", "CESU-8"};
  static final String kSample = "Hello, w\u00f6rld \u00e9\u00e8\u00ea \u20ac \u00a3 \u0416\u4e2d\u6587 \ud83d\ude00 end";

  static String hex(byte[] b) {
    StringBuilder sb = new StringBuilder();
    for (byte x : b) sb.append(String.format("%02x", x));
    return sb.toString();
  }

  static String esc(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) sb.append(String.format("%04x ", (int) s.charAt(i)));
    return sb.toString();
  }

  static long hash(long h, String s) {
    return h * 31 + s.hashCode();
  }

  public static void main(String[] args) throws Exception {
    io("default", () -> Charset.defaultCharset() + " " + StandardCharsets.UTF_8 + " " + StandardCharsets.ISO_8859_1.aliases().size());
    for (String n : new String[] {"UTF-8", "utf8", "UTF8", "Utf-8", "latin1", "ISO-8859-1", "ISO_8859_1", "ascii", "us-ascii", "UTF-16", "UTF_16BE", "unicode", "UnicodeBig", "cp1252", "windows-1252", "Cp1252", "CP437", "IBM437", "koi8-r", "x-MacRoman", "MacRoman", "UTF-32", "cesu8", "nope", "x-nope", "", "-bad", "bad name", "bad/name", "ok.name_1+x:y", "\u00e9"}) {
      io("forName " + n, () -> { Charset cs = Charset.forName(n); return cs + " " + cs.name() + " " + new TreeSet<>(cs.aliases()) + " " + cs.isRegistered() + " " + cs.canEncode() + " " + cs.displayName(); });
      io("isSupported " + n, () -> Charset.isSupported(n));
    }
    io("forName null", () -> Charset.forName(null));
    io("isSupported null", () -> Charset.isSupported(null));
    io("forName fallback", () -> Charset.forName("nope", StandardCharsets.UTF_8) + " " + Charset.forName("!!", StandardCharsets.UTF_8) + " " + Charset.forName("utf-8", null) + " " + Charset.forName("nope", null));
    io("forName fallback null name", () -> Charset.forName(null, StandardCharsets.UTF_8));
    io("aliases sorted", () -> new TreeSet<>(Charset.forName("UTF-32").aliases()) + " " + new TreeSet<>(Charset.forName("ISO-8859-1").aliases()) + " " + new TreeSet<>(Charset.forName("US-ASCII").aliases()) + " " + new TreeSet<>(Charset.forName("windows-1252").aliases()) + " " + new TreeSet<>(Charset.forName("UTF-16").aliases()));
    io("aliases immutable", () -> { try { Charset.forName("UTF-8").aliases().add("x"); return "mutable"; } catch (UnsupportedOperationException e) { return "immutable"; } });
    io("available", () -> { SortedMap<String, Charset> m = Charset.availableCharsets(); StringBuilder sb = new StringBuilder(); for (String n : kSingleByte) sb.append(m.containsKey(n) ? "" : n + " "); for (String n : kUnicode) sb.append(m.containsKey(n) ? "" : n + " "); return sb + "|" + m.get("utf-8") + "|" + m.comparator().getClass().equals(String.CASE_INSENSITIVE_ORDER.getClass()) + "|" + (m.size() > 100); });
    io("available immutable", () -> { try { Charset.availableCharsets().put("x", null); return "mutable"; } catch (UnsupportedOperationException e) { return "immutable"; } });
    io("equals", () -> StandardCharsets.UTF_8.equals(Charset.forName("utf8")) + " " + (StandardCharsets.UTF_8.hashCode() == Charset.forName("UTF-8").hashCode()) + " " + StandardCharsets.UTF_8.compareTo(StandardCharsets.UTF_16) + " " + StandardCharsets.UTF_8.equals("UTF-8") + " " + (Charset.forName("latin1") == StandardCharsets.ISO_8859_1));
    io("contains", () -> { StringBuilder sb = new StringBuilder(); for (String a : new String[] {"UTF-8", "UTF-16", "US-ASCII", "ISO-8859-1", "windows-1252", "UTF-32"}) for (String b : new String[] {"UTF-8", "US-ASCII", "ISO-8859-1", "windows-1252", "ISO-8859-2"}) sb.append(Charset.forName(a).contains(Charset.forName(b)) ? '1' : '0'); return sb; });
    io("metrics", () -> { StringBuilder sb = new StringBuilder(); for (String n : kUnicode) { Charset cs = Charset.forName(n); sb.append(n).append(':').append(cs.newDecoder().averageCharsPerByte()).append('/').append(cs.newDecoder().maxCharsPerByte()).append('/').append(cs.newEncoder().averageBytesPerChar()).append('/').append(cs.newEncoder().maxBytesPerChar()).append('/').append(hex(cs.newEncoder().replacement())).append(' '); } for (String n : new String[] {"ISO-8859-1", "US-ASCII", "windows-1252", "IBM037", "x-MacRoman"}) { Charset cs = Charset.forName(n); sb.append(n).append(':').append(cs.newEncoder().maxBytesPerChar()).append('/').append(hex(cs.newEncoder().replacement())).append(' '); } return sb; });
    io("historical", () -> { StringBuilder sb = new StringBuilder(); for (String n : kUnicode) sb.append(new InputStreamReader(new ByteArrayInputStream(new byte[0]), Charset.forName(n)).getEncoding()).append(' '); for (String n : kSingleByte) sb.append(new InputStreamReader(new ByteArrayInputStream(new byte[0]), Charset.forName(n)).getEncoding()).append(' '); sb.append(new InputStreamReader(new ByteArrayInputStream(new byte[0]), "latin1").getEncoding()); return sb; });
    io("registered", () -> { StringBuilder sb = new StringBuilder(); for (String n : kSingleByte) sb.append(Charset.forName(n).isRegistered() ? '1' : '0'); for (String n : kUnicode) sb.append(Charset.forName(n).isRegistered() ? '1' : '0'); return sb; });
    io("aliases all", () -> { long h = 0; for (String n : kSingleByte) h = hash(h, new TreeSet<>(Charset.forName(n).aliases()).toString()); for (String n : kUnicode) h = hash(h, new TreeSet<>(Charset.forName(n).aliases()).toString()); return h; });
    List<String> all = new ArrayList<>(Arrays.asList(kUnicode)); all.add("US-ASCII"); all.add("ISO-8859-1");
    all.addAll(Arrays.asList("ISO-8859-2", "ISO-8859-5", "ISO-8859-15", "windows-1250", "windows-1252", "windows-1257", "KOI8-R", "IBM437", "IBM850", "IBM037", "x-MacRoman", "JIS_X0201", "IBM420", "IBM864", "TIS-620"));
    for (String n : all) {
      io("encode " + n, () -> {
        Charset cs = Charset.forName(n);
        return hex(kSample.getBytes(cs)) + " " + hex(cs.encode(kSample).array()).length() + " " + hex("a\ud800b\udc00c\ud83d".getBytes(cs));
      });
      io("decode all bytes " + n, () -> {
        Charset cs = Charset.forName(n);
        long h = 0;
        for (int b = 0; b < 256; b++) h = hash(h, esc(new String(new byte[] {(byte) b}, cs)));
        byte[] two = new byte[2];
        for (int b = 0; b < 256; b += 11) for (int c = 0; c < 256; c += 7) { two[0] = (byte) b; two[1] = (byte) c; h = hash(h, esc(new String(two, cs))); }
        return h;
      });
      io("canEncode sweep " + n, () -> {
        CharsetEncoder e = Charset.forName(n).newEncoder();
        long h = 0;
        boolean special = Arrays.asList("windows-1252", "IBM437", "JIS_X0201", "x-IBM1129", "IBM420", "IBM037", "IBM864").contains(n);
        int step = special ? 1 : 29;
        for (int c = 0; c < 0x10000; c += (special && c >= 0x3000 && c < 0xfe00) ? 211 : step) { if (c >= 0xd800 && c <= 0xdfff && c % 97 != 0) continue; h = h * 31 + (e.canEncode((char) c) ? 1 : 0); }
        for (int c = 0xd7f0; c < 0xe010; c++) h = h * 31 + (e.canEncode((char) c) ? 1 : 0);
        return h;
      });
      io("roundtrip sweep " + n, () -> {
        Charset cs = Charset.forName(n);
        CharsetEncoder e = cs.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        CharsetDecoder d = cs.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        long h = 0;
        for (int c = 0; c < 0x10000; c += 97) {
          String s = String.valueOf((char) c);
          if (c >= 0xd800 && c <= 0xdfff) s = s + "x";
          byte[] b = s.getBytes(cs);
          h = hash(h, hex(b));
          h = hash(h, esc(new String(b, cs)));
        }
        return h;
      });
    }
    io("utf8 malformed fuzz", () -> {
      long h = 0;
      long seed = 12345;
      for (int i = 0; i < 1500; i++) {
        int len = 1 + (int) ((seed >>> 8) % 6);
        byte[] b = new byte[len];
        for (int j = 0; j < len; j++) { seed = seed * 6364136223846793005L + 1442695040888963407L; int v = (int) (seed >>> 33) & 0xff; b[j] = (byte) ((v % 3 == 0) ? 0x80 + (v % 64) : (v % 3 == 1) ? 0xc0 + (v % 64) : v); }
        h = hash(h, esc(new String(b, StandardCharsets.UTF_8)));
        try { StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(b)); h = h * 31 + 1; } catch (CharacterCodingException ex) { h = h * 31 + ((MalformedInputException) ex).getInputLength(); }
      }
      return h;
    });
    for (String n : new String[] {"UTF-16", "UTF-16BE", "UTF-16LE", "UTF-32", "UTF-32BE", "UTF-32LE", "x-UTF-16LE-BOM", "CESU-8", "US-ASCII"}) {
      io("malformed fuzz " + n, () -> {
        Charset cs = Charset.forName(n);
        long h = 0;
        long seed = 99;
        for (int i = 0; i < 800; i++) {
          int len = 1 + (int) ((seed >>> 8) % 10);
          byte[] b = new byte[len];
          for (int j = 0; j < len; j++) { seed = seed * 6364136223846793005L + 1442695040888963407L; int v = (int) (seed >>> 33) & 0xff; b[j] = (byte) (v % 4 == 0 ? 0xd8 + (v % 8) : v % 4 == 1 ? 0xfe : v % 4 == 2 ? v % 3 : v); }
          h = hash(h, esc(new String(b, cs)));
          try { cs.newDecoder().decode(ByteBuffer.wrap(b)); h = h * 31 + 1; } catch (CharacterCodingException ex) { h = h * 31 + (ex instanceof MalformedInputException ? 100 + ((MalformedInputException) ex).getInputLength() : 200 + ((UnmappableCharacterException) ex).getInputLength()); }
        }
        return h;
      });
    }
    io("decoder actions", () -> {
      byte[] b = {0x41, (byte) 0xff, 0x42, (byte) 0xe2, (byte) 0x82};
      StringBuilder sb = new StringBuilder();
      for (CodingErrorAction a : new CodingErrorAction[] {CodingErrorAction.REPORT, CodingErrorAction.IGNORE, CodingErrorAction.REPLACE}) {
        CharsetDecoder d = StandardCharsets.UTF_8.newDecoder().onMalformedInput(a).onUnmappableCharacter(a);
        try { sb.append(esc(d.decode(ByteBuffer.wrap(b).asReadOnlyBuffer()).toString())); } catch (CharacterCodingException e) { sb.append(e.getClass().getSimpleName()).append(':').append(e.getMessage()); }
        sb.append(" | ");
      }
      return sb;
    });
    io("decoder replace string", () -> { CharsetDecoder d = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).replaceWith("??"); return esc(d.decode(ByteBuffer.wrap(new byte[] {(byte) 0xff, 0x41})).toString()) + d.replacement(); });
    io("decoder replace bad", () -> { StringBuilder sb = new StringBuilder(); CharsetDecoder d = StandardCharsets.UTF_8.newDecoder(); for (String r : new String[] {null, "", "abc"}) { try { d.replaceWith(r); sb.append("ok "); } catch (IllegalArgumentException e) { sb.append(e.getMessage()).append(" | "); } } return sb; });
    io("encoder replace bad", () -> { StringBuilder sb = new StringBuilder(); CharsetEncoder e = StandardCharsets.ISO_8859_1.newEncoder(); for (byte[] r : new byte[][] {null, {}, {1, 2}}) { try { e.replaceWith(r); sb.append("ok "); } catch (IllegalArgumentException x) { sb.append(x.getMessage()).append(" | "); } } CharsetEncoder u = StandardCharsets.US_ASCII.newEncoder(); try { u.replaceWith(new byte[] {(byte) 0xff}); sb.append("ok"); } catch (IllegalArgumentException x) { sb.append(x.getMessage()); } return sb; });
    io("encoder actions", () -> {
      String s = "a\u20acb\ud800c";
      StringBuilder sb = new StringBuilder();
      for (CodingErrorAction a : new CodingErrorAction[] {CodingErrorAction.REPORT, CodingErrorAction.IGNORE, CodingErrorAction.REPLACE}) {
        CharsetEncoder e = StandardCharsets.ISO_8859_1.newEncoder().onMalformedInput(a).onUnmappableCharacter(a);
        try { sb.append(hex(toBytes(e.encode(CharBuffer.wrap(s))))); } catch (CharacterCodingException x) { sb.append(x.getClass().getSimpleName()).append(':').append(x.getMessage()); }
        sb.append(" | ");
      }
      return sb;
    });
    io("encoder replacement bytes", () -> { CharsetEncoder e = StandardCharsets.ISO_8859_1.newEncoder().onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith(new byte[] {(byte) '#'}); return hex(toBytes(e.encode(CharBuffer.wrap("a\u20acb")))) + " " + hex(e.replacement()); });
    io("canEncode", () -> { CharsetEncoder e = StandardCharsets.ISO_8859_1.newEncoder(); return e.canEncode('a') + " " + e.canEncode('\u20ac') + " " + e.canEncode("abc\u00e9") + " " + e.canEncode("abc\u20ac") + " " + e.canEncode('\ud800') + " " + e.canEncode("\ud83d\ude00"); });
    io("canEncode state", () -> { CharsetEncoder e = StandardCharsets.UTF_8.newEncoder(); ByteBuffer out = ByteBuffer.allocate(8); e.encode(CharBuffer.wrap("ab"), out, false); try { e.canEncode('a'); return "ok"; } catch (IllegalStateException x) { return x.getMessage(); } });
    io("state errors", () -> { CharsetDecoder d = StandardCharsets.UTF_8.newDecoder(); StringBuilder sb = new StringBuilder(); ByteBuffer in = ByteBuffer.wrap(new byte[] {0x41}); CharBuffer out = CharBuffer.allocate(4); d.decode(in, out, true); try { d.decode(in, out, false); } catch (IllegalStateException x) { sb.append(x.getMessage()).append(" | "); } try { d.flush(CharBuffer.allocate(1)); d.flush(CharBuffer.allocate(1)); sb.append("flushed twice | "); d.decode(in, out, true); } catch (IllegalStateException x) { sb.append(x.getMessage()); } return sb; });
    io("flush before end", () -> { CharsetDecoder d = StandardCharsets.UTF_8.newDecoder(); try { d.flush(CharBuffer.allocate(1)); return "ok"; } catch (IllegalStateException x) { return x.getMessage(); } });
    io("small buffers", () -> {
      CharsetEncoder e = StandardCharsets.UTF_8.newEncoder();
      CharBuffer in = CharBuffer.wrap("a\u00e9\u20ac\ud83d\ude00z");
      ByteBuffer out = ByteBuffer.allocate(3);
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 20; i++) {
        CoderResult cr = e.encode(in, out, true);
        sb.append(cr).append(':').append(in.position()).append('/').append(out.position()).append(' ');
        if (cr.isUnderflow()) { sb.append(e.flush(out)); break; }
        out.flip(); sb.append(hex(toBytes(out))).append(' '); out.clear();
      }
      return sb;
    });
    io("small decode buffers", () -> {
      CharsetDecoder d = StandardCharsets.UTF_8.newDecoder();
      ByteBuffer in = ByteBuffer.wrap("a\u00e9\u20ac\ud83d\ude00z".getBytes(StandardCharsets.UTF_8));
      CharBuffer out = CharBuffer.allocate(2);
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 20; i++) {
        CoderResult cr = d.decode(in, out, true);
        sb.append(cr).append(':').append(in.position()).append('/').append(out.position()).append(' ');
        if (cr.isUnderflow()) break;
        out.flip(); sb.append(esc(out.toString())); out.clear();
      }
      return sb;
    });
    io("split input", () -> {
      CharsetDecoder d = StandardCharsets.UTF_8.newDecoder();
      byte[] bytes = "\u20ac\ud83d\ude00".getBytes(StandardCharsets.UTF_8);
      CharBuffer out = CharBuffer.allocate(10);
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < bytes.length; i++) {
        ByteBuffer in = ByteBuffer.wrap(bytes, i, 1);
        CoderResult cr = d.decode(in, out, i == bytes.length - 1);
        sb.append(cr).append(':').append(in.remaining()).append(' ');
      }
      sb.append(d.flush(out));
      out.flip();
      return sb + esc(out.toString());
    });
    io("coderresult", () -> CoderResult.malformedForLength(1) + " " + CoderResult.unmappableForLength(3) + " " + (CoderResult.malformedForLength(2) == CoderResult.malformedForLength(2)) + " " + CoderResult.UNDERFLOW + " " + CoderResult.OVERFLOW + " " + CoderResult.malformedForLength(1).length() + " " + CoderResult.malformedForLength(1).isError());
    io("coderresult bad", () -> CoderResult.malformedForLength(0));
    io("coderresult length", () -> CoderResult.UNDERFLOW.length());
    io("coderresult throw", () -> { StringBuilder sb = new StringBuilder(); for (CoderResult cr : new CoderResult[] {CoderResult.UNDERFLOW, CoderResult.OVERFLOW, CoderResult.malformedForLength(2), CoderResult.unmappableForLength(1)}) { try { cr.throwException(); } catch (Exception x) { sb.append(x.getClass().getSimpleName()).append(':').append(x.getMessage()).append(' '); } } return sb; });
    io("decoder misc", () -> { CharsetDecoder d = StandardCharsets.UTF_8.newDecoder(); StringBuilder sb = new StringBuilder(); sb.append(d.charset()).append(d.malformedInputAction()).append(d.unmappableCharacterAction()).append(esc(d.replacement())).append(d.isAutoDetecting()); try { d.isCharsetDetected(); } catch (UnsupportedOperationException e) { sb.append(" uoe"); } return sb; });
    io("encoder misc", () -> { CharsetEncoder e = StandardCharsets.UTF_8.newEncoder(); return e.charset() + " " + e.malformedInputAction() + " " + e.unmappableCharacterAction() + " " + hex(e.replacement()) + " " + e.isLegalReplacement(new byte[] {0x41}) + " " + e.isLegalReplacement(new byte[] {(byte) 0xff}); });
    io("charset encode decode", () -> { Charset cs = Charset.forName("windows-1252"); ByteBuffer bb = cs.encode("a\u20ac\u4e2d"); CharBuffer cb = cs.decode(ByteBuffer.wrap(new byte[] {(byte) 0x80, 0x41, (byte) 0x81})); return hex(toBytes(bb)) + " " + esc(cb.toString()); });
    io("string ctor", () -> { byte[] b = {0x41, (byte) 0xe9, 0x42}; return esc(new String(b, StandardCharsets.ISO_8859_1)) + esc(new String(b, 1, 2, StandardCharsets.UTF_16BE)) + esc(new String(b, 0, 3, "windows-1252")) + esc(new String(b)); });
    io("string ctor unsupported", () -> new String(new byte[] {1}, "nope"));
    io("string ctor null", () -> new String(new byte[] {1}, (String) null));
    io("string ctor null cs", () -> new String(new byte[] {1}, (Charset) null));
    io("string ctor bounds", () -> new String(new byte[] {1, 2}, 1, 5, StandardCharsets.UTF_8));
    io("string ctor bounds2", () -> new String(new byte[] {1, 2}, -1, 1, StandardCharsets.UTF_8));
    io("string ctor bounds3", () -> new String(new byte[] {1, 2}, 1, -1));
    io("getBytes variants", () -> { String s = "\u00e9\u20ac"; return hex(s.getBytes()) + " " + hex(s.getBytes("UTF-16")) + " " + hex(s.getBytes("iso-8859-1")) + " " + hex(s.getBytes(StandardCharsets.US_ASCII)) + " " + hex(s.getBytes(Charset.forName("windows-1252"))); });
    io("getBytes unsupported", () -> "a".getBytes("nope"));
    io("getBytes null", () -> "a".getBytes((String) null));
    io("getBytes null cs", () -> "a".getBytes((Charset) null));
    io("reader utf16 bom", () -> { byte[] b = {(byte) 0xff, (byte) 0xfe, 0x41, 0, 0x42, 0}; Reader r = new InputStreamReader(new ByteArrayInputStream(b), "UTF-16"); char[] c = new char[10]; int n = r.read(c); return n + " " + new String(c, 0, n); });
    io("reader chunks", () -> { byte[] b = "a\u00e9\u20ac\ud83d\ude00z".getBytes("UTF-8"); InputStream in = new InputStream() { int i; public int read() { return i < b.length ? b[i++] & 0xff : -1; } public int read(byte[] buf, int off, int len) { if (i >= b.length) return -1; int n = Math.min(len, Math.min(2, b.length - i)); System.arraycopy(b, i, buf, off, n); i += n; return n; } public int available() { return 0; } }; Reader r = new InputStreamReader(in, "UTF-8"); StringBuilder sb = new StringBuilder(); char[] c = new char[8]; int n; while ((n = r.read(c, 0, 8)) > 0) sb.append(n).append(':'); return sb; });
    io("reader single", () -> { Reader r = new InputStreamReader(new ByteArrayInputStream("\ud83d\ude00x".getBytes("UTF-8")), "UTF-8"); StringBuilder sb = new StringBuilder(); int c; while ((c = r.read()) >= 0) sb.append(String.format("%04x ", c)); return sb; });
    io("reader ready", () -> { Reader r = new InputStreamReader(new ByteArrayInputStream("ab".getBytes())); boolean a = r.ready(); r.read(); boolean b = r.ready(); r.read(); boolean c = r.ready(); return a + " " + b + " " + c; });
    io("reader closed", () -> { InputStreamReader r = new InputStreamReader(new ByteArrayInputStream("ab".getBytes())); r.close(); StringBuilder sb = new StringBuilder(r.getEncoding() + " "); try { r.read(); } catch (IOException e) { sb.append(e.getMessage()); } try { r.ready(); } catch (IOException e) { sb.append(" ").append(e.getMessage()); } r.close(); return sb; });
    io("reader malformed", () -> { Reader r = new InputStreamReader(new ByteArrayInputStream(new byte[] {0x41, (byte) 0xff, 0x42}), "UTF-8"); char[] c = new char[5]; int n = r.read(c); return n + esc(new String(c, 0, n)); });
    io("reader decoder report", () -> { Reader r = new InputStreamReader(new ByteArrayInputStream(new byte[] {0x41, (byte) 0xff, 0x42}), StandardCharsets.UTF_8.newDecoder()); char[] c = new char[5]; try { return r.read(c); } catch (CharacterCodingException e) { return e.getClass().getSimpleName() + " " + e.getMessage(); } });
    io("reader ctor errors", () -> { StringBuilder sb = new StringBuilder(); try { new InputStreamReader(null); } catch (NullPointerException e) { sb.append("npe "); } try { new InputStreamReader(new ByteArrayInputStream(new byte[0]), "nope"); } catch (UnsupportedEncodingException e) { sb.append(e.getMessage()).append(' '); } try { new InputStreamReader(new ByteArrayInputStream(new byte[0]), (String) null); } catch (NullPointerException e) { sb.append("npe2 "); } try { new InputStreamReader(new ByteArrayInputStream(new byte[0]), (Charset) null); } catch (NullPointerException e) { sb.append("npe3 "); } try { new InputStreamReader(new ByteArrayInputStream(new byte[0]), (CharsetDecoder) null); } catch (NullPointerException e) { sb.append("npe4"); } return sb; });
    io("writer buffering", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); Writer w = new OutputStreamWriter(bo, "UTF-8"); w.write("hello"); int a = bo.size(); w.flush(); int b = bo.size(); w.write("\u00e9"); w.close(); return a + " " + b + " " + bo.size(); });
    io("writer utf16", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); Writer w = new OutputStreamWriter(bo, "UTF-16"); w.write("hi"); w.write("\ud83d"); w.write("\ude00"); w.close(); return hex(bo.toByteArray()) + " " + w.getClass().getSimpleName(); });
    io("writer empty utf16", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); Writer w = new OutputStreamWriter(bo, "UTF-16"); w.close(); return bo.size(); });
    io("writer surrogate halves", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); Writer w = new OutputStreamWriter(bo, "UTF-8"); w.write("\ud83d"); w.flush(); int a = bo.size(); w.write("\ude00"); w.flush(); int b = bo.size(); w.write("\ud83d"); w.close(); return a + " " + b + " " + hex(bo.toByteArray()); });
    io("writer encoding names", () -> { StringBuilder sb = new StringBuilder(); for (String n : new String[] {"UTF-8", "ISO-8859-1", "windows-1252", "UTF-16LE", "latin1", "ascii"}) sb.append(new OutputStreamWriter(new ByteArrayOutputStream(), n).getEncoding()).append(' '); Writer w = new OutputStreamWriter(new ByteArrayOutputStream()); w.close(); sb.append(((OutputStreamWriter) w).getEncoding()); return sb; });
    io("writer closed", () -> { Writer w = new OutputStreamWriter(new ByteArrayOutputStream()); w.close(); StringBuilder sb = new StringBuilder(); try { w.write("x"); } catch (IOException e) { sb.append(e.getMessage()); } try { w.flush(); } catch (IOException e) { sb.append(" ").append(e.getMessage()); } w.close(); return sb; });
    io("writer encoder report", () -> { Writer w = new OutputStreamWriter(new ByteArrayOutputStream(), StandardCharsets.ISO_8859_1.newEncoder()); try { w.write("\u20ac"); w.flush(); return "none"; } catch (CharacterCodingException e) { return e.getClass().getSimpleName() + " " + e.getMessage(); } });
    io("writer replace", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); Writer w = new OutputStreamWriter(bo, "ISO-8859-1"); w.write("a\u20acb\ud83d\ude00c"); w.close(); return hex(bo.toByteArray()); });
    io("writer big", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); Writer w = new OutputStreamWriter(bo, "UTF-8"); StringBuilder sb = new StringBuilder(); for (int i = 0; i < 10000; i++) sb.append((char) ('a' + i % 26)).append('\u00e9'); w.write(sb.toString()); int before = bo.size(); w.close(); return bo.size() + " " + (before > 0) + " " + (before % 8192 < 10); });
    io("writer append", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); Writer w = new OutputStreamWriter(bo, "UTF-8"); w.append('x').append("yz").append("abcdef", 2, 4).append(null); w.close(); return new String(bo.toByteArray(), "UTF-8"); });
    io("writer ctor errors", () -> { StringBuilder sb = new StringBuilder(); try { new OutputStreamWriter(null); } catch (NullPointerException e) { sb.append("npe "); } try { new OutputStreamWriter(new ByteArrayOutputStream(), "nope"); } catch (UnsupportedEncodingException e) { sb.append(e.getMessage()).append(' '); } try { new OutputStreamWriter(new ByteArrayOutputStream(), (String) null); } catch (NullPointerException e) { sb.append("npe2 "); } try { new OutputStreamWriter(new ByteArrayOutputStream(), (Charset) null); } catch (NullPointerException e) { sb.append("npe3 "); } try { new OutputStreamWriter(new ByteArrayOutputStream(), (CharsetEncoder) null); } catch (NullPointerException e) { sb.append("npe4"); } return sb; });
    io("printstream charset", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(bo, true, "ISO-8859-1"); ps.print("\u00e9"); ps.println("\u20ac"); ps.print('x'); ps.print(12); ps.print(1.5); ps.print(true); ps.print((Object) null); ps.print(new char[] {'a', 'b'}); ps.printf("%s|%d%n", "\u00e9", 5); ps.flush(); return hex(bo.toByteArray()) + " " + ps.charset() + " " + ps.checkError(); });
    io("printstream utf16", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(bo, false, StandardCharsets.UTF_16BE); ps.print("ab"); ps.println(); ps.close(); return hex(bo.toByteArray()) + " " + ps.charset() + " " + ps.checkError(); });
    io("printstream default", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(bo); ps.print("\u20ac"); ps.write(65); ps.write(new byte[] {66, 67}, 0, 2); ps.writeBytes(new byte[] {68}); ps.append('E').append("FG").append("xyz", 1, 2); ps.flush(); return hex(bo.toByteArray()) + " " + ps.charset(); });
    io("printstream errors", () -> { StringBuilder sb = new StringBuilder(); try { new PrintStream((OutputStream) null); } catch (NullPointerException e) { sb.append(e.getMessage()).append(" | "); } try { new PrintStream(new ByteArrayOutputStream(), true, "nope"); } catch (UnsupportedEncodingException e) { sb.append(e.getMessage()).append(" | "); } try { new PrintStream(new ByteArrayOutputStream(), true, (String) null); } catch (NullPointerException e) { sb.append(e.getMessage()).append(" | "); } try { new PrintStream(new ByteArrayOutputStream(), true, (Charset) null); } catch (NullPointerException e) { sb.append(e.getMessage()); } return sb; });
    io("printstream trouble", () -> { PrintStream ps = new PrintStream(new OutputStream() { public void write(int b) throws IOException { throw new IOException("boom"); } }); ps.print("x"); boolean a = ps.checkError(); PrintStream closed = new PrintStream(new ByteArrayOutputStream()); closed.close(); closed.print("y"); return a + " " + closed.checkError(); });
    io("printstream autoflush", () -> { final int[] flushes = {0}; OutputStream o = new ByteArrayOutputStream() { public void flush() { flushes[0]++; } }; PrintStream ps = new PrintStream(o, true); ps.print("abc"); int a = flushes[0]; ps.print("a\nb"); int b = flushes[0]; ps.println(); int c = flushes[0]; ps.write('x'); int d = flushes[0]; ps.write('\n'); return a + " " + b + " " + c + " " + d + " " + flushes[0]; });
    io("bufferedwriter", () -> { StringWriter sw = new StringWriter(); BufferedWriter bw = new BufferedWriter(sw, 4); bw.write("ab"); int a = sw.getBuffer().length(); bw.write("cdef"); int b = sw.getBuffer().length(); bw.write('g'); bw.newLine(); bw.close(); return a + " " + b + " " + sw.toString().replace("\n", "N"); });
    io("bufferedwriter errors", () -> { StringBuilder sb = new StringBuilder(); try { new BufferedWriter(new StringWriter(), 0); } catch (IllegalArgumentException e) { sb.append(e.getMessage()).append(' '); } BufferedWriter bw = new BufferedWriter(new StringWriter()); bw.close(); try { bw.write("x"); } catch (IOException e) { sb.append(e.getMessage()).append(' '); } bw.close(); try { new BufferedWriter(new StringWriter()).write(new char[2], 1, 5); } catch (IndexOutOfBoundsException e) { sb.append("ioobe"); } return sb; });
    io("filerw", () -> {
      Path dir = Files.createTempDirectory("cs");
      try {
        File f = dir.resolve("a.txt").toFile();
        try (Writer w = new FileWriter(f, StandardCharsets.ISO_8859_1)) { w.write("caf\u00e9"); }
        try (Writer w = new FileWriter(f, StandardCharsets.ISO_8859_1, true)) { w.write("!\u20ac"); }
        byte[] raw = Files.readAllBytes(f.toPath());
        String back;
        try (Reader r = new FileReader(f, StandardCharsets.ISO_8859_1)) { char[] c = new char[20]; int n = r.read(c); back = new String(c, 0, n); }
        String utf8;
        try (Reader r = new FileReader(f)) { char[] c = new char[20]; int n = r.read(c); utf8 = new String(c, 0, n); }
        try (Writer w = new FileWriter(f.getPath(), true)) { w.write("Z"); }
        return hex(raw) + " " + esc(back) + "| " + esc(utf8) + "| " + Files.size(f.toPath()) + " " + new FileReader(f).getEncoding();
      } finally { Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    });
    io("filerw errors", () -> { StringBuilder sb = new StringBuilder(); try { new FileReader("/nonexistent/x.txt"); } catch (FileNotFoundException e) { sb.append("fnf "); } try { new FileWriter("/nonexistent/x.txt"); } catch (FileNotFoundException e) { sb.append("fnf2 "); } catch (IOException e) { sb.append("io2 "); } return sb; });
    io("properties encodings", () -> { Properties p = new Properties(); p.load(new ByteArrayInputStream(new byte[] {'k', '=', (byte) 0xe9, '\n', 'u', '=', '\\', 'u', '0', '0', 'e', '9', '\n'})); Properties q = new Properties(); q.load(new InputStreamReader(new ByteArrayInputStream("k=\u00e9\n".getBytes("UTF-8")), "UTF-8")); ByteArrayOutputStream bo = new ByteArrayOutputStream(); q.store(bo, null); String s = bo.toString("ISO-8859-1"); return esc(p.getProperty("k")) + esc(p.getProperty("u")) + esc(q.getProperty("k")) + " " + s.substring(s.indexOf('\n') + 1).replace("\n", "N"); });
    io("scanner charset", () -> { Scanner sc = new Scanner(new ByteArrayInputStream(new byte[] {(byte) 0xe9, ' ', 'x'}), "ISO-8859-1"); String a = sc.next(); String b = sc.next(); return esc(a) + b; });
    io("datainput utf", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); DataOutputStream d = new DataOutputStream(bo); d.writeUTF("a\u00e9\u20ac\ud83d\ude00\u0000"); return hex(bo.toByteArray()) + " " + esc(new DataInputStream(new ByteArrayInputStream(bo.toByteArray())).readUTF()); });
    io("bytearray tostring", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); bo.write("\u00e9".getBytes("UTF-8")); return esc(bo.toString("ISO-8859-1")) + esc(bo.toString(StandardCharsets.UTF_8)) + esc(bo.toString()); });
    io("files strings", () -> { Path dir = Files.createTempDirectory("cs2"); try { Path p = dir.resolve("s.txt"); Files.writeString(p, "caf\u00e9\n\u20ac", StandardCharsets.ISO_8859_1); byte[] raw = Files.readAllBytes(p); List<String> lines = Files.readAllLines(p, StandardCharsets.ISO_8859_1); String s = Files.readString(p, StandardCharsets.ISO_8859_1); Files.write(p, Arrays.asList("\u00e9", "x"), StandardCharsets.UTF_16LE); return hex(raw) + " " + lines.size() + " " + esc(s) + " " + hex(Files.readAllBytes(p)); } finally { Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(q -> q.toFile().delete()); } });
    io("files strict utf8", () -> { Path dir = Files.createTempDirectory("cs3"); try { Path p = dir.resolve("b.bin"); Files.write(p, new byte[] {0x41, (byte) 0xff}); StringBuilder sb = new StringBuilder(); try { Files.readString(p); } catch (CharacterCodingException e) { sb.append(e.getClass().getSimpleName()).append(' '); } try { Files.readAllLines(p); } catch (CharacterCodingException e) { sb.append(e.getClass().getSimpleName()).append(' '); } try { Files.writeString(dir.resolve("c"), "a\ud800", StandardCharsets.UTF_8); sb.append("wrote"); } catch (CharacterCodingException e) { sb.append(e.getClass().getSimpleName()); } return sb; } finally { Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(q -> q.toFile().delete()); } });
  }

  static byte[] toBytes(ByteBuffer bb) {
    byte[] b = new byte[bb.remaining()];
    bb.get(b);
    return b;
  }
}
