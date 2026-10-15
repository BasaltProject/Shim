/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ModuleDescriptor implements Comparable<ModuleDescriptor> {
  private final String name;
  private final boolean open;
  private final boolean automatic;
  private final Set<String> packages;
  private final Version version;
  private final String rawVersion;
  private final String mainClass;
  private final Set<Requires> requires;
  private final Set<Exports> exports;
  private final Set<Opens> opens;
  private final Set<Provides> provides;
  private final Set<String> uses;

  private ModuleDescriptor(
      String name,
      boolean open,
      boolean automatic,
      Set<String> packages,
      Version version,
      String rawVersion,
      String mainClass,
      Set<Requires> requires,
      Set<Exports> exports,
      Set<Opens> opens,
      Set<Provides> provides,
      Set<String> uses) {
    this.name = name;
    this.open = open;
    this.automatic = automatic;
    this.packages = packages != null ? Collections.unmodifiableSet(new HashSet<String>(packages)) : Collections.<String>emptySet();
    this.version = version;
    this.rawVersion = rawVersion;
    this.mainClass = mainClass;
    this.requires = requires != null ? Collections.unmodifiableSet(new HashSet<Requires>(requires)) : Collections.<Requires>emptySet();
    this.exports = exports != null ? Collections.unmodifiableSet(new HashSet<Exports>(exports)) : Collections.<Exports>emptySet();
    this.opens = opens != null ? Collections.unmodifiableSet(new HashSet<Opens>(opens)) : Collections.<Opens>emptySet();
    this.provides = provides != null ? Collections.unmodifiableSet(new HashSet<Provides>(provides)) : Collections.<Provides>emptySet();
    this.uses = uses != null ? Collections.unmodifiableSet(new HashSet<String>(uses)) : Collections.<String>emptySet();
  }

  public String name() {
    return name;
  }

  public boolean isOpen() {
    return open;
  }

  public boolean isAutomatic() {
    return automatic;
  }

  public Set<String> packages() {
    return packages;
  }

  public Optional<Version> version() {
    return Optional.ofNullable(version);
  }

  public Optional<String> rawVersion() {
    return Optional.ofNullable(rawVersion);
  }

  public Optional<String> mainClass() {
    return Optional.ofNullable(mainClass);
  }

  public Set<Requires> requires() {
    return requires;
  }

  public Set<Exports> exports() {
    return exports;
  }

  public Set<Opens> opens() {
    return opens;
  }

  public Set<Provides> provides() {
    return provides;
  }

  public Set<String> uses() {
    return uses;
  }

  public int compareTo(ModuleDescriptor that) {
    return this.name.compareTo(that.name);
  }

  @Override
  public boolean equals(Object ob) {
    if (this == ob) return true;
    if (!(ob instanceof ModuleDescriptor)) return false;
    ModuleDescriptor that = (ModuleDescriptor) ob;
    return Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  @Override
  public String toString() {
    return "module { name: " + name + " }";
  }

  public static Builder newModule(String name) {
    return new Builder(name, false);
  }

  public static Builder newOpenModule(String name) {
    return new Builder(name, true);
  }

  public static Builder newAutomaticModule(String name) {
    Builder b = new Builder(name, false);
    b.automatic = true;
    return b;
  }

  public static final class Version implements Comparable<Version> {
    private final String version;
    private final List<Object> sequence;

    private Version(String version) {
      this.version = version;
      this.sequence = parseSequence(version);
    }

    public static Version parse(String v) {
      if (v == null) {
        throw new IllegalArgumentException("Null version");
      }
      if (v.isEmpty()) {
        throw new IllegalArgumentException("Empty version");
      }
      return new Version(v);
    }

    private static List<Object> parseSequence(String v) {
      List<Object> list = new ArrayList<Object>();
      int len = v.length();
      int i = 0;
      while (i < len) {
        char c = v.charAt(i);
        if (Character.isDigit(c)) {
          int start = i;
          while (i < len && Character.isDigit(v.charAt(i))) {
            i++;
          }
          list.add(Long.parseLong(v.substring(start, i)));
        } else if (Character.isLetter(c)) {
          int start = i;
          while (i < len && Character.isLetter(v.charAt(i))) {
            i++;
          }
          list.add(v.substring(start, i));
        } else {
          i++;
        }
      }
      return list;
    }

    public int compareTo(Version that) {
      int s1 = this.sequence.size();
      int s2 = that.sequence.size();
      int min = Math.min(s1, s2);
      for (int i = 0; i < min; i++) {
        Object o1 = this.sequence.get(i);
        Object o2 = that.sequence.get(i);
        if (o1 instanceof Long && o2 instanceof Long) {
          int cmp = Long.compare(((Long) o1).longValue(), ((Long) o2).longValue());
          if (cmp != 0) return cmp;
        } else if (o1 instanceof String && o2 instanceof String) {
          int cmp = ((String) o1).compareTo((String) o2);
          if (cmp != 0) return cmp;
        } else if (o1 instanceof Long) {
          return 1;
        } else {
          return -1;
        }
      }
      return Integer.compare(s1, s2);
    }

    @Override
    public boolean equals(Object ob) {
      if (this == ob) return true;
      if (!(ob instanceof Version)) return false;
      return this.version.equals(((Version) ob).version);
    }

    @Override
    public int hashCode() {
      return version.hashCode();
    }

    @Override
    public String toString() {
      return version;
    }
  }

  public static final class Requires implements Comparable<Requires> {
    public enum Modifier {
      TRANSITIVE, STATIC, SYNTHETIC, MANDATED
    }

    private final Set<Modifier> modifiers;
    private final String name;
    private final Version compiledVersion;
    private final String rawCompiledVersion;

    Requires(Set<Modifier> modifiers, String name, Version compiledVersion, String rawCompiledVersion) {
      this.modifiers = modifiers != null ? Collections.unmodifiableSet(new HashSet<Modifier>(modifiers)) : Collections.<Modifier>emptySet();
      this.name = Objects.requireNonNull(name);
      this.compiledVersion = compiledVersion;
      this.rawCompiledVersion = rawCompiledVersion;
    }

    public Set<Modifier> modifiers() {
      return modifiers;
    }

    public String name() {
      return name;
    }

    public Optional<Version> compiledVersion() {
      return Optional.ofNullable(compiledVersion);
    }

    public Optional<String> rawCompiledVersion() {
      return Optional.ofNullable(rawCompiledVersion);
    }

    public int compareTo(Requires that) {
      return this.name.compareTo(that.name);
    }

    @Override
    public boolean equals(Object ob) {
      if (this == ob) return true;
      if (!(ob instanceof Requires)) return false;
      Requires that = (Requires) ob;
      return this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

    @Override
    public String toString() {
      return "requires " + name;
    }
  }

  public static final class Exports implements Comparable<Exports> {
    public enum Modifier {
      SYNTHETIC, MANDATED
    }

    private final Set<Modifier> modifiers;
    private final String source;
    private final Set<String> targets;

    Exports(Set<Modifier> modifiers, String source, Set<String> targets) {
      this.modifiers = modifiers != null ? Collections.unmodifiableSet(new HashSet<Modifier>(modifiers)) : Collections.<Modifier>emptySet();
      this.source = Objects.requireNonNull(source);
      this.targets = targets != null ? Collections.unmodifiableSet(new HashSet<String>(targets)) : Collections.<String>emptySet();
    }

    public Set<Modifier> modifiers() {
      return modifiers;
    }

    public String source() {
      return source;
    }

    public boolean isQualified() {
      return !targets.isEmpty();
    }

    public Set<String> targets() {
      return targets;
    }

    public int compareTo(Exports that) {
      return this.source.compareTo(that.source);
    }

    @Override
    public boolean equals(Object ob) {
      if (this == ob) return true;
      if (!(ob instanceof Exports)) return false;
      Exports that = (Exports) ob;
      return this.source.equals(that.source) && this.targets.equals(that.targets);
    }

    @Override
    public int hashCode() {
      return Objects.hash(source, targets);
    }

    @Override
    public String toString() {
      return "exports " + source;
    }
  }

  public static final class Opens implements Comparable<Opens> {
    public enum Modifier {
      SYNTHETIC, MANDATED
    }

    private final Set<Modifier> modifiers;
    private final String source;
    private final Set<String> targets;

    Opens(Set<Modifier> modifiers, String source, Set<String> targets) {
      this.modifiers = modifiers != null ? Collections.unmodifiableSet(new HashSet<Modifier>(modifiers)) : Collections.<Modifier>emptySet();
      this.source = Objects.requireNonNull(source);
      this.targets = targets != null ? Collections.unmodifiableSet(new HashSet<String>(targets)) : Collections.<String>emptySet();
    }

    public Set<Modifier> modifiers() {
      return modifiers;
    }

    public String source() {
      return source;
    }

    public boolean isQualified() {
      return !targets.isEmpty();
    }

    public Set<String> targets() {
      return targets;
    }

    public int compareTo(Opens that) {
      return this.source.compareTo(that.source);
    }

    @Override
    public boolean equals(Object ob) {
      if (this == ob) return true;
      if (!(ob instanceof Opens)) return false;
      Opens that = (Opens) ob;
      return this.source.equals(that.source) && this.targets.equals(that.targets);
    }

    @Override
    public int hashCode() {
      return Objects.hash(source, targets);
    }

    @Override
    public String toString() {
      return "opens " + source;
    }
  }

  public static final class Provides implements Comparable<Provides> {
    private final String service;
    private final List<String> providers;

    Provides(String service, List<String> providers) {
      this.service = Objects.requireNonNull(service);
      this.providers = providers != null ? Collections.unmodifiableList(new ArrayList<String>(providers)) : Collections.<String>emptyList();
    }

    public String service() {
      return service;
    }

    public List<String> providers() {
      return providers;
    }

    public int compareTo(Provides that) {
      return this.service.compareTo(that.service);
    }

    @Override
    public boolean equals(Object ob) {
      if (this == ob) return true;
      if (!(ob instanceof Provides)) return false;
      Provides that = (Provides) ob;
      return this.service.equals(that.service) && this.providers.equals(that.providers);
    }

    @Override
    public int hashCode() {
      return Objects.hash(service, providers);
    }

    @Override
    public String toString() {
      return "provides " + service + " with " + providers;
    }
  }

  public static final class Builder {
    private final String name;
    private final boolean open;
    private boolean automatic;
    private final Set<String> packages = new HashSet<String>();
    private Version version;
    private String rawVersion;
    private String mainClass;
    private final Set<Requires> requires = new HashSet<Requires>();
    private final Set<Exports> exports = new HashSet<Exports>();
    private final Set<Opens> opens = new HashSet<Opens>();
    private final Set<Provides> provides = new HashSet<Provides>();
    private final Set<String> uses = new HashSet<String>();

    Builder(String name, boolean open) {
      this.name = Objects.requireNonNull(name);
      this.open = open;
    }

    public Builder version(Version v) {
      this.version = Objects.requireNonNull(v);
      this.rawVersion = v.toString();
      return this;
    }

    public Builder version(String v) {
      Objects.requireNonNull(v);
      this.version = Version.parse(v);
      this.rawVersion = v;
      return this;
    }

    public Builder mainClass(String mainClass) {
      this.mainClass = Objects.requireNonNull(mainClass);
      return this;
    }

    public Builder packages(Set<String> pns) {
      this.packages.addAll(Objects.requireNonNull(pns));
      return this;
    }

    public Builder requires(String name) {
      return requires(Collections.<Requires.Modifier>emptySet(), name);
    }

    public Builder requires(Set<Requires.Modifier> ms, String name) {
      this.requires.add(new Requires(ms, name, null, null));
      return this;
    }

    public Builder exports(String pn) {
      return exports(Collections.<Exports.Modifier>emptySet(), pn, Collections.<String>emptySet());
    }

    public Builder exports(String pn, Set<String> targets) {
      return exports(Collections.<Exports.Modifier>emptySet(), pn, targets);
    }

    public Builder exports(Set<Exports.Modifier> ms, String pn) {
      return exports(ms, pn, Collections.<String>emptySet());
    }

    public Builder exports(Set<Exports.Modifier> ms, String pn, Set<String> targets) {
      this.exports.add(new Exports(ms, pn, targets));
      return this;
    }

    public Builder opens(String pn) {
      return opens(Collections.<Opens.Modifier>emptySet(), pn, Collections.<String>emptySet());
    }

    public Builder opens(String pn, Set<String> targets) {
      return opens(Collections.<Opens.Modifier>emptySet(), pn, targets);
    }

    public Builder opens(Set<Opens.Modifier> ms, String pn) {
      return opens(ms, pn, Collections.<String>emptySet());
    }

    public Builder opens(Set<Opens.Modifier> ms, String pn, Set<String> targets) {
      this.opens.add(new Opens(ms, pn, targets));
      return this;
    }

    public Builder provides(String service, List<String> providers) {
      this.provides.add(new Provides(service, providers));
      return this;
    }

    public Builder uses(String service) {
      this.uses.add(Objects.requireNonNull(service));
      return this;
    }

    public ModuleDescriptor build() {
      return new ModuleDescriptor(
          name,
          open,
          automatic,
          packages,
          version,
          rawVersion,
          mainClass,
          requires,
          exports,
          opens,
          provides,
          uses);
    }
  }
}
