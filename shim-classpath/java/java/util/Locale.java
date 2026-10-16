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

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.stream.Stream;

public final class Locale implements Cloneable, Serializable {
  private static final long serialVersionUID = 9149081749638150636L;

  public static final Locale ENGLISH = createConstant("en", "");
  public static final Locale FRENCH = createConstant("fr", "");
  public static final Locale GERMAN = createConstant("de", "");
  public static final Locale ITALIAN = createConstant("it", "");
  public static final Locale JAPANESE = createConstant("ja", "");
  public static final Locale KOREAN = createConstant("ko", "");
  public static final Locale CHINESE = createConstant("zh", "");
  public static final Locale SIMPLIFIED_CHINESE = createConstant("zh", "CN");
  public static final Locale TRADITIONAL_CHINESE = createConstant("zh", "TW");
  public static final Locale FRANCE = createConstant("fr", "FR");
  public static final Locale GERMANY = createConstant("de", "DE");
  public static final Locale ITALY = createConstant("it", "IT");
  public static final Locale JAPAN = createConstant("ja", "JP");
  public static final Locale KOREA = createConstant("ko", "KR");
  public static final Locale UK = createConstant("en", "GB");
  public static final Locale US = createConstant("en", "US");
  public static final Locale CANADA = createConstant("en", "CA");
  public static final Locale CANADA_FRENCH = createConstant("fr", "CA");
  public static final Locale ROOT = createConstant("", "");
  public static final Locale CHINA = SIMPLIFIED_CHINESE;
  public static final Locale PRC = SIMPLIFIED_CHINESE;
  public static final Locale TAIWAN = TRADITIONAL_CHINESE;

  public static final char PRIVATE_USE_EXTENSION = 'x';
  public static final char UNICODE_LOCALE_EXTENSION = 'u';

  private static final ObjectStreamField[] serialPersistentFields = {
    new ObjectStreamField("language", String.class),
    new ObjectStreamField("country", String.class),
    new ObjectStreamField("variant", String.class),
    new ObjectStreamField("hashcode", Integer.TYPE),
    new ObjectStreamField("script", String.class),
    new ObjectStreamField("extensions", String.class)
  };

  public enum Category {
    DISPLAY,
    FORMAT
  }

  public enum IsoCountryCode {
    PART1_ALPHA2,
    PART1_ALPHA3,
    PART3
  }

  public enum FilteringMode {
    AUTOSELECT_FILTERING,
    EXTENDED_FILTERING,
    IGNORE_EXTENDED_RANGES,
    MAP_EXTENDED_RANGES,
    REJECT_EXTENDED_RANGES
  }

  private static volatile Locale defaultLocale;
  private static volatile Locale defaultDisplayLocale;
  private static volatile Locale defaultFormatLocale;

  private transient String language;
  private transient String script;
  private transient String country;
  private transient String variant;
  private transient LocaleExtensions extensions;
  private transient volatile int hashCodeValue;

  static {
    String lang = System.getProperty("user.language", "en");
    String scr = System.getProperty("user.script", "");
    String reg = System.getProperty("user.country", System.getProperty("user.region", "US"));
    String var = System.getProperty("user.variant", "");
    defaultLocale = getInstance(lang, scr, reg, var, null);
    defaultDisplayLocale = defaultLocale;
    defaultFormatLocale = defaultLocale;
  }

  private Locale(String language, String script, String country, String variant, LocaleExtensions extensions) {
    this.language = language;
    this.script = script;
    this.country = country;
    this.variant = variant;
    this.extensions = extensions;
  }

  private static Locale createConstant(String language, String country) {
    return new Locale(language, "", country, "", null);
  }

  private static String convertOldIsoCodes(String language) {
    if (language.equals("iw")) {
      return "he";
    } else if (language.equals("ji")) {
      return "yi";
    } else if (language.equals("in")) {
      return "id";
    }
    return language;
  }

