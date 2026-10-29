/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE.
 * SPDX-License-Identifier: MIT
 */
import java.util.*;

public class UnmodifiableListCursorModern {
  static int checks;
  static void expect(boolean condition) {
    if (!condition) throw new AssertionError("Cursor check " + checks);
    ++checks;
  }
  static void immutable(Runnable action) {
    try { action.run(); throw new AssertionError("Mutation accepted"); }
    catch (UnsupportedOperationException expected) { ++checks; }
  }
  static void check(List<String> list) {
    for (int start = 0; start <= list.size(); ++start) {
      ListIterator<String> cursor = list.listIterator(start);
      expect(cursor.nextIndex() == start); expect(cursor.previousIndex() == start - 1);
      for (int at = start; at < list.size(); ++at) {
        expect(cursor.hasNext()); expect(cursor.next().equals(list.get(at)));
        expect(cursor.nextIndex() == at + 1); expect(cursor.previousIndex() == at);
      }
      expect(!cursor.hasNext());
      for (int at = list.size() - 1; at >= 0; --at) {
        expect(cursor.hasPrevious()); expect(cursor.previous().equals(list.get(at)));
        expect(cursor.nextIndex() == at); expect(cursor.previousIndex() == at - 1);
      }
      expect(!cursor.hasPrevious());
    }
    ListIterator<String> cursor = list.listIterator();
    immutable(() -> cursor.add("forbidden")); immutable(() -> cursor.remove());
    immutable(() -> cursor.set("forbidden"));
    if (cursor.hasNext()) {
      cursor.next(); immutable(() -> cursor.remove()); immutable(() -> cursor.set("forbidden"));
      immutable(() -> cursor.add("forbidden"));
    }
  }
  static String dropTrailingEmpty(List<String> list) {
    ListIterator<String> cursor = list.listIterator(list.size());
    while (cursor.hasPrevious()) {
      if (!cursor.previous().isEmpty()) return String.join(".", list.subList(0, cursor.nextIndex() + 1));
    }
    return "";
  }
  public static void main(String[] args) {
    for (List<String> values : List.of(List.<String>of(), List.of("one"), List.of("one", "two", ""))) {
      check(Collections.unmodifiableList(new ArrayList<>(values)));
      check(Collections.unmodifiableList(new LinkedList<>(values)));
      check(Collections.unmodifiableList(Collections.unmodifiableList(new ArrayList<>(values))));
      check(List.copyOf(values));
    }
    expect(dropTrailingEmpty(Collections.unmodifiableList(List.of("locale"))).equals("locale"));
    expect(dropTrailingEmpty(Collections.unmodifiableList(List.of("core", "locale", "", ""))).equals("core.locale"));
    expect(dropTrailingEmpty(Collections.unmodifiableList(List.of("", ""))).isEmpty());
    System.out.println("Unmodifiable cursor checks=" + checks);
  }
}
