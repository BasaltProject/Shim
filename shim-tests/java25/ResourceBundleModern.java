/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
public class ResourceBundleModern {
  static void p(String n, java.util.concurrent.Callable<Object> c) { try { System.out.println(n + "=" + c.call()); } catch (Throwable t) { System.out.println(n + "=!" + t.getClass().getName() + ":" + (t instanceof NullPointerException ? "npe" : t.getMessage())); } }
  public static class ListB extends ListResourceBundle { protected Object[][] getContents() { return new Object[][] {{"k1", "root-k1"}, {"k2", "root-k2"}, {"arr", new String[] {"a", "b"}}, {"num", 42}}; } }
  public static class ListB_de extends ListResourceBundle { protected Object[][] getContents() { return new Object[][] {{"k1", "de-k1"}, {"k3", "de-k3"}}; } }
  public static class ListB_de_DE extends ListResourceBundle { protected Object[][] getContents() { return new Object[][] {{"k1", "de_DE-k1"}}; } }
  public static class BadList extends ListResourceBundle { protected Object[][] getContents() { return new Object[][] {{"k", null}}; } }
  public static class NullKey extends ListResourceBundle { protected Object[][] getContents() { return new Object[][] {{null, "v"}}; } }
  public static class NotPublic extends ListResourceBundle { protected Object[][] getContents() { return new Object[][] {{"k", "v"}}; } NotPublic() {} }
  public static void main(String[] a) throws Exception {
    Locale.setDefault(Locale.US);
    Path dir = Files.createTempDirectory("rbm");
    Files.write(dir.resolve("msgs.properties"), "greeting=Hello\nfarewell=Bye\nuni=caf\\u00e9\nutf=café\nmulti=a\\\n  b\n".getBytes(StandardCharsets.UTF_8));
    Files.write(dir.resolve("msgs_es.properties"), "greeting=Hola\nonlyes=solo\n".getBytes(StandardCharsets.UTF_8));
    Files.write(dir.resolve("msgs_es_ES.properties"), "greeting=Hola ES\n".getBytes(StandardCharsets.UTF_8));
    Files.write(dir.resolve("msgs_fr.properties"), "greeting=Bonjour\n".getBytes(StandardCharsets.UTF_8));
    Files.write(dir.resolve("only_fr.properties"), "k=fr-only\n".getBytes(StandardCharsets.UTF_8));
    Files.write(dir.resolve("only_fr_FR.properties"), "k=fr-FR-only\n".getBytes(StandardCharsets.UTF_8));
    Files.write(dir.resolve("latin.properties"), new byte[] {'k', '=', (byte) 0xe9, '\n'});
    Files.write(dir.resolve("sub.properties"), "a=1\n".getBytes());
    ClassLoader cl = new URLClassLoader(new URL[] {dir.toUri().toURL()}, null);
    ClassLoader self = ResourceBundleModern.class.getClassLoader();
    String L = "ResourceBundleModern$ListB";
    p("list-root", () -> { ResourceBundle b = ResourceBundle.getBundle(L, Locale.ROOT, self); return b.getString("k1") + " " + b.getLocale() + " " + b.getBaseBundleName() + " " + b.getClass().getName() + " " + b.keySet() .stream().sorted().toList() + " " + b.containsKey("k2") + " " + b.containsKey("zz"); });
    p("list-de", () -> { ResourceBundle b = ResourceBundle.getBundle(L, Locale.GERMAN, self); return b.getString("k1") + " " + b.getString("k2") + " " + b.getString("k3") + " " + b.getLocale() + " " + new TreeSet<>(b.keySet()); });
    p("list-de-DE", () -> { ResourceBundle b = ResourceBundle.getBundle(L, Locale.GERMANY, self); return b.getString("k1") + " " + b.getString("k3") + " " + b.getLocale() + " " + Collections.list(b.getKeys()).stream().sorted().toList(); });
    p("list-fr-fallback-default", () -> { Locale old = Locale.getDefault(); Locale.setDefault(Locale.GERMAN); try { ResourceBundle b = ResourceBundle.getBundle(L, Locale.FRENCH, self); return b.getString("k1") + " " + b.getLocale(); } finally { Locale.setDefault(old); } });
    p("list-fr-nofallback", () -> { Locale old = Locale.getDefault(); Locale.setDefault(Locale.US); try { ResourceBundle b = ResourceBundle.getBundle(L, Locale.FRENCH, self); return b.getString("k1") + " [" + b.getLocale() + "]"; } finally { Locale.setDefault(old); } });
    p("list-array", () -> Arrays.toString(ResourceBundle.getBundle(L, Locale.ROOT, self).getStringArray("arr")));
    p("list-object", () -> ResourceBundle.getBundle(L, Locale.ROOT, self).getObject("num"));
    p("list-wrongtype", () -> { try { return ResourceBundle.getBundle(L, Locale.ROOT, self).getString("num"); } catch (ClassCastException e) { return "cce"; } });
    p("list-missing", () -> ResourceBundle.getBundle(L, Locale.ROOT, self).getString("zz"));
    p("list-null-key-arg", () -> ResourceBundle.getBundle(L, Locale.ROOT, self).getString(null));
    p("list-null-value", () -> ResourceBundle.getBundle("ResourceBundleModern$BadList", Locale.ROOT, self).getString("k"));
    p("list-null-key", () -> ResourceBundle.getBundle("ResourceBundleModern$NullKey", Locale.ROOT, self).getString("k"));
    p("missing-bundle", () -> ResourceBundle.getBundle("no.such.Bundle", Locale.US, self));
    p("missing-bundle-root", () -> ResourceBundle.getBundle("no.such.Bundle", Locale.ROOT, self));
    p("missing-exc", () -> { try { ResourceBundle.getBundle("no.such.Bundle", Locale.FRANCE, self); return "none"; } catch (MissingResourceException e) { return e.getClassName() + "|" + e.getKey() + "|" + e.getMessage(); } });
    p("missing-key-exc", () -> { try { ResourceBundle.getBundle(L, Locale.ROOT, self).getString("zz"); return "none"; } catch (MissingResourceException e) { return e.getClassName() + "|" + e.getKey() + "|" + e.getMessage(); } });
    p("props-root", () -> { ResourceBundle b = ResourceBundle.getBundle("msgs", Locale.ROOT, cl); return b.getString("greeting") + " " + b.getString("uni") + " " + b.getString("utf") + " " + b.getString("multi") + " " + b.getLocale() + " " + b.getClass().getName() + " " + new TreeSet<>(b.keySet()); });
    p("props-es", () -> { ResourceBundle b = ResourceBundle.getBundle("msgs", new Locale("es"), cl); return b.getString("greeting") + " " + b.getString("farewell") + " " + b.getString("onlyes") + " " + b.getLocale() + " " + new TreeSet<>(b.keySet()) + " " + b.getBaseBundleName(); });
    p("props-es-ES", () -> { ResourceBundle b = ResourceBundle.getBundle("msgs", new Locale("es", "ES"), cl); return b.getString("greeting") + " " + b.getString("onlyes") + " " + b.getLocale(); });
    p("props-es-MX", () -> { ResourceBundle b = ResourceBundle.getBundle("msgs", new Locale("es", "MX"), cl); return b.getString("greeting") + " " + b.getLocale(); });
    p("props-fr-default-es", () -> { Locale old = Locale.getDefault(); Locale.setDefault(new Locale("es")); try { ResourceBundle b = ResourceBundle.getBundle("only", new Locale("de"), cl); return b.getString("k"); } catch (MissingResourceException e) { return "missing " + e.getMessage(); } finally { Locale.setDefault(old); } });
    p("props-fallback-fr", () -> { Locale old = Locale.getDefault(); Locale.setDefault(Locale.FRANCE); try { ResourceBundle b = ResourceBundle.getBundle("only", new Locale("de"), cl); return b.getString("k") + " " + b.getLocale(); } catch (MissingResourceException e) { return "missing " + e.getMessage(); } finally { Locale.setDefault(old); } });
    p("props-latin1", () -> ResourceBundle.getBundle("latin", Locale.ROOT, cl).getString("k"));
    p("props-cache", () -> { ResourceBundle b1 = ResourceBundle.getBundle("msgs", Locale.ROOT, cl); ResourceBundle b2 = ResourceBundle.getBundle("msgs", Locale.ROOT, cl); ResourceBundle.clearCache(cl); ResourceBundle b3 = ResourceBundle.getBundle("msgs", Locale.ROOT, cl); return (b1 == b2) + " " + (b1 == b3); });
    p("props-cache-list", () -> { ResourceBundle b1 = ResourceBundle.getBundle(L, Locale.ROOT, self); ResourceBundle b2 = ResourceBundle.getBundle(L, Locale.ROOT, self); ResourceBundle.clearCache(); ResourceBundle b3 = ResourceBundle.getBundle(L, Locale.ROOT, self); return (b1 == b2) + " " + (b1 == b3); });
    p("control-props-only", () -> { ResourceBundle b = ResourceBundle.getBundle("msgs", new Locale("es"), cl, ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES)); return b.getString("greeting"); });
    p("control-class-only", () -> { ResourceBundle b = ResourceBundle.getBundle("msgs", new Locale("es"), cl, ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_CLASS)); return b.getString("greeting"); });
    p("control-nofallback", () -> { Locale old = Locale.getDefault(); Locale.setDefault(Locale.FRANCE); try { return ResourceBundle.getBundle("only", new Locale("de"), cl, ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT)); } catch (MissingResourceException e) { return "missing " + e.getMessage(); } finally { Locale.setDefault(old); } });
    p("control-formats", () -> ResourceBundle.Control.FORMAT_DEFAULT + " " + ResourceBundle.Control.FORMAT_CLASS + " " + ResourceBundle.Control.FORMAT_PROPERTIES + " " + ResourceBundle.Control.TTL_DONT_CACHE + " " + ResourceBundle.Control.TTL_NO_EXPIRATION_CONTROL);
    p("control-candidates", () -> { ResourceBundle.Control c = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT); return c.getCandidateLocales("x", new Locale("es", "ES", "WIN")) + " " + c.getCandidateLocales("x", Locale.ROOT) + " " + c.getCandidateLocales("x", Locale.forLanguageTag("zh-Hant-TW")) + " " + c.getCandidateLocales("x", Locale.forLanguageTag("zh-Hans-CN")) + " " + c.getCandidateLocales("x", Locale.forLanguageTag("sr-Latn-RS")) + " " + c.getCandidateLocales("x", new Locale("no", "NO", "NY")) + " " + c.getCandidateLocales("x", Locale.forLanguageTag("zh-Hant")) + " " + c.getCandidateLocales("x", Locale.forLanguageTag("en-u-ca-gregory")); });
    p("control-names", () -> { ResourceBundle.Control c = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT); return c.toBundleName("a.b.msgs", Locale.ROOT) + " " + c.toBundleName("a.b.msgs", new Locale("es")) + " " + c.toBundleName("a.b.msgs", new Locale("es", "ES")) + " " + c.toBundleName("a.b.msgs", new Locale("", "ES")) + " " + c.toBundleName("a.b.msgs", new Locale("es", "", "WIN")) + " " + c.toBundleName("a.b.msgs", new Locale("", "", "WIN")) + " " + c.toBundleName("a.b.msgs", Locale.forLanguageTag("zh-Hant-TW")) + " " + c.toBundleName("a.b.msgs", Locale.forLanguageTag("sr-Latn")) + " " + c.toResourceName("a.b.msgs_es", "properties") + " " + c.toResourceName("a$b", "class"); });
    p("control-fallback", () -> { ResourceBundle.Control c = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT); Locale old = Locale.getDefault(); Locale.setDefault(Locale.FRANCE); try { return c.getFallbackLocale("x", Locale.GERMANY) + " " + c.getFallbackLocale("x", Locale.FRANCE) + " " + ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT).getFallbackLocale("x", Locale.GERMANY); } finally { Locale.setDefault(old); } });
    p("control-formats-list", () -> { ResourceBundle.Control c = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT); return c.getFormats("x") + " " + ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_CLASS).getFormats("x") + " " + ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES).getFormats("x") + " " + c.getTimeToLive("x", Locale.US); });
    p("control-bad-formats", () -> ResourceBundle.Control.getControl(List.of("java.class", "x")));
    p("control-null-formats", () -> ResourceBundle.Control.getControl(null));
    p("control-sameinstance", () -> (ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT) == ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT)) + " " + (ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES) == ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)));
    p("control-newbundle", () -> { ResourceBundle.Control c = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT); ResourceBundle b = c.newBundle("msgs", new Locale("es"), "java.properties", cl, false); ResourceBundle none = c.newBundle("msgs", new Locale("zz"), "java.properties", cl, false); ResourceBundle lb = c.newBundle(L, Locale.GERMAN, "java.class", self, false); return b.getString("greeting") + " " + none + " " + lb.getString("k1"); });
    p("control-newbundle-bad-format", () -> ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT).newBundle("msgs", Locale.ROOT, "java.xml", cl, false));
    p("control-newbundle-nulls", () -> ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT).newBundle(null, Locale.ROOT, "java.class", cl, false));
    p("custom-control", () -> { ResourceBundle.Control c = new ResourceBundle.Control() { public List<String> getFormats(String n) { return FORMAT_PROPERTIES; } public long getTimeToLive(String n, Locale l) { return TTL_DONT_CACHE; } public Locale getFallbackLocale(String n, Locale l) { return null; } public List<Locale> getCandidateLocales(String n, Locale l) { return List.of(l, Locale.ROOT); } }; ResourceBundle b1 = ResourceBundle.getBundle("msgs", new Locale("es"), cl, c); ResourceBundle b2 = ResourceBundle.getBundle("msgs", new Locale("es"), cl, c); return b1.getString("greeting") + " " + (b1 == b2); });
    p("custom-control-ttl", () -> { ResourceBundle.Control c = new ResourceBundle.Control() { public long getTimeToLive(String n, Locale l) { return TTL_NO_EXPIRATION_CONTROL; } }; ResourceBundle b1 = ResourceBundle.getBundle("msgs", Locale.ROOT, cl, c); ResourceBundle b2 = ResourceBundle.getBundle("msgs", Locale.ROOT, cl, c); return (b1 == b2); });
    p("setparent", () -> { ResourceBundle child = ResourceBundle.getBundle(L, Locale.GERMAN, self); ResourceBundle other = ResourceBundle.getBundle("msgs", Locale.ROOT, cl); return child.getString("k2"); });
    p("handleKeySet", () -> { ResourceBundle b = new ResourceBundle() { protected Object handleGetObject(String k) { return k.equals("a") ? "A" : null; } public Enumeration<String> getKeys() { return Collections.enumeration(List.of("a", "b")); } }; return b.containsKey("a") + " " + b.containsKey("b") + " " + new TreeSet<>(b.keySet()) + " " + b.getLocale() + " " + b.getBaseBundleName(); });
    p("parent-chain", () -> { ResourceBundle parent = new ListResourceBundle() { protected Object[][] getContents() { return new Object[][] {{"p", "pv"}}; } }; ListResourceBundle child = new ListResourceBundle() { { setParent(parent); } protected Object[][] getContents() { return new Object[][] {{"c", "cv"}}; } }; return child.getString("p") + " " + child.getString("c") + " " + new TreeSet<>(child.keySet()) + " " + child.containsKey("p") + " " + Collections.list(child.getKeys()).stream().sorted().toList(); });
    p("prb-reader", () -> { PropertyResourceBundle b = new PropertyResourceBundle(new StringReader("a=1\nb=2\n")); return b.getString("a") + " " + new TreeSet<>(b.keySet()) + " " + Collections.list(b.getKeys()).stream().sorted().toList() + " " + b.getObject("b"); });
    p("prb-stream-utf8", () -> new PropertyResourceBundle(new ByteArrayInputStream("k=café\n".getBytes(StandardCharsets.UTF_8))).getString("k"));
    p("prb-stream-latin1", () -> new PropertyResourceBundle(new ByteArrayInputStream(new byte[] {'k', '=', (byte) 0xe9, '\n'})).getString("k").codePointAt(0));
    p("prb-null", () -> new PropertyResourceBundle((InputStream) null));
    p("prb-null-reader", () -> new PropertyResourceBundle((Reader) null));
    p("prb-parent", () -> { PropertyResourceBundle b = new PropertyResourceBundle(new StringReader("a=1\n")); PropertyResourceBundle c = new PropertyResourceBundle(new StringReader("c=3\n")); return b.getString("a"); });
    p("module", () -> ResourceBundle.getBundle("no.such.bundle", ResourceBundleModern.class.getModule()));
    p("module-locale", () -> ResourceBundle.getBundle("no.such.bundle", Locale.US, ResourceBundleModern.class.getModule()));
    p("default-loader", () -> ResourceBundle.getBundle(L).getString("k1"));
    p("default-loader-locale", () -> ResourceBundle.getBundle(L, Locale.GERMAN).getString("k1"));
    p("getBundle-null", () -> ResourceBundle.getBundle(null));
    p("getBundle-null-locale", () -> ResourceBundle.getBundle(L, (Locale) null));
    p("keys-tostring", () -> { ResourceBundle b = ResourceBundle.getBundle(L, Locale.GERMAN, self); return b.getClass().getName() + " " + (b.getLocale().equals(Locale.GERMAN)); });
    p("not-public", () -> ResourceBundle.getBundle("ResourceBundleModern$NotPublic", Locale.ROOT, self));
    p("not-resourcebundle", () -> ResourceBundle.getBundle("java.lang.String", Locale.ROOT, self));
  }
}
