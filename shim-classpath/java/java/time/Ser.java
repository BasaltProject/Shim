/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.time;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;

final class Ser implements Externalizable {
  private static final long serialVersionUID = -7683839454370182990L;

  static final byte DURATION_TYPE = 1;
  static final byte INSTANT_TYPE = 2;
  static final byte LOCAL_DATE_TYPE = 3;
  static final byte LOCAL_TIME_TYPE = 4;
  static final byte LOCAL_DATE_TIME_TYPE = 5;
  static final byte ZONE_DATE_TIME_TYPE = 6;
  static final byte ZONE_REGION_TYPE = 7;
  static final byte ZONE_OFFSET_TYPE = 8;
  static final byte OFFSET_TIME_TYPE = 9;
  static final byte OFFSET_DATE_TIME_TYPE = 10;
  static final byte YEAR_TYPE = 11;
  static final byte YEAR_MONTH_TYPE = 12;
  static final byte MONTH_DAY_TYPE = 13;
  static final byte PERIOD_TYPE = 14;

  private byte type;
  private Object object;

  public Ser() {
  }

  Ser(byte type, Object object) {
    this.type = type;
    this.object = object;
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    writeInternal(type, object, out);
  }

  static void writeInternal(byte type, Object object, ObjectOutput out) throws IOException {
    out.writeByte(type);
    writeBody(type, object, out);
  }

  private static void writeBody(byte type, Object object, DataOutput out) throws IOException {
    switch (type) {
      case DURATION_TYPE: {
        Duration d = (Duration) object;
        out.writeLong(d.getSeconds());
        out.writeInt(d.getNano());
        break;
      }
      case INSTANT_TYPE: {
        Instant i = (Instant) object;
        out.writeLong(i.getEpochSecond());
        out.writeInt(i.getNano());
        break;
      }
      case LOCAL_DATE_TYPE:
        writeDate((LocalDate) object, out);
        break;
      case LOCAL_TIME_TYPE:
        writeTime((LocalTime) object, out);
        break;
      case LOCAL_DATE_TIME_TYPE: {
        LocalDateTime dt = (LocalDateTime) object;
        writeDate(dt.toLocalDate(), out);
        writeTime(dt.toLocalTime(), out);
        break;
      }
      case ZONE_DATE_TIME_TYPE: {
        ZonedDateTime z = (ZonedDateTime) object;
        writeDate(z.toLocalDate(), out);
        writeTime(z.toLocalTime(), out);
        writeOffset(z.getOffset(), out);
        writeZone(z.getZone(), out);
        break;
      }
      case ZONE_REGION_TYPE:
        out.writeUTF(((ZoneId) object).getId());
        break;
      case ZONE_OFFSET_TYPE:
        writeOffset((ZoneOffset) object, out);
        break;
      case OFFSET_TIME_TYPE: {
        OffsetTime o = (OffsetTime) object;
        writeTime(o.toLocalTime(), out);
        writeOffset(o.getOffset(), out);
        break;
      }
      case OFFSET_DATE_TIME_TYPE: {
        OffsetDateTime o = (OffsetDateTime) object;
        writeDate(o.toLocalDate(), out);
        writeTime(o.toLocalTime(), out);
        writeOffset(o.getOffset(), out);
        break;
      }
      case YEAR_TYPE:
        out.writeInt(((Year) object).getValue());
        break;
      case YEAR_MONTH_TYPE: {
        YearMonth ym = (YearMonth) object;
        out.writeInt(ym.getYear());
        out.writeByte(ym.getMonthValue());
        break;
      }
      case MONTH_DAY_TYPE: {
        MonthDay md = (MonthDay) object;
        out.writeByte(md.getMonthValue());
        out.writeByte(md.getDayOfMonth());
        break;
      }
      case PERIOD_TYPE: {
        Period p = (Period) object;
        out.writeInt(p.getYears());
        out.writeInt(p.getMonths());
        out.writeInt(p.getDays());
        break;
      }
      default:
        throw new InvalidClassException("Unknown serialized type");
    }
  }

  private static void writeDate(LocalDate d, DataOutput out) throws IOException {
    out.writeInt(d.getYear());
    out.writeByte(d.getMonthValue());
    out.writeByte(d.getDayOfMonth());
  }

  private static void writeTime(LocalTime t, DataOutput out) throws IOException {
    int hour = t.getHour();
    int minute = t.getMinute();
    int second = t.getSecond();
    int nano = t.getNano();
    if (nano == 0) {
      if (second == 0) {
        if (minute == 0) {
          out.writeByte(~hour);
        } else {
          out.writeByte(hour);
          out.writeByte(~minute);
        }
      } else {
        out.writeByte(hour);
        out.writeByte(minute);
        out.writeByte(~second);
      }
    } else {
      out.writeByte(hour);
      out.writeByte(minute);
      out.writeByte(second);
      out.writeInt(nano);
    }
  }

