/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

import shim.Classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public class SignatureParser {
  private final ClassLoader loader;
  private final char[] array;
  private final String signature;
  private int offset;
  private final Type type;
  private final Map<String, TypeVariable> typeVariables;

  public static class MethodSignature {
    public final TypeVariable<?>[] typeParameters;
    public final Type[] parameterTypes;
    public final Type returnType;
    public final Type[] exceptionTypes;

    public MethodSignature(
        TypeVariable<?>[] typeParameters,
        Type[] parameterTypes,
        Type returnType,
        Type[] exceptionTypes) {
      this.typeParameters = typeParameters;
      this.parameterTypes = parameterTypes;
      this.returnType = returnType;
      this.exceptionTypes = exceptionTypes;
    }
  }

  public static Type parse(ClassLoader loader, String signature, Class declaringClass) {
    return new SignatureParser(loader, signature, collectTypeVariables(declaringClass)).type;
  }

  public static MethodSignature parseMethod(ClassLoader loader, String signature, Class declaringClass) {
    Map<String, TypeVariable> varsMap = new HashMap<>(collectTypeVariables(declaringClass));
    List<TypeVariableImpl> methodVarsList = new ArrayList<>();
    char[] signChars = signature.toCharArray();
    int i = 0;

    if (signChars.length > 0 && signChars[i] == '<') {
      i++;
      while (i < signChars.length && signChars[i] != '>') {
        int colon = signature.indexOf(':', i);
        if (colon < 0) {
          break;
        }
        String typeVarName = new String(signChars, i, colon - i);
        i = colon + 1;
        if (i < signChars.length && signChars[i] == ':') {
          i++;
        }
        int start = i;
        int angles = 0;
        while (i < signChars.length && (angles > 0 || signChars[i] != ';')) {
          if (signChars[i] == '<') angles++;
          else if (signChars[i] == '>') angles--;
          i++;
        }
        String typeName = new String(signChars, start, i - start + 1);
        Type baseType = Object.class;
        try {
          baseType = parse(loader, typeName, varsMap);
        } catch (Exception ignored) {
        }
        TypeVariableImpl tv = new TypeVariableImpl(typeVarName, baseType);
        methodVarsList.add(tv);
        varsMap.put(typeVarName, tv);
        i++;
      }
      if (i < signChars.length && signChars[i] == '>') {
        i++;
      }
      for (TypeVariableImpl tv : methodVarsList) {
        tv.setVars(methodVarsList);
      }
    }

    if (i >= signChars.length || signChars[i] != '(') {
      throw new IllegalArgumentException("Expected '(' in method signature: " + signature);
    }
    i++;

    List<Type> paramTypes = new ArrayList<>();
    SignatureParser parser = new SignatureParser(loader, signature, varsMap, i);
    while (parser.offset < signChars.length && signChars[parser.offset] != ')') {
      paramTypes.add(parser.parseType());
    }
    if (parser.offset < signChars.length && signChars[parser.offset] == ')') {
      parser.offset++;
    }

    Type returnType = parser.parseType();

    List<Type> exceptionTypes = new ArrayList<>();
    while (parser.offset < signChars.length && signChars[parser.offset] == '^') {
      parser.offset++;
      exceptionTypes.add(parser.parseType());
    }

    return new MethodSignature(
        methodVarsList.toArray(new TypeVariable[0]),
        paramTypes.toArray(new Type[0]),
        returnType,
        exceptionTypes.toArray(new Type[0]));
  }

  private static Type parse(ClassLoader loader, String signature, Map<String, TypeVariable> typeVariables) {
    return new SignatureParser(loader, signature, typeVariables).type;
  }

  private SignatureParser(ClassLoader loader, String signature, Map<String, TypeVariable> typeVariables) {
    this.loader = loader;
    this.signature = signature;
    array = signature.toCharArray();
    this.typeVariables = typeVariables;
    type = parseType();
    if (offset != array.length) {
      throw new IllegalArgumentException("Extra characters after " + offset
          + ": " + signature);
    }
  }

  private SignatureParser(ClassLoader loader, String signature, Map<String, TypeVariable> typeVariables, int initialOffset) {
    this.loader = loader;
    this.signature = signature;
    this.array = signature.toCharArray();
    this.typeVariables = typeVariables;
    this.offset = initialOffset;
    this.type = null;
  }

  private Type parseType() {
    char c = array[offset++];
    if (c == 'B') {
      return Byte.TYPE;
    } else if (c == 'C') {
      return Character.TYPE;
    } else if (c == 'D') {
      return Double.TYPE;
    } else if (c == 'F') {
      return Float.TYPE;
    } else if (c == 'I') {
      return Integer.TYPE;
    } else if (c == 'J') {
      return Long.TYPE;
    } else if (c == 'S') {
      return Short.TYPE;
    } else if (c == 'Z') {
      return Boolean.TYPE;
    } else if (c == 'V') {
      return Void.TYPE;
    } else if (c == '[') {
      Type component = parseType();
      if (component instanceof Class) {
        return Array.newInstance((Class<?>) component, 0).getClass();
      } else {
        return makeGenericArrayType(component);
      }
    } else if (c == 'T') {
      int end = signature.indexOf(';', offset);
      if (end < 0) {
        throw new RuntimeException("No semicolon found while parsing signature");
      }
      String name = new String(array, offset, end - offset);
      Type res = typeVariables.get(name);
      offset = end + 1;
      if (res == null) {
        res = new TypeVariableImpl(name, Object.class);
      }
      return res;
    } else if (c != 'L') {
      throw new IllegalArgumentException("Unexpected character: " + c + ", signature: " + new String(array, 0, array.length) + ", i = " + offset);
    }
    StringBuilder builder = new StringBuilder();
    Type ownerType = null;
    for (;;) {
      for (;;) {
        c = array[offset++];
        if (c == ';' || c == '<') {
          break;
        }
        builder.append(c == '/' ? '.' : c);
      }
      String rawTypeName = builder.toString();
      Class<?> rawType;
      try {
        rawType = loader.loadClass(rawTypeName);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Could not find class " + rawTypeName);
      }

      int lastDollar = rawTypeName.lastIndexOf('$');
      if (lastDollar != -1 && ownerType == null) {
        String ownerName = rawTypeName.substring(0, lastDollar);
        try {
          ownerType = loader.loadClass(ownerName);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Could not find class " + ownerName);
        }
      }

      if (c == ';') {
        return rawType;
      }
      List<Type> args = new ArrayList<Type>();
      while (array[offset] != '>') {
        args.add(parseTypeArgument());
      }
      ++offset;
      c = array[offset++];
      ParameterizedType type = makeType(args.toArray(new Type[args.size()]), ownerType, rawType);
      if (c == ';') {
        return type;
      }
      if (c != '.') {
        throw new RuntimeException("TODO");
      }
      ownerType = type;
      builder.append("$");
    }
  }

  private Type parseTypeArgument() {
    char c = array[offset];
    if (c == '*') {
      offset++;
      return makeWildcardType(new Type[] { Object.class }, new Type[0]);
    } else if (c == '+') {
      offset++;
      Type upper = parseType();
      return makeWildcardType(new Type[] { upper }, new Type[0]);
    } else if (c == '-') {
      offset++;
      Type lower = parseType();
      return makeWildcardType(new Type[] { Object.class }, new Type[] { lower });
    } else {
      return parseType();
    }
  }

  private static WildcardType makeWildcardType(final Type[] upperBounds, final Type[] lowerBounds) {
    return new WildcardType() {
      @Override
      public Type[] getUpperBounds() {
        return upperBounds.clone();
      }

      @Override
      public Type[] getLowerBounds() {
        return lowerBounds.clone();
      }

      @Override
      public boolean equals(Object o) {
        if (o instanceof WildcardType) {
          WildcardType that = (WildcardType) o;
          return Arrays.equals(this.getLowerBounds(), that.getLowerBounds())
              && Arrays.equals(this.getUpperBounds(), that.getUpperBounds());
        }
        return false;
      }

      @Override
      public int hashCode() {
        return Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
      }

      @Override
      public String toString() {
        Type[] lower = getLowerBounds();
        Type[] bounds = lower;
        StringBuilder sb = new StringBuilder();
        if (lower.length > 0) {
          sb.append("? super ");
        } else {
          Type[] upper = getUpperBounds();
          if (upper.length > 0 && !Object.class.equals(upper[0])) {
            bounds = upper;
            sb.append("? extends ");
          } else {
            return "?";
          }
        }
        for (int i = 0; i < bounds.length; i++) {
          if (i > 0) sb.append(" & ");
          sb.append(typeName(bounds[i]));
        }
        return sb.toString();
      }
    };
  }

  private static GenericArrayType makeGenericArrayType(final Type component) {
    return new GenericArrayType() {
      @Override
      public Type getGenericComponentType() {
        return component;
      }

      @Override
      public boolean equals(Object o) {
        if (o instanceof GenericArrayType) {
          GenericArrayType that = (GenericArrayType) o;
          return Objects.equals(component, that.getGenericComponentType());
        }
        return false;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(component);
      }

      @Override
      public String toString() {
        return typeName(component) + "[]";
      }
    };
  }

  private static String typeName(Type type) {
    if (type == null) {
      return "null";
    }
    if (type instanceof Class) {
      Class<?> clazz = (Class<?>) type;
      return clazz.getName();
    }
    return type.toString();
  }

  private static ParameterizedType makeType(final Type[] args, final Type owner, final Type raw) {
    return new ParameterizedType() {
      @Override
      public Type getRawType() {
        return raw;
      }

      @Override
      public Type getOwnerType() {
        return owner;
      }

      @Override
      public Type[] getActualTypeArguments() {
        return args;
      }

      @Override
      public boolean equals(Object o) {
        if (o instanceof ParameterizedType) {
          ParameterizedType that = (ParameterizedType) o;
          if (this == that) return true;
          return Objects.equals(owner, that.getOwnerType())
              && Objects.equals(raw, that.getRawType())
              && Arrays.equals(args, that.getActualTypeArguments());
        }
        return false;
      }

      @Override
      public int hashCode() {
        return Arrays.hashCode(args) ^ Objects.hashCode(owner) ^ Objects.hashCode(raw);
      }

      @Override
      public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(typeName(raw));
        builder.append('<');
        String sep = "";
        for (Type t : args) {
          builder.append(sep).append(typeName(t));
          sep = ", ";
        }
        builder.append('>');
        return builder.toString();
      }
    };
  }
  
  private static Map<String, TypeVariable> collectTypeVariables(Class clz) {
    Map<String, TypeVariable> varsMap = new HashMap<String, TypeVariable>();
    LinkedList<Class> classList = new LinkedList<Class>();
    for (Class c = clz; c != null; c = c.getDeclaringClass()) {
      classList.addFirst(c);
    }
    
    for (Class cur : classList) {
      final LinkedList<TypeVariableImpl> varsList = new LinkedList<TypeVariableImpl>();
      if (cur.vmClass.addendum != null && cur.vmClass.addendum.signature != null) {
        String signature = Classes.toString((byte[]) cur.vmClass.addendum.signature);
        final char[] signChars = signature.toCharArray();
        try {
          int i = 0;
          if (signChars[i] == '<') {
            i++;
            do {
              final int colon = signature.indexOf(':', i);
              if (colon < 0 || colon + 1 == signChars.length) {
                throw new RuntimeException("Can't find ':' in the signature " + signature + " starting from " + i);
              }
              String typeVarName = new String(signChars, i, colon - i);
              i = colon + 1;
              
              int start = i;
              int angles = 0;
              while (angles > 0 || signChars[i] != ';') {
                if (signChars[i] == '<') angles ++;
                else if (signChars[i] == '>') angles --;
                i++;
              }
              String typeName = new String(signChars, start, i - start + 1);
              final Type baseType = SignatureParser.parse(cur.vmClass.loader, typeName, varsMap);
  
              TypeVariableImpl tv = new TypeVariableImpl(typeVarName, baseType);
              varsList.add(tv);

              i++;
            } while (signChars[i] != '>');
          
          }
        } catch (IndexOutOfBoundsException e) {
          throw new RuntimeException("Signature of " + cur + " is broken (" + signature + ") and can't be parsed", e);
        }
      }
      for (TypeVariableImpl tv : varsList) {
        tv.setVars(varsList);
        varsMap.put(tv.getName(), tv);
      }
      cur = cur.getDeclaringClass();
    };
    return varsMap;
  } 

  private static class TypeVariableImpl implements TypeVariable {
    private String name;
    private Type baseType;
    private TypeVariableImpl[] vars;

    public Type[] getBounds() {
      return new Type[] { baseType };
    }
    
    public GenericDeclaration getGenericDeclaration() {
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
    
    TypeVariableImpl(String name, Type baseType) {
      this.name = name;
      this.baseType = baseType;
    }
    
    void setVars(List<TypeVariableImpl> vars) {
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
        return Objects.equals(name, that.getName());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name);
    }
  }
}
