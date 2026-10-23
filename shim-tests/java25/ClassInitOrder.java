/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ClassInitOrder {
    static final List<String> log = new ArrayList<>();

    static class Base {
        static { log.add("Base"); }
        static int value = 1;
    }

    static class Middle extends Base {
        static { log.add("Middle"); }
    }

    static class Leaf extends Middle {
        static { log.add("Leaf"); }
        static void touch() { }
    }

    static class Plain extends Base {
        static final int CONSTANT = 7;
    }

    static class Failing {
        static int boom = 1 / Integer.parseInt("0");
    }

    static class Cyclic1 {
        static { log.add("Cyclic1 start"); log.add("saw " + Cyclic2.value); log.add("Cyclic1 end"); }
        static int value = 1;
    }

    static class Cyclic2 {
        static { log.add("Cyclic2 start"); log.add("saw " + Cyclic1.value); log.add("Cyclic2 end"); }
        static int value = 2;
    }

    static class Slow {
        static final CountDownLatch entered = new CountDownLatch(1);
        static { entered.countDown(); try { Thread.sleep(50); } catch (InterruptedException e) { } log.add("Slow"); }
        static int value = 3;
    }

    interface WithDefault {
        int CONSTANT = compute();
        static int compute() { log.add("WithDefault"); return 4; }
        default int hello() { return 1; }
    }

    static class Implementor implements WithDefault { }

    static void flush(String label) {
        System.out.println(label + ": " + log);
        log.clear();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("constant: " + Plain.CONSTANT);
        flush("after constant");
        new Leaf();
        flush("after new Leaf");
        new Leaf();
        Leaf.touch();
        flush("after second use");
        System.out.println(Plain.value);
        flush("after Plain.value");
        for (int i = 0; i < 2; i++) {
            try {
                System.out.println(Failing.boom);
            } catch (Throwable error) {
                System.out.println("failing " + i + ": " + error.getClass().getName());
            }
        }
        System.out.println(Cyclic1.value);
        flush("after cycle");
        Thread[] threads = new Thread[4];
        int[] seen = new int[threads.length];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> seen[index] = Slow.value);
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println(seen[0] + " " + seen[1] + " " + seen[2] + " " + seen[3]);
        flush("after threads");
        new Implementor();
        flush("after implementor");
        System.out.println(new Implementor().hello());
        System.out.println(WithDefault.CONSTANT);
        flush("after interface constant");
        Object[] array = new Leaf[2];
        System.out.println(array.length);
        flush("after array");
    }
}
