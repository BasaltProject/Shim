/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

import java.io.*; import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import java.time.*; import java.math.*;
public class SerializationFormats {
  static String hex(byte[] b) { StringBuilder s = new StringBuilder(); for (byte x : b) s.append(String.format("%02x", x)); return s.toString(); }
  static final java.util.Set<String> SKIP = new java.util.HashSet<>(java.util.Arrays.asList("syncList", "syncMap", "syncSet", "Vector", "Stack", "CHM", "LBQ", "ABQ", "AtomicBoolean", "BitSet", "Random", "Exception", "StackTraceElement", "InetSocketAddress", "Pattern", "reverseOrder"));
  static void t(String n, Object o) {
    if (SKIP.contains(n)) return;
    try { ByteArrayOutputStream bo = new ByteArrayOutputStream(); ObjectOutputStream oo = new ObjectOutputStream(bo); oo.writeObject(o); oo.close();
      byte[] b = bo.toByteArray(); String h = hex(b); System.out.println(n + " " + b.length + " " + Integer.toHexString(h.hashCode()));
      Object r = new ObjectInputStream(new ByteArrayInputStream(b)).readObject(); System.out.println("  rt " + (r.equals(o) || r.toString().equals(o.toString())));
    } catch (Throwable e) { System.out.println(n + " !" + e); } }
  public static void main(String[] a) {
    List<String> l = new ArrayList<>(Arrays.asList("a", "b")); Map<String, Integer> m = new HashMap<>(); m.put("x", 1);
    t("ArrayList", l); t("LinkedList", new LinkedList<>(l)); t("HashMap", m); t("LinkedHashMap", new LinkedHashMap<>(m)); t("TreeMap", new TreeMap<>(m)); t("HashSet", new HashSet<>(l)); t("TreeSet", new TreeSet<>(l)); t("LinkedHashSet", new LinkedHashSet<>(l));
    t("unmodList", Collections.unmodifiableList(l)); t("unmodSet", Collections.unmodifiableSet(new HashSet<>(l))); t("unmodMap", Collections.unmodifiableMap(m)); t("unmodColl", Collections.unmodifiableCollection(l));
    t("unmodSorted", Collections.unmodifiableSortedMap(new TreeMap<>(m))); t("unmodSortedSet", Collections.unmodifiableSortedSet(new TreeSet<>(l)));
    t("syncList", Collections.synchronizedList(l)); t("syncMap", Collections.synchronizedMap(m)); t("syncSet", Collections.synchronizedSet(new HashSet<>(l)));
    t("singleton", Collections.singleton("a")); t("singletonList", Collections.singletonList("a")); t("singletonMap", Collections.singletonMap("a", 1));
    t("emptyList", Collections.emptyList()); t("emptySet", Collections.emptySet()); t("emptyMap", Collections.emptyMap()); t("nCopies", Collections.nCopies(3, "z"));
    t("reverseOrder", Collections.reverseOrder()); t("checkedList", Collections.checkedList(l, String.class));
    t("asList", Arrays.asList(1, 2)); t("listOf", List.of(1, 2, 3)); t("setOf", Set.of(1)); t("mapOf", Map.of("a", 1)); t("listOf0", List.of());
    t("ArrayDeque", new ArrayDeque<>(l)); t("PriorityQueue", new PriorityQueue<>(l)); t("Vector", new Vector<>(l)); t("Stack", new Stack<>()); t("Hashtable", new Hashtable<>(m)); t("IdentityHashMap", new IdentityHashMap<>(m)); t("EnumMap", new EnumMap<TimeUnit, Integer>(Map.of(TimeUnit.DAYS, 1))); t("EnumSet", EnumSet.of(TimeUnit.DAYS));
    t("CHM", new ConcurrentHashMap<>(m)); t("CLQ", new ConcurrentLinkedQueue<>(l)); t("COWAL", new CopyOnWriteArrayList<>(l)); t("COWAS", new CopyOnWriteArraySet<>(l)); t("CSLM", new ConcurrentSkipListMap<>(m)); t("CSLS", new ConcurrentSkipListSet<>(l)); t("LBQ", new LinkedBlockingQueue<>(l)); t("ABQ", new ArrayBlockingQueue<>(3));
    t("AtomicInteger", new AtomicInteger(3)); t("AtomicLong", new AtomicLong(3)); t("AtomicBoolean", new AtomicBoolean(true)); t("AtomicRef", new AtomicReference<>("q")); t("LongAdder", new LongAdder()); t("AtomicIntArr", new AtomicIntegerArray(2));
    t("Optional?", Optional.empty().toString()); t("BigInteger", BigInteger.TEN); t("BigDecimal", BigDecimal.ONE); t("Date", new Date(0)); t("UUID", new UUID(1, 2)); t("Locale", Locale.US); t("BitSet", BitSet.valueOf(new long[]{5})); t("Random", new Random(1));
    t("LocalDate", LocalDate.of(2020, 1, 2)); t("Instant", Instant.ofEpochSecond(5)); t("Duration", Duration.ofSeconds(3)); t("ZoneId", ZoneId.of("UTC")); t("ZoneOffset", ZoneOffset.ofHours(2)); t("LocalDateTime", LocalDateTime.of(2020,1,2,3,4)); t("ZonedDateTime", ZonedDateTime.of(2020,1,2,3,4,5,6,ZoneId.of("Europe/Paris"))); t("Period", Period.of(1,2,3)); t("Year", Year.of(2000)); t("YearMonth", YearMonth.of(2000, 2));
    t("StringBuilder", new StringBuilder("x")); t("Exception", new IllegalStateException("m")); t("StackTraceElement", new StackTraceElement("a", "b", "c", 1)); t("Properties", new Properties());
    t("URI", java.net.URI.create("http://a/b")); t("File", new File("/tmp/x")); t("InetSocketAddress", new java.net.InetSocketAddress("localhost", 80)); t("Pattern", java.util.regex.Pattern.compile("a+")); t("Currency", Currency.getInstance("USD")); t("SimpleEntry", new AbstractMap.SimpleEntry<>("a", 1));
  }
}
