/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class Configuration {
  private static final Configuration EMPTY = new Configuration();

  private final List<Configuration> parents;
  private final Map<String, ResolvedModule> byName;
  private final Set<ResolvedModule> modules;
  private final Map<ResolvedModule, Set<ResolvedModule>> graph;

  private Configuration() {
    parents = Collections.emptyList();
    byName = Collections.emptyMap();
    modules = Collections.emptySet();
    graph = Collections.emptyMap();
  }

  private Configuration(List<Configuration> parents, Map<String, ModuleReference> refs, boolean bind,
      ModuleFinder after) {
    this.parents = Collections.unmodifiableList(new ArrayList<Configuration>(parents));
    Map<String, ResolvedModule> names = new HashMap<String, ResolvedModule>();
    for (Map.Entry<String, ModuleReference> e : refs.entrySet()) {
      names.put(e.getKey(), new ResolvedModule(this, e.getValue()));
    }
    this.byName = names;
    this.modules = Collections.unmodifiableSet(new HashSet<ResolvedModule>(names.values()));
    Map<ResolvedModule, Set<ResolvedModule>> g = new HashMap<ResolvedModule, Set<ResolvedModule>>();
    for (ResolvedModule m : names.values()) {
      Set<ResolvedModule> reads = new HashSet<ResolvedModule>();
      ModuleDescriptor d = m.reference().descriptor();
      if (d.isAutomatic()) {
        reads.addAll(names.values());
        for (Configuration p : parents) {
          p.addAllTo(reads);
        }
        reads.remove(m);
      } else {
        for (ModuleDescriptor.Requires r : d.requires()) {
          ResolvedModule target = locate(r.name(), names);
          if (target != null) {
            addTransitive(target, reads, new HashSet<ResolvedModule>());
          }
        }
      }
      g.put(m, Collections.unmodifiableSet(reads));
    }
    this.graph = g;
  }

  private void addAllTo(Set<ResolvedModule> out) {
    out.addAll(modules);
    for (Configuration p : parents) {
      p.addAllTo(out);
    }
  }

  private ResolvedModule locate(String name, Map<String, ResolvedModule> local) {
    ResolvedModule m = local.get(name);
    if (m != null) {
      return m;
    }
    for (Configuration p : parents) {
      Optional<ResolvedModule> o = p.findModule(name);
      if (o.isPresent()) {
        return o.get();
      }
    }
    return null;
  }

  private void addTransitive(ResolvedModule target, Set<ResolvedModule> reads, Set<ResolvedModule> seen) {
    reads.add(target);
    if (!seen.add(target)) {
      return;
    }
    for (ModuleDescriptor.Requires r : target.reference().descriptor().requires()) {
      if (r.modifiers().contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE)) {
        ResolvedModule next = target.configuration().locate(r.name(), target.configuration().byName);
        if (next != null) {
          addTransitive(next, reads, seen);
        }
      }
    }
  }

  Set<ResolvedModule> readsOf(ResolvedModule m) {
    if (m.configuration() != this) {
      return m.configuration().readsOf(m);
    }
    return graph.getOrDefault(m, Collections.<ResolvedModule>emptySet());
  }

  public Configuration resolve(ModuleFinder before, ModuleFinder after, Collection<String> roots) {
    return resolve(before, List.of(this), after, roots);
  }

  public Configuration resolveAndBind(ModuleFinder before, ModuleFinder after, Collection<String> roots) {
    return resolveAndBind(before, List.of(this), after, roots);
  }

  public static Configuration resolve(ModuleFinder before, List<Configuration> parents, ModuleFinder after,
      Collection<String> roots) {
    return build(before, parents, after, roots, false);
  }

  public static Configuration resolveAndBind(ModuleFinder before, List<Configuration> parents, ModuleFinder after,
      Collection<String> roots) {
    return build(before, parents, after, roots, true);
  }

  public static Configuration empty() {
    return EMPTY;
  }

  private static Configuration build(ModuleFinder before, List<Configuration> parents, ModuleFinder after,
      Collection<String> roots, boolean bind) {
    List<Configuration> ps = new ArrayList<Configuration>(Objects.requireNonNull(parents));
    if (ps.isEmpty()) {
      throw new IllegalArgumentException("'parents' is empty");
    }
    Objects.requireNonNull(before);
    Objects.requireNonNull(after);
    Objects.requireNonNull(roots);
    for (Configuration p : ps) {
      Objects.requireNonNull(p);
    }
    Map<String, ModuleReference> resolved = new java.util.LinkedHashMap<String, ModuleReference>();
    Deque<String> queue = new ArrayDeque<String>();
    Map<String, String> requiredBy = new HashMap<String, String>();
    for (String root : roots) {
      Objects.requireNonNull(root);
      queue.add(root);
    }
    boolean first = true;
    Set<String> rootNames = new HashSet<String>(roots);
    while (!queue.isEmpty()) {
      String name = queue.poll();
      if (resolved.containsKey(name) || findInParents(ps, name) != null) {
        continue;
      }
      ModuleReference ref = findRef(before, after, name);
      if (ref == null) {
        String by = requiredBy.get(name);
        throw new FindException(by == null ? "Module " + name + " not found"
            : "Module " + name + " not found, required by " + by);
      }
      resolved.put(name, ref);
      for (ModuleDescriptor.Requires r : ref.descriptor().requires()) {
        String rn = r.name();
        if (r.modifiers().contains(ModuleDescriptor.Requires.Modifier.STATIC)
            && !resolved.containsKey(rn) && findInParents(ps, rn) == null) {
          continue;
        }
        if (!requiredBy.containsKey(rn)) {
          requiredBy.put(rn, name);
        }
        queue.add(rn);
      }
    }
    if (bind) {
      boolean grew = true;
      while (grew) {
        grew = false;
        Set<String> services = new LinkedHashSet<String>();
        for (ModuleReference r : resolved.values()) {
          services.addAll(r.descriptor().uses());
        }
        for (Configuration p : ps) {
          for (ResolvedModule m : p.modules()) {
            services.addAll(m.reference().descriptor().uses());
          }
        }
        List<ModuleReference> candidates = new ArrayList<ModuleReference>();
        candidates.addAll(before.findAll());
        candidates.addAll(after.findAll());
        for (ModuleReference c : candidates) {
          String cn = c.descriptor().name();
          if (resolved.containsKey(cn) || findInParents(ps, cn) != null) {
            continue;
          }
          for (ModuleDescriptor.Provides pr : c.descriptor().provides()) {
            if (services.contains(pr.service())) {
              queue.add(cn);
              requiredBy.put(cn, null);
              break;
            }
          }
        }
        while (!queue.isEmpty()) {
          String name = queue.poll();
          if (resolved.containsKey(name) || findInParents(ps, name) != null) {
            continue;
          }
          ModuleReference ref = findRef(before, after, name);
          if (ref == null) {
            String by = requiredBy.get(name);
            throw new FindException(by == null ? "Module " + name + " not found"
                : "Module " + name + " not found, required by " + by);
          }
          resolved.put(name, ref);
          grew = true;
          for (ModuleDescriptor.Requires r : ref.descriptor().requires()) {
            String rn = r.name();
            if (r.modifiers().contains(ModuleDescriptor.Requires.Modifier.STATIC)
                && !resolved.containsKey(rn) && findInParents(ps, rn) == null) {
              continue;
            }
            requiredBy.putIfAbsent(rn, name);
            queue.add(rn);
          }
        }
      }
    }
    detectCycles(resolved);
    Configuration cf = new Configuration(ps, resolved, bind, after);
    cf.checkUses();
    return cf;
  }

  private static void detectCycles(Map<String, ModuleReference> resolved) {
    Set<String> visited = new HashSet<String>();
    Set<String> done = new HashSet<String>();
    for (String n : resolved.keySet()) {
      visit(n, resolved, visited, done, new ArrayList<String>());
    }
  }

  private static void visit(String n, Map<String, ModuleReference> resolved, Set<String> visited, Set<String> done,
      List<String> path) {
    ModuleReference ref = resolved.get(n);
    if (ref == null || done.contains(n)) {
      return;
    }
    path.add(n);
    if (!visited.add(n)) {
      int from = path.indexOf(n);
      StringBuilder sb = new StringBuilder("Cycle detected: ");
      for (int i = from; i < path.size(); i++) {
        sb.append(i == from ? "" : " -> ").append(path.get(i));
      }
      throw new ResolutionException(sb.toString());
    }
    for (ModuleDescriptor.Requires r : ref.descriptor().requires()) {
      visit(r.name(), resolved, visited, done, path);
    }
    visited.remove(n);
    path.remove(path.size() - 1);
    done.add(n);
  }

  private void checkUses() {
    for (ResolvedModule m : modules) {
      ModuleDescriptor d = m.reference().descriptor();
      Set<String> svcs = new java.util.TreeSet<String>(d.uses());
      for (ModuleDescriptor.Provides pr : d.provides()) {
        svcs.add(pr.service());
      }
      for (String svc : svcs) {
        int dot = svc.lastIndexOf('.');
        String pn = dot < 0 ? "" : svc.substring(0, dot);
        boolean ok = d.packages().contains(pn);
        for (ResolvedModule r : readsOf(m)) {
          if (ok) {
            break;
          }
          for (ModuleDescriptor.Exports ex : r.reference().descriptor().exports()) {
            if (ex.source().equals(pn) && (!ex.isQualified() || ex.targets().contains(d.name()))) {
              ok = true;
            }
          }
          if (r.reference().descriptor().isAutomatic()) {
            ok = true;
          }
        }
        if (!ok) {
          throw new ResolutionException("Module " + d.name() + (d.uses().contains(svc) ? " uses " : " provides ") + svc
              + " but does not read a module that exports " + pn + " to " + d.name());
        }
      }
    }
  }

  private static ModuleReference findRef(ModuleFinder before, ModuleFinder after, String name) {
    Optional<ModuleReference> r = before.find(name);
    if (r.isPresent()) {
      return r.get();
    }
    r = after.find(name);
    return r.orElse(null);
  }

  private static ResolvedModule findInParents(List<Configuration> ps, String name) {
    for (Configuration p : ps) {
      Optional<ResolvedModule> o = p.findModule(name);
      if (o.isPresent()) {
        return o.get();
      }
    }
    return null;
  }

  public List<Configuration> parents() {
    return parents;
  }

  public Set<ResolvedModule> modules() {
    return modules;
  }

  public Optional<ResolvedModule> findModule(String name) {
    Objects.requireNonNull(name);
    ResolvedModule m = byName.get(name);
    if (m != null) {
      return Optional.of(m);
    }
    for (Configuration p : parents) {
      Optional<ResolvedModule> o = p.findModule(name);
      if (o.isPresent()) {
        return o;
      }
    }
    return Optional.empty();
  }

  @Override
  public String toString() {
    return modules.stream().map(ResolvedModule::name).collect(Collectors.joining(", "));
  }
}
