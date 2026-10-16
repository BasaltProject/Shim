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

import java.io.Serializable;

public class Locale implements Cloneable, Serializable {
  private static final long serialVersionUID = 9149081749638150636L;

  public static final Locale ENGLISH = new Locale("en", "");
  public static final Locale FRENCH = new Locale("fr", "");
  public static final Locale GERMAN = new Locale("de", "");
  public static final Locale ITALIAN = new Locale("it", "");
  public static final Locale JAPANESE = new Locale("ja", "");
  public static final Locale KOREAN = new Locale("ko", "");
  public static final Locale CHINESE = new Locale("zh", "");
  public static final Locale SIMPLIFIED_CHINESE = new Locale("zh", "CN");
  public static final Locale TRADITIONAL_CHINESE = new Locale("zh", "TW");
  public static final Locale FRANCE = new Locale("fr", "FR");
  public static final Locale GERMANY = new Locale("de", "DE");
  public static final Locale ITALY = new Locale("it", "IT");
  public static final Locale JAPAN = new Locale("ja", "JP");
  public static final Locale KOREA = new Locale("ko", "KR");
  public static final Locale CHINA = SIMPLIFIED_CHINESE;
  public static final Locale PRC = SIMPLIFIED_CHINESE;
  public static final Locale TAIWAN = TRADITIONAL_CHINESE;
  public static final Locale UK = new Locale("en", "GB");
  public static final Locale US = new Locale("en", "US");
  public static final Locale CANADA = new Locale("en", "CA");
  public static final Locale CANADA_FRENCH = new Locale("fr", "CA");
  public static final Locale ROOT = new Locale("", "");

  public enum Category {
    DISPLAY,
    FORMAT
  }

  private static volatile Locale defaultLocale;
  private static volatile Locale defaultDisplayLocale;
  private static volatile Locale defaultFormatLocale;

  private final String language;
  private final String country;
  private final String variant;
  private final String script;

  static {
    String lang = System.getProperty("user.language");
    String reg = System.getProperty("user.region");
    defaultLocale = new Locale(lang != null ? lang : "en", reg != null ? reg : "US");
    defaultDisplayLocale = defaultLocale;
    defaultFormatLocale = defaultLocale;
  }

  public Locale(String language, String country, String variant) {
    this(language, country, variant, "");
  }

  public Locale(String language, String country, String variant, String script) {
    this.language = language != null ? language.toLowerCase(ROOT) : "";
    this.country = country != null ? country.toUpperCase(ROOT) : "";
    this.variant = variant != null ? variant : "";
    this.script = script != null ? script : "";
  }

  public Locale(String language, String country) {
    this(language, country, "");
  }

  public Locale(String language) {
    this(language, "");
  }

  public static Locale of(String language) {
    return of(language, "", "");
  }

  public static Locale of(String language, String country) {
    return of(language, country, "");
  }

  public static Locale of(String language, String country, String variant) {
    Objects.requireNonNull(language, "language");
    Objects.requireNonNull(country, "country");
    Objects.requireNonNull(variant, "variant");
    return new Locale(language, country, variant);
  }

  public String getLanguage() {
    return language;
  }

  public String getCountry() {
    return country;
  }

  public String getVariant() {
    return variant;
  }

  public String getScript() {
    return script;
  }

  public static Locale getDefault() {
    return defaultLocale;
  }

  public static Locale getDefault(Category category) {
    Objects.requireNonNull(category, "category");
    if (category == Category.DISPLAY) {
      return defaultDisplayLocale != null ? defaultDisplayLocale : defaultLocale;
    }
    return defaultFormatLocale != null ? defaultFormatLocale : defaultLocale;
  }

  public static synchronized void setDefault(Locale newLocale) {
    Objects.requireNonNull(newLocale, "newLocale");
    defaultLocale = newLocale;
    defaultDisplayLocale = newLocale;
    defaultFormatLocale = newLocale;
  }

  public static synchronized void setDefault(Category category, Locale newLocale) {
    Objects.requireNonNull(category, "category");
    Objects.requireNonNull(newLocale, "newLocale");
    if (category == Category.DISPLAY) {
      defaultDisplayLocale = newLocale;
    } else {
      defaultFormatLocale = newLocale;
    }
  }

