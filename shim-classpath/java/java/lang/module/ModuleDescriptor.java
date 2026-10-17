/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.AccessFlag;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

public final class ModuleDescriptor implements Comparable<ModuleDescriptor> {
  public enum Modifier {
    OPEN, AUTOMATIC, SYNTHETIC, MANDATED
  }

  private static final Set<String> kKeywords = new HashSet<String>(java.util.Arrays.asList("abstract", "assert",
      "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double",
      "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import",
      "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public",
      "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
      "transient", "try", "void", "volatile", "while", "true", "false", "null", "_"));

  private static <E extends Enum<E>> Set<E> enumSet(Collection<E> c, Class<E> type) {
    EnumSet<E> s = EnumSet.noneOf(type);
    s.addAll(c);
    return Collections.unmodifiableSet(s);
  }

  private static <T> Set<T> copy(Set<T> s) {
    return Collections.unmodifiableSet(new HashSet<T>(s));
  }

  private static String toString(Set<? extends Enum<?>> mods, String what) {
    StringBuilder sb = new StringBuilder();
    for (Enum<?> e : mods) {
      sb.append(e.toString().toLowerCase(Locale.ROOT)).append(' ');
    }
    return sb.append(what).toString();
  }

  public static final class Requires implements Comparable<Requires> {
    public enum Modifier {
      TRANSITIVE, STATIC, SYNTHETIC, MANDATED
    }

    private final Set<Modifier> mods;
    private final String name;
    private final Version compiledVersion;
    private final String rawCompiledVersion;

    private Requires(Set<Modifier> ms, String mn, Version v, String vs) {
      this.mods = enumSet(ms, Modifier.class);
      this.name = mn;
      this.compiledVersion = v;
      this.rawCompiledVersion = vs;
    }

    public Set<Modifier> modifiers() {
      return mods;
    }

    public Set<AccessFlag> accessFlags() {
      EnumSet<AccessFlag> s = EnumSet.noneOf(AccessFlag.class);
      for (Modifier m : mods) {
        switch (m) {
        case TRANSITIVE:
          s.add(AccessFlag.TRANSITIVE);
          break;
        case STATIC:
          s.add(AccessFlag.STATIC_PHASE);
          break;
        case SYNTHETIC:
          s.add(AccessFlag.SYNTHETIC);
          break;
        default:
          s.add(AccessFlag.MANDATED);
          break;
        }
      }
      return Collections.unmodifiableSet(s);
    }

    public String name() {
      return name;
    }

    public Optional<Version> compiledVersion() {
      return Optional.ofNullable(compiledVersion);
    }

    public Optional<String> rawCompiledVersion() {
      if (compiledVersion != null) {
        return Optional.of(compiledVersion.toString());
      }
      return Optional.ofNullable(rawCompiledVersion);
    }

    @Override
    public int compareTo(Requires that) {
      if (this == that) {
        return 0;
      }
      int c = name.compareTo(that.name);
      if (c != 0) {
        return c;
      }
      c = compareSets(mods, that.mods);
      if (c != 0) {
        return c;
      }
      return compareVersions(compiledVersion, that.compiledVersion, rawCompiledVersion, that.rawCompiledVersion);
    }

    @Override
    public boolean equals(Object ob) {
      if (!(ob instanceof Requires)) {
        return false;
      }
      Requires that = (Requires) ob;
      return name.equals(that.name) && mods.equals(that.mods)
          && Objects.equals(compiledVersion, that.compiledVersion)
          && Objects.equals(rawCompiledVersion, that.rawCompiledVersion);
    }

    @Override
    public int hashCode() {
      int hash = name.hashCode() * 43 + mods.hashCode();
      if (compiledVersion != null) {
        hash = hash * 43 + compiledVersion.hashCode();
      }
      if (rawCompiledVersion != null) {
        hash = hash * 43 + rawCompiledVersion.hashCode();
      }
      return hash;
    }

    @Override
    public String toString() {
      String what;
      if (compiledVersion != null) {
        what = name() + " (@" + compiledVersion + ")";
      } else {
        what = name();
      }
      return ModuleDescriptor.toString(mods, what);
    }
  }

