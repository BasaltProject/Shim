# Copyright (c) 2026 BasaltMC.
# Licensed under MIT; see LICENSE:
# https://github.com/BasaltProject/Shim/blob/main/LICENSE
# SPDX-License-Identifier: MIT

"""Generate an original class-file fixture for loadable method constants."""

import struct


def generate(directory, major):
    entries = []

    def add(tag, data):
        entries.append(bytes([tag]) + data)
        return len(entries)

    def u2(value):
        return struct.pack(">H", value)

    def utf(text):
        data = text.encode("utf-8")
        return add(1, u2(len(data)) + data)

    def class_(name):
        return add(7, u2(utf(name)))

    def method_type(descriptor):
        return add(16, u2(utf(descriptor)))

    def handle(kind, owner, name, descriptor):
        owner_index = class_(owner)
        signature = add(12, u2(utf(name)) + u2(utf(descriptor)))
        reference = add(10, u2(owner_index) + u2(signature))
        return add(15, bytes([kind]) + u2(reference))

    this = class_("LoadableConstantData")
    super_ = class_("java/lang/Object")
    code_name = utf("Code")
    methods = []

    def method(name, result, constant, wide=False):
        name_index = utf(name)
        descriptor = utf("()Ljava/lang/invoke/" + result + ";")
        code = (b"\x13" + u2(constant) if wide else b"\x12" + bytes([constant])) + b"\xb0"
        body = struct.pack(">HHI", 1, 0, len(code)) + code + u2(0) + u2(0)
        methods.append(struct.pack(">HHHHHI", 0x0009, name_index, descriptor, 1,
                                   code_name, len(body)) + body)

    lazy = "LoadableConstants$Lazy"
    type_index = method_type("(I[Ljava/lang/String;)J")
    method("type", "MethodType", type_index)
    method("wideType", "MethodType", type_index, True)
    method("lazyType", "MethodType", method_type("(L" + lazy + ";)L" + lazy + ";"))
    method("staticHandle", "MethodHandle", handle(6, "java/lang/String", "valueOf", "(I)Ljava/lang/String;"))
    method("virtualHandle", "MethodHandle", handle(5, "java/lang/String", "length", "()I"))
    method("constructorHandle", "MethodHandle", handle(8, "java/lang/String", "<init>", "()V"))
    method("lazyHandle", "MethodHandle", handle(6, lazy, "target", "()I"), True)
    bootstrap_name = utf("BootstrapMethods")
    bootstrap = handle(6, "java/lang/invoke/StringConcatFactory", "makeConcatWithConstants",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;"
        "Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;")
    recipe = add(8, u2(utf("type=\u0002")))
    concat_name = utf("concat")
    concat_descriptor = utf("()Ljava/lang/String;")
    concat_signature = add(12, u2(concat_name) + u2(concat_descriptor))
    concat = add(18, u2(0) + u2(concat_signature))
    code = b"\xba" + u2(concat) + b"\x00\x00\xb0"
    body = struct.pack(">HHI", 1, 0, len(code)) + code + u2(0) + u2(0)
    methods.append(struct.pack(">HHHHHI", 0x0009, concat_name, concat_descriptor, 1,
                               code_name, len(body)) + body)
    bootstrap_body = u2(1) + u2(bootstrap) + u2(2) + u2(recipe) + u2(type_index)
    bootstrap_attribute = u2(bootstrap_name) + struct.pack(">I", len(bootstrap_body)) + bootstrap_body
    data = struct.pack(">IHHH", 0xcafebabe, 0, major, len(entries) + 1)
    data += b"".join(entries)
    data += struct.pack(">HHHHHH", 0x0021, this, super_, 0, 0, len(methods))
    data += b"".join(methods) + u2(1) + bootstrap_attribute
    (directory / "LoadableConstantData.class").write_bytes(data)
