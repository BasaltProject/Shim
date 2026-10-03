/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package jdk.jfr.consumer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jdk.jfr.ValueDescriptor;

public class RecordedObject {
  final Chunk.TypeDef td;
  final Object[] vals;
  final Chunk chunk;

  RecordedObject(Chunk.TypeDef td, Object[] vals, Chunk chunk) {
    this.td = td;
    this.vals = vals;
    this.chunk = chunk;
  }

  private int indexOf(String name) {
    for (int i = 0; i < td.fields.size(); i++) {
      if (td.fields.get(i).name.equals(name)) {
        return i;
      }
    }
    return -1;
  }

  private Object[] locate(String name) {
    java.util.Objects.requireNonNull(name);
    RecordedObject cur = this;
    String[] parts = name.split("\\.");
    for (int k = 0; k < parts.length; k++) {
      int i = cur.indexOf(parts[k]);
      if (i < 0) {
        throw new IllegalArgumentException("Attempt to get unknown field \"" + parts[k] + "\"");
      }
      if (k == parts.length - 1) {
        return new Object[] {cur, i};
      }
      Object v = cur.chunk.resolve(cur.vals[i]);
      if (!(v instanceof RecordedObject)) {
        if (v == null) {
          return new Object[] {null, -1};
        }
        throw new IllegalArgumentException("Attempt to get unknown field \"" + parts[k + 1] + "\"");
      }
      cur = (RecordedObject) v;
    }
    throw new IllegalArgumentException(name);
  }

  public boolean hasField(String name) {
    java.util.Objects.requireNonNull(name);
    try {
      locate(name);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public final <T> T getValue(String name) {
    Object[] loc = locate(name);
    if (loc[0] == null) {
      return null;
    }
    RecordedObject o = (RecordedObject) loc[0];
    return (T) o.chunk.resolve(o.vals[(Integer) loc[1]]);
  }

  private Object raw(String name) {
    return getValue(name);
  }

  public final boolean getBoolean(String name) {
    Object v = raw(name);
    if (v instanceof Boolean) {
      return (Boolean) v;
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final byte getByte(String name) {
    Object v = raw(name);
    if (v instanceof Byte) {
      return (Byte) v;
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final char getChar(String name) {
    Object v = raw(name);
    if (v instanceof Character) {
      return (Character) v;
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final short getShort(String name) {
    Object v = raw(name);
    if (v instanceof Byte || v instanceof Short) {
      return ((Number) v).shortValue();
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final int getInt(String name) {
    Object v = raw(name);
    if (v instanceof Byte || v instanceof Short || v instanceof Integer) {
      return ((Number) v).intValue();
    }
    if (v instanceof Character) {
      return (Character) v;
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final long getLong(String name) {
    Object v = raw(name);
    if (v instanceof Byte || v instanceof Short || v instanceof Integer || v instanceof Long) {
      return ((Number) v).longValue();
    }
    if (v instanceof Character) {
      return (Character) v;
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final float getFloat(String name) {
    Object v = raw(name);
    if (v instanceof Float) {
      return (Float) v;
    }
    if (v instanceof Byte || v instanceof Short || v instanceof Integer || v instanceof Long) {
      return ((Number) v).floatValue();
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final double getDouble(String name) {
    Object v = raw(name);
    if (v instanceof Number && !(v instanceof java.math.BigInteger)) {
      return ((Number) v).doubleValue();
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final String getString(String name) {
    Object v = raw(name);
    if (v == null || v instanceof String) {
      return (String) v;
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final RecordedThread getThread(String name) {
    Object v = raw(name);
    if (v == null || v instanceof RecordedThread) {
      return (RecordedThread) v;
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final RecordedClass getClass(String name) {
    Object v = raw(name);
    if (v == null || v instanceof RecordedClass) {
      return (RecordedClass) v;
    }
    throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
  }

  public final Duration getDuration(String name) {
    Object[] loc = locate(name);
    RecordedObject o = (RecordedObject) loc[0];
    if (o == null) {
      return null;
    }
    Chunk.FieldDef f = o.td.fields.get((Integer) loc[1]);
    Object v = o.chunk.resolve(o.vals[(Integer) loc[1]]);
    if (!(v instanceof Number)) {
      throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
    }
    long n = ((Number) v).longValue();
    String unit = chunk.unit(f, "jdk.jfr.Timespan");
    if (unit == null) {
      throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
    }
    return toDuration(n, unit, o.chunk);
  }

  static Duration toDuration(long n, String unit, Chunk c) {
    switch (unit) {
      case "TICKS":
        return Duration.ofNanos((long) (n * (1e9 / c.ticksPerSecond)));
      case "MICROSECONDS":
        return Duration.ofNanos(n * 1000L);
      case "MILLISECONDS":
        return Duration.ofMillis(n);
      case "SECONDS":
        return Duration.ofSeconds(n);
      default:
        return Duration.ofNanos(n);
    }
  }

  public final Instant getInstant(String name) {
    Object[] loc = locate(name);
    RecordedObject o = (RecordedObject) loc[0];
    if (o == null) {
      return null;
    }
    Chunk.FieldDef f = o.td.fields.get((Integer) loc[1]);
    Object v = o.chunk.resolve(o.vals[(Integer) loc[1]]);
    if (!(v instanceof Number)) {
      throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
    }
    long n = ((Number) v).longValue();
    String unit = chunk.unit(f, "jdk.jfr.Timestamp");
    if (unit == null) {
      throw new IllegalArgumentException("Attempt to get field \"" + name + "\" with wrong accessor");
    }
    if (unit.equals("TICKS")) {
      return o.chunk.instantOf(n);
    }
    return Instant.ofEpochMilli(n);
  }

  public List<ValueDescriptor> getFields() {
    return chunk.descriptors(td);
  }

  public final String toString() {
    StringBuilder sb = new StringBuilder();
    format(sb, "", 0);
    return sb.toString();
  }

  void format(StringBuilder sb, String indent, int depth) {
    sb.append(td.name).append(" {\n");
    for (int i = 0; i < td.fields.size(); i++) {
      Chunk.FieldDef f = td.fields.get(i);
      sb.append(indent).append("  ").append(f.name).append(" = ");
      Object v = chunk.resolve(vals[i]);
      if (v instanceof RecordedObject) {
        if (depth >= 2) {
          sb.append("...");
        } else {
          ((RecordedObject) v).format(sb, indent + "  ", depth + 1);
        }
      } else if (v instanceof List) {
        sb.append("[\n");
        for (Object e : (List<?>) v) {
          sb.append(indent).append("    ");
          if (e instanceof RecordedObject && depth < 2) {
            ((RecordedObject) e).format(sb, indent + "    ", depth + 1);
          } else {
            sb.append(e);
          }
          sb.append('\n');
        }
        sb.append(indent).append("  ]");
      } else if (v instanceof String) {
        sb.append('"').append(v).append('"');
      } else {
        sb.append(v);
      }
      sb.append('\n');
    }
    sb.append(indent).append('}');
  }
}
