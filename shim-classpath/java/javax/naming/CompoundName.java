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
import java.util.Properties;

public class CompoundName implements Name {
  private static final long serialVersionUID = 3513100557083972036L;

  protected transient Properties mySyntax;

  private transient List<String> components = new ArrayList<String>();
  private transient int direction;
  private transient String separator;
  private transient String escape;
  private transient String beginQuote;
  private transient String endQuote;
  private transient String beginQuote2;
  private transient String endQuote2;
  private transient boolean trimBlanks;
  private transient boolean ignoreCase;

  protected CompoundName(Enumeration<String> comps, Properties syntax) {
    if (syntax == null) {
      throw new NullPointerException();
    }
    mySyntax = syntax;
    readSyntax();
    while (comps.hasMoreElements()) {
      components.add(comps.nextElement());
    }
  }

  public CompoundName(String n, Properties syntax) throws InvalidNameException {
    if (syntax == null) {
      throw new NullPointerException();
    }
    mySyntax = syntax;
    readSyntax();
    parse(n);
  }

  private void readSyntax() {
    String dir = mySyntax.getProperty("jndi.syntax.direction", "flat");
    if (dir.equals("left_to_right")) {
      direction = 1;
    } else if (dir.equals("right_to_left")) {
      direction = -1;
    } else {
      direction = 0;
    }
    separator = mySyntax.getProperty("jndi.syntax.separator");
    if (direction != 0 && separator == null) {
      throw new IllegalArgumentException("jndi.syntax.separator property required for non-flat syntax");
    }
    escape = mySyntax.getProperty("jndi.syntax.escape");
    beginQuote = mySyntax.getProperty("jndi.syntax.beginquote");
    endQuote = mySyntax.getProperty("jndi.syntax.endquote", beginQuote);
    beginQuote2 = mySyntax.getProperty("jndi.syntax.beginquote2");
    endQuote2 = mySyntax.getProperty("jndi.syntax.endquote2", beginQuote2);
    trimBlanks = "true".equalsIgnoreCase(mySyntax.getProperty("jndi.syntax.trimblanks", "false"));
    ignoreCase = "true".equalsIgnoreCase(mySyntax.getProperty("jndi.syntax.ignorecase", "false"));
  }

  private void parse(String n) throws InvalidNameException {
    if (direction == 0) {
      components.add(n);
      return;
    }
    List<String> parts = new ArrayList<String>();
    StringBuilder cur = new StringBuilder();
    boolean any = false;
    int i = 0;
    int len = n.length();
    while (i < len) {
      if (escape != null && n.startsWith(escape, i) && i + escape.length() < len) {
        i += escape.length();
        cur.append(n.charAt(i));
        i++;
        any = true;
      } else if (cur.length() == 0 && beginQuote != null && n.startsWith(beginQuote, i)) {
        i = readQuoted(n, i, beginQuote, endQuote, cur);
        any = true;
      } else if (cur.length() == 0 && beginQuote2 != null && n.startsWith(beginQuote2, i)) {
        i = readQuoted(n, i, beginQuote2, endQuote2, cur);
        any = true;
      } else if (n.startsWith(separator, i)) {
        parts.add(finish(cur));
        cur.setLength(0);
        i += separator.length();
        any = true;
        if (i >= len) {
          parts.add("");
          any = false;
        }
      } else {
        cur.append(n.charAt(i));
        i++;
        any = true;
      }
    }
    if (any || len == 0) {
      if (len > 0) {
        parts.add(finish(cur));
      }
    }
    if (direction < 0) {
      for (int k = parts.size() - 1; k >= 0; k--) {
        components.add(parts.get(k));
      }
    } else {
      components.addAll(parts);
    }
  }

  private int readQuoted(String n, int i, String open, String close, StringBuilder out) throws InvalidNameException {
    i += open.length();
    while (true) {
      if (i >= n.length()) {
        throw new InvalidNameException(n + ": no close quote");
      }
      if (n.startsWith(close, i)) {
        return i + close.length();
      }
      if (escape != null && n.startsWith(escape, i) && i + escape.length() < n.length()) {
        i += escape.length();
      }
      out.append(n.charAt(i));
      i++;
    }
  }

  private String finish(StringBuilder cur) {
    String s = cur.toString();
    return trimBlanks ? s.trim() : s;
  }

