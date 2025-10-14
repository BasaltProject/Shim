/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.security.ssl;

import java.security.Provider;
import java.util.Arrays;
import java.util.HashMap;

public final class SunJSSE extends Provider {
  private static final long serialVersionUID = 3231825739635378733L;

  private static final String kInfo = "Sun JSSE provider(PKCS12, SunX509/PKIX key/trust factories, "
      + "SSLv3/TLSv1/TLSv1.1/TLSv1.2/TLSv1.3/DTLSv1.0/DTLSv1.2)";

  public SunJSSE() {
    super("SunJSSE", "25", kInfo);
    register();
  }

  private void add(String type, String algorithm, String className, String... aliases) {
    putService(new Provider.Service(this, type, algorithm, className, Arrays.asList(aliases),
        new HashMap<String, String>()));
  }

  private void register() {
    add("SSLContext", "TLSv1", "sun.security.ssl.SSLContextImpl$TLS10Context", "SSLv3");
    add("TrustManagerFactory", "PKIX", "sun.security.ssl.TrustManagerFactoryImpl$PKIXFactory", "SunPKIX", "X509",
        "X.509");
    add("KeyManagerFactory", "NewSunX509", "sun.security.ssl.KeyManagerFactoryImpl$X509", "PKIX");
    add("SSLContext", "TLS", "sun.security.ssl.SSLContextImpl$TLSContext", "SSL");
    add("SSLContext", "Default", "sun.security.ssl.SSLContextImpl$DefaultSSLContext");
    add("SSLContext", "TLSv1.2", "sun.security.ssl.SSLContextImpl$TLS12Context");
    add("SSLContext", "TLSv1.1", "sun.security.ssl.SSLContextImpl$TLS11Context");
    add("SSLContext", "TLSv1.3", "sun.security.ssl.SSLContextImpl$TLS13Context");
    add("TrustManagerFactory", "SunX509", "sun.security.ssl.TrustManagerFactoryImpl$SimpleFactory");
    add("KeyManagerFactory", "SunX509", "sun.security.ssl.KeyManagerFactoryImpl$SunX509");
  }
}
