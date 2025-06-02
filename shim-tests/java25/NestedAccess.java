/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

public class NestedAccess {
    private int value = 41;
    private int read() { return value; }
    static class Reader {
        int read(NestedAccess owner) { return owner.read(); }
        void write(NestedAccess owner, int value) { owner.value = value; }
    }
    public static void main(String[] args) {
        NestedAccess owner = new NestedAccess();
        Reader reader = new Reader();
        System.out.println(reader.read(owner));
        reader.write(owner, 59);
        System.out.println(reader.read(owner));
    }
}
