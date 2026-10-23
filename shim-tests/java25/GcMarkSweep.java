/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 * Minimum release: 21
 */

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GcMarkSweep {
    static final class Node {
        final int id;
        final Object[] refs;
        final byte[] data;
        Node next;

        Node(int id, int refCount) {
            this.id = id;
            this.refs = new Object[refCount];
            this.data = new byte[16 + (id * 7) % 300];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (id + i);
            }
        }

        boolean valid() {
            for (int i = 0; i < data.length; i++) {
                if (data[i] != (byte) (id + i)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) {
        Random random = new Random(7);
        Node[] table = new Node[20000];
        Map<Integer, Object> hashes = new HashMap<>();
        List<WeakReference<Object>> weak = new ArrayList<>();
        int errors = 0;
        for (int round = 0; round < 60; round++) {
            for (int k = 0; k < 4000; k++) {
                Node node = new Node(random.nextInt(1 << 20), random.nextInt(6));
                for (int j = 0; j < node.refs.length; j++) {
                    node.refs[j] = table[random.nextInt(table.length)];
                }
                node.next = table[random.nextInt(table.length)];
                table[random.nextInt(table.length)] = node;
                if (random.nextInt(100) == 0) {
                    Object o = new Object();
                    hashes.put(o.hashCode(), o);
                }
                if (random.nextInt(30) == 0) {
                    byte[] junk = new byte[1 + random.nextInt(100000)];
                    junk[0] = 1;
                }
            }
            if (round % 10 == 0) {
                System.gc();
            }
            for (Node node : table) {
                if (node != null && !node.valid()) {
                    errors++;
                }
            }
            for (Map.Entry<Integer, Object> e : hashes.entrySet()) {
                if (e.getValue().hashCode() != e.getKey()) {
                    errors++;
                }
            }
        }
        for (int i = 0; i < 1000; i++) {
            weak.add(new WeakReference<>(new Object()));
        }
        System.gc();
        int alive = 0;
        for (WeakReference<Object> w : weak) {
            if (w.get() != null) {
                alive++;
            }
        }
        System.out.println("errors=" + errors);
        System.out.println("weak cleared=" + (alive == 0));
    }
}
