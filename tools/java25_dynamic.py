# Copyright (c) 2026 BasaltMC.
# Licensed under MIT; see LICENSE:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE
# SPDX-License-Identifier: MIT

"""Generate original CONSTANT_Dynamic bytecode fixtures."""

import struct


def generate(directory, major):
    entries, methods = [], []
    def u2(value):
        return struct.pack(">H", value)
    def add(tag, payload):
        entries.append(bytes([tag]) + payload)
        return len(entries)
    def utf(text):
        data = text.encode("utf-8")
        return add(1, u2(len(data)) + data)
    def class_(name):
        return add(7, u2(utf(name)))
    def method_ref(owner, name, descriptor):
        owner_index = class_(owner)
        signature = add(12, u2(utf(name)) + u2(utf(descriptor)))
        return add(10, u2(owner_index) + u2(signature))
    def method(name, constant, descriptor):
        wide = descriptor in ("J", "D")
        code = b"\x14" + u2(constant) if wide else (
            b"\x12" + bytes([constant]) if constant <= 255 else b"\x13" + u2(constant))
        wrappers = {"Z": "Boolean", "B": "Byte", "S": "Short", "C": "Character",
                    "I": "Integer", "J": "Long", "F": "Float", "D": "Double"}
        if descriptor in wrappers:
            wrapper = "java/lang/" + wrappers[descriptor]
            reference = method_ref(wrapper, "valueOf", "(" + descriptor + ")L" + wrapper + ";")
            code += b"\xb8" + u2(reference)
        code += b"\xb0"
        body = struct.pack(">HHI", 2 if wide else 1, 0, len(code)) + code + u2(0) + u2(0)
        methods.append(struct.pack(">HHHHHI", 0x0009, utf(name), utf("()Ljava/lang/Object;"),
                                   1, code_name, len(body)) + body)

    this, super_ = class_("DynamicConstantData"), class_("java/lang/Object")
    code_name, bootstrap_name = utf("Code"), utf("BootstrapMethods")
    bootstrap = add(15, b"\x06" + u2(method_ref("DynamicConstants", "bootstrap",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;")))
    cases = [("booleanValue", "Z"), ("byteValue", "B"), ("shortValue", "S"), ("charValue", "C"),
             ("intValue", "I"), ("longValue", "J"), ("floatValue", "F"), ("doubleValue", "D"),
             ("stringValue", "Ljava/lang/String;"), ("nullValue", "Ljava/lang/String;"), ("arrayValue", "[I"),
             ("lazyValue", "LDynamicConstants$Lazy;"), ("widenInt", "I"), ("widenFloat", "F"),
             ("wrongPrimitive", "I"), ("nullPrimitive", "I"), ("narrowByte", "B"),
             ("wrongReference", "Ljava/lang/String;"), ("failRuntime", "Ljava/lang/Object;"),
             ("failError", "Ljava/lang/Object;"), ("concurrentValue", "[I")]
    constants = {}
    for name, descriptor in cases:
        signature = add(12, u2(utf(name)) + u2(utf(descriptor)))
        constant = add(17, u2(0) + u2(signature))
        constants[name] = constant
        method(name, constant, descriptor)
    method("aliasInt", constants["intValue"], "I")
    nested_bootstrap = add(15, b"\x06" + u2(method_ref("DynamicConstants", "nested",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;I)Ljava/lang/Object;")))
    nested = add(17, u2(1) + u2(add(12, u2(utf("nestedValue")) + u2(utf("I")))))
    method("nestedValue", nested, "I")
    primitive_bootstraps = []
    for bootstrap_index, name, descriptor in ((2, "primitiveFloat", "F"), (3, "primitiveDouble", "D")):
        target = add(15, b"\x06" + u2(method_ref("DynamicConstants", name,
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)" + descriptor)))
        constant = add(17, u2(bootstrap_index) + u2(add(12, u2(utf(name)) + u2(utf(descriptor)))))
        method(name, constant, descriptor)
        primitive_bootstraps.append(u2(target) + u2(0))
    unused = add(17, u2(0) + u2(add(12, u2(utf("unusedValue")) + u2(utf("I")))))
    load = b"\x12" + bytes([unused]) if unused <= 255 else b"\x13" + u2(unused)
    box = method_ref("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;")
    selected = load + b"\xb8" + u2(box) + b"\xb0"
    target = 4 + len(selected)
    code = b"\x1a\x99" + u2(target - 1) + selected + b"\x01\xb0"
    stack_map = u2(1) + bytes([target])
    attribute = u2(utf("StackMapTable")) + struct.pack(">I", len(stack_map)) + stack_map
    body = struct.pack(">HHI", 1, 1, len(code)) + code + u2(0) + u2(1) + attribute
    methods.append(struct.pack(">HHHHHI", 0x0009, utf("unused"), utf("(Z)Ljava/lang/Object;"),
                               1, code_name, len(body)) + body)
    bootstrap_body = u2(4) + u2(bootstrap) + u2(0) + u2(nested_bootstrap) + u2(1) + u2(constants["intValue"])
    bootstrap_body += b"".join(primitive_bootstraps)
    data = struct.pack(">IHHH", 0xcafebabe, 0, major, len(entries) + 1) + b"".join(entries)
    data += struct.pack(">HHHHHH", 0x0021, this, super_, 0, 0, len(methods)) + b"".join(methods)
    data += u2(1) + u2(bootstrap_name) + struct.pack(">I", len(bootstrap_body)) + bootstrap_body
    (directory / "DynamicConstantData.class").write_bytes(data)
