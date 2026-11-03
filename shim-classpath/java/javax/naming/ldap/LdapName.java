/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.ldap;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.naming.InvalidNameException;
import javax.naming.Name;

public class LdapName implements Name {
  private static final long serialVersionUID = -1595520034788997356L;

  private transient List<Rdn> rdns;
  private transient String unparsed;

  public LdapName(String name) throws InvalidNameException {
    unparsed = name;
    rdns = new ArrayList<Rdn>();
    parse(name);
  }

  public LdapName(List<Rdn> rdns) {
    this.rdns = new ArrayList<Rdn>(rdns.size());
    for (Rdn r : rdns) {
      if (!(r instanceof Rdn)) {
        throw new IllegalArgumentException("Entry:" + r + "  not a valid type;" + "list entries must be of type Rdn");
      }
      this.rdns.add(r);
    }
  }

  private void parse(String name) throws InvalidNameException {
    List<Rdn> parsed = new ArrayList<Rdn>();
    int n = name.length();
    int start = 0;
    boolean quoted = false;
    int i = 0;
    boolean any = false;
    while (i <= n) {
      char c = i < n ? name.charAt(i) : ',';
      if (i < n && c == '\\') {
        i += 2;
        continue;
      }
      if (i < n && c == '"') {
        quoted = !quoted;
        i++;
        continue;
      }
      if ((c == ',' || c == ';') && !quoted) {
        String part = name.substring(start, Math.min(i, n));
        if (part.trim().length() == 0) {
          if (i < n || any) {
            throw new InvalidNameException("Invalid name: " + name);
          }
        } else {
          try {
            parsed.add(0, new Rdn(part));
          } catch (InvalidNameException e) {
            throw new InvalidNameException("Invalid name: " + name);
          }
          any = true;
        }
        start = i + 1;
      }
      i++;
    }
    if (quoted) {
      throw new InvalidNameException("Invalid name: " + name);
    }
    if (name.trim().length() > 0 && parsed.isEmpty()) {
      throw new InvalidNameException("Invalid name: " + name);
    }
    rdns = parsed;
  }

  public int size() {
    return rdns.size();
  }

  public boolean isEmpty() {
    return rdns.isEmpty();
  }

  public Enumeration<String> getAll() {
    final Iterator<Rdn> it = new ArrayList<Rdn>(rdns).iterator();
    return new Enumeration<String>() {
      public boolean hasMoreElements() {
        return it.hasNext();
      }

      public String nextElement() {
        if (!it.hasNext()) {
          throw new NoSuchElementException();
        }
        return it.next().toString();
      }
    };
  }

  public String get(int posn) {
    return rdns.get(posn).toString();
  }

  public Rdn getRdn(int posn) {
    return rdns.get(posn);
  }

  public Name getPrefix(int posn) {
    if (posn < 0 || posn > rdns.size()) {
      throw new IndexOutOfBoundsException("Posn: " + posn + ", Size: " + rdns.size());
    }
    return new LdapName(new ArrayList<Rdn>(rdns.subList(0, posn)));
  }

  public Name getSuffix(int posn) {
    if (posn < 0 || posn > rdns.size()) {
      throw new IndexOutOfBoundsException("Posn: " + posn + ", Size: " + rdns.size());
    }
    return new LdapName(new ArrayList<Rdn>(rdns.subList(posn, rdns.size())));
  }

  public boolean startsWith(Name n) {
    if (n == null) {
      return false;
    }
    int len1 = rdns.size();
    int len2 = n.size();
    return len1 >= len2 && matches(0, n);
  }

  public boolean startsWith(List<Rdn> rdns) {
    if (rdns == null) {
      return false;
    }
    int len2 = rdns.size();
    if (this.rdns.size() < len2) {
      return false;
    }
    for (int i = 0; i < len2; i++) {
      if (!this.rdns.get(i).equals(rdns.get(i))) {
        return false;
      }
    }
    return true;
  }

  public boolean endsWith(Name n) {
    if (n == null) {
      return false;
    }
    int len1 = rdns.size();
    int len2 = n.size();
    return len1 >= len2 && matches(len1 - len2, n);
  }

  public boolean endsWith(List<Rdn> rdns) {
    if (rdns == null) {
      return false;
    }
    int len1 = this.rdns.size();
    int len2 = rdns.size();
    if (len1 < len2) {
      return false;
    }
    for (int i = 0; i < len2; i++) {
      if (!this.rdns.get(len1 - len2 + i).equals(rdns.get(i))) {
        return false;
      }
    }
    return true;
  }

