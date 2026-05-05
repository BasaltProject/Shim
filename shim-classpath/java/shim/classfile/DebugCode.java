/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.Attribute;
import java.lang.classfile.CodeElement;
import java.lang.classfile.Instruction;
import java.lang.classfile.Label;
import java.lang.classfile.MethodModel;
import java.lang.classfile.TypeAnnotation;
import java.lang.classfile.attribute.CharacterRangeInfo;
import java.lang.classfile.attribute.CharacterRangeTableAttribute;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.attribute.LineNumberInfo;
import java.lang.classfile.attribute.LineNumberTableAttribute;
import java.lang.classfile.attribute.LocalVariableInfo;
import java.lang.classfile.attribute.LocalVariableTableAttribute;
import java.lang.classfile.attribute.LocalVariableTypeInfo;
import java.lang.classfile.attribute.LocalVariableTypeTableAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.StackMapFrameInfo;
import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.instruction.ExceptionCatch;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The code part of the debug tree: tables, frames, exception handlers and the instruction listing. */
final class DebugCode {
    private DebugCode() { }

    private static final class Handler {
        final int start;
        final int end;
        final int handler;
        final String catchType;

        Handler(int start, int end, int handler, String catchType) {
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.catchType = catchType;
        }

        DebugNode node(String name, String typeKey) {
            return DebugNode.flowMap(name).with("start", start).with("end", end).with("handler", handler)
                    .with(typeKey, catchType);
        }
    }

    static DebugNode codeTree(CodeAttribute com) {
        DebugNode code = DebugNode.blockMap("code");
        code.with("max stack", com.maxStack());
        code.with("max locals", com.maxLocals());
        code.with(DebugPrinter.attributeNames(com));
        DebugNode stackMap = DebugNode.blockMap("stack map frames");
        Map<Integer, List<DebugNode>> visible = new HashMap<>();
        Map<Integer, List<DebugNode>> invisible = new HashMap<>();
        List<LocalVariableInfo> locals = Collections.emptyList();
        for (Attribute<?> attr : com.attributes()) {
            if (attr instanceof StackMapTableAttribute) {
                for (StackMapFrameInfo f : ((StackMapTableAttribute) attr).entries()) {
                    stackMap.with(DebugNode.flowMap(com.labelToBci(f.target()))
                            .with(DebugNode.flowList("locals", types(com, f.locals())))
                            .with(DebugNode.flowList("stack", types(com, f.stack()))));
                }
                code.with(stackMap);
            } else if (attr instanceof LocalVariableTableAttribute) {
                locals = ((LocalVariableTableAttribute) attr).localVariables();
                DebugNode list = DebugNode.blockList("local variables");
                for (LocalVariableInfo lv : locals) {
                    list.add(DebugNode.flowMap("lv").with("start", lv.startPc())
                            .with("end", lv.startPc() + lv.length()).with("slot", lv.slot())
                            .with("name", lv.name().stringValue()).with("type", lv.type().stringValue()));
                }
                code.with(list);
            } else if (attr instanceof LocalVariableTypeTableAttribute) {
                DebugNode list = DebugNode.blockList("local variable types");
                for (LocalVariableTypeInfo lv : ((LocalVariableTypeTableAttribute) attr).localVariableTypes()) {
                    list.add(DebugNode.flowMap("lvt").with("start", lv.startPc())
                            .with("end", lv.startPc() + lv.length()).with("slot", lv.slot())
                            .with("name", lv.name().stringValue())
                            .with("signature", lv.signature().stringValue()));
                }
                code.with(list);
            } else if (attr instanceof LineNumberTableAttribute) {
                DebugNode list = DebugNode.blockList("line numbers");
                for (LineNumberInfo ln : ((LineNumberTableAttribute) attr).lineNumbers()) {
                    list.add(DebugNode.flowMap("ln").with("start", ln.startPc()).with("line number", ln.lineNumber()));
                }
                code.with(list);
            } else if (attr instanceof CharacterRangeTableAttribute) {
                DebugNode list = DebugNode.blockList("character ranges");
                for (CharacterRangeInfo cr : ((CharacterRangeTableAttribute) attr).characterRangeTable()) {
                    list.add(DebugNode.flowMap("cr").with("start", cr.startPc()).with("end", cr.endPc())
                            .with("range start", cr.characterRangeStart())
                            .with("range end", cr.characterRangeEnd()).with("flags", cr.flags()));
                }
                code.with(list);
            } else if (attr instanceof RuntimeVisibleTypeAnnotationsAttribute) {
                byOffset(com, ((RuntimeVisibleTypeAnnotationsAttribute) attr).annotations(), visible);
            } else if (attr instanceof RuntimeInvisibleTypeAnnotationsAttribute) {
                byOffset(com, ((RuntimeInvisibleTypeAnnotationsAttribute) attr).annotations(), invisible);
            }
        }
        code.withAll(DebugAttributes.attributes(com.attributes()));
        if (!stackMap.entries.containsKey(0)) {
            code.with(DebugNode.flowMap("//stack map frame @0")
                    .with(DebugNode.flowList("locals", initialLocals(com.parent().get())))
                    .with(DebugNode.flowList("stack")));
        }
        List<Handler> handlers = new ArrayList<>();
        for (ExceptionCatch ec : com.exceptionHandlers()) {
            handlers.add(new Handler(com.labelToBci(ec.tryStart()), com.labelToBci(ec.tryEnd()),
                    com.labelToBci(ec.handler()),
                    ec.catchType().isPresent() ? ec.catchType().get().asInternalName() : "null"));
        }
        int bci = 0;
        for (CodeElement e : com) {
            if (!(e instanceof Instruction)) {
                continue;
            }
            Instruction ins = (Instruction) e;
            DebugNode frame = stackMap.mapEntry(bci);
            if (frame != null) {
                DebugNode copy = DebugNode.flowMap("//stack map frame @" + bci);
                for (DebugNode v : frame.entries.values()) {
                    copy.with(v);
                }
                code.with(copy);
            }
            inlineAnnotations(code, "//invisible type annotations @" + bci, invisible.get(bci));
            inlineAnnotations(code, "//visible type annotations @" + bci, visible.get(bci));
            for (int i = 0; i < handlers.size(); i++) {
                Handler h = handlers.get(i);
                if (h.start == bci) {
                    code.with(h.node("//try block " + (i + 1) + " start", "catch type"));
                }
                if (h.end == bci) {
                    code.with(h.node("//try block " + (i + 1) + " end", "catch type"));
                }
                if (h.handler == bci) {
                    code.with(h.node("//exception handler " + (i + 1) + " start", "catch type"));
                }
            }
            DebugNode in = DebugNode.flowMap(bci).with("opcode", ins.opcode().name());
            DebugInstructions.details(in, ins, com, locals, bci);
            code.with(in);
            bci += ins.sizeInBytes();
        }
        if (!handlers.isEmpty()) {
            DebugNode map = DebugNode.blockMap("exception handlers");
            for (int i = 0; i < handlers.size(); i++) {
                map.with(handlers.get(i).node("handler " + (i + 1), "type"));
            }
            code.with(map);
        }
        return code;
    }