  public static final class Exports implements Comparable<Exports> {
    public enum Modifier {
      SYNTHETIC, MANDATED
    }

    private final Set<Modifier> mods;
    private final String source;
    private final Set<String> targets;

    private Exports(Set<Modifier> ms, String source, Set<String> targets) {
      this.mods = enumSet(ms, Modifier.class);
      this.source = source;
      this.targets = copy(targets);
    }

    public Set<Modifier> modifiers() {
      return mods;
    }

    public Set<AccessFlag> accessFlags() {
      return flags(mods.contains(Modifier.SYNTHETIC), mods.contains(Modifier.MANDATED));
    }

    public boolean isQualified() {
      return !targets.isEmpty();
    }

    public String source() {
      return source;
    }

    public Set<String> targets() {
      return targets;
    }

    @Override
    public int compareTo(Exports that) {
      if (this == that) {
        return 0;
      }
      int c = source.compareTo(that.source);
      if (c != 0) {
        return c;
      }
      c = compareSets(mods, that.mods);
      if (c != 0) {
        return c;
      }
      return compareSets(targets, that.targets);
    }

    @Override
    public int hashCode() {
      int hash = mods.hashCode();
      hash = hash * 43 + source.hashCode();
      return hash * 43 + targets.hashCode();
    }

    @Override
    public boolean equals(Object ob) {
      if (!(ob instanceof Exports)) {
        return false;
      }
      Exports other = (Exports) ob;
      return Objects.equals(this.mods, other.mods) && Objects.equals(this.source, other.source)
          && Objects.equals(this.targets, other.targets);
    }

    @Override
    public String toString() {
      String s = ModuleDescriptor.toString(mods, source);
      if (targets.isEmpty()) {
        return s;
      }
      return s + " to " + targets;
    }
  }

  public static final class Opens implements Comparable<Opens> {
    public enum Modifier {
      SYNTHETIC, MANDATED
    }

    private final Set<Modifier> mods;
    private final String source;
    private final Set<String> targets;

    private Opens(Set<Modifier> ms, String source, Set<String> targets) {
      this.mods = enumSet(ms, Modifier.class);
      this.source = source;
      this.targets = copy(targets);
    }

    public Set<Modifier> modifiers() {
      return mods;
    }

    public Set<AccessFlag> accessFlags() {
      return flags(mods.contains(Modifier.SYNTHETIC), mods.contains(Modifier.MANDATED));
    }

    public boolean isQualified() {
      return !targets.isEmpty();
    }

    public String source() {
      return source;
    }

    public Set<String> targets() {
      return targets;
    }

    @Override
    public int compareTo(Opens that) {
      if (this == that) {
        return 0;
      }
      int c = source.compareTo(that.source);
      if (c != 0) {
        return c;
      }
      c = compareSets(mods, that.mods);
      if (c != 0) {
        return c;
      }
      return compareSets(targets, that.targets);
    }

    @Override
    public int hashCode() {
      int hash = mods.hashCode();
      hash = hash * 43 + source.hashCode();
      return hash * 43 + targets.hashCode();
    }

    @Override
    public boolean equals(Object ob) {
      if (!(ob instanceof Opens)) {
        return false;
      }
      Opens other = (Opens) ob;
      return Objects.equals(this.mods, other.mods) && Objects.equals(this.source, other.source)
          && Objects.equals(this.targets, other.targets);
    }

    @Override
    public String toString() {
      String s = ModuleDescriptor.toString(mods, source);
      if (targets.isEmpty()) {
        return s;
      }
      return s + " to " + targets;
    }
  }

  public static final class Provides implements Comparable<Provides> {
    private final String service;
    private final List<String> providers;

    private Provides(String service, List<String> providers) {
      this.service = service;
      this.providers = Collections.unmodifiableList(new ArrayList<String>(providers));
    }

    public String service() {
      return service;
    }

    public List<String> providers() {
      return providers;
    }

