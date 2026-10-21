/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.ExceptionCatch;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The checks behind {@code ClassFile.verify}: class structure first, then each method body by type checking. */
final class Verifier {
    private Verifier() { }

    static void verify(ClassFileImpl cf, ClassModel model, List<VerifyError> errors) {
        if (model.majorVersion() > ClassFile.latestMajorVersion()) {
            errors.add(new VerifyError("Unsupported class file version: " + model.majorVersion()));
            return;
        }
        for (java.lang.classfile.constantpool.PoolEntry e : model.constantPool()) {
            e.index();
        }
        for (MethodModel m : model.methods()) {
            Optional<CodeAttribute> code = m.findAttribute(Attributes.code());
            if (code.isPresent()) {
                if (code.get() instanceof CodeImpl) {
                    ((CodeImpl) code.get()).inflateLabels();
                }
                for (ExceptionCatch c : code.get().exceptionHandlers()) {
                    c.catchType();
                }
            }
        }
        List<VerifyError> structure = new ArrayList<>();
        StructureChecks.check(model, structure);
        errors.addAll(structure);
        String name = model.thisClass().asInternalName();
        if (name.equals("java/lang/Object") || name.equals("java/lang/Class") || name.equals("java/lang/String")
                || name.equals("java/lang/Throwable")) {
            for (MethodModel m : model.methods()) {
                if (argumentsDoNotFit(m)) {
                    errors.add(new VerifyError(argumentsMessage(model, m)));
                }
            }
            return;
        }
        int major = model.majorVersion();
        if (major < 50) {
            errors.add(new VerifyError("Inference verification is not supported"));
            return;
        }
        VerifyHierarchy hierarchy = new VerifyHierarchy(cf.hierarchyResolver());
        List<VerifyError> found = new ArrayList<>();
        for (MethodModel m : model.methods()) {
            int flags = m.flags().flagsMask();
            Optional<CodeAttribute> code = m.findAttribute(Attributes.code());
            if (argumentsDoNotFit(m)) {
                int max = code.get().maxLocals();
                found.add(new VerifyError(argumentsMessage(model, m)));
                if ((flags & ClassFile.ACC_BRIDGE) == 0) {
                    found.add(new VerifyError("java.lang.ArrayIndexOutOfBoundsException: Index " + max
                        + " out of bounds for length " + max));
                }
                continue;
            }
            String message = verifyMethod(model, m, hierarchy);
            if (message != null) {
                found.add(new VerifyError(message));
            }
        }
        if (!found.isEmpty() && major < 51) {
            errors.add(new VerifyError("Inference verification is not supported"));
        } else {
            errors.addAll(found);
        }
    }

    private static boolean argumentsDoNotFit(MethodModel m) {
        Optional<CodeAttribute> code = m.findAttribute(Attributes.code());
        return (m.flags().flagsMask() & (ClassFile.ACC_ABSTRACT | ClassFile.ACC_NATIVE)) == 0 && code.isPresent()
            && MethodVerifier.argumentSlots(m) > code.get().maxLocals();
    }

    private static String argumentsMessage(ClassModel model, MethodModel m) {
        return "Arguments can't fit into locals in method " + model.thisClass().asSymbol().displayName() + "::"
            + m.methodName().stringValue() + StructureChecks.displayParameters(m.methodType().stringValue());
    }

    private static String verifyMethod(ClassModel model, MethodModel m, VerifyHierarchy hierarchy) {
        int flags = m.flags().flagsMask();
        Optional<CodeAttribute> code = m.findAttribute(Attributes.code());
        if ((flags & (ClassFile.ACC_ABSTRACT | ClassFile.ACC_NATIVE | ClassFile.ACC_BRIDGE)) != 0) {
            return null;
        }
        MethodVerifier v = null;
        try {
            if (!code.isPresent()) {
                v = new MethodVerifier(model, m, null, hierarchy);
                return v.message("Missing Code attribute", null, null, false);
            }
            v = new MethodVerifier(model, m, code.get(), hierarchy);
            v.verify();
            return null;
        } catch (VerifyFrame.Failure f) {
            return f.bare ? f.getMessage() : v.message(f.getMessage(), f.detail, f.frames, f.noOffset);
        } catch (RuntimeException e) {
            return e.toString();
        } finally {
            if (v != null) {
                hierarchy.lastOffset = v.bci;
            }
        }
    }
}
