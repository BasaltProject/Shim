/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

// Minimum release: 11
import java.lang.invoke.MethodHandles;

public class BytecodeValidation {
    static int bootstraps;
    public static Object bootstrap(MethodHandles.Lookup lookup, String name, Class<?> type) {
        ++bootstraps;
        return 0;
    }

    public static void main(String[] args) throws Exception {
        String[] controls = {"ValidInt", "ValidLong", "ValidType", "ValidHandle", "ValidDynamicInt", "ValidDynamicLong",
                "ValidTable", "ValidLookup", "ValidLocals", "ValidHandler", "ValidHandlerEnd", "ValidInterface", "ValidWideInterface", "ValidReferenceInterface", "ValidMultiFull", "ValidMultiPartial", "ValidArrayLimit", "ValidNewObject", "ValidStaticArguments", "ValidInstanceArguments", "ValidNativeMethod", "ValidAbstractMethod", "HandleStatic52", "HandleSpecial52"};
        for (String name : controls) {
            Class.forName("Verify" + name);
            System.out.println("valid " + name);
        }
        String[] invalid = {"LdcLong", "LdcWideDouble", "Ldc2Int", "Ldc2String", "LdcUtf", "LdcZero", "LdcOutside", "LdcReserved",
                "DynamicNarrowLong", "DynamicWideInt", "TruncatedLdc", "TruncatedWide", "IllegalWide", "ReservedOpcode",
                "BranchOperand", "BranchOutside", "TruncatedSwitch", "NegativePairs", "InvalidRange", "UnsortedKeys",
                "DynamicReserved", "WrongFieldRef", "WrongClassRef", "BadArrayType", "ZeroDimensions", "InterfaceReserved", "LocalOutside", "ImplicitLocalOutside", "WideLocalOutside", "WideLocalStride", "InterfaceCount", "WideInterfaceCount", "MultiExcess", "MultiScalar", "NewArrayClass", "ArrayDimensionLimit", "ModernJsr", "ModernJsrWide", "ModernRet", "ModernWideRet"};
        for (String name : invalid) {
            try { Class.forName("Verify" + name); throw new AssertionError(name); }
            catch (VerifyError expected) { System.out.println("verify " + name); }
        }
        for (String name : new String[] {"EmptyCode", "OversizeCode"}) {
            try { Class.forName("Verify" + name); throw new AssertionError(name); }
            catch (ClassFormatError expected) { System.out.println("format " + name); }
        }
        for (String name : new String[] {"HandlerEmpty", "HandlerReverse", "HandlerEndOutside", "HandlerTargetOutside",
                "HandlerCatchOutside", "HandlerCatchTag", "HandlerStartOperand", "HandlerEndOperand", "HandlerTargetOperand"}) {
            try { Class.forName("Verify" + name); throw new AssertionError(name); }
            catch (LinkageError expected) { System.out.println(name + " " + expected.getClass().getSimpleName()); }
        }
        for (String name : new String[] {"DescriptorEmpty", "DescriptorVoidParameter", "DescriptorVoidArray", "DescriptorUnknown",
                "DescriptorNoReturn", "DescriptorReturn", "DescriptorTrailing", "DescriptorDotted", "DescriptorDimensions",
                "DescriptorSlots", "DescriptorMethodType", "DescriptorReference", "ArgumentsStatic", "ArgumentsWide",
                "ArgumentsReceiver", "ArgumentsInstance", "MissingCode", "DuplicateCode", "NativeCode", "AbstractCode", "CodeLengthShort", "CodeLengthLong", "CodeLengthZero", "HandleNewMethod", "HandleStaticInit",
                "HandleVirtualInit", "HandleSpecialInit", "HandleStaticClinit", "HandleStatic51", "HandleSpecial51"}) {
            try { Class.forName("Verify" + name); throw new AssertionError(name); }
            catch (ClassFormatError expected) { System.out.println("descriptor " + name); }
        }
        System.out.println(bootstraps);
    }
}
