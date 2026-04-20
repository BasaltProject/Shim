/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile;

import java.lang.classfile.instruction.ExceptionCatch;
import java.util.List;
import java.util.Optional;

public interface CodeModel extends CompoundElement<CodeElement>, AttributedElement, MethodElement {
    Optional<MethodModel> parent();
    List<ExceptionCatch> exceptionHandlers();
}
