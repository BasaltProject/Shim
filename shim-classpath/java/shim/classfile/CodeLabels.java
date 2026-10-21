/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

/** Creates the labels referenced by stack map frames and code type annotations of a parsed code attribute. */
final class CodeLabels {
    private CodeLabels() { }

    static void inflateFrames(CodeImpl code, int body) {
        ClassReaderImpl r = code.reader;
        int n = r.readU2(body);
        int p = body + 2;
        int bci = -1;
        for (int i = 0; i < n; i++) {
            int type = r.readU1(p++);
            int delta;
            if (type < 64) {
                delta = type;
            } else if (type < 128) {
                delta = type - 64;
                p = verificationType(code, p);
            } else if (type == 247) {
                delta = r.readU2(p);
                p = verificationType(code, p + 2);
            } else if (type >= 248 && type <= 251) {
                delta = r.readU2(p);
                p += 2;
            } else if (type >= 252 && type <= 254) {
                delta = r.readU2(p);
                p += 2;
                for (int k = 0; k < type - 251; k++) {
                    p = verificationType(code, p);
                }
            } else if (type == 255) {
                delta = r.readU2(p);
                p += 2;
                for (int part = 0; part < 2; part++) {
                    int k = r.readU2(p);
                    p += 2;
                    for (int j = 0; j < k; j++) {
                        p = verificationType(code, p);
                    }
                }
            } else {
                throw new IllegalArgumentException("Bad frame type: " + type);
            }
            bci = bci + delta + 1;
            code.getLabel(bci);
        }
    }

    private static int verificationType(CodeImpl code, int p) {
        int tag = code.reader.readU1(p);
        if (tag == 7) {
            return p + 3;
        }
        if (tag == 8) {
            code.getLabel(code.reader.readU2(p + 1));
            return p + 3;
        }
        return p + 1;
    }

    static void inflateTypeAnnotations(CodeImpl code, int body) {
        ClassReaderImpl r = code.reader;
        int n = r.readU2(body);
        int p = body + 2;
        for (int i = 0; i < n; i++) {
            int target = r.readU1(p++);
            switch (target) {
                case 0x40:
                case 0x41: {
                    int k = r.readU2(p);
                    p += 2;
                    for (int j = 0; j < k; j++) {
                        int start = r.readU2(p);
                        code.getLabel(start);
                        code.getLabel(start + r.readU2(p + 2));
                        p += 6;
                    }
                    break;
                }
                case 0x42:
                    p += 2;
                    break;
                case 0x43:
                case 0x44:
                case 0x45:
                case 0x46:
                    code.getLabel(r.readU2(p));
                    p += 2;
                    break;
                case 0x47:
                case 0x48:
                case 0x49:
                case 0x4A:
                case 0x4B:
                    code.getLabel(r.readU2(p));
                    p += 3;
                    break;
                default:
                    p += targetInfoSize(target);
                    break;
            }
            p += 1 + 2 * r.readU1(p);
            p = skipAnnotation(r, p);
        }
    }

    private static int targetInfoSize(int target) {
        switch (target) {
            case 0x00:
            case 0x01:
            case 0x16:
                return 1;
            case 0x10:
            case 0x11:
            case 0x12:
            case 0x17:
                return 2;
            case 0x13:
            case 0x14:
            case 0x15:
                return 0;
            default:
                throw new IllegalArgumentException("Bad type annotation target: " + target);
        }
    }

    private static int skipAnnotation(ClassReaderImpl r, int p) {
        int pairs = r.readU2(p + 2);
        p += 4;
        for (int i = 0; i < pairs; i++) {
            p = skipElementValue(r, p + 2);
        }
        return p;
    }

    private static int skipElementValue(ClassReaderImpl r, int p) {
        int tag = r.readU1(p);
        switch (tag) {
            case 'e':
                return p + 5;
            case '@':
                return skipAnnotation(r, p + 1);
            case '[': {
                int n = r.readU2(p + 1);
                p += 3;
                for (int i = 0; i < n; i++) {
                    p = skipElementValue(r, p);
                }
                return p;
            }
            default:
                return p + 3;
        }
    }
}