    @Override
    public int compareTo(Provides that) {
      if (this == that) {
        return 0;
      }
      int c = service.compareTo(that.service);
      if (c != 0) {
        return c;
      }
      int size1 = providers.size();
      int size2 = that.providers.size();
      for (int i = 0; i < Math.min(size1, size2); i++) {
        c = providers.get(i).compareTo(that.providers.get(i));
        if (c != 0) {
          return c;
        }
      }
      return size1 - size2;
    }

    @Override
    public int hashCode() {
      return service.hashCode() * 43 + providers.hashCode();
    }

    @Override
    public boolean equals(Object ob) {
      if (!(ob instanceof Provides)) {
        return false;
      }
      Provides other = (Provides) ob;
      return Objects.equals(this.service, other.service) && Objects.equals(this.providers, other.providers);
    }

    @Override
    public String toString() {
      return service + " with " + providers;
    }
  }

  private static Set<AccessFlag> flags(boolean synthetic, boolean mandated) {
    EnumSet<AccessFlag> s = EnumSet.noneOf(AccessFlag.class);
    if (synthetic) {
      s.add(AccessFlag.SYNTHETIC);
    }
    if (mandated) {
      s.add(AccessFlag.MANDATED);
    }
    return Collections.unmodifiableSet(s);
  }

  private static <T extends Comparable<T>> int compareSets(Set<T> a, Set<T> b) {
    List<T> x = new ArrayList<T>(a);
    List<T> y = new ArrayList<T>(b);
    Collections.sort(x);
    Collections.sort(y);
    int n = Math.min(x.size(), y.size());
    for (int i = 0; i < n; i++) {
      int c = x.get(i).compareTo(y.get(i));
      if (c != 0) {
        return c;
      }
    }
    return x.size() - y.size();
  }

  private static int compareVersions(Version v1, Version v2, String r1, String r2) {
    if (v1 != null && v2 != null) {
      return v1.compareTo(v2);
    }
    if (v1 != null) {
      return 1;
    }
    if (v2 != null) {
      return -1;
    }
    if (r1 == null) {
      return r2 == null ? 0 : -1;
    }
    return r2 == null ? 1 : r1.compareTo(r2);
  }

  public static final class Version implements Comparable<Version> {
    private final String version;
    private final List<Object> sequence = new ArrayList<Object>(4);
    private final List<Object> pre = new ArrayList<Object>(2);
    private final List<Object> build = new ArrayList<Object>(2);

    private static int takeNumber(String s, int i, List<Object> acc) {
      char c = s.charAt(i);
      int d = c - '0';
      int n = s.length();
      while (++i < n) {
        c = s.charAt(i);
        if (c >= '0' && c <= '9') {
          d = d * 10 + (c - '0');
          continue;
        }
        break;
      }
      acc.add(d);
      return i;
    }

    private static int takeString(String s, int i, List<Object> acc) {
      int b = i;
      int n = s.length();
      while (++i < n) {
        char c = s.charAt(i);
        if (c != '.' && c != '-' && c != '+' && !(c >= '0' && c <= '9')) {
          continue;
        }
        break;
      }
      acc.add(s.substring(b, i));
      return i;
    }

    private Version(String v) {
      if (v == null) {
        throw new IllegalArgumentException("Null version string");
      }
      int n = v.length();
      if (n == 0) {
        throw new IllegalArgumentException("Empty version string");
      }
      int i = 0;
      char c = v.charAt(i);
      if (!(c >= '0' && c <= '9')) {
        throw new IllegalArgumentException(v + ": Version string does not start with a number");
      }
      i = takeNumber(v, i, sequence);
      while (i < n) {
        c = v.charAt(i);
        if (c == '.') {
          i++;
          continue;
        }
        if (c == '-' || c == '+') {
          i++;
          break;
        }
        if (c >= '0' && c <= '9') {
          i = takeNumber(v, i, sequence);
        } else {
          i = takeString(v, i, sequence);
        }
      }
      if ((c == '-' || c == '+') && i >= n) {
        throw new IllegalArgumentException(v + ": Empty pre-release");
      }
      boolean plus = false;
      while (i < n) {
        c = v.charAt(i);
        if (c == '.' || c == '-') {
          i++;
          continue;
        }
        if (c == '+') {
          plus = true;
          i++;
          break;
        }
        if (c >= '0' && c <= '9') {
          i = takeNumber(v, i, pre);
        } else {
          i = takeString(v, i, pre);
        }
      }
      if (plus && i >= n) {
        throw new IllegalArgumentException(v + ": Empty build");
      }
      while (i < n) {
        c = v.charAt(i);
        if (c == '.' || c == '-' || c == '+') {
          i++;
          continue;
        }
        if (c >= '0' && c <= '9') {
          i = takeNumber(v, i, build);
        } else {
          i = takeString(v, i, build);
        }
      }
      this.version = v;
    }

