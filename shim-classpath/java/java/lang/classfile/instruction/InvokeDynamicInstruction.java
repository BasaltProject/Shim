/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.instruction;

import java.lang.classfile.Instruction;
import java.lang.classfile.constantpool.InvokeDynamicEntry;
import java.lang.classfile.constantpool.LoadableConstantEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import shim.classfile.UnboundInsns;

public interface InvokeDynamicInstruction extends Instruction {
    InvokeDynamicEntry invokedynamic();

    default Utf8Entry name() {
        return invokedynamic().name();
    }

    default Utf8Entry type() {
        return invokedynamic().type();
    }

    default MethodTypeDesc typeSymbol() {
        return MethodTypeDesc.ofDescriptor(type().stringValue());
    }

    default DirectMethodHandleDesc bootstrapMethod() {
        return invokedynamic().bootstrap().bootstrapMethod().asSymbol();
    }

    default List<ConstantDesc> bootstrapArgs() {
        List<LoadableConstantEntry> args = invokedynamic().bootstrap().arguments();
        List<ConstantDesc> out = new ArrayList<>(args.size());
        for (LoadableConstantEntry e : args) {
            out.add(e.constantValue());
        }
        return Collections.unmodifiableList(out);
    }

    static InvokeDynamicInstruction of(InvokeDynamicEntry invokedynamic) {
        return new UnboundInsns.UnboundInvokeDynamicInstruction(invokedynamic);
    }
}
