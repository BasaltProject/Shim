/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.naming;

import java.util.Enumeration;
import java.util.Vector;

public class Reference implements Cloneable, java.io.Serializable {
  private static final long serialVersionUID = -1673475790065791735L;

  protected String className;
  protected Vector<RefAddr> addrs = null;
  protected String classFactory = null;
  protected String classFactoryLocation = null;

  public Reference(String className) {
    this.className = className;
    addrs = new Vector<RefAddr>();
  }

  public Reference(String className, RefAddr addr) {
    this.className = className;
    addrs = new Vector<RefAddr>();
    addrs.addElement(addr);
  }

  public Reference(String className, String factory, String factoryLocation) {
    this(className);
    classFactory = factory;
    classFactoryLocation = factoryLocation;
  }

  public Reference(String className, RefAddr addr, String factory, String factoryLocation) {
    this(className, addr);
    classFactory = factory;
    classFactoryLocation = factoryLocation;
  }

  public String getClassName() {
    return className;
  }

  public String getFactoryClassName() {
    return classFactory;
  }

  public String getFactoryClassLocation() {
    return classFactoryLocation;
  }

  public RefAddr get(String addrType) {
    int len = addrs.size();
    for (int i = 0; i < len; i++) {
      RefAddr addr = addrs.elementAt(i);
      if (addr.getType().compareTo(addrType) == 0) {
        return addr;
      }
    }
    return null;
  }

  public RefAddr get(int posn) {
    return addrs.elementAt(posn);
  }

  public Enumeration<RefAddr> getAll() {
    return addrs.elements();
  }

  public int size() {
    return addrs.size();
  }

  public void add(RefAddr addr) {
    addrs.addElement(addr);
  }

  public void add(int posn, RefAddr addr) {
    addrs.insertElementAt(addr, posn);
  }

  public Object remove(int posn) {
    Object r = addrs.elementAt(posn);
    addrs.removeElementAt(posn);
    return r;
  }

  public void clear() {
    addrs.removeAllElements();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Reference) {
      Reference target = (Reference) obj;
      if (target.className.equals(this.className) && target.size() == this.size()) {
        Enumeration<RefAddr> mycomps = getAll();
        Enumeration<RefAddr> comps = target.getAll();
        while (mycomps.hasMoreElements()) {
          if (!(mycomps.nextElement().equals(comps.nextElement()))) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = className.hashCode();
    Enumeration<RefAddr> enumeration = getAll();
    while (enumeration.hasMoreElements()) {
      hash += enumeration.nextElement().hashCode();
    }
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder("Reference Class Name: " + className + "\n");
    int len = addrs.size();
    for (int i = 0; i < len; i++) {
      str.append(get(i).toString());
    }
    return str.toString();
  }

  @Override
  public Object clone() {
    Reference r = new Reference(className, classFactory, classFactoryLocation);
    Enumeration<RefAddr> enumeration = getAll();
    r.addrs = new Vector<RefAddr>();
    while (enumeration.hasMoreElements()) {
      r.addrs.addElement(enumeration.nextElement());
    }
    return r;
  }
}
