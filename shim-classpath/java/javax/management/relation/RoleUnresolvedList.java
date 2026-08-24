/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.management.relation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoleUnresolvedList extends ArrayList<Object> {
  private static final long serialVersionUID = 4054902803091433324L;

  private transient boolean typeSafe;
  private transient boolean tainted;

  public RoleUnresolvedList() {
    super();
  }

  public RoleUnresolvedList(int initialCapacity) {
    super(initialCapacity);
  }

  public RoleUnresolvedList(List<RoleUnresolved> list) throws IllegalArgumentException {
    super();
    if (list == null) {
      throw new IllegalArgumentException("Null parameter");
    }
    checkTypeSafe(list);
    super.addAll(list);
  }

  public List<RoleUnresolved> asList() {
    if (!typeSafe) {
      if (tainted) {
        checkTypeSafe(this);
      }
      typeSafe = true;
    }
    @SuppressWarnings("unchecked")
    List<RoleUnresolved> l = (List<RoleUnresolved>) (List<?>) this;
    return l;
  }

  public void add(RoleUnresolved role) throws IllegalArgumentException {
    if (role == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    super.add(role);
  }

  public void add(int index, RoleUnresolved role) throws IllegalArgumentException, IndexOutOfBoundsException {
    if (role == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    super.add(index, role);
  }

  public void set(int index, RoleUnresolved role) throws IllegalArgumentException, IndexOutOfBoundsException {
    if (role == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    super.set(index, role);
  }

  public boolean addAll(RoleUnresolvedList list) throws IndexOutOfBoundsException {
    if (list == null) {
      return true;
    }
    return super.addAll(list);
  }

  public boolean addAll(int index, RoleUnresolvedList list) throws IllegalArgumentException, IndexOutOfBoundsException {
    if (list == null) {
      throw new IllegalArgumentException("Invalid parameter");
    }
    return super.addAll(index, list);
  }

  @Override
  public boolean add(Object o) {
    if (!tainted) {
      tainted = isTainted(o);
    }
    if (typeSafe) {
      checkTypeSafe(o);
    }
    return super.add(o);
  }

  @Override
  public void add(int index, Object element) {
    if (!tainted) {
      tainted = isTainted(element);
    }
    if (typeSafe) {
      checkTypeSafe(element);
    }
    super.add(index, element);
  }

  @Override
  public boolean addAll(Collection<?> c) {
    if (!tainted) {
      tainted = isTainted(c);
    }
    if (typeSafe) {
      checkTypeSafe(c);
    }
    return super.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<?> c) {
    if (!tainted) {
      tainted = isTainted(c);
    }
    if (typeSafe) {
      checkTypeSafe(c);
    }
    return super.addAll(index, c);
  }

  @Override
  public Object set(int index, Object element) {
    if (!tainted) {
      tainted = isTainted(element);
    }
    if (typeSafe) {
      checkTypeSafe(element);
    }
    return super.set(index, element);
  }

  private static boolean isTainted(Object o) {
    try {
      checkTypeSafe(o);
    } catch (IllegalArgumentException e) {
      return true;
    }
    return false;
  }

  private static boolean isTainted(Collection<?> c) {
    try {
      checkTypeSafe(c);
    } catch (IllegalArgumentException e) {
      return true;
    }
    return false;
  }

  private static void checkTypeSafe(Object o) {
    try {
      RoleUnresolved r = (RoleUnresolved) o;
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static void checkTypeSafe(Collection<?> c) {
    try {
      RoleUnresolved r;
      for (Object o : c) {
        r = (RoleUnresolved) o;
      }
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
