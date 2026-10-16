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
#include "shim/util.h"

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
    case CONSTANT_Dynamic:
        return resolveDynamicConstant(t, cast<GcDynamicConstant>(t, singletonObject(t, pool, index)), invocation->class_());
    case CONSTANT_Class:
    case CONSTANT_MethodType:
    case CONSTANT_MethodHandle:
        return resolveLoadableConstant(t, invocation->class_()->loader(), pool, index);
    default:
        throwNew(t, GcBootstrapMethodError::Type, "Unsupported bootstrap constant tag %u", tags->body()[index]);
    }
}

} // namespace

GcInvocation* dynamicInvocation(Thread* t, GcMethod* method, GcCode* code,
                                unsigned poolIndex, unsigned offset)
{
    PROTECT(t, method);
    PROTECT(t, code);
    ACQUIRE(t, t->m->classLock);
    GcList* sites = code->dynamicSites();
    if (!sites) {
        sites = makeList(t, 0, 0, 0);
        code->setDynamicSites(t, sites);
    }
    PROTECT(t, sites);
    for (GcPair* node = cast<GcPair>(t, sites->front()); node;
         node = cast<GcPair>(t, node->second())) {
        GcPair* entry = cast<GcPair>(t, node->first());
        if (cast<GcInt>(t, entry->first())->value() == static_cast<int>(offset)) {
            return cast<GcInvocation>(t, entry->second());
        }
    }
    GcInvocation* prototype = cast<GcInvocation>(t, singletonObject(t, code->pool(), poolIndex));
    PROTECT(t, prototype);
    GcInvocation* invocation = makeInvocation(t, prototype->bootstrap(), -1,
        method->class_(), prototype->pool(), prototype->poolTags(), prototype->template_(),
        prototype->bootstrapMethodTable(), 0, 0);
    PROTECT(t, invocation);
    GcInt* position = makeInt(t, offset);
    PROTECT(t, position);
    GcPair* entry = makePair(t, position, invocation);
    listAppend(t, sites, entry);
    return invocation;
}

