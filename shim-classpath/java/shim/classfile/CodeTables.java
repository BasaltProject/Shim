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
import java.lang.classfile.Label;
import java.lang.classfile.instruction.CharacterRange;
import java.lang.classfile.instruction.LocalVariable;
import java.lang.classfile.instruction.LocalVariableType;

/** Writes the debug tables collected from the pseudo-instructions of an assembled body. */
final class CodeTables {
    private CodeTables() { }

    /** Writes the CharacterRange, LocalVariable, LocalVariableType and LineNumber tables; returns their count. */
    static int write(BufWriterImpl out, CodeAssembler a) {
        boolean drop = a.options.deadLabels == ClassFile.DeadLabelsOption.DROP_DEAD_LABELS;
        int count = 0;
        if (!a.characterRanges.isEmpty()) {
            int pos = frame(out, Attributes.NAME_CHARACTER_RANGE_TABLE);
            int n = 0;
            int countPos = out.size();
            out.writeU2(0);
            for (CharacterRange r : a.characterRanges) {
                int start = a.bciOf(r.startScope());
                int end = a.bciOf(r.endScope());
                if (start < 0 || end < 0) {
                    if (drop) {
                        continue;
                    }
                    throw new IllegalArgumentException("Unbound label in character range");
                }
                out.writeU2(start);
                out.writeU2(end - 1);
                out.writeInt(r.characterRangeStart());
                out.writeInt(r.characterRangeEnd());
                out.writeU2(r.flags());
                n++;
            }
            out.patchInt(countPos, 2, n);
            end(out, pos);
            count++;
        }
        if (!a.localVariables.isEmpty()) {
            int pos = frame(out, Attributes.NAME_LOCAL_VARIABLE_TABLE);
            int n = 0;
            int countPos = out.size();
            out.writeU2(0);
            for (LocalVariable v : a.localVariables) {
                if (writeLocal(out, a, v.startScope(), v.endScope(), drop)) {
                    out.writeIndex(v.name());
                    out.writeIndex(v.type());
                    out.writeU2(v.slot());
                    n++;
                }
            }
            out.patchInt(countPos, 2, n);
            end(out, pos);
            count++;
        }
        if (!a.localVariableTypes.isEmpty()) {
            int pos = frame(out, Attributes.NAME_LOCAL_VARIABLE_TYPE_TABLE);
            int n = 0;
            int countPos = out.size();
            out.writeU2(0);
            for (LocalVariableType v : a.localVariableTypes) {
                if (writeLocal(out, a, v.startScope(), v.endScope(), drop)) {
                    out.writeIndex(v.name());
                    out.writeIndex(v.signature());
                    out.writeU2(v.slot());
                    n++;
                }
            }
            out.patchInt(countPos, 2, n);
            end(out, pos);
            count++;
        }
        if (!a.lines.isEmpty()) {
            int pos = frame(out, Attributes.NAME_LINE_NUMBER_TABLE);
            int countPos = out.size();
            out.writeU2(0);
            int n = writeLines(out, a);
            out.patchInt(countPos, 2, n);
            end(out, pos);
            count++;
        }
        return count;
    }

    private static boolean writeLocal(BufWriterImpl out, CodeAssembler a, Label startScope, Label endScope,
                                      boolean drop) {
        int start = a.bciOf(startScope);
        int end = a.bciOf(endScope);
        if (start < 0 || end < 0) {
            if (drop) {
                return false;
            }
            throw new IllegalArgumentException("Unbound label in local variable type");
        }
        out.writeU2(start);
        out.writeU2(end - start);
        return true;
    }

    /** Successive entries with the same line are merged; a later entry at the same offset replaces the earlier. */
    private static int writeLines(BufWriterImpl out, CodeAssembler a) {
        int n = 0;
        int lastPc = -1;
        int lastLine = 0;
        int written = -1;
        for (int[] l : a.lines) {
            int pc = a.pcs[l[0]];
            int line = l[1];
            if (lastPc != pc && lastLine != line) {
                if (lastPc >= 0 && lastLine != written) {
                    out.writeU2(lastPc);
                    out.writeU2(lastLine);
                    written = lastLine;
                    n++;
                }
                lastPc = pc;
            }
            lastLine = line;
        }
        if (lastPc >= 0 && lastLine != written) {
            out.writeU2(lastPc);
            out.writeU2(lastLine);
            n++;
        }
        return n;
    }

    static int frame(BufWriterImpl out, String name) {
        out.writeIndex(out.constantPool().utf8Entry(name));
        int pos = out.size();
        out.writeInt(0);
        return pos;
    }

    static void end(BufWriterImpl out, int pos) {
        out.patchInt(pos, 4, out.size() - pos - 4);
    }
}
