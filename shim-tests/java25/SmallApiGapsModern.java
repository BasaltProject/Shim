/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.random.RandomGenerator;
import java.util.stream.*;
import java.util.zip.*;
import java.util.jar.*;

public class SmallApiGapsModern {
  enum E { A, B }
  public static class Pub { public static class In1 {} private static class In2 {} public interface In3 {} }
  public static class Sub extends Pub { public static class In4 {} }
  interface Br<T> { void f(T t); }
  static class Bi implements Br<String> { public void f(String s) {} }
  public E field;
  public int plain;
  static void t(String n, Callable<Object> c) { try { System.out.println(n + ": " + c.call()); } catch (Throwable e) { System.out.println(n + ": !" + e.getClass().getName() + ":" + (e instanceof NullPointerException ? null : e.getMessage())); } }
  @SuppressWarnings("removal")
  public static void main(String[] a) throws Exception {
    t("byteValueOf", () -> Byte.valueOf("12") + " " + Byte.valueOf("-7f", 16) + " " + Byte.valueOf("0x10".substring(2), 16));
    t("byteValueOfBad", () -> Byte.valueOf("300"));
    t("byteValueOfNull", () -> Byte.valueOf((String) null));
    t("byteDecode", () -> Byte.decode("0x7f") + " " + Byte.decode("-010") + " " + Byte.decode("#7"));
    t("byteDecodeBad", () -> Byte.decode("0x80"));
    t("byteDecodeBad2", () -> Byte.decode("abc"));
    t("byteCtor", () -> new Byte("5") + " " + new Short("6"));
    t("shortValueOf", () -> Short.valueOf("1234") + " " + Short.valueOf("-7fff", 16));
    t("shortValueOfBad", () -> Short.valueOf("40000"));
    t("shortDecode", () -> Short.decode("0x7fff") + " " + Short.decode("-0"));
    t("shortDecodeBad", () -> Short.decode("0x8000"));
    t("floatCtor", () -> new Float(1.1) + " " + new Float(1e40) + " " + new Float((double) Float.MIN_VALUE / 2));
    t("assert0", () -> new AssertionError().getMessage() + " " + new AssertionError().getCause());
    t("assertObj", () -> new AssertionError("x").getMessage() + " " + new AssertionError((Object) null).getMessage() + " " + new AssertionError(5).getMessage() + " " + new AssertionError('c').getMessage() + " " + new AssertionError(1.5f).getMessage() + " " + new AssertionError(true).getMessage() + " " + new AssertionError(2L).getMessage());
    t("assertThrowable", () -> { Throwable th = new RuntimeException("boom"); AssertionError e = new AssertionError(th); return e.getMessage() + " | " + e.getCause().getMessage(); });
    t("assertCause", () -> { AssertionError e = new AssertionError("m", new RuntimeException("c")); return e.getMessage() + " " + e.getCause().getMessage(); });
    t("assertCauseNull", () -> { AssertionError e = new AssertionError("m", null); return e.getMessage() + " " + e.getCause(); });
    t("classSynth", () -> SmallApiGapsModern.class.isSynthetic() + " " + Runnable.class.isSynthetic() );
    t("signers", () -> String.class.getSigners() + " " + SmallApiGapsModern.class.getSigners());
    t("classes", () -> Arrays.stream(Sub.class.getClasses()).map(Class::getSimpleName).sorted().collect(Collectors.toList()) + " " + Arrays.stream(Pub.class.getClasses()).map(Class::getSimpleName).sorted().collect(Collectors.toList()) + " " + Arrays.toString(int.class.getClasses()) + " " + Arrays.toString(Runnable.class.getClasses()));
    t("forNameModule", () -> Class.forName(SmallApiGapsModern.class.getModule(), "SmallApiGapsModern") + " " + Class.forName(SmallApiGapsModern.class.getModule(), "NoSuch") + " " + Class.forName(Object.class.getModule(), "java.lang.String"));
    t("forNameModuleNull", () -> Class.forName((Module) null, "X"));
    t("forNameModuleNull2", () -> Class.forName(SmallApiGapsModern.class.getModule(), null));
    t("resources", () -> ClassLoader.getSystemClassLoader().resources("no/such/resource.txt").count());
    t("resourcesNull", () -> ClassLoader.getSystemClassLoader().resources(null));
    t("native", () -> SmallApiGapsModern.class.getModule().isNativeAccessEnabled() + " " + Object.class.getModule().isNativeAccessEnabled());
    t("pkgCompat", () -> { Package p = Object.class.getPackage(); try { return p.isCompatibleWith("1.0"); } catch (Exception e) { return e.getClass().getName() + e.getMessage(); } });
    t("pkgGet", () -> { Object.class.getPackage(); return Package.getPackage("java.lang") + " " + Package.getPackage("no.such") + " " + (Package.getPackages().length > 0); });
    t("pkgGetNull", () -> Package.getPackage(null));
    t("waitForDur", () -> { Process p = new ProcessBuilder("true").start(); return p.waitFor(java.time.Duration.ofSeconds(5)); });
    t("waitForDurNull", () -> { Process p = new ProcessBuilder("true").start(); p.waitFor(); return p.waitFor((java.time.Duration) null); });
    t("runFin", () -> { Runtime.getRuntime().runFinalization(); System.runFinalization(); return "ok"; });
    t("removeHook", () -> { Thread h = new Thread(() -> {}); Runtime.getRuntime().addShutdownHook(h); boolean r = Runtime.getRuntime().removeShutdownHook(h); boolean r2 = Runtime.getRuntime().removeShutdownHook(h); return r + " " + r2; });
    t("removeHookNull", () -> Runtime.getRuntime().removeShutdownHook(null));
    t("utf32", () -> java.nio.charset.StandardCharsets.UTF_32 + " " + java.nio.charset.StandardCharsets.UTF_32BE + " " + java.nio.charset.StandardCharsets.UTF_32LE);
    t("utf32enc", () -> Arrays.toString("a😀".getBytes(java.nio.charset.StandardCharsets.UTF_32BE)) + Arrays.toString("a".getBytes(java.nio.charset.StandardCharsets.UTF_32LE)) + Arrays.toString("a".getBytes(java.nio.charset.StandardCharsets.UTF_32)) + new String(new byte[] {0, 0, 0, 0x61}, java.nio.charset.StandardCharsets.UTF_32));
    t("pathResolve", () -> Path.of("/a").resolve("b", "c", "d") + " " + Path.of("/a").resolve(Path.of("b"), Path.of("c")) + " " + Path.of("a").resolve("b") + " " + Path.of("/a").resolve("b", new String[0]));
    t("pathResolveNull", () -> Path.of("/a").resolve("b", (String[]) null));
    t("pathResolveNull2", () -> Path.of("/a").resolve("b", new String[] {null}));
    t("instantUntil", () -> Instant.ofEpochSecond(5).until(Instant.ofEpochSecond(8, 500)) + " " + Instant.ofEpochSecond(5).until(Instant.ofEpochSecond(2)));
    t("instantUntilNull", () -> Instant.EPOCH.until(null));
    t("bitsetBB", () -> BitSet.valueOf(ByteBuffer.wrap(new byte[] {0x15, 0x2, 3})) + " " + BitSet.valueOf(LongBuffer.wrap(new long[] {5, 1})));
    t("bitsetBBpos", () -> { ByteBuffer bb = ByteBuffer.wrap(new byte[] {1, 2, 3, 4}); bb.position(1); BitSet bs = BitSet.valueOf(bb); return bs + " " + bb.position(); });
    t("bitsetLBpos", () -> { LongBuffer lb = LongBuffer.wrap(new long[] {1, 2, 3, 0}); lb.position(1).limit(3); BitSet bs = BitSet.valueOf(lb); return bs + " " + lb.position(); });
    t("bitsetBBnull", () -> BitSet.valueOf((ByteBuffer) null));
    t("currency", () -> Currency.availableCurrencies().count() + " " + Currency.availableCurrencies().anyMatch(c -> c.getCurrencyCode().equals("EUR")));
    t("enumIt", () -> { Iterator<String> it = Collections.enumeration(List.of("a", "b")).asIterator(); StringBuilder sb = new StringBuilder(); while (it.hasNext()) sb.append(it.next()); try { it.remove(); } catch (Exception e) { sb.append(e.getClass().getName()); } return sb; });
    t("hex", () -> HexFormat.of().toHexDigits(255L, 4) + " " + HexFormat.of().toHexDigits(0x1234L, 16) + " " + HexFormat.of().withUpperCase().toHexDigits(255L, 2) + " " + HexFormat.of().toHexDigits(new StringBuilder("x"), (byte) 0x7f));
    t("hexBad", () -> HexFormat.of().toHexDigits(1L, 17));
    t("hexBad2", () -> HexFormat.of().toHexDigits(1L, -1));
    t("hexZero", () -> "[" + HexFormat.of().toHexDigits(255L, 0) + "]");
    t("hexNeg", () -> HexFormat.of().toHexDigits(-1L, 16) + HexFormat.of().toHexDigits(-1L, 3));
    t("ihm", () -> { Map<String, Integer> m = new HashMap<>(); m.put("a", 1); IdentityHashMap<String, Integer> i = new IdentityHashMap<>(m); return i.size() + " " + i.get("a"); });
    t("ihmNull", () -> new IdentityHashMap<String, String>((Map<String, String>) null));
    t("objIdent", () -> { Object o = new Object(); return Objects.toIdentityString(o).equals("java.lang.Object@" + Integer.toHexString(System.identityHashCode(o))) + " " + Objects.toIdentityString("s").startsWith("java.lang.String@"); });
    t("objIdentNull", () -> Objects.toIdentityString(null));
    t("obsDelete", () -> { Observable o = new Observable(); o.addObserver((x, y) -> {}); o.addObserver((x, y) -> {}); int c1 = o.countObservers(); o.deleteObservers(); return c1 + " " + o.countObservers(); });
    t("tzIds", () -> TimeZone.availableIDs().count() > 100 && TimeZone.availableIDs().anyMatch("UTC"::equals));
    t("tzIdsOff", () -> TimeZone.availableIDs(0).anyMatch("UTC"::equals) + " " + TimeZone.availableIDs(3600000).anyMatch("Europe/Paris"::equals));
    t("weak", () -> { WeakHashMap<String, Integer> w = WeakHashMap.newWeakHashMap(10); w.put("k", 1); Map<String, Integer> m = new HashMap<>(); m.put("x", 2); WeakHashMap<String, Integer> w2 = new WeakHashMap<>(m); WeakHashMap<String, Integer> w3 = new WeakHashMap<>(4, 0.5f); w3.put("z", 3); return w + " " + w2 + " " + w3; });
    t("weakBad", () -> WeakHashMap.newWeakHashMap(-1));
    t("weakBad2", () -> new WeakHashMap<String, String>(-1, 0.5f));
    t("weakBad3", () -> new WeakHashMap<String, String>(1, 0f));
    t("callablePriv", () -> Executors.callable((java.security.PrivilegedExceptionAction<String>) () -> "pea").call());
    t("callablePrivCL", () -> Executors.privilegedCallableUsingCurrentClassLoader(() -> "pcl").call());
    t("blocker", () -> { LockSupport.setCurrentBlocker(null); return "ok"; });
    t("rlQueued", () -> { ReentrantLock l = new ReentrantLock(); return l.hasQueuedThread(Thread.currentThread()) + " " + l.hasQueuedThreads(); });
    t("rlQueuedNull", () -> new ReentrantLock().hasQueuedThread(null));
    t("zipName", () -> { Path p = Files.createTempFile("zf", ".zip"); try (ZipOutputStream z = new ZipOutputStream(Files.newOutputStream(p))) { z.setComment("zc"); z.putNextEntry(new ZipEntry("a")); } try (ZipFile zf = new ZipFile(p.toFile())) { return zf.getName().equals(p.toString()) + " " + zf.getComment() + " " + zf.size(); } finally { Files.delete(p); } });
    t("zipNoComment", () -> { Path p = Files.createTempFile("zf", ".zip"); try (ZipOutputStream z = new ZipOutputStream(Files.newOutputStream(p))) { z.putNextEntry(new ZipEntry("a")); } try (ZipFile zf = new ZipFile(p.toFile())) { return zf.getComment(); } finally { Files.delete(p); } });
    t("jarVersioned", () -> { Path p = Files.createTempFile("jf", ".jar"); try (JarOutputStream z = new JarOutputStream(Files.newOutputStream(p))) { z.putNextEntry(new JarEntry("a.txt")); z.write(1); z.putNextEntry(new JarEntry("META-INF/versions/9/a.txt")); z.write(2); z.putNextEntry(new JarEntry("b.txt")); } try (JarFile jf = new JarFile(p.toFile())) { return jf.versionedStream().map(JarEntry::getName).sorted().collect(Collectors.toList()); } finally { Files.delete(p); } });
    t("equi", () -> { RandomGenerator g = new Random(42); return g.equiDoubles(0.0, 1.0, true, false).limit(3).count(); });
    t("dfsLocales", () -> java.text.DateFormatSymbols.getAvailableLocales().length > 0);
    t("bridge", () -> { for (Method m : Bi.class.getDeclaredMethods()) if (m.getName().equals("f") && m.getParameterTypes()[0] == Object.class) return m.isBridge() + " " + m.isSynthetic(); return "none"; });
    t("bridge2", () -> Bi.class.getMethod("f", String.class).isBridge());
    t("enumConst", () -> SmallApiGapsModern.class.getField("field").isEnumConstant() + " " + E.class.getField("A").isEnumConstant() + " " + SmallApiGapsModern.class.getField("plain").isEnumConstant());
    t("prevLookup", () -> MethodHandles.lookup().previousLookupClass() + " " + MethodHandles.lookup().in(String.class).previousLookupClass());
  }
}
