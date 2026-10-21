/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Label;

/** Maps labels to bytecode offsets for one code body. */
public interface LabelContext {
    Label newLabel();

    /** The label for the given bytecode offset, created on first request. */
    Label getLabel(int bci);

    /** The bytecode offset a label is bound to; throws IllegalArgumentException for a foreign or unbound label. */
    int labelToBci(Label label);
}
