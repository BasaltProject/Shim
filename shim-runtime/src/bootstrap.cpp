/*
 * Based on Avian (https://github.com/ReadyTalk/avian).
 * Original work Copyright (c) 2008-2015, Avian Contributors.
 * Original work licensed under ISC; see LICENSE.txt:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE.txt
 *
 * Modifications Copyright (c) 2026 BasaltMC.
 * Modifications licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: ISC AND MIT
 */

/* Copyright (c) 2008-2015, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "shim/machine.h"
#include "shim/constants.h"
#include "shim/processor.h"

namespace vm {
namespace {

object bootstrapConstant(Thread* t, GcInvocation* invocation, unsigned index)
{
    PROTECT(t, invocation);
    GcSingleton* pool = invocation->pool();
    PROTECT(t, pool);
    GcByteArray* tags = invocation->poolTags();
    PROTECT(t, tags);
    if (index >= tags->length() / 2) {
        throwNew(t, GcClassFormatError::Type, "Invalid bootstrap argument index");
    }
    switch (tags->body()[index]) {
    case CONSTANT_Integer:
        return makeInt(t, singletonValue(t, pool, index));
    case CONSTANT_Float:
        return makeFloat(t, singletonValue(t, pool, index));
    case CONSTANT_Long:
    case CONSTANT_Double: {
        uint64_t bits;
        memcpy(&bits, &singletonValue(t, pool, index), sizeof(bits));
        return tags->body()[index] == CONSTANT_Long
            ? reinterpret_cast<object>(makeLong(t, bits))
            : reinterpret_cast<object>(makeDouble(t, bits));
    }
    case CONSTANT_String:
        return singletonObject(t, pool, index);
    case CONSTANT_Class:
    case CONSTANT_MethodType:
    case CONSTANT_MethodHandle:
        return resolveLoadableConstant(t, invocation->class_()->loader(), pool, index);
    default:
        throwNew(t, GcBootstrapMethodError::Type, "Unsupported bootstrap constant tag %u", tags->body()[index]);
    }
}

} // namespace

object resolveLoadableConstant(Thread* t, GcClassLoader* loader, GcSingleton* pool, unsigned index)
{
    PROTECT(t, pool);
    PROTECT(t, loader);
    object value = singletonObject(t, pool, index);
    loadMemoryBarrier();
    PROTECT(t, value);
    if (objectClass(t, value) == type(t, GcReference::Type)) {
        if (cast<GcReference>(t, value)->kind() != 0) {
            return resolve(t, loader, pool, index, findMethodInClass, GcNoSuchMethodError::Type);
        }
        GcClass* class_ = resolveClass(t, loader, cast<GcReference>(t, value)->name());
        storeStoreMemoryBarrier();
        pool->setBodyElement(t, index, reinterpret_cast<uintptr_t>(class_));
        return getJClass(t, class_);
    }
    if (objectClass(t, value) == type(t, GcClass::Type)) {
        return getJClass(t, cast<GcClass>(t, value));
    }
    if (objectClass(t, value) == type(t, GcByteArray::Type)) {
        value = makeMethodType(t, loader, cast<GcByteArray>(t, value), 0, 0, 0);
        GcMethod* validate = resolveMethod(t, roots(t)->bootLoader(),
            "java/lang/invoke/MethodType", "parameterCount", "()I");
        t->m->processor->invokeArray(t, validate, value, static_cast<GcArray*>(0));
        storeStoreMemoryBarrier();
        pool->setBodyElement(t, index, reinterpret_cast<uintptr_t>(value));
    }
    return value;
}

GcCallSite* resolveDynamic(Thread* t, GcInvocation* invocation)
{
    PROTECT(t, invocation);
    GcClass* caller = invocation->class_();
    PROTECT(t, caller);
    GcArray* table = cast<GcArray>(t, invocation->bootstrapMethodTable());
    if (table == 0 or invocation->bootstrap() >= table->length()) {
        throwNew(t, GcBootstrapMethodError::Type, "Invalid bootstrap method index");
    }
    GcCharArray* constants = cast<GcCharArray>(t, table->body()[invocation->bootstrap()]);
    PROTECT(t, constants);
    GcMethodHandle* bootstrapHandle = cast<GcMethodHandle>(t,
        resolve(t, caller->loader(), invocation->pool(), constants->body()[0],
                findMethodInClass, GcNoSuchMethodError::Type));
    PROTECT(t, bootstrapHandle);
    GcMethod* bootstrap = bootstrapHandle->method();
    PROTECT(t, bootstrap);
    if ((bootstrap->flags() & ACC_STATIC) == 0) {
        throwNew(t, GcBootstrapMethodError::Type, "Bootstrap method must be static");
    }
    unsigned parameters = bootstrap->parameterCount();
    unsigned staticArguments = constants->length() - 1;
    bool varargs = (bootstrap->flags() & ACC_VARARGS) != 0;
    unsigned fixed = varargs ? parameters - 1 : parameters;
    if (fixed < 3 or staticArguments < fixed - 3 or (!varargs and staticArguments != fixed - 3)) {
        throwNew(t, GcBootstrapMethodError::Type, "Invalid bootstrap argument count");
    }
    GcArray* arguments = makeArray(t, parameters);
    PROTECT(t, arguments);
    GcLookup* lookup = makeLookup(t, caller, 0x5f);
    arguments->setBodyElement(t, 0, lookup);
    GcByteArray* name = invocation->template_()->name();
    GcString* string = t->m->classpath->makeString(t, name, 0, name->length() - 1);
    arguments->setBodyElement(t, 1, string);
    GcMethodType* methodType = makeMethodType(t, caller->loader(), invocation->template_()->spec(), 0, 0, 0);
    arguments->setBodyElement(t, 2, methodType);
    for (unsigned i = 3; i < fixed; ++i) {
        object value = bootstrapConstant(t, invocation, constants->body()[i - 2]);
        arguments->setBodyElement(t, i, value);
    }
    if (varargs) {
        GcArray* trailing = makeArray(t, staticArguments - (fixed - 3));
        PROTECT(t, trailing);
        for (unsigned i = 0; i < trailing->length(); ++i) {
            object value = bootstrapConstant(t, invocation, constants->body()[fixed - 2 + i]);
            trailing->setBodyElement(t, i, value);
        }
        arguments->setBodyElement(t, parameters - 1, trailing);
    }
    initClass(t, bootstrap->class_());
    object result = t->m->processor->invokeArray(t, bootstrap, 0, arguments);
    if (result == 0 or !instanceOf(t, type(t, GcCallSite::Type), result)) {
        throwNew(t, GcBootstrapMethodError::Type, "Bootstrap method did not return a CallSite");
    }
    GcCallSite* site = cast<GcCallSite>(t, result);
    PROTECT(t, site);
    if (site->target() == 0 or site->target()->method() == 0) {
        throwNew(t, GcBootstrapMethodError::Type, "CallSite has no target");
    }
    GcMethod* typeMethod = resolveMethod(t, roots(t)->bootLoader(),
        "java/lang/invoke/MethodHandle", "type", "()Ljava/lang/invoke/MethodType;");
    object targetType = t->m->processor->invokeArray(t, typeMethod, site->target(), static_cast<GcArray*>(0));
    PROTECT(t, targetType);
    GcMethod* equalsMethod = resolveMethod(t, roots(t)->bootLoader(),
        "java/lang/invoke/MethodType", "equals", "(Ljava/lang/Object;)Z");
    PROTECT(t, equalsMethod);
    GcArray* comparison = makeArray(t, 1);
    PROTECT(t, comparison);
    comparison->setBodyElement(t, 0, arguments->body()[2]);
    object matches = t->m->processor->invokeArray(t, equalsMethod, targetType, comparison);
    if (!cast<GcInt>(t, matches)->value()) {
        throwNew(t, GcBootstrapMethodError::Type, "CallSite target type does not match invocation type");
    }
    return site;
}

} // namespace vm
