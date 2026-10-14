/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

package java.util.function;

@FunctionalInterface
public interface LongConsumer {
    void accept(long value);
    default LongConsumer andThen(LongConsumer after) {
        java.util.Objects.requireNonNull(after);
        return value -> { accept(value); after.accept(value); };
    }
}