  private static LocaleExtensions compatibilityExtensions(String language, String script, String country,
      String variant) {
    if (script.isEmpty() && variant.equals("JP") && language.equals("ja") && country.equals("JP")) {
      return new LocaleExtensions(new TreeMap<Character, String>(), new TreeSet<String>(),
          singleKeyword("ca", "japanese"), null);
    } else if (script.isEmpty() && variant.equals("TH") && language.equals("th") && country.equals("TH")) {
      return new LocaleExtensions(new TreeMap<Character, String>(), new TreeSet<String>(),
          singleKeyword("nu", "thai"), null);
    }
    return null;
  }

  private static Map<String, String> singleKeyword(String key, String type) {
    Map<String, String> m = new TreeMap<String, String>();
    m.put(key, type);
    return m;
  }

  private static Locale getInstance(String language, String script, String country, String variant,
      LocaleExtensions extensions) {
    language = convertOldIsoCodes(LocaleTags.lower(language));
    country = LocaleTags.upper(country);
    if (extensions == null) {
      extensions = compatibilityExtensions(language, script, country, variant);
    }
    return new Locale(language, script, country, variant, extensions);
  }

  public Locale(String language, String country, String variant) {
    if (language == null || country == null || variant == null) {
      throw new NullPointerException();
    }
    Locale l = getInstance(language, "", country, variant, null);
    this.language = l.language;
    this.script = l.script;
    this.country = l.country;
    this.variant = l.variant;
    this.extensions = l.extensions;
  }

  public Locale(String language, String country) {
    this(language, country, "");
  }

  public Locale(String language) {
    this(language, "", "");
  }

  public static Locale of(String language) {
    return new Locale(language, "", "");
  }

  public static Locale of(String language, String country) {
    return new Locale(language, country, "");
  }

  public static Locale of(String language, String country, String variant) {
    return new Locale(language, country, variant);
  }

  public static Locale getDefault() {
    return defaultLocale;
  }

  public static Locale getDefault(Category category) {
    switch (category) {
    case DISPLAY:
      return defaultDisplayLocale;
    case FORMAT:
      return defaultFormatLocale;
    default:
      return defaultLocale;
    }
  }

  public static synchronized void setDefault(Locale newLocale) {
    setDefault(Category.DISPLAY, newLocale);
    setDefault(Category.FORMAT, newLocale);
    defaultLocale = newLocale;
  }

  public static synchronized void setDefault(Category category, Locale newLocale) {
    if (category == null) {
      throw new NullPointerException("Category cannot be NULL");
    }
    if (newLocale == null) {
      throw new NullPointerException("Can't set default locale to NULL");
    }
    if (category == Category.DISPLAY) {
      defaultDisplayLocale = newLocale;
    } else {
      defaultFormatLocale = newLocale;
    }
  }

  private static Locale[] availableCache;

  public static synchronized Locale[] getAvailableLocales() {
    if (availableCache == null) {
      String[] tags = LocaleData.kAvailable.split(" ");
      Locale[] result = new Locale[tags.length];
      for (int i = 0; i < tags.length; i++) {
        result[i] = tags[i].equals("und") ? ROOT : tags[i].indexOf('_') > 0 ? legacy(tags[i]) : forLanguageTag(tags[i]);
      }
      availableCache = result;
    }
    return availableCache.clone();
  }

  private static Locale legacy(String token) {
    String[] parts = token.split("_");
    return new Locale(parts[0], parts[1], parts[2]);
  }

  public static Stream<Locale> availableLocales() {
    return Arrays.stream(getAvailableLocales());
  }

  private static final class Iso {
    static final String[] kLanguages;
    static final Map<String, String> kLanguage3 = new HashMap<String, String>();
    static final String[] kCountries;
    static final Map<String, String> kCountry3 = new HashMap<String, String>();
    static final String[] kPart3;