  private static void writeOffset(ZoneOffset o, DataOutput out) throws IOException {
    final int offsetSecs = o.getTotalSeconds();
    int offsetByte = offsetSecs % 900 == 0 ? offsetSecs / 900 : 127;
    out.writeByte(offsetByte);
    if (offsetByte == 127) {
      out.writeInt(offsetSecs);
    }
  }

  private static void writeZone(ZoneId zone, DataOutput out) throws IOException {
    if (zone instanceof ZoneOffset) {
      out.writeByte(ZONE_OFFSET_TYPE);
      writeOffset((ZoneOffset) zone, out);
    } else {
      out.writeByte(ZONE_REGION_TYPE);
      out.writeUTF(zone.getId());
    }
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    type = in.readByte();
    object = readInternal(type, in);
  }

  static Object read(ObjectInput in) throws IOException, ClassNotFoundException {
    byte type = in.readByte();
    return readInternal(type, in);
  }

  private static Object readInternal(byte type, DataInput in) throws IOException {
    switch (type) {
      case DURATION_TYPE: {
        long s = in.readLong();
        int n = in.readInt();
        return Duration.ofSeconds(s, n);
      }
      case INSTANT_TYPE: {
        long s = in.readLong();
        int n = in.readInt();
        return Instant.ofEpochSecond(s, n);
      }
      case LOCAL_DATE_TYPE:
        return readDate(in);
      case LOCAL_TIME_TYPE:
        return readTime(in);
      case LOCAL_DATE_TIME_TYPE: {
        LocalDate d = readDate(in);
        return LocalDateTime.of(d, readTime(in));
      }
      case ZONE_DATE_TIME_TYPE: {
        LocalDate d = readDate(in);
        LocalTime t = readTime(in);
        ZoneOffset off = readOffset(in);
        ZoneId zone = readZone(in);
        return ZonedDateTime.ofLenient(LocalDateTime.of(d, t), off, zone);
      }
      case ZONE_REGION_TYPE:
        return ZoneId.of(in.readUTF());
      case ZONE_OFFSET_TYPE:
        return readOffset(in);
      case OFFSET_TIME_TYPE: {
        LocalTime t = readTime(in);
        return OffsetTime.of(t, readOffset(in));
      }
      case OFFSET_DATE_TIME_TYPE: {
        LocalDate d = readDate(in);
        LocalTime t = readTime(in);
        return OffsetDateTime.of(LocalDateTime.of(d, t), readOffset(in));
      }
      case YEAR_TYPE:
        return Year.of(in.readInt());
      case YEAR_MONTH_TYPE: {
        int y = in.readInt();
        return YearMonth.of(y, in.readByte());
      }
      case MONTH_DAY_TYPE: {
        int m = in.readByte();
        return MonthDay.of(m, in.readByte());
      }
      case PERIOD_TYPE: {
        int y = in.readInt();
        int m = in.readInt();
        return Period.of(y, m, in.readInt());
      }
      default:
        throw new StreamCorruptedException("Unknown serialized type");
    }
  }

  private static LocalDate readDate(DataInput in) throws IOException {
    int year = in.readInt();
    int month = in.readByte();
    int day = in.readByte();
    return LocalDate.of(year, month, day);
  }

  private static LocalTime readTime(DataInput in) throws IOException {
    int hour = in.readByte();
    int minute = 0;
    int second = 0;
    int nano = 0;
    if (hour < 0) {
      hour = ~hour;
    } else {
      minute = in.readByte();
      if (minute < 0) {
        minute = ~minute;
      } else {
        second = in.readByte();
        if (second < 0) {
          second = ~second;
        } else {
          nano = in.readInt();
        }
      }
    }
    return LocalTime.of(hour, minute, second, nano);
  }

  private static ZoneOffset readOffset(DataInput in) throws IOException {
    int offsetByte = in.readByte();
    return offsetByte == 127 ? ZoneOffset.ofTotalSeconds(in.readInt()) : ZoneOffset.ofTotalSeconds(offsetByte * 900);
  }

  private static ZoneId readZone(DataInput in) throws IOException {
    byte t = in.readByte();
    if (t == ZONE_OFFSET_TYPE) {
      return readOffset(in);
    }
    return ZoneId.of(in.readUTF());
  }

  private Object readResolve() {
    return object;
  }
}