  public String toLanguageTag() {
    if (this == ROOT || (language.isEmpty() && country.isEmpty())) {
      return "und";
    }
    StringBuilder sb = new StringBuilder();
    if (!language.isEmpty()) {
      sb.append(language);
    }
    if (!script.isEmpty()) {
      sb.append('-').append(script);
    }
    if (!country.isEmpty()) {
      sb.append('-').append(country);
    }
    if (!variant.isEmpty()) {
      sb.append('-').append(variant);
    }
    return sb.length() > 0 ? sb.toString() : "und";
  }

  public static Locale forLanguageTag(String languageTag) {
    Objects.requireNonNull(languageTag, "languageTag");
    if (languageTag.isEmpty() || languageTag.equals("und")) {
      return ROOT;
    }
    String[] parts = languageTag.split("[-_]");
    String lang = parts.length > 0 ? parts[0] : "";
    String scr = "";
    String ctry = "";
    String var = "";
    int idx = 1;
    if (idx < parts.length && parts[idx].length() == 4) {
      scr = parts[idx++];
    }
    if (idx < parts.length) {
      ctry = parts[idx++];
    }
    if (idx < parts.length) {
      var = parts[idx++];
    }
    return new Locale(lang, ctry, var, scr);
  }

  public Locale stripExtensions() {
    return this;
  }

  private static final String[] kLanguageNames = {
      "en", "English",
      "de", "German",
      "fr", "French",
      "es", "Spanish",
      "it", "Italian",
      "pt", "Portuguese",
      "ja", "Japanese",
      "zh", "Chinese",
      "ko", "Korean",
      "ru", "Russian",
      "nl", "Dutch",
      "sv", "Swedish",
      "da", "Danish",
      "nb", "Norwegian Bokmål",
      "no", "Norwegian",
      "fi", "Finnish",
      "pl", "Polish",
      "cs", "Czech",
      "tr", "Turkish",
      "ar", "Arabic",
      "he", "Hebrew",
      "hi", "Hindi",
      "el", "Greek",
      "hu", "Hungarian",
      "uk", "Ukrainian",
      "th", "Thai",
      "vi", "Vietnamese",
      "id", "Indonesian",
      "ro", "Romanian",
      "bg", "Bulgarian",
      "ca", "Catalan",
      "hr", "Croatian",
      "sk", "Slovak",
      "sl", "Slovenian",
      "lt", "Lithuanian",
      "lv", "Latvian",
      "et", "Estonian",
      "is", "Icelandic",
      "ga", "Irish",
      "la", "Latin"};
  private static final String[] kCountryNames = {
      "US", "United States",
      "GB", "United Kingdom",
      "DE", "Germany",
      "FR", "France",
      "ES", "Spain",
      "IT", "Italy",
      "PT", "Portugal",
      "BR", "Brazil",
      "JP", "Japan",
      "CN", "China",
      "TW", "Taiwan",
      "KR", "South Korea",
      "RU", "Russia",
      "NL", "Netherlands",
      "SE", "Sweden",
      "DK", "Denmark",
      "NO", "Norway",
      "FI", "Finland",
      "PL", "Poland",
      "CZ", "Czechia",
      "TR", "Türkiye",
      "AR", "Argentina",
      "MX", "Mexico",
      "CA", "Canada",
      "AU", "Australia",
      "NZ", "New Zealand",
      "IN", "India",
      "CH", "Switzerland",
      "AT", "Austria",
      "BE", "Belgium",
      "IE", "Ireland",
      "ZA", "South Africa",
      "EG", "Egypt",
      "SA", "Saudi Arabia",
      "IL", "Israel",
      "GR", "Greece",
      "HU", "Hungary",
      "UA", "Ukraine",
      "TH", "Thailand",
      "VN", "Vietnam",
      "ID", "Indonesia",
      "SG", "Singapore",
      "HK", "Hong Kong SAR China",
      "CL", "Chile",
      "CO", "Colombia",
      "PE", "Peru",
      "RO", "Romania",
      "BG", "Bulgaria",
      "HR", "Croatia",
      "SK", "Slovakia",
      "SI", "Slovenia",
      "LT", "Lithuania",
      "LV", "Latvia",
      "EE", "Estonia",
      "IS", "Iceland",
      "LU", "Luxembourg",
      "AE", "United Arab Emirates"};

