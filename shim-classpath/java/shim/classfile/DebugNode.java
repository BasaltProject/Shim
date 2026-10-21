/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A node of the tree that {@code toDebugString} renders as YAML. */
final class DebugNode {
    static final int LEAF = 0;
    static final int LIST = 1;
    static final int MAP = 2;

    private static final String NL = System.lineSeparator();
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    final int kind;
    final boolean block;
    final Object name;
    final Object value;
    final List<DebugNode> items;
    final Map<Object, DebugNode> entries;
    private final boolean merged;

    private DebugNode(int kind, boolean block, Object name, Object value, boolean merged) {
        this.kind = kind;
        this.block = block;
        this.name = name;
        this.value = value;
        this.merged = merged;
        this.items = kind == LIST ? new ArrayList<DebugNode>() : null;
        this.entries = kind == MAP ? new LinkedHashMap<Object, DebugNode>() : null;
    }

    static DebugNode leaf(Object name, Object value) {
        return new DebugNode(LEAF, false, name, value, false);
    }

    static DebugNode blockList(Object name) {
        return new DebugNode(LIST, true, name, null, false);
    }

    static DebugNode flowList(Object name) {
        return new DebugNode(LIST, false, name, null, false);
    }

    static DebugNode flowList(Object name, Iterable<?> values) {
        DebugNode n = flowList(name);
        for (Object v : values) {
            n.items.add(leaf(name, v));
        }
        return n;
    }

    static DebugNode blockMap(Object name) {
        return new DebugNode(MAP, true, name, null, false);
    }

    static DebugNode flowMap(Object name) {
        return new DebugNode(MAP, false, name, null, false);
    }

    DebugNode add(DebugNode item) {
        items.add(item);
        return this;
    }

    DebugNode with(DebugNode n) {
        if (n == null) {
            return this;
        }
        DebugNode old = entries.get(n.name);
        if (old == null) {
            entries.put(n.name, n);
        } else if (old.merged) {
            old.items.add(n);
        } else {
            DebugNode list = new DebugNode(LIST, true, n.name, null, true);
            list.items.add(old);
            list.items.add(n);
            entries.put(n.name, list);
        }
        return this;
    }

    DebugNode with(Object name, Object value) {
        return with(leaf(name, value));
    }

    DebugNode withAll(List<DebugNode> nodes) {
        for (DebugNode n : nodes) {
            with(n);
        }
        return this;
    }

    /** Returns the map entry, failing like a cast when duplicates were merged into a list. */
    DebugNode mapEntry(Object key) {
        DebugNode n = entries.get(key);
        if (n != null && n.merged) {
            throw new ClassCastException("class jdk.internal.classfile.impl.ClassPrinterImpl$MapNodeImpl"
                    + "$PrivateListNodeImpl cannot be cast to class jdk.internal.classfile.impl.ClassPrinterImpl"
                    + "$MapNodeImpl (jdk.internal.classfile.impl.ClassPrinterImpl$MapNodeImpl$PrivateListNodeImpl"
                    + " and jdk.internal.classfile.impl.ClassPrinterImpl$MapNodeImpl are in module java.base of"
                    + " loader 'bootstrap')");
        }
        return n;
    }

    static String render(DebugNode root) {
        StringBuilder sb = new StringBuilder();
        DebugNode top = blockList("root");
        top.items.add(root);
        top.print(sb, 0, false);
        sb.append(NL);
        return sb.toString();
    }

    private void print(StringBuilder sb, int depth, boolean first) {
        switch (kind) {
            case LEAF:
                sb.append(quote(value));
                break;
            case LIST:
                if (block) {
                    for (DebugNode n : items) {
                        sb.append(NL);
                        indent(sb, depth);
                        sb.append("  - ");
                        n.print(sb, depth + 1, true);
                    }
                } else {
                    sb.append('[');
                    for (int i = 0; i < items.size(); i++) {
                        if (i > 0) {
                            sb.append(", ");
                        }
                        items.get(i).print(sb, 0, false);
                    }
                    sb.append(']');
                }
                break;
            default:
                if (block) {
                    for (DebugNode n : entries.values()) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(NL);
                            indent(sb, depth);
                        }
                        sb.append(quote(n.name)).append(": ");
                        n.print(sb, n.kind == LIST && n.block ? depth : depth + 1, false);
                    }
                } else {
                    sb.append('{');
                    Iterator<DebugNode> it = entries.values().iterator();
                    while (it.hasNext()) {
                        DebugNode n = it.next();
                        sb.append(quote(n.name)).append(": ");
                        n.print(sb, 0, false);
                        if (it.hasNext()) {
                            sb.append(", ");
                        }
                    }
                    sb.append('}');
                }
        }
    }

    private static void indent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append("    ");
        }
    }

    static String quote(Object value) {
        String s = String.valueOf(value);
        if (value == null || value instanceof Number) {
            return s;
        }
        if (s.isEmpty()) {
            return "''";
        }
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                sb.append("''");
            } else {
                escape(c, sb);
            }
        }
        String esc = sb.toString();
        if (esc.length() != s.length()) {
            return "'" + esc + "'";
        }
        switch (esc.charAt(0)) {
            case '-': case '?': case ':': case ',': case '[': case ']': case '{': case '}': case '#': case '&':
            case '*': case '!': case '|': case '>': case '\'': case '"': case '%': case '@': case '`':
                return "'" + esc + "'";
            default:
                break;
        }
        for (int i = 1; i < esc.length(); i++) {
            switch (esc.charAt(i)) {
                case ',': case '[': case ']': case '{': case '}':
                    return "'" + esc + "'";
                default:
                    break;
            }
        }
        return "null".equalsIgnoreCase(esc) ? "'" + esc + "'" : esc;
    }

    private static void escape(char c, StringBuilder sb) {
        switch (c) {
            case '\\': sb.append("\\\\"); break;
            case '"': sb.append("\\\""); break;
            case '\b': sb.append("\\b"); break;
            case '\n': sb.append("\\n"); break;
            case '\t': sb.append("\\t"); break;
            case '\f': sb.append("\\f"); break;
            case '\r': sb.append("\\r"); break;
            default:
                if (c >= 0x20 && c < 0x7f) {
                    sb.append(c);
                } else {
                    sb.append("\\u").append(HEX[(c >> 12) & 0xf]).append(HEX[(c >> 8) & 0xf])
                            .append(HEX[(c >> 4) & 0xf]).append(HEX[c & 0xf]);
                }
        }
    }
}
