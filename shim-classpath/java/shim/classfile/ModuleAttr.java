/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.classfile;

import java.lang.classfile.AttributeMapper.AttributeStability;
import java.lang.classfile.attribute.ModuleAttribute;
import java.lang.classfile.attribute.ModuleExportInfo;
import java.lang.classfile.attribute.ModuleOpenInfo;
import java.lang.classfile.attribute.ModuleProvideInfo;
import java.lang.classfile.attribute.ModuleRequireInfo;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.constantpool.ModuleEntry;
import java.lang.classfile.constantpool.PackageEntry;
import java.lang.classfile.constantpool.Utf8Entry;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ModuleAttr extends AttrImpl<ModuleAttribute> implements ModuleAttribute {
    public static final Mapper<ModuleAttribute> MAPPER = new Mapper<>("Module", AttributeStability.CP_REFS,
        (owner, r, p) -> new ModuleAttr(r, p), ModuleAttr::write);

    private ModuleEntry moduleName;
    private int flags;
    private Optional<Utf8Entry> version;
    private List<ModuleRequireInfo> requires;
    private List<ModuleExportInfo> exports;
    private List<ModuleOpenInfo> opens;
    private List<ClassEntry> uses;
    private List<ModuleProvideInfo> provides;

    public ModuleAttr(ModuleEntry moduleName, int flags, Utf8Entry version, Collection<ModuleRequireInfo> requires,
                      Collection<ModuleExportInfo> exports, Collection<ModuleOpenInfo> opens,
                      Collection<ClassEntry> uses, Collection<ModuleProvideInfo> provides) {
        super(MAPPER);
        this.moduleName = Objects.requireNonNull(moduleName);
        this.flags = flags;
        this.version = Optional.ofNullable(version);
        this.requires = List.copyOf(requires);
        this.exports = List.copyOf(exports);
        this.opens = List.copyOf(opens);
        this.uses = List.copyOf(uses);
        this.provides = List.copyOf(provides);
    }

    private ModuleAttr(ClassReaderImpl r, int p) {
        super(MAPPER, r, p);
    }

    private void load() {
        ClassReaderImpl r = reader;
        int q = pos + 6;
        int n = r.readU2(q);
        q += 2;
        ModuleRequireInfo[] req = new ModuleRequireInfo[n];
        for (int i = 0; i < n; i++, q += 6) {
            req[i] = new ModuleRequireInfoImpl(r.readEntry(q, ModuleEntry.class), r.readU2(q + 2),
                r.readEntryOrNull(q + 4, Utf8Entry.class));
        }
        requires = List.of(req);
        n = r.readU2(q);
        q += 2;
        ModuleExportInfo[] exp = new ModuleExportInfo[n];
        for (int i = 0; i < n; i++) {
            int count = r.readU2(q + 4);
            exp[i] = new ModuleExportInfoImpl(r.readEntry(q, PackageEntry.class), r.readU2(q + 2),
                AttrSupport.readEntries(r, q + 6, count, ModuleEntry.class));
            q += 6 + 2 * count;
        }
        exports = List.of(exp);
        n = r.readU2(q);
        q += 2;
        ModuleOpenInfo[] opn = new ModuleOpenInfo[n];
        for (int i = 0; i < n; i++) {
            int count = r.readU2(q + 4);
            opn[i] = new ModuleOpenInfoImpl(r.readEntry(q, PackageEntry.class), r.readU2(q + 2),
                AttrSupport.readEntries(r, q + 6, count, ModuleEntry.class));
            q += 6 + 2 * count;
        }
        opens = List.of(opn);
        n = r.readU2(q);
        uses = AttrSupport.readEntries(r, q + 2, n, ClassEntry.class);
        q += 2 + 2 * n;
        n = r.readU2(q);
        q += 2;
        provides = List.of();
        ModuleProvideInfo[] prv = new ModuleProvideInfo[n];
        for (int i = 0; i < n; i++) {
            int count = r.readU2(q + 2);
            prv[i] = new ModuleProvideInfoImpl(r.readEntry(q, ClassEntry.class),
                AttrSupport.readEntries(r, q + 4, count, ClassEntry.class));
            q += 4 + 2 * count;
        }
        provides = List.of(prv);
    }

    @Override
    public ModuleEntry moduleName() {
        if (moduleName == null) {
            moduleName = reader.readEntry(pos, ModuleEntry.class);
        }
        return moduleName;
    }

    @Override
    public int moduleFlagsMask() {
        return reader == null ? flags : reader.readU2(pos + 2);
    }

    @Override
    public Optional<Utf8Entry> moduleVersion() {
        if (version == null) {
            version = Optional.ofNullable(reader.readEntryOrNull(pos + 4, Utf8Entry.class));
        }
        return version;
    }

    @Override
    public List<ModuleRequireInfo> requires() {
        if (requires == null) {
            load();
        }
        return requires;
    }

    @Override
    public List<ModuleExportInfo> exports() {
        if (exports == null) {
            load();
        }
        return exports;
    }

    @Override
    public List<ModuleOpenInfo> opens() {
        if (opens == null) {
            load();
        }
        return opens;
    }

    @Override
    public List<ClassEntry> uses() {
        if (uses == null) {
            load();
        }
        return uses;
    }

    @Override
    public List<ModuleProvideInfo> provides() {
        if (provides == null) {
            load();
        }
        return provides;
    }

    private static void write(BufWriterImpl out, ModuleAttribute a) {
        if (writeDirect(out, a)) {
            return;
        }
        out.writeIndex(a.moduleName());
        out.writeU2(a.moduleFlagsMask());
        out.writeIndexOrZero(a.moduleVersion().orElse(null));
        out.writeU2(a.requires().size());
        for (ModuleRequireInfo e : a.requires()) {
            out.writeIndex(e.requires());
            out.writeU2(e.requiresFlagsMask());
            out.writeIndexOrZero(e.requiresVersion().orElse(null));
        }
        out.writeU2(a.exports().size());
        for (ModuleExportInfo e : a.exports()) {
            out.writeIndex(e.exportedPackage());
            out.writeU2(e.exportsFlagsMask());
            AttrSupport.writeEntryList(out, e.exportsTo());
        }
        out.writeU2(a.opens().size());
        for (ModuleOpenInfo e : a.opens()) {
            out.writeIndex(e.openedPackage());
            out.writeU2(e.opensFlagsMask());
            AttrSupport.writeEntryList(out, e.opensTo());
        }
        AttrSupport.writeEntryList(out, a.uses());
        out.writeU2(a.provides().size());
        for (ModuleProvideInfo e : a.provides()) {
            out.writeIndex(e.provides());
            AttrSupport.writeEntryList(out, e.providesWith());
        }
    }
}
