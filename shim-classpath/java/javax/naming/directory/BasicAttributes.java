/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.directory;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class BasicAttributes implements Attributes {
  private static final long serialVersionUID = 4980164073184639448L;

  private boolean ignoreCase = false;
  transient Hashtable<String, Attribute> attrs = new Hashtable<String, Attribute>(11);

  public BasicAttributes() {
  }

  public BasicAttributes(boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  public BasicAttributes(String attrID, Object val) {
    this();
    this.put(new BasicAttribute(attrID, val));
  }

  public BasicAttributes(String attrID, Object val, boolean ignoreCase) {
    this(ignoreCase);
    this.put(new BasicAttribute(attrID, val));
  }

  public Object clone() {
    BasicAttributes attrset;
    try {
      attrset = (BasicAttributes) super.clone();
    } catch (CloneNotSupportedException e) {
      attrset = new BasicAttributes(ignoreCase);
    }
    attrset.attrs = new Hashtable<String, Attribute>(11);
    for (Attribute a : attrs.values()) {
      attrset.attrs.put(key(a.getID()), (Attribute) a.clone());
    }
    return attrset;
  }

  private String key(String id) {
    return ignoreCase ? id.toLowerCase(java.util.Locale.ENGLISH) : id;
  }

  public boolean isCaseIgnored() {
    return ignoreCase;
  }

  public int size() {
    return attrs.size();
  }

  public Attribute get(String attrID) {
    return attrs.get(key(attrID));
  }

  public NamingEnumeration<Attribute> getAll() {
    final Enumeration<Attribute> e = new java.util.Vector<Attribute>(attrs.values()).elements();
    return new NamingEnumeration<Attribute>() {
      public boolean hasMoreElements() {
        return e.hasMoreElements();
      }

      public Attribute nextElement() {
        return e.nextElement();
      }

      public boolean hasMore() throws NamingException {
        return e.hasMoreElements();
      }

      public Attribute next() throws NamingException {
        return e.nextElement();
      }

      public void close() throws NamingException {
      }
    };
  }

  public NamingEnumeration<String> getIDs() {
    final java.util.List<String> ids = new java.util.ArrayList<String>();
    for (Attribute a : attrs.values()) {
      ids.add(a.getID());
    }
    final java.util.Iterator<String> it = ids.iterator();
    return new NamingEnumeration<String>() {
      public boolean hasMoreElements() {
        return it.hasNext();
      }

      public String nextElement() {
        return it.next();
      }

      public boolean hasMore() throws NamingException {
        return it.hasNext();
      }

      public String next() throws NamingException {
        return it.next();
      }

      public void close() throws NamingException {
      }
    };
  }

  public Attribute put(String attrID, Object val) {
    return this.put(new BasicAttribute(attrID, val));
  }

  public Attribute put(Attribute attr) {
    String id = attr.getID();
    return attrs.put(key(id), attr);
  }

  public Attribute remove(String attrID) {
    return attrs.remove(key(attrID));
  }

  public String toString() {
    if (attrs.size() == 0) {
      return "No attributes";
    }
    return attrs.toString();
  }

  public boolean equals(Object obj) {
    if ((obj != null) && (obj instanceof Attributes)) {
      Attributes target = (Attributes) obj;
      if (ignoreCase != target.isCaseIgnored()) {
        return false;
      }
      if (size() == target.size()) {
        try {
          NamingEnumeration<? extends Attribute> theirs = target.getAll();
          while (theirs.hasMore()) {
            Attribute their = theirs.next();
            Attribute mine = get(their.getID());
            if (!their.equals(mine)) {
              return false;
            }
          }
        } catch (NamingException e) {
          return false;
        }
        return true;
      }
    }
    return false;
  }

  public int hashCode() {
    int hash = (ignoreCase ? 1 : 0);
    for (Attribute a : attrs.values()) {
      hash += a.hashCode();
    }
    return hash;
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    s.defaultWriteObject();
    s.writeInt(attrs.size());
    for (Attribute a : attrs.values()) {
      s.writeObject(a);
    }
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    int n = s.readInt();
    attrs = new Hashtable<String, Attribute>(n >= 1 ? n : 1);
    while (--n >= 0) {
      put((Attribute) s.readObject());
    }
  }
}
