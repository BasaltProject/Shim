/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package java.lang.module;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

final class ModuleInfoParser {
  private final DataInputStream in;
  private Object[] pool;

  private ModuleInfoParser(InputStream is) {
    this.in = new DataInputStream(is);
  }

  static ModuleDescriptor read(InputStream is, Supplier<Set<String>> packageFinder) throws IOException {
    return new ModuleInfoParser(is).parse(packageFinder);
  }

  private static InvalidModuleDescriptorException invalid(String msg) {
    return new InvalidModuleDescriptorException(msg);
  }

  private String utf(int i) {
    return (String) pool[i];
  }

  private String ref(int i) {
    return utf((Integer) pool[i]);
  }

  private String binaryName(int i) {
    return ref(i).replace('/', '.');
  }

  private ModuleDescriptor parse(Supplier<Set<String>> packageFinder) throws IOException {
    if (in.readInt() != 0xCAFEBABE) {
      throw invalid("Bad magic number");
    }
    in.readUnsignedShort();
    in.readUnsignedShort();
    int n = in.readUnsignedShort();
    pool = new Object[n];
    for (int i = 1; i < n; i++) {
      int tag = in.readUnsignedByte();
      switch (tag) {
      case 1:
        pool[i] = in.readUTF();
        break;
      case 3:
      case 4:
        in.readInt();
        break;
      case 5:
      case 6:
        in.readLong();
        i++;
        break;
      case 7:
      case 8:
      case 16:
      case 19:
      case 20:
        pool[i] = Integer.valueOf(in.readUnsignedShort());
        break;
      case 9:
      case 10:
      case 11:
      case 12:
      case 17:
      case 18:
        in.readInt();
        break;
      case 15:
        in.readUnsignedByte();
        in.readUnsignedShort();
        break;
      default:
        throw invalid("Unknown constant pool tag " + tag);
      }
    }
    int access = in.readUnsignedShort();
    if (access != 0x8000) {
      throw invalid("access_flags should be ACC_MODULE");
    }
    in.readUnsignedShort();
    in.readUnsignedShort();
    int ifaces = in.readUnsignedShort();
    for (int i = 0; i < ifaces; i++) {
      in.readUnsignedShort();
    }
    for (int k = 0; k < 2; k++) {
      int count = in.readUnsignedShort();
      for (int i = 0; i < count; i++) {
        in.readUnsignedShort();
        in.readUnsignedShort();
        in.readUnsignedShort();
        skipAttributes();
      }
    }
    ModuleDescriptor.Builder b = null;
    Set<String> allPackages = null;
    String mainClass = null;
    int attrs = in.readUnsignedShort();
    for (int a = 0; a < attrs; a++) {
      String name = utf(in.readUnsignedShort());
      int len = in.readInt();
      if (name.equals("Module")) {
        b = module();
      } else if (name.equals("ModulePackages")) {
        allPackages = new HashSet<String>();
        int c = in.readUnsignedShort();
        for (int i = 0; i < c; i++) {
          allPackages.add(binaryName(in.readUnsignedShort()));
        }
      } else if (name.equals("ModuleMainClass")) {
        mainClass = binaryName(in.readUnsignedShort());
      } else {
        in.skipNBytes(len);
      }
    }
    if (b == null) {
      throw invalid("Module attribute not found");
    }
    if (mainClass != null) {
      b.mainClass(mainClass);
    }
    if (allPackages == null && packageFinder != null) {
      try {
        allPackages = packageFinder.get();
      } catch (UncheckedIOException e) {
        throw e.getCause();
      }
    }
    if (allPackages != null) {
      for (String pn : new java.util.TreeSet<String>(b.currentPackages())) {
        if (!allPackages.contains(pn)) {
          throw invalid("Package " + pn + " not found in module");
        }
      }
      b.packages(allPackages);
    }
    return b.build();
  }

  private void skipAttributes() throws IOException {
    int c = in.readUnsignedShort();
    for (int i = 0; i < c; i++) {
      in.readUnsignedShort();
      in.skipNBytes(in.readInt());
    }
  }

  private String modName(int i) {
    return utf((Integer) pool[i]);
  }

