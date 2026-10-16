/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 9
 */

import java.util.*;

public class ListsModern {
  interface Action { Object run() throws Throwable; }

  static void probe(String label, Action action) {
    try {
      Object value = action.run();
      System.out.println(label + ":" + (value instanceof Object[] ? Arrays.toString((Object[]) value) : value));
    } catch (Throwable error) {
      boolean generated = error instanceof NullPointerException || error instanceof ArrayStoreException || error instanceof ArrayIndexOutOfBoundsException;
      System.out.println(label + ":" + error.getClass().getName() + (generated ? "" : ":" + error.getMessage()));
    }
  }

  static List<Integer> numbers(int... values) {
    List<Integer> list = new ArrayList<>();
    for (int v : values) list.add(v);
    return list;
  }

  static class ReadOnly extends AbstractList<String> {
    public String get(int index) {
      if (index < 0 || index >= 3) throw new IndexOutOfBoundsException("bad " + index);
      return "e" + index;
    }
    public int size() { return 3; }
  }

  static class Bag extends AbstractCollection<String> {
    final List<String> items = new ArrayList<>(List.of("a", "b", "a", "c", "a"));
    public Iterator<String> iterator() { return items.iterator(); }
    public int size() { return items.size(); }
  }

  public static void main(String[] args) {
    probe("ctor-negative", () -> new ArrayList<Integer>(-1));
    probe("ctor-collection", () -> new ArrayList<>(Set.of(5)));
    probe("add-get", () -> { List<Integer> l = numbers(1, 2, 3); l.add(1, 9); return l + ":" + l.get(1) + ":" + l.size(); });
    probe("add-bad-index", () -> { numbers(1).add(5, 1); return 0; });
    probe("add-negative-index", () -> { numbers(1).add(-1, 1); return 0; });
    probe("get-bad", () -> numbers(1, 2).get(2));
    probe("get-negative", () -> numbers(1, 2).get(-1));
    probe("set-bad", () -> numbers(1, 2).set(2, 0));
    probe("set-old", () -> numbers(1, 2).set(0, 7));
    probe("remove-index", () -> { List<Integer> l = numbers(1, 2, 3); return l.remove(1) + ":" + l; });
    probe("remove-index-bad", () -> numbers(1).remove(1));
    probe("remove-object", () -> { List<Integer> l = numbers(1, 2, 1); return l.remove(Integer.valueOf(1)) + ":" + l + ":" + l.remove(Integer.valueOf(9)); });
    probe("remove-null", () -> { List<Integer> l = new ArrayList<>(); l.add(null); l.add(1); return l.remove(null) + ":" + l; });
    probe("contains-index-of", () -> { List<Integer> l = numbers(4, 5, 4); return l.contains(5) + "," + l.indexOf(4) + "," + l.lastIndexOf(4) + "," + l.indexOf(null) + "," + l.contains(null); });
    probe("add-all-empty", () -> new ArrayList<Integer>().addAll(List.of()));
    probe("add-all-index", () -> { List<Integer> l = numbers(1, 2); l.addAll(1, List.of(8, 9)); return l; });
    probe("add-all-index-bad", () -> numbers(1).addAll(3, List.of(1)));
    probe("add-all-null", () -> numbers(1).addAll(null));
    probe("remove-all-duplicates", () -> { List<Integer> l = numbers(1, 2, 1, 3, 1); boolean r = l.removeAll(List.of(1)); return r + ":" + l; });
    probe("retain-all", () -> { List<Integer> l = numbers(1, 2, 1, 3, 1); boolean r = l.retainAll(List.of(1, 3)); return r + ":" + l; });
    probe("remove-all-none", () -> numbers(1, 2).removeAll(List.of(9)));
    probe("remove-all-null", () -> numbers(1, 2).removeAll(null));
    probe("remove-if", () -> { List<Integer> l = numbers(1, 2, 3, 4, 5, 6); boolean r = l.removeIf(x -> x % 2 == 0); return r + ":" + l; });
    probe("remove-if-none", () -> numbers(1, 3).removeIf(x -> x % 2 == 0));
    probe("remove-if-null", () -> numbers(1).removeIf(null));
    probe("replace-all", () -> { List<Integer> l = numbers(1, 2, 3); l.replaceAll(x -> x * 10); return l; });
    probe("sort", () -> { List<Integer> l = numbers(3, 1, 2); l.sort(null); return l; });
    probe("sort-cmp", () -> { List<Integer> l = numbers(3, 1, 2); l.sort(Comparator.reverseOrder()); return l; });
    probe("sort-stable", () -> { List<String> l = new ArrayList<>(List.of("bb", "a", "cc", "d", "ee", "f")); l.sort(Comparator.comparingInt(String::length)); return l; });
    probe("clear", () -> { List<Integer> l = numbers(1, 2); l.clear(); return l + ":" + l.size() + ":" + l.isEmpty(); });
    probe("to-array", () -> numbers(1, 2, 3).toArray());
    probe("to-array-typed", () -> numbers(1, 2, 3).toArray(new Integer[0]));
    probe("to-array-big", () -> numbers(1, 2).toArray(new Integer[] {9, 9, 9, 9}));
    probe("to-array-exact", () -> { Integer[] a = new Integer[2]; return numbers(1, 2).toArray(a) == a; });
    probe("to-array-bad-type", () -> numbers(1).toArray(new String[0]));
    probe("to-array-generator", () -> numbers(1, 2).toArray(Integer[]::new));
    probe("equals-hash", () -> { List<Integer> a = numbers(1, 2, 3); return a.equals(List.of(1, 2, 3)) + "," + a.equals(List.of(1, 2)) + "," + a.hashCode() + "," + a.equals(null)
        + "," + a.equals(new LinkedList<>(a)) + "," + new ArrayList<>().hashCode(); });
    probe("clone", () -> { ArrayList<Integer> a = new ArrayList<>(numbers(1, 2)); @SuppressWarnings("unchecked") ArrayList<Integer> b = (ArrayList<Integer>) a.clone(); b.add(3); return a + ":" + b; });
    probe("to-string-self", () -> { List<Object> l = new ArrayList<>(); l.add(l); l.add(1); return l; });
    probe("trim-ensure", () -> { ArrayList<Integer> l = new ArrayList<>(100); l.add(1); l.trimToSize(); l.ensureCapacity(500); return l; });
    probe("for-each", () -> { StringBuilder sb = new StringBuilder(); numbers(1, 2, 3).forEach(sb::append); return sb; });
    probe("for-each-null", () -> { numbers(1).forEach(null); return 0; });

    probe("iterator", () -> { StringBuilder sb = new StringBuilder(); for (int x : numbers(1, 2, 3)) sb.append(x); return sb; });
    probe("iterator-remove", () -> { List<Integer> l = numbers(1, 2, 3, 4); Iterator<Integer> it = l.iterator(); while (it.hasNext()) if (it.next() % 2 == 0) it.remove(); return l; });
    probe("iterator-remove-twice", () -> { Iterator<Integer> it = numbers(1, 2).iterator(); it.next(); it.remove(); it.remove(); return 0; });
    probe("iterator-remove-first", () -> { numbers(1).iterator().remove(); return 0; });
    probe("iterator-exhausted", () -> { Iterator<Integer> it = numbers(1).iterator(); it.next(); it.next(); return 0; });
    probe("iterator-cme", () -> { List<Integer> l = numbers(1, 2, 3); for (int x : l) l.add(4); return 0; });
    probe("iterator-cme-remove", () -> { List<Integer> l = numbers(1, 2, 3); for (int x : l) l.remove(0); return l; });
    probe("iterator-no-cme-second-last", () -> { List<Integer> l = numbers(1, 2, 3); for (int x : l) if (x == 2) l.remove(Integer.valueOf(x)); return l; });
    probe("iterator-for-each-remaining", () -> { StringBuilder sb = new StringBuilder(); Iterator<Integer> it = numbers(1, 2, 3).iterator(); it.next(); it.forEachRemaining(sb::append); return sb; });
    probe("list-iterator", () -> { List<Integer> l = numbers(1, 2, 3); ListIterator<Integer> it = l.listIterator(); it.next(); it.add(9); it.next(); it.set(7); return l + ":" + it.nextIndex() + ":" + it.previousIndex(); });
    probe("list-iterator-back", () -> { ListIterator<Integer> it = numbers(1, 2, 3).listIterator(3); StringBuilder sb = new StringBuilder(); while (it.hasPrevious()) sb.append(it.previous()); return sb; });
    probe("list-iterator-bad", () -> numbers(1).listIterator(5));
    probe("list-iterator-set-illegal", () -> { numbers(1).listIterator().set(1); return 0; });
    probe("list-iterator-previous-start", () -> numbers(1).listIterator().previous());
    probe("modify-while-for-each", () -> { ArrayList<Integer> l = new ArrayList<>(numbers(1, 2, 3)); l.forEach(x -> l.add(x)); return 0; });
    probe("modify-in-removeIf", () -> { ArrayList<Integer> l = new ArrayList<>(numbers(1, 2, 3)); l.removeIf(x -> { l.add(9); return false; }); return 0; });
    probe("modify-in-sort", () -> { List<Integer> l = new ArrayList<>(numbers(3, 1, 2)); l.sort((a, b) -> { l.add(0); return a - b; }); return 0; });

    probe("sublist", () -> { List<Integer> l = numbers(1, 2, 3, 4, 5); List<Integer> s = l.subList(1, 4); return s + ":" + s.size() + ":" + s.get(0) + ":" + s.indexOf(4) + ":" + s.contains(5); });
    probe("sublist-write-through", () -> { List<Integer> l = numbers(1, 2, 3, 4, 5); List<Integer> s = l.subList(1, 4); s.set(0, 20); s.add(99); s.remove(Integer.valueOf(3)); return l + ":" + s; });
    probe("sublist-clear", () -> { List<Integer> l = numbers(1, 2, 3, 4, 5); l.subList(1, 3).clear(); return l; });
    probe("sublist-nested", () -> { List<Integer> l = numbers(1, 2, 3, 4, 5, 6); List<Integer> s = l.subList(1, 5).subList(1, 3); s.add(0, 100); return l + ":" + s; });
    probe("sublist-cme", () -> { List<Integer> l = numbers(1, 2, 3, 4); List<Integer> s = l.subList(1, 3); l.add(5); return s.size(); });
    probe("sublist-bad", () -> numbers(1, 2).subList(1, 5));
    probe("sublist-bad-from", () -> numbers(1, 2).subList(-1, 1));
    probe("sublist-reversed-range", () -> numbers(1, 2, 3).subList(2, 1));
    probe("sublist-iterate", () -> { StringBuilder sb = new StringBuilder(); for (int x : numbers(1, 2, 3, 4).subList(1, 3)) sb.append(x); return sb; });
    probe("sublist-add-all", () -> { List<Integer> l = numbers(1, 2, 3); List<Integer> s = l.subList(1, 2); s.addAll(List.of(7, 8)); return l + ":" + s; });
    probe("sublist-sort", () -> { List<Integer> l = numbers(5, 4, 3, 2, 1); l.subList(1, 4).sort(null); return l; });
    probe("sublist-equals", () -> numbers(1, 2, 3).subList(0, 2).equals(List.of(1, 2)) + "," + numbers(1, 2, 3).subList(0, 2).hashCode());
    probe("sublist-random-access", () -> numbers(1, 2, 3).subList(0, 1) instanceof RandomAccess);

    probe("abstract-read-only", () -> { ReadOnly r = new ReadOnly(); return r + ":" + r.size() + ":" + r.contains("e1") + ":" + r.indexOf("e2") + ":" + r.lastIndexOf("e0"); });
    probe("abstract-add", () -> new ReadOnly().add("x"));
    probe("abstract-set", () -> new ReadOnly().set(0, "x"));
    probe("abstract-remove", () -> new ReadOnly().remove(0));
    probe("abstract-clear", () -> { new ReadOnly().clear(); return 0; });
    probe("abstract-iterator", () -> { StringBuilder sb = new StringBuilder(); for (String s : new ReadOnly()) sb.append(s); return sb; });
    probe("abstract-iterator-remove", () -> { Iterator<String> it = new ReadOnly().iterator(); it.next(); it.remove(); return 0; });
    probe("abstract-equals", () -> new ReadOnly().equals(List.of("e0", "e1", "e2")) + "," + new ReadOnly().hashCode() + "," + List.of("e0", "e1", "e2").equals(new ReadOnly()));
    probe("abstract-sublist", () -> new ReadOnly().subList(1, 3));
    probe("abstract-list-iterator", () -> { ListIterator<String> it = new ReadOnly().listIterator(2); return it.previous() + it.previous() + it.nextIndex(); });
    probe("abstract-to-array", () -> new ReadOnly().toArray(new String[5]));

    probe("bag-remove-all", () -> { Bag b = new Bag(); boolean r = b.removeAll(List.of("a")); return r + ":" + b.items; });
    probe("bag-retain-all", () -> { Bag b = new Bag(); boolean r = b.retainAll(List.of("a", "c")); return r + ":" + b.items; });
    probe("bag-remove", () -> { Bag b = new Bag(); boolean r = b.remove("a"); return r + ":" + b.items + ":" + b.remove("zzz") + ":" + b.remove(null); });
    probe("bag-clear", () -> { Bag b = new Bag(); b.clear(); return b.items + ":" + b.isEmpty(); });
    probe("bag-contains-all", () -> new Bag().containsAll(List.of("a", "b")) + "," + new Bag().containsAll(List.of("z")));
    probe("bag-to-string", () -> new Bag().toString());
    probe("bag-to-array", () -> { Bag b = new Bag(); return Arrays.toString(b.toArray()) + Arrays.toString(b.toArray(new String[2])) + Arrays.toString(b.toArray(new String[8])); });
    probe("bag-add", () -> new Bag().add("q"));
    probe("bag-add-all", () -> new Bag().addAll(List.of("q")));
    probe("bag-remove-all-null", () -> new Bag().removeAll(null));
    probe("bag-empty-to-string", () -> new AbstractCollection<String>() {
      public Iterator<String> iterator() { return Collections.emptyIterator(); }
      public int size() { return 0; }
    }.toString());

    probe("linked-list-ops", () -> { LinkedList<Integer> l = new LinkedList<>(numbers(1, 2, 3)); l.addFirst(0); l.removeLast(); return l + ":" + l.getFirst() + ":" + l.peekLast(); });
    probe("linked-removeall", () -> { List<Integer> l = new LinkedList<>(numbers(1, 2, 1, 3, 1)); boolean r = l.removeAll(List.of(1)); return r + ":" + l; });
    probe("vector-ops", () -> { Vector<Integer> v = new Vector<>(numbers(1, 2, 3)); v.add(4); v.removeAll(List.of(2)); return v + ":" + v.size() + ":" + v.firstElement() + ":" + v.indexOf(3); });
    probe("vector-more", () -> { Vector<Integer> v = new Vector<>(5, 2); v.addAll(numbers(1, 2, 3, 4, 5, 6)); return v.capacity() + ":" + v.lastElement() + ":" + v.indexOf(3, 3) + ":" + v.lastIndexOf(3, 5); });
    probe("vector-capacity-growth", () -> { Vector<Integer> v = new Vector<>(); for (int i = 0; i < 11; ++i) v.add(i); return v.capacity(); });
    probe("vector-set-size", () -> { Vector<Integer> v = new Vector<>(numbers(1, 2, 3)); v.setSize(5); v.setSize(2); return v; });
    probe("vector-bad-elementat", () -> new Vector<Integer>().elementAt(0));
    probe("vector-first-empty", () -> new Vector<Integer>().firstElement());
    probe("vector-insert", () -> { Vector<Integer> v = new Vector<>(numbers(1, 3)); v.insertElementAt(2, 1); return v; });
    probe("vector-insert-bad", () -> { new Vector<Integer>().insertElementAt(1, 3); return 0; });
    probe("vector-elements", () -> { Enumeration<Integer> e = new Vector<>(numbers(7, 8)).elements(); return e.nextElement() + ":" + e.hasMoreElements() + ":" + e.nextElement() + ":" + e.hasMoreElements(); });
    probe("vector-enumeration-end", () -> new Vector<Integer>().elements().nextElement());
    probe("vector-cme", () -> { Vector<Integer> v = new Vector<>(numbers(1, 2, 3)); for (int x : v) v.add(4); return 0; });
    probe("vector-remove-if", () -> { Vector<Integer> v = new Vector<>(numbers(1, 2, 3, 4)); v.removeIf(x -> x > 2); return v; });
    probe("vector-sort-clone", () -> { Vector<Integer> v = new Vector<>(numbers(3, 1, 2)); v.sort(null); @SuppressWarnings("unchecked") Vector<Integer> c = (Vector<Integer>) v.clone(); c.add(9); return v + ":" + c; });
    probe("vector-to-array", () -> new Vector<>(numbers(1, 2)).toArray(new Integer[3]));
    probe("vector-sublist", () -> { Vector<Integer> v = new Vector<>(numbers(1, 2, 3, 4)); v.subList(1, 3).clear(); return v; });
    probe("vector-copy-into", () -> { Object[] a = new Object[3]; new Vector<>(numbers(1, 2)).copyInto(a); return Arrays.toString(a); });
    probe("vector-trim", () -> { Vector<Integer> v = new Vector<>(50); v.add(1); v.trimToSize(); return v.capacity(); });
    probe("stack-ops", () -> { Stack<Integer> s = new Stack<>(); s.push(1); s.push(2); s.push(3); return s.peek() + ":" + s.pop() + ":" + s.search(1) + ":" + s.search(9) + ":" + s + ":" + s.empty(); });
    probe("stack-empty-pop", () -> new Stack<Integer>().pop());
    probe("stack-empty-peek", () -> new Stack<Integer>().peek());
    probe("vector-removeall-duplicates", () -> { Vector<Integer> v = new Vector<>(numbers(1, 1, 2)); v.removeAll(List.of(1)); return v; });
  }
}
