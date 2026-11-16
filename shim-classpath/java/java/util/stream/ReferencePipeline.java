/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.util.stream;

/** The class JDK-targeting libraries look up by name to recognise a stream the JDK built itself. */
abstract class ReferencePipeline<T> implements Stream<T> { }