  private static String lookup(String[] table, String key) {
    for (int i = 0; i < table.length; i += 2) {
      if (table[i].equals(key)) {
        return table[i + 1];
      }
    }
    return key;
  }

  public String getDisplayLanguage() {
    return getDisplayLanguage(getDefault(Category.DISPLAY));
  }

  public String getDisplayLanguage(Locale inLocale) {
    return language.isEmpty() ? "" : lookup(kLanguageNames, language);
  }

  public String getDisplayCountry() {
    return getDisplayCountry(getDefault(Category.DISPLAY));
  }

  public String getDisplayCountry(Locale inLocale) {
    return country.isEmpty() ? "" : lookup(kCountryNames, country);
  }

  public String getDisplayVariant() {
    return variant;
  }

  public String getDisplayVariant(Locale inLocale) {
    return variant;
  }

  public String getDisplayName() {
    return getDisplayName(getDefault(Category.DISPLAY));
  }

  public String getDisplayName(Locale inLocale) {
    String lang = getDisplayLanguage(inLocale);
    String ctry = getDisplayCountry(inLocale);
    StringBuilder qualifiers = new StringBuilder();
    if (!ctry.isEmpty()) {
      qualifiers.append(ctry);
    }
    if (!variant.isEmpty()) {
      if (qualifiers.length() > 0) {
        qualifiers.append(", ");
      }
      qualifiers.append(variant);
    }
    if (lang.isEmpty()) {
      return qualifiers.toString();
    }
    if (qualifiers.length() == 0) {
      return lang;
    }
    return lang + " (" + qualifiers + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Locale)) return false;
    Locale other = (Locale) obj;
    return language.equalsIgnoreCase(other.language)
        && country.equalsIgnoreCase(other.country)
        && variant.equalsIgnoreCase(other.variant)
        && script.equalsIgnoreCase(other.script);
  }

  @Override
  public int hashCode() {
    return language.toLowerCase(ROOT).hashCode()
        ^ country.toUpperCase(ROOT).hashCode()
        ^ variant.hashCode()
        ^ script.hashCode();
  }

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.getMessage());
    }
  }

  @Override
  public final String toString() {
    boolean hasLanguage = !language.isEmpty();
    boolean hasCountry = !country.isEmpty();
    boolean hasVariant = !variant.isEmpty();

    if (!hasLanguage && !hasCountry) return "";
    return language + (hasCountry || hasVariant ? "_" + country : "") + (hasVariant ? "_" + variant : "");
  }

  public static final class Builder {
    private String language = "";
    private String script = "";
    private String region = "";
    private String variant = "";

    public Builder() {}

    public Builder setLocale(Locale locale) {
      Objects.requireNonNull(locale, "locale");
      this.language = locale.getLanguage();
      this.script = locale.getScript();
      this.region = locale.getCountry();
      this.variant = locale.getVariant();
      return this;
    }

    public Builder setLanguage(String language) {
      this.language = language != null ? language : "";
      return this;
    }

    public Builder setScript(String script) {
      this.script = script != null ? script : "";
      return this;
    }

    public Builder setRegion(String region) {
      this.region = region != null ? region : "";
      return this;
    }

    public Builder setVariant(String variant) {
      this.variant = variant != null ? variant : "";
      return this;
    }

    public Builder setLanguageTag(String languageTag) {
      Locale l = Locale.forLanguageTag(languageTag);
      return setLocale(l);
    }

    public Builder clear() {
      language = "";
      script = "";
      region = "";
      variant = "";
      return this;
    }

    public Builder clearExtensions() {
      return this;
    }

    public Locale build() {
      return new Locale(language, region, variant, script);
    }
  }
}
