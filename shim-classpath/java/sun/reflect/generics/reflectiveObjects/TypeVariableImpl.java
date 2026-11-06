/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package sun.reflect.generics.reflectiveObjects;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Objects;

public final class TypeVariableImpl implements TypeVariable {
  private String name;
  private Type baseType;
  public TypeVariableImpl[] vars;
  public GenericDeclaration owner;

  private Type[] bounds;

  public Type[] getBounds() {
    return bounds != null ? bounds.clone() : new Type[] { baseType };
  }

  public void setBounds(Type[] bounds) {
    this.bounds = bounds;
    this.baseType = bounds[0];
  }

  public void setOwner(GenericDeclaration owner) {
    this.owner = owner;
  }

  public GenericDeclaration getGenericDeclaration() {
    if (owner != null) {
      return owner;
    }
    return new GenericDeclaration() {
      public TypeVariable<?>[] getTypeParameters() {
        return vars;
      }

      public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
      }

      public java.lang.annotation.Annotation[] getAnnotations() {
        return new java.lang.annotation.Annotation[0];
      }

      public java.lang.annotation.Annotation[] getDeclaredAnnotations() {
        return new java.lang.annotation.Annotation[0];
      }
    };
  }
  
  public String getName() {
    return name;
  }
  
  public TypeVariableImpl(String name, Type baseType) {
    this.name = name;
    this.baseType = baseType;
  }
  
  public void setVars(List<TypeVariableImpl> vars) {
    this.vars = new TypeVariableImpl[vars.size()];
    vars.toArray(this.vars);
  }
  
  @Override
  public String toString() {
    return name;
  }

  @Override
  public java.lang.annotation.Annotation[] getAnnotations() {
    return new java.lang.annotation.Annotation[0];
  }

  @Override
  public java.lang.annotation.Annotation[] getDeclaredAnnotations() {
    return new java.lang.annotation.Annotation[0];
  }

  @Override
  public boolean isAnnotationPresent(Class<? extends java.lang.annotation.Annotation> annotationClass) {
    return false;
  }

  @Override
  public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationClass) {
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TypeVariable) {
      TypeVariable<?> that = (TypeVariable<?>) o;
      return Objects.equals(name, that.getName())
          && (owner == null || that.getGenericDeclaration() == null || owner.equals(that.getGenericDeclaration()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }
}
