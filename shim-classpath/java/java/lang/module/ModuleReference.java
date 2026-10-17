/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public abstract class ModuleReference {
  private final ModuleDescriptor descriptor;
  private final URI location;

  protected ModuleReference(ModuleDescriptor descriptor, URI location) {
    this.descriptor = Objects.requireNonNull(descriptor);
    this.location = location;
  }

  public final ModuleDescriptor descriptor() {
    return descriptor;
  }

  public final Optional<URI> location() {
    return Optional.ofNullable(location);
  }

  public abstract ModuleReader open() throws IOException;
}
