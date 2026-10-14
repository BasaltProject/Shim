/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 16
import java.util.Objects;

public class ObjectRanges {
    public static void main(String[] args) {
        int[] ints = {Integer.MIN_VALUE, -1, 0, 1, 2, Integer.MAX_VALUE};
        long[] longs = {Long.MIN_VALUE, -1, 0, 1, 2, 2147483648L, Long.MAX_VALUE};
        for (int length : ints) for (int from : ints) {
            try { System.out.println("i " + Objects.checkIndex(from, length)); }
            catch (IndexOutOfBoundsException expected) { System.out.println(expected.getMessage()); }
            for (int end : ints) {
                try { System.out.println("it " + Objects.checkFromToIndex(from, end, length)); }
                catch (IndexOutOfBoundsException expected) { System.out.println(expected.getMessage()); }
                try { System.out.println("is " + Objects.checkFromIndexSize(from, end, length)); }
                catch (IndexOutOfBoundsException expected) { System.out.println(expected.getMessage()); }
            }
        }
        for (long length : longs) for (long from : longs) {
            try { System.out.println("l " + Objects.checkIndex(from, length)); }
            catch (IndexOutOfBoundsException expected) { System.out.println(expected.getMessage()); }
            for (long end : longs) {
                try { System.out.println("lt " + Objects.checkFromToIndex(from, end, length)); }
                catch (IndexOutOfBoundsException expected) { System.out.println(expected.getMessage()); }
                try { System.out.println("ls " + Objects.checkFromIndexSize(from, end, length)); }
                catch (IndexOutOfBoundsException expected) { System.out.println(expected.getMessage()); }
            }
        }
    }
}