  private ModuleDescriptor.Builder module() throws IOException {
    String name = modName(in.readUnsignedShort());
    int flags = in.readUnsignedShort();
    int ver = in.readUnsignedShort();
    Set<ModuleDescriptor.Modifier> mods = EnumSet.noneOf(ModuleDescriptor.Modifier.class);
    if ((flags & 0x20) != 0) {
      mods.add(ModuleDescriptor.Modifier.OPEN);
    }
    if ((flags & 0x1000) != 0) {
      mods.add(ModuleDescriptor.Modifier.SYNTHETIC);
    }
    if ((flags & 0x8000) != 0) {
      mods.add(ModuleDescriptor.Modifier.MANDATED);
    }
    ModuleDescriptor.Builder b = ModuleDescriptor.builderFor(name, mods, false);
    if (ver != 0) {
      b.version(utf(ver));
    }
    int rc = in.readUnsignedShort();
    for (int i = 0; i < rc; i++) {
      String r = modName(in.readUnsignedShort());
      int rf = in.readUnsignedShort();
      int rv = in.readUnsignedShort();
      Set<ModuleDescriptor.Requires.Modifier> ms = EnumSet.noneOf(ModuleDescriptor.Requires.Modifier.class);
      if ((rf & 0x20) != 0) {
        ms.add(ModuleDescriptor.Requires.Modifier.TRANSITIVE);
      }
      if ((rf & 0x40) != 0) {
        ms.add(ModuleDescriptor.Requires.Modifier.STATIC);
      }
      if ((rf & 0x1000) != 0) {
        ms.add(ModuleDescriptor.Requires.Modifier.SYNTHETIC);
      }
      if ((rf & 0x8000) != 0) {
        ms.add(ModuleDescriptor.Requires.Modifier.MANDATED);
      }
      if (rv != 0) {
        b.requires(ms, r, utf(rv));
      } else {
        b.requires(ms, r);
      }
    }
    int ec = in.readUnsignedShort();
    for (int i = 0; i < ec; i++) {
      String pn = ref(in.readUnsignedShort()).replace('/', '.');
      int ef = in.readUnsignedShort();
      Set<ModuleDescriptor.Exports.Modifier> ms = EnumSet.noneOf(ModuleDescriptor.Exports.Modifier.class);
      if ((ef & 0x1000) != 0) {
        ms.add(ModuleDescriptor.Exports.Modifier.SYNTHETIC);
      }
      if ((ef & 0x8000) != 0) {
        ms.add(ModuleDescriptor.Exports.Modifier.MANDATED);
      }
      int tc = in.readUnsignedShort();
      Set<String> targets = new HashSet<String>();
      for (int j = 0; j < tc; j++) {
        targets.add(modName(in.readUnsignedShort()));
      }
      if (targets.isEmpty()) {
        b.exports(ms, pn);
      } else {
        b.exports(ms, pn, targets);
      }
    }
    int oc = in.readUnsignedShort();
    for (int i = 0; i < oc; i++) {
      String pn = ref(in.readUnsignedShort()).replace('/', '.');
      int of = in.readUnsignedShort();
      Set<ModuleDescriptor.Opens.Modifier> ms = EnumSet.noneOf(ModuleDescriptor.Opens.Modifier.class);
      if ((of & 0x1000) != 0) {
        ms.add(ModuleDescriptor.Opens.Modifier.SYNTHETIC);
      }
      if ((of & 0x8000) != 0) {
        ms.add(ModuleDescriptor.Opens.Modifier.MANDATED);
      }
      int tc = in.readUnsignedShort();
      Set<String> targets = new HashSet<String>();
      for (int j = 0; j < tc; j++) {
        targets.add(modName(in.readUnsignedShort()));
      }
      if (targets.isEmpty()) {
        b.opens(ms, pn);
      } else {
        b.opens(ms, pn, targets);
      }
    }
    int uc = in.readUnsignedShort();
    for (int i = 0; i < uc; i++) {
      b.uses(binaryName(in.readUnsignedShort()));
    }
    int pc = in.readUnsignedShort();
    for (int i = 0; i < pc; i++) {
      String svc = binaryName(in.readUnsignedShort());
      int c = in.readUnsignedShort();
      List<String> impls = new ArrayList<String>();
      for (int j = 0; j < c; j++) {
        impls.add(binaryName(in.readUnsignedShort()));
      }
      b.provides(svc, impls);
    }
    return b;
  }
}