    public static Version parse(String v) {
      return new Version(v);
    }

    @SuppressWarnings("unchecked")
    private int cmp(Object o1, Object o2) {
      return ((Comparable<Object>) o1).compareTo(o2);
    }

    private int compareTokens(List<Object> ts1, List<Object> ts2) {
      int n = Math.min(ts1.size(), ts2.size());
      for (int i = 0; i < n; i++) {
        Object o1 = ts1.get(i);
        Object o2 = ts2.get(i);
        int c;
        if (o1 instanceof Integer && o2 instanceof Integer) {
          c = cmp(o1, o2);
        } else if (o1 instanceof String && o2 instanceof String) {
          c = cmp(o1, o2);
        } else {
          c = o1.toString().compareTo(o2.toString());
        }
        if (c != 0) {
          return c;
        }
      }
      List<Object> rest = ts1.size() > ts2.size() ? ts1 : ts2;
      int sign = 0;
      for (int i = n; i < rest.size(); i++) {
        Object o = rest.get(i);
        if (o instanceof Integer && ((Integer) o).intValue() == 0) {
          continue;
        }
        sign = 1;
        break;
      }
      return rest == ts1 ? sign : -sign;
    }

    @Override
    public int compareTo(Version that) {
      int c = compareTokens(this.sequence, that.sequence);
      if (c != 0) {
        return c;
      }
      if (this.pre.isEmpty()) {
        if (!that.pre.isEmpty()) {
          return +1;
        }
      } else {
        if (that.pre.isEmpty()) {
          return -1;
        }
      }
      c = compareTokens(this.pre, that.pre);
      if (c != 0) {
        return c;
      }
      return compareTokens(this.build, that.build);
    }

