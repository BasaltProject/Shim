/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public class ElementFilter {

  private ElementFilter() {
  }

  private static final java.util.Set<javax.lang.model.element.ElementKind> CONSTRUCTOR_KIND =
      java.util.Collections.unmodifiableSet(java.util.EnumSet.of(javax.lang.model.element.ElementKind.CONSTRUCTOR));
  private static final java.util.Set<javax.lang.model.element.ElementKind> FIELD_KINDS =
      java.util.Collections.unmodifiableSet(java.util.EnumSet.of(javax.lang.model.element.ElementKind.FIELD, javax.lang.model.element.ElementKind.ENUM_CONSTANT));
  private static final java.util.Set<javax.lang.model.element.ElementKind> METHOD_KIND =
      java.util.Collections.unmodifiableSet(java.util.EnumSet.of(javax.lang.model.element.ElementKind.METHOD));
  private static final java.util.Set<javax.lang.model.element.ElementKind> PACKAGE_KIND =
      java.util.Collections.unmodifiableSet(java.util.EnumSet.of(javax.lang.model.element.ElementKind.PACKAGE));
  private static final java.util.Set<javax.lang.model.element.ElementKind> MODULE_KIND =
      java.util.Collections.unmodifiableSet(java.util.EnumSet.of(javax.lang.model.element.ElementKind.MODULE));
  private static final java.util.Set<javax.lang.model.element.ElementKind> TYPE_KINDS =
      java.util.Collections.unmodifiableSet(java.util.EnumSet.of(javax.lang.model.element.ElementKind.CLASS, javax.lang.model.element.ElementKind.ENUM,
          javax.lang.model.element.ElementKind.INTERFACE, javax.lang.model.element.ElementKind.RECORD, javax.lang.model.element.ElementKind.ANNOTATION_TYPE));
  private static final java.util.Set<javax.lang.model.element.ElementKind> RECORD_COMPONENT_KIND =
      java.util.Collections.unmodifiableSet(java.util.EnumSet.of(javax.lang.model.element.ElementKind.RECORD_COMPONENT));

  private static <E extends javax.lang.model.element.Element> java.util.List<E> listFilter(
      java.lang.Iterable<? extends javax.lang.model.element.Element> elements,
      java.util.Set<javax.lang.model.element.ElementKind> targetKinds, java.lang.Class<E> clazz) {
    java.util.List<E> list = new java.util.ArrayList<E>();
    for (javax.lang.model.element.Element e : elements) {
      if (targetKinds.contains(e.getKind())) {
        list.add(clazz.cast(e));
      }
    }
    return list;
  }

  private static <E extends javax.lang.model.element.Element> java.util.Set<E> setFilter(
      java.util.Set<? extends javax.lang.model.element.Element> elements,
      java.util.Set<javax.lang.model.element.ElementKind> targetKinds, java.lang.Class<E> clazz) {
    java.util.Set<E> set = new java.util.LinkedHashSet<E>();
    for (javax.lang.model.element.Element e : elements) {
      if (targetKinds.contains(e.getKind())) {
        set.add(clazz.cast(e));
      }
    }
    return set;
  }

  private static <D extends javax.lang.model.element.ModuleElement.Directive> java.util.List<D> listFilter(
      java.lang.Iterable<? extends javax.lang.model.element.ModuleElement.Directive> directives,
      javax.lang.model.element.ModuleElement.DirectiveKind directiveKind, java.lang.Class<D> clazz) {
    java.util.List<D> list = new java.util.ArrayList<D>();
    for (javax.lang.model.element.ModuleElement.Directive d : directives) {
      if (d.getKind() == directiveKind) {
        list.add(clazz.cast(d));
      }
    }
    return list;
  }

  public static java.util.List<javax.lang.model.element.ExecutableElement> constructorsIn(java.lang.Iterable<? extends javax.lang.model.element.Element> iterable) {
    return listFilter(iterable, CONSTRUCTOR_KIND, javax.lang.model.element.ExecutableElement.class);
  }

  public static java.util.Set<javax.lang.model.element.ExecutableElement> constructorsIn(java.util.Set<? extends javax.lang.model.element.Element> set) {
    return setFilter(set, CONSTRUCTOR_KIND, javax.lang.model.element.ExecutableElement.class);
  }

  public static java.util.List<javax.lang.model.element.VariableElement> fieldsIn(java.lang.Iterable<? extends javax.lang.model.element.Element> iterable) {
    return listFilter(iterable, FIELD_KINDS, javax.lang.model.element.VariableElement.class);
  }

  public static java.util.Set<javax.lang.model.element.VariableElement> fieldsIn(java.util.Set<? extends javax.lang.model.element.Element> set) {
    return setFilter(set, FIELD_KINDS, javax.lang.model.element.VariableElement.class);
  }

  public static java.util.List<javax.lang.model.element.ExecutableElement> methodsIn(java.lang.Iterable<? extends javax.lang.model.element.Element> iterable) {
    return listFilter(iterable, METHOD_KIND, javax.lang.model.element.ExecutableElement.class);
  }

  public static java.util.Set<javax.lang.model.element.ExecutableElement> methodsIn(java.util.Set<? extends javax.lang.model.element.Element> set) {
    return setFilter(set, METHOD_KIND, javax.lang.model.element.ExecutableElement.class);
  }

  public static java.util.List<javax.lang.model.element.ModuleElement> modulesIn(java.lang.Iterable<? extends javax.lang.model.element.Element> iterable) {
    return listFilter(iterable, MODULE_KIND, javax.lang.model.element.ModuleElement.class);
  }

  public static java.util.Set<javax.lang.model.element.ModuleElement> modulesIn(java.util.Set<? extends javax.lang.model.element.Element> set) {
    return setFilter(set, MODULE_KIND, javax.lang.model.element.ModuleElement.class);
  }

  public static java.util.List<javax.lang.model.element.PackageElement> packagesIn(java.lang.Iterable<? extends javax.lang.model.element.Element> iterable) {
    return listFilter(iterable, PACKAGE_KIND, javax.lang.model.element.PackageElement.class);
  }

  public static java.util.Set<javax.lang.model.element.PackageElement> packagesIn(java.util.Set<? extends javax.lang.model.element.Element> set) {
    return setFilter(set, PACKAGE_KIND, javax.lang.model.element.PackageElement.class);
  }

  public static java.util.List<javax.lang.model.element.RecordComponentElement> recordComponentsIn(java.lang.Iterable<? extends javax.lang.model.element.Element> iterable) {
    return listFilter(iterable, RECORD_COMPONENT_KIND, javax.lang.model.element.RecordComponentElement.class);
  }

  public static java.util.Set<javax.lang.model.element.RecordComponentElement> recordComponentsIn(java.util.Set<? extends javax.lang.model.element.Element> set) {
    return setFilter(set, RECORD_COMPONENT_KIND, javax.lang.model.element.RecordComponentElement.class);
  }

  public static java.util.List<javax.lang.model.element.TypeElement> typesIn(java.lang.Iterable<? extends javax.lang.model.element.Element> iterable) {
    return listFilter(iterable, TYPE_KINDS, javax.lang.model.element.TypeElement.class);
  }

  public static java.util.Set<javax.lang.model.element.TypeElement> typesIn(java.util.Set<? extends javax.lang.model.element.Element> set) {
    return setFilter(set, TYPE_KINDS, javax.lang.model.element.TypeElement.class);
  }

  public static java.util.List<javax.lang.model.element.ModuleElement.ExportsDirective> exportsIn(java.lang.Iterable<? extends javax.lang.model.element.ModuleElement.Directive> iterable) {
    return listFilter(iterable, javax.lang.model.element.ModuleElement.DirectiveKind.EXPORTS, javax.lang.model.element.ModuleElement.ExportsDirective.class);
  }

  public static java.util.List<javax.lang.model.element.ModuleElement.OpensDirective> opensIn(java.lang.Iterable<? extends javax.lang.model.element.ModuleElement.Directive> iterable) {
    return listFilter(iterable, javax.lang.model.element.ModuleElement.DirectiveKind.OPENS, javax.lang.model.element.ModuleElement.OpensDirective.class);
  }

  public static java.util.List<javax.lang.model.element.ModuleElement.ProvidesDirective> providesIn(java.lang.Iterable<? extends javax.lang.model.element.ModuleElement.Directive> iterable) {
    return listFilter(iterable, javax.lang.model.element.ModuleElement.DirectiveKind.PROVIDES, javax.lang.model.element.ModuleElement.ProvidesDirective.class);
  }

  public static java.util.List<javax.lang.model.element.ModuleElement.RequiresDirective> requiresIn(java.lang.Iterable<? extends javax.lang.model.element.ModuleElement.Directive> iterable) {
    return listFilter(iterable, javax.lang.model.element.ModuleElement.DirectiveKind.REQUIRES, javax.lang.model.element.ModuleElement.RequiresDirective.class);
  }

  public static java.util.List<javax.lang.model.element.ModuleElement.UsesDirective> usesIn(java.lang.Iterable<? extends javax.lang.model.element.ModuleElement.Directive> iterable) {
    return listFilter(iterable, javax.lang.model.element.ModuleElement.DirectiveKind.USES, javax.lang.model.element.ModuleElement.UsesDirective.class);
  }
}