    private static void inlineAnnotations(DebugNode code, String name, List<DebugNode> annos) {
        if (annos != null) {
            DebugNode list = DebugNode.flowList(name);
            for (DebugNode a : annos) {
                list.add(a);
            }
            code.with(list);
        }
    }

    private static void byOffset(CodeAttribute com, List<TypeAnnotation> annos, Map<Integer, List<DebugNode>> map) {
        for (TypeAnnotation ta : annos) {
            TypeAnnotation.TargetInfo ti = ta.targetInfo();
            List<Label> labels = new ArrayList<>();
            if (ti instanceof TypeAnnotation.LocalVarTarget) {
                for (TypeAnnotation.LocalVarTargetInfo lv : ((TypeAnnotation.LocalVarTarget) ti).table()) {
                    labels.add(lv.startLabel());
                }
            } else if (ti instanceof TypeAnnotation.OffsetTarget) {
                labels.add(((TypeAnnotation.OffsetTarget) ti).target());
            } else if (ti instanceof TypeAnnotation.TypeArgumentTarget) {
                labels.add(((TypeAnnotation.TypeArgumentTarget) ti).target());
            }
            for (Label l : labels) {
                int bci = com.labelToBci(l);
                List<DebugNode> list = map.get(bci);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(bci, list);
                }
                list.add(DebugAttributes.typeAnnotation(ta));
            }
        }
    }

    private static List<Object> types(CodeAttribute com, List<StackMapFrameInfo.VerificationTypeInfo> vtis) {
        List<Object> out = new ArrayList<>();
        for (StackMapFrameInfo.VerificationTypeInfo vti : vtis) {
            if (vti instanceof StackMapFrameInfo.SimpleVerificationTypeInfo) {
                switch ((StackMapFrameInfo.SimpleVerificationTypeInfo) vti) {
                    case DOUBLE: out.add("double"); out.add("double2"); break;
                    case FLOAT: out.add("float"); break;
                    case INTEGER: out.add("int"); break;
                    case LONG: out.add("long"); out.add("long2"); break;
                    case NULL: out.add(null); break;
                    case TOP: out.add("?"); break;
                    default: out.add("THIS"); break;
                }
            } else if (vti instanceof StackMapFrameInfo.ObjectVerificationTypeInfo) {
                out.add(((StackMapFrameInfo.ObjectVerificationTypeInfo) vti).className().asInternalName());
            } else {
                out.add("UNINITIALIZED @" + com.labelToBci(
                        ((StackMapFrameInfo.UninitializedVerificationTypeInfo) vti).newTarget()));
            }
        }
        return out;
    }

    private static List<Object> initialLocals(MethodModel m) {
        List<Object> out = new ArrayList<>();
        MethodTypeDesc type = m.methodTypeSymbol();
        if (!m.flags().has(AccessFlag.STATIC)) {
            ClassEntry thisClass = m.parent().get().thisClass();
            if ("<init>".equals(m.methodName().stringValue())
                    && !ConstantDescs.CD_Object.equals(thisClass.asSymbol())) {
                out.add("THIS");
            } else {
                out.add(thisClass.asInternalName());
            }
        }
        for (ClassDesc d : type.parameterList()) {
            String desc = d.descriptorString();
            switch (desc.charAt(0)) {
                case 'J': out.add("long"); out.add("long2"); break;
                case 'F': out.add("float"); break;
                case 'D': out.add("double"); out.add("double2"); break;
                case 'I': case 'B': case 'C': case 'S': case 'Z': out.add("int"); break;
                case 'L': out.add(desc.substring(1, desc.length() - 1)); break;
                default: out.add(desc); break;
            }
        }
        return out;
    }
}
