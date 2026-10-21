/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.Label;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.constant.ClassDesc;
import java.util.List;
import shim.classfile.ObjectVerificationTypeInfoImpl;
import shim.classfile.PoolImpl;
import shim.classfile.StackMapFrameImpl;
import shim.classfile.UninitializedVerificationTypeInfoImpl;

public interface StackMapFrameInfo {
    List<StackMapFrameInfo.VerificationTypeInfo> stack();
    Label target();
    public static StackMapFrameInfo of(Label label, List<StackMapFrameInfo.VerificationTypeInfo> list,
                                       List<StackMapFrameInfo.VerificationTypeInfo> list2) {
        return new StackMapFrameImpl(255, label, list, list2);
    }

    List<StackMapFrameInfo.VerificationTypeInfo> locals();
    int frameType();

    public interface UninitializedVerificationTypeInfo extends StackMapFrameInfo.VerificationTypeInfo {
        public static StackMapFrameInfo.UninitializedVerificationTypeInfo of(Label label) {
            return new UninitializedVerificationTypeInfoImpl(label);
        }

        Label newTarget();
    }

    public interface ObjectVerificationTypeInfo extends StackMapFrameInfo.VerificationTypeInfo {
        ClassEntry className();
        public static StackMapFrameInfo.ObjectVerificationTypeInfo of(ClassEntry classEntry) {
            return new ObjectVerificationTypeInfoImpl(classEntry);
        }

        public static StackMapFrameInfo.ObjectVerificationTypeInfo of(ClassDesc classDesc) {
            return of(new PoolImpl().classEntry(classDesc));
        }

        default ClassDesc classSymbol() {
            return className().asSymbol();
        }
    }

    public enum SimpleVerificationTypeInfo implements StackMapFrameInfo.VerificationTypeInfo {
        TOP(ITEM_TOP),
        INTEGER(ITEM_INTEGER),
        FLOAT(ITEM_FLOAT),
        DOUBLE(ITEM_DOUBLE),
        LONG(ITEM_LONG),
        NULL(ITEM_NULL),
        UNINITIALIZED_THIS(ITEM_UNINITIALIZED_THIS);

        private final int tag;

        SimpleVerificationTypeInfo(int tag) {
            this.tag = tag;
        }

        public int tag() {
            return tag;
        }
    }

    public interface VerificationTypeInfo {
        public static final int ITEM_TOP = 0;
        public static final int ITEM_INTEGER = 1;
        public static final int ITEM_FLOAT = 2;
        public static final int ITEM_DOUBLE = 3;
        public static final int ITEM_LONG = 4;
        public static final int ITEM_NULL = 5;
        public static final int ITEM_UNINITIALIZED_THIS = 6;
        public static final int ITEM_OBJECT = 7;
        public static final int ITEM_UNINITIALIZED = 8;
        int tag();
    }
}
