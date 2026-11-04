/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package javax.lang.model.util;

public interface Types {
  javax.lang.model.element.Element asElement(javax.lang.model.type.TypeMirror typeMirror);
  javax.lang.model.type.TypeMirror asMemberOf(javax.lang.model.type.DeclaredType declaredType, javax.lang.model.element.Element element);
  javax.lang.model.element.TypeElement boxedClass(javax.lang.model.type.PrimitiveType primitiveType);
  javax.lang.model.type.TypeMirror capture(javax.lang.model.type.TypeMirror typeMirror);
  boolean contains(javax.lang.model.type.TypeMirror typeMirror, javax.lang.model.type.TypeMirror typeMirror2);
  java.util.List<? extends javax.lang.model.type.TypeMirror> directSupertypes(javax.lang.model.type.TypeMirror typeMirror);
  javax.lang.model.type.TypeMirror erasure(javax.lang.model.type.TypeMirror typeMirror);
  javax.lang.model.type.ArrayType getArrayType(javax.lang.model.type.TypeMirror typeMirror);
  javax.lang.model.type.DeclaredType getDeclaredType(javax.lang.model.element.TypeElement typeElement, javax.lang.model.type.TypeMirror... typeMirrors);
  javax.lang.model.type.DeclaredType getDeclaredType(javax.lang.model.type.DeclaredType declaredType, javax.lang.model.element.TypeElement typeElement, javax.lang.model.type.TypeMirror... typeMirrors);
  javax.lang.model.type.NoType getNoType(javax.lang.model.type.TypeKind typeKind);
  javax.lang.model.type.NullType getNullType();
  javax.lang.model.type.PrimitiveType getPrimitiveType(javax.lang.model.type.TypeKind typeKind);
  javax.lang.model.type.WildcardType getWildcardType(javax.lang.model.type.TypeMirror typeMirror, javax.lang.model.type.TypeMirror typeMirror2);
  boolean isAssignable(javax.lang.model.type.TypeMirror typeMirror, javax.lang.model.type.TypeMirror typeMirror2);
  boolean isSameType(javax.lang.model.type.TypeMirror typeMirror, javax.lang.model.type.TypeMirror typeMirror2);
  boolean isSubsignature(javax.lang.model.type.ExecutableType executableType, javax.lang.model.type.ExecutableType executableType2);
  boolean isSubtype(javax.lang.model.type.TypeMirror typeMirror, javax.lang.model.type.TypeMirror typeMirror2);
  default <T extends javax.lang.model.type.TypeMirror> T stripAnnotations(T t) {
    return t;
  }
  javax.lang.model.type.PrimitiveType unboxedType(javax.lang.model.type.TypeMirror typeMirror);
}
