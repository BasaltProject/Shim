/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import jdk.jfr.*; import jdk.jfr.consumer.*; import java.nio.file.*; import java.time.Duration; import java.util.*; import java.util.concurrent.*;
public class JfrRecording {
  @Name("t.Slow") @Label("Slow") @Category("T") @Threshold("20 ms") @StackTrace(false) static class Slow extends Event { String what; int n; }
  @Name("t.Off") @Enabled(false) static class Off extends Event { int x; }
  @Registered(false) @Name("t.Unreg") static class Unreg extends Event { }
  static class Sub extends Slow { @Label("sub") long extra; Class<?> cls; Thread th; transient int skip; static int st; Object ignored; }
  public static void main(String[] a) throws Exception {
    System.out.println(FlightRecorder.isAvailable());
    EventType st = EventType.getEventType(Slow.class); System.out.println(st.getName() + "|" + st.getLabel() + "|" + st.getCategoryNames() + "|" + st.isEnabled());
    for (ValueDescriptor v : st.getFields()) System.out.println("  " + v.getName() + ":" + v.getTypeName() + ":" + v.getLabel() + ":" + v.getContentType());
    for (SettingDescriptor s : st.getSettingDescriptors()) System.out.println("  S " + s.getName() + " " + s.getTypeName() + " " + s.getDefaultValue() + " " + s.getLabel());
    EventType sub = EventType.getEventType(Sub.class); for (ValueDescriptor v : sub.getFields()) System.out.print(v.getName() + " "); System.out.println();
    Recording r = new Recording(); System.out.println(r.getName() + " " + r.getState() + " " + r.isToDisk());
    r.enable(Slow.class).withThreshold(Duration.ofMillis(10)); r.enable("t.Off"); r.enable(Sub.class); System.out.println(r.getSettings().size()); r.start();
    Slow s1 = new Slow(); s1.begin(); Thread.sleep(30); s1.what = "slow"; s1.n = 1; s1.end(); System.out.println("should " + s1.shouldCommit() + " enabled " + s1.isEnabled()); s1.commit();
    Slow s2 = new Slow(); s2.begin(); s2.what = "fast"; s2.end(); System.out.println("should2 " + s2.shouldCommit()); s2.commit();
    Off o = new Off(); o.x = 3; System.out.println("off " + o.isEnabled()); o.commit();
    Sub sb = new Sub(); sb.extra = 9; sb.cls = String.class; sb.th = Thread.currentThread(); sb.what = "sub"; sb.commit();
    Unreg u = new Unreg(); System.out.println("unreg " + u.isEnabled()); FlightRecorder.register(Unreg.class); System.out.println("unreg2 " + u.isEnabled()); u.commit();
    List<AnnotationElement> ann = List.of(new AnnotationElement(Name.class, "dyn.Ev"), new AnnotationElement(Label.class, "Dyn"));
    List<ValueDescriptor> vds = List.of(new ValueDescriptor(String.class, "msg"), new ValueDescriptor(int.class, "cnt", List.of(new AnnotationElement(Label.class, "Count"))));
    EventFactory f = EventFactory.create(ann, vds); f.register(); r.enable("dyn.Ev"); Event de = f.newEvent(); de.set(0, "hello"); de.set(1, 42); de.commit();
    try { de.set(5, 1); } catch (Exception e) { System.out.println(e.getClass().getSimpleName()); }
    r.stop(); System.out.println(r.getState() + " " + (r.getStopTime() != null)); Path p = Files.createTempFile("jfrrec", ".jfr"); r.dump(p);
    List<RecordedEvent> evs = RecordingFile.readAllEvents(p); Map<String, Integer> cnt = new TreeMap<>(); for (RecordedEvent e : evs) cnt.merge(e.getEventType().getName(), 1, Integer::sum); System.out.println(cnt);
    for (RecordedEvent e : evs) { String n = e.getEventType().getName(); if (n.equals("t.Slow")) System.out.println(n + " " + e.getString("what") + " " + e.getInt("n") + " dur>=30ms " + (e.getDuration().toMillis() >= 25) + " stack=" + e.getStackTrace()); if (n.equals("dyn.Ev")) System.out.println(n + " " + e.getString("msg") + " " + e.getInt("cnt") + " " + e.hasField("cnt") + " " + e.hasField("nope")); if (n.endsWith("Sub") || n.equals("F$Sub") || n.contains("Sub")) System.out.println("sub " + e.getLong("extra") + " " + e.getClass("cls").getName() + " " + e.getThread("th").getJavaName() + " " + e.getString("what")); }
    r.close(); System.out.println(r.getState());
    CountDownLatch l = new CountDownLatch(2); List<String> got = Collections.synchronizedList(new ArrayList<>());
    try (RecordingStream rs = new RecordingStream()) { rs.enable("t.Off"); rs.onEvent("t.Off", e -> { got.add("off" + e.getInt("x")); l.countDown(); }); rs.startAsync(); for (int i = 0; i < 2; i++) { Off x = new Off(); x.x = i; x.commit(); } System.out.println(l.await(5, TimeUnit.SECONDS)); System.out.println(got); }
    try (EventStream es = EventStream.openFile(p)) { int[] c = {0}; es.onEvent("t.Slow", e2 -> c[0]++); es.start(); System.out.println("file stream " + c[0]); }
    try (RecordingFile rf = new RecordingFile(p)) { rf.write(p.resolveSibling(p.getFileName() + ".out"), e -> e.getEventType().getName().equals("t.Slow")); } System.out.println(RecordingFile.readAllEvents(p.resolveSibling(p.getFileName() + ".out")).size());
    try (Recording r2 = new Recording(Configuration.getConfiguration("default"))) { r2.start(); r2.stop(); Path q = Files.createTempFile("jfrcfg", ".jfr"); r2.dump(q); Set<String> names = new TreeSet<>(); for (RecordedEvent e : RecordingFile.readAllEvents(q)) names.add(e.getEventType().getName()); System.out.println(names.contains("jdk.JVMInformation") + " " + names.contains("jdk.OSInformation") + " " + names.contains("jdk.InitialSystemProperty")); Files.delete(q); }
    Files.deleteIfExists(p); Files.deleteIfExists(p.resolveSibling(p.getFileName() + ".out"));
  }
}
