/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.security.auth;

import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayDeque;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;

public final class Subject implements Serializable {
  private static final long serialVersionUID = -8308522755600156056L;

  private static final int kPrincipals = 1;
  private static final int kPublic = 2;
  private static final int kPrivate = 3;

  private static final ThreadLocal<ArrayDeque<Subject>> kCurrent = new ThreadLocal<ArrayDeque<Subject>>() {
    @Override
    protected ArrayDeque<Subject> initialValue() {
      return new ArrayDeque<Subject>();
    }
  };

  private final Set<Principal> principals;
  private final Set<Object> pubCredentials;
  private final Set<Object> privCredentials;
  private volatile boolean readOnly;

  public Subject() {
    this.principals = Collections.synchronizedSet(new SecureSet<Principal>(this, kPrincipals));
    this.pubCredentials = Collections.synchronizedSet(new SecureSet<Object>(this, kPublic));
    this.privCredentials = Collections.synchronizedSet(new SecureSet<Object>(this, kPrivate));
  }

  public Subject(boolean readOnly, Set<? extends Principal> principals, Set<?> pubCredentials,
      Set<?> privCredentials) {
    Objects.requireNonNull(principals, "invalid null input(s)");
    Objects.requireNonNull(pubCredentials, "invalid null input(s)");
    Objects.requireNonNull(privCredentials, "invalid null input(s)");
    this.principals = Collections.synchronizedSet(new SecureSet<Principal>(this, kPrincipals, principals));
    this.pubCredentials = Collections.synchronizedSet(new SecureSet<Object>(this, kPublic, pubCredentials));
    this.privCredentials = Collections.synchronizedSet(new SecureSet<Object>(this, kPrivate, privCredentials));
    this.readOnly = readOnly;
  }

  public void setReadOnly() {
    this.readOnly = true;
  }

  public boolean isReadOnly() {
    return this.readOnly;
  }

  @Deprecated
  public static Subject getSubject(final AccessControlContext acc) {
    Objects.requireNonNull(acc, "invalid null AccessControlContext provided");
    return null;
  }

  public static Subject current() {
    return kCurrent.get().peek();
  }

  public static <T> T callAs(final Subject subject, final Callable<T> action) throws CompletionException {
    Objects.requireNonNull(action);
    ArrayDeque<Subject> stack = kCurrent.get();
    stack.push(subject == null ? new Subject() : subject);
    boolean anonymous = subject == null;
    try {
      return action.call();
    } catch (Exception e) {
      throw new CompletionException(e);
    } finally {
      stack.pop();
    }
  }

  @Deprecated
  public static <T> T doAs(final Subject subject, final PrivilegedAction<? extends T> action) {
    Objects.requireNonNull(action, "invalid null action provided");
    ArrayDeque<Subject> stack = kCurrent.get();
    stack.push(subject == null ? new Subject() : subject);
    try {
      return action.run();
    } finally {
      stack.pop();
    }
  }

  @Deprecated
  public static <T> T doAs(final Subject subject, final PrivilegedExceptionAction<T> action)
      throws PrivilegedActionException {
    Objects.requireNonNull(action, "invalid null action provided");
    ArrayDeque<Subject> stack = kCurrent.get();
    stack.push(subject == null ? new Subject() : subject);
    try {
      return action.run();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new PrivilegedActionException(e);
    } finally {
      stack.pop();
    }
  }

  @Deprecated
  public static <T> T doAsPrivileged(final Subject subject, final PrivilegedAction<? extends T> action,
      final AccessControlContext acc) {
    return doAs(subject, action);
  }

  @Deprecated
  public static <T> T doAsPrivileged(final Subject subject, final PrivilegedExceptionAction<T> action,
      final AccessControlContext acc) throws PrivilegedActionException {
    return doAs(subject, action);
  }

  public Set<Principal> getPrincipals() {
    return principals;
  }

  public <T extends Principal> Set<T> getPrincipals(Class<T> c) {
    Objects.requireNonNull(c, "invalid null Class provided");
    return new ClassSet<T>(this, kPrincipals, c);
  }

  public Set<Object> getPublicCredentials() {
    return pubCredentials;
  }

  public Set<Object> getPrivateCredentials() {
    return privCredentials;
  }

  public <T> Set<T> getPublicCredentials(Class<T> c) {
    Objects.requireNonNull(c, "invalid null Class provided");
    return new ClassSet<T>(this, kPublic, c);
  }

  public <T> Set<T> getPrivateCredentials(Class<T> c) {
    Objects.requireNonNull(c, "invalid null Class provided");
    return new ClassSet<T>(this, kPrivate, c);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (this == o) {
      return true;
    }
    if (o instanceof Subject) {
      final Subject that = (Subject) o;
      Set<Principal> thatPrincipals;
      synchronized (that.principals) {
        thatPrincipals = new java.util.HashSet<Principal>(that.principals);
      }
      if (!principals.equals(thatPrincipals)) {
        return false;
      }
      Set<Object> thatPub;
      synchronized (that.pubCredentials) {
        thatPub = new java.util.HashSet<Object>(that.pubCredentials);
      }
      if (!pubCredentials.equals(thatPub)) {
        return false;
      }
      Set<Object> thatPriv;
      synchronized (that.privCredentials) {
        thatPriv = new java.util.HashSet<Object>(that.privCredentials);
      }
      return privCredentials.equals(thatPriv);
    }
    return false;
  }

