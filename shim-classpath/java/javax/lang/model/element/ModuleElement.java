/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.element;

public interface ModuleElement extends javax.lang.model.element.Element, javax.lang.model.element.QualifiedNameable {
  javax.lang.model.type.TypeMirror asType();
  java.util.List<? extends javax.lang.model.element.ModuleElement.Directive> getDirectives();
  java.util.List<? extends javax.lang.model.element.Element> getEnclosedElements();
  javax.lang.model.element.Element getEnclosingElement();
  javax.lang.model.element.Name getQualifiedName();
  javax.lang.model.element.Name getSimpleName();
  boolean isOpen();
  boolean isUnnamed();

  public interface Directive {
    <R, P> R accept(javax.lang.model.element.ModuleElement.DirectiveVisitor<R, P> directiveVisitor, P p);
    javax.lang.model.element.ModuleElement.DirectiveKind getKind();
  }

  public enum DirectiveKind {
    REQUIRES, EXPORTS, OPENS, USES, PROVIDES;


  }

  public interface DirectiveVisitor<R, P> {
    default R visit(javax.lang.model.element.ModuleElement.Directive directive, P p) {
      return directive.accept(this, p);
    }
    default R visit(javax.lang.model.element.ModuleElement.Directive directive) {
      return directive.accept(this, null);
    }
    R visitExports(javax.lang.model.element.ModuleElement.ExportsDirective exportsDirective, P p);
    R visitOpens(javax.lang.model.element.ModuleElement.OpensDirective opensDirective, P p);
    R visitProvides(javax.lang.model.element.ModuleElement.ProvidesDirective providesDirective, P p);
    R visitRequires(javax.lang.model.element.ModuleElement.RequiresDirective requiresDirective, P p);
    default R visitUnknown(javax.lang.model.element.ModuleElement.Directive directive, P p) {
      throw new UnknownDirectiveException(directive, p);
    }
    R visitUses(javax.lang.model.element.ModuleElement.UsesDirective usesDirective, P p);
  }

  public interface ExportsDirective extends javax.lang.model.element.ModuleElement.Directive {
    javax.lang.model.element.PackageElement getPackage();
    java.util.List<? extends javax.lang.model.element.ModuleElement> getTargetModules();
  }

  public interface OpensDirective extends javax.lang.model.element.ModuleElement.Directive {
    javax.lang.model.element.PackageElement getPackage();
    java.util.List<? extends javax.lang.model.element.ModuleElement> getTargetModules();
  }

  public interface ProvidesDirective extends javax.lang.model.element.ModuleElement.Directive {
    java.util.List<? extends javax.lang.model.element.TypeElement> getImplementations();
    javax.lang.model.element.TypeElement getService();
  }

  public interface RequiresDirective extends javax.lang.model.element.ModuleElement.Directive {
    javax.lang.model.element.ModuleElement getDependency();
    boolean isStatic();
    boolean isTransitive();
  }

  public interface UsesDirective extends javax.lang.model.element.ModuleElement.Directive {
    javax.lang.model.element.TypeElement getService();
  }
}
