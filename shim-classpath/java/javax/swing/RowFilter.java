/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RowFilter<M, I> {
  public enum ComparisonType {
    BEFORE, AFTER, EQUAL, NOT_EQUAL
  }

  public abstract static class Entry<M, I> {
    public Entry() { }

    public abstract M getModel();

    public abstract int getValueCount();

    public abstract Object getValue(int index);

    public String getStringValue(int index) {
      Object value = getValue(index);
      return (value == null) ? "" : value.toString();
    }

    public abstract I getIdentifier();
  }

  protected RowFilter() { }

  public abstract boolean include(Entry<? extends M, ? extends I> entry);

  public static <M, I> RowFilter<M, I> regexFilter(String regex, int... indices) {
    return new RegexFilter<M, I>(Pattern.compile(regex), indices);
  }

  public static <M, I> RowFilter<M, I> dateFilter(ComparisonType type, Date date, int... indices) {
    return new DateOrNumberFilter<M, I>(type, date, indices);
  }

  public static <M, I> RowFilter<M, I> numberFilter(ComparisonType type, Number number, int... indices) {
    return new DateOrNumberFilter<M, I>(type, number, indices);
  }

  public static <M, I> RowFilter<M, I> orFilter(Iterable<? extends RowFilter<? super M, ? super I>> filters) {
    return new OrFilter<M, I>(filters);
  }

  public static <M, I> RowFilter<M, I> andFilter(Iterable<? extends RowFilter<? super M, ? super I>> filters) {
    return new AndFilter<M, I>(filters);
  }

  public static <M, I> RowFilter<M, I> notFilter(RowFilter<M, I> filter) { return new NotFilter<M, I>(filter); }

  private static void checkIndices(int[] indices) {
    for (int i = indices.length - 1; i >= 0; i--) {
      if (indices[i] < 0) {
        throw new IllegalArgumentException("Index must be >= 0");
      }
    }
  }

  private abstract static class GeneralFilter<M, I> extends RowFilter<M, I> {
    private final int[] columns;

    GeneralFilter(int[] columns) {
      checkIndices(columns);
      this.columns = columns;
    }

    public boolean include(Entry<? extends M, ? extends I> value) {
      int count = value.getValueCount();
      if (columns.length > 0) {
        for (int i = columns.length - 1; i >= 0; i--) {
          int index = columns[i];
          if (index < count && include(value, index)) {
            return true;
          }
        }
      } else {
        while (--count >= 0) {
          if (include(value, count)) {
            return true;
          }
        }
      }
      return false;
    }

    protected abstract boolean include(Entry<? extends M, ? extends I> value, int index);
  }

  private static final class RegexFilter<M, I> extends GeneralFilter<M, I> {
    private final Matcher matcher;

    RegexFilter(Pattern regex, int[] columns) {
      super(columns);
      if (regex == null) {
        throw new IllegalArgumentException("Pattern must be non-null");
      }
      matcher = regex.matcher("");
    }

    protected boolean include(Entry<? extends M, ? extends I> value, int index) {
      matcher.reset(value.getStringValue(index));
      return matcher.find();
    }
  }

  private static final class DateOrNumberFilter<M, I> extends GeneralFilter<M, I> {
    private final ComparisonType type;
    private final long length;
    private final boolean isDate;

    DateOrNumberFilter(ComparisonType type, Object o, int[] columns) {
      super(columns);
      if (type == null || o == null) {
        throw new IllegalArgumentException("type and object must be non-null");
      }
      this.type = type;
      if (o instanceof Date) {
        length = ((Date) o).getTime();
        isDate = true;
      } else if (o instanceof Number) {
        length = ((Number) o).longValue();
        isDate = false;
      } else {
        throw new IllegalArgumentException("object must be date or number");
      }
    }

    protected boolean include(Entry<? extends M, ? extends I> value, int index) {
      Object v = value.getValue(index);
      if (isDate) {
        if (v instanceof Date) {
          long vl = ((Date) v).getTime();
          switch (type) {
            case BEFORE: return vl < length;
            case AFTER: return vl > length;
            case EQUAL: return vl == length;
            case NOT_EQUAL: return vl != length;
            default: break;
          }
        }
      } else if (v instanceof Number) {
        boolean floatingPoint = (v instanceof Float || v instanceof Double);
        long vl = ((Number) v).longValue();
        if (floatingPoint) {
          double d = ((Number) v).doubleValue();
          switch (type) {
            case BEFORE: return d < length;
            case AFTER: return d > length;
            case EQUAL: return d == length;
            case NOT_EQUAL: return d != length;
            default: break;
          }
        }
        switch (type) {
          case BEFORE: return vl < length;
          case AFTER: return vl > length;
          case EQUAL: return vl == length;
          case NOT_EQUAL: return vl != length;
          default: break;
        }
      }
      return false;
    }
  }

  private static final class OrFilter<M, I> extends RowFilter<M, I> {
    private final List<RowFilter<? super M, ? super I>> filters = new ArrayList<>();

    OrFilter(Iterable<? extends RowFilter<? super M, ? super I>> filters) {
      for (RowFilter<? super M, ? super I> filter : filters) {
        if (filter == null) {
          throw new IllegalArgumentException("Filter must be non-null");
        }
        this.filters.add(filter);
      }
    }

    public boolean include(Entry<? extends M, ? extends I> value) {
      for (RowFilter<? super M, ? super I> filter : filters) {
        if (filter.include(value)) {
          return true;
        }
      }
      return false;
    }
  }

  private static final class AndFilter<M, I> extends RowFilter<M, I> {
    private final List<RowFilter<? super M, ? super I>> filters = new ArrayList<>();

    AndFilter(Iterable<? extends RowFilter<? super M, ? super I>> filters) {
      for (RowFilter<? super M, ? super I> filter : filters) {
        if (filter == null) {
          throw new IllegalArgumentException("Filter must be non-null");
        }
        this.filters.add(filter);
      }
    }

    public boolean include(Entry<? extends M, ? extends I> value) {
      for (RowFilter<? super M, ? super I> filter : filters) {
        if (!filter.include(value)) {
          return false;
        }
      }
      return true;
    }
  }

  private static final class NotFilter<M, I> extends RowFilter<M, I> {
    private final RowFilter<M, I> filter;

    NotFilter(RowFilter<M, I> filter) {
      if (filter == null) {
        throw new IllegalArgumentException("filter must be non-null");
      }
      this.filter = filter;
    }

    public boolean include(Entry<? extends M, ? extends I> value) { return !filter.include(value); }
  }
}
