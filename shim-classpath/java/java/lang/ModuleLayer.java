/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.lang;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ModuleLayer {
  private static ModuleLayer bootLayer;

  private Set<Module> modules;
  private final List<ModuleLayer> parents;

  ModuleLayer(Set<Module> modules) {
    this(modules, Collections.<ModuleLayer>emptyList());
  }

  ModuleLayer(Set<Module> modules, List<ModuleLayer> parents) {
    this.modules = modules != null ? Collections.unmodifiableSet(new HashSet<Module>(modules)) : Collections.<Module>emptySet();
    this.parents = parents != null ? Collections.unmodifiableList(parents) : Collections.<ModuleLayer>emptyList();
  }

  static void setBoot(ModuleLayer layer) {
    bootLayer = layer;
  }

  void setModules(Set<Module> modules) {
    this.modules = Collections.unmodifiableSet(new HashSet<Module>(modules));
  }

  public static ModuleLayer boot() {
    if (bootLayer == null) {
      Module m = Module.JAVA_BASE;
    }
    return bootLayer;
  }

  public static ModuleLayer empty() {
    return new ModuleLayer(Collections.<Module>emptySet());
  }

  public Set<Module> modules() {
    return modules;
  }

  public List<ModuleLayer> parents() {
    return parents;
  }

  public Optional<Module> findModule(String name) {
    Objects.requireNonNull(name);
    for (Module m : modules) {
      if (name.equals(m.getName())) {
        return Optional.of(m);
      }
    }
    for (ModuleLayer parent : parents) {
      Optional<Module> m = parent.findModule(name);
      if (m.isPresent()) {
        return m;
      }
    }
    return Optional.empty();
  }

  public ClassLoader findLoader(String name) {
    Objects.requireNonNull(name);
    Optional<Module> m = findModule(name);
    if (m.isPresent()) {
      return m.get().getClassLoader();
    }
    throw new IllegalArgumentException("Module " + name + " not found in layer");
  }

  @Override
  public String toString() {
    return "layer[" + modules.size() + " modules]";
  }
}