    static {
      String[] langs = LocaleData.kIsoLanguages.split(" ");
      kLanguages = new String[langs.length];
      for (int i = 0; i < langs.length; i++) {
        int c = langs[i].indexOf(':');
        kLanguages[i] = langs[i].substring(0, c);
        kLanguage3.put(kLanguages[i], langs[i].substring(c + 1));
      }
      String[] countries = LocaleData.kIsoCountries.split(" ");
      kCountries = new String[countries.length];
      for (int i = 0; i < countries.length; i++) {
        int c = countries[i].indexOf(':');
        kCountries[i] = countries[i].substring(0, c);
        kCountry3.put(kCountries[i], countries[i].substring(c + 1));
      }
      kPart3 = LocaleData.kIsoCountriesPart3.split(" ");
    }
  }

  static String languageForIso3(String code) {
    for (Map.Entry<String, String> e : Iso.kLanguage3.entrySet()) {
      if (e.getValue().equals(code)) {
        return e.getKey();
      }
    }
    return null;
  }

  static String countryForIso3(String code) {
    for (Map.Entry<String, String> e : Iso.kCountry3.entrySet()) {
      if (e.getValue().equals(code)) {
        return e.getKey();
      }
    }
    return null;
  }

  public static String[] getISOCountries() {
    return Iso.kCountries.clone();
  }

  public static Set<String> getISOCountries(IsoCountryCode type) {
    Objects.requireNonNull(type);
    Set<String> result = new HashSet<String>();
    switch (type) {
    case PART1_ALPHA2:
      result.addAll(Arrays.asList(Iso.kCountries));
      break;
    case PART1_ALPHA3:
      result.addAll(Iso.kCountry3.values());
      break;
    default:
      result.addAll(Arrays.asList(Iso.kPart3));
      break;
    }
    return Collections.unmodifiableSet(result);
  }

  public static String[] getISOLanguages() {
    return Iso.kLanguages.clone();
  }

  public String getLanguage() {
    return language;
  }

  public String getScript() {
    return script;
  }

  public String getCountry() {
    return country;
  }

  public String getVariant() {
    return variant;
  }

  public boolean hasExtensions() {
    return extensions != null;
  }

  public Locale stripExtensions() {
    return extensions == null ? this : new Locale(language, script, country, variant, null);
  }

  public String getExtension(char key) {
    if (!isValidExtensionKey(key)) {
      throw new IllegalArgumentException("Ill-formed extension key: " + key);
    }
    return extensions == null ? null : extensions.get(key);
  }

  private static boolean isValidExtensionKey(char key) {
    char c = Character.toLowerCase(key);
    return c >= 'a' && c <= 'z';
  }

  public Set<Character> getExtensionKeys() {
    return extensions == null ? Collections.<Character>emptySet() : extensions.keys();
  }

  public Set<String> getUnicodeLocaleAttributes() {
    return extensions == null ? Collections.<String>emptySet() : Collections.unmodifiableSet(extensions.attributes);
  }

  public String getUnicodeLocaleType(String key) {
    if (!LocaleTags.isUnicodeKey(key)) {
      throw new IllegalArgumentException("Ill-formed Unicode locale key: " + key);
    }
    return extensions == null ? null : extensions.keywords.get(LocaleTags.lower(key));
  }

  public Set<String> getUnicodeLocaleKeys() {
    return extensions == null ? Collections.<String>emptySet() : Collections.unmodifiableSet(extensions.keywords.keySet());
  }

  @Override
  public final String toString() {
    boolean l = !language.isEmpty();
    boolean s = !script.isEmpty();
    boolean r = !country.isEmpty();
    boolean v = !variant.isEmpty();
    boolean e = extensions != null && !extensions.id().isEmpty();
    StringBuilder result = new StringBuilder(language);
    if (r || (l && (v || s || e))) {
      result.append('_').append(country);
    }
    if (v && (l || r)) {
      result.append('_').append(variant);
    }
    if (s && (l || r)) {
      result.append("_#").append(script);
    }
    if (e && (l || r)) {
      result.append('_');
      if (!s) {
        result.append('#');
      }
      result.append(extensions.id());
    }
    return result.toString();
  }

