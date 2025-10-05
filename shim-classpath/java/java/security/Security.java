/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import sun.security.jca.Providers;

public final class Security {
  private static final Properties props = new Properties();

  static {
    String[][] defaults = {
      {"securerandom.source", "file:/dev/random"},
      {"securerandom.strongAlgorithms", "NativePRNGBlocking:SUN,DRBG:SUN"},
      {"login.configuration.provider", "sun.security.provider.ConfigFile"},
      {"policy.provider", "sun.security.provider.PolicyFile"},
      {"policy.expandProperties", "true"},
      {"policy.allowSystemProperty", "true"},
      {"policy.ignoreIdentityScope", "false"},
      {"keystore.type", "pkcs12"},
      {"keystore.type.compat", "true"},
      {"package.access", "sun.misc.,sun.reflect."},
      {"package.definition", "sun.misc.,sun.reflect."},
      {"security.overridePropertiesFile", "true"},
      {"ssl.KeyManagerFactory.algorithm", "SunX509"},
      {"ssl.TrustManagerFactory.algorithm", "PKIX"},
      {"networkaddress.cache.negative.ttl", "10"},
      {"krb5.kdc.bad.policy", "tryLast"},
      {"sun.security.krb5.disableReferrals", "false"},
      {"sun.security.krb5.maxReferrals", "5"},
      {"jdk.certpath.disabledAlgorithms", "MD2, MD5, SHA1 jdkCA & usage TLSServer, RSA keySize < 1024, DSA keySize < 1024, EC keySize < 224, SHA1 usage SignedJAR & denyAfter 2019-01-01"},
      {"jdk.security.legacyAlgorithms", "RSA keySize < 2048, DSA keySize < 2048, DES, DESede, MD5, RC2, ARCFOUR"},
      {"jdk.jar.disabledAlgorithms", "MD2, MD5, RSA keySize < 1024, DSA keySize < 1024, SHA1 denyAfter 2019-01-01"},
      {"jdk.tls.disabledAlgorithms", "SSLv3, TLSv1, TLSv1.1, DTLSv1.0, RC4, DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL, ECDH"},
      {"jdk.tls.legacyAlgorithms", "NULL, anon, RC4, DES, 3DES_EDE_CBC"},
      {"jdk.tls.keyLimits", "AES/GCM/NoPadding KeyUpdate 2^37, ChaCha20-Poly1305 KeyUpdate 2^37"},
      {"crypto.policy", "unlimited"},
      {"jdk.xml.dsig.secureValidationPolicy", "disallowAlg http://www.w3.org/TR/1999/REC-xslt-19991116"},
      {"jceks.key.serialFilter", "java.base/java.lang.Enum;java.base/java.security.KeyRep;java.base/java.security.KeyRep$Type;java.base/javax.crypto.spec.SecretKeySpec;!*"},
      {"jdk.sasl.disabledMechanisms", ""},
      {"jdk.security.caDistrustPolicies", "SYMANTEC_TLS"},
      {"jdk.io.permissionsUseCanonicalPath", "false"},
      {"jdk.tls.alpnCharset", "ISO_8859_1"},
    };
    for (String[] kv : defaults) {
      props.setProperty(kv[0], kv[1]);
    }
  }

  private Security() {
  }

  @Deprecated
  public static String getAlgorithmProperty(String algName, String propName) {
    return null;
  }

  public static synchronized int insertProviderAt(Provider provider, int position) {
    Objects.requireNonNull(provider);
    return Providers.insert(provider, position);
  }

  public static int addProvider(Provider provider) {
    return insertProviderAt(provider, 0);
  }

  public static synchronized void removeProvider(String name) {
    Providers.remove(name);
  }

  public static Provider[] getProviders() {
    return Providers.array();
  }

  public static Provider getProvider(String name) {
    return Providers.named(name);
  }

  public static Provider[] getProviders(String filter) {
    String key = null;
    String value = null;
    int index = filter.indexOf(':');
    if (index == -1) {
      key = filter;
      value = "";
    } else {
      key = filter.substring(0, index);
      value = filter.substring(index + 1);
    }
    Map<String, String> hashtableFilter = new HashMap<String, String>(1);
    hashtableFilter.put(key, value);
    return getProviders(hashtableFilter);
  }

  public static Provider[] getProviders(Map<String, String> filter) {
    java.util.List<Provider> result = new java.util.ArrayList<Provider>();
    for (Provider p : Providers.list()) {
      if (matches(p, filter)) {
        result.add(p);
      }
    }
    return result.isEmpty() ? null : result.toArray(new Provider[0]);
  }

  private static boolean matches(Provider p, Map<String, String> filter) {
    for (Map.Entry<String, String> e : filter.entrySet()) {
      String key = e.getKey();
      String expected = e.getValue();
      int space = key.indexOf(' ');
      String head = space < 0 ? key : key.substring(0, space);
      String attr = space < 0 ? null : key.substring(space + 1).trim();
      int dot = head.indexOf('.');
      if (dot < 0) {
        throw new InvalidParameterException("Invalid filter");
      }
      String type = head.substring(0, dot);
      String alg = head.substring(dot + 1);
      Provider.Service s = p.getService(type, alg);
      if (s == null) {
        return false;
      }
      if (attr != null) {
        String actual = s.getAttribute(attr);
        if (actual == null || !actual.equalsIgnoreCase(expected)) {
          return false;
        }
      }
    }
    return true;
  }

  public static String getProperty(String key) {
    return props.getProperty(key);
  }

  public static void setProperty(String key, String datum) {
    props.setProperty(key, datum);
  }

  public static Set<String> getAlgorithms(String serviceName) {
    if (serviceName == null || serviceName.isEmpty() || serviceName.endsWith(".")) {
      return java.util.Collections.emptySet();
    }
    HashSet<String> result = new HashSet<String>();
    for (Provider p : Providers.list()) {
      for (Provider.Service s : p.getServices()) {
        if (s.getType().equalsIgnoreCase(serviceName)) {
          result.add(s.getAlgorithm().toUpperCase(Locale.ENGLISH));
        }
      }
    }
    return java.util.Collections.unmodifiableSet(result);
  }
}
