/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.time.zone;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ZoneRules implements Serializable {
  private static final long serialVersionUID = 3044319355680032515L;

  private static final int LAST_CACHED_YEAR = 2100;

  private final long[] standardTransitions;
  private final ZoneOffset[] standardOffsets;
  private final long[] savingsInstantTransitions;
  private final LocalDateTime[] savingsLocalTransitions;
  private final ZoneOffset[] wallOffsets;
  private final ZoneOffsetTransitionRule[] lastRules;
  private final transient ConcurrentMap<Integer, ZoneOffsetTransition[]> lastRulesCache =
      new ConcurrentHashMap<Integer, ZoneOffsetTransition[]>();

  private static final long[] EMPTY_LONG_ARRAY = new long[0];
  private static final ZoneOffsetTransitionRule[] EMPTY_LASTRULES = new ZoneOffsetTransitionRule[0];
  private static final LocalDateTime[] EMPTY_LDT_ARRAY = new LocalDateTime[0];

  public static ZoneRules of(ZoneOffset baseStandardOffset, ZoneOffset baseWallOffset,
      List<ZoneOffsetTransition> standardOffsetTransitionList, List<ZoneOffsetTransition> transitionList,
      List<ZoneOffsetTransitionRule> lastRules) {
    Objects.requireNonNull(baseStandardOffset, "baseStandardOffset");
    Objects.requireNonNull(baseWallOffset, "baseWallOffset");
    Objects.requireNonNull(standardOffsetTransitionList, "standardOffsetTransitionList");
    Objects.requireNonNull(transitionList, "transitionList");
    Objects.requireNonNull(lastRules, "lastRules");
    return new ZoneRules(baseStandardOffset, baseWallOffset, standardOffsetTransitionList, transitionList, lastRules);
  }

  public static ZoneRules of(ZoneOffset offset) {
    Objects.requireNonNull(offset, "offset");
    return new ZoneRules(offset);
  }

  private ZoneRules(ZoneOffset baseStandardOffset, ZoneOffset baseWallOffset,
      List<ZoneOffsetTransition> standardOffsetTransitionList, List<ZoneOffsetTransition> transitionList,
      List<ZoneOffsetTransitionRule> lastRules) {
    super();
    this.standardTransitions = new long[standardOffsetTransitionList.size()];
    this.standardOffsets = new ZoneOffset[standardOffsetTransitionList.size() + 1];
    this.standardOffsets[0] = baseStandardOffset;
    for (int i = 0; i < standardOffsetTransitionList.size(); i++) {
      this.standardTransitions[i] = standardOffsetTransitionList.get(i).toEpochSecond();
      this.standardOffsets[i + 1] = standardOffsetTransitionList.get(i).getOffsetAfter();
    }
    List<LocalDateTime> localTransitionList = new ArrayList<LocalDateTime>();
    List<ZoneOffset> localTransitionOffsetList = new ArrayList<ZoneOffset>();
    localTransitionOffsetList.add(baseWallOffset);
    for (ZoneOffsetTransition trans : transitionList) {
      if (trans.isGap()) {
        localTransitionList.add(trans.getDateTimeBefore());
        localTransitionList.add(trans.getDateTimeAfter());
      } else {
        localTransitionList.add(trans.getDateTimeAfter());
        localTransitionList.add(trans.getDateTimeBefore());
      }
      localTransitionOffsetList.add(trans.getOffsetAfter());
    }
    this.savingsLocalTransitions = localTransitionList.toArray(new LocalDateTime[localTransitionList.size()]);
    this.wallOffsets = localTransitionOffsetList.toArray(new ZoneOffset[localTransitionOffsetList.size()]);
    this.savingsInstantTransitions = new long[transitionList.size()];
    for (int i = 0; i < transitionList.size(); i++) {
      this.savingsInstantTransitions[i] = transitionList.get(i).toEpochSecond();
    }
    if (lastRules.size() > 16) {
      throw new IllegalArgumentException("Too many transition rules");
    }
    this.lastRules = lastRules.toArray(new ZoneOffsetTransitionRule[lastRules.size()]);
  }

  private ZoneRules(ZoneOffset offset) {
    this.standardOffsets = new ZoneOffset[1];
    this.standardOffsets[0] = offset;
    this.standardTransitions = EMPTY_LONG_ARRAY;
    this.savingsInstantTransitions = EMPTY_LONG_ARRAY;
    this.savingsLocalTransitions = EMPTY_LDT_ARRAY;
    this.wallOffsets = standardOffsets;
    this.lastRules = EMPTY_LASTRULES;
  }

  public boolean isFixedOffset() {
    return standardOffsets[0].equals(wallOffsets[0]) && standardTransitions.length == 0
        && savingsInstantTransitions.length == 0 && lastRules.length == 0;
  }

  private long lastSavingsSecond() {
    return savingsInstantTransitions.length == 0 ? Long.MIN_VALUE
        : savingsInstantTransitions[savingsInstantTransitions.length - 1];
  }

  public ZoneOffset getOffset(Instant instant) {
    if (savingsInstantTransitions.length == 0 && lastRules.length == 0) {
      return wallOffsets[0];
    }
    long epochSec = instant.getEpochSecond();
    if (lastRules.length > 0 && epochSec > lastSavingsSecond()) {
      int year = findYear(epochSec, wallOffsets[wallOffsets.length - 1]);
      ZoneOffsetTransition[] transArray = findTransitionArray(year);
      ZoneOffsetTransition trans = null;
      for (int i = 0; i < transArray.length; i++) {
        trans = transArray[i];
        if (epochSec < trans.toEpochSecond()) {
          return trans.getOffsetBefore();
        }
      }
      return trans.getOffsetAfter();
    }
    int index = Arrays.binarySearch(savingsInstantTransitions, epochSec);
    if (index < 0) {
      index = -index - 2;
    }
    return wallOffsets[index + 1];
  }

  public ZoneOffset getOffset(LocalDateTime localDateTime) {
    Object info = getOffsetInfo(localDateTime);
    if (info instanceof ZoneOffsetTransition) {
      return ((ZoneOffsetTransition) info).getOffsetBefore();
    }
    return (ZoneOffset) info;
  }

  public List<ZoneOffset> getValidOffsets(LocalDateTime localDateTime) {
    Object info = getOffsetInfo(localDateTime);
    if (info instanceof ZoneOffsetTransition) {
      return ((ZoneOffsetTransition) info).getValidOffsets();
    }
    return Collections.singletonList((ZoneOffset) info);
  }

  public ZoneOffsetTransition getTransition(LocalDateTime localDateTime) {
    Object info = getOffsetInfo(localDateTime);
    return (info instanceof ZoneOffsetTransition ? (ZoneOffsetTransition) info : null);
  }

  private Object getOffsetInfo(LocalDateTime dt) {
    if (savingsInstantTransitions.length == 0 && lastRules.length == 0) {
      return wallOffsets[0];
    }
    if (lastRules.length > 0
        && (savingsLocalTransitions.length == 0
            || dt.isAfter(savingsLocalTransitions[savingsLocalTransitions.length - 1]))) {
      ZoneOffsetTransition[] transArray = findTransitionArray(dt.getYear());
      Object info = null;
      for (ZoneOffsetTransition trans : transArray) {
        info = findOffsetInfo(dt, trans);
        if (info instanceof ZoneOffsetTransition || info.equals(trans.getOffsetBefore())) {
          return info;
        }
      }
      return info;
    }
    int index = Arrays.binarySearch(savingsLocalTransitions, dt);
    if (index == -1) {
      return wallOffsets[0];
    }
    if (index < 0) {
      index = -index - 2;
    } else if (index < savingsLocalTransitions.length - 1
        && savingsLocalTransitions[index].equals(savingsLocalTransitions[index + 1])) {
      index++;
    }
    if ((index & 1) == 0) {
      LocalDateTime dtBefore = savingsLocalTransitions[index];
      LocalDateTime dtAfter = savingsLocalTransitions[index + 1];
      ZoneOffset offsetBefore = wallOffsets[index / 2];
      ZoneOffset offsetAfter = wallOffsets[index / 2 + 1];
      if (offsetAfter.getTotalSeconds() > offsetBefore.getTotalSeconds()) {
        return new ZoneOffsetTransition(dtBefore, offsetBefore, offsetAfter);
      } else {
        return new ZoneOffsetTransition(dtAfter, offsetBefore, offsetAfter);
      }
    } else {
      return wallOffsets[index / 2 + 1];
    }
  }

  private Object findOffsetInfo(LocalDateTime dt, ZoneOffsetTransition trans) {
    LocalDateTime localTransition = trans.getDateTimeBefore();
    if (trans.isGap()) {
      if (dt.isBefore(localTransition)) {
        return trans.getOffsetBefore();
      }
      if (dt.isBefore(trans.getDateTimeAfter())) {
        return trans;
      } else {
        return trans.getOffsetAfter();
      }
    } else {
      if (!dt.isBefore(localTransition)) {
        return trans.getOffsetAfter();
      }
      if (dt.isBefore(trans.getDateTimeAfter())) {
        return trans.getOffsetBefore();
      } else {
        return trans;
      }
    }
  }

  private ZoneOffsetTransition[] findTransitionArray(int year) {
    Integer yearObj = year;
    ZoneOffsetTransition[] transArray = lastRulesCache.get(yearObj);
    if (transArray != null) {
      return transArray;
    }
    ZoneOffsetTransitionRule[] ruleArray = lastRules;
    transArray = new ZoneOffsetTransition[ruleArray.length];
    for (int i = 0; i < ruleArray.length; i++) {
      transArray[i] = ruleArray[i].createTransition(year);
    }
    if (year < LAST_CACHED_YEAR) {
      lastRulesCache.putIfAbsent(yearObj, transArray);
    }
    return transArray;
  }

  public ZoneOffset getStandardOffset(Instant instant) {
    if (standardTransitions.length == 0) {
      return standardOffsets[0];
    }
    long epochSec = instant.getEpochSecond();
    int index = Arrays.binarySearch(standardTransitions, epochSec);
    if (index < 0) {
      index = -index - 2;
    }
    return standardOffsets[index + 1];
  }

  public Duration getDaylightSavings(Instant instant) {
    if (savingsInstantTransitions.length == 0 && lastRules.length == 0) {
      return Duration.ZERO;
    }
    ZoneOffset standardOffset = getStandardOffset(instant);
    ZoneOffset actualOffset = getOffset(instant);
    return Duration.ofSeconds(actualOffset.getTotalSeconds() - standardOffset.getTotalSeconds());
  }

  public boolean isDaylightSavings(Instant instant) {
    return (!getStandardOffset(instant).equals(getOffset(instant)));
  }

  public boolean isValidOffset(LocalDateTime localDateTime, ZoneOffset offset) {
    return getValidOffsets(localDateTime).contains(offset);
  }

  public ZoneOffsetTransition nextTransition(Instant instant) {
    if (savingsInstantTransitions.length == 0 && lastRules.length == 0) {
      return null;
    }
    long epochSec = instant.getEpochSecond();
    if (lastRules.length > 0 && (savingsInstantTransitions.length == 0 || epochSec >= lastSavingsSecond())) {
      if (savingsInstantTransitions.length == 0) {
        epochSec = Math.max(epochSec, Long.MIN_VALUE + 1);
      }
      int year = findYear(epochSec, wallOffsets[wallOffsets.length - 1]);
      ZoneOffsetTransition[] transArray = findTransitionArray(year);
      for (ZoneOffsetTransition trans : transArray) {
        if (epochSec < trans.toEpochSecond()) {
          return trans;
        }
      }
      if (year < Year.MAX_VALUE) {
        transArray = findTransitionArray(year + 1);
        return transArray[0];
      }
      return null;
    }
    if (savingsInstantTransitions.length == 0) {
      return null;
    }
    if (epochSec >= lastSavingsSecond()) {
      return null;
    }
    int index = Arrays.binarySearch(savingsInstantTransitions, epochSec);
    if (index < 0) {
      index = -index - 1;
    } else {
      index += 1;
    }
    return new ZoneOffsetTransition(savingsInstantTransitions[index], wallOffsets[index], wallOffsets[index + 1]);
  }

  public ZoneOffsetTransition previousTransition(Instant instant) {
    if (savingsInstantTransitions.length == 0 && lastRules.length == 0) {
      return null;
    }
    long epochSec = instant.getEpochSecond();
    if (instant.getNano() > 0 && epochSec < Long.MAX_VALUE) {
      epochSec += 1;
    }
    long lastHistoric = lastSavingsSecond();
    if (lastRules.length > 0 && epochSec > lastHistoric) {
      ZoneOffset lastHistoricOffset = wallOffsets[wallOffsets.length - 1];
      int year = findYear(epochSec, lastHistoricOffset);
      ZoneOffsetTransition[] transArray = findTransitionArray(year);
      for (int i = transArray.length - 1; i >= 0; i--) {
        if (epochSec > transArray[i].toEpochSecond()) {
          return transArray[i];
        }
      }
      int lowerYear = savingsInstantTransitions.length == 0 ? Year.MIN_VALUE
          : findYear(lastHistoric, lastHistoricOffset);
      if (--year > lowerYear) {
        transArray = findTransitionArray(year);
        return transArray[transArray.length - 1];
      }
    }
    if (savingsInstantTransitions.length == 0) {
      return null;
    }
    int index = Arrays.binarySearch(savingsInstantTransitions, epochSec);
    if (index < 0) {
      index = -index - 1;
    }
    if (index <= 0) {
      return null;
    }
    return new ZoneOffsetTransition(savingsInstantTransitions[index - 1], wallOffsets[index - 1], wallOffsets[index]);
  }

  private int findYear(long epochSecond, ZoneOffset offset) {
    long localSecond = epochSecond + offset.getTotalSeconds();
    long localEpochDay = Math.floorDiv(localSecond, 86400);
    return LocalDate.ofEpochDay(localEpochDay).getYear();
  }

  public List<ZoneOffsetTransition> getTransitions() {
    List<ZoneOffsetTransition> list = new ArrayList<ZoneOffsetTransition>();
    for (int i = 0; i < savingsInstantTransitions.length; i++) {
      list.add(new ZoneOffsetTransition(savingsInstantTransitions[i], wallOffsets[i], wallOffsets[i + 1]));
    }
    return Collections.unmodifiableList(list);
  }

  public List<ZoneOffsetTransitionRule> getTransitionRules() {
    return Collections.unmodifiableList(Arrays.asList(lastRules));
  }

  @Override
  public boolean equals(Object otherRules) {
    if (this == otherRules) {
      return true;
    }
    if (otherRules instanceof ZoneRules) {
      ZoneRules other = (ZoneRules) otherRules;
      return Arrays.equals(standardTransitions, other.standardTransitions)
          && Arrays.equals(standardOffsets, other.standardOffsets)
          && Arrays.equals(savingsInstantTransitions, other.savingsInstantTransitions)
          && Arrays.equals(wallOffsets, other.wallOffsets) && Arrays.equals(lastRules, other.lastRules);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(standardTransitions) ^ Arrays.hashCode(standardOffsets)
        ^ Arrays.hashCode(savingsInstantTransitions) ^ Arrays.hashCode(wallOffsets) ^ Arrays.hashCode(lastRules);
  }

  @Override
  public String toString() {
    return "ZoneRules[currentStandardOffset=" + standardOffsets[standardOffsets.length - 1] + "]";
  }
}
