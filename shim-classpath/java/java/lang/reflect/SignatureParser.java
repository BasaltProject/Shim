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
import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;

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

  public static TypeVariable<?>[] classTypeParameters(Class declaringClass) {
    for (TypeVariable tv : collectTypeVariables(declaringClass).values()) {
      if (tv instanceof TypeVariableImpl && ((TypeVariableImpl) tv).owner == declaringClass) {
        return ((TypeVariableImpl) tv).vars.clone();
      }
    }
    return new TypeVariable<?>[0];
  }

  public static Type parse(ClassLoader loader, String signature, Class declaringClass) {
    return new SignatureParser(loader, signature, collectTypeVariables(declaringClass)).type;
  }

  public static MethodSignature parseMethod(ClassLoader loader, String signature, Class declaringClass) {
    return parseMethod(loader, signature, declaringClass, null);
  }

  public static MethodSignature parseMethod(ClassLoader loader, String signature, Class declaringClass,
      GenericDeclaration owner) {
    Map<String, TypeVariable> varsMap = new HashMap<>(collectTypeVariables(declaringClass));
    List<TypeVariableImpl> methodVarsList = new ArrayList<>();
    char[] signChars = signature.toCharArray();
    int i = 0;

    if (signChars.length > 0 && signChars[i] == '<') {
      int[] pos = {i};
      methodVarsList.addAll(parseTypeParams(loader, signature, pos, varsMap, owner));
      i = pos[0];
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
    return new WildcardTypeImpl(upperBounds, lowerBounds);
  }

  private static GenericArrayType makeGenericArrayType(final Type component) {
    return new GenericArrayTypeImpl(component);
  }

  private static String typeName(Type type) {
    return ParameterizedTypeImpl.typeName(type);
  }

  private static ParameterizedType makeType(final Type[] args, final Type owner, final Type raw) {
    return new ParameterizedTypeImpl(args, owner, raw);
  }
  
  private static int skipTypeSignature(String sig, int i) {
    char c = sig.charAt(i);
    if (c == '[') {
      return skipTypeSignature(sig, i + 1);
    }
    if (c == 'T') {
      return sig.indexOf(';', i) + 1;
    }
    if (c == 'L') {
      int depth = 0;
      for (int k = i; k < sig.length(); k++) {
        char d = sig.charAt(k);
        if (d == '<') {
          depth++;
        } else if (d == '>') {
          depth--;
        } else if (d == ';' && depth == 0) {
          return k + 1;
        }
      }
    }
    return i + 1;
  }

  private static List<TypeVariableImpl> parseTypeParams(ClassLoader loader, String sig, int[] pos,
      Map<String, TypeVariable> varsMap, GenericDeclaration owner) {
    int i = pos[0] + 1;
    List<String> names = new ArrayList<String>();
    List<List<String>> bounds = new ArrayList<List<String>>();
    while (i < sig.length() && sig.charAt(i) != '>') {
      int colon = sig.indexOf(':', i);
      names.add(sig.substring(i, colon));
      i = colon;
      List<String> bs = new ArrayList<String>();
      while (i < sig.length() && sig.charAt(i) == ':') {
        i++;
        if (i < sig.length() && sig.charAt(i) != ':' && sig.charAt(i) != '>') {
          int end = skipTypeSignature(sig, i);
          bs.add(sig.substring(i, end));
          i = end;
        }
      }
      bounds.add(bs);
    }
    pos[0] = i + 1;
    List<TypeVariableImpl> out = new ArrayList<TypeVariableImpl>();
    for (String n : names) {
      TypeVariableImpl tv = new TypeVariableImpl(n, Object.class);
      tv.setOwner(owner);
      out.add(tv);
      varsMap.put(n, tv);
    }
    for (int k = 0; k < out.size(); k++) {
      List<Type> bt = new ArrayList<Type>();
      for (String b : bounds.get(k)) {
        try {
          bt.add(parse(loader, b, varsMap));
        } catch (Exception e) {
          bt.add(Object.class);
        }
      }
      if (bt.isEmpty()) {
        bt.add(Object.class);
      }
      out.get(k).setBounds(bt.toArray(new Type[0]));
    }
    for (TypeVariableImpl tv : out) {
      tv.setVars(out);
    }
    return out;
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
        if (signature.length() > 0 && signature.charAt(0) == '<') {
          try {
            int[] pos = {0};
            varsList.addAll(parseTypeParams(cur.vmClass.loader, signature, pos, varsMap, cur));
          } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("Signature of " + cur + " is broken (" + signature + ") and can't be parsed", e);
          }
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
}
