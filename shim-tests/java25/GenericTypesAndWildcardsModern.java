/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 17

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

public class GenericTypesAndWildcardsModern {

  static class SampleContainer<T> {
    List<?> unboundedList;
    List<? extends Number> upperBoundedList;
    List<? super Integer> lowerBoundedList;
    T[] genericArray;
    List<String>[] parameterizedArray;
    Map<String, List<? extends Number>> nestedMap;

    public <E> E[] process(List<E> elements) {
      return null;
    }
  }

  public static void main(String[] args) throws Exception {
    testWildcards();
    testGenericArrays();
    testParameterizedTypes();
    testGenericMethods();
    System.out.println("GenericTypesAndWildcardsModern passed successfully");
  }

  private static void testWildcards() throws Exception {
    Field fUnbounded = SampleContainer.class.getDeclaredField("unboundedList");
    ParameterizedType ptUnbounded = (ParameterizedType) fUnbounded.getGenericType();
    WildcardType wtUnbounded = (WildcardType) ptUnbounded.getActualTypeArguments()[0];
    System.out.println("unbounded is WildcardType: " + (wtUnbounded != null));
    System.out.println("unbounded upper: " + wtUnbounded.getUpperBounds()[0].getTypeName());
    System.out.println("unbounded lower count: " + wtUnbounded.getLowerBounds().length);
    System.out.println("unbounded toString: " + wtUnbounded.toString());

    Field fUpper = SampleContainer.class.getDeclaredField("upperBoundedList");
    ParameterizedType ptUpper = (ParameterizedType) fUpper.getGenericType();
    WildcardType wtUpper = (WildcardType) ptUpper.getActualTypeArguments()[0];
    System.out.println("upper upper: " + wtUpper.getUpperBounds()[0].getTypeName());
    System.out.println("upper lower count: " + wtUpper.getLowerBounds().length);
    System.out.println("upper toString: " + wtUpper.toString());

    Field fLower = SampleContainer.class.getDeclaredField("lowerBoundedList");
    ParameterizedType ptLower = (ParameterizedType) fLower.getGenericType();
    WildcardType wtLower = (WildcardType) ptLower.getActualTypeArguments()[0];
    System.out.println("lower lower: " + wtLower.getLowerBounds()[0].getTypeName());
    System.out.println("lower upper: " + wtLower.getUpperBounds()[0].getTypeName());
    System.out.println("lower toString: " + wtLower.toString());
  }

  private static void testGenericArrays() throws Exception {
    Field fGenArr = SampleContainer.class.getDeclaredField("genericArray");
    Type tGenArr = fGenArr.getGenericType();
    System.out.println("genericArray is GenericArrayType: " + (tGenArr instanceof GenericArrayType));
    GenericArrayType gat = (GenericArrayType) tGenArr;
    Type comp = gat.getGenericComponentType();
    System.out.println("genericArray comp is TypeVariable: " + (comp instanceof TypeVariable));
    System.out.println("genericArray comp name: " + ((TypeVariable<?>) comp).getName());
    System.out.println("genericArray toString: " + gat.toString());

    Field fParamArr = SampleContainer.class.getDeclaredField("parameterizedArray");
    Type tParamArr = fParamArr.getGenericType();
    System.out.println("parameterizedArray is GenericArrayType: " + (tParamArr instanceof GenericArrayType));
    GenericArrayType gatParam = (GenericArrayType) tParamArr;
    Type compParam = gatParam.getGenericComponentType();
    System.out.println("parameterizedArray comp is ParameterizedType: " + (compParam instanceof ParameterizedType));
    System.out.println("parameterizedArray toString: " + gatParam.toString());
  }

  private static void testParameterizedTypes() throws Exception {
    Field fMap = SampleContainer.class.getDeclaredField("nestedMap");
    ParameterizedType ptMap = (ParameterizedType) fMap.getGenericType();
    System.out.println("map raw: " + ptMap.getRawType().getTypeName());
    Type[] typeArgs = ptMap.getActualTypeArguments();
    System.out.println("map args count: " + typeArgs.length);
    System.out.println("map key arg: " + typeArgs[0].getTypeName());

    ParameterizedType valType = (ParameterizedType) typeArgs[1];
    System.out.println("map value raw: " + valType.getRawType().getTypeName());
    WildcardType valWildcard = (WildcardType) valType.getActualTypeArguments()[0];
    System.out.println("map value wildcard upper: " + valWildcard.getUpperBounds()[0].getTypeName());

    // Equality test
    Field fMap2 = SampleContainer.class.getDeclaredField("nestedMap");
    ParameterizedType ptMap2 = (ParameterizedType) fMap2.getGenericType();
    System.out.println("parameterized types equal: " + ptMap.equals(ptMap2));
    System.out.println("parameterized hash equal: " + (ptMap.hashCode() == ptMap2.hashCode()));
  }

  private static void testGenericMethods() throws Exception {
    Method m = SampleContainer.class.getDeclaredMethod("process", List.class);
    Type ret = m.getGenericReturnType();
    System.out.println("method ret is GenericArrayType: " + (ret instanceof GenericArrayType));
    Type[] paramTypes = m.getGenericParameterTypes();
    System.out.println("method params count: " + paramTypes.length);
    System.out.println("method param 0 is ParameterizedType: " + (paramTypes[0] instanceof ParameterizedType));
  }
}