    @Override
    public boolean equals(Object ob) {
      if (!(ob instanceof Version)) {
        return false;
      }
      return compareTo((Version) ob) == 0;
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

  private final String name;
  private final Version version;
  private final String rawVersionString;
  private final Set<Modifier> modifiers;
  private final boolean open;
  private final boolean automatic;
  private final Set<Requires> requires;
  private final Set<Exports> exports;
  private final Set<Opens> opens;
  private final Set<String> uses;
  private final Set<Provides> provides;
  private final Set<String> packages;
  private final String mainClass;

  private ModuleDescriptor(String name, Version version, String rawVersionString, Set<Modifier> modifiers,
      Set<Requires> requires, Set<Exports> exports, Set<Opens> opens, Set<String> uses, Set<Provides> provides,
      Set<String> packages, String mainClass) {
    this.name = name;
    this.version = version;
    this.rawVersionString = rawVersionString;
    this.modifiers = enumSet(modifiers, Modifier.class);
    this.open = modifiers.contains(Modifier.OPEN);
    this.automatic = modifiers.contains(Modifier.AUTOMATIC);
    this.requires = copy(requires);
    this.exports = copy(exports);
    this.opens = copy(opens);
    this.uses = copy(uses);
    this.provides = copy(provides);
    this.packages = copy(packages);
    this.mainClass = mainClass;
  }

  public String name() {
    return name;
  }

  public Set<Modifier> modifiers() {
    return modifiers;
  }

  public Set<AccessFlag> accessFlags() {
    EnumSet<AccessFlag> s = EnumSet.noneOf(AccessFlag.class);
    if (modifiers.contains(Modifier.OPEN)) {
      s.add(AccessFlag.OPEN);
    }
    if (modifiers.contains(Modifier.SYNTHETIC)) {
      s.add(AccessFlag.SYNTHETIC);
    }
    if (modifiers.contains(Modifier.MANDATED)) {
      s.add(AccessFlag.MANDATED);
    }
    return Collections.unmodifiableSet(s);
  }

  public boolean isOpen() {
    return open;
  }

  public boolean isAutomatic() {
    return automatic;
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

  public Set<String> uses() {
    return uses;
  }

  public Set<Provides> provides() {
    return provides;
  }

  public Optional<Version> version() {
    return Optional.ofNullable(version);
  }

  public Optional<String> rawVersion() {
    if (version != null) {
      return Optional.of(version.toString());
    }
    return Optional.ofNullable(rawVersionString);
  }

  public String toNameAndVersion() {
    if (version != null) {
      return name() + "@" + version;
    }
    return name();
  }

  public Optional<String> mainClass() {
    return Optional.ofNullable(mainClass);
  }

  public Set<String> packages() {
    return packages;
  }

  @Override
  public int compareTo(ModuleDescriptor that) {
    if (this == that) {
      return 0;
    }
    int c = this.name().compareTo(that.name());
    if (c != 0) {
      return c;
    }
    c = compareVersions(version, that.version, rawVersionString, that.rawVersionString);
    if (c != 0) {
      return c;
    }
    c = compareSets(modifiers, that.modifiers);
    if (c != 0) {
      return c;
    }
    c = compareSets(requires, that.requires);
    if (c != 0) {
      return c;
    }
    c = compareSets(packages, that.packages);
    if (c != 0) {
      return c;
    }
    c = compareSets(exports, that.exports);
    if (c != 0) {
      return c;
    }
    c = compareSets(opens, that.opens);
    if (c != 0) {
      return c;
    }
    c = compareSets(uses, that.uses);
    if (c != 0) {
      return c;
    }
    c = compareSets(provides, that.provides);
    if (c != 0) {
      return c;
    }
    return mainClass().orElse("").compareTo(that.mainClass().orElse(""));
  }

  @Override
  public boolean equals(Object ob) {
    if (ob == this) {
      return true;
    }
    if (!(ob instanceof ModuleDescriptor)) {
      return false;
    }
    ModuleDescriptor that = (ModuleDescriptor) ob;
    return name.equals(that.name) && modifiers.equals(that.modifiers) && requires.equals(that.requires)
        && Objects.equals(packages, that.packages) && exports.equals(that.exports) && opens.equals(that.opens)
        && uses.equals(that.uses) && provides.equals(that.provides) && Objects.equals(version, that.version)
        && Objects.equals(rawVersionString, that.rawVersionString) && Objects.equals(mainClass, that.mainClass);
  }

  @Override
  public int hashCode() {
    int hc = name.hashCode();
    hc = hc * 43 + modifiers.hashCode();
    hc = hc * 43 + requires.hashCode();
    hc = hc * 43 + Objects.hashCode(packages);
    hc = hc * 43 + exports.hashCode();
    hc = hc * 43 + opens.hashCode();
    hc = hc * 43 + uses.hashCode();
    hc = hc * 43 + provides.hashCode();
    hc = hc * 43 + Objects.hashCode(version);
    hc = hc * 43 + Objects.hashCode(rawVersionString);
    hc = hc * 43 + Objects.hashCode(mainClass);
    return hc;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (isOpen()) {
      sb.append("open ");
    }
    sb.append("module { name: ").append(toNameAndVersion());
    if (!requires.isEmpty()) {
      sb.append(", ").append(requires);
    }
    if (!uses.isEmpty()) {
      sb.append(", uses: ").append(uses);
    }
    if (!exports.isEmpty()) {
      sb.append(", exports: ").append(exports);
    }
    if (!opens.isEmpty()) {
      sb.append(", opens: ").append(opens);
    }
    if (!provides.isEmpty()) {
      sb.append(", provides: ").append(provides);
    }
    sb.append(" }");
    return sb.toString();
  }

  public static Builder newModule(String name, Set<Modifier> ms) {
    Set<Modifier> mods = new HashSet<Modifier>(ms);
    if (mods.contains(Modifier.AUTOMATIC) && mods.size() > 1) {
      throw new IllegalArgumentException("AUTOMATIC cannot be used with other modifiers");
    }
    return new Builder(name, true, mods);
  }

  public static Builder newModule(String name) {
    return new Builder(name, true, Collections.<Modifier>emptySet());
  }

  public static Builder newOpenModule(String name) {
    return new Builder(name, true, Collections.singleton(Modifier.OPEN));
  }

  public static Builder newAutomaticModule(String name) {
    return new Builder(name, true, Collections.singleton(Modifier.AUTOMATIC));
  }

  public static ModuleDescriptor read(InputStream in, Supplier<Set<String>> packageFinder) throws IOException {
    return ModuleInfoParser.read(Objects.requireNonNull(in), packageFinder);
  }

  public static ModuleDescriptor read(InputStream in) throws IOException {
    return read(in, null);
  }

  public static ModuleDescriptor read(ByteBuffer bb, Supplier<Set<String>> packageFinder) {
    byte[] b = new byte[bb.remaining()];
    bb.get(b);
    try {
      return read(new java.io.ByteArrayInputStream(b), packageFinder);
    } catch (IOException e) {
      throw new InvalidModuleDescriptorException(e.getMessage());
    }
  }

  public static ModuleDescriptor read(ByteBuffer bb) {
    return read(bb, null);
  }

  static Builder builderFor(String name, Set<Modifier> mods, boolean strict) {
    return new Builder(name, strict, mods);
  }

  public static final class Builder {
    final String name;
    final boolean strict;
    final Set<Modifier> modifiers;
    final boolean open;
    final boolean automatic;
    final Set<String> packages = new HashSet<String>();
    final Map<String, Requires> requires = new HashMap<String, Requires>();
    final Map<String, Exports> exports = new HashMap<String, Exports>();
    final Map<String, Opens> opens = new HashMap<String, Opens>();
    final Set<String> uses = new HashSet<String>();
    final Map<String, Provides> provides = new HashMap<String, Provides>();
    Version version;
    String rawVersionString;
    String mainClass;

    private Builder(String name, boolean strict, Set<Modifier> modifiers) {
      this.strict = strict;
      this.name = strict ? requireModuleName(name) : Objects.requireNonNull(name);
      this.modifiers = modifiers.isEmpty() ? Collections.<Modifier>emptySet() : EnumSet.copyOf(modifiers);
      this.open = modifiers.contains(Modifier.OPEN);
      this.automatic = modifiers.contains(Modifier.AUTOMATIC);
    }

    Set<String> currentPackages() {
      return packages;
    }

    public Builder requires(Requires req) {
      if (automatic) {
        throw new IllegalStateException("Automatic modules cannot declare dependences");
      }
      String mn = req.name();
      if (name.equals(mn)) {
        throw new IllegalArgumentException("Dependence on self");
      }
      if (requires.containsKey(mn)) {
        throw new IllegalStateException("Dependence upon " + mn + " already declared");
      }
      requires.put(mn, req);
      return this;
    }

    public Builder requires(Set<Requires.Modifier> ms, String mn, Version compiledVersion) {
      Objects.requireNonNull(compiledVersion);
      if (strict) {
        mn = requireModuleName(mn);
      }
      return requires(new Requires(ms, mn, compiledVersion, null));
    }

    Builder requires(Set<Requires.Modifier> ms, String mn, String rawCompiledVersion) {
      Objects.requireNonNull(rawCompiledVersion);
      if (strict) {
        mn = requireModuleName(mn);
      }
      return requires(new Requires(ms, mn, null, rawCompiledVersion));
    }

    public Builder requires(Set<Requires.Modifier> ms, String mn) {
      if (strict) {
        mn = requireModuleName(mn);
      }
      return requires(new Requires(ms, mn, null, null));
    }

    public Builder requires(String mn) {
      return requires(EnumSet.noneOf(Requires.Modifier.class), mn);
    }

    public Builder exports(Exports e) {
      if (automatic) {
        throw new IllegalStateException("Automatic modules cannot declare exported packages");
      }
      String source = e.source();
      if (exports.containsKey(source)) {
        throw new IllegalStateException("Exported package " + source + " already declared");
      }
      exports.put(source, e);
      packages.add(source);
      return this;
    }

    public Builder exports(Set<Exports.Modifier> ms, String pn, Set<String> targets) {
      Set<String> ts = new HashSet<String>(targets);
      if (ts.isEmpty()) {
        throw new IllegalArgumentException("Empty target set");
      }
      if (strict) {
        requirePackageName(pn);
        for (String t : ts) {
          requireModuleName(t);
        }
      }
      return exports(new Exports(ms, pn, ts));
    }

    public Builder exports(Set<Exports.Modifier> ms, String pn) {
      if (strict) {
        requirePackageName(pn);
      }
      return exports(new Exports(ms, pn, Collections.<String>emptySet()));
    }

    public Builder exports(String pn, Set<String> targets) {
      return exports(EnumSet.noneOf(Exports.Modifier.class), pn, targets);
    }

    public Builder exports(String pn) {
      return exports(EnumSet.noneOf(Exports.Modifier.class), pn);
    }

    public Builder opens(Opens obj) {
      if (automatic || open) {
        throw new IllegalStateException("Open or automatic modules cannot declare open packages");
      }
      String source = obj.source();
      if (opens.containsKey(source)) {
        throw new IllegalStateException("Open package " + source + " already declared");
      }
      opens.put(source, obj);
      packages.add(source);
      return this;
    }

    public Builder opens(Set<Opens.Modifier> ms, String pn, Set<String> targets) {
      Set<String> ts = new HashSet<String>(targets);
      if (ts.isEmpty()) {
        throw new IllegalArgumentException("Empty target set");
      }
      if (strict) {
        requirePackageName(pn);
        for (String t : ts) {
          requireModuleName(t);
        }
      }
      return opens(new Opens(ms, pn, ts));
    }

    public Builder opens(Set<Opens.Modifier> ms, String pn) {
      if (strict) {
        requirePackageName(pn);
      }
      return opens(new Opens(ms, pn, Collections.<String>emptySet()));
    }

    public Builder opens(String pn, Set<String> targets) {
      return opens(EnumSet.noneOf(Opens.Modifier.class), pn, targets);
    }

    public Builder opens(String pn) {
      return opens(EnumSet.noneOf(Opens.Modifier.class), pn);
    }

    public Builder uses(String service) {
      if (automatic) {
        throw new IllegalStateException("Automatic modules cannot declare service dependences");
      }
      String sn = service;
      if (strict) {
        sn = requireServiceTypeName(service);
      }
      if (uses.contains(sn)) {
        throw new IllegalStateException("Dependence upon service " + sn + " already declared");
      }
      uses.add(sn);
      return this;
    }

    public Builder provides(Provides p) {
      if (automatic) {
        throw new IllegalStateException("Automatic modules cannot declare provided services");
      }
      String st = p.service();
      if (provides.containsKey(st)) {
        throw new IllegalStateException("Providers of service " + st + " already declared");
      }
      provides.put(st, p);
      for (String pn : p.providers()) {
        packages.add(packageName(pn));
      }
      return this;
    }

    public Builder provides(String service, List<String> providers) {
      List<String> ps = new ArrayList<String>(providers);
      if (ps.isEmpty()) {
        throw new IllegalArgumentException("Empty providers set");
      }
      if (strict) {
        requireServiceTypeName(service);
        for (String p : ps) {
          requireServiceProviderName(p);
        }
      } else {
        for (String p : ps) {
          if (packageName(p).isEmpty()) {
            throw new IllegalArgumentException(p + ": unnamed package");
          }
        }
      }
      return provides(new Provides(service, ps));
    }

    public Builder packages(Set<String> pns) {
      if (strict) {
        for (String pn : pns) {
          requirePackageName(pn);
        }
      }
      packages.addAll(pns);
      return this;
    }

    public Builder version(Version v) {
      version = Objects.requireNonNull(v);
      rawVersionString = null;
      return this;
    }

    public Builder version(String vs) {
      Objects.requireNonNull(vs);
      try {
        version = Version.parse(vs);
        rawVersionString = null;
      } catch (IllegalArgumentException e) {
        if (strict) {
          throw e;
        }
        version = null;
        rawVersionString = vs;
      }
      return this;
    }

    public Builder mainClass(String mc) {
      String pn;
      if (strict) {
        mc = requireQualifiedClassName(mc);
        pn = packageName(mc);
      } else {
        pn = packageName(mc);
        if (pn.isEmpty()) {
          throw new IllegalArgumentException(mc + ": unnamed package");
        }
      }
      packages.add(pn);
      mainClass = mc;
      return this;
    }

    public ModuleDescriptor build() {
      Set<Requires> reqs = new HashSet<Requires>(requires.values());
      if (strict && !name.equals("java.base") && !requires.containsKey("java.base")) {
        reqs.add(new Requires(Collections.singleton(Requires.Modifier.MANDATED), "java.base", null, null));
      }
      return new ModuleDescriptor(name, version, rawVersionString, modifiers, reqs,
          new HashSet<Exports>(exports.values()), new HashSet<Opens>(opens.values()), uses,
          new HashSet<Provides>(provides.values()), packages, mainClass);
    }
  }

  private static String packageName(String cn) {
    int index = cn.lastIndexOf('.');
    return index == -1 ? "" : cn.substring(0, index);
  }

  private static boolean isJavaIdentifier(String str) {
    if (str.isEmpty() || kKeywords.contains(str)) {
      return false;
    }
    int first = Character.codePointAt(str, 0);
    if (!Character.isJavaIdentifierStart(first)) {
      return false;
    }
    int i = Character.charCount(first);
    while (i < str.length()) {
      int cp = Character.codePointAt(str, i);
      if (!Character.isJavaIdentifierPart(cp)) {
        return false;
      }
      i += Character.charCount(cp);
    }
    return true;
  }

  private static String requireModuleName(String name) {
    Objects.requireNonNull(name);
    int next;
    int off = 0;
    while ((next = name.indexOf('.', off)) != -1) {
      String id = name.substring(off, next);
      if (!isJavaIdentifier(id)) {
        throw new IllegalArgumentException(name + ": Invalid module name: '" + id + "' is not a Java identifier");
      }
      off = next + 1;
    }
    String last = name.substring(off);
    if (!isJavaIdentifier(last)) {
      throw new IllegalArgumentException(name + ": Invalid module name: '" + last + "' is not a Java identifier");
    }
    return name;
  }

  private static String requirePackageName(String name) {
    return requireQualifiedName("package", name, false);
  }

  private static String requireServiceTypeName(String name) {
    return requireQualifiedClassName(name);
  }

  private static String requireServiceProviderName(String name) {
    return requireQualifiedClassName(name);
  }

  private static String requireQualifiedClassName(String name) {
    Objects.requireNonNull(name);
    if (packageName(name).isEmpty()) {
      throw new IllegalArgumentException(name + ": is not a qualified name of a Java class in a named package");
    }
    return requireQualifiedName("class", name, true);
  }

  private static String requireQualifiedName(String what, String name, boolean cls) {
    Objects.requireNonNull(name);
    int next;
    int off = 0;
    while ((next = name.indexOf('.', off)) != -1) {
      String id = name.substring(off, next);
      if (!isJavaIdentifier(id)) {
        throw new IllegalArgumentException(name + ": Invalid " + (cls ? "package" : what) + " name: '" + id
            + "' is not a Java identifier");
      }
      off = next + 1;
    }
    String last = name.substring(off);
    if (!isJavaIdentifier(last)) {
      throw new IllegalArgumentException(name + ": Invalid " + (cls ? "class" : what) + " name: '" + last
          + "' is not a Java identifier");
    }
    return name;
  }
}
