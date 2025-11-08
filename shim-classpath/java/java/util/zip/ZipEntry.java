/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.zip;

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ZipEntry implements Cloneable {
  public static final int STORED = 0;
  public static final int DEFLATED = 8;

  static final long DOSTIME_BEFORE_1980 = (1 << 21) | (1 << 16);
  static final long UPPER_DOSTIME_BOUND = 128L * 365 * 24 * 60 * 60 * 1000;
  static final long UPPER_UNIXTIME_BOUND = 0x7fffffffL;

  String name;
  long xdostime = -1;
  FileTime mtime;
  FileTime atime;
  FileTime ctime;
  long crc = -1;
  long size = -1;
  long csize = -1;
  int method = -1;
  int flag = 0;
  byte[] extra;
  String comment;

  public ZipEntry(String name) {
    Objects.requireNonNull(name, "name");
    if (name.length() > 0xFFFF / 3 && utf8Length(name) > 0xFFFF) {
      throw new IllegalArgumentException("entry name too long");
    }
    this.name = name;
  }

  ZipEntry(String name, boolean unchecked) {
    this.name = name;
  }

  public ZipEntry(ZipEntry e) {
    Objects.requireNonNull(e, "entry");
    name = e.name;
    xdostime = e.xdostime;
    mtime = e.mtime;
    atime = e.atime;
    ctime = e.ctime;
    crc = e.crc;
    size = e.size;
    csize = e.csize;
    method = e.method;
    flag = e.flag;
    extra = e.extra;
    comment = e.comment;
  }

  private static int utf8Length(String s) {
    return s.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
  }

  public String getName() {
    return name;
  }

  public void setTime(long time) {
    this.xdostime = javaToExtendedDosTime(time);
    if (xdostime != DOSTIME_BEFORE_1980 && time <= UPPER_DOSTIME_BOUND) {
      this.mtime = null;
    } else {
      this.mtime = FileTime.from(time, TimeUnit.MILLISECONDS);
    }
  }

  public long getTime() {
    if (mtime != null) {
      return mtime.toMillis();
    }
    return xdostime != -1 ? extendedDosToJavaTime(xdostime) : -1;
  }

  public void setTimeLocal(LocalDateTime time) {
    int year = time.getYear() - 1980;
    if (year < 0) {
      this.xdostime = DOSTIME_BEFORE_1980;
    } else {
      this.xdostime = ((year << 25 | time.getMonthValue() << 21 | time.getDayOfMonth() << 16
          | time.getHour() << 11 | time.getMinute() << 5 | time.getSecond() >> 1) & 0xffffffffL)
          + ((long) (((time.getSecond() & 1) * 1000) + time.getNano() / 1000_000) << 32);
    }
    if (xdostime != DOSTIME_BEFORE_1980 && year <= 0x7f) {
      this.mtime = null;
    } else {
      this.mtime = FileTime.from(ZonedDateTimeHolder.toInstant(time));
    }
  }

  public LocalDateTime getTimeLocal() {
    if (mtime != null) {
      return LocalDateTime.ofInstant(mtime.toInstant(), ZoneId.systemDefault());
    }
    int ms = (int) (xdostime >> 32);
    return LocalDateTime.of(((int) (xdostime >> 25) & 0x7f) + 1980, (int) (xdostime >> 21) & 0x0f,
        (int) (xdostime >> 16) & 0x1f, (int) (xdostime >> 11) & 0x1f, (int) (xdostime >> 5) & 0x3f,
        (int) (xdostime << 1) & 0x3e).plusNanos(ms * 1_000_000L);
  }

  private static final class ZonedDateTimeHolder {
    static Instant toInstant(LocalDateTime t) {
      return t.atZone(ZoneId.systemDefault()).toInstant();
    }
  }

  public ZipEntry setLastModifiedTime(FileTime time) {
    this.mtime = Objects.requireNonNull(time, "lastModifiedTime");
    this.xdostime = javaToExtendedDosTime(time.to(TimeUnit.MILLISECONDS));
    return this;
  }

  public FileTime getLastModifiedTime() {
    if (mtime != null) {
      return mtime;
    }
    if (xdostime == -1) {
      return null;
    }
    return FileTime.from(getTime(), TimeUnit.MILLISECONDS);
  }

  public ZipEntry setLastAccessTime(FileTime time) {
    this.atime = Objects.requireNonNull(time, "lastAccessTime");
    return this;
  }

  public FileTime getLastAccessTime() {
    return atime;
  }

  public ZipEntry setCreationTime(FileTime time) {
    this.ctime = Objects.requireNonNull(time, "creationTime");
    return this;
  }

  public FileTime getCreationTime() {
    return ctime;
  }

  public void setSize(long size) {
    if (size < 0) {
      throw new IllegalArgumentException("invalid entry size");
    }
    this.size = size;
  }

  public long getSize() {
    return size;
  }

  public long getCompressedSize() {
    return csize;
  }

  public void setCompressedSize(long csize) {
    this.csize = csize;
  }

  public void setCrc(long crc) {
    if (crc < 0 || crc > 0xFFFFFFFFL) {
      throw new IllegalArgumentException("invalid entry crc-32");
    }
    this.crc = crc;
  }

  public long getCrc() {
    return crc;
  }

  public void setMethod(int method) {
    if (method != STORED && method != DEFLATED) {
      throw new IllegalArgumentException("invalid compression method");
    }
    this.method = method;
  }

  public int getMethod() {
    return method;
  }

  public void setExtra(byte[] extra) {
    setExtra0(extra, false);
  }

  void setExtra0(byte[] extra, boolean doZIP64) {
    if (extra != null) {
      if (extra.length > 0xFFFF) {
        throw new IllegalArgumentException("invalid extra field length");
      }
      int off = 0;
      int len = extra.length;
      while (off + 4 < len) {
        int tag = get16(extra, off);
        int sz = get16(extra, off + 2);
        off += 4;
        int end = off + sz;
        if (end > len) {
          break;
        }
        if (tag == 0x5455) {
          int flags = extra[off] & 0xff;
          int pos = off + 1;
          if ((flags & 1) != 0 && pos + 4 <= end) {
            mtime = FileTime.from(get32(extra, pos), TimeUnit.SECONDS);
            pos += 4;
          }
          if ((flags & 2) != 0 && pos + 4 <= end) {
            atime = FileTime.from(get32(extra, pos), TimeUnit.SECONDS);
            pos += 4;
          }
          if ((flags & 4) != 0 && pos + 4 <= end) {
            ctime = FileTime.from(get32(extra, pos), TimeUnit.SECONDS);
          }
        } else if (tag == 0x000a && sz >= 32) {
          int pos = off + 4;
          if (get16(extra, pos) == 1 && get16(extra, pos + 2) == 24) {
            mtime = winTimeToFileTime(get64(extra, pos + 4));
            atime = winTimeToFileTime(get64(extra, pos + 12));
            ctime = winTimeToFileTime(get64(extra, pos + 20));
          }
        }
        off = end;
      }
    }
    this.extra = extra;
  }

  static int get16(byte[] b, int off) {
    return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8);
  }

  static long get32(byte[] b, int off) {
    return (get16(b, off) | ((long) get16(b, off + 2) << 16)) & 0xffffffffL;
  }

  static long get64(byte[] b, int off) {
    return get32(b, off) | (get32(b, off + 4) << 32);
  }

  static FileTime winTimeToFileTime(long wtime) {
    return FileTime.from(wtime / 10 + -11644473600000000L, TimeUnit.MICROSECONDS);
  }

  public byte[] getExtra() {
    return extra;
  }

  public void setComment(String comment) {
    if (comment != null && comment.length() > 0xFFFF / 3 && utf8Length(comment) > 0xFFFF) {
      throw new IllegalArgumentException("entry comment too long");
    }
    this.comment = comment;
  }

  public String getComment() {
    return comment;
  }

  public boolean isDirectory() {
    return getName().endsWith("/");
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public Object clone() {
    try {
      ZipEntry e = (ZipEntry) super.clone();
      e.extra = extra == null ? null : extra.clone();
      return e;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  static long javaToExtendedDosTime(long time) {
    if (time < 0) {
      return DOSTIME_BEFORE_1980;
    }
    long dostime = javaToDosTime(time);
    return dostime != DOSTIME_BEFORE_1980 ? dostime + ((time % 2000) << 32) : DOSTIME_BEFORE_1980;
  }

  static long javaToDosTime(long time) {
    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    int year = ldt.getYear() - 1980;
    if (year < 0) {
      return DOSTIME_BEFORE_1980;
    }
    return (year << 25 | ldt.getMonthValue() << 21 | ldt.getDayOfMonth() << 16 | ldt.getHour() << 11
        | ldt.getMinute() << 5 | ldt.getSecond() >> 1) & 0xffffffffL;
  }

  static long extendedDosToJavaTime(long xdostime) {
    long time = dosToJavaTime(xdostime);
    return time + (xdostime >> 32);
  }

  static long dosToJavaTime(long dtime) {
    LocalDateTime ldt = LocalDateTime.of(((int) (dtime >> 25) & 0x7f) + 1980, (int) (dtime >> 21) & 0x0f,
        (int) (dtime >> 16) & 0x1f, (int) (dtime >> 11) & 0x1f, (int) (dtime >> 5) & 0x3f,
        (int) (dtime << 1) & 0x3e);
    ZoneOffset off = ZoneId.systemDefault().getRules().getOffset(ldt);
    return TimeUnit.SECONDS.toMillis(ldt.toEpochSecond(off));
  }
}
