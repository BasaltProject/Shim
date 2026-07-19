/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

public class DefaultMethodSelection {
    interface Base { default String who() { return "Base"; } default String other() { return "Base"; } }
    interface Holder extends Base { @Override default String who() { return "Holder"; } @Override default String other() { return "Holder"; } }
    interface Mid extends Holder { }
    interface Side { }
    interface Queue<T> extends Side, Mid, Holder { }
    static class Plain implements Holder { }
    static abstract class Ext extends Plain implements Runnable { }
    static class Leaf extends Ext implements Queue<String> { public void run() { } }
    static class Direct implements Base, Holder { }
    static class Reverse implements Holder, Base { }
    static class Override2 extends Plain implements Base { }

    public static void main(String[] args) {
        System.out.println(new Leaf().who() + " " + ((Base) new Leaf()).other());
        System.out.println(new Direct().who() + " " + new Reverse().other());
        System.out.println(new Override2().who() + " " + ((Base) new Override2()).other());
    }
}