  public String toLanguageTag() {
    String lang = language;
    String ctry = country;
    String var = variant;
    if (lang.equals("no") && ctry.equals("NO") && var.equals("NY")) {
      lang = "nn";
      var = "";
    }
    StringBuilder tag = new StringBuilder();
    boolean validLanguage = LocaleTags.isLanguage(lang);
    boolean validScript = LocaleTags.isScript(script);
    boolean validRegion = LocaleTags.isRegion(ctry);
    List<String> variantTags = new ArrayList<String>();
    List<String> lvariant = new ArrayList<String>();
    if (!var.isEmpty()) {
      String[] pieces = var.split("_", -1);
      int valid = 0;
      while (valid < pieces.length && LocaleTags.isVariant(pieces[valid])) {
        variantTags.add(pieces[valid++]);
      }
      boolean restPrivate = true;
      for (int i = valid; i < pieces.length; i++) {
        if (!LocaleTags.isPrivateUseSubtag(pieces[i])) {
          restPrivate = false;
        }
      }
      if (restPrivate) {
        for (int i = valid; i < pieces.length; i++) {
          lvariant.add(pieces[i]);
        }
      }
    }
    String ext = extensions == null ? "" : extensionsWithoutPrivate();
    String privateUse = extensions == null || extensions.privateUse == null ? "" : extensions.privateUse;
    boolean hasOther = validScript || validRegion || !variantTags.isEmpty() || !ext.isEmpty();
    boolean onlyPrivate = !validLanguage && !hasOther && (!privateUse.isEmpty() || !lvariant.isEmpty());
    if (!onlyPrivate) {
      tag.append(validLanguage ? lang : "und");
      if (validScript) {
        tag.append('-').append(script);
      }
      if (validRegion) {
        tag.append('-').append(ctry);
      }
      for (String v : variantTags) {
        tag.append('-').append(v);
      }
      if (!ext.isEmpty()) {
        tag.append('-').append(ext);
      }
    }
    if (!privateUse.isEmpty() || !lvariant.isEmpty()) {
      if (tag.length() > 0) {
        tag.append('-');
      }
      tag.append('x');
      if (!privateUse.isEmpty()) {
        tag.append('-').append(privateUse);
      }
      if (!lvariant.isEmpty()) {
        tag.append("-lvariant");
        for (String piece : lvariant) {
          tag.append('-').append(piece);
        }
      }
    }
    return tag.length() == 0 ? "und" : tag.toString();
  }

  private String extensionsWithoutPrivate() {
    String id = extensions.id();
    if (extensions.privateUse == null) {
      return id;
    }
    int cut = id.length() - ("x-" + extensions.privateUse).length();
    return cut <= 0 ? "" : id.substring(0, cut - 1);
  }

  public static Locale forLanguageTag(String languageTag) {
    Objects.requireNonNull(languageTag);
    return fromParsed(LocaleTags.parse(languageTag, false));
  }

  static Locale fromParsed(LocaleTags.Parsed p) {
    String lang = p.language;
    if (!p.extlangs.isEmpty()) {
      lang = p.extlangs.get(0);
    } else if (lang.equals("und")) {
      lang = "";
    }
    TreeMap<Character, String> others = new TreeMap<Character, String>();
    TreeSet<String> attributes = new TreeSet<String>();
    TreeMap<String, String> keywords = new TreeMap<String, String>();
    for (Map.Entry<Character, List<String>> e : p.extensions.entrySet()) {
      if (e.getKey() == 'u') {
        LocaleExtensions.addUnicode(e.getValue(), attributes, keywords);
      } else {
        others.put(e.getKey(), LocaleExtensions.join(e.getValue(), "-"));
      }
    }
    List<String> variants = new ArrayList<String>(p.variants);
    List<String> privateUse = new ArrayList<String>();
    for (String token : p.privateUse) {
      privateUse.add(LocaleTags.lower(token));
    }
    int lv = privateUse.indexOf("lvariant");
    if (lv >= 0 && lv + 1 < privateUse.size()) {
      variants.addAll(p.privateUse.subList(lv + 1, p.privateUse.size()));
      privateUse = new ArrayList<String>(privateUse.subList(0, lv));
    }
    LocaleExtensions ext = LocaleExtensions.create(others, attributes, keywords,
        LocaleExtensions.join(privateUse, "-"));
    return getInstance(lang, LocaleTags.title(p.script), p.region, LocaleExtensions.join(variants, "_"), ext);
  }

