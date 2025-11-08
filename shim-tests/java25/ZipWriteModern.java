/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.*;

public class ZipWriteModern {
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  static String hex(byte[] b) { StringBuilder sb = new StringBuilder(); for (byte x : b) sb.append(String.format("%02x", x)); return sb.toString(); }
  static interface Body { void run(ZipOutputStream z) throws Exception; }
  static String zip(Body body) throws Exception {
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    ZipOutputStream z = new ZipOutputStream(bo);
    body.run(z);
    z.close();
    return bo.size() + " " + hex(bo.toByteArray());
  }
  static ZipEntry entry(String n, long time) { ZipEntry e = new ZipEntry(n); e.setTime(time); return e; }
  static final long T = 1700000000000L;
  public static void main(String[] a) throws Exception {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    t("empty", () -> zip(z -> { }));
    t("one", () -> zip(z -> { z.putNextEntry(entry("a.txt", T)); z.write("hello hello hello".getBytes()); z.closeEntry(); }));
    t("two", () -> zip(z -> { z.putNextEntry(entry("a.txt", T)); z.write(1); z.write(2); z.putNextEntry(entry("dir/", T + 3000)); z.putNextEntry(entry("é.bin", T + 1001)); z.write(new byte[500]); }));
    t("level0", () -> zip(z -> { z.setLevel(0); z.putNextEntry(entry("a", T)); z.write(new byte[50]); }));
    t("level9", () -> zip(z -> { z.setLevel(9); z.putNextEntry(entry("a", T)); z.write("abcabcabcabcabcabcabc".getBytes()); }));
    t("comment", () -> zip(z -> { z.setComment("zip comment"); z.putNextEntry(entry("a", T)); ZipEntry e = entry("b", T); e.setComment("entry comment"); z.putNextEntry(e); }));
    t("stored", () -> zip(z -> { ZipEntry e = entry("s", T); e.setMethod(ZipEntry.STORED); byte[] d = "stored data".getBytes(); CRC32 c = new CRC32(); c.update(d); e.setSize(d.length); e.setCrc(c.getValue()); z.putNextEntry(e); z.write(d); }));
    t("storedMissing", () -> zip(z -> { ZipEntry e = entry("s", T); e.setMethod(ZipEntry.STORED); z.putNextEntry(e); }));
    t("storedMismatch", () -> zip(z -> { ZipEntry e = entry("s", T); e.setMethod(ZipEntry.STORED); e.setSize(3); e.setCompressedSize(4); e.setCrc(0); z.putNextEntry(e); }));
    t("storedPast", () -> zip(z -> { ZipEntry e = entry("s", T); e.setMethod(ZipEntry.STORED); e.setSize(1); e.setCrc(0); z.putNextEntry(e); z.write(new byte[2]); }));
    t("storedShort", () -> zip(z -> { ZipEntry e = entry("s", T); e.setMethod(ZipEntry.STORED); e.setSize(2); e.setCrc(0); z.putNextEntry(e); z.write(new byte[1]); z.closeEntry(); }));
    t("storedBadCrc", () -> zip(z -> { ZipEntry e = entry("s", T); e.setMethod(ZipEntry.STORED); e.setSize(1); e.setCrc(12345); z.putNextEntry(e); z.write(new byte[1]); z.closeEntry(); }));
    t("setMethodStored", () -> zip(z -> { z.setMethod(ZipOutputStream.STORED); ZipEntry e = entry("s", T); e.setSize(1); e.setCrc(0xd202ef8dL); z.putNextEntry(e); z.write(0); }));
    t("deflPre", () -> zip(z -> { ZipEntry e = entry("d", T); byte[] d = new byte[40]; CRC32 c = new CRC32(); c.update(d); Deflater df = new Deflater(-1, true); df.setInput(d); df.finish(); byte[] o = new byte[100]; int n = df.deflate(o); e.setSize(40); e.setCompressedSize(n); e.setCrc(c.getValue()); z.putNextEntry(e); z.write(d); }));
    t("deflPreBad", () -> zip(z -> { ZipEntry e = entry("d", T); e.setSize(40); e.setCompressedSize(3); e.setCrc(0); z.putNextEntry(e); z.write(new byte[40]); z.closeEntry(); }));
    t("dup", () -> zip(z -> { z.putNextEntry(entry("x", T)); z.putNextEntry(entry("x", T)); }));
    t("noEntry", () -> zip(z -> z.write(1)));
    t("noTime", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); ZipOutputStream z = new ZipOutputStream(bo); ZipEntry e = new ZipEntry("n"); z.putNextEntry(e); return e.getTime() > 1600000000000L && e.getMethod() == 8; });
    t("mtimeOut", () -> zip(z -> { ZipEntry e = new ZipEntry("m"); e.setLastModifiedTime(FileTime.fromMillis(4102444800000L)); z.putNextEntry(e); }));
    t("mtimeIn", () -> zip(z -> { ZipEntry e = new ZipEntry("m"); e.setLastModifiedTime(FileTime.fromMillis(T)); z.putNextEntry(e); }));
    t("atimeCtime", () -> zip(z -> { ZipEntry e = entry("m", T); e.setLastAccessTime(FileTime.fromMillis(T + 5000)); e.setCreationTime(FileTime.fromMillis(T + 9000)); z.putNextEntry(e); }));
    t("ntfs", () -> zip(z -> { ZipEntry e = entry("m", T); e.setLastModifiedTime(FileTime.fromMillis(T)); e.setLastAccessTime(FileTime.fromMillis(4102444800000L)); z.putNextEntry(e); }));
    t("old", () -> zip(z -> { ZipEntry e = new ZipEntry("o"); e.setTime(100000000000L); z.putNextEntry(e); }));
    t("extra", () -> zip(z -> { ZipEntry e = entry("x", T); e.setExtra(new byte[] {(byte) 0xca, (byte) 0xfe, 2, 0, 7, 8}); z.putNextEntry(e); }));
    t("extraBad", () -> zip(z -> { ZipEntry e = entry("x", T); e.setExtra(new byte[70000]); }));
    t("charset", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); ZipOutputStream z = new ZipOutputStream(bo, StandardCharsets.ISO_8859_1); z.putNextEntry(entry("é", T)); z.close(); return hex(bo.toByteArray()); });
    t("closed", () -> { ZipOutputStream z = new ZipOutputStream(new ByteArrayOutputStream()); z.close(); z.putNextEntry(entry("a", T)); return 1; });
    t("closedWrite", () -> { ZipOutputStream z = new ZipOutputStream(new ByteArrayOutputStream()); z.close(); z.write(1); return 1; });
    t("finishTwice", () -> { ZipOutputStream z = new ZipOutputStream(new ByteArrayOutputStream()); z.putNextEntry(entry("a", T)); z.finish(); z.finish(); z.close(); z.close(); return 1; });
    t("nullCharset", () -> new ZipOutputStream(new ByteArrayOutputStream(), null));
    t("nullName", () -> new ZipEntry((String) null));
    t("longName", () -> new ZipEntry("x".repeat(70000)));
    t("longComment", () -> { new ZipEntry("x").setComment("y".repeat(70000)); return 1; });
    t("entryDefaults", () -> { ZipEntry e = new ZipEntry("d"); return e.getTime() + " " + e.getSize() + " " + e.getCompressedSize() + " " + e.getCrc() + " " + e.getMethod() + " " + e.getExtra() + " " + e.getComment() + " " + e.getTimeLocal(); });
    t("entryCopy", () -> { ZipEntry e = entry("d", T); e.setComment("c"); e.setSize(5); ZipEntry c = new ZipEntry(e); ZipEntry cl = (ZipEntry) e.clone(); return c.getTime() + " " + c.getSize() + " " + c.getComment() + " " + cl.getName() + " " + e.hashCode() + " " + e; });
    t("badValues", () -> { ZipEntry e = new ZipEntry("d"); try { e.setSize(-1); } catch (Exception x) { System.out.println(x.getMessage()); } try { e.setCrc(-1); } catch (Exception x) { System.out.println(x.getMessage()); } try { e.setCrc(1L << 32); } catch (Exception x) { System.out.println(x.getMessage()); } try { e.setMethod(5); } catch (Exception x) { System.out.println(x.getMessage()); } e.setCompressedSize(-5); return e.getCompressedSize(); });
    t("roundtrip", () -> { ByteArrayOutputStream bo = new ByteArrayOutputStream(); ZipOutputStream z = new ZipOutputStream(bo); ZipEntry e = entry("t.txt", T + 1001); e.setLastAccessTime(FileTime.fromMillis(T)); e.setExtra(new byte[] {(byte) 0xca, (byte) 0xfe, 1, 0, 9}); z.putNextEntry(e); z.write("abc".getBytes()); z.putNextEntry(entry("u/", T)); z.close(); ZipInputStream zi = new ZipInputStream(new ByteArrayInputStream(bo.toByteArray())); StringBuilder sb = new StringBuilder(); ZipEntry r; while ((r = zi.getNextEntry()) != null) { sb.append(r.getName()).append(' ').append(r.getTime()).append(' ').append(r.getMethod()).append(' ').append(r.getLastAccessTime()).append(' ').append(r.getLastModifiedTime()).append(' ').append(hex(r.getExtra() == null ? new byte[0] : r.getExtra())).append(" | "); zi.readAllBytes(); sb.append(r.getSize()).append(' ').append(r.getCompressedSize()).append(' ').append(r.getCrc()).append("; "); } return sb; });
  }
}
