/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Provider extends Properties {
  private static final long serialVersionUID = -4298000515446427739L;

  private String name;
  private String info;
  private String versionStr;
  private double version;

  private transient boolean legacyChanged;
  private transient Map<ServiceKey, Service> serviceMap;
  private transient Map<ServiceKey, Service> legacyMap;
  private transient Set<Service> serviceSet;
  private transient Set<Service> legacyServices;
  private transient boolean registering;

  @Deprecated
  protected Provider(String name, double version, String info) {
    this.name = name;
    this.version = version;
    this.versionStr = Double.toString(version);
    this.info = info;
    putId();
  }

  protected Provider(String name, String versionStr, String info) {
    this.name = name;
    this.versionStr = versionStr;
    this.info = info;
    this.version = parseVersionStr(versionStr);
    putId();
  }

  private static double parseVersionStr(String s) {
    try {
      int firstDotIdx = s.indexOf('.');
      int nextDotIdx = s.indexOf('.', firstDotIdx + 1);
      if (nextDotIdx != -1) {
        s = s.substring(0, nextDotIdx);
      }
      int endIdx = s.indexOf('-');
      if (endIdx > 0) {
        s = s.substring(0, endIdx);
      }
      endIdx = s.indexOf('+');
      if (endIdx > 0) {
        s = s.substring(0, endIdx);
      }
      return Double.parseDouble(s);
    } catch (NumberFormatException | NullPointerException e) {
      return 0;
    }
  }

  private void putId() {
    super.put("Provider.id name", String.valueOf(name));
    super.put("Provider.id version", String.valueOf(version));
    super.put("Provider.id info", String.valueOf(info));
    super.put("Provider.id className", this.getClass().getName());
  }

  public Provider configure(String configArg) {
    throw new UnsupportedOperationException("configure is not supported");
  }

  public boolean isConfigured() {
    return true;
  }

  public String getName() {
    return name;
  }

  @Deprecated
  public double getVersion() {
    return version;
  }

  public String getVersionStr() {
    return versionStr;
  }

  public String getInfo() {
    return info;
  }

  @Override
  public String toString() {
    return name + " version " + versionStr;
  }

  @Override
  public synchronized void clear() {
    legacyChanged = true;
    serviceMap = null;
    legacyMap = null;
    serviceSet = null;
    legacyServices = null;
    super.clear();
    putId();
  }

  @Override
  public synchronized void load(InputStream inStream) throws IOException {
    Properties tempProperties = new Properties();
    tempProperties.load(inStream);
    implPutAll(tempProperties);
  }

  @Override
  public synchronized void putAll(Map<?, ?> t) {
    implPutAll(t);
  }

  private void implPutAll(Map<?, ?> t) {
    for (Map.Entry<?, ?> e : t.entrySet()) {
      implPut(e.getKey(), e.getValue());
    }
  }

  @Override
  public synchronized Object put(Object key, Object value) {
    return implPut(key, value);
  }

  @Override
  public synchronized Object putIfAbsent(Object key, Object value) {
    if (!containsKey(key)) {
      return implPut(key, value);
    }
    return get(key);
  }

  @Override
  public synchronized Object remove(Object key) {
    return implRemove(key);
  }

  @Override
  public synchronized boolean remove(Object key, Object value) {
    if (containsKey(key) && Objects.equals(get(key), value)) {
      implRemove(key);
      return true;
    }
    return false;
  }

  @Override
  public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
    if (containsKey(key) && Objects.equals(get(key), oldValue)) {
      implPut(key, newValue);
      return true;
    }
    return false;
  }

  @Override
  public synchronized Object replace(Object key, Object value) {
    if (containsKey(key)) {
      return implPut(key, value);
    }
    return null;
  }

  @Override
  public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
    for (Object key : new ArrayList<Object>(keySet())) {
      implPut(key, function.apply(key, get(key)));
    }
  }

  @Override
  public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
    Object oldValue = get(key);
    Object newValue = remappingFunction.apply(key, oldValue);
    if (newValue == null) {
      if (oldValue != null || containsKey(key)) {
        implRemove(key);
      }
      return null;
    }
    implPut(key, newValue);
    return newValue;
  }

  @Override
  public synchronized Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
    Object oldValue = get(key);
    if (oldValue == null) {
      Object newValue = mappingFunction.apply(key);
      if (newValue != null) {
        implPut(key, newValue);
      }
      return newValue;
    }
    return oldValue;
  }

  @Override
  public synchronized Object computeIfPresent(Object key,
      BiFunction<? super Object, ? super Object, ?> remappingFunction) {
    Object oldValue = get(key);
    if (oldValue != null) {
      Object newValue = remappingFunction.apply(key, oldValue);
      if (newValue != null) {
        implPut(key, newValue);
      } else {
        implRemove(key);
      }
      return newValue;
    }
    return null;
  }

  @Override
  public synchronized Object merge(Object key, Object value,
      BiFunction<? super Object, ? super Object, ?> remappingFunction) {
    Object oldValue = get(key);
    Object newValue = oldValue == null ? value : remappingFunction.apply(oldValue, value);
    if (newValue == null) {
      implRemove(key);
    } else {
      implPut(key, newValue);
    }
    return newValue;
  }

  private Object implRemove(Object key) {
    if (key instanceof String) {
      String keyString = (String) key;
      if (keyString.startsWith("Provider.")) {
        return null;
      }
      legacyChanged = true;
      if (legacyMap != null) {
        legacyMap.remove(null);
      }
      removeFromSets();
    }
    return super.remove(key);
  }

  private Object implPut(Object key, Object value) {
    if ((key instanceof String) && (value instanceof String)) {
      if (((String) key).startsWith("Provider.")) {
        return null;
      }
      legacyChanged = true;
      removeFromSets();
    }
    return super.put(key, value);
  }

  private void removeFromSets() {
    serviceSet = null;
    legacyServices = null;
    legacyMap = null;
  }

  private static final class ServiceKey {
    private final String type;
    private final String algorithm;
    private final String originalAlgorithm;

    private ServiceKey(String type, String algorithm, boolean intern) {
      this.type = type;
      this.originalAlgorithm = algorithm;
      this.algorithm = algorithm.toUpperCase(Locale.ENGLISH);
    }

    @Override
    public int hashCode() {
      return type.hashCode() * 31 + algorithm.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof ServiceKey)) {
        return false;
      }
      ServiceKey other = (ServiceKey) obj;
      return type.equals(other.type) && algorithm.equals(other.algorithm);
    }

    boolean matches(String type, String algorithm) {
      return this.type == type && this.originalAlgorithm == algorithm;
    }
  }

  private static final String[] kAliasPrefixes = {"Alg.Alias."};

  private void ensureLegacyParsed() {
    if (!legacyChanged && legacyMap != null) {
      return;
    }
    if (legacyMap == null || legacyChanged) {
      parseLegacy();
    }
  }

  private void parseLegacy() {
    legacyChanged = false;
    Map<ServiceKey, Service> map = new LinkedHashMap<ServiceKey, Service>();
    legacyMap = map;
    legacyServices = null;
    List<Object> keys = new ArrayList<Object>(super.keySet());
    for (Object o : keys) {
      if (!(o instanceof String)) {
        continue;
      }
      String key = (String) o;
      if (key.startsWith("Provider.")) {
        continue;
      }
      Object v = super.get(key);
      if (!(v instanceof String)) {
        continue;
      }
      String value = (String) v;
      if (key.regionMatches(true, 0, "Alg.Alias.", 0, 10)) {
        String stdAlg = value;
        String aliasKey = key.substring(10);
        int dot = aliasKey.indexOf('.');
        if (dot < 0) {
          continue;
        }
        String type = aliasKey.substring(0, dot);
        String aliasAlg = aliasKey.substring(dot + 1);
        if (serviceMap != null && serviceMap.containsKey(new ServiceKey(type, aliasAlg, true))) {
          continue;
        }
        ServiceKey sk = new ServiceKey(type, stdAlg, true);
        Service s = map.get(sk);
        if (s == null) {
          s = new Service(this, type, stdAlg);
          map.put(sk, s);
        }
        s.addAlias(aliasAlg);
      } else {
        int sp = key.indexOf(' ');
        String head = sp < 0 ? key : key.substring(0, sp);
        int dot = head.indexOf('.');
        if (dot < 0) {
          continue;
        }
        String type = head.substring(0, dot);
        String alg = head.substring(dot + 1);
        if (serviceMap != null && serviceMap.containsKey(new ServiceKey(type, alg, true))) {
          continue;
        }
        if (sp < 0) {
          ServiceKey sk = new ServiceKey(type, alg, true);
          Service s = map.get(sk);
          if (s == null) {
            s = new Service(this, type, alg);
            map.put(sk, s);
          }
          s.className = value;
        } else {
          String attr = key.substring(sp + 1).trim();
          ServiceKey sk = new ServiceKey(type, alg, true);
          Service s = map.get(sk);
          if (s == null) {
            s = new Service(this, type, alg);
            map.put(sk, s);
          }
          s.addAttribute(attr, value);
        }
      }
    }
    for (Service s : new ArrayList<Service>(map.values())) {
      for (String alias : s.aliases) {
        ServiceKey ak = new ServiceKey(s.type, alias, true);
        if (!map.containsKey(ak)) {
          map.put(ak, s);
        }
      }
    }
  }

  public synchronized Service getService(String type, String algorithm) {
    Objects.requireNonNull(type);
    Objects.requireNonNull(algorithm);
    ServiceKey key = new ServiceKey(type, algorithm, false);
    if (serviceMap != null) {
      Service service = serviceMap.get(key);
      if (service != null) {
        return service;
      }
    }
    ensureLegacyParsed();
    return legacyMap != null ? legacyMap.get(key) : null;
  }

  public synchronized Set<Service> getServices() {
    ensureLegacyParsed();
    if (serviceSet == null) {
      Set<Service> set = new LinkedHashSet<Service>();
      if (serviceMap != null) {
        set.addAll(serviceMap.values());
      }
      if (legacyMap != null) {
        for (Service s : legacyMap.values()) {
          if (s.className != null) {
            set.add(s);
          }
        }
      }
      serviceSet = Collections.unmodifiableSet(set);
    }
    return serviceSet;
  }

  protected synchronized void putService(Service s) {
    Objects.requireNonNull(s);
    if (s.getProvider() != this) {
      throw new IllegalArgumentException("service.getProvider() must match this Provider object");
    }
    if (serviceMap == null) {
      serviceMap = new LinkedHashMap<ServiceKey, Service>();
    }
    registering = true;
    try {
      String type = s.getType();
      String algo = s.getAlgorithm();
      ServiceKey key = new ServiceKey(type, algo, true);
      implRemoveService(serviceMap.get(key));
      serviceMap.put(key, s);
      for (String alias : s.getAliases()) {
        serviceMap.put(new ServiceKey(type, alias, true), s);
      }
      putPropertyStrings(s);
    } finally {
      registering = false;
      serviceSet = null;
    }
  }

  private void putPropertyStrings(Service s) {
    String type = s.getType();
    String algo = s.getAlgorithm();
    super.put(type + "." + algo, s.getClassName());
    for (String alias : s.getAliases()) {
      super.put("Alg.Alias." + type + "." + alias, algo);
    }
    for (Map.Entry<String, String> e : s.attributes.entrySet()) {
      super.put(type + "." + algo + " " + e.getKey(), e.getValue());
    }
  }

  private void removePropertyStrings(Service s) {
    String type = s.getType();
    String algo = s.getAlgorithm();
    super.remove(type + "." + algo);
    for (String alias : s.getAliases()) {
      super.remove("Alg.Alias." + type + "." + alias);
    }
    for (Map.Entry<String, String> e : s.attributes.entrySet()) {
      super.remove(type + "." + algo + " " + e.getKey());
    }
  }

  protected synchronized void removeService(Service s) {
    Objects.requireNonNull(s);
    implRemoveService(s);
    serviceSet = null;
  }

  private void implRemoveService(Service s) {
    if (s == null || serviceMap == null) {
      return;
    }
    String type = s.getType();
    String algo = s.getAlgorithm();
    ServiceKey key = new ServiceKey(type, algo, false);
    Service oldService = serviceMap.get(key);
    if (s != oldService) {
      return;
    }
    serviceMap.remove(key);
    for (String alias : s.getAliases()) {
      serviceMap.remove(new ServiceKey(type, alias, false));
    }
    removePropertyStrings(s);
  }

  public static class Service {
    private final String type;
    private final String algorithm;
    private String className;
    private final Provider provider;
    private List<String> aliases;
    private Map<String, String> attributes;

    public Service(Provider provider, String type, String algorithm, String className, List<String> aliases,
        Map<String, String> attributes) {
      if (provider == null || type == null || algorithm == null || className == null) {
        throw new NullPointerException();
      }
      this.provider = provider;
      this.type = type;
      this.algorithm = algorithm;
      this.className = className;
      this.aliases = aliases == null ? new ArrayList<String>() : new ArrayList<String>(aliases);
      this.attributes = attributes == null ? new HashMap<String, String>() : new HashMap<String, String>(attributes);
    }

    private Service(Provider provider, String type, String algorithm) {
      this.provider = provider;
      this.type = type;
      this.algorithm = algorithm;
      this.className = null;
      this.aliases = new ArrayList<String>();
      this.attributes = new HashMap<String, String>();
    }

    private void addAlias(String alias) {
      if (!aliases.contains(alias)) {
        aliases.add(alias);
      }
    }

    private void addAttribute(String name, String value) {
      attributes.put(name, value);
    }

    public final String getType() {
      return type;
    }

    public final String getAlgorithm() {
      return algorithm;
    }

    public final Provider getProvider() {
      return provider;
    }

    public final String getClassName() {
      return className;
    }

    private List<String> getAliases() {
      return aliases;
    }

    public final String getAttribute(String name) {
      Objects.requireNonNull(name);
      return attributes.get(name);
    }

    private static String engineParameterClass(String type) {
      switch (type) {
      case "SecureRandom":
        return "java.security.SecureRandomParameters";
      case "CertStore":
        return "java.security.cert.CertStoreParameters";
      case "Policy":
        return "java.security.Policy$Parameters";
      case "Configuration":
        return "javax.security.auth.login.Configuration$Parameters";
      case "KDF":
        return "javax.crypto.KDFParameters";
      default:
        return null;
      }
    }

    public Object newInstance(Object constructorParameter) throws NoSuchAlgorithmException {
      try {
        ClassLoader cl = provider.getClass().getClassLoader();
        Class<?> clazz = cl == null ? Class.forName(className) : Class.forName(className, true, cl);
        String paramClassName = engineParameterClass(type);
        if (paramClassName != null) {
          Class<?> paramClass = Class.forName(paramClassName);
          if (constructorParameter != null && !paramClass.isInstance(constructorParameter)) {
            throw new InvalidParameterException("constructorParameter must be instanceof "
                + paramClassName.replace('$', '.') + " for engine type " + type);
          }
          return clazz.getConstructor(paramClass).newInstance(constructorParameter);
        }
        if (constructorParameter == null) {
          return clazz.getConstructor().newInstance();
        }
        Class<?> argClass = constructorParameter.getClass();
        for (java.lang.reflect.Constructor<?> c : clazz.getConstructors()) {
          Class<?>[] params = c.getParameterTypes();
          if (params.length == 1 && params[0].isAssignableFrom(argClass)) {
            return c.newInstance(constructorParameter);
          }
        }
        throw new NoSuchAlgorithmException("No public constructor matching " + argClass.getName()
            + " found in class " + className);
      } catch (NoSuchAlgorithmException | InvalidParameterException e) {
        throw e;
      } catch (ClassNotFoundException e) {
        throw new NoSuchAlgorithmException("class configured for " + type + " (provider: " + provider.getName()
            + ") cannot be found.", e);
      } catch (java.lang.reflect.InvocationTargetException e) {
        throw new NoSuchAlgorithmException("Error constructing implementation (algorithm: " + algorithm
            + ", provider: " + provider.getName() + ", class: " + className + ")", e.getCause());
      } catch (Exception e) {
        throw new NoSuchAlgorithmException("Error constructing implementation (algorithm: " + algorithm
            + ", provider: " + provider.getName() + ", class: " + className + ")", e);
      }
    }

    @Deprecated
    public boolean supportsParameter(Object parameter) {
      if (parameter != null && !(parameter instanceof Key)) {
        return true;
      }
      switch (type) {
      case "Cipher":
      case "Signature":
      case "Mac":
      case "KeyAgreement":
      case "KEM":
        break;
      default:
        return true;
      }
      Key key = (Key) parameter;
      String classes = attributes.get("SupportedKeyClasses");
      if (key == null) {
        return attributes.get("SupportedKeyFormats") == null;
      }
      if (classes != null) {
        boolean ok = false;
        for (String name : classes.split("\\|")) {
          if (keyHasClassName(key.getClass(), name)) {
            ok = true;
            break;
          }
        }
        if (!ok) {
          return false;
        }
      }
      String formats = attributes.get("SupportedKeyFormats");
      if (formats != null) {
        String format = key.getFormat();
        boolean ok = false;
        for (String f : formats.split("\\|")) {
          if (f.equals(format)) {
            ok = true;
            break;
          }
        }
        return ok;
      }
      return true;
    }

    private static boolean keyHasClassName(Class<?> clazz, String name) {
      for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
        if (c.getName().equals(name)) {
          return true;
        }
        for (Class<?> i : c.getInterfaces()) {
          if (keyHasClassName(i, name)) {
            return true;
          }
        }
      }
      return false;
    }

    @Override
    public String toString() {
      String aString = aliases.isEmpty() ? "" : "\r\n  aliases: " + aliases.toString();
      String attrs = attributes.isEmpty() ? "" : "\r\n  attributes: " + attributes.toString();
      return provider.getName() + ": " + type + "." + algorithm + " -> " + className + aString + attrs + "\r\n";
    }
  }
}
