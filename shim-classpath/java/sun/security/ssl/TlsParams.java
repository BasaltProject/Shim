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
    return kSupportedProtocols.clone();
  }

  static List<Integer> versions(SslConfig cfg) {
    List<Integer> out = new ArrayList<Integer>();
    for (String p : cfg.protocols) {
      int code = TlsConnection.enabledVersionCode(p);
      if (code != 0) {
        out.add(code);
      }
    }
    Collections.sort(out, Collections.reverseOrder());
    return out;
  }

  static List<CipherSuite> suites(SslConfig cfg, boolean allow13, boolean allow12) {
    List<CipherSuite> out = new ArrayList<CipherSuite>();
    for (String n : cfg.cipherSuites) {
      CipherSuite s = CipherSuite.byName(n);
      if (s == null) {
        continue;
      }
      if (s.tls13 ? allow13 : allow12) {
        out.add(s);
      }
    }
    return out;
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

  static void checkProtocols(String[] protocols) {
    if (protocols == null) {
      throw new IllegalArgumentException("Protocols cannot be null");
    }
    for (String p : protocols) {
      boolean ok = false;
      for (String s : kSupportedProtocols) {
        ok |= s.equals(p);
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
