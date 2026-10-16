/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.reflect;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public enum AccessFlag {
  PUBLIC(0x0001, true, Location.CLASS, Location.FIELD, Location.METHOD, Location.INNER_CLASS),
  PRIVATE(0x0002, true, Location.FIELD, Location.METHOD, Location.INNER_CLASS),
  PROTECTED(0x0004, true, Location.FIELD, Location.METHOD, Location.INNER_CLASS),
  STATIC(0x0008, true, Location.FIELD, Location.METHOD, Location.INNER_CLASS),
  FINAL(0x0010, true, Location.CLASS, Location.FIELD, Location.METHOD, Location.INNER_CLASS, Location.METHOD_PARAMETER),
  SUPER(0x0020, false, Location.CLASS),
  OPEN(0x0020, false, Location.MODULE),
  TRANSITIVE(0x0020, false, Location.MODULE_REQUIRES),
  SYNCHRONIZED(0x0020, true, Location.METHOD),
  STATIC_PHASE(0x0040, false, Location.MODULE_REQUIRES),
  VOLATILE(0x0040, true, Location.FIELD),
  BRIDGE(0x0040, false, Location.METHOD),
  TRANSIENT(0x0080, true, Location.FIELD),
  VARARGS(0x0080, false, Location.METHOD),
  NATIVE(0x0100, true, Location.METHOD),
  INTERFACE(0x0200, false, Location.CLASS, Location.INNER_CLASS),
  ABSTRACT(0x0400, true, Location.CLASS, Location.METHOD, Location.INNER_CLASS),
  STRICT(0x0800, true, Location.METHOD),
  SYNTHETIC(0x1000, false, Location.CLASS, Location.FIELD, Location.METHOD, Location.INNER_CLASS, Location.METHOD_PARAMETER, Location.MODULE, Location.MODULE_REQUIRES, Location.MODULE_EXPORTS, Location.MODULE_OPENS),
  ANNOTATION(0x2000, false, Location.CLASS, Location.INNER_CLASS),
  ENUM(0x4000, false, Location.CLASS, Location.FIELD, Location.INNER_CLASS),
  MANDATED(0x8000, false, Location.METHOD_PARAMETER, Location.MODULE, Location.MODULE_REQUIRES, Location.MODULE_EXPORTS, Location.MODULE_OPENS),
  MODULE(0x8000, false, Location.CLASS);

  public enum Location {
    CLASS(63025),
    FIELD(20703),
    METHOD(5631),
    INNER_CLASS(30239),
    METHOD_PARAMETER(36880),
    MODULE(36896),
    MODULE_REQUIRES(36960),
    MODULE_EXPORTS(36864),
    MODULE_OPENS(36864);

    private final int mask;

    Location(int mask) {
      this.mask = mask;
    }

    public int flagsMask() {
      return mask;
    }

    public int flagsMask(ClassFileFormatVersion cffv) {
      Objects.requireNonNull(cffv);
      if (this == METHOD) {
        if (cffv.compareTo(ClassFileFormatVersion.RELEASE_16) <= 0) {
          return 7679;
        }
        return 5631;
      }
      return mask;
    }

    public Set<AccessFlag> flags() {
      return flags(ClassFileFormatVersion.latest());
    }

    public Set<AccessFlag> flags(ClassFileFormatVersion cffv) {
      Objects.requireNonNull(cffv);
      Set<AccessFlag> s = EnumSet.noneOf(AccessFlag.class);
      for (AccessFlag f : AccessFlag.values()) {
        if (f.locations(cffv).contains(this)) {
          s.add(f);
        }
      }
      return Collections.unmodifiableSet(s);
    }
  }

  private final int mask;
  private final boolean sourceModifier;
  private final Set<Location> locations;

  AccessFlag(int mask, boolean sourceModifier, Location... locs) {
    this.mask = mask;
    this.sourceModifier = sourceModifier;
    Set<Location> s = EnumSet.noneOf(Location.class);
    for (Location loc : locs) {
      s.add(loc);
    }
    this.locations = Collections.unmodifiableSet(s);
  }

  public int mask() {
    return mask;
  }

  public boolean sourceModifier() {
    return sourceModifier;
  }

  public Set<Location> locations() {
    return locations;
  }

  public Set<Location> locations(ClassFileFormatVersion cffv) {
    Objects.requireNonNull(cffv);
    if (this == STRICT) {
      if (cffv.compareTo(ClassFileFormatVersion.RELEASE_16) > 0) {
        return Collections.emptySet();
      }
    }
    return locations;
  }

  public static Set<AccessFlag> maskToAccessFlags(int mask, Location location) {
    Objects.requireNonNull(location);
    int unmatched = mask & ~location.flagsMask();
    if (unmatched != 0) {
      throw new IllegalArgumentException("Unmatched bit position 0x" + Integer.toHexString(unmatched) + " for location " + location);
    }
    Set<AccessFlag> flags = EnumSet.noneOf(AccessFlag.class);
    for (AccessFlag flag : values()) {
      if (flag.locations().contains(location) && (mask & flag.mask()) != 0) {
        flags.add(flag);
      }
    }
    return Collections.unmodifiableSet(flags);
  }

  public static Set<AccessFlag> maskToAccessFlags(int mask, Location location, ClassFileFormatVersion cffv) {
    Objects.requireNonNull(location);
    Objects.requireNonNull(cffv);
    int unmatched = mask & ~location.flagsMask(cffv);
    if (unmatched != 0) {
      throw new IllegalArgumentException("Unmatched bit position 0x" + Integer.toHexString(unmatched) +
          " for location " + location + " for class file format " + cffv);
    }
    Set<AccessFlag> flags = EnumSet.noneOf(AccessFlag.class);
    for (AccessFlag flag : values()) {
      if (flag.locations(cffv).contains(location) && (mask & flag.mask()) != 0) {
        flags.add(flag);
      }
    }
    return Collections.unmodifiableSet(flags);
  }
}
