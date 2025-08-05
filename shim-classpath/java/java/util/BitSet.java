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

package java.util;

import java.io.Serializable;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class BitSet implements Serializable, Cloneable {
  private static final long serialVersionUID = 7997698588984042384L;

  private static final int BITS_PER_LONG = 64;
  private static final int BITS_PER_LONG_SHIFT = 6;
  private static final long MASK = 0xFFFFFFFFFFFFFFFFL;

  private long[] bits;

  private static int longPosition(int index) {
    return index >> BITS_PER_LONG_SHIFT;
  }

  private static long bitPosition(int index) {
    return 1L << (index % BITS_PER_LONG);
  }

  public BitSet() {
    this(BITS_PER_LONG);
  }

  public BitSet(int nbits) {
    if (nbits < 0) {
      throw new NegativeArraySizeException("nbits < 0: " + nbits);
    }
    int words = (nbits + BITS_PER_LONG - 1) >> BITS_PER_LONG_SHIFT;
    this.bits = new long[Math.max(words, 1)];
  }

  private void enlarge(int newPartition) {
    if (bits == null || bits.length < (newPartition + 1)) {
      int newLen = Math.max(newPartition + 1, (bits == null) ? 1 : (bits.length * 2));
      long[] newBits = new long[newLen];
      if (bits != null) {
        System.arraycopy(bits, 0, newBits, 0, bits.length);
      }
      bits = newBits;
    }
  }

  public boolean get(int bitIndex) {
    if (bitIndex < 0) {
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
    }
    int pos = longPosition(bitIndex);
    if (pos < bits.length) {
      return (bits[pos] & bitPosition(bitIndex)) != 0;
    }
    return false;
  }

  public BitSet get(int fromIndex, int toIndex) {
    if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex) {
      throw new IndexOutOfBoundsException();
    }
    int len = length();
    if (len <= fromIndex || fromIndex == toIndex) {
      return new BitSet(0);
    }
    if (toIndex > len) {
      toIndex = len;
    }
    BitSet result = new BitSet(toIndex - fromIndex);
    for (int i = fromIndex; i < toIndex; i++) {
      if (get(i)) {
        result.set(i - fromIndex);
      }
    }
    return result;
  }

  public void set(int bitIndex) {
    if (bitIndex < 0) {
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
    }
    int pos = longPosition(bitIndex);
    enlarge(pos);
    bits[pos] |= bitPosition(bitIndex);
  }

  public void set(int bitIndex, boolean value) {
    if (value) {
      set(bitIndex);
    } else {
      clear(bitIndex);
    }
  }

  public void set(int fromIndex, int toIndex) {
    if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex) {
      throw new IndexOutOfBoundsException();
    }
    if (fromIndex == toIndex) {
      return;
    }
    int startWord = longPosition(fromIndex);
    int endWord = longPosition(toIndex - 1);
    enlarge(endWord);

    long firstWordMask = MASK << (fromIndex % BITS_PER_LONG);
    long lastWordMask = MASK >>> (BITS_PER_LONG - (toIndex % BITS_PER_LONG == 0 ? BITS_PER_LONG : toIndex % BITS_PER_LONG));

    if (startWord == endWord) {
      bits[startWord] |= (firstWordMask & lastWordMask);
    } else {
      bits[startWord] |= firstWordMask;
      for (int i = startWord + 1; i < endWord; i++) {
        bits[i] = MASK;
      }
      bits[endWord] |= lastWordMask;
    }
  }

  public void set(int fromIndex, int toIndex, boolean value) {
    if (value) {
      set(fromIndex, toIndex);
    } else {
      clear(fromIndex, toIndex);
    }
  }

  public void clear(int bitIndex) {
    if (bitIndex < 0) {
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
    }
    int pos = longPosition(bitIndex);
    if (pos < bits.length) {
      bits[pos] &= ~bitPosition(bitIndex);
    }
  }

  public void clear(int fromIndex, int toIndex) {
    if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex) {
      throw new IndexOutOfBoundsException();
    }
    if (fromIndex == toIndex) {
      return;
    }
    int startWord = longPosition(fromIndex);
    if (startWord >= bits.length) {
      return;
    }
    int endWord = longPosition(toIndex - 1);
    if (endWord >= bits.length) {
      endWord = bits.length - 1;
    }

    long firstWordMask = MASK << (fromIndex % BITS_PER_LONG);
    long lastWordMask = MASK >>> (BITS_PER_LONG - (toIndex % BITS_PER_LONG == 0 ? BITS_PER_LONG : toIndex % BITS_PER_LONG));

    if (startWord == endWord) {
      bits[startWord] &= ~(firstWordMask & lastWordMask);
    } else {
      bits[startWord] &= ~firstWordMask;
      for (int i = startWord + 1; i < endWord; i++) {
        bits[i] = 0;
      }
      bits[endWord] &= ~lastWordMask;
    }
  }

  public void clear() {
    for (int i = 0; i < bits.length; i++) {
      bits[i] = 0;
    }
  }

  public void flip(int bitIndex) {
    if (bitIndex < 0) {
      throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
    }
    int pos = longPosition(bitIndex);
    enlarge(pos);
    bits[pos] ^= bitPosition(bitIndex);
  }

  public void flip(int fromIndex, int toIndex) {
    if (fromIndex < 0 || toIndex < 0 || fromIndex > toIndex) {
      throw new IndexOutOfBoundsException();
    }
    if (fromIndex == toIndex) {
      return;
    }
    int startWord = longPosition(fromIndex);
    int endWord = longPosition(toIndex - 1);
    enlarge(endWord);

    long firstWordMask = MASK << (fromIndex % BITS_PER_LONG);
    long lastWordMask = MASK >>> (BITS_PER_LONG - (toIndex % BITS_PER_LONG == 0 ? BITS_PER_LONG : toIndex % BITS_PER_LONG));

    if (startWord == endWord) {
      bits[startWord] ^= (firstWordMask & lastWordMask);
    } else {
      bits[startWord] ^= firstWordMask;
      for (int i = startWord + 1; i < endWord; i++) {
        bits[i] ^= MASK;
      }
      bits[endWord] ^= lastWordMask;
    }
  }

  public void and(BitSet set) {
    if (this == set) {
      return;
    }
    int common = Math.min(bits.length, set.bits.length);
    for (int i = 0; i < common; i++) {
      bits[i] &= set.bits[i];
    }
    for (int i = common; i < bits.length; i++) {
      bits[i] = 0;
    }
  }

  public void or(BitSet set) {
    if (this == set) {
      return;
    }
    int common = Math.min(bits.length, set.bits.length);
    if (set.bits.length > bits.length) {
      enlarge(set.bits.length - 1);
    }
    for (int i = 0; i < common; i++) {
      bits[i] |= set.bits[i];
    }
    if (set.bits.length > common) {
      System.arraycopy(set.bits, common, bits, common, set.bits.length - common);
    }
  }

  public void xor(BitSet set) {
    int common = Math.min(bits.length, set.bits.length);
    if (set.bits.length > bits.length) {
      enlarge(set.bits.length - 1);
    }
    for (int i = 0; i < common; i++) {
      bits[i] ^= set.bits[i];
    }
    if (set.bits.length > common) {
      System.arraycopy(set.bits, common, bits, common, set.bits.length - common);
    }
  }

  public void andNot(BitSet set) {
    int common = Math.min(bits.length, set.bits.length);
    for (int i = 0; i < common; i++) {
      bits[i] &= ~set.bits[i];
    }
  }

  public boolean intersects(BitSet set) {
    int common = Math.min(bits.length, set.bits.length);
    for (int i = 0; i < common; i++) {
      if ((bits[i] & set.bits[i]) != 0) {
        return true;
      }
    }
    return false;
  }

  public int cardinality() {
    int sum = 0;
    for (int i = 0; i < bits.length; i++) {
      sum += Long.bitCount(bits[i]);
    }
    return sum;
  }

  public boolean isEmpty() {
    for (int i = 0; i < bits.length; i++) {
      if (bits[i] != 0) {
        return false;
      }
    }
    return true;
  }

  public int length() {
    for (int i = bits.length - 1; i >= 0; i--) {
      if (bits[i] != 0) {
        return (i << BITS_PER_LONG_SHIFT) + (BITS_PER_LONG - Long.numberOfLeadingZeros(bits[i]));
      }
    }
    return 0;
  }

  public int size() {
    return bits.length << BITS_PER_LONG_SHIFT;
  }

  public int nextSetBit(int fromIndex) {
    if (fromIndex < 0) {
      throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
    }
    int u = longPosition(fromIndex);
    if (u >= bits.length) {
      return -1;
    }
    long word = bits[u] & (MASK << (fromIndex % BITS_PER_LONG));
    while (true) {
      if (word != 0) {
        return (u << BITS_PER_LONG_SHIFT) + Long.numberOfTrailingZeros(word);
      }
      if (++u >= bits.length) {
        return -1;
      }
      word = bits[u];
    }
  }

  public int nextClearBit(int fromIndex) {
    if (fromIndex < 0) {
      throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
    }
    int u = longPosition(fromIndex);
    if (u >= bits.length) {
      return fromIndex;
    }
    long word = (~bits[u]) & (MASK << (fromIndex % BITS_PER_LONG));
    while (true) {
      if (word != 0) {
        return (u << BITS_PER_LONG_SHIFT) + Long.numberOfTrailingZeros(word);
      }
      if (++u >= bits.length) {
        return u << BITS_PER_LONG_SHIFT;
      }
      word = ~bits[u];
    }
  }

  public int previousSetBit(int fromIndex) {
    if (fromIndex < 0) {
      if (fromIndex == -1) {
        return -1;
      }
      throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
    }
    int u = longPosition(fromIndex);
    if (u >= bits.length) {
      u = bits.length - 1;
      if (u < 0) {
        return -1;
      }
      fromIndex = (u + 1) * BITS_PER_LONG - 1;
    }
    long word = bits[u] & (MASK >>> (BITS_PER_LONG - 1 - (fromIndex % BITS_PER_LONG)));
    while (true) {
      if (word != 0) {
        return (u << BITS_PER_LONG_SHIFT) + (BITS_PER_LONG - 1 - Long.numberOfLeadingZeros(word));
      }
      if (u == 0) {
        return -1;
      }
      word = bits[--u];
    }
  }

  public int previousClearBit(int fromIndex) {
    if (fromIndex < 0) {
      if (fromIndex == -1) {
        return -1;
      }
      throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
    }
    int u = longPosition(fromIndex);
    if (u >= bits.length) {
      return fromIndex;
    }
    long word = (~bits[u]) & (MASK >>> (BITS_PER_LONG - 1 - (fromIndex % BITS_PER_LONG)));
    while (true) {
      if (word != 0) {
        return (u << BITS_PER_LONG_SHIFT) + (BITS_PER_LONG - 1 - Long.numberOfLeadingZeros(word));
      }
      if (u == 0) {
        return -1;
      }
      word = ~bits[--u];
    }
  }

  public long[] toLongArray() {
    int wordsInUse = 0;
    for (int i = bits.length - 1; i >= 0; i--) {
      if (bits[i] != 0) {
        wordsInUse = i + 1;
        break;
      }
    }
    return Arrays.copyOf(bits, wordsInUse);
  }

  public byte[] toByteArray() {
    int wordsInUse = 0;
    for (int i = bits.length - 1; i >= 0; i--) {
      if (bits[i] != 0) {
        wordsInUse = i + 1;
        break;
      }
    }
    if (wordsInUse == 0) {
      return new byte[0];
    }
    int len = 8 * (wordsInUse - 1);
    for (long x = bits[wordsInUse - 1]; x != 0; x >>>= 8) {
      len++;
    }
    byte[] bytes = new byte[len];
    for (int i = 0; i < len; i++) {
      bytes[i] = (byte) ((bits[i / 8] >>> (8 * (i % 8))) & 0xff);
    }
    return bytes;
  }

  public static BitSet valueOf(long[] longs) {
    if (longs == null) {
      throw new NullPointerException();
    }
    int n;
    for (n = longs.length; n > 0 && longs[n - 1] == 0; n--);
    BitSet bs = new BitSet(0);
    bs.bits = Arrays.copyOf(longs, Math.max(n, 1));
    return bs;
  }

  public static BitSet valueOf(byte[] bytes) {
    if (bytes == null) {
      throw new NullPointerException();
    }
    int n = (bytes.length + 7) / 8;
    long[] longs = new long[Math.max(n, 1)];
    for (int i = 0; i < bytes.length; i++) {
      longs[i / 8] |= ((long) (bytes[i] & 0xff)) << (8 * (i % 8));
    }
    return valueOf(longs);
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append('{');
    int i = nextSetBit(0);
    if (i != -1) {
      b.append(i);
      while (true) {
        if (++i < 0) {
          break;
        }
        i = nextSetBit(i);
        if (i < 0) {
          break;
        }
        int endOfRun = nextClearBit(i);
        do {
          b.append(", ").append(i);
        } while (++i != endOfRun);
      }
    }
    b.append('}');
    return b.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BitSet)) {
      return false;
    }
    BitSet other = (BitSet) obj;
    int common = Math.min(bits.length, other.bits.length);
    for (int i = 0; i < common; i++) {
      if (bits[i] != other.bits[i]) {
        return false;
      }
    }
    for (int i = common; i < bits.length; i++) {
      if (bits[i] != 0) {
        return false;
      }
    }
    for (int i = common; i < other.bits.length; i++) {
      if (other.bits[i] != 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    long h = 1234;
    int wordsInUse = 0;
    for (int i = bits.length - 1; i >= 0; i--) {
      if (bits[i] != 0) {
        wordsInUse = i + 1;
        break;
      }
    }
    for (int i = wordsInUse; --i >= 0; ) {
      h ^= bits[i] * (i + 1);
    }
    return (int) ((h >> 32) ^ h);
  }

  @Override
  public Object clone() {
    try {
      BitSet result = (BitSet) super.clone();
      result.bits = bits.clone();
      return result;
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  public IntStream stream() {
    PrimitiveIterator.OfInt it = new PrimitiveIterator.OfInt() {
      private int next = nextSetBit(0);

      @Override
      public boolean hasNext() {
        return next != -1;
      }

      @Override
      public int nextInt() {
        if (next == -1) {
          throw new NoSuchElementException();
        }
        int ret = next;
        next = (ret == Integer.MAX_VALUE) ? -1 : nextSetBit(ret + 1);
        return ret;
      }
    };
    return StreamSupport.intStream(
        Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED),
        false);
  }
}