  private boolean matches(int off, Name n) {
    List<Rdn> other;
    if (n instanceof LdapName) {
      other = ((LdapName) n).rdns;
    } else {
      other = new ArrayList<Rdn>();
      try {
        for (int i = 0; i < n.size(); i++) {
          other.add(new Rdn(n.get(i)));
        }
      } catch (InvalidNameException e) {
        return false;
      }
    }
    for (int i = 0; i < other.size(); i++) {
      if (!rdns.get(off + i).equals(other.get(i))) {
        return false;
      }
    }
    return true;
  }

  public Name addAll(Name suffix) throws InvalidNameException {
    return addAll(size(), suffix);
  }

  public Name addAll(List<Rdn> suffixRdns) {
    return addAll(size(), suffixRdns);
  }

  public Name addAll(int posn, Name suffix) throws InvalidNameException {
    unparsed = null;
    List<Rdn> add = new ArrayList<Rdn>();
    if (suffix instanceof LdapName) {
      add.addAll(((LdapName) suffix).rdns);
    } else {
      for (int i = 0; i < suffix.size(); i++) {
        add.add(new Rdn(suffix.get(i)));
      }
    }
    rdns.addAll(posn, add);
    return this;
  }

  public Name addAll(int posn, List<Rdn> suffixRdns) {
    unparsed = null;
    for (int i = 0; i < suffixRdns.size(); i++) {
      if (!(suffixRdns.get(i) instanceof Rdn)) {
        throw new IllegalArgumentException("Entry:" + suffixRdns.get(i) + "  not a valid type;"
            + "suffix list entries must be of type Rdn");
      }
    }
    rdns.addAll(posn, suffixRdns);
    return this;
  }

  public Name add(String comp) throws InvalidNameException {
    return add(size(), comp);
  }

  public Name add(Rdn comp) {
    return add(size(), comp);
  }

  public Name add(int posn, String comp) throws InvalidNameException {
    Rdn rdn = new Rdn(comp);
    rdns.add(posn, rdn);
    unparsed = null;
    return this;
  }

  public Name add(int posn, Rdn comp) {
    if (comp == null) {
      throw new NullPointerException("Cannot add null RDN");
    }
    rdns.add(posn, comp);
    unparsed = null;
    return this;
  }

  public Object remove(int posn) throws InvalidNameException {
    unparsed = null;
    return rdns.remove(posn).toString();
  }

  public List<Rdn> getRdns() {
    return java.util.Collections.unmodifiableList(new ArrayList<Rdn>(rdns));
  }

  @Override
  public Object clone() {
    return new LdapName(rdns);
  }

  @Override
  public String toString() {
    if (unparsed != null) {
      return unparsed;
    }
    StringBuilder b = new StringBuilder();
    for (int i = rdns.size() - 1; i >= 0; i--) {
      if (b.length() > 0) {
        b.append(',');
      }
      b.append(rdns.get(i));
    }
    return unparsed = b.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof LdapName)) {
      return false;
    }
    LdapName other = (LdapName) obj;
    return rdns.size() == other.rdns.size() && compareTo(other) == 0;
  }

  public int compareTo(Object obj) {
    if (!(obj instanceof LdapName)) {
      throw new ClassCastException("The obj is not a LdapName");
    }
    if (obj == this) {
      return 0;
    }
    LdapName other = (LdapName) obj;
    int n = Math.min(rdns.size(), other.rdns.size());
    for (int i = 0; i < n; i++) {
      int c = rdns.get(i).compareTo(other.rdns.get(i));
      if (c != 0) {
        return c;
      }
    }
    return rdns.size() - other.rdns.size();
  }

  @Override
  public int hashCode() {
    int h = 0;
    for (Rdn r : rdns) {
      h += r.hashCode();
    }
    return h;
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    s.defaultWriteObject();
    s.writeObject(toString());
    s.writeInt(rdns.size());
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    unparsed = (String) s.readObject();
    s.readInt();
    rdns = new ArrayList<Rdn>();
    try {
      parse(unparsed);
    } catch (InvalidNameException e) {
      throw new java.io.InvalidObjectException(e.getMessage());
    }
  }
}