object resolveLoadableConstant(Thread* t, GcClassLoader* loader, GcSingleton* pool, unsigned index)
{
    PROTECT(t, pool);
    PROTECT(t, loader);
    object value = singletonObject(t, pool, index);
    loadMemoryBarrier();
    PROTECT(t, value);
    if (objectClass(t, value) == type(t, GcReference::Type)) {
        unsigned kind = cast<GcReference>(t, value)->kind();
        if (kind != 0) {
            if (kind >= REF_getField and kind <= REF_putStatic) {
                GcReference* ref = cast<GcReference>(t, value);
                PROTECT(t, ref);
                GcClass* class_ = resolveClassInObject(t, loader, ref, ReferenceClass);
                PROTECT(t, class_);
                GcField* field = cast<GcField>(t, findInHierarchy(t, class_, ref->name(), ref->spec(), findFieldInClass, GcNoSuchFieldError::Type));
                PROTECT(t, field);
                GcMethod* makeFieldHandle = resolveMethod(t, roots(t)->bootLoader(),
                    "java/lang/invoke/MethodHandle", "makeFieldHandle", "(ILshim/VMField;)Ljava/lang/invoke/MethodHandle;");
                GcArray* args = makeArray(t, 2);
                PROTECT(t, args);
                args->setBodyElement(t, 0, makeInt(t, kind));
                args->setBodyElement(t, 1, field);
                object handle = t->m->processor->invokeArray(t, makeFieldHandle, 0, args);
                storeStoreMemoryBarrier();
                pool->setBodyElement(t, index, reinterpret_cast<uintptr_t>(handle));
                return handle;
            }
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

static object invokeBootstrap(Thread* t, GcInvocation* invocation, object typeArgument)
{
    PROTECT(t, invocation);
    PROTECT(t, typeArgument);
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
    arguments->setBodyElement(t, 2, typeArgument);
    for (unsigned i = 3; i < fixed; ++i) {
        object value = bootstrapConstant(t, invocation, constants->body()[i - 2]);
        arguments->setBodyElement(t, i, value);
    }
    if (varargs) {
        object trailing = makeObjectArray(t, staticArguments - (fixed - 3));
        PROTECT(t, trailing);
        unsigned trailingLength = objectArrayLength(t, trailing);
        for (unsigned i = 0; i < trailingLength; ++i) {
            object value = bootstrapConstant(t, invocation, constants->body()[fixed - 2 + i]);
            reinterpret_cast<GcArray*>(trailing)->setBodyElement(t, i, value);
        }
        arguments->setBodyElement(t, parameters - 1, trailing);
    }
    initClass(t, bootstrap->class_());
    object result = t->m->processor->invokeArray(t, bootstrap, 0, arguments);
    if (result) {
        switch (bootstrap->returnCode()) {
        case BooleanField: return makeBoolean(t, cast<GcInt>(t, result)->value());
        case ByteField: return makeByte(t, cast<GcInt>(t, result)->value());
        case CharField: return makeChar(t, cast<GcInt>(t, result)->value());
        case ShortField: return makeShort(t, cast<GcInt>(t, result)->value());
        case FloatField: return makeFloat(t, cast<GcInt>(t, result)->value());
        case DoubleField: return makeDouble(t, cast<GcLong>(t, result)->value());
        default: break;
        }
    }
    return result;
}

static GcCallSite* linkDynamic(Thread* t, GcInvocation* invocation)
{
    PROTECT(t, invocation);
    GcMethodType* methodType = makeMethodType(t, invocation->class_()->loader(), invocation->template_()->spec(), 0, 0, 0);
    PROTECT(t, methodType);
    object result = invokeBootstrap(t, invocation, methodType);
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
    comparison->setBodyElement(t, 0, methodType);
    object matches = t->m->processor->invokeArray(t, equalsMethod, targetType, comparison);
    if (!cast<GcInt>(t, matches)->value()) {
        throwNew(t, GcBootstrapMethodError::Type, "CallSite target type does not match invocation type");
    }
    return site;
}

namespace {

object convertConstant(Thread* t, GcDynamicConstant* constant, object value, GcClass* expected)
{
    PROTECT(t, value);
    unsigned code = constant->template_()->returnCode();
    if (code == ObjectField) {
        if (value && !instanceOf(t, expected, value)) {
            throwNew(t, GcClassCastException::Type, "Dynamic constant has incompatible reference type");
        }
        return value;
    }
    if (!value) throwNew(t, GcNullPointerException::Type, "Null dynamic primitive constant");
    GcClass* actual = objectClass(t, value);
    if (code == BooleanField && actual == type(t, GcBoolean::Type)) return value;
    if (code == CharField && actual == type(t, GcChar::Type)) return value;
    int rank = 0;
    int64_t integral = 0;
    double real = 0;
    if (actual == type(t, GcByte::Type)) {
        rank = 1; integral = static_cast<int8_t>(cast<GcByte>(t, value)->value());
    } else if (actual == type(t, GcShort::Type)) {
        rank = 2; integral = static_cast<int16_t>(cast<GcShort>(t, value)->value());
    } else if (actual == type(t, GcChar::Type)) {
        rank = 2; integral = cast<GcChar>(t, value)->value();
    } else if (actual == type(t, GcInt::Type)) {
        rank = 3; integral = cast<GcInt>(t, value)->value();
    } else if (actual == type(t, GcLong::Type)) {
        rank = 4; integral = static_cast<int64_t>(cast<GcLong>(t, value)->value());
    } else if (actual == type(t, GcFloat::Type)) {
        rank = 5; real = bitsToFloat(cast<GcFloat>(t, value)->value());
    } else if (actual == type(t, GcDouble::Type)) {
        rank = 6; real = bitsToDouble(cast<GcDouble>(t, value)->value());
    }
    if (rank) {
        switch (code) {
        case ByteField:
            if (rank == 1) return value;
            break;
        case ShortField:
            if (rank <= 2 && actual != type(t, GcChar::Type)) return makeShort(t, integral);
            break;
        case IntField:
            if (rank <= 3) return makeInt(t, integral);
            break;
        case LongField:
            if (rank <= 4) return makeLong(t, integral);
            break;
        case FloatField:
            if (rank <= 5) return makeFloat(t, floatToBits(rank <= 4 ? static_cast<float>(integral) : static_cast<float>(real)));
            break;
        case DoubleField:
            return makeDouble(t, doubleToBits(rank <= 4 ? static_cast<double>(integral) : real));
        default: break;
        }
    }
    throwNew(t, GcClassCastException::Type, "Dynamic constant has incompatible primitive type");
}

uint64_t runConstantBootstrap(Thread* t, uintptr_t* arguments)
{
    GcDynamicConstant* constant = reinterpret_cast<GcDynamicConstant*>(arguments[0]);
    PROTECT(t, constant);
    GcMethodType* methodType = makeMethodType(t, constant->class_()->loader(), constant->template_()->spec(), 0, 0, 0);
    PROTECT(t, methodType);
    GcMethod* returnType = resolveMethod(t, roots(t)->bootLoader(),
        "java/lang/invoke/MethodType", "returnType", "()Ljava/lang/Class;");
    GcJclass* expected = cast<GcJclass>(t, t->m->processor->invokeArray(t, returnType, methodType, static_cast<GcArray*>(0)));
    PROTECT(t, expected);
    object value = invokeBootstrap(t, cast<GcInvocation>(t, constant), expected);
    return reinterpret_cast<uintptr_t>(convertConstant(t, constant, value, expected->vmClass()));
}

} // namespace

object resolveDynamicConstant(Thread* t, GcDynamicConstant* constant, GcClass* owner)
{
    PROTECT(t, constant);
    PROTECT(t, owner);
    {
        ACQUIRE(t, t->m->classLock);
        if (constant->resolved()) return constant->value();
        if (constant->failure()) throw_(t, constant->failure());
        constant->setClass(t, owner);
    }
    uintptr_t arguments[] = {reinterpret_cast<uintptr_t>(constant)};
    object value = reinterpret_cast<object>(runRaw(t, runConstantBootstrap, arguments));
    PROTECT(t, value);
    GcThrowable* failure = t->exception;
    t->exception = 0;
    PROTECT(t, failure);
    if (failure && !instanceOf(t, type(t, GcError::Type), failure)) {
        failure = makeThrowable(t, GcBootstrapMethodError::Type, 0, 0, failure);
    }
    ACQUIRE(t, t->m->classLock);
    if (constant->resolved()) return constant->value();
    if (constant->failure()) throw_(t, constant->failure());
    if (failure) {
        constant->setFailure(t, failure);
        throw_(t, failure);
    }
    constant->setValue(t, value);
    constant->resolved() = 1;
    return value;
}

uint64_t dynamicConstantBits(Thread* t, GcDynamicConstant* constant, object value)
{
    switch (constant->template_()->returnCode()) {
    case BooleanField: return cast<GcBoolean>(t, value)->value();
    case ByteField: return static_cast<int32_t>(static_cast<int8_t>(cast<GcByte>(t, value)->value()));
    case CharField: return cast<GcChar>(t, value)->value();
    case ShortField: return static_cast<int32_t>(static_cast<int16_t>(cast<GcShort>(t, value)->value()));
    case IntField: return cast<GcInt>(t, value)->value();
    case FloatField: return cast<GcFloat>(t, value)->value();
    case LongField: return cast<GcLong>(t, value)->value();
    case DoubleField: return cast<GcDouble>(t, value)->value();
    default: abort(t);
    }
}

namespace {

uint64_t runLinkDynamic(Thread* t, uintptr_t* arguments)
{
    return reinterpret_cast<uintptr_t>(linkDynamic(t,
        reinterpret_cast<GcInvocation*>(arguments[0])));
}

} // namespace

GcCallSite* resolveDynamic(Thread* t, GcInvocation* invocation)
{
    PROTECT(t, invocation);
    {
        ACQUIRE(t, t->m->classLock);
        if (invocation->site()) return invocation->site();
        if (invocation->failure()) throw_(t, invocation->failure());
    }

    uintptr_t arguments[] = {reinterpret_cast<uintptr_t>(invocation)};
    GcCallSite* site = reinterpret_cast<GcCallSite*>(runRaw(t, runLinkDynamic, arguments));
    PROTECT(t, site);
    GcThrowable* failure = t->exception;
    t->exception = 0;
    PROTECT(t, failure);
    if (failure && !instanceOf(t, type(t, GcError::Type), failure)) {
        failure = makeThrowable(t, GcBootstrapMethodError::Type, 0, 0, failure);
    }

    // Bootstrap execution may race, but only one result is installed. Do not
    // hold the class lock while running arbitrary Java bootstrap code.
    ACQUIRE(t, t->m->classLock);
    if (invocation->site()) return invocation->site();
    if (invocation->failure()) throw_(t, invocation->failure());
    if (failure) {
        invocation->setFailure(t, failure);
        storeStoreMemoryBarrier();
        throw_(t, failure);
    }
    site->setInvocation(t, invocation);
    invocation->setSite(t, site);
    storeStoreMemoryBarrier();
    return site;
}

} // namespace vm
