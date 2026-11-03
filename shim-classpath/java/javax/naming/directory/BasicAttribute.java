/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming.directory;

import java.util.Vector;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

public class BasicAttribute implements Attribute {
  private static final long serialVersionUID = 6743528196119291326L;

  protected String attrID;
  protected transient Vector<Object> values;
  protected boolean ordered = false;

  @SuppressWarnings("unchecked")
  public Object clone() {
    BasicAttribute attr;
    try {
      attr = (BasicAttribute) super.clone();
    } catch (CloneNotSupportedException e) {
      attr = new BasicAttribute(attrID, ordered);
    }
    attr.values = (Vector<Object>) values.clone();
    return attr;
  }

  public boolean equals(Object obj) {
    if ((obj != null) && (obj instanceof Attribute)) {
      Attribute target = (Attribute) obj;
      if (isOrdered() != target.isOrdered()) {
        return false;
      }
      int len;
      if (attrID.equals(target.getID()) && (len = size()) == target.size()) {
        try {
          if (isOrdered()) {
            for (int i = 0; i < len; i++) {
              if (!valueEquals(get(i), target.get(i))) {
                return false;
              }
            }
          } else {
            java.util.Enumeration<?> theirs = target.getAll();
            while (theirs.hasMoreElements()) {
              if (find(theirs.nextElement()) < 0) {
                return false;
              }
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
    int hash = attrID.hashCode();
    int num = values.size();
    Object val;
    for (int i = 0; i < num; i++) {
      val = values.elementAt(i);
      if (val != null) {
        if (val.getClass().isArray()) {
          int len = java.lang.reflect.Array.getLength(val);
          for (int j = 0; j < len; j++) {
            Object it = java.lang.reflect.Array.get(val, j);
            if (it != null) {
              hash += it.hashCode();
            }
          }
        } else {
          hash += val.hashCode();
        }
      }
    }
    return hash;
  }

  public String toString() {
    StringBuilder answer = new StringBuilder(attrID + ": ");
    if (values.size() == 0) {
      answer.append("No values");
    } else {
      boolean start = true;
      java.util.Enumeration<Object> e = values.elements();
      while (e.hasMoreElements()) {
        if (!start) {
          answer.append(", ");
        }
        answer.append(e.nextElement());
        start = false;
      }
    }
    return answer.toString();
  }

  public BasicAttribute(String id) {
    this(id, false);
  }

  public BasicAttribute(String id, Object value) {
    this(id, value, false);
  }

  public BasicAttribute(String id, boolean ordered) {
    attrID = id;
    values = new Vector<Object>();
    this.ordered = ordered;
  }

  public BasicAttribute(String id, Object value, boolean ordered) {
    this(id, ordered);
    values.addElement(value);
  }

  public NamingEnumeration<?> getAll() throws NamingException {
    return new ValuesEnumeration(values);
  }

  public Object get() throws NamingException {
    if (values.size() == 0) {
      throw new java.util.NoSuchElementException("Attribute " + getID() + " has no value");
    }
    return values.elementAt(0);
  }

  public int size() {
    return values.size();
  }

  public String getID() {
    return attrID;
  }

  public boolean contains(Object attrVal) {
    return find(attrVal) >= 0;
  }

  private int find(Object target) {
    Class<?> cl;
    if (target == null) {
      int ct = values.size();
      for (int i = 0; i < ct; i++) {
        if (values.elementAt(i) == null) {
          return i;
        }
      }
    } else if ((cl = target.getClass()).isArray()) {
      int ct = values.size();
      for (int i = 0; i < ct; i++) {
        Object val = values.elementAt(i);
        if (val != null && cl == val.getClass() && arrayEquals(target, val)) {
          return i;
        }
      }
    } else {
      return values.indexOf(target, 0);
    }
    return -1;
  }

  private static boolean valueEquals(Object a, Object b) {
    if (a == b) {
      return true;
    }
    if (a == null || b == null) {
      return false;
    }
    if (a.getClass().isArray() && b.getClass().isArray()) {
      return arrayEquals(a, b);
    }
    return a.equals(b);
  }

  private static boolean arrayEquals(Object a1, Object a2) {
    int len;
    if ((len = java.lang.reflect.Array.getLength(a1)) != java.lang.reflect.Array.getLength(a2)) {
      return false;
    }
    for (int j = 0; j < len; j++) {
      Object i1 = java.lang.reflect.Array.get(a1, j);
      Object i2 = java.lang.reflect.Array.get(a2, j);
      if (i1 == null || i2 == null) {
        if (i1 != i2) {
          return false;
        }
      } else if (!i1.equals(i2)) {
        return false;
      }
    }
    return true;
  }

  public boolean add(Object attrVal) {
    if (isOrdered() || (find(attrVal) < 0)) {
      values.addElement(attrVal);
      return true;
    }
    return false;
  }

  public boolean remove(Object attrval) {
    int i = find(attrval);
    if (i >= 0) {
      values.removeElementAt(i);
      return true;
    }
    return false;
  }

  public void clear() {
    values.setSize(0);
  }

  public boolean isOrdered() {
    return ordered;
  }

  public Object get(int ix) throws NamingException {
    return values.elementAt(ix);
  }

  public Object remove(int ix) {
    Object answer = values.elementAt(ix);
    values.removeElementAt(ix);
    return answer;
  }

  public void add(int ix, Object attrVal) {
    if (!isOrdered() && contains(attrVal)) {
      throw new IllegalStateException("Cannot add duplicate to unordered attribute");
    }
    values.insertElementAt(attrVal, ix);
  }

  public Object set(int ix, Object attrVal) {
    if (!isOrdered() && contains(attrVal)) {
      throw new IllegalStateException("Cannot add duplicate to unordered attribute");
    }
    Object answer = values.elementAt(ix);
    values.setElementAt(attrVal, ix);
    return answer;
  }

  public DirContext getAttributeSyntaxDefinition() throws NamingException {
    throw new OperationNotSupportedException("attribute syntax");
  }

  public DirContext getAttributeDefinition() throws NamingException {
    throw new OperationNotSupportedException("attribute definition");
  }

  private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
    s.defaultWriteObject();
    s.writeInt(values.size());
    for (int i = 0; i < values.size(); i++) {
      s.writeObject(values.elementAt(i));
    }
  }

  private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
    s.defaultReadObject();
    int n = s.readInt();
    values = new Vector<Object>(n);
    while (--n >= 0) {
      values.addElement(s.readObject());
    }
  }
}

class ValuesEnumeration implements NamingEnumeration<Object> {
  private final java.util.Enumeration<Object> e;

  ValuesEnumeration(Vector<Object> v) {
    e = new Vector<Object>(v).elements();
  }

  public boolean hasMoreElements() {
    return e.hasMoreElements();
  }

  public Object nextElement() {
    return e.nextElement();
  }

  public boolean hasMore() throws NamingException {
    return e.hasMoreElements();
  }

  public Object next() throws NamingException {
    return e.nextElement();
  }

  public void close() throws NamingException {
  }
}
