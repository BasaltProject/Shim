# Copyright (c) 2026 BasaltMC.
# Licensed under MIT; see LICENSE:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE
# SPDX-License-Identifier: MIT

"""Generate original bytecode verification controls and malformed classes."""

import struct


def generate(directory, major):
    def build(name, case):
        entries = []
        def u2(value): return struct.pack(">H", value)
        def i4(value): return struct.pack(">i", value)
        def add(tag, data):
            entries.append(bytes([tag]) + data)
            index = len(entries)
            if tag in (5, 6): entries.append(None)
            return index
        def utf(text):
            data = text.encode("utf-8")
            return add(1, u2(len(data)) + data)
        def class_(name): return add(7, u2(utf(name)))
        def ref(owner, method, descriptor, tag=10):
            owner_index = class_(owner)
            signature = add(12, u2(utf(method)) + u2(utf(descriptor)))
            return add(tag, u2(owner_index) + u2(signature))
        this, super_ = class_(name), class_("java/lang/Object")
        code_name, method_name, method_descriptor = utf("Code"), utf("test"), utf("()V")
        integer = add(3, i4(7))
        long_ = add(5, struct.pack(">q", 42))
        double = add(6, struct.pack(">d", 3.5))
        raw = utf("text")
        string = add(8, u2(raw))
        method_type = add(16, u2(method_descriptor))
        bootstrap_ref = ref("BytecodeValidation", "bootstrap",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;")
        handle = add(15, b"\x06" + u2(bootstrap_ref))
        dynamic_int = add(17, u2(0) + u2(add(12, u2(utf("intValue")) + u2(utf("I")))))
        dynamic_long = add(17, u2(0) + u2(add(12, u2(utf("longValue")) + u2(utf("J")))))
        invocation = add(18, u2(0) + u2(add(12, u2(utf("call")) + u2(method_descriptor))))
        interface = ref("java/lang/Runnable", "run", "()V", 11)
        array = class_("[[I")
        wide_interface = ref("java/util/function/LongUnaryOperator", "applyAsLong", "(J)J", 11)
        array_interface = ref("java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V", 11)
        table = b"\x03\xaa\x00\x00" + i4(23) + i4(0) + i4(1) + i4(23) + i4(23) + b"\xb1"
        lookup = b"\x03\xab\x00\x00" + i4(27) + i4(2) + i4(1) + i4(27) + i4(2) + i4(27) + b"\xb1"
        variants = {
            "ValidInt": b"\x12" + bytes([integer]) + b"\x57\xb1",
            "ValidLong": b"\x14" + u2(long_) + b"\x58\xb1",
            "ValidType": b"\x12" + bytes([method_type]) + b"\x57\xb1",
            "ValidHandle": b"\x12" + bytes([handle]) + b"\x57\xb1",
            "ValidDynamicInt": b"\x12" + bytes([dynamic_int]) + b"\x57\xb1",
            "ValidDynamicLong": b"\x14" + u2(dynamic_long) + b"\x58\xb1",
            "ValidTable": table,
            "ValidLookup": lookup,
            "ValidInterface": b"\x01\xb9" + u2(interface) + b"\x01\x00\xb1",
            "ValidWideInterface": b"\x01\x09\xb9" + u2(wide_interface) + b"\x03\x00\x58\xb1",
            "ValidReferenceInterface": b"\x01\x01\xb9" + u2(array_interface) + b"\x02\x00\xb1",
            "InterfaceCount": b"\x01\xb9" + u2(interface) + b"\x02\x00\xb1",
            "WideInterfaceCount": b"\x01\x09\xb9" + u2(wide_interface) + b"\x02\x00\x58\xb1",
            "ValidLocals": b"\x09\x3f\x1e\x58\xb1",
            "LocalOutside": b"\x15\x00\x57\xb1",
            "ImplicitLocalOutside": b"\x2a\x57\xb1",
            "WideLocalOutside": b"\xc4\x15\xff\xff\x57\xb1",
            "WideLocalStride": b"\x16\x00\x58\xb1",
            "LdcLong": b"\x12" + bytes([long_]) + b"\x58\xb1",
            "LdcWideDouble": b"\x13" + u2(double) + b"\x58\xb1",
            "Ldc2Int": b"\x14" + u2(integer) + b"\x58\xb1",
            "Ldc2String": b"\x14" + u2(string) + b"\x58\xb1",
            "LdcUtf": b"\x12" + bytes([raw]) + b"\x57\xb1",
            "LdcZero": b"\x12\x00\x57\xb1",
            "LdcOutside": b"\x13\xff\xff\x57\xb1",
            "LdcReserved": b"\x12" + bytes([long_ + 1]) + b"\x57\xb1",
            "DynamicNarrowLong": b"\x12" + bytes([dynamic_long]) + b"\x58\xb1",
            "DynamicWideInt": b"\x14" + u2(dynamic_int) + b"\x58\xb1",
            "TruncatedLdc": b"\x12",
            "TruncatedWide": b"\xc4\x15",
            "IllegalWide": b"\xc4\x00\x00\x00\xb1",
            "ReservedOpcode": b"\xca\xb1",
            "BranchOperand": b"\xa7\x00\x01\xb1",
            "BranchOutside": b"\xa7\x7f\xff\xb1",
            "TruncatedSwitch": b"\xaa\x00\x00\x00",
            "NegativePairs": b"\xab\x00\x00\x00" + i4(0) + i4(-1) + b"\xb1",
            "InvalidRange": b"\xaa\x00\x00\x00" + i4(0) + i4(2) + i4(1) + b"\xb1",
            "UnsortedKeys": lookup[:12] + i4(2) + lookup[16:20] + i4(1) + lookup[24:],
            "DynamicReserved": b"\xba" + u2(invocation) + b"\x01\x00\xb1",
            "WrongFieldRef": b"\xb2" + u2(bootstrap_ref) + b"\x57\xb1",
            "WrongClassRef": b"\xbb" + u2(string) + b"\x57\xb1",
            "BadArrayType": b"\x03\xbc\x01\x57\xb1",
            "ZeroDimensions": b"\xc5" + u2(array) + b"\x00\x57\xb1",
            "InterfaceReserved": b"\x01\xb9" + u2(interface) + b"\x01\x01\xb1",
            "EmptyCode": b"",
            "OversizeCode": b"\x00" * 65535 + b"\xb1",
        }
        handler_cases = {
            "ValidHandler": (0, 2, 2, 0),
            "ValidHandlerEnd": (0, 4, 2, 0),
            "HandlerEmpty": (0, 0, 2, 0),
            "HandlerReverse": (2, 1, 2, 0),
            "HandlerEndOutside": (0, 5, 2, 0),
            "HandlerTargetOutside": (0, 2, 4, 0),
            "HandlerCatchOutside": (0, 2, 2, 65535),
            "HandlerCatchTag": (0, 2, 2, raw),
            "HandlerStartOperand": (1, 2, 2, 0),
            "HandlerEndOperand": (0, 1, 2, 0),
            "HandlerTargetOperand": (0, 2, 1, 0),
        }
        handlers = b""
        if case in handler_cases:
            code = b"\x01\xbf\x57\xb1"
            if case in ("HandlerStartOperand", "HandlerEndOperand", "HandlerTargetOperand"):
                code = b"\x10\x00\x57\xb1"
            handlers = struct.pack(">HHHH", *handler_cases[case])
        else:
            code = variants[case]
        attributes = b""
        if case in ("ValidTable", "ValidLookup", "UnsortedKeys"):
            position = 24 if case == "ValidTable" else 28
            payload = u2(1) + bytes([position])
            attributes = u2(utf("StackMapTable")) + struct.pack(">I", len(payload)) + payload
        if handlers:
            payload = u2(1) + b"\x42\x07" + u2(class_("java/lang/Throwable"))
            attributes = u2(utf("StackMapTable")) + struct.pack(">I", len(payload)) + payload
        locals_ = 2 if case == "ValidLocals" else (1 if case == "WideLocalStride" else 0)
        body = struct.pack(">HHI", 3 if case in ("ValidWideInterface", "WideInterfaceCount") else 2, locals_, len(code)) + code + u2(bool(handlers)) + handlers + u2(bool(attributes)) + attributes
        method = struct.pack(">HHHHHI", 0x0009, method_name, method_descriptor, 1, code_name, len(body)) + body
        bootstrap_name = utf("BootstrapMethods")
        bootstrap_body = u2(1) + u2(handle) + u2(0)
        data = struct.pack(">IHHH", 0xcafebabe, 0, major, len(entries) + 1)
        data += b"".join(entry for entry in entries if entry is not None)
        data += struct.pack(">HHHHHH", 0x0021, this, super_, 0, 0, 1) + method
        data += u2(1) + u2(bootstrap_name) + struct.pack(">I", len(bootstrap_body)) + bootstrap_body
        (directory / (name + ".class")).write_bytes(data)

    controls = ("ValidInt", "ValidLong", "ValidType", "ValidHandle", "ValidDynamicInt", "ValidDynamicLong", "ValidTable", "ValidLookup", "ValidLocals", "ValidInterface", "ValidWideInterface", "ValidReferenceInterface")
    invalid = ("LdcLong", "LdcWideDouble", "Ldc2Int", "Ldc2String", "LdcUtf", "LdcZero", "LdcOutside", "LdcReserved",
               "DynamicNarrowLong", "DynamicWideInt", "TruncatedLdc", "TruncatedWide", "IllegalWide", "ReservedOpcode",
               "BranchOperand", "BranchOutside", "TruncatedSwitch", "NegativePairs", "InvalidRange", "UnsortedKeys",
               "DynamicReserved", "WrongFieldRef", "WrongClassRef", "BadArrayType", "ZeroDimensions", "InterfaceReserved",
               "LocalOutside", "ImplicitLocalOutside", "WideLocalOutside", "WideLocalStride", "InterfaceCount", "WideInterfaceCount", "EmptyCode", "OversizeCode")
    handlers = ("ValidHandler", "ValidHandlerEnd", "HandlerEmpty", "HandlerReverse", "HandlerEndOutside",
                "HandlerTargetOutside", "HandlerCatchOutside", "HandlerCatchTag", "HandlerStartOperand",
                "HandlerEndOperand", "HandlerTargetOperand")
    for case in controls + invalid + handlers:
        build("Verify" + case, case)
