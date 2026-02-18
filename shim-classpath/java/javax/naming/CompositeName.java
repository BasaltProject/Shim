/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

public class CompositeName implements Name {
  private static final long serialVersionUID = 1667768148915813118L;

  private transient List<String> components = new ArrayList<String>();

  public CompositeName() { }

  protected CompositeName(Enumeration<String> comps) {
    while (comps.hasMoreElements()) {
      components.add(comps.nextElement());
    }
  }

  public CompositeName(String n) throws InvalidNameException {
    parse(n);
  }

  private void parse(String n) throws InvalidNameException {
    if (n.isEmpty()) {
      return;
    }
    if (n.equals("/")) {
      components.add("");
      return;
    }
    int i = 0;
    int len = n.length();
    StringBuilder cur = new StringBuilder();
    boolean any = false;
    while (i < len) {
      char c = n.charAt(i);
      if (c == '\\' && i + 1 < len) {
        cur.append(n.charAt(i + 1));
        i += 2;
        continue;
      }
      if (c == '"' || c == '\'') {
        int end = n.indexOf(c, i + 1);
        if (end < 0) {
          throw new InvalidNameException(n + ": no close quote");
        }
        cur.append(n, i + 1, end);
        i = end + 1;
        if (i < len && n.charAt(i) != '/') {
          throw new InvalidNameException(n + ": close quote appears before end of component");
        }
        continue;
      }
      if (c == '/') {
        components.add(cur.toString());
        cur.setLength(0);
        i++;
        any = true;
        continue;
      }
      cur.append(c);
      i++;
    }
    components.add(cur.toString());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int n = components.size();
    for (int i = 0; i < n; i++) {
      String comp = components.get(i);
      if (i > 0) {
        sb.append('/');
      } else if (n == 1 && comp.isEmpty()) {
        return "/";
      }
      if (comp.indexOf('/') >= 0) {
        sb.append('"').append(comp).append('"');
      } else {
        sb.append(comp);
      }
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj != null && obj instanceof CompositeName && components.equals(((CompositeName) obj).components));
  }

  @Override
  public int hashCode() {
    int h = 0;
    for (String s : components) {
      h += s.hashCode();
    }
    return h;
  }

  @Override
  public int compareTo(Object obj) {
    if (!(obj instanceof CompositeName)) {
      throw new ClassCastException("Not a CompositeName");
    }
    CompositeName other = (CompositeName) obj;
    int n = Math.min(components.size(), other.components.size());
    for (int i = 0; i < n; i++) {
      int c = components.get(i).compareTo(other.components.get(i));
      if (c != 0) {
        return c;
      }
    }
    return components.size() - other.components.size();
  }

  @Override
  public Object clone() {
    CompositeName c = new CompositeName();
    c.components = new ArrayList<String>(components);
    return c;
  }

  @Override
  public int size() {
    return components.size();
  }

  @Override
  public boolean isEmpty() {
    return components.isEmpty();
  }

  @Override
  public Enumeration<String> getAll() {
    final java.util.Iterator<String> it = new ArrayList<String>(components).iterator();
    return new Enumeration<String>() {
      public boolean hasMoreElements() {
        return it.hasNext();
      }

      public String nextElement() {
        if (!it.hasNext()) {
          throw new NoSuchElementException();
        }
        return it.next();
      }
    };
  }

  @Override
  public String get(int posn) {
    return components.get(posn);
  }

  @Override
  public Name getPrefix(int posn) {
    if (posn < 0 || posn > size()) {
      throw new ArrayIndexOutOfBoundsException(posn);
    }
    CompositeName c = new CompositeName();
    c.components = new ArrayList<String>(components.subList(0, posn));
    return c;
  }

  @Override
  public Name getSuffix(int posn) {
    if (posn < 0 || posn > size()) {
      throw new ArrayIndexOutOfBoundsException(posn);
    }
    CompositeName c = new CompositeName();
    c.components = new ArrayList<String>(components.subList(posn, size()));
    return c;
  }

  @Override
  public boolean startsWith(Name n) {
    if (!(n instanceof CompositeName)) {
      return false;
    }
    List<String> o = ((CompositeName) n).components;
    return o.size() <= components.size() && components.subList(0, o.size()).equals(o);
  }

  @Override
  public boolean endsWith(Name n) {
    if (!(n instanceof CompositeName)) {
      return false;
    }
    List<String> o = ((CompositeName) n).components;
    return o.size() <= components.size() && components.subList(components.size() - o.size(), components.size()).equals(o);
  }

  @Override
  public Name addAll(Name suffix) throws InvalidNameException {
    if (suffix instanceof CompositeName) {
      components.addAll(((CompositeName) suffix).components);
      return this;
    }
    throw new InvalidNameException("Not a composite name: " + suffix.toString());
  }

  @Override
  public Name addAll(int posn, Name n) throws InvalidNameException {
    if (n instanceof CompositeName) {
      components.addAll(posn, ((CompositeName) n).components);
      return this;
    }
    throw new InvalidNameException("Not a composite name: " + n.toString());
  }

  @Override
  public Name add(String comp) throws InvalidNameException {
    components.add(comp);
    return this;
  }

  @Override
  public Name add(int posn, String comp) throws InvalidNameException {
    components.add(posn, comp);
    return this;
  }

  @Override
  public Object remove(int posn) throws InvalidNameException {
    return components.remove(posn);
  }
}
