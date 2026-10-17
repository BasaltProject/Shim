/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.net.ssl;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class KeyStoreBuilderParameters implements ManagerFactoryParameters {
  private final List<KeyStore.Builder> parameters;

  public KeyStoreBuilderParameters(KeyStore.Builder builder) {
    parameters = Collections.singletonList(Objects.requireNonNull(builder));
  }

  public KeyStoreBuilderParameters(List<KeyStore.Builder> parameters) {
    if (parameters.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.parameters = Collections.unmodifiableList(new ArrayList<KeyStore.Builder>(parameters));
  }

  public List<KeyStore.Builder> getParameters() {
    return parameters;
  }
}
