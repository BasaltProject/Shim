/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.security;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DomainLoadStoreParameter implements KeyStore.LoadStoreParameter {
  private final URI configuration;
  private final Map<String, KeyStore.ProtectionParameter> protectionParams;

  public DomainLoadStoreParameter(URI configuration, Map<String, KeyStore.ProtectionParameter> protectionParams) {
    if (configuration == null || protectionParams == null) {
      throw new NullPointerException("invalid null input");
    }
    this.configuration = configuration;
    this.protectionParams = Collections.unmodifiableMap(new HashMap<String, KeyStore.ProtectionParameter>(
        protectionParams));
  }

  public URI getConfiguration() {
    return configuration;
  }

  public Map<String, KeyStore.ProtectionParameter> getProtectionParams() {
    return protectionParams;
  }

  @Override
  public KeyStore.ProtectionParameter getProtectionParameter() {
    return null;
  }
}
