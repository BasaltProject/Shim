/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class StringConcatenation {
    static class Value {
        int calls;
        public String toString() {
            ++calls;
            return "value";
        }
    }
    public static void main(String[] args) {
        int number = 17;
        long wide = 1234567890123L;
        boolean flag = true;
        char letter = 'ñ';
        Value value = new Value();
        Object absent = null;
        System.out.println("prefix[" + number + ":" + wide + ":" + flag + ":" + letter + "]");
        System.out.println("objects[" + value + ":" + absent + "]");
        System.out.println(value.calls);
        System.out.println("unicode[" + number + "\001\002終]");
        for (int i = 0; i < 3; ++i) {
            System.out.println("repeat=" + i);
        }
    }
}
