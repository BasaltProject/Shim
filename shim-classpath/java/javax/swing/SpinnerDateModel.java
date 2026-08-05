/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.swing;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class SpinnerDateModel extends AbstractSpinnerModel implements Serializable {
  private Comparable<Date> start;
  private Comparable<Date> end;
  private Calendar value;
  private int calendarField;

  private boolean calendarFieldOK(int calendarField) {
    switch (calendarField) {
      case Calendar.ERA:
      case Calendar.YEAR:
      case Calendar.MONTH:
      case Calendar.WEEK_OF_YEAR:
      case Calendar.WEEK_OF_MONTH:
      case Calendar.DAY_OF_MONTH:
      case Calendar.DAY_OF_YEAR:
      case Calendar.DAY_OF_WEEK:
      case Calendar.DAY_OF_WEEK_IN_MONTH:
      case Calendar.AM_PM:
      case Calendar.HOUR:
      case Calendar.HOUR_OF_DAY:
      case Calendar.MINUTE:
      case Calendar.SECOND:
      case Calendar.MILLISECOND:
        return true;
      default:
        return false;
    }
  }

  public SpinnerDateModel(Date value, Comparable<Date> start, Comparable<Date> end, int calendarField) {
    if (value == null) {
      throw new IllegalArgumentException("value is null");
    }
    if (!calendarFieldOK(calendarField)) {
      throw new IllegalArgumentException("invalid calendarField");
    }
    if (!(((start == null) || (start.compareTo(value) <= 0)) && ((end == null) || (end.compareTo(value) >= 0)))) {
      throw new IllegalArgumentException("(start <= value <= end) is false");
    }
    this.value = Calendar.getInstance();
    this.start = start;
    this.end = end;
    this.calendarField = calendarField;
    this.value.setTime(value);
  }

  public SpinnerDateModel() { this(new Date(), null, null, Calendar.DAY_OF_MONTH); }

  public void setStart(Comparable<Date> start) {
    if ((start == null) ? (this.start != null) : !start.equals(this.start)) {
      this.start = start;
      fireStateChanged();
    }
  }

  public Comparable<Date> getStart() { return start; }

  public void setEnd(Comparable<Date> end) {
    if ((end == null) ? (this.end != null) : !end.equals(this.end)) {
      this.end = end;
      fireStateChanged();
    }
  }

  public Comparable<Date> getEnd() { return end; }

  public void setCalendarField(int calendarField) {
    if (!calendarFieldOK(calendarField)) {
      throw new IllegalArgumentException("invalid calendarField");
    }
    if (calendarField != this.calendarField) {
      this.calendarField = calendarField;
      fireStateChanged();
    }
  }

  public int getCalendarField() { return calendarField; }

  private Date incrValue(int dir) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(value.getTime());
    cal.add(calendarField, dir);
    Date next = cal.getTime();
    if ((end == null) || (end.compareTo(next) >= 0)) {
      if ((start == null) || (start.compareTo(next) <= 0)) {
        return next;
      }
    }
    return null;
  }

  public Object getNextValue() { return incrValue(+1); }

  public Object getPreviousValue() { return incrValue(-1); }

  public Date getDate() { return value.getTime(); }

  public Object getValue() { return value.getTime(); }

  public void setValue(Object value) {
    if ((value == null) || !(value instanceof Date)) {
      throw new IllegalArgumentException("illegal value");
    }
    if (!value.equals(this.value.getTime())) {
      this.value.setTime((Date) value);
      fireStateChanged();
    }
  }
}
