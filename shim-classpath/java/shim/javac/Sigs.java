/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 *
 * SPDX-License-Identifier: MIT
 */

package shim.javac;

import java.util.List;

/** Generic signature strings for the Signature attribute. */
final class Sigs {
  private Sigs() {
  }

  static boolean generic(Ty t) {
    t = TypeOps.resolve(t);
    switch (t.tag) {
      case Ty.TVAR:
      case Ty.WILD:
      case Ty.INTER:
        return true;
      case Ty.CLASS:
        return t.args.length > 0;
      case Ty.ARRAY:
        return generic(t.elem);
      default:
        return false;
    }
  }

  static String ty(Ty t) {
    t = TypeOps.resolve(t);
    switch (t.tag) {
      case Ty.CLASS: {
        StringBuilder sb = new StringBuilder("L").append(t.sym.internal);
        if (t.args.length > 0) {
          sb.append('<');
          for (Ty a : t.args) {
            sb.append(ty(a));
          }
          sb.append('>');
        }
        return sb.append(';').toString();
      }
      case Ty.ARRAY:
        return "[" + ty(t.elem);
      case Ty.TVAR:
        return "T" + t.tv.name + ";";
      case Ty.WILD:
        return t.wildKind == 0 ? "*" : (t.wildKind == 1 ? "+" : "-") + ty(t.bound);
      case Ty.INTER:
        return ty(t.args[0]);
      case Ty.IVAR:
      case Ty.NULL:
      case Ty.ERROR:
        return "Ljava/lang/Object;";
      default:
        return t.desc();
    }
  }

  static String tparams(List<TVSym> tps) {
    if (tps.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder("<");
    for (TVSym tv : tps) {
      sb.append(tv.name);
      List<Ty> bs = tv.bounds();
      boolean first = true;
      for (Ty b : bs) {
        Ty rb = TypeOps.resolve(b);
        boolean iface = rb.tag == Ty.CLASS && rb.sym.isInterface();
        if (first && iface) {
          sb.append(':');
        }
        sb.append(':').append(ty(b));
        first = false;
      }
      if (bs.isEmpty()) {
        sb.append(":Ljava/lang/Object;");
      }
    }
    return sb.append('>').toString();
  }

  static String classSig(CSym c) {
    boolean need = !c.typeParams().isEmpty();
    Ty sc = c.superclass();
    if (sc != null && generic(sc)) {
      need = true;
    }
    for (Ty i : c.interfaces()) {
      if (generic(i)) {
        need = true;
      }
    }
    if (!need) {
      return null;
    }
    StringBuilder sb = new StringBuilder(tparams(c.tparams));
    sb.append(c.isInterface() || sc == null ? "Ljava/lang/Object;" : ty(sc));
    for (Ty i : c.interfaces()) {
      sb.append(ty(i));
    }
    return sb.toString();
  }

  static String fieldSig(FSym f) {
    return generic(f.type) ? ty(f.type) : null;
  }

  static String methodSig(MSym m, CSym c) {
    boolean need = !m.tparams.isEmpty() || generic(m.ret);
    for (Ty p : m.params) {
      if (generic(p)) {
        need = true;
      }
    }
    boolean thrownGeneric = false;
    for (Ty t : m.thrown) {
      if (generic(t)) {
        need = true;
        thrownGeneric = true;
      }
    }
    if (!need) {
      return null;
    }
    StringBuilder sb = new StringBuilder(tparams(m.tparams)).append('(');
    for (Ty p : m.params) {
      sb.append(ty(p));
    }
    sb.append(')').append(m.isCtor() ? "V" : ty(m.ret));
    if (thrownGeneric) {
      for (Ty t : m.thrown) {
        sb.append('^').append(ty(t));
      }
    }
    return sb.toString();
  }
}