  public static String caseFoldLanguageTag(String languageTag) {
    Objects.requireNonNull(languageTag);
    if (LocaleTags.isGrandfathered(languageTag)) {
      return LocaleTags.lower(languageTag);
    }
    LocaleTags.Parsed p = LocaleTags.parse(languageTag, true);
    LocaleTags.checkStrict(p);
    StringBuilder sb = new StringBuilder();
    if (!p.language.isEmpty()) {
      sb.append(LocaleTags.lower(p.language));
    }
    for (String x : p.extlangs) {
      sb.append('-').append(LocaleTags.lower(x));
    }
    if (!p.script.isEmpty()) {
      sb.append('-').append(LocaleTags.title(p.script));
    }
    if (!p.region.isEmpty()) {
      sb.append('-').append(LocaleTags.upper(p.region));
    }
    for (String v : p.variants) {
      sb.append('-').append(v);
    }
    for (Map.Entry<Character, List<String>> e : p.extensions.entrySet()) {
      sb.append('-').append(e.getKey()).append('-').append(LocaleExtensions.join(e.getValue(), "-"));
    }
    if (!p.privateUse.isEmpty()) {
      if (sb.length() > 0) {
        sb.append('-');
      }
      sb.append("x-");
      for (int i = 0; i < p.privateUse.size(); i++) {
        sb.append(i > 0 ? "-" : "").append(LocaleTags.lower(p.privateUse.get(i)));
      }
    }
    return sb.toString();
  }

  public String getISO3Language() {
    if (language.isEmpty()) {
      return "";
    }
    if (language.length() == 3) {
      return language;
    }
    String code = Iso.kLanguage3.get(language);
    if (code == null) {
      throw new MissingResourceException("Couldn't find 3-letter language code for " + language,
          "FormatData_" + this, "ShortLanguage");
    }
    return code;
  }

  public String getISO3Country() {
    if (country.isEmpty()) {
      return "";
    }
    String code = Iso.kCountry3.get(country);
    if (code == null) {
      throw new MissingResourceException("Couldn't find 3-letter country code for " + country,
          "FormatData_" + this, "ShortCountry");
    }
    return code;
  }

  public final String getDisplayLanguage() {
    return getDisplayLanguage(getDefault(Category.DISPLAY));
  }

  public String getDisplayLanguage(Locale inLocale) {
    return LocaleNames.language(this, Objects.requireNonNull(inLocale));
  }

  public String getDisplayScript() {
    return getDisplayScript(getDefault(Category.DISPLAY));
  }

  public String getDisplayScript(Locale inLocale) {
    return LocaleNames.script(this, Objects.requireNonNull(inLocale));
  }

  public final String getDisplayCountry() {
    return getDisplayCountry(getDefault(Category.DISPLAY));
  }

  public String getDisplayCountry(Locale inLocale) {
    return LocaleNames.country(this, Objects.requireNonNull(inLocale));
  }

  public final String getDisplayVariant() {
    return getDisplayVariant(getDefault(Category.DISPLAY));
  }

  public String getDisplayVariant(Locale inLocale) {
    return LocaleNames.variant(this, Objects.requireNonNull(inLocale));
  }

  public final String getDisplayName() {
    return getDisplayName(getDefault(Category.DISPLAY));
  }

  public String getDisplayName(Locale inLocale) {
    return LocaleNames.name(this, Objects.requireNonNull(inLocale));
  }