  private String quote(String comp) {
    if (direction == 0) {
      return comp;
    }
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < comp.length(); ) {
      if (escape != null && comp.startsWith(escape, i)) {
        b.append(escape).append(escape);
        i += escape.length();
      } else if (comp.startsWith(separator, i)) {
        b.append(escape != null ? escape : "").append(separator);
        i += separator.length();
      } else {
        b.append(comp.charAt(i));
        i++;
      }
    }
    return b.toString();
  }

  @Override
  public String toString() {
    if (direction == 0) {
      return components.isEmpty() ? "" : components.get(0);
    }
    StringBuilder b = new StringBuilder();
    int n = components.size();
    for (int k = 0; k < n; k++) {
      String c = direction > 0 ? components.get(k) : components.get(n - 1 - k);
      if (k > 0) {
        b.append(separator);
      }
      b.append(quote(c));
    }
    if (n == 1 && components.get(0).length() == 0) {
      return "";
    }
    return b.toString();
  }

  private boolean sameComp(String a, String b) {
    return ignoreCase ? a.equalsIgnoreCase(b) : a.equals(b);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CompoundName) {
      CompoundName other = (CompoundName) obj;
      return components.size() == other.components.size() && compareTo(obj) == 0;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h = 0;
    for (String c : components) {
      h += ignoreCase ? c.toUpperCase().hashCode() : c.hashCode();
    }
    return h;
  }

  @Override
  public Object clone() {
    return new CompoundName(getAll(), mySyntax);
  }

  public int compareTo(Object obj) {
    if (!(obj instanceof CompoundName)) {
      throw new ClassCastException("Not a CompoundName");
    }
    CompoundName other = (CompoundName) obj;
    int n = Math.min(components.size(), other.components.size());
    for (int i = 0; i < n; i++) {
      String a = components.get(i);
      String b = other.components.get(i);
      int c = ignoreCase ? a.compareToIgnoreCase(b) : a.compareTo(b);
      if (c != 0) {
        return c;
      }
    }
    return components.size() - other.components.size();
  }

  public int size() {
    return components.size();
  }

  public boolean isEmpty() {
    return components.isEmpty();
  }

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

  public String get(int posn) {
    return components.get(posn);
  }

  public Name getPrefix(int posn) {
    if (posn < 0 || posn > size()) {
      throw new ArrayIndexOutOfBoundsException(posn);
    }
    return new CompoundName(java.util.Collections.enumeration(new ArrayList<String>(components.subList(0, posn))),
        mySyntax);
  }

  public Name getSuffix(int posn) {
    if (posn < 0 || posn > size()) {
      throw new ArrayIndexOutOfBoundsException(posn);
    }
    return new CompoundName(
        java.util.Collections.enumeration(new ArrayList<String>(components.subList(posn, size()))), mySyntax);
  }

  public boolean startsWith(Name n) {
    if (!(n instanceof CompoundName) || n.size() > size()) {
      return false;
    }
    CompoundName o = (CompoundName) n;
    for (int i = 0; i < o.size(); i++) {
      if (!sameComp(components.get(i), o.components.get(i))) {
        return false;
      }
    }
    return true;
  }

  public boolean endsWith(Name n) {
    if (!(n instanceof CompoundName) || n.size() > size()) {
      return false;
    }
    CompoundName o = (CompoundName) n;
    int off = size() - o.size();
    for (int i = 0; i < o.size(); i++) {
      if (!sameComp(components.get(off + i), o.components.get(i))) {
        return false;
      }
    }
    return true;
  }

  public Name addAll(Name suffix) throws InvalidNameException {
    return addAll(size(), suffix);
  }

  public Name addAll(int posn, Name n) throws InvalidNameException {
    if (!(n instanceof CompoundName)) {
      throw new InvalidNameException("Not a compound name: " + n.toString());
    }
    List<String> add = new ArrayList<String>();
    for (int i = 0; i < n.size(); i++) {
      add.add(n.get(i));
    }
    components.addAll(posn, add);
    return this;
  }

  public Name add(String comp) throws InvalidNameException {
    components.add(comp);
    return this;
  }

  public Name add(int posn, String comp) throws InvalidNameException {
    components.add(posn, comp);
    return this;
  }

  public Object remove(int posn) throws InvalidNameException {
    return components.remove(posn);
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    s.defaultWriteObject();
    s.writeObject(mySyntax);
    s.writeInt(components.size());
    for (String c : components) {
      s.writeObject(c);
    }
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    mySyntax = (Properties) s.readObject();
    readSyntax();
    components = new ArrayList<String>();
    int n = s.readInt();
    for (int i = 0; i < n; i++) {
      components.add((String) s.readObject());
    }
  }
}
