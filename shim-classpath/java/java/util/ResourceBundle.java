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
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class ResourceBundle {
  private static final ResourceBundle NONEXISTENT = new ResourceBundle() {
    public Enumeration<String> getKeys() {
      return null;
    }

    protected Object handleGetObject(String key) {
      return null;
    }

    public String toString() {
      return "NONEXISTENT_BUNDLE";
    }
  };

  private static final class Entry {
    final ResourceBundle bundle;
    final String format;
    final long loadTime;
    long expiration;

    Entry(ResourceBundle bundle, String format, long loadTime, long expiration) {
      this.bundle = bundle;
      this.format = format;
      this.loadTime = loadTime;
      this.expiration = expiration;
    }
  }

  private static final Map<Object, Map<String, Entry>> kCache = new WeakHashMap<Object, Map<String, Entry>>();
  private static final Object kNullLoader = new Object();

  protected ResourceBundle parent;
  private Locale locale;
  private String name;
  private volatile Set<String> keySet;

  public ResourceBundle() { }

  public String getBaseBundleName() {
    return name;
  }

  public final String getString(String key) {
    return (String) getObject(key);
  }

  public final String[] getStringArray(String key) {
    return (String[]) getObject(key);
  }

  public final Object getObject(String key) {
    Object obj = handleGetObject(key);
    if (obj == null) {
      if (parent != null) {
        obj = parent.getObject(key);
      }
      if (obj == null) {
        throw new MissingResourceException("Can't find resource for bundle " + this.getClass().getName()
            + ", key " + key, this.getClass().getName(), key);
      }
    }
    return obj;
  }

  public Locale getLocale() {
    return locale;
  }

  protected void setParent(ResourceBundle parent) {
    this.parent = parent;
  }

  protected abstract Object handleGetObject(String key);

  public abstract Enumeration<String> getKeys();

  public boolean containsKey(String key) {
    if (key == null) {
      throw new NullPointerException();
    }
    for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
      if (rb.handleKeySet().contains(key)) {
        return true;
      }
    }
    return false;
  }

  public Set<String> keySet() {
    Set<String> keys = new HashSet<String>();
    for (ResourceBundle rb = this; rb != null; rb = rb.parent) {
      keys.addAll(rb.handleKeySet());
    }
    return keys;
  }

  protected Set<String> handleKeySet() {
    if (keySet == null) {
      Set<String> keys = new HashSet<String>();
      Enumeration<String> enumKeys = getKeys();
      while (enumKeys.hasMoreElements()) {
        String key = enumKeys.nextElement();
        if (handleGetObject(key) != null) {
          keys.add(key);
        }
      }
      keySet = keys;
    }
    return keySet;
  }

  private static ClassLoader callerLoader() {
    return Method.getCaller().class_.loader;
  }

  public static final ResourceBundle getBundle(String baseName) {
    return getBundleImpl(baseName, Locale.getDefault(), callerLoader(), Control.INSTANCE);
  }

  public static final ResourceBundle getBundle(String baseName, Control control) {
    return getBundleImpl(baseName, Locale.getDefault(), callerLoader(), control);
  }

  public static final ResourceBundle getBundle(String baseName, Locale locale) {
    return getBundleImpl(baseName, locale, callerLoader(), Control.INSTANCE);
  }

  public static ResourceBundle getBundle(String baseName, Module module) {
    return getBundleImpl(baseName, Locale.getDefault(), moduleLoader(module), Control.INSTANCE);
  }

  public static ResourceBundle getBundle(String baseName, Locale targetLocale, Module module) {
    return getBundleImpl(baseName, targetLocale, moduleLoader(module), Control.INSTANCE);
  }

  private static ClassLoader moduleLoader(Module module) {
    Objects.requireNonNull(module);
    ClassLoader loader = module.getClassLoader();
    return loader != null ? loader : ClassLoader.getSystemClassLoader();
  }

  public static final ResourceBundle getBundle(String baseName, Locale targetLocale, Control control) {
    return getBundleImpl(baseName, targetLocale, callerLoader(), control);
  }

  public static ResourceBundle getBundle(String baseName, Locale locale, ClassLoader loader) {
    if (loader == null) {
      throw new NullPointerException();
    }
    return getBundleImpl(baseName, locale, loader, Control.INSTANCE);
  }

  public static ResourceBundle getBundle(String baseName, Locale targetLocale, ClassLoader loader,
      Control control) {
    if (loader == null) {
      throw new NullPointerException();
    }
    return getBundleImpl(baseName, targetLocale, loader, control);
  }

  private static ResourceBundle getBundleImpl(String baseName, Locale locale, ClassLoader loader,
      Control control) {
    if (baseName == null || locale == null || control == null) {
      throw new NullPointerException();
    }
    if (loader == null) {
      loader = ClassLoader.getSystemClassLoader();
    }
    List<String> formats = control.getFormats(baseName);
    ResourceBundle baseBundle = null;
    ResourceBundle bundle = null;
    Throwable[] cause = new Throwable[1];
    for (Locale targetLocale = locale; targetLocale != null;
         targetLocale = control.getFallbackLocale(baseName, targetLocale)) {
      List<Locale> candidates = control.getCandidateLocales(baseName, targetLocale);
      bundle = findBundle(baseName, loader, candidates, formats, 0, control, cause);
      if (bundle != null) {
        boolean isBase = Locale.ROOT.equals(bundle.locale);
        if (!isBase || bundle.locale.equals(locale)
            || (candidates.size() == 1 && bundle.locale.equals(candidates.get(0)))) {
          break;
        }
        if (baseBundle == null) {
          baseBundle = bundle;
        }
      }
    }
    if (bundle == null) {
      if (baseBundle == null) {
        MissingResourceException e = new MissingResourceException("Can't find bundle for base name " + baseName
            + ", locale " + locale, baseName + "_" + locale, "");
        if (cause[0] != null) {
          e.initCause(cause[0]);
        }
        throw e;
      }
      bundle = baseBundle;
    }
    return bundle;
  }

  private static Map<String, Entry> cacheFor(ClassLoader loader) {
    Object key = loader == null ? kNullLoader : loader;
    Map<String, Entry> map = kCache.get(key);
    if (map == null) {
      map = new HashMap<String, Entry>();
      kCache.put(key, map);
    }
    return map;
  }

  private static ResourceBundle findBundle(String baseName, ClassLoader loader, List<Locale> candidates,
      List<String> formats, int index, Control control, Throwable[] cause) {
    Locale targetLocale = candidates.get(index);
    ResourceBundle parent = null;
    if (index != candidates.size() - 1) {
      parent = findBundle(baseName, loader, candidates, formats, index + 1, control, cause);
    }
    String key = baseName + '\u0000' + targetLocale;
    ResourceBundle bundle = null;
    synchronized (kCache) {
      Entry entry = cacheFor(loader).get(key);
      if (entry != null) {
        long now = System.currentTimeMillis();
        if (entry.expiration >= 0 && entry.expiration <= now) {
          boolean reload = entry.bundle == NONEXISTENT
              || control.needsReload(baseName, targetLocale, entry.format, loader, entry.bundle, entry.loadTime);
          if (reload) {
            cacheFor(loader).remove(key);
            entry = null;
          } else {
            long ttl = control.getTimeToLive(baseName, targetLocale);
            entry.expiration = ttl >= 0 ? now + ttl : ttl;
          }
        }
      }
      if (entry != null) {
        if (entry.bundle == NONEXISTENT) {
          return parent;
        }
        bundle = entry.bundle;
      }
    }
    if (bundle == null) {
      String foundFormat = null;
      for (String format : formats) {
        try {
          ResourceBundle b = control.newBundle(baseName, targetLocale, format, loader, false);
          if (b != null) {
            bundle = b;
            foundFormat = format;
            break;
          }
        } catch (Exception e) {
          cause[0] = e;
        } catch (LinkageError e) {
          cause[0] = e;
        }
      }
      long ttl = control.getTimeToLive(baseName, targetLocale);
      if (ttl != Control.TTL_DONT_CACHE) {
        long now = System.currentTimeMillis();
        synchronized (kCache) {
          cacheFor(loader).put(key, new Entry(bundle == null ? NONEXISTENT : bundle, foundFormat, now,
              ttl >= 0 ? now + ttl : ttl));
        }
      }
      if (bundle == null) {
        return parent;
      }
    }
    bundle.locale = targetLocale;
    bundle.name = baseName;
    if (parent != null) {
      bundle.setParent(parent);
    }
    return bundle;
  }

  public static final void clearCache() {
    clearCache(callerLoader());
  }

  public static final void clearCache(ClassLoader loader) {
    if (loader == null) {
      throw new NullPointerException();
    }
    synchronized (kCache) {
      kCache.remove(loader);
    }
  }

  public static class Control {
    public static final List<String> FORMAT_DEFAULT =
        Collections.unmodifiableList(Arrays.asList("java.class", "java.properties"));
    public static final List<String> FORMAT_CLASS = Collections.unmodifiableList(Arrays.asList("java.class"));
    public static final List<String> FORMAT_PROPERTIES =
        Collections.unmodifiableList(Arrays.asList("java.properties"));
    public static final long TTL_DONT_CACHE = -1;
    public static final long TTL_NO_EXPIRATION_CONTROL = -2;

    private static final Control INSTANCE = new Control();

    private static final class FormatControl extends Control {
      private final List<String> formats;
      private final boolean fallback;

      FormatControl(List<String> formats, boolean fallback) {
        this.formats = formats;
        this.fallback = fallback;
      }

      @Override
      public List<String> getFormats(String baseName) {
        if (baseName == null) {
          throw new NullPointerException();
        }
        return formats;
      }

      @Override
      public Locale getFallbackLocale(String baseName, Locale locale) {
        if (baseName == null) {
          throw new NullPointerException();
        }
        return fallback ? super.getFallbackLocale(baseName, locale) : null;
      }
    }

    private static final Control kClassOnly = new FormatControl(FORMAT_CLASS, true);
    private static final Control kPropertiesOnly = new FormatControl(FORMAT_PROPERTIES, true);
    private static final Control kClassOnlyNoFallback = new FormatControl(FORMAT_CLASS, false);
    private static final Control kPropertiesOnlyNoFallback = new FormatControl(FORMAT_PROPERTIES, false);
    private static final Control kDefaultNoFallback = new FormatControl(FORMAT_DEFAULT, false);

    protected Control() { }

    public static final Control getControl(List<String> formats) {
      if (formats.equals(FORMAT_PROPERTIES)) {
        return kPropertiesOnly;
      }
      if (formats.equals(FORMAT_CLASS)) {
        return kClassOnly;
      }
      if (formats.equals(FORMAT_DEFAULT)) {
        return INSTANCE;
      }
      throw new IllegalArgumentException();
    }

    public static final Control getNoFallbackControl(List<String> formats) {
      if (formats.equals(FORMAT_DEFAULT)) {
        return kDefaultNoFallback;
      }
      if (formats.equals(FORMAT_PROPERTIES)) {
        return kPropertiesOnlyNoFallback;
      }
      if (formats.equals(FORMAT_CLASS)) {
        return kClassOnlyNoFallback;
      }
      throw new IllegalArgumentException();
    }

    public List<String> getFormats(String baseName) {
      if (baseName == null) {
        throw new NullPointerException();
      }
      return FORMAT_DEFAULT;
    }

    public List<Locale> getCandidateLocales(String baseName, Locale locale) {
      if (baseName == null) {
        throw new NullPointerException();
      }
      return new ArrayList<Locale>(candidates(locale));
    }

    private static List<Locale> candidates(Locale base) {
      String language = base.getLanguage();
      String script = base.getScript();
      String region = base.getCountry();
      String variant = base.getVariant();
      boolean norwegianBokmal = false;
      boolean norwegianNynorsk = false;
      if (language.equals("no")) {
        if (region.equals("NO") && variant.equals("NY")) {
          variant = "";
          norwegianNynorsk = true;
        } else {
          norwegianBokmal = true;
        }
      }
      if (language.equals("nb") || norwegianBokmal) {
        List<Locale> tmp = defaultList("nb", script, region, variant, "");
        List<Locale> bokmal = new ArrayList<Locale>();
        for (Locale l : tmp) {
          bokmal.add(l);
          if (l.getLanguage().isEmpty()) {
            break;
          }
          bokmal.add(locale("no", l.getScript(), l.getCountry(), l.getVariant()));
        }
        return bokmal;
      } else if (language.equals("nn") || norwegianNynorsk) {
        List<Locale> nynorsk = defaultList("nn", script, region, variant, "");
        int idx = nynorsk.size() - 1;
        nynorsk.add(idx++, locale("no", "", "NO", "NY"));
        nynorsk.add(idx++, locale("no", "", "NO", ""));
        nynorsk.add(idx++, locale("no", "", "", ""));
        return nynorsk;
      }
      String suppliedRegion = "";
      if (language.equals("zh")) {
        if (script.isEmpty() && !region.isEmpty()) {
          if (region.equals("TW") || region.equals("HK") || region.equals("MO")) {
            script = "Hant";
          } else if (region.equals("CN") || region.equals("SG")) {
            script = "Hans";
          }
        } else if (!script.isEmpty() && region.isEmpty()) {
          if (script.equals("Hans")) {
            suppliedRegion = "CN";
          } else if (script.equals("Hant")) {
            suppliedRegion = "TW";
          }
        }
      }
      return defaultList(language, script, region, variant, suppliedRegion);
    }

    private static Locale locale(String language, String script, String region, String variant) {
      Locale.Builder b = new Locale.Builder();
      try {
        b.setLanguage(language).setScript(script).setRegion(region).setVariant(variant);
        return b.build();
      } catch (IllformedLocaleException e) {
        return new Locale(language, region, variant);
      }
    }

    private static List<Locale> defaultList(String language, String script, String region, String variant,
        String suppliedRegion) {
      List<String> variants = null;
      if (!variant.isEmpty()) {
        variants = new ArrayList<String>();
        int idx = variant.length();
        while (idx != -1) {
          variants.add(variant.substring(0, idx));
          idx = variant.lastIndexOf('_', --idx);
        }
      }
      List<Locale> list = new ArrayList<Locale>();
      if (variants != null) {
        for (String v : variants) {
          list.add(locale(language, script, region, v));
        }
      }
      if (!region.isEmpty()) {
        list.add(locale(language, script, region, ""));
      }
      if (!script.isEmpty()) {
        list.add(locale(language, script, "", ""));
        if (variants != null) {
          for (String v : variants) {
            list.add(locale(language, "", region, v));
          }
        }
        String noScriptRegion = region.isEmpty() ? suppliedRegion : region;
        if (!noScriptRegion.isEmpty()) {
          list.add(locale(language, "", noScriptRegion, ""));
        }
      }
      if (!language.isEmpty()) {
        list.add(locale(language, "", "", ""));
      }
      list.add(Locale.ROOT);
      return list;
    }

    public Locale getFallbackLocale(String baseName, Locale locale) {
      if (baseName == null) {
        throw new NullPointerException();
      }
      Locale defaultLocale = Locale.getDefault();
      return locale.equals(defaultLocale) ? null : defaultLocale;
    }

    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
        boolean reload) throws IllegalAccessException, InstantiationException, IOException {
      Objects.requireNonNull(baseName);
      Objects.requireNonNull(locale);
      Objects.requireNonNull(format);
      Objects.requireNonNull(loader);
      String bundleName = toBundleName(baseName, locale);
      ResourceBundle bundle = null;
      if (format.equals("java.class")) {
        try {
          Class<?> c = Class.forName(bundleName, true, loader);
          if (!ResourceBundle.class.isAssignableFrom(c)) {
            throw new ClassCastException(c.getName() + " cannot be cast to ResourceBundle");
          }
          if (!java.lang.reflect.Modifier.isPublic(c.getModifiers())) {
            throw new IllegalAccessException("Bundle class is not public: " + c.getName());
          }
          Constructor<?> ctor = c.getConstructor();
          bundle = (ResourceBundle) ctor.newInstance();
        } catch (ClassNotFoundException e) {
          // bundle class not present
        } catch (NoSuchMethodException e) {
          throw new IllegalAccessException(e.getMessage());
        } catch (java.lang.reflect.InvocationTargetException e) {
          throw new InstantiationException(e.getMessage());
        }
      } else if (format.equals("java.properties")) {
        String resourceName = toResourceName0(bundleName, "properties");
        if (resourceName == null) {
          return null;
        }
        InputStream stream = loader.getResourceAsStream(resourceName);
        if (stream != null) {
          try {
            bundle = new PropertyResourceBundle(stream);
          } finally {
            stream.close();
          }
        }
      } else {
        throw new IllegalArgumentException("unknown format: " + format);
      }
      return bundle;
    }

    public long getTimeToLive(String baseName, Locale locale) {
      if (baseName == null || locale == null) {
        throw new NullPointerException();
      }
      return TTL_NO_EXPIRATION_CONTROL;
    }

    public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader,
        ResourceBundle bundle, long loadTime) {
      if (bundle == null) {
        throw new NullPointerException();
      }
      if (format.equals("java.class") || format.equals("java.properties")) {
        format = format.substring(5);
      }
      boolean result = false;
      try {
        String resourceName = toResourceName0(toBundleName(baseName, locale), format);
        if (resourceName == null) {
          return result;
        }
        java.net.URL url = loader.getResource(resourceName);
        if (url != null) {
          java.net.URLConnection connection = url.openConnection();
          long lastModified = connection.getLastModified();
          result = lastModified >= loadTime;
        }
      } catch (NullPointerException e) {
        throw e;
      } catch (Exception e) {
        // treated as not needing a reload
      }
      return result;
    }

    public String toBundleName(String baseName, Locale locale) {
      if (locale == Locale.ROOT) {
        return baseName;
      }
      String language = locale.getLanguage();
      String script = locale.getScript();
      String country = locale.getCountry();
      String variant = locale.getVariant();
      if (language.isEmpty() && country.isEmpty() && variant.isEmpty()) {
        return baseName;
      }
      StringBuilder sb = new StringBuilder(baseName);
      sb.append('_');
      if (!script.isEmpty()) {
        if (!language.isEmpty()) {
          sb.append(language).append('_');
        }
        sb.append(script);
        if (!country.isEmpty()) {
          sb.append('_').append(country);
        }
        if (!variant.isEmpty()) {
          sb.append('_').append(variant);
        }
      } else {
        sb.append(language);
        if (!country.isEmpty() || !variant.isEmpty()) {
          sb.append('_').append(country);
        }
        if (!variant.isEmpty()) {
          sb.append('_').append(variant);
        }
      }
      return sb.toString();
    }

    public final String toResourceName(String bundleName, String suffix) {
      StringBuilder sb = new StringBuilder(bundleName.length() + 1 + suffix.length());
      sb.append(bundleName.replace('.', '/')).append('.').append(suffix);
      return sb.toString();
    }

    private String toResourceName0(String bundleName, String suffix) {
      if (bundleName.contains("://")) {
        return null;
      }
      return toResourceName(bundleName, suffix);
    }
  }
}
