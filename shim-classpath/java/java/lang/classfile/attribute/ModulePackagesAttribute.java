/*
 * Copyright (c) 2026 BasaltMC.
 *
 * Licensed under the MIT License; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.classfile.attribute;

import java.lang.classfile.Attribute;
import java.lang.classfile.ClassElement;
import java.lang.classfile.constantpool.PackageEntry;
import java.lang.constant.PackageDesc;
import java.util.Arrays;
import java.util.List;

public interface ModulePackagesAttribute extends Attribute<ModulePackagesAttribute>, ClassElement {
    List<PackageEntry> packages();

    static ModulePackagesAttribute of(List<PackageEntry> packages) {
        return new shim.classfile.ModulePackagesAttr(packages);
    }

    static ModulePackagesAttribute of(PackageEntry... packages) {
        return of(Arrays.asList(packages));
    }

    static ModulePackagesAttribute ofNames(List<PackageDesc> packages) {
        return of(shim.classfile.AttrSupport.packageEntries(packages));
    }

    static ModulePackagesAttribute ofNames(PackageDesc... packages) {
        return ofNames(Arrays.asList(packages));
    }
}
