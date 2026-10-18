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

  private java.lang.module.Configuration cf;
  private java.util.Map<String, ClassLoader> loaders;

  public java.lang.module.Configuration configuration() {
    if (cf == null && this == bootLayer) {
      java.lang.module.ModuleFinder sys = java.lang.module.ModuleFinder.ofSystem();
      cf = java.lang.module.Configuration.resolve(sys, java.util.List.of(java.lang.module.Configuration.empty()), sys,
          java.util.List.of("java.base"));
    }
    return cf != null ? cf : java.lang.module.Configuration.empty();
  }

  public static final class Controller {
    private final ModuleLayer layer;

    Controller(ModuleLayer layer) {
      this.layer = layer;
    }

    public ModuleLayer layer() {
      return layer;
    }

    private Module check(Module m) {
      Objects.requireNonNull(m);
      if (m.getLayer() != layer) {
        throw new IllegalArgumentException(m.toString().replace("module ", "module ") + " not in layer");
      }
      return m;
    }

    public Controller addReads(Module source, Module target) {
      check(source).addReads(target);
      return this;
    }

    public Controller addExports(Module source, String pn, Module target) {
      check(source).addExports(pn, target);
      return this;
    }

    public Controller addOpens(Module source, String pn, Module target) {
      check(source).addOpens(pn, target);
      return this;
    }

    public Controller enableNativeAccess(Module target) {
      check(target);
      return this;
    }
  }

  public static Controller defineModules(java.lang.module.Configuration cf, List<ModuleLayer> parentLayers,
      java.util.function.Function<String, ClassLoader> clf) {
    Objects.requireNonNull(clf);
    return define(cf, parentLayers, clf);
  }

  public ModuleLayer defineModules(java.lang.module.Configuration cf,
      java.util.function.Function<String, ClassLoader> clf) {
    return defineModules(cf, List.of(this), clf).layer();
  }

  public ModuleLayer defineModulesWithOneLoader(java.lang.module.Configuration cf, ClassLoader parentLoader) {
    return defineModulesWithOneLoader(cf, List.of(this), parentLoader).layer();
  }

  public ModuleLayer defineModulesWithManyLoaders(java.lang.module.Configuration cf, ClassLoader parentLoader) {
    return defineModulesWithManyLoaders(cf, List.of(this), parentLoader).layer();
  }

  public static Controller defineModulesWithOneLoader(java.lang.module.Configuration cf,
      List<ModuleLayer> parentLayers, ClassLoader parentLoader) {
    ClassLoader loader = parentLoader != null ? parentLoader : ClassLoader.getSystemClassLoader();
    return define(cf, parentLayers, name -> loader);
  }

  public static Controller defineModulesWithManyLoaders(java.lang.module.Configuration cf,
      List<ModuleLayer> parentLayers, ClassLoader parentLoader) {
    ClassLoader loader = parentLoader != null ? parentLoader : ClassLoader.getSystemClassLoader();
    return define(cf, parentLayers, name -> loader);
  }

  private static Controller define(java.lang.module.Configuration cf, List<ModuleLayer> parentLayers,
      java.util.function.Function<String, ClassLoader> clf) {
    Objects.requireNonNull(cf);
    List<ModuleLayer> parents = new java.util.ArrayList<ModuleLayer>(Objects.requireNonNull(parentLayers));
    for (ModuleLayer p : parents) {
      Objects.requireNonNull(p);
    }
    List<java.lang.module.Configuration> cfParents = cf.parents();
    if (parents.size() != cfParents.size()) {
      throw new IllegalArgumentException("wrong number of parents");
    }
    for (int i = 0; i < parents.size(); i++) {
      if (parents.get(i).configuration() != cfParents.get(i)) {
        throw new IllegalArgumentException("Parent of configuration != configuration of this Layer");
      }
    }
    ModuleLayer layer = new ModuleLayer(Collections.<Module>emptySet(), parents);
    layer.cf = cf;
    Set<Module> defined = new HashSet<Module>();
    layer.loaders = new java.util.HashMap<String, ClassLoader>();
    for (java.lang.module.ResolvedModule r : cf.modules()) {
      String name = r.name();
      ClassLoader loader = clf.apply(name);
      if (loader == null) {
        throw new LayerInstantiationException("loader can't be 'null' or the platform class loader");
      }
      java.lang.module.ModuleDescriptor d = r.reference().descriptor();
      Module m = new Module(name, loader, d, layer, d.packages());
      m.define(r);
      defined.add(m);
      layer.loaders.put(name, loader);
    }
    layer.setModules(defined);
    return new Controller(layer);
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
    if (loaders != null && loaders.containsKey(name)) {
      return loaders.get(name);
    }
    for (ModuleLayer parent : parents) {
      if (parent.findModule(name).isPresent()) {
        return parent.findLoader(name);
      }
    }
    if (loaders == null) {
      Optional<Module> m = findModule(name);
      if (m.isPresent()) {
        return m.get().getClassLoader();
      }
    }
    throw new IllegalArgumentException("Module " + name + " not known to this layer");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    java.util.List<String> names = new java.util.ArrayList<String>();
    for (Module m : modules) {
      names.add(m.getName());
    }
    Collections.sort(names);
    return String.join(", ", names);
  }
}
