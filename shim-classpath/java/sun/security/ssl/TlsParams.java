/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TlsParams {
  static final String[] kSupportedProtocols = {"TLSv1.3", "TLSv1.2"};
  static final String[] kLegacyProtocols = {"TLSv1.1", "TLSv1", "SSLv3", "SSLv2Hello"};
  static final String[] kDatagramProtocols = {"DTLSv1.2", "DTLSv1.0"};

  private TlsParams() {
  }

  static String[] supportedCipherSuites() {
    List<CipherSuite> all = CipherSuite.all();
    String[] out = new String[all.size()];
    for (int i = 0; i < out.length; i++) {
      out[i] = all.get(i).name;
    }
    return out;
  }

  static String[] supportedProtocols() {
    String[] out = java.util.Arrays.copyOf(kSupportedProtocols, kSupportedProtocols.length + kLegacyProtocols.length);
    System.arraycopy(kLegacyProtocols, 0, out, kSupportedProtocols.length, kLegacyProtocols.length);
    return out;
  }

  static String[] supportedCipherSuites(boolean datagram) {
    if (!datagram) {
      return supportedCipherSuites();
    }
    List<String> out = new ArrayList<String>();
    for (CipherSuite s : CipherSuite.all()) {
      if (!s.tls13) {
        out.add(s.name);
      }
    }
    return out.toArray(new String[0]);
  }

  private static volatile java.util.Set<String> disabledAlgorithms;

  static boolean permitted(String protocol) {
    java.util.Set<String> set = disabledAlgorithms;
    if (set == null) {
      set = new java.util.HashSet<String>();
      String property = java.security.Security.getProperty("jdk.tls.disabledAlgorithms");
      if (property != null) {
        for (String token : property.split(",")) {
          set.add(token.trim());
        }
      }
      disabledAlgorithms = set;
    }
    return !set.contains(protocol);
  }

  static int versionCode(String protocol, boolean datagram) {
    if (datagram) {
      return "DTLSv1.2".equals(protocol) ? TlsConnection.kTls12 : "DTLSv1.0".equals(protocol) ? TlsConnection.kTls11
          : 0;
    }
    return TlsConnection.enabledVersionCode(protocol);
  }

  static String[] availableProtocols(String[] candidates) {
    List<String> out = new ArrayList<String>();
    for (String p : candidates) {
      if (permitted(p)) {
        out.add(p);
      }
    }
    return out.toArray(new String[0]);
  }

  static List<Integer> versions(SslConfig cfg) {
    List<Integer> out = new ArrayList<Integer>();
    for (String p : cfg.protocols) {
      int code = permitted(p) ? versionCode(p, cfg.datagram) : 0;
      if (code != 0 && !out.contains(code)) {
        out.add(code);
      }
    }
    Collections.sort(out, Collections.reverseOrder());
    return out;
  }

  static List<CipherSuite> suites(SslConfig cfg, List<Integer> versions) {
    List<CipherSuite> out = new ArrayList<CipherSuite>();
    for (String n : cfg.cipherSuites) {
      CipherSuite s = CipherSuite.byName(n);
      if (s == null || s.scsv) {
        continue;
      }
      for (int v : versions) {
        if (s.supports(v)) {
          out.add(s);
          break;
        }
      }
    }
    return out;
  }

  static String[] applicableSuites(String[] protocols, boolean datagram) {
    List<Integer> versions = new ArrayList<Integer>();
    for (String p : protocols) {
      int code = versionCode(p, datagram);
      if (code != 0) {
        versions.add(code);
      }
    }
    List<String> out = new ArrayList<String>();
    for (CipherSuite s : CipherSuite.all()) {
      if (datagram && s.tls13) {
        continue;
      }
      for (int v : versions) {
        if (s.supports(v)) {
          out.add(s.name);
          break;
        }
      }
    }
    return out.toArray(new String[0]);
  }

  static List<NamedGroup> groups(SslConfig cfg) {
    List<NamedGroup> out = new ArrayList<NamedGroup>();
    if (cfg.namedGroups != null) {
      for (String n : cfg.namedGroups) {
        NamedGroup g = NamedGroup.byName(n);
        if (g != null) {
          out.add(g);
        }
      }
      return out;
    }
    return new ArrayList<NamedGroup>(NamedGroup.all());
  }

  static List<SignatureScheme> schemes(SslConfig cfg) {
    List<SignatureScheme> out = new ArrayList<SignatureScheme>();
    if (cfg.signatureSchemes != null) {
      for (String n : cfg.signatureSchemes) {
        SignatureScheme s = SignatureScheme.byName(n);
        if (s != null) {
          out.add(s);
        }
      }
      return out;
    }
    return new ArrayList<SignatureScheme>(SignatureScheme.all());
  }

  static void checkProtocols(String[] protocols, boolean datagram) {
    if (protocols == null) {
      throw new IllegalArgumentException("Protocols cannot be null");
    }
    for (String p : protocols) {
      boolean ok = false;
      for (String s : kSupportedProtocols) {
        ok |= s.equals(p);
      }
      for (String s : kDatagramProtocols) {
        ok |= datagram && s.equals(p);
      }
      for (String s : kLegacyProtocols) {
        ok |= !datagram && s.equals(p);
      }
      if (!ok) {
        throw new IllegalArgumentException("Unsupported protocol: " + p);
      }
    }
  }

  static void checkSuites(String[] suites) {
    if (suites == null) {
      throw new IllegalArgumentException("CipherSuites cannot be null");
    }
    for (String s : suites) {
      if (s == null || s.isEmpty()) {
        throw new IllegalArgumentException("The specified CipherSuites array contains invalid null or empty string elements");
      }
      if (CipherSuite.byName(s) == null) {
        throw new IllegalArgumentException("Unsupported CipherSuite: " + s);
      }
    }
  }
}
