/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;

interface Node<T> {
  Spliterator<T> spliterator();

  void forEach(Consumer<? super T> consumer);

  default int getChildCount() {
    return 0;
  }

  default Node<T> getChild(int i) {
    throw new IndexOutOfBoundsException();
  }

  default Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
    if (from == 0 && to == count()) {
      return this;
    }
    throw new UnsupportedOperationException();
  }

  T[] asArray(IntFunction<T[]> generator);

  void copyInto(T[] array, int offset);

  default StreamShape getShape() {
    return StreamShape.REFERENCE;
  }

  long count();

  interface Builder<T> extends Sink<T> {
    Node<T> build();

    interface OfInt extends Node.Builder<Integer>, Sink.OfInt {
      @Override
      Node.OfInt build();
    }

    interface OfLong extends Node.Builder<Long>, Sink.OfLong {
      @Override
      Node.OfLong build();
    }

    interface OfDouble extends Node.Builder<Double>, Sink.OfDouble {
      @Override
      Node.OfDouble build();
    }
  }

  interface OfPrimitive<T, T_CONS, T_ARR, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>,
      T_NODE extends OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>> extends Node<T> {
    @Override
    T_SPLITR spliterator();

    void forEach(T_CONS action);

    @Override
    default T_NODE getChild(int i) {
      throw new IndexOutOfBoundsException();
    }

    T_NODE truncate(long from, long to, IntFunction<T[]> generator);

    @Override
    default T[] asArray(IntFunction<T[]> generator) {
      long size = count();
      if (size >= Integer.MAX_VALUE) {
        throw new IllegalArgumentException("Stream size exceeds max array size");
      }
      T[] boxed = generator.apply((int) size);
      copyInto(boxed, 0);
      return boxed;
    }

    T_ARR asPrimitiveArray();

    T_ARR newArray(int count);

    void copyInto(T_ARR array, int offset);
  }

  interface OfInt extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, OfInt> {
    @Override
    default void forEach(Consumer<? super Integer> consumer) {
      if (consumer instanceof IntConsumer) {
        forEach((IntConsumer) consumer);
      } else {
        spliterator().forEachRemaining(consumer);
      }
    }

    @Override
    default void copyInto(Integer[] boxed, int offset) {
      int[] array = asPrimitiveArray();
      for (int i = 0; i < array.length; i++) {
        boxed[offset + i] = array[i];
      }
    }

    @Override
    default Node.OfInt truncate(long from, long to, IntFunction<Integer[]> generator) {
      if (from == 0 && to == count()) {
        return this;
      }
      throw new UnsupportedOperationException();
    }

    @Override
    default int[] newArray(int count) {
      return new int[count];
    }

    @Override
    default StreamShape getShape() {
      return StreamShape.INT_VALUE;
    }
  }

  interface OfLong extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, OfLong> {
    @Override
    default void forEach(Consumer<? super Long> consumer) {
      if (consumer instanceof LongConsumer) {
        forEach((LongConsumer) consumer);
      } else {
        spliterator().forEachRemaining(consumer);
      }
    }

    @Override
    default void copyInto(Long[] boxed, int offset) {
      long[] array = asPrimitiveArray();
      for (int i = 0; i < array.length; i++) {
        boxed[offset + i] = array[i];
      }
    }

    @Override
    default Node.OfLong truncate(long from, long to, IntFunction<Long[]> generator) {
      if (from == 0 && to == count()) {
        return this;
      }
      throw new UnsupportedOperationException();
    }

    @Override
    default long[] newArray(int count) {
      return new long[count];
    }

    @Override
    default StreamShape getShape() {
      return StreamShape.LONG_VALUE;
    }
  }

  interface OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, OfDouble> {
    @Override
    default void forEach(Consumer<? super Double> consumer) {
      if (consumer instanceof DoubleConsumer) {
        forEach((DoubleConsumer) consumer);
      } else {
        spliterator().forEachRemaining(consumer);
      }
    }

    @Override
    default void copyInto(Double[] boxed, int offset) {
      double[] array = asPrimitiveArray();
      for (int i = 0; i < array.length; i++) {
        boxed[offset + i] = array[i];
      }
    }

    @Override
    default Node.OfDouble truncate(long from, long to, IntFunction<Double[]> generator) {
      if (from == 0 && to == count()) {
        return this;
      }
      throw new UnsupportedOperationException();
    }

    @Override
    default double[] newArray(int count) {
      return new double[count];
    }

    @Override
    default StreamShape getShape() {
      return StreamShape.DOUBLE_VALUE;
    }
  }
}