  LocaleExtensions extensions() {
    return extensions;
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  @Override
  public int hashCode() {
    int h = hashCodeValue;
    if (h == 0) {
      h = LocaleTags.lower(language + script + country).concat(variant).hashCode();
      if (extensions != null) {
        h ^= extensions.hashCode();
      }
      hashCodeValue = h;
    }
    return h;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Locale)) {
      return false;
    }
    Locale other = (Locale) obj;
    return language.equals(other.language) && script.equals(other.script) && country.equals(other.country)
        && variant.equals(other.variant) && Objects.equals(extensions, other.extensions);
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    ObjectOutputStream.PutField fields = out.putFields();
    fields.put("language", language);
    fields.put("script", script);
    fields.put("country", country);
    fields.put("variant", variant);
    fields.put("extensions", extensions == null ? "" : extensions.id());
    fields.put("hashcode", -1);
    out.writeFields();
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    ObjectInputStream.GetField fields = in.readFields();
    String l = (String) fields.get("language", "");
    String s = (String) fields.get("script", "");
    String c = (String) fields.get("country", "");
    String v = (String) fields.get("variant", "");
    String e = (String) fields.get("extensions", "");
    if (l == null || s == null || c == null || v == null) {
      throw new java.io.InvalidObjectException("null field");
    }
    Locale parsed = e == null || e.isEmpty() ? null : forLanguageTag("und-" + e);
    this.language = l;
    this.script = s;
    this.country = c;
    this.variant = v;
    this.extensions = parsed == null ? null : parsed.extensions;
  }

  private Object readResolve() {
    return getInstance(language, script, country, variant, extensions);
  }

  public static final class Builder {
    private String language = "";
    private String script = "";
    private String region = "";
    private String variant = "";
    private final TreeMap<Character, String> others = new TreeMap<Character, String>();
    private final TreeSet<String> attributes = new TreeSet<String>();
    private final TreeMap<String, String> keywords = new TreeMap<String, String>();
    private String privateUse = "";

    public Builder() { }

    public Builder setLocale(Locale locale) {
      Objects.requireNonNull(locale);
      String lang = locale.language;
      String scr = locale.script;
      String reg = locale.country;
      String var = locale.variant;
      if (lang.equals("no") && reg.equals("NO") && var.equals("NY")) {
        lang = "nn";
        var = "";
      }
      if (locale.extensions != null && ((lang.equals("ja") && reg.equals("JP") && var.equals("JP"))
          || (lang.equals("th") && reg.equals("TH") && var.equals("TH")))) {
        var = "";
      }
      checkLanguage(lang);
      checkScript(scr);
      checkRegion(reg);
      String normalizedVariant = checkVariant(var);
      setBase(lang, scr, reg, normalizedVariant);
      others.clear();
      attributes.clear();
      keywords.clear();
      privateUse = "";
      LocaleExtensions ext = locale.extensions;
      if (ext != null) {
        others.putAll(ext.others);
        attributes.addAll(ext.attributes);
        keywords.putAll(ext.keywords);
        privateUse = ext.privateUse == null ? "" : ext.privateUse;
      }
      return this;
    }

    private void setBase(String lang, String scr, String reg, String var) {
      language = lang.isEmpty() ? "" : convertOldIsoCodes(LocaleTags.lower(lang));
      script = LocaleTags.title(scr);
      region = LocaleTags.upper(reg);
      variant = var;
    }

    private static void checkLanguage(String lang) {
      if (!lang.isEmpty() && !LocaleTags.isLanguage(lang)) {
        throw new IllformedLocaleException("Ill-formed language: " + lang, 0);
      }
    }

    private static void checkScript(String scr) {
      if (!scr.isEmpty() && !LocaleTags.isScript(scr)) {
        throw new IllformedLocaleException("Ill-formed script: " + scr, 0);
      }
    }

    private static void checkRegion(String reg) {
      if (!reg.isEmpty() && !LocaleTags.isRegion(reg)) {
        throw new IllformedLocaleException("Ill-formed region: " + reg, 0);
      }
    }

    private static String checkVariant(String var) {
      if (var.isEmpty()) {
        return "";
      }
      StringBuilder sb = new StringBuilder();
      int index = 0;
      int i = 0;
      while (i <= var.length()) {
        int j = i;
        while (j < var.length() && var.charAt(j) != '-' && var.charAt(j) != '_') {
          j++;
        }
        String piece = var.substring(i, j);
        if (!LocaleTags.isVariant(piece)) {
          throw new IllformedLocaleException("Ill-formed variant: " + var, index);
        }
        if (sb.length() > 0) {
          sb.append('_');
        }
        sb.append(piece);
        i = j + 1;
        index = i;
      }
      return sb.toString();
    }

    public Builder setLanguageTag(String languageTag) {
      if (languageTag == null) {
        return clear();
      }
      LocaleTags.Parsed p = LocaleTags.parse(languageTag, true);
      LocaleTags.checkStrict(p);
      Locale l = fromParsed(p);
      Locale base = new Locale(l.language, l.script, l.country, l.variant, l.extensions);
      String lang = base.language;
      String var = base.variant;
      clear();
      language = lang;
      script = base.script;
      region = base.country;
      variant = var;
      if (base.extensions != null) {
        others.putAll(base.extensions.others);
        attributes.addAll(base.extensions.attributes);
        keywords.putAll(base.extensions.keywords);
        privateUse = base.extensions.privateUse == null ? "" : base.extensions.privateUse;
      }
      return this;
    }

    public Builder setLanguage(String language) {
      if (language == null || language.isEmpty()) {
        this.language = "";
        return this;
      }
      checkLanguage(language);
      this.language = convertOldIsoCodes(LocaleTags.lower(language));
      return this;
    }

    public Builder setScript(String script) {
      if (script == null || script.isEmpty()) {
        this.script = "";
        return this;
      }
      checkScript(script);
      this.script = LocaleTags.title(script);
      return this;
    }

    public Builder setRegion(String region) {
      if (region == null || region.isEmpty()) {
        this.region = "";
        return this;
      }
      checkRegion(region);
      this.region = LocaleTags.upper(region);
      return this;
    }

    public Builder setVariant(String variant) {
      this.variant = variant == null ? "" : checkVariant(variant);
      return this;
    }

    public Builder setExtension(char key, String value) {
      if (!isValidExtensionKey(key)) {
        throw new IllformedLocaleException("Ill-formed extension key: " + key, 0);
      }
      char k = Character.toLowerCase(key);
      if (value == null || value.isEmpty()) {
        if (k == 'u') {
          attributes.clear();
          keywords.clear();
        } else if (k == 'x') {
          privateUse = "";
        } else {
          others.remove(k);
        }
        return this;
      }
      List<String> tokens = new ArrayList<String>();
      int index = 0;
      for (String piece : value.split("[-_]", -1)) {
        boolean ok = k == 'x' ? LocaleTags.isPrivateUseSubtag(piece) : LocaleTags.isExtensionSubtag(piece);
        if (k == 'u') {
          ok = LocaleTags.isUnicodeType(piece) || LocaleTags.isUnicodeKey(piece);
        }
        if (!ok) {
          throw new IllformedLocaleException("Ill-formed extension value: " + piece, index);
        }
        tokens.add(LocaleTags.lower(piece));
        index += piece.length() + 1;
      }
      if (k == 'u') {
        attributes.clear();
        keywords.clear();
        LocaleExtensions.addUnicode(tokens, attributes, keywords);
      } else if (k == 'x') {
        privateUse = LocaleExtensions.join(tokens, "-");
      } else {
        others.put(k, LocaleExtensions.join(tokens, "-"));
      }
      return this;
    }

    public Builder setUnicodeLocaleKeyword(String key, String type) {
      Objects.requireNonNull(key);
      if (!LocaleTags.isUnicodeKey(key)) {
        throw new IllformedLocaleException("Ill-formed Unicode locale keyword key: " + key, 0);
      }
      String k = LocaleTags.lower(key);
      if (type == null) {
        keywords.remove(k);
        return this;
      }
      String t = "";
      if (!type.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (String piece : type.split("[-_]", -1)) {
          if (!LocaleTags.isUnicodeType(piece)) {
            throw new IllformedLocaleException("Ill-formed Unicode locale keyword type: " + type, 0);
          }
          if (sb.length() > 0) {
            sb.append('-');
          }
          sb.append(LocaleTags.lower(piece));
        }
        t = sb.toString();
      }
      keywords.put(k, t);
      return this;
    }

    public Builder addUnicodeLocaleAttribute(String attribute) {
      Objects.requireNonNull(attribute);
      if (!LocaleTags.isUnicodeType(attribute)) {
        throw new IllformedLocaleException("Ill-formed Unicode locale attribute: " + attribute, 0);
      }
      attributes.add(LocaleTags.lower(attribute));
      return this;
    }

    public Builder removeUnicodeLocaleAttribute(String attribute) {
      Objects.requireNonNull(attribute);
      if (!LocaleTags.isUnicodeType(attribute)) {
        throw new IllformedLocaleException("Ill-formed Unicode locale attribute: " + attribute, 0);
      }
      attributes.remove(LocaleTags.lower(attribute));
      return this;
    }

    public Builder clear() {
      language = "";
      script = "";
      region = "";
      variant = "";
      return clearExtensions();
    }

    public Builder clearExtensions() {
      others.clear();
      attributes.clear();
      keywords.clear();
      privateUse = "";
      return this;
    }

    public Locale build() {
      LocaleExtensions ext = LocaleExtensions.create(others, attributes, keywords, privateUse);
      return getInstance(language, script, region, variant, ext);
    }
  }

  public static Locale lookup(List<LanguageRange> priorityList, Collection<Locale> locales) {
    return LocaleMatcher.lookup(priorityList, locales);
  }

  public static String lookupTag(List<LanguageRange> priorityList, Collection<String> tags) {
    return LocaleMatcher.lookupTag(priorityList, tags);
  }

  public static List<Locale> filter(List<LanguageRange> priorityList, Collection<Locale> locales,
      FilteringMode mode) {
    return LocaleMatcher.filter(priorityList, locales, mode);
  }

  public static List<Locale> filter(List<LanguageRange> priorityList, Collection<Locale> locales) {
    return filter(priorityList, locales, FilteringMode.AUTOSELECT_FILTERING);
  }

  public static List<String> filterTags(List<LanguageRange> priorityList, Collection<String> tags,
      FilteringMode mode) {
    return LocaleMatcher.filterTags(priorityList, tags, mode);
  }

  public static List<String> filterTags(List<LanguageRange> priorityList, Collection<String> tags) {
    return filterTags(priorityList, tags, FilteringMode.AUTOSELECT_FILTERING);
  }

  public static final class LanguageRange {
    public static final double MAX_WEIGHT = 1.0;
    public static final double MIN_WEIGHT = 0.0;

    private final String range;
    private final double weight;

    public LanguageRange(String range) {
      this(range, MAX_WEIGHT);
    }

    public LanguageRange(String range, double weight) {
      if (range == null) {
        throw new NullPointerException();
      }
      if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
        throw new IllegalArgumentException("weight=" + weight);
      }
      range = LocaleTags.lower(range);
      if (!LocaleMatcher.isValidRange(range)) {
        throw new IllegalArgumentException("range=" + range);
      }
      this.range = range;
      this.weight = weight;
    }

    public String getRange() {
      return range;
    }

    public double getWeight() {
      return weight;
    }

    public static List<LanguageRange> parse(String ranges) {
      return LocaleMatcher.parse(ranges, null);
    }

    public static List<LanguageRange> parse(String ranges, Map<String, List<String>> map) {
      return LocaleMatcher.parse(ranges, map);
    }

    public static List<LanguageRange> mapEquivalents(List<LanguageRange> priorityList,
        Map<String, List<String>> map) {
      return LocaleMatcher.mapEquivalents(priorityList, map);
    }

    @Override
    public int hashCode() {
      int result = 17;
      result = 37 * result + range.hashCode();
      long bits = Double.doubleToLongBits(weight);
      return 37 * result + (int) (bits ^ (bits >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof LanguageRange)) {
        return false;
      }
      LanguageRange other = (LanguageRange) obj;
      return range.equals(other.range) && weight == other.weight;
    }

    @Override
    public String toString() {
      return weight == MAX_WEIGHT ? range : range + ";q=" + weight;
    }
  }
}
