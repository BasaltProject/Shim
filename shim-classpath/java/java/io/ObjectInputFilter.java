/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

@FunctionalInterface
public interface ObjectInputFilter {
  Status checkInput(FilterInfo filterInfo);

  interface FilterInfo {
    Class<?> serialClass();

    long arrayLength();

    long depth();

    long references();

    long streamBytes();
  }

  enum Status {
    UNDECIDED,
    ALLOWED,
    REJECTED
  }

  static ObjectInputFilter allowFilter(Predicate<Class<?>> predicate, Status otherStatus) {
    Objects.requireNonNull(predicate, "predicate");
    Objects.requireNonNull(otherStatus, "otherStatus");
    return new PredicateFilter(predicate, Status.ALLOWED, otherStatus);
  }

  static ObjectInputFilter rejectFilter(Predicate<Class<?>> predicate, Status otherStatus) {
    Objects.requireNonNull(predicate, "predicate");
    Objects.requireNonNull(otherStatus, "otherStatus");
    return new PredicateFilter(predicate, Status.REJECTED, otherStatus);
  }

  static ObjectInputFilter merge(ObjectInputFilter filter, ObjectInputFilter anotherFilter) {
    Objects.requireNonNull(filter, "filter");
    if (anotherFilter == null) {
      return filter;
    }
    return new MergedFilter(filter, anotherFilter);
  }

  static ObjectInputFilter rejectUndecidedClass(ObjectInputFilter filter) {
    Objects.requireNonNull(filter, "filter");
    return new RejectUndecided(filter);
  }

  final class PredicateFilter implements ObjectInputFilter {
    private final Predicate<Class<?>> predicate;
    private final Status ifTrue;
    private final Status otherStatus;

    PredicateFilter(Predicate<Class<?>> predicate, Status ifTrue, Status otherStatus) {
      this.predicate = predicate;
      this.ifTrue = ifTrue;
      this.otherStatus = otherStatus;
    }

    public Status checkInput(FilterInfo info) {
      Class<?> cl = info.serialClass();
      if (cl == null) {
        return Status.UNDECIDED;
      }
      return predicate.test(cl) ? ifTrue : otherStatus;
    }

    public String toString() {
      return "PredicateFilter";
    }
  }

  final class MergedFilter implements ObjectInputFilter {
    private final ObjectInputFilter first;
    private final ObjectInputFilter second;

    MergedFilter(ObjectInputFilter first, ObjectInputFilter second) {
      this.first = first;
      this.second = second;
    }

    public Status checkInput(FilterInfo info) {
      Status s = first.checkInput(info);
      if (s != Status.UNDECIDED) {
        return s;
      }
      return second.checkInput(info);
    }
  }

  final class RejectUndecided implements ObjectInputFilter {
    private final ObjectInputFilter filter;

    RejectUndecided(ObjectInputFilter filter) {
      this.filter = filter;
    }

    public Status checkInput(FilterInfo info) {
      Status s = filter.checkInput(info);
      if (s == Status.UNDECIDED && info.serialClass() != null) {
        return Status.REJECTED;
      }
      return s;
    }
  }

  final class Config {
    private static ObjectInputFilter serialFilter;
    private static BinaryOperator<ObjectInputFilter> filterFactory = new BuiltinFactory();
    private static boolean filterSet;
    private static boolean factorySet;
    private static boolean factoryUsed;

    private Config() { }

    static final class BuiltinFactory implements BinaryOperator<ObjectInputFilter> {
      public ObjectInputFilter apply(ObjectInputFilter current, ObjectInputFilter next) {
        if (current == null) {
          return next;
        }
        return next == null ? current : merge(next, current);
      }
    }

    public static synchronized ObjectInputFilter getSerialFilter() {
      return serialFilter;
    }