  @Override
  public String toString() {
    return toString(true);
  }

  String toString(boolean includePrivateCredentials) {
    StringBuilder sb = new StringBuilder("Subject:\n");
    synchronized (principals) {
      for (Principal p : principals) {
        sb.append("\tPrincipal: ").append(p).append('\n');
      }
    }
    synchronized (pubCredentials) {
      for (Object c : pubCredentials) {
        sb.append("\tPublic Credential: ").append(c).append('\n');
      }
    }
    if (includePrivateCredentials) {
      synchronized (privCredentials) {
        for (Object c : privCredentials) {
          sb.append("\tPrivate Credential: ").append(c).append('\n');
        }
      }
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    synchronized (principals) {
      for (Principal p : principals) {
        hashCode ^= p.hashCode();
      }
    }
    synchronized (pubCredentials) {
      for (Object c : pubCredentials) {
        hashCode ^= System.identityHashCode(c) == 0 ? 0 : c.hashCode();
      }
    }
    return hashCode;
  }

  private static final class SecureSet<E> extends AbstractSet<E> implements Serializable {
    private static final long serialVersionUID = 7911754171111800359L;

    private final Subject subject;
    private final int which;
    private final LinkedList<E> elements = new LinkedList<E>();

    SecureSet(Subject subject, int which) {
      this.subject = subject;
      this.which = which;
    }

    SecureSet(Subject subject, int which, Set<?> set) {
      this.subject = subject;
      this.which = which;
      for (Object o : set) {
        @SuppressWarnings("unchecked")
        E e = (E) o;
        if (!elements.contains(e)) {
          elements.add(e);
        }
      }
    }

    @Override
    public String toString() {
      return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    @Override
    public int size() {
      return elements.size();
    }

    @Override
    public Iterator<E> iterator() {
      final LinkedList<E> list = elements;
      return new Iterator<E>() {
        private final Iterator<E> iterator = list.iterator();

        public boolean hasNext() {
          return iterator.hasNext();
        }

        public E next() {
          return iterator.next();
        }

        public void remove() {
          if (subject.isReadOnly()) {
            throw new IllegalStateException("Subject is read-only");
          }
          iterator.remove();
        }
      };
    }

    @Override
    public boolean add(E o) {
      Objects.requireNonNull(o, "invalid null input(s)");
      if (subject.isReadOnly()) {
        throw new IllegalStateException("Subject is read-only");
      }
      if (which == kPrincipals && !(o instanceof Principal)) {
        throw new SecurityException("attempting to add an object which is not an instance of "
            + "java.security.Principal to a Subject's Principal Set");
      }
      if (!elements.contains(o)) {
        return elements.add(o);
      }
      return false;
    }

    @Override
    public boolean remove(Object o) {
      Objects.requireNonNull(o, "invalid null input(s)");
      if (subject.isReadOnly()) {
        throw new IllegalStateException("Subject is read-only");
      }
      return elements.remove(o);
    }

    @Override
    public boolean contains(Object o) {
      Objects.requireNonNull(o, "invalid null input(s)");
      return elements.contains(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      Objects.requireNonNull(c, "invalid null input(s)");
      if (subject.isReadOnly()) {
        throw new IllegalStateException("Subject is read-only");
      }
      return elements.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      Objects.requireNonNull(c, "invalid null input(s)");
      if (subject.isReadOnly()) {
        throw new IllegalStateException("Subject is read-only");
      }
      return elements.retainAll(c);
    }

    @Override
    public void clear() {
      if (subject.isReadOnly()) {
        throw new IllegalStateException("Subject is read-only");
      }
      elements.clear();
    }
  }

  private static final class ClassSet<T> extends AbstractSet<T> {
    private final Subject subject;
    private final int which;
    private final Class<?> c;
    private final Set<T> set = new java.util.HashSet<T>();

    @SuppressWarnings("unchecked")
    ClassSet(Subject subject, int which, Class<?> c) {
      this.subject = subject;
      this.which = which;
      this.c = c;
      Set<?> source = which == kPrincipals ? subject.principals : which == kPublic ? subject.pubCredentials
          : subject.privCredentials;
      synchronized (source) {
        for (Object o : source) {
          if (c.isAssignableFrom(o.getClass())) {
            set.add((T) o);
          }
        }
      }
    }

    @Override
    public int size() {
      return set.size();
    }

    @Override
    public Iterator<T> iterator() {
      return set.iterator();
    }

    @Override
    public boolean add(T o) {
      if (!c.isAssignableFrom(o.getClass())) {
        throw new SecurityException("attempting to add an object which is not an instance of " + c);
      }
      return set.add(o);
    }
  }
}
