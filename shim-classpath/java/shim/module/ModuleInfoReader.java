/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.module;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.InvalidModuleDescriptorException;
import java.lang.module.ModuleDescriptor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ModuleInfoReader {
  private final DataInputStream in;
  private Object[] pool;
  private int[] tags;

  private ModuleInfoReader(InputStream is) {
    this.in = new DataInputStream(is);
  }

  public static ModuleDescriptor read(InputStream is, Set<String> extraPackages) throws IOException {
    return new ModuleInfoReader(is).parse(extraPackages);
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

  private ModuleDescriptor parse(Set<String> extra) throws IOException {
    if (in.readInt() != 0xCAFEBABE) {
      throw new InvalidModuleDescriptorException("Bad magic number");
    }
    in.readUnsignedShort();
    in.readUnsignedShort();
    int n = in.readUnsignedShort();
    pool = new Object[n];
    tags = new int[n];
    for (int i = 1; i < n; i++) {
      int tag = in.readUnsignedByte();
      tags[i] = tag;
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
        throw new InvalidModuleDescriptorException("Unknown constant pool tag " + tag);
      }
    }
    int access = in.readUnsignedShort();
    if (access != 0x8000) {
      throw new InvalidModuleDescriptorException("access_flags should be ACC_MODULE");
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
    Set<String> packages = new HashSet<String>();
    String mainClass = null;
    int attrs = in.readUnsignedShort();
    for (int a = 0; a < attrs; a++) {
      String name = utf(in.readUnsignedShort());
      int len = in.readInt();
      if (name.equals("Module")) {
        b = module();
      } else if (name.equals("ModulePackages")) {
        int c = in.readUnsignedShort();
        for (int i = 0; i < c; i++) {
          packages.add(binaryName(in.readUnsignedShort()));
        }
      } else if (name.equals("ModuleMainClass")) {
        mainClass = binaryName(in.readUnsignedShort());
      } else {
        in.skipNBytes(len);
      }
    }
    if (b == null) {
      throw new InvalidModuleDescriptorException("Module attribute not found");
    }
    if (mainClass != null) {
      b.mainClass(mainClass);
    }
    packages.addAll(extra);
    b.packages(packages);
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
    ModuleDescriptor.Builder b = (flags & 0x20) != 0 ? ModuleDescriptor.newOpenModule(name)
        : ModuleDescriptor.newModule(name);
    if (ver != 0) {
      b.version(utf(ver));
    }
    int rc = in.readUnsignedShort();
    for (int i = 0; i < rc; i++) {
      String r = modName(in.readUnsignedShort());
      int rf = in.readUnsignedShort();
      int rv = in.readUnsignedShort();
      Set<ModuleDescriptor.Requires.Modifier> ms = new HashSet<ModuleDescriptor.Requires.Modifier>();
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
      b.requires(ms, r);
    }
    int ec = in.readUnsignedShort();
    for (int i = 0; i < ec; i++) {
      String pn = ref(in.readUnsignedShort()).replace('/', '.');
      int ef = in.readUnsignedShort();
      Set<ModuleDescriptor.Exports.Modifier> ms = new HashSet<ModuleDescriptor.Exports.Modifier>();
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
      b.exports(ms, pn, targets);
    }
    int oc = in.readUnsignedShort();
    for (int i = 0; i < oc; i++) {
      String pn = ref(in.readUnsignedShort()).replace('/', '.');
      int of = in.readUnsignedShort();
      Set<ModuleDescriptor.Opens.Modifier> ms = new HashSet<ModuleDescriptor.Opens.Modifier>();
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
      b.opens(ms, pn, targets);
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
