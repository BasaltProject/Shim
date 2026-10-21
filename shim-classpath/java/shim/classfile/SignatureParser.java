/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.ClassSignature;
import java.lang.classfile.MethodSignature;
import java.lang.classfile.Signature;
import java.lang.classfile.Signature.TypeArg;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Parses type, class and method signatures. */
public final class SignatureParser {
    private static final class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;

        EndOfInput() {
            super(null, null, false, false);
        }
    }

    private final String sig;
    private int pos;

    private SignatureParser(String sig) {
        this.sig = TempEntries.nonNull(sig);
    }

    public static Signature parseType(String s) {
        SignatureParser p = new SignatureParser(s);
        Signature r;
        try {
            r = p.typeSig();
        } catch (EndOfInput e) {
            r = null;
        }
        if (r == null || p.pos != s.length()) {
            throw new IllegalArgumentException("Not a valid type signature: " + s);
        }
        return r;
    }

    public static ClassSignature parseClass(String s) {
        SignatureParser p = new SignatureParser(s);
        try {
            List<Signature.TypeParam> tps = p.typeParams();
            Signature.ClassTypeSig sup = p.superType();
            List<Signature.ClassTypeSig> ifs = new ArrayList<>();
            while (p.pos < s.length()) {
                ifs.add(p.superType());
            }
            return new SignaturesImpl.ClassSignatureImpl(tps, sup, Collections.unmodifiableList(ifs));
        } catch (EndOfInput e) {
            throw new IllegalArgumentException("Not a valid class signature: " + s);
        }
    }

    public static MethodSignature parseMethod(String s) {
        SignatureParser p = new SignatureParser(s);
        try {
            List<Signature.TypeParam> tps = p.typeParams();
            p.require('(');
            List<Signature> args = new ArrayList<>();
            while (p.ch() != ')') {
                args.add(p.typeSig());
            }
            p.pos++;
            Signature result = p.typeSig();
            List<Signature.ThrowableSig> throwables = new ArrayList<>();
            while (p.pos < s.length()) {
                p.require('^');
                Signature t = p.refTypeSig();
                if (!(t instanceof Signature.ThrowableSig)) {
                    throw new IllegalArgumentException("Not a valid throwable signature " + t.signatureString()
                        + " in: " + s);
                }
                throwables.add((Signature.ThrowableSig) t);
            }
            return new SignaturesImpl.MethodSignatureImpl(tps, Collections.unmodifiableList(throwables), result,
                Collections.unmodifiableList(args));
        } catch (EndOfInput e) {
            throw new IllegalArgumentException("Not a valid method signature: " + s);
        }
    }

    private char ch() {
        if (pos >= sig.length()) {
            throw new EndOfInput();
        }
        return sig.charAt(pos);
    }

    private IllegalArgumentException error(String expected) {
        if (pos >= sig.length()) {
            return new IllegalArgumentException("Unexpected end of signature at position " + pos + ", expected "
                + expected + ": " + sig);
        }
        return new IllegalArgumentException("Unexpected character " + sig.charAt(pos) + " at position " + pos
            + ", expected " + expected + ": " + sig);
    }

    private void require(char c) {
        if (pos >= sig.length() || sig.charAt(pos) != c) {
            throw error(String.valueOf(c));
        }
        pos++;
    }

    private boolean at(char c) {
        return pos < sig.length() && sig.charAt(pos) == c;
    }

    private String identifier() {
        int start = pos;
        while (pos < sig.length()) {
            char c = sig.charAt(pos);
            if (c == '.' || c == ';' || c == '[' || c == '/' || c == '<' || c == '>' || c == ':') {
                break;
            }
            pos++;
        }
        if (pos == start) {
            throw error("an identifier");
        }
        return sig.substring(start, pos);
    }

    private List<Signature.TypeParam> typeParams() {
        if (!at('<')) {
            return Collections.emptyList();
        }
        pos++;
        List<Signature.TypeParam> out = new ArrayList<>();
        do {
            String id = identifier();
            require(':');
            Optional<Signature.RefTypeSig> classBound = Optional.empty();
            if (ch() != ':') {
                classBound = Optional.of(refTypeSig());
            }
            List<Signature.RefTypeSig> ifs = new ArrayList<>();
            while (at(':')) {
                pos++;
                ifs.add(refTypeSig());
            }
            out.add(new SignaturesImpl.TypeParamImpl(id, classBound, Collections.unmodifiableList(ifs)));
        } while (!at('>'));
        pos++;
        return Collections.unmodifiableList(out);
    }

    private Signature.ClassTypeSig superType() {
        if (!at('L')) {
            throw error("L");
        }
        pos++;
        return classTypeSig();
    }

    private Signature typeSig() {
        char c = ch();
        switch (c) {
            case 'B': case 'C': case 'D': case 'F': case 'I': case 'J': case 'S': case 'Z': case 'V':
                pos++;
                return new SignaturesImpl.BaseTypeSigImpl(c);
            default:
                return refTypeSig();
        }
    }

    private Signature.RefTypeSig refTypeSig() {
        char c = ch();
        switch (c) {
            case 'L':
                pos++;
                return classTypeSig();
            case 'T': {
                pos++;
                String id = identifier();
                require(';');
                return new SignaturesImpl.TypeVarSigImpl(id);
            }
            case '[':
                pos++;
                return SignaturesImpl.ArrayTypeSigImpl.of(1, typeSig());
            default:
                throw error("a type signature");
        }
    }

    private Signature.ClassTypeSig classTypeSig() {
        int start = pos;
        identifier();
        while (at('/')) {
            pos++;
            identifier();
        }
        Signature.ClassTypeSig t = new SignaturesImpl.ClassTypeSigImpl(Optional.<Signature.ClassTypeSig>empty(),
            sig.substring(start, pos), typeArgs());
        while (true) {
            if (pos >= sig.length()) {
                throw error(". or ;");
            }
            char c = sig.charAt(pos);
            if (c == ';') {
                pos++;
                return t;
            }
            if (c != '.') {
                throw error(". or ;");
            }
            pos++;
            String name = identifier();
            t = new SignaturesImpl.ClassTypeSigImpl(Optional.of(t), name, typeArgs());
        }
    }

    private List<TypeArg> typeArgs() {
        if (!at('<')) {
            return Collections.emptyList();
        }
        pos++;
        List<TypeArg> out = new ArrayList<>();
        do {
            char c = ch();
            if (c == '*') {
                pos++;
                out.add(SignaturesImpl.UnboundedTypeArgImpl.INSTANCE);
            } else if (c == '+') {
                pos++;
                out.add(new SignaturesImpl.TypeArgImpl(TypeArg.Bounded.WildcardIndicator.EXTENDS, refTypeSig()));
            } else if (c == '-') {
                pos++;
                out.add(new SignaturesImpl.TypeArgImpl(TypeArg.Bounded.WildcardIndicator.SUPER, refTypeSig()));
            } else {
                out.add(new SignaturesImpl.TypeArgImpl(TypeArg.Bounded.WildcardIndicator.NONE, refTypeSig()));
            }
        } while (ch() != '>');
        pos++;
        return Collections.unmodifiableList(out);
    }
}