    public static synchronized void setSerialFilter(ObjectInputFilter filter) {
      Objects.requireNonNull(filter, "filter");
      if (filterSet) {
        throw new IllegalStateException("Serial filter can only be set once");
      }
      filterSet = true;
      serialFilter = filter;
    }

    public static synchronized BinaryOperator<ObjectInputFilter> getSerialFilterFactory() {
      factoryUsed = true;
      return filterFactory;
    }

    public static synchronized void setSerialFilterFactory(BinaryOperator<ObjectInputFilter> filterFactory) {
      Objects.requireNonNull(filterFactory, "filterFactory");
      if (factoryUsed) {
        throw new IllegalStateException("Cannot replace filter factory: " + Config.filterFactory);
      }
      if (factorySet) {
        throw new IllegalStateException("Cannot replace filter factory: " + Config.filterFactory);
      }
      factorySet = true;
      Config.filterFactory = filterFactory;
    }

    public static ObjectInputFilter createFilter(String pattern) {
      Objects.requireNonNull(pattern, "pattern");
      return PatternFilter.create(pattern);
    }
  }

  final class PatternFilter implements ObjectInputFilter {
    private final List<String> rules = new ArrayList<String>();
    private long maxStream = Long.MAX_VALUE;
    private long maxDepth = Long.MAX_VALUE;
    private long maxRefs = Long.MAX_VALUE;
    private long maxArray = Long.MAX_VALUE;
    private final String text;

    private PatternFilter(String text) {
      this.text = text;
    }

    static PatternFilter create(String pattern) {
      PatternFilter f = new PatternFilter(pattern);
      for (String raw : pattern.split(";")) {
        String p = raw.trim();
        if (p.isEmpty()) {
          continue;
        }
        int eq = p.indexOf('=');
        if (eq >= 0) {
          String key = p.substring(0, eq);
          long value = Long.parseLong(p.substring(eq + 1));
          if (value < 0) {
            throw new IllegalArgumentException("Invalid limit: " + p);
          }
          if (key.equals("maxdepth")) {
            f.maxDepth = value;
          } else if (key.equals("maxrefs")) {
            f.maxRefs = value;
          } else if (key.equals("maxbytes")) {
            f.maxStream = value;
          } else if (key.equals("maxarray")) {
            f.maxArray = value;
          } else {
            throw new IllegalArgumentException("Unknown limit: " + key);
          }
        } else {
          f.rules.add(p);
        }
      }
      return f;
    }

    public Status checkInput(FilterInfo info) {
      if (info.references() > maxRefs || info.depth() > maxDepth || info.streamBytes() > maxStream
          || info.arrayLength() > maxArray) {
        return Status.REJECTED;
      }
      Class<?> cl = info.serialClass();
      if (cl == null) {
        return Status.UNDECIDED;
      }
      while (cl.isArray()) {
        cl = cl.getComponentType();
      }
      if (cl.isPrimitive()) {
        return Status.UNDECIDED;
      }
      String name = cl.getName();
      for (String rule : rules) {
        boolean reject = rule.startsWith("!");
        String pat = reject ? rule.substring(1) : rule;
        int slash = pat.indexOf('/');
        if (slash >= 0) {
          pat = pat.substring(slash + 1);
        }
        if (matches(pat, name)) {
          return reject ? Status.REJECTED : Status.ALLOWED;
        }
      }
      return Status.UNDECIDED;
    }

    private static boolean matches(String pat, String name) {
      if (pat.equals("*")) {
        return true;
      }
      if (pat.endsWith(".**")) {
        return name.startsWith(pat.substring(0, pat.length() - 2));
      }
      if (pat.endsWith(".*")) {
        String prefix = pat.substring(0, pat.length() - 1);
        return name.startsWith(prefix) && name.indexOf('.', prefix.length()) < 0;
      }
      if (pat.endsWith("$*")) {
        return name.startsWith(pat.substring(0, pat.length() - 1));
      }
      return name.equals(pat);
    }

    public String toString() {
      return text;
    }
  }
}
