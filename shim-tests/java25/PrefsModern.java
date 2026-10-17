/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.io.*; import java.nio.file.*; import java.util.*; import java.util.concurrent.*; import java.util.prefs.*;
public class PrefsModern {
  interface Body { Object get() throws Exception; }
  static void p(String n, Body b) { String o; try { o = String.valueOf(b.get()); } catch (Throwable t) { o = "!" + t.getClass().getName() + ":" + t.getMessage(); } System.out.println(n + "=" + o); }
  static String ex(Preferences n, boolean sub) throws Exception { ByteArrayOutputStream bo = new ByteArrayOutputStream(); if (sub) n.exportSubtree(bo); else n.exportNode(bo); return bo.toString("UTF-8"); }
  static void deleteTree(Path root) throws IOException { try (var s = Files.walk(root)) { s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); } }
  public static void main(String[] a) throws Exception {
    Path home = Files.createTempDirectory("prefsmodern");
    Files.createDirectories(home.resolve(".java/.userPrefs"));
    Files.writeString(home.resolve(".java/.userPrefs/prefs.xml"), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<!DOCTYPE map SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">\n<map MAP_XML_VERSION=\"1.0\"/>\n");
    Path sys = Files.createTempDirectory("prefssys");
    Files.createDirectories(sys.resolve(".systemPrefs"));
    Files.writeString(sys.resolve(".systemPrefs/prefs.xml"), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<!DOCTYPE map SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">\n<map MAP_XML_VERSION=\"1.0\"/>\n");
    System.setProperty("java.util.prefs.userRoot", home.toString());
    System.setProperty("java.util.prefs.systemRoot", sys.toString());
    Preferences root = Preferences.userRoot();
    p("root", () -> root + "|" + root.name().isEmpty() + "|" + root.absolutePath() + "|" + root.isUserNode() + "|" + (root.parent() == null));
    p("system", () -> Preferences.systemRoot() + "|" + Preferences.systemRoot().isUserNode());
    Preferences n = root.node("app/sub");
    p("node", () -> n + "|" + n.name() + "|" + n.absolutePath() + "|" + n.parent().absolutePath() + "|" + (root.node("/app/sub") == n) + "|" + (root.node("app").node("sub") == n) + "|" + (n.node("") == n) + "|" + (n.node("/") == root));
    p("pkg", () -> Preferences.userNodeForPackage(PrefsModern.class) + "|" + Preferences.userNodeForPackage(String.class) + "|" + Preferences.systemNodeForPackage(Map.Entry.class));
    p("pkg array", () -> Preferences.userNodeForPackage(int[].class));
    n.put("s", "text"); n.putInt("i", -42); n.putLong("l", 1L << 40); n.putBoolean("b", true); n.putFloat("f", 1.5f); n.putDouble("d", Math.E); n.putByteArray("ba", new byte[]{0, 1, 2, (byte) 255, 127});
    p("values", () -> n.get("s", "def") + "|" + n.getInt("i", 0) + "|" + n.getLong("l", 0) + "|" + n.getBoolean("b", false) + "|" + n.getFloat("f", 0) + "|" + n.getDouble("d", 0) + "|" + Arrays.toString(n.getByteArray("ba", null)));
    p("defaults", () -> n.get("none", "def") + "|" + n.get("none", null) + "|" + n.getInt("none", 7) + "|" + n.getLong("none", 8) + "|" + n.getBoolean("none", true) + "|" + n.getFloat("none", 1f) + "|" + n.getDouble("none", 2d) + "|" + Arrays.toString(n.getByteArray("none", new byte[]{9})));
    p("type mismatch", () -> n.getInt("s", 5) + "|" + n.getLong("s", 6) + "|" + n.getBoolean("s", true) + "|" + n.getFloat("s", 3f) + "|" + n.getDouble("s", 4d) + "|" + n.getByteArray("i", new byte[]{1}).length);
    n.put("tb", "TRUE"); n.put("fb", "False"); n.put("bad", "yes");
    p("booleans", () -> n.getBoolean("tb", false) + "|" + n.getBoolean("fb", true) + "|" + n.getBoolean("bad", true) + "|" + n.getBoolean("bad", false));
    p("keys", () -> { String[] k = n.keys(); Arrays.sort(k); return Arrays.toString(k); });
    p("children", () -> Arrays.toString(root.childrenNames()) + Arrays.toString(root.node("app").childrenNames()) + Arrays.toString(n.childrenNames()));
    p("null key put", () -> { n.put(null, "x"); return "no"; });
    p("null value put", () -> { n.put("k", null); return "no"; });
    p("null key get", () -> n.get(null, "x"));
    p("null key remove", () -> { n.remove(null); return "no"; });
    p("long key", () -> { n.put("k".repeat(81), "x"); return "no"; });
    p("max key", () -> { n.put("k".repeat(80), "x"); return n.get("k".repeat(80), null).length(); });
    p("long value", () -> { n.put("k", "v".repeat(8193)); return "no"; });
    p("max value", () -> { n.put("k", "v".repeat(8192)); return n.get("k", null).length(); });
    p("long bytes", () -> { n.putByteArray("k", new byte[6145]); return "no"; });
    p("max bytes", () -> { n.putByteArray("k", new byte[6144]); return n.getByteArray("k", null).length; });
    p("null bytes", () -> { try { n.putByteArray("k", null); return "no"; } catch (NullPointerException e) { return "NPE"; } });
    p("long name", () -> { root.node("n".repeat(81)); return "no"; });
    p("max name", () -> root.node("n".repeat(80)).name().length());
    p("double slash", () -> root.node("a//b"));
    p("trailing slash", () -> root.node("a/b/"));
    p("abs trailing slash", () -> root.node("/a/b/"));
    p("exists", () -> root.nodeExists("app/sub") + "|" + root.nodeExists("app/none") + "|" + root.nodeExists("/app") + "|" + root.nodeExists("/") + "|" + n.nodeExists("") + "|" + root.nodeExists("none/deeper/still"));
    p("exists bad", () -> root.nodeExists("a//b"));
    p("exists trailing", () -> root.nodeExists("app/"));
    p("remove key", () -> { n.remove("s"); return n.get("s", "gone"); });
    p("export node", () -> ex(n, false));
    Preferences deep = root.node("tree/a b/éü/x_y.z");
    deep.put("quo\"te", "<&>'\n\t"); deep.put("k2", "v2");
    root.node("tree").put("rootval", "1");
    p("export subtree", () -> ex(root.node("tree"), true));
    p("export root", () -> ex(root, true).length() > 100);
    root.flush();
    p("files", () -> { try (var s = Files.walk(home)) { List<String> l = new ArrayList<>(); s.forEach(f -> { String r = home.relativize(f).toString(); if (!r.contains(".lock") && !r.contains("ModFile")) l.add(r); }); Collections.sort(l); return l; } });
    p("prefs file", () -> { try (var s = Files.walk(home)) { StringBuilder sb = new StringBuilder(); List<Path> l = new ArrayList<>(); s.filter(f -> f.getFileName().toString().equals("prefs.xml")).forEach(l::add); Collections.sort(l); for (Path f : l) sb.append(home.relativize(f)).append('\n').append(Files.readString(f)); return sb; } });
    p("import", () -> { String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<!DOCTYPE preferences SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">\n<preferences EXTERNAL_XML_VERSION=\"1.0\"><root type=\"user\"><map><entry key=\"rk\" value=\"rv\"/></map><node name=\"imp\"><map><entry key=\"a\" value=\"b &amp; c\"/></map><node name=\"inner\"><map/></node></node></root></preferences>"; Preferences.importPreferences(new ByteArrayInputStream(xml.getBytes("UTF-8"))); return root.get("rk", null) + "|" + root.node("imp").get("a", null) + "|" + root.nodeExists("imp/inner"); });
    p("import bad version", () -> { String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<!DOCTYPE preferences SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">\n<preferences EXTERNAL_XML_VERSION=\"2.0\"><root type=\"user\"><map/></root></preferences>"; Preferences.importPreferences(new ByteArrayInputStream(xml.getBytes("UTF-8"))); return "no"; });
    p("import garbage", () -> { try { Preferences.importPreferences(new ByteArrayInputStream("not xml".getBytes("UTF-8"))); return "no"; } catch (InvalidPreferencesFormatException e) { return "IPFE"; } });
    p("import roundtrip", () -> { Preferences src = root.node("tree"); String xml = ex(src, true); root.node("tree").removeNode(); Preferences.importPreferences(new ByteArrayInputStream(xml.getBytes("UTF-8"))); return ex(root.node("tree"), true).equals(xml); });
    List<String> events = new CopyOnWriteArrayList<>();
    CountDownLatch latch = new CountDownLatch(7);
    PreferenceChangeListener pcl = e -> { events.add("pref " + e.getNode().absolutePath() + " " + e.getKey() + "=" + e.getNewValue()); latch.countDown(); };
    NodeChangeListener ncl = new NodeChangeListener() {
      public void childAdded(NodeChangeEvent e) { events.add("added " + e.getParent().absolutePath() + " " + e.getChild().name()); latch.countDown(); }
      public void childRemoved(NodeChangeEvent e) { events.add("removed " + e.getParent().absolutePath() + " " + e.getChild().name()); latch.countDown(); }
    };
    Preferences ev = root.node("events");
    ev.addPreferenceChangeListener(pcl); ev.addNodeChangeListener(ncl);
    ev.put("a", "1"); ev.put("a", "2"); ev.remove("a");
    Preferences kid = ev.node("kid"); ev.node("kid2");
    kid.removeNode();
    p("events wait", () -> latch.await(10, TimeUnit.SECONDS));
    p("events", () -> events);
    p("remove listener", () -> { ev.removePreferenceChangeListener(pcl); ev.removePreferenceChangeListener(pcl); return "no"; });
    p("remove node listener", () -> { ev.removeNodeChangeListener(ncl); ev.removeNodeChangeListener(ncl); return "no"; });
    p("null listener", () -> { ev.addPreferenceChangeListener(null); return "no"; });
    p("removed state", () -> { Preferences k = ev.node("tmp"); k.put("x", "y"); k.removeNode(); StringBuilder sb = new StringBuilder(); try { k.get("x", null); } catch (IllegalStateException e) { sb.append(e.getMessage()).append('|'); } try { k.put("x", "y"); } catch (IllegalStateException e) { sb.append(e.getMessage()).append('|'); } try { k.keys(); } catch (IllegalStateException e) { sb.append(e.getMessage()).append('|'); } try { k.node("a"); } catch (IllegalStateException e) { sb.append(e.getMessage()).append('|'); } try { k.removeNode(); } catch (IllegalStateException e) { sb.append(e.getMessage()).append('|'); } try { k.parent(); } catch (IllegalStateException e) { sb.append(e.getMessage()).append('|'); } try { k.childrenNames(); } catch (IllegalStateException e) { sb.append(e.getMessage()).append('|'); } try { k.sync(); } catch (IllegalStateException e) { sb.append(e.getMessage()).append('|'); } try { k.exportNode(new ByteArrayOutputStream()); } catch (IllegalStateException e) { sb.append(e.getMessage()).append('|'); } sb.append(k.nodeExists("")).append('|').append(k.name()).append('|').append(k.absolutePath()); try { k.flush(); sb.append("|flush ok"); } catch (Exception e) { sb.append("|flush ").append(e.getClass().getSimpleName()); } return sb; });
    p("remove root", () -> { root.removeNode(); return "no"; });
    p("clear", () -> { Preferences c = root.node("clr"); c.put("a", "1"); c.put("b", "2"); c.clear(); return c.keys().length; });
    p("sync", () -> { Preferences c = root.node("syn"); c.put("a", "1"); c.sync(); return c.get("a", null) + "|" + Files.exists(home.resolve(".java/.userPrefs/syn/prefs.xml")); });
    p("persistence", () -> { Preferences c = root.node("persist"); c.put("z", "9"); c.flush(); String body = Files.readString(home.resolve(".java/.userPrefs/persist/prefs.xml")); return body.contains("key=\"z\" value=\"9\""); });
    p("remove node files", () -> { Preferences c = root.node("rmfiles/sub"); c.put("a", "b"); c.flush(); Path d = home.resolve(".java/.userPrefs/rmfiles/sub"); boolean before = Files.exists(d); root.node("rmfiles").removeNode(); root.flush(); return before + "|" + Files.exists(home.resolve(".java/.userPrefs/rmfiles")) + "|" + root.nodeExists("rmfiles"); });
    p("existing dir child names", () -> { Files.createDirectories(home.resolve(".java/.userPrefs/predir/childdir")); return Arrays.toString(root.node("predir").childrenNames()) + root.nodeExists("predir/childdir"); });
    p("max constants", () -> Preferences.MAX_KEY_LENGTH + "|" + Preferences.MAX_VALUE_LENGTH + "|" + Preferences.MAX_NAME_LENGTH);
    p("exception types", () -> new BackingStoreException("m").getMessage() + new BackingStoreException(new Exception("c")).getMessage() + new InvalidPreferencesFormatException("x").getMessage() + new InvalidPreferencesFormatException("m", new Exception("c")).getCause().getMessage());
    p("event not serializable", () -> { try { new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(new PreferenceChangeEvent(root, "k", "v")); return "no"; } catch (NotSerializableException e) { return e.getMessage(); } });
    deleteTree(home); deleteTree(sys);
    System.out.flush();
    Runtime.getRuntime().halt(0);
  }
}
